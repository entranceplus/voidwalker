(ns voidwalker.content
  (:require [clojure.string :as str]
            [compojure.core :refer [context defroutes GET POST routes]]
            [ring.util.http-response :as response]
            [snow.db :as dbutil]
            [snow.util :as u]
            [voidwalker.aws :as aws]
            [environ.core :refer [env]]
            [konserve.core :as k]
            [clojure.core.async :as async :refer [<!!]]
            [clojure.set :as c.s]
            [clojure.spec.alpha :as s]))
;             [clojure.java.jdbc :as jdbc]))
;
; (def uri "datahike:file:///tmp/dh-data9")
;
; (h/create-database uri)
;
; (def conn (h/connect uri))
;
; @(h/transact conn [{:user/id 1 :user/name "Akash" :user/age 33}])
;
(defn get-conn [] (-> system.repl/system :conn :store))
;
; (h/q '[:find ?age
;        :where [?e :user/name "Akash"]
;               [?e :user/age ?age]]
;       @conn)

(s/def ::url string?)
(s/def ::content string?)
(s/def ::title string?)
(s/def ::tags (s/coll-of string?))
(s/def ::id string?)
(s/def ::css (s/coll-of string?))

(s/def ::post (s/keys :req [::url ::content ::title ::tags]
                      :opt [::css]))

(s/check-asserts true)

(defn transact-data
  [conn spec data]
  ; {:pre [(s/valid? spec data)]}
  (println "data to be inserted  is " data)
  (let [id (dbutil/uuid)]
    {:status (<!! (k/assoc-in conn [spec id] data))
     :id id}))


; (transact-data (get-conn) ::new {:ac "def"})

(defn add-data-with-id [conn spec data]
      (let [id (->> data
                    (transact-data conn spec))]
           (merge data {:id id})))

(defn update-data
  [conn spec data ref]
  (<!! (k/assoc-in conn [spec ref] data)))
