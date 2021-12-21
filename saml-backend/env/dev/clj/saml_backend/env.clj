(ns saml-backend.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [saml-backend.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[saml-backend started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[saml-backend has shut down successfully]=-"))
   :middleware wrap-dev})
