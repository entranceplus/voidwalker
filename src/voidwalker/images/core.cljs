(ns voidwalker.images.core
  (:require [re-frame.core :as rf]
            [snow.files.ui :as files]
            [snow.comm.core :as c]))

;;;;;;;;;;;;;;
;; handlers ;;
;;;;;;;;;;;;;;

(rf/reg-event-fx
 ::added
 (fn [{:keys [db]} _]
   {:db (assoc db ::uploading :uploading)
    ::c/request {:data [::added {:images (-> db ::new :image)}]
                 :on-failure [::failed]}}))

(rf/reg-event-db
 ::upload-complete
 (fn [db [_ urls]]
   (assoc db
          ::images urls
          ::uploading :success)))

(rf/reg-event-db
 ::failed
 (fn [db _]
   (assoc db ::uploading :failed)))

;;;;;;;;;;;;;;;;;;;
;; subscriptions ;;
;;;;;;;;;;;;;;;;;;;

(rf/reg-sub
 ::images
 (fn [db _] (::images db)))

(rf/reg-sub
 ::uploading
 (fn [db _] (::uploading db)))

;;;;;;;;;;;
;; views ;;
;;;;;;;;;;;

(defn image []
  [:section.section>div.container>div.columns
   [:div.column
    [files/file-input {:type :image
                       :id ::new
                       :binary true
                       :dispatch [::added]
                       :placeholder "Upload images here"}]
    [:div.notification (some-> @(rf/subscribe [::uploading]) name)]]
   [:div.column>table.table
    [:thead
     [:th "Name"]
     [:th "Url"]]
    [:tbody (for [{:keys [name url]}  @(rf/subscribe  [::images])]
              [:tr {:key name}
               [:td name]
               [:td url]])]]])