;
; (defn make-query-set [q w]
;   (c.s/union q (-> w keys set)))
;
; (defn make-where [q w]
;   (->> w
;        (make-query-set q)
;        (map (fn [k]
;               (cond-> ['?e k]
;                 (contains? w k) (conj (k w)))))))
;
; ; (make-where #{::title} {::name "akash"})
;
; (defn make-query
;   [q w]
;   `[:find  ~'?e (~'pull ~'?e ~'[*])
;     :where ~@(make-where q w)])
;
; ; (make-query #{::title} {::name "Akash"})
; ; (make-query #{::title} {})


(defn assoc-id
  "the query returns eids as [eid map], this will add
   eid to map"
  [coll]
  (map (fn [[id m]]
          (assoc m :id id)) coll))

; (defn query
;   ([conn q] (query conn q {}))
;   ([conn q w]
;    (if (some? (:id w))
;      (h/pull @conn '[*] (u/make-int (:id w)))
;      (-> q
;          (make-query w)
;          (h/q @conn)
;          assoc-id))))


; (query (get-conn) #{:title :content :url :tags} {:id 43})

(def sample-post {:url "http://wwwasas.google.com"
                  :content "A asa content"
                  :title "aas "
                  :tags ["abc"]})

; (def update-sample-post (assoc sample-post :css [{:name "http://a"}]))
; (transact-data conn ::post)

(defn process-css [css]
  (->> css
    (map (fn [{:keys [file-content name] :as c}]
           (if (some? file-content)
             (aws/upload name {:content file-content})
             name)))))


(defn add-post [store {:keys [css] :as post}]
  (transact-data store ::post (merge (select-keys post [:url :content :title :tags :css])
                                     {:css (process-css css)})))

(defn update-post [store {:keys [url content title tags css] :as post} id]
  (update-data store ::post (merge post
                                   {:css (process-css css)}) id))
;
; (h/q  '[:find ?e (pull ?e [*])
;         :where [?e :content
;                        [?e :title]
;                        [?e :url]
;                        [?e :tags ?tags]]]
;       @(get-conn))
; (h/pull @(get-conn) '[:_tags-sm] 10)
; (def post (add-post (get-conn) sample-post))
; (get-post (get-conn) (:id post))
; (get-post  (get-conn) {:id nil})

; (<!! (k/assoc-in (get-conn) [::abc "def"] "abc"))
; (<!! (k/get-in (get-conn) [::abc "def"]))
; (<!! (k/get-in (get-conn) [::post "f95d9619-6908-4473-9bf3-b79a028b8b8e"]))
; (<!! (k/update-in (get-conn) [::post "f95d9619-6908-4473-9bf3-b79a028b8b8e"] {:name "a"}))
; (def get-stuff (get-post (get-conn) 2))
; (update-post (get-conn) (assoc get-stuff :tags ["def" "jkl" "a"]) 2)
; CRU testing
; @(h/transact (get-conn) [{:tags-sm ["abc" "de" "no"] :db/id 11}])
;
; (<!! (k/assoc-in (get-conn) [:temp] {:tags ["a" "b" "c"]}))
;
; (<!! (k/get-in (get-conn) [:temp]))
;
; (def update-data {::title "new title"})
; @(h/transact conn [(merge update-data {:db/id 10})])


; (defn get-post
;   "get posts from database. if id or url is provided, rows are filtered according
;   to that. where of url overwrites id. support for querying via both id and url is
;   not provided"
;   [db & {:keys [id url]}]
;   (let [query (cond-> {:select [:*]
;                        :from [:posts]}
;                 (some? id) (merge {:where [:= :id id]})
;                 (some? url) (merge {:where [:= :url url]}))]
;     (cond-> (dbutil/query db query)
;       (or (some? id)
;           (some? url)) first)))
(defn get-post
  ([store] (get-post store nil))
  ([store q]
   (if (or (some? (:id q))
           (some? (:url q)))
     (first (filter (fn [{:keys [id url]}]
                      (if (some? (:id q))
                          (= (:id q) id)
                          (= (:url q) url)))
                    (assoc-id (<!! (k/get-in store [::post])))))
     (assoc-id (<!! (k/get-in store [::post]))))))

; (get-post (get-conn) {:id 43})
; (get-post (get-conn) nil)
;; ([db] (dbutil/query (:void-db system.repl/system) {:select [:*]
;;                                                    :from [:posts]}))
;; ([db id] (first (dbutil/query db {:select [:*]
;;                                   :  from [:posts]
;;                                   :where [:= :id (Integer/parseInt id)]})))
;; ([db url] (first (dbutil/query db {:select [:*]
;;                                    :from [:posts]
;;                                    :where [:= :url url]})))

(defn send-response [response]
  (-> response
      (response/header "Content-Type" "application/json; charset=utf-8")))

(defn upload-css
  "takes css-files with file-content and name and returns public url
   after uploading to s3"
  [css-files]
  (map (fn [{:keys [file-content name]}]
         (aws/upload name {:content file-content})) css-files))


(defn handle-add-post [conn post]
  (send-response (response/ok {:msg "Post added"
                               :id (:id (add-post conn post))})))

(defn handle-update-post [conn {:keys [:id] :as post}]
  (update-post conn post id)
  (send-response (response/ok {:msg "Post updated"})))

(defn content-routes [{{conn :store} :conn}]
  (routes
   (context "/articles" []
            (GET "/" {{:keys [id]} :params}
                 (println "id is " id)
                 (send-response (response/ok {:articles (get-post conn {:id id})})))
            (POST "/" {{:keys [:id] :as post} :params}
                  (println "received params as " post)
                  (if (some? id)
                    (handle-update-post conn post)
                    (handle-add-post conn post)))
            (POST "/file" {:keys [body]}
                  (send-response (response/ok (slurp body)))))))
