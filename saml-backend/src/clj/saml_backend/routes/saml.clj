(ns saml-backend.routes.saml
  (:require [clojure.string :as s]
            [saml-backend.handlers.saml :as saml]))


(defn saml-routes
  []
  ["/saml"
   {:swagger {:tags ["SAML"]}}
   [""
    {:get {:summary "API endpoint which sends SAML AuthRequest to IDP"
           :handler saml/create-saml-authn-request}}]
   ["/callback"
    {:post {:summary "API endpoint which receives SAML Response from IDP"
            :handler saml/saml-response-callback}}]
   ["/return"
    {:get {:summary "API endpoint"
           :handler saml/saml-return}}]])
