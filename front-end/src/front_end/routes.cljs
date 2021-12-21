(ns front-end.routes
  (:require [re-frame.core :as rf]
            [reitit.coercion.malli]
            [reitit.frontend]
            [reitit.frontend.easy :as rfe]
            [reitit.frontend.controllers :as rfc]
            [front-end.views :as views]
            [front-end.pages.login :refer [login]]
            [front-end.events.login]))

;; Subs

(rf/reg-sub
  :current-route
  (fn [db]
      (:current-route db)))

;;; Events

(rf/reg-event-fx
  :navigate
  (fn [_cofx [_ & route]]
      {:navigate! route}))

;; Triggering navigation from events.
(rf/reg-fx
  :navigate!
  (fn [route]
      (apply rfe/push-state route)))


(rf/reg-event-db
  :navigated
  (fn [db [_ new-match]]
      (let [old-match   (:current-route db)
            controllers (rfc/apply-controllers (:controllers old-match) new-match)]
           (assoc db :current-route (assoc new-match :controllers controllers)))))

;;; Routes

(def routes
  ["/"
   [""
    {:name      :routes/home
     :view      views/main-panel
     :link-text "Home"}]
   ["about"
    {:name      :routes/about
     :view      views/about-page
     :link-text "About"}]
   ["login"
    {:name      :routes/login
     :view      login
     :link-text "Login"}]
   ])

(def router
  (reitit.frontend/router
    routes
    {:data {:coercion reitit.coercion.malli/coercion}}))

(defn on-navigate [new-match]
      (when new-match
            (rf/dispatch [:navigated new-match])))

(defn init-routes! []
      ;;(js/console.log "initializing routes")
      (rfe/start!
        router
        on-navigate
        {:use-fragment true}))