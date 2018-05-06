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

(def config (read-edn "profiles.edn"))

(defn start-systems! []
  (repl/start-systems {:snow.systems/system-fn system-config
                       :snow.systems/config config}))

(defn stop-systems! []
  (repl/stop-systems))

(defn cljs-repl []
  (cemerick.piggieback/cljs-repl :app))

(defn restart-systems! []
  (do (start-systems!)
      (stop-systems!)))

(restart-systems!)

#_(cljs-repl)

(defn -main [& args]
  (println "Starting systems...")
  (start-systems!)
  (nrepl/start-server :port (:repl-port config) :handler cider-nrepl-handler)
  (println "nrepl started")
  (server/start!)
  (shadow/dev :app))
