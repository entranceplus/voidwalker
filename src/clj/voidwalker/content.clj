(ns voidwalker.content
  (:require [clojure.string :as str]
            [compojure.core :refer [context defroutes GET POST routes]]
            [ring.util.http-response :as response]
            [snow.db :as dbutil]
            [environ.core :refer [env]]
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
  "get posts from database. if id or url is provided, rows are filtered according
  to that. where of url overwrites id. support for querying via both id and url is
  not provided"
  [db & {:keys [id url]}]
  (let [query (cond-> {:select [:*]
                       :from [:posts]}
                (some? id) (merge {:where [:= :id id]})
                (some? url) (merge {:where [:= :url url]}))]
    (cond-> (dbutil/query db query)
      (or (some? id)
          (some? url)) first)))

;; ([db] (dbutil/query (:void-db system.repl/system) {:select [:*]
;;                                                    :from [:posts]}))
;; ([db id] (first (dbutil/query db {:select [:*]
;;                                   :from [:posts]
;;                                   :where [:= :id (Integer/parseInt id)]})))
;; ([db url] (first (dbutil/query db {:select [:*]
;;                                    :from [:posts]
;;                                    :where [:= :url url]})))

(defn send-response [response]
  (-> response
      (response/header "Content-Type" "application/json; charset=utf-8")))

(def tempfile "")

(defn content-routes [{db :db}]
  (routes
   (context "/articles" []
            (GET "/" {{:keys [id]}  :params}
                 (send-response (response/ok (get-post db :id id))))
            (POST "/" {post :params}
                  (add-post db post)
                  (send-response (response/ok {:msg "Post added"})))
            (POST "/file" {:keys [body]}
                  (send-response (response/ok (slurp body)))))))
