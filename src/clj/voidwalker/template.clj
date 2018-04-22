(ns voidwalker.template
  (:require [clojure.spec.alpha :as s]
            [konserve.core :as k]))

(defn add-template [conn {:keys [name fn]}]
  (k/assoc-in conn ::template ))





