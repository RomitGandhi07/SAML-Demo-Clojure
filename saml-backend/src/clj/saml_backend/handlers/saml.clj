(ns saml-backend.handlers.saml
  (:require [saml20-clj.core :as saml]
            [saml20-clj.sp.request :refer [uri-query-str]]
            [saml20-clj.encode-decode :as saml-decode]
            [clojure.string :as s]
            [ring.util.http-response :refer :all]
            [hiccup.core :refer [html]]))

(defn- parse-certificate
  "Strip the ---BEGIN CERTIFICATE--- and ---END CERTIFICATE--- headers and newlines
  from certificate."
  [certstring]
  (->> (s/split certstring #"\n") rest drop-last s/join))

(def ^:private config
  {:sp-name "http://localhost:7171/saml"
   :idp-url "https://dev-eblvw5vy.us.auth0.com/samlp/ZtFqFPfnwS7jp5JNhGkeAgh2Vp1PT7Ma"
   :idp-cert (parse-certificate (slurp "./Clojure SAML Demo Cert.pem"))
   :issuer  "http://localhost:7171/saml"
   :acs-url "http://localhost:7171/api/saml/callback"
   :slo-url "https://my-app.com/saml/logout"})


(defn create-saml-authn-request
  [_]
  (-> (saml/request
        {:sp-name (:sp-name config)
         :acs-url (:acs-url config)
         :idp-url (:idp-url config)
         :issuer  (:issuer config)})
      (saml/idp-redirect-response
        (:idp-url config)
        ""))
  )

(defn saml-response-callback
  [{params :params}]
  (try
    (let [assertions (-> params
                         :SAMLResponse
                         saml-decode/base64->str
                         saml/->Response
                         (saml/validate (:idp-cert config) nil {:acs-url (:acs-url config)})
                         saml/assertions)]
      (if assertions
        {:status 302
         :headers {"Location" (str "http://localhost:8280/saml/return?" (-> (uri-query-str
                                                                              {:token (-> assertions
                                                                                          (first)
                                                                                          :name-id
                                                                                          :value)})))}
         :body    ""}
        {:status 400
         :headers {"Content-Type" "text/html"}
         :body "Something went wrong... Please try again"}))
    (catch Exception e
      {:status 400
       :headers {"Content-Type" "text/html"}
       :body "Something went wrong... Please try again"})))

(defn saml-return
  [_]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "<script>
            document.domain = \"http://localhost:8280/\"
          </script>"})
