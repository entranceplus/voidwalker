(ns voidwalker.images.thumbnail
  (:require #?(:cljs [snow.files.ui :as files])
            [snow.comm.core :as c]
            #?(:cljs [reagent.core :as r])
            [taoensso.timbre :as timbre :refer [info]]
            [re-frame.core :as rf]
            [voidwalker.images.core :as i]))

(def n nil)

(rf/reg-event-fx
 ::added
 #?(:cljs (fn [{:keys [db]} [_ {:keys [id _]}]]
            {:db (assoc db ::uploading :uploading)
             ::c/request {:data [::added  {:thumbnail (-> db id :thumbnail)
                                           :id id}]
                          :on-failure [::failed]}})
    :clj (fn [{db :db} [_  {id :id [name file-content] :thumbnail} reply]]
           (reply :done)
           (println "will now try to upload " id)
           {::i/upload {:images {name file-content}
                        :on-success [::upload-complete {:id id}]}})))

#?(:cljs (rf/reg-event-db
          ::upload-complete
          (fn [db [_ {:keys [id url]}]]
            (println "updatubg " id url)
            (-> db
                (assoc-in [:articles id :thumbnail] url)
                (assoc ::uploading :done)))))
#?(:clj (rf/reg-event-fx
         ::upload-complete
         (fn [db [_ {id :id [image] :urls}]]
           {:db db
            ::c/broadcast {:dispatch [::upload-complete {:id id
                                                         :url (image :url)}]}})))

(rf/reg-event-db
 ::failed
 (fn [db _]
   (assoc db ::uploading :failed)))


(rf/reg-sub
 ::thumbnail
 (fn [db [_ id]]
   (-> db :articles id :thumbnail)))


(rf/reg-sub
 ::uploading
 (fn [db _] (::uploading db)))

#?(:cljs (defn ui [id]
           [:section.section>div.container
            [:div.columns
             [:div.column [files/file-input {:id id
                                             :placeholder "Add thumbnail."
                                             :type :thumbnail
                                             :binary true
                                             :single? true
                                             :dispatch [::added {:id id}]}]]
             [:div.column>figure.image.is-128x128 [:img {:src @(rf/subscribe [::thumbnail id])}]]]
            [:div.notification (some-> @(rf/subscribe [::uploading]) name)]]))
