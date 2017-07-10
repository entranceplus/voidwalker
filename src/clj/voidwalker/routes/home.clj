(ns voidwalker.routes.home
  (:require [clojure.java.io :as io]
            [compojure.core :refer [ANY defroutes GET]]
            [ring.util.http-response :as response]
            [voidwalker.layout :as layout]
            [voidwalker.auth :as auth]))

(defn home-page []
  (layout/render "home.html"))

(defroutes home-routes
  (GET "/" []
       (auth/disp))
  (GET "/docs" []
       (-> (response/ok (-> "docs/docs.md" io/resource slurp))
           (response/header "Content-Type" "text/plain; charset=utf-8")))
  (ANY "*" []
       (home-page)))
