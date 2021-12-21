(ns front-end.core
  (:require
   [day8.re-frame.http-fx]
   [reagent.dom :as rdom]
   [re-frame.core :as re-frame]
   [front-end.events :as events]
   [front-end.views :as views]
   [front-end.config :as config]
   [front-end.routes :as routes]))


(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn main-page
      []
      (let [current-route @(re-frame/subscribe [:current-route])]
           [:div
            (when current-route
                  [(-> current-route :data :view)])]))

(defn root []
      [:div#root
       {:style {:width "100vw"}}
       [main-page]])

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
      (routes/init-routes!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [root] root-el)))

(defn init []
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))
