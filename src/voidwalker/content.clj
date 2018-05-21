(ns voidwalker.content
  (:require [clojure.string :as str]
            [compojure.core :refer [context defroutes GET POST routes DELETE]]
            [ring.util.http-response :as response]
            [hiccup.core :as hi]
            [snow.util :as u]
            [snow.comm.core :as comm]
            [re-frame.core :as rf]
            [voidwalker.aws :as aws]
            [environ.core :refer [env]]
            [konserve.core :as k]
            [clojure.core.async :as async :refer [<!!]]
            [clojure.set :as c.s]
            [snow.repl :as r]
            [clojure.spec.alpha :as s]
            [taoensso.timbre :as timbre
             :refer [log  trace  debug  info  warn  error  fatal  report
                     logf tracef debugf infof warnf errorf fatalf reportf
                     spy get-env]]))

(defn get-conn [] (-> (r/system) :voidwalker.systems/conn :store))

(s/def ::url string?)
(s/def ::content string?)
(s/def ::title string?)
(s/def ::tags (s/coll-of string?))
(s/def ::id string?)
(s/def ::css (s/coll-of string?))

(s/def ::template (s/keys :req [::data ::html]))

(s/def ::post (s/keys :req [::url ::content ::title ::tags]
                      :opt [::css ::template]))

(s/check-asserts true)

(def a nil)

(defn transact-data
  [conn spec data]
  ;; {:pre [(s/valid? spec data)]}
  (info "data to be inserted in is " conn spec (keys data))
  (let [id (-> (u/uuid) keyword)]
    (def a (assoc-in a [spec id] data))
    {:status (<!! (k/assoc-in conn [spec id] data))
     :id id}))

#_(transact-data (get-conn) ::post {:url "asdasd", :content nil, :title "tio", :tags nil, :datasource nil, :css ()})

(defn delete-data [conn ref]
  (info "trying to delete " ref)
  (<!! (k/update-in conn (butlast ref) (fn [coll]
                                         (dissoc coll (last ref))))))

;; (delete-data (get-conn) [::post "538cc9ed-7d84-4a41-acc1-1ae48cf97ce2"])

(defn add-data-with-id [conn spec data]
  (let [id (->> data
              (transact-data conn spec))]
    (merge data {:id id})))

(defn update-data
  [conn spec data ref]
  (info "updating post " ref)
  (<!! (k/assoc-in conn [spec ref] data)))


(defn assoc-id
  "the query returns eids as [eid map], this will add
   eid to map"
  [coll]
  (into {} (map (fn [[id m]]
                  (array-map (cond-> id
                               (string? id) keyword) m)) coll)))

