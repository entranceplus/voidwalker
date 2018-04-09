(ns voidwalker.aws
  (:require [environ.core :refer [env]]
            [amazonica.aws.s3 :as s3]
            [clojure.java.io :as io]
            [amazonica.aws.s3transfer :as s3transfer])
  (:import [com.amazonaws.services.s3.model CannedAccessControlList]))


(def cred {:endpoint (env :reigon)
           :access-key (env :aws-access-key-id)
           :secret-key (env :aws-secret-access-key)})

(defn s3-key [key-or-str]
  (cond-> key-or-str
    (keyword? key-or-str) name))

(defn get-bucket-name
  "will return the bucket name of the first bucket it finds in the account"
  []
  (:name (first (s3/list-buckets cred))))

(defn string-request [content]
  (let [bytes (-> content (.getBytes "UTF-8"))]
    {:input-stream (java.io.ByteArrayInputStream. bytes)
     :metadata {:content-length (count bytes)}}))

(defn upload
  [key {:keys [file content]}]
  (let [bucket-name (get-bucket-name)
        key-str (s3-key key)]
    (s3/put-object cred
                   (merge {:bucket-name bucket-name
                           :canned-acl CannedAccessControlList/PublicRead
                           :key key-str}
                          (if (some? content)
                           (string-request content)
                           {:file file})))
    (s3/get-resource-url cred bucket-name key)))

(defn get-file [key]
  (s3/get-object cred
                 :bucket-name (get-bucket-name)
                 :key (s3-key key)))

(defn read-file [key]
  (-> key get-file :object-content slurp))


; (upload :junkmap {:content "akash shakdwipeea"})
; (s3/get-resource-url cred (get-bucket-name) :junkmap)
; (upload :file-junk {:file "./build.boot"})
; (read-file :junkmap)
; ;
; ;; set S3 Client Options
; (s3/list-buckets cred
;   {:client-config {}
;     :path-style-access-enabled false
;     :chunked-encoding-disabled false
;     :accelerate-mode-enabled false
;     :payload-signing-enabled true
;     :dualstack-enabled true
;     :force-global-bucket-access-enabled true})
