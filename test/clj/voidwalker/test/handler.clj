(ns voidwalker.test.handler
  (:require [clojure.test :refer :all]
            [muuntaja.core :as muuntja]
            [ring.mock.request :refer :all]
            [voidwalker.handler :refer :all]))

(def m (muuntja/create))

(defn json-request [request]
  (->> ((app) request)
       :body
       slurp
       (muuntja/decode m "application/json")))

(defn contains-many? [m & ks]
  (every? #(contains? m %) ks))

(deftest test-app
  (testing "main route"
    (let [response ((app) (request :get "/"))]
      (is (= 200 (:status response)))))

  (testing "not-found route"
    "because routing of wild card will be handled at client side..Scary!!!"
    (let [response ((app) (request :get "/some-random-route"))]
      (is (= 200 (:status response)))))

  (testing "list posts"
    (let [response (json-request (request :get "/article"))]
      (is (and (seq? response)
               (contains-many? (first response) :url :content))))))

