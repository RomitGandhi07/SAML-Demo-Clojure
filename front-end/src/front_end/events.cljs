(ns front-end.events
  (:require
   [re-frame.core :as re-frame]
   [front-end.db :as db]
   ))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))
