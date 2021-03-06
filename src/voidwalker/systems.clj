(ns voidwalker.systems
  (:require [com.stuartsierra.component :as component]
            [environ.core :refer [env]]
            [voidwalker.routes :refer [hello-routes site]]
            [ring.middleware.format :refer [wrap-restful-format]]
            [ring.middleware.defaults :refer [wrap-defaults
                                              site-defaults
                                              api-defaults]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :as params]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.anti-forgery :as anti-forgery]
            [re-frame.core :as rf]
            [taoensso.sente.server-adapters.immutant :refer (get-sch-adapter)]
            [snow.db :as db]
            [snow.systems :as system]
            [voidwalker.content :refer [content-routes] :as content]
            [voidwalker.dispatch :refer [request-handler]]
            [voidwalker.template :as t]
            [voidwalker.images.core]
            [voidwalker.images.thumbnail]
            [snow.comm.core :as comm]
            [taoensso.timbre :as timbre
             :refer [log  trace  debug  info  warn  error  fatal  report
                     logf tracef debugf infof warnf errorf fatalf reportf
                     spy get-env]]
            (system.components
             [postgres :refer [new-postgres-database]]
             [immutant-web :refer [new-immutant-web]]
             [jetty :refer [new-web-server]]
             [repl-server :refer [new-repl-server]]
             [endpoint :refer [new-endpoint]]
             [middleware :refer [new-middleware]]
             [handler :refer [new-handler]]
             [konserve :refer [new-konserve]]
             [sente :refer [new-channel-socket-server sente-routes]])))



(def rest-middleware
  (fn [handler]
    (wrap-restful-format handler
                         :formats [:json-kw]
                         :response-options {:json-kw {:pretty true}})))

;; (def conn (-> (snow.repl/system) ::conn :store))

(rf/reg-event-fx :voidwalker/init
                 [(rf/inject-cofx :system)]
                 (fn [{:keys [db system]} _]
                   (info "ok boyrs got init now will dispatch ")
                   (let [conn (-> system ::conn :store)]
                     {:db db
                      ::comm/broadcast {:dispatch [:voidwalker/data
                                                   {::content/articles (content/get-post conn)
                                                    ::t/tmpl [{::t/name "Ranklist"
                                                               ::t/fun :ranklist}
                                                              {::t/name "Examlist"
                                                               ::t/fun :examlist}
                                                              {::t/name "Blog"
                                                               ::t/fun :blog}]}]}})))

;; (rf/dispatch [:voidwalker/init])


(defn system-config [config]
  [::site-endpoint (component/using (new-endpoint site)
                                    [::site-middleware])
   ::conn (new-konserve :type :filestore :path (config :db-path))
   ::api-endpoint (component/using (new-endpoint content-routes)
                                   [::api-middleware ::conn])
   ::site-middleware (new-middleware {:middleware [wrap-session
                                                   anti-forgery/wrap-anti-forgery
                                                   params/wrap-params
                                                   wrap-keyword-params
                                                   [wrap-resource "public"]]})
   :middleware (new-middleware {:middleware [wrap-session
                                             anti-forgery/wrap-anti-forgery
                                             params/wrap-params
                                             wrap-keyword-params
                                             [wrap-resource "public"]]})
   ::api-middleware (new-middleware
                     {:middleware  [rest-middleware
                                    [wrap-defaults api-defaults]
                                    ]})
   ::sente-endpoint (component/using
                     (new-endpoint comm/sente-routes)
                     [::site-middleware ::comm/comm ])
   ::comm/comm (component/using (comm/new-comm comm/event-msg-handler
                                               comm/broadcast
                                               request-handler)
                                [::conn])
   ::broadcast-enabled?_ (atom true)
   ::handler (component/using (new-handler) [::sente-endpoint ::api-endpoint ::site-endpoint :middleware])
   ::api-server (component/using (new-immutant-web :port (system/get-port config ::http-port))
                                 [::handler])])


(defn dev-system []
    (system/gen-system system-config))




(defn prod-system
  "Assembles and returns components for a production deployment"
  []
  (merge (dev-system)
         (component/system-map
          :repl-server (new-repl-server (read-string (env :repl-port))))))
