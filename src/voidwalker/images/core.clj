(ns voidwalker.images.core
  (:require [snow.s3 :as s3]
            [snow.comm.core :as comm]
            [re-frame.core :as rf]
            [clojure.core.async :refer [go]]
            [taoensso.timbre :refer [info]]))

(defn upload [images]
  (->> images
     (map (fn [[n content]]
            {:name n
             :url (s3/upload n {:content content
                                :binary true})}))
     doall))

(rf/reg-event-fx
 ::added
 (fn [{db :db} [_ {images :images} reply]]
   (def i images)
   (reply :done)
   {:db db
    ::comm/broadcast {:dispatch [::upload-complete (upload images)]}}))

