(ns voidwalker.routes
  (:require [compojure.core :refer [routes GET ANY]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]))

(defn ok-response [response]
  (-> (response/ok response)
      (response/header "Content-Type" "text/plain")))

(defn hello-routes [_]
  (routes
   (GET "/hello" [] (ok-response "Hello world!!"))))


(defn home-page []
  (-> "index.html"
      io/resource
      slurp
      response/ok
      (response/header "Content-Type" "text/html")))


(defn site [_]
  (routes
   (GET "/" [] (home-page))
   (ANY "*" [] (home-page))))
