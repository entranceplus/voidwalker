(ns voidwalker.template
  (:require [clojure.spec.alpha :as s]
            [snow.db :as db]
            [voidwalker.template.ranklist :as r]
            [konserve.core :as k]
            [clojure.java.io :as io]))

(s/def ::name string?)

(s/def ::fun fn?)

(s/def ::tmpl (s/keys :req [::name ::fn]))

;; (def conn (-> (snow.repl/system) :conn :store))

(def ranklist r/root-tmpl)

;; (load-string ranklist-template)         

(defn add-template [conn {:keys [::name ::fun] :as t}]
  (db/add-entity conn ::template t))

(defn get-template [conn]
  (db/get-entity conn ::template))





