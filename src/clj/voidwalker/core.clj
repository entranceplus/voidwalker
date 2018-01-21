(ns voidwalker.core
  (:require [voidwalker.handler :as handler]
            [luminus.repl-server :as repl]
            [luminus.http-server :as http]
            [voidwalker.config :refer [env]]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.tools.logging :as log]
            [migratus.core :as migratus]
            [cprop.core :refer [load-config]]
            [mount.core :as mount])
  (:gen-class))

(def cli-options
  [["-p" "--port PORT" "Port number"
    :parse-fn #(Integer/parseInt %)]])

(mount/defstate ^{:on-reload :noop}
  http-server
  :start
  (do
    (println "really")
    (http/start
     (-> env
         (assoc :handler (handler/app))
         (update :port #(or (-> env :options :port) %)))))
  :stop
  (http/stop http-server))

(mount/defstate ^{:on-reload :noop}
  repl-server
  :start
  (when-let [nrepl-port (env :nrepl-port)]
    (repl/start {:port nrepl-port}))
  :stop
  (when repl-server
    (repl/stop repl-server)))



(defn stop-app []
  (println "stop")
  (doseq [component (:stopped (mount/stop))]
    (log/info component "stopped"))
  (shutdown-agents))

(defn start-app [args]
  (println "starting app")
  (doseq [component (-> args
                        (parse-opts cli-options)
                        mount/start-with-args
                        :started)]
    (log/info component "started"))
  (.addShutdownHook (Runtime/getRuntime) (Thread. stop-app)))

(defn config []
  {:store                :database
   :migration-dir        "migrations/"
   :db (merge {:classname   "org.mysql.Driver"
               :subprotocol "mysql"
               :dbtype "mysql"}
              (:db (load-config)))})

(defn -main [& args]
  ;; run migrations from here
  (println "db config for migrations is " (config))
  (migratus/migrate (config))
  (start-app args))

