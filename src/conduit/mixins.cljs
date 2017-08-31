(ns conduit.mixins
  (:require [rum.core :as rum]
            [citrus.core :as citrus]))

(defn dispatch-on-mount [events-fn]
  {:did-mount
   (fn [{[r] :rum/args :as state}]
     (doseq [[ctrl event-vector] (apply events-fn (:rum/args state))]
       (apply citrus/dispatch! (into [r ctrl] event-vector)))
     state)
   :did-remount
   (fn [old {[r] :rum/args :as state}]
     (when (not= (:rum/args old) (:rum/args state))
       (doseq [[ctrl event-vector] (apply events-fn (:rum/args state))]
         (apply citrus/dispatch! (into [r ctrl] event-vector))))
     state)})

(defn form-state [{:keys [fields validators submit-handler]}]
  {:will-mount
   (fn [{[r _ params] :rum/args
         :as state}]
     (let [form-data (atom (->> fields (map :key) (reduce #(assoc %1 %2 "") {})))
           form-errors (atom (->> fields (map :key) (reduce #(assoc %1 %2 nil) {})))]
       (assoc state
              :form-fields fields
              :form-validators validators
              :form-submit-handler (submit-handler r form-data form-errors validators)
              :form-data form-data
              :form-errors form-errors)))})