(def sample-post {:url "http://wwwasas.google.com"
                  :content "A asa content \n go again"
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

(defn process-post [{:keys [url content title tags css datasource] :as post}]
  {:url url
   ;; :content (when (some? content) (clojure.core/str "<div>"
   ;;                                                  (str/replace content #"\n" "</div><div>")
   ;;                                                  "</div>"))
   :content content
   :title title
   :tags tags
   :datasource datasource
   :css (process-css css)})

(defn add-post [store {:keys [css] :as post}]
  (info "Adding post " (:id post) (:title post))
  (transact-data store ::post (process-post post)))



(defn update-post [store {:keys [url content title tags css id] :as post}]
  (update-data store ::post (process-post post) id))

;; (<!! (k/assoc-in (get-conn) [::abc "def"] "abc"))
;; (<!! (k/get-in (get-conn) [::abc "def"]))
#_(count (<!! (k/get-in (get-conn) [::post])))
;; (<!! (k/update-in (get-conn) [::post "f95d9619-6908-4473-9bf3-b79a028b8b8e"] {:name "a"}))
;; (def get-stuff (get-post (get-conn) {:id "26df8057-9501-466f-81f6-3249a2936240"}))

;; (def id  "26df8057-9501-466f-81f6-3249a2936240")



;; (delete-data (get-conn) [::post id])
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



(def find-first (comp first filter))

(defn get-all-posts [store]
  (assoc-id (<!! (k/get-in store [::post]))))

(defn get-post
  ([store] (get-post store nil))
  ([store q]
   (if (or (some? (:id q))
          (some? (:url q)))
     (find-first (fn [{:keys [id url]}]
                   (if (some? (:id q))
                     (= (:id q) id)
                     (= (:url q) url)))
                 (get-all-posts store))
     (get-all-posts store))))

;; (def store (get-conn))

#_(->  (get-conn) get-post keys)
;; (->> (get-all-posts (get-conn)) first)
;;    (filter #(= (:title %) "qwdqwdwqd"))
;;    first
;;    :datasource
;;    first
;;    vals
;;    first


;;;;;;;;;;;;;;;;;;;;;;;
;; Starting template ;;
;;;;;;;;;;;;;;;;;;;;;;;

(defn data-tmpl  [idx {:keys [name location website mhrd placement mq mode]}]
  [:div.list-container
   [:div.list-heading
    [:p (+ idx 1)]
    [:p (:c name)]]
   [:div.list-info-container
    [:div.list-info-content
     [:p [:span (str (:h location) ":")] (:c location)]
     [:p [:span (:h  mhrd)] (:c mhrd)]
     [:p.last-info-content-child [:span (:h  placement)] (:c placement)]]
    [:div.list-info-content
     [:p [:span (:h mq)]  (if (empty? (:c mq))
                            "No"
                            (:c mq))]
     [:p [:span (:h mode)] (:c mode)]
     [:a.last-info-content-child-a {:href (:c website)} "Visit Website"]]]])

(empty? nil)

(defn root-tmpl [data]
  [:section.exam-list>div.exam-list-container (map-indexed data-tmpl data)])

;;;;;;;;;;;;;;;;;;;;;;;;
;; Templatecode ends ;;
;;;;;;;;;;;;;;;;;;;;;;;;

(defn parse-post [{:keys [content] :as post} template]
  (println "Parsing post " (-> post :datasource first :data))
  (assoc post :content (str content (hi/html (template (some-> post
                                                               :datasource
                                                               first
                                                               vals
                                                               first))))))


(defn get-templated-post [store]
  (->> (get-all-posts store)
     (map (fn [post]
            (if (some? (:datasource post))
              (parse-post post root-tmpl)
              post)))))


;; (->> (get-templated-post (get-conn))
;;    (filter #(= (:title %) "qwdqwdwqd"))
;;    (map :content))

;; (def row  (->> (get-all-posts store)
;;                (filter #(some? (:datasource %)))
;;                first))
;; (require '[hickory.core :as hc])


;; (def d  (-> row :datasource first :data first))
;; (hc/as-hiccup (hc/parse  "<div> </div>"))
;; (h/hiccup-to-html (seq (vector (sample-template d))))


(defn send-response [response]
  (-> response
     (response/header "Content-Type" "application/json; charset=utf-8")))

(defn upload-css
  "takes css-files with file-content and name and returns public url
   after uploading to s3"
  [css-files]
  (map (fn [{:keys [file-content name]}]
         (aws/upload name {:content file-content})) css-files))

(defn add-post-handler
  [{{:keys [::post]} :data
    {{conn :store} :voidwalker.systems/conn} :component
    reply-fn :?reply-fn}]
  (info "Add post handler ::post :id :title" (:id post) (:title post))
  (let [p (if (some? (:id post))
            (update-post conn post)
            (add-post conn post))]
    (info "reply-fn is a fn " (fn? reply-fn))
    (reply-fn {:msg :saved})
    (rf/dispatch [::saved (-> p :id)])))

(defn delete-post-handler
  [{{:keys [id]} :data
    {{conn :store} :voidwalker.systems/conn} :component
    reply-fn :?reply-fn}]
  (info "Deleting post " id)
  (delete-data conn [::post (keyword id)])
  (reply-fn {:msg :deleted}))

(rf/reg-event-fx
 ::saved
 (fn [{db :db} [_ id]]
   {::comm/broadcast {:dispatch [::saved id]}
    :db db}))

(defn handle-add-post [conn post]
  (send-response (response/ok (merge {:msg "Post added"}
                                     (add-post conn post)))))

(defn handle-update-post [conn {:keys [:id] :as post}]
  (update-post conn post id)
  (send-response (response/ok {:msg "Post updated"})))



(defn content-routes [{{conn :store} :voidwalker.systems/conn}]
  (routes
   (context "/articles" []
            (GET "/" {{:keys [id]} :params}
                 (println "id is " id)
                 (send-response (response/ok {:articles (get-post conn {:id id})})))
            (POST "/" {{:keys [id] :as post} :params}
                  (println "received params as " post)
                  (if (some? id)
                    (handle-update-post conn post)
                    (handle-add-post conn post)))
            (DELETE "/:id" [id]
                    (delete-data conn [::post (keyword id)])
                    (send-response (response/ok {:msg "deleted"})))
            (POST "/file" {:keys [body]}
                  (send-response (response/ok (slurp body)))))))




