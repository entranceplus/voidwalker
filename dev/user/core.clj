(ns user.core
  (:require [clojure.spec.alpha :as s]
            [snow.repl :as repl]
            [snow.env :refer [read-edn]]
            [voidwalker.systems :refer [system-config]]
            [cider.nrepl :refer [cider-nrepl-handler]]
            [clojure.tools.nrepl.server :as nrepl]
            [shadow.cljs.devtools.server :as server]
            [shadow.cljs.devtools.api :as shadow]))

(s/check-asserts true)

(defn cljs-repl []
  (cemerick.piggieback/cljs-repl :app))

(defn restart-systems! []
  (do (repl/stop!)
      (repl/start! system-config)))

#_(restart-systems!)

#_(cljs-repl)

(defn compile-cljs []
  (server/start!)
  (shadow/dev :app))

(defn -main [& args]
  (println "Starting systems...")
  (repl/start! system-config)
  (repl/start-nrepl)
  (println "nrepl started")
  (server/start!)
  (shadow/dev :app)  )
