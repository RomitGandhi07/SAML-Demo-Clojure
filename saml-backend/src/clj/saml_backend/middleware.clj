(ns saml-backend.middleware
  (:require
    [saml-backend.env :refer [defaults]]
    [saml-backend.config :refer [env]]
    [ring.middleware.flash :refer [wrap-flash]]
    [ring.adapter.undertow.middleware.session :refer [wrap-session]]
    [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
    [ring.middleware.cors :refer [wrap-cors]])
  )

(defn wrap-base [handler]
  (-> ((:middleware defaults) handler)
      wrap-flash
      (wrap-cors :access-control-allow-origin #"http://localhost:8280" :access-control-allow-methods [:get :put :post :delete])
      (wrap-session {:cookie-attrs {:http-only true}})
      (wrap-defaults
        (-> site-defaults
            (assoc-in [:security :anti-forgery] false)
            (dissoc :session)))))
