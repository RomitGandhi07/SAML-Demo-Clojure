(ns saml-backend.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[saml-backend started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[saml-backend has shut down successfully]=-"))
   :middleware identity})
