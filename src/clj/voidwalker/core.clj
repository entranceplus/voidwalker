(ns voidwalker.core
  (:gen-class)
  (:require [snow.repl :as repl]
            [snow.env :refer [read-edn]]
            [voidwalker.systems :as void-system]))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Starting voidwalker " args)
  (repl/start-systems {:snow.systems/system-fn void-system/system-config
                       :snow.systems/config (read-edn "profiles.edn")}))
