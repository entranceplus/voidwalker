(ns voidwalker.handler
  (:require [compojure.core :refer [routes wrap-routes]]
            [compojure.route :as route]
            [mount.core :as mount]
            [voidwalker.auth :refer [auth-routes]]
            [voidwalker.content.core :refer [content-routes]]
            [voidwalker.env :refer [defaults]]
            [voidwalker.layout :refer [error-page]]
            [voidwalker.middleware :as middleware]
            [voidwalker.routes.home :refer [home-routes]]))

(mount/defstate init-app
                :start ((or (:init defaults) identity))
                :stop  ((or (:stop defaults) identity)))

(def app-routes
  (routes
   (-> #'auth-routes
       (wrap-routes middleware/wrap-formats))
   (-> #'content-routes
       (wrap-routes middleware/wrap-formats))
   (-> #'home-routes
       (wrap-routes middleware/wrap-csrf)
       (wrap-routes middleware/wrap-formats))
   (route/not-found
    (:body
     (error-page {:status 404
                  :title "page not found"})))))


(defn app [] (middleware/wrap-base #'app-routes))
