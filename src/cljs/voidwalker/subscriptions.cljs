(ns voidwalker.subscriptions
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(reg-sub
  :page
  (fn [db _]
    {:page (:page db)
     :page-param (:page-param db)}))

(reg-sub
  :docs
  (fn [db _]
    (:docs db)))

(reg-sub
 :new/post-status
 (fn [db _]
   (:new/post-status db)))


(reg-sub
 :articles
 (fn [db _]
   (:articles db)))


(defn find-first
         [f coll]
         (first (filter f coll)))


(reg-sub
 :article
 (fn [_ id] (subscribe [:articles]))
 (fn [articles [_ id]]
   (find-first (fn [article]
                 (= (:id article) (int id)))
              articles)))
