(ns voidwalker.db
  (:require [cprop.core :refer [load-config]]))

(defn fetch-conf [conf]
  (get-in (load-config) conf))

(def db-config {:user (fetch-conf [:db :user])
                :password (fetch-conf [:db :password])
                :db (fetch-conf [:db :dbname])})

(korma.db/defdb dbcon (korma.db/mysql db-config))
