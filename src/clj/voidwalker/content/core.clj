(ns voidwalker.content.core
  (:require [clojure.string :as str]
            [compojure.core :refer [context defroutes GET POST]]
            [korma.core :as k]
            [ring.util.http-response :as response]
            [voidwalker.db]
            [snow.db :as db]
            [clojure.java.jdbc :as jdbc]))


(def db (->> (cprop.core/load-config)
               :db
               (db/get-db-spec-from-env :config)))

;; todo define specs asap
;; this is going to get very confusing

;; (k/insert posts (k/values {:url "/murlocs"
;;                            :content "Summon a 1/1 murloc"}))

(defn add-post [{:keys [url content title tags id] :as post}]
  (println "Got a request for adding " post)
  (if (nil? id)
    (db/add db :posts post)
    (db/update db
               :posts
               {:set {:url url
                      :content content
                      :title title
                      :tags tags}
                :where [:= :id id]})))

;; (add-post {:url "fwe" :id 13 :tags "fwe" :content "some" :title "fwef"})
;; (get-post)

(defn get-post
  ([] (db/query db {:select [:*]
                    :from [:posts]}))
  ([id] (first (db/query db {:select [:*]
                               :from [:posts]
                               :where [:= :id (Integer/parseInt id)]}))))

(defn send-response [response]
  (-> response
      (response/header "Content-Type" "application/json; charset=utf-8")))

(defroutes content-routes
  (context "/articles" []
           (GET "/" {{:keys [id]}  :params}
                (let [posts (if (nil? id)
                              (get-post)
                              (get-post id))]
                  (send-response (response/ok posts))))
           (POST "/" {post :params}
                 (add-post post)
                 (send-response (response/ok {:msg "Post added"})))))
