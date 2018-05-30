(ns voidwalker.dispatch
  (:require [voidwalker.content :as c]
            [snow.comm.core :as comm]))

(defmulti request-handler #(-> % :data ::comm/type))

(defmethod request-handler ::c/add [m] (c/add-post-handler m))

(defmethod request-handler ::c/delete [m] (c/delete-post-handler m))
