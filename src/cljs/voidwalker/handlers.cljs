(ns voidwalker.handlers
  (:require [voidwalker.db :as db]
            [ajax.core :as ajax]
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
      (case page
        :home {:dispatch [:get-articles]
               :db db}
        (:add :edit) {:db (dissoc db :new/post-status)}
        {:db db}))))

;;;;;;;;;;;;;;;;;;;;;
;; listing article ;;
;;;;;;;;;;;;;;;;;;;;;

(reg-event-fx
 :get-articles
 (fn [_ _]
   (println "calling get articles")
   {:http-xhrio (new-request {:method :get
                              :uri "/article"
                              :on-success [:set-article]})}))

(reg-event-db
 :set-article
 (fn [db [_ articles]]
   (assoc db :articles articles)))


;;;;;;;;;;;;;;;;;;;;
;; saving article ;;
;;;;;;;;;;;;;;;;;;;;

(reg-event-fx
 :save-article
 (fn [{:keys [db]} [_ article]]
   {:http-xhrio (new-request {:method :post
                              :uri "/article"
                              :params article
                              :on-success [:article-saved]
                              :on-failure [:error-post]})
    :db (assoc db :new/post-status :loading)}))

(reg-event-db
 :article-saved
 (fn [db _]
   (assoc db :new/post-status :success)))


(reg-event-db
 :error-post
 (fn [db _]
   (println "Error occurred")
   (assoc db :new/post-status :error)))
