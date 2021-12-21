(ns front-end.pages.login
  (:require [re-frame.core :as rf]
            [clojure.string :as s]))

(defn url-from-query-params
  [url]
  (let [q-params (-> url 
                     (s/split #"\?") 
                     (second)
                     (s/split #"&"))]
    (loop [q q-params
           query-params (transient {})]
      (if (empty? q)
        (persistent! query-params)
        (let [pair (s/split (first q) #"=")]
          (recur
           (rest q)
           (assoc! query-params (first pair) (second pair))))))))

(defn saml-login
  []
  (let [win (js/window.open "http://localhost:7171/api/saml" 
                            "SAMLAuth" "width=800, height=600")
        interval (atom nil)]
    (reset! interval (js/window.setInterval
                      (fn []
                        (try
                          (let [url (if (and win
                                             (.-document win)
                                             (-> win .-document .-URL))
                                      (-> win .-document .-URL)
                                      "")]
                            (cond
                              ;; If URL matches return URL then close dialog and save the token
                              (s/includes? url 
                                           "http://localhost:8280/saml/return"
                                           ;;"http://localhost:7171/api/saml/return"
                                           )
                              (do
                                (js/console.log "Matched")
                                (.close win)
                                (println (url-from-query-params "abc.com?a=1&b=2&c=3&d=4&e=5"))
                                (println (url-from-query-params url))
                                (js/window.clearInterval @interval)
                                (rf/dispatch [:sso-login-succes (get (url-from-query-params url) "token")]))
                              ;; If dialog is already closed then clear the interval
                              (.-closed win)
                              (do
                                (js/console.log "Closed")
                                (js/window.clearInterval @interval))))
                          (catch js/Error e
                            (when (.-closed win)
                              (js/window.clearInterval @interval)))))
                      500))
    ))

(defn login
  []
  [:div.container
   [:div.row.mb-3
    [:h2.line "Login"]]
   [:div.row
    [:div.col-md-6.col-sm-12.col-xs-12
     [:form
      [:div.form-group.mb-3
       [:input.form-control {:type :email
                             :placeholder "Enter email"}]]
      [:div.form-group.mb-3
       [:input.form-control {:type :password
                             :placeholder "Enter password"}]]
      [:div.d-flex.justify-content-between.mb-3
       [:button.btn.btn-primary
         {:on-click (fn [e]
                      (.preventDefault e))}
        "Login"]
       [:button.btn.btn-primary
        {:on-click (fn [e]
                     (.preventDefault e)
                     (saml-login))}
        "SSO"]]]]]])