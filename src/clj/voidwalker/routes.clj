(ns voidwalker.routes
  (:require [compojure.core :refer [routes GET ANY]]
            [ring.util.http-response :as response]))

(defn ok-response [response]
  (-> (response/ok response)
      (response/header "Content-Type" "application/json; charset=utf-8")))

(defn hello-routes [_]
  (routes
   (GET "/hello" [] (ok-response {:msg "Hello world!!"}))))

(defn home-page []
    (-> (response/file-response "index.html"
                                {:root "resources"})
        (response/header "Content-Type" "text/html")))

(defn site [_]
  (routes
   (GET "/" [] (home-page))
   (ANY "*" [] (home-page))))
