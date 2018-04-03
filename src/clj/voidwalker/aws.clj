(ns voidwalker.aws
  (:require [amazonica.aws.s3 :as s3]
            [environ.core :refer [env]]
            [amazonica.aws.s3transfer :as s3transfer]))

(def cred {:endpoint (env :reigon)
           :access-key (env :aws-access-key-id)
           :secret-key (env :aws-secret-access-key)})


(defn get-bucket-name
  "will return the bucket name of the first bucket it finds in the account"
  []
  (:name (first (s3/list-buckets cred))))

(defn upload
  [key file]
  (s3/put-object cred
                 :bucket-name (get-bucket-name)
                 :key key
                 :metadata {:server-side-encryption "AES256"}
                 :file file))

(defn get-file [key]
  (s3/get-object cred
                 :bucket-name (get-bucket-name)
                 :key key))
