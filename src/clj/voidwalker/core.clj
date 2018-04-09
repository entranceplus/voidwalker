(ns voidwalker.core
  (:gen-class)
  (:require [system.repl :refer [set-init! start]]
            [voidwalker.systems :as system]))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Starting voidwalker")
  (set-init! #'system/prod-system)
  (start))
