(ns front-end.events.login
  (:require [re-frame.core :as rf]
            [ajax.core :as ajax]))

(rf/reg-event-fx
 :login
 (fn [cofx]
   {:db (:db cofx)}))

(rf/reg-event-fx
 :sso-login-succes
 (fn [cofx [_ name-id]]
   {:db (assoc (:db cofx) :token name-id)
    :navigate! [:routes/home]}))