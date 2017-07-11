(ns voidwalker.app
  (:require [voidwalker.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
