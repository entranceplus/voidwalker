(ns voidwalker.template
  (:require [clojure.spec.alpha :as s]
            [snow.db :as db]
            [konserve.core :as k]
            [clojure.java.io :as io]))

(s/def ::name string?)

(s/def ::fn fn?)

(s/def ::tmpl (s/keys :req [::name ::fn]))

;; (def conn (-> (snow.repl/system) :conn :store))

(def ranklist (slurp (io/resource "voidwalker/template/ranklist.cljc")))

;; (load-string ranklist-template)         

(defn add-template [conn {:keys [::name ::fn] :as t}]
  (db/add-entity conn ::template t))

(defn get-template [conn]
  (db/get-entity conn ::template))





