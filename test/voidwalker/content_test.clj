(ns voidwalker.content-test
  (:require [snow.client :as client]
            [clojure.test :refer :all]
            [voidwalker.content :refer :all]))

(def c1 (client/restclient "http://localhost:8000"))

(def sample {:voidwalker.content/url "new-url"
             :voidwalker.content/title "a glorious title"
             :voidwalker.content/tags ["tag2"]
             :voidwalker.content/content "A content"})

(defn success? [request]
  (is (= 200 (:status request))))

(defn test-add [res body]
  (let [r (c1 :post res :body body)]
    (success? r)
    (-> r :body :id)))

(defn test-update [res body id]
  (let [r (c1 :post res
              :body (merge body
                      {:voidwalker.content/id id}))]
    (success? r)))

(defn get-articles []
  (c1 :get "/articles"))

(deftest editor-api
  (testing "Articles"
    (is (contains? (get-articles) :articles))
    (let [id (test-add "/articles" sample)
          new-body (assoc sample :voidwalker.content/title "awakening")]
      (test-update "/articles" new-body id)
      (is (-> get-articles :articles :title "awakening")))))
