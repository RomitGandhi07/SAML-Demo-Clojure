(ns saml-backend.handlers.saml
  (:require [saml20-clj.routes :as saml-routes]
            [saml20-clj.sp :as saml-sp]
            [saml20-clj.shared :as saml-shared]
            [clojure.string :as s]
            [ring.util.http-response :refer :all]
            [hiccup.core :refer [html]]))

(defn- parse-certificate
  "Strip the ---BEGIN CERTIFICATE--- and ---END CERTIFICATE--- headers and newlines
  from certificate."
  [certstring]
  (->> (s/split certstring #"\n") rest drop-last s/join))

(def ^:private config
         {:app-name "http://localhost:7171/saml"
          :base-uri "http://localhost:7171"
          :idp-uri "https://dev-eblvw5vy.us.auth0.com/samlp/ZtFqFPfnwS7jp5JNhGkeAgh2Vp1PT7Ma"
          :idp-logout-uri "https://dev-eblvw5vy.us.auth0.com/samlp/ZtFqFPfnwS7jp5JNhGkeAgh2Vp1PT7Ma/logout"
          :idp-cert (parse-certificate (slurp "./Clojure SAML Demo Cert.pem"))
          :keystore-file "keystore.jks"
          :keystore-password (System/getenv "KEYSTORE_PASS")
          :key-alias "mylocalsp"})

(defn generate
  []
  (let [mutables (saml-sp/generate-mutables)
        acs-uri (str (:base-uri config) "/api/saml/callback")]
    {:decrypter nil
     :mutables mutables
     :acs-uri acs-uri
     :saml-req-factory! (saml-sp/create-request-factory mutables
                                                        (:idp-uri config)
                                                        saml-routes/saml-format
                                                        (:app-name config)
                                                        acs-uri)}))

(def config1 (generate))

(defn create-saml-authn-request
  [_]
  (let [saml-request ((:saml-req-factory! config1))
        hmac-relay-state (saml-routes/create-hmac-relay-state (:secret-key-spec (:mutables config1)) "target")
        ]
    (saml-sp/get-idp-redirect (:idp-uri config) saml-request hmac-relay-state)))

(defn saml-response-callback
  [{params :params}]
  (let [xml-respnose (saml-shared/base64->inflate->str (:SAMLResponse params))
        relay-state (:RelayState params)
        [valid-relay-state? continue-url] (saml-routes/valid-hmac-relay-state? (:secret-key-spec (:mutables config1)) relay-state)
        saml-resp (saml-sp/xml-string->saml-resp xml-respnose)
        valid-signature? (if (:idp-cert config)
                           (saml-sp/validate-saml-response-signature saml-resp (:idp-cert config))
                           false)
        valid? (and valid-relay-state? valid-signature?)
        saml-info (and valid? (saml-sp/saml-resp->assertions saml-resp nil))]
    (clojure.pprint/pprint saml-info)
    (if valid?
      {:status 302
       :headers {"Location" (str "http://localhost:8280/saml/return?" (saml-shared/uri-query-str
                                                                              {:token
                                                                               (-> saml-info
                                                                                   :assertions
                                                                                   (first)
                                                                                   :name-id
                                                                                   :value)}))}
       :body    ""}
      {:status 400
       :body "SAML response does not validated"})))

(defn saml-return
  [_]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "<script>
            document.domain = \"http://localhost:8280/\"
          </script>"})
