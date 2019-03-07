(ns voidwalker.source.util
  (:require [bide.core :as b]))

(defn p [whatever]
  (println "from whatever" whatever)
  whatever)


(defn prompt! [message]
  (.prompt js/window message))

(defn num [st]
  (println "num" st)
  (when (not= nil st) (.parseInt js/window st)))


(defn get-value [e]
  (-> e .-target .-value))

(defn keycode [e]
  (or (.-which e)
      (.-keyCode e)))
