(ns voidwalker.source.handlers
  (:require [voidwalker.source.db :as db]
            [ajax.core :as ajax]
            [snow.comm.core :as comm]
            [re-frame.core :refer [dispatch reg-event-db reg-event-fx]]
            [day8.re-frame.http-fx]))

(defn new-request [request]
  (merge {:timeout 8000
          :response-format (ajax/json-response-format
                            {:keywords? true})
          :format (ajax/json-request-format)
          :on-failure [:error-result]} request))

(reg-event-db
  :initialize-db
  (fn [_ _]
    db/default-db))

;; todo add better default error handling
(reg-event-db
 :error-result
 (fn [db _]
   (println "error occurred")
   (assoc db :error? true)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; pagination and initial data loading ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(reg-event-fx
  :set-active-page
  (fn [{:keys [db]} [_ page param]]
    (let [db (assoc db :page page :page-param param)]
      (println "setting page " page)
      (case page
        :voidwalker.home {:dispatch [:get-articles]
                          :db db}
        (:voidwalker.add :edit) {:db (dissoc db :new/post-status)}
        {:db db}))))

;;;;;;;;;;;;;;;;;;;;;
;; listing article ;;
;;;;;;;;;;;;;;;;;;;;;

(reg-event-fx
 :get-articles
 (fn [_ _]
   (println "calling get articles")
   {:http-xhrio (new-request {:method :get
                              :uri "/articles"
                              :on-success [:set-article]})}))

(reg-event-db
 :set-article
 (fn [db [_ articles]]
   (assoc db :articles (:articles articles))))


;;;;;;;;;;;;;;;;;;;;
;; saving article ;;
;;;;;;;;;;;;;;;;;;;;

(reg-event-fx
 :save-article
 (fn [{:keys [db]} [_ article]]
   (println "article is " article)
   {::comm/request {:data {::comm/type :voidwalker.content/add
                           :voidwalker.content/post article}
                    :on-success [:voidwalker.content/saved]
                    :on-failure [:error-post]}
    :db (assoc db :new/post-status :loading)}))

(reg-event-fx
 :delete-article
 (fn [{:keys [db]} [_ id]]
   {:http-xhrio (new-request {:method :delete
                              :uri (str "/articles/" id)
                              :on-success [:get-articles]
                              :on-failure [:error-post]})
    :db db}))

(reg-event-db
 :voidwalker.content/saved
 (fn [db [_ id]]
   (println )
   (assoc db :new/post-status :success)))


(reg-event-db
 :update-article
 (fn [db [_ article]]
   (println "called  update-article handle" db)
   (assoc db :articles (map (fn [a]
                              (if (= (:id a) (:id article)) article a))
                            (:articles db)))))

(reg-event-db
 :post-status-init
 (fn [db _]
   (println "post status init")
   (dissoc db :new/post-status)))


(reg-event-db
 :error-post
 (fn [db _]
   (println "Error occurred")
   (assoc db :new/post-status :error)))


;; FILES stuff

(reg-event-db
  :file-content
  (fn [db [_ {:keys [id content]}]]
    (assoc db id content)))
