(ns voidwalker.systems
  (:require [com.stuartsierra.component :as component]
            [environ.core :refer [env]]
            [voidwalker.routes :refer [hello-routes site]]
            [ring.middleware.format :refer [wrap-restful-format]]
            [ring.middleware.defaults :refer [wrap-defaults
                                              site-defaults
                                              api-defaults]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [snow.db :as db]
            [snow.systems :as system]
            [voidwalker.content :refer [content-routes]]
            (system.components
             [postgres :refer [new-postgres-database]]
             [immutant-web :refer [new-immutant-web]]
             [jetty :refer [new-web-server]]
             [repl-server :refer [new-repl-server]]
             [endpoint :refer [new-endpoint]]
             [middleware :refer [new-middleware]]
             [handler :refer [new-handler]]
             [konserve :refer [new-konserve]])))


(def rest-middleware
  (fn [handler]
    (wrap-restful-format handler
                         :formats [:json-kw]
                         :response-options {:json-kw {:pretty true}})))

;;(system.repl/system :conn)


(defn system-config [config]
  [:site-endpoint (component/using (new-endpoint site)
                                   [:site-middleware])
   :conn (new-konserve :type :filestore :path (config :db-path))
   :api-endpoint (component/using (new-endpoint content-routes)
                                  [:api-middleware :conn])
   :site-middleware (new-middleware {:middleware [[wrap-defaults site-defaults]]})
   :api-middleware (new-middleware
                    {:middleware  [rest-middleware
                                   [wrap-defaults api-defaults]]})
   :handler (component/using (new-handler) [:api-endpoint :site-endpoint])
   :api-server (component/using (new-web-server (system/get-port config))
                                [:handler])])

(defn dev-system []
    (system/gen-system system-config))


(defn prod-system
  "Assembles and returns components for a production deployment"
  []
  (merge (dev-system)
         (component/system-map
          :repl-server (new-repl-server (read-string (env :repl-port))))))
