(ns front-end.views
  (:require
   [re-frame.core :as re-frame]
   [front-end.subs :as subs]
   ))

(defn main-panel []
  (let [name (re-frame/subscribe [::subs/name])]
    [:div
     [:h1
      "Hello from " @name]
     ]))

(defn about-page []
  [:div
   [:h1  "This is about page..."]])
