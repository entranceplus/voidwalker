(ns voidwalker.content
  (:require [clojure.string :as str]
            [compojure.core :refer [context defroutes GET POST routes]]
            [ring.util.http-response :as response]
            [snow.db :as dbutil]
            [clojure.java.jdbc :as jdbc]))

(defn add-post [db {:keys [url content title tags id] :as post}]
  (println "Got a request for adding " post)
  (if (nil? id)
    (dbutil/add db :posts post)
    (dbutil/update db
               :posts
               {:set {:url url
                      :content content
                      :title title
                      :tags tags}
                :where [:= :id id]})))

;; (add-post {:url "fwe" :id 13 :tags "fwe" :content "some" :title "fwef"})
;; (get-post)

(defn get-post
  ([db] (dbutil/query db {:select [:*]
                          :from [:posts]}))
  ([db id] (first (dbutil/query db {:select [:*]
                                    :from [:posts]
                                    :where [:= :id (Integer/parseInt id)]}))))

(defn send-response [response]
  (-> response
      (response/header "Content-Type" "application/json; charset=utf-8")))

(defn content-routes [{db :db}]
  (routes
   (context "/articles" []
            (GET "/" {{:keys [id]}  :params}
                 (let [posts (if (nil? id)
                               (get-post db)
                               (get-post db id))]
                   (send-response (response/ok posts))))
            (POST "/" {post :params}
                  (add-post db post)
                  (send-response (response/ok {:msg "Post added"}))))))
