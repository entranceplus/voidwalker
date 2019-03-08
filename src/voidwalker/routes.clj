(ns voidwalker.routes
  (:require [compojure.core :refer [routes GET ANY]]
            [ring.util.http-response :as response]
            [ring.middleware.anti-forgery :as anti-forgery]
            [clojure.java.io :as io]))

(require '[hiccup.core :as h])

(defn ok-response [response]
  (-> (response/ok response)
     (response/header "Content-Type" "text/plain")))

(defn hello-routes [_]
  (routes
   (GET "/hello" [] (ok-response "Hello world!!"))))

(defn  preload-html [csrf-token]
  (h/html [:html [:head
                  [:meta {:content "text/html; charset=UTF-8", :http-equiv "Content-Type"}]
                  [:meta {:content "width=device-width, initial-scale=1", :name "viewport"}]
                  [:title "A true voidwalker"]
                  [:link
                   {:crossorigin "anonymous",
                    :href "https://cdnjs.cloudflare.com/ajax/libs/bulma/0.6.2/css/bulma.min.css",
                    :rel "stylesheet"}]
                  [:script {:src "https://code.jquery.com/jquery-3.2.1.min.js"}]
                  [:link
                   {:type "text/css", :rel "stylesheet", :href "/css/screen.css"}]
                  [:link
                   {:type "text/css", :rel "stylesheet", :href "/css/editor.css"}]
                  [:link {:href "https://entranceplus.in/css/ranklist.css",
                          :type "text/css",
                          :rel "stylesheet"}]
                  [:link {:href "https://entranceplus.in/css/default.css",
                          :type "text/css",
                          :rel "stylesheet"}]
                  [:link {:href "https://entranceplus.in/css/examlist.css",
                          :type "text/css",
                          :rel "stylesheet"}]
                  [:link {:href "https://entranceplus.in/css/article-common.css",
                          :type "text/css",
                          :rel "stylesheet"}]]
           [:body {:data-csrf-token csrf-token}
            [:div#app.animation-container
             [:div.l-circle]
             [:div.l-circle-small]
             [:div.l-circle-big]
             [:div.l-circle-inner-inner]
             [:div.l-circle-inner]]
            [:script
             {:src "https://use.fontawesome.com/releases/v5.0.6/js/all.js",
              :defer "defer"}]
            [:script
             {:src "https://cdnjs.cloudflare.com/ajax/libs/tinymce/4.7.4/jquery.tinymce.min.js"}]
            [:script
             {:src "https://cdnjs.cloudflare.com/ajax/libs/tinymce/4.7.4/tinymce.min.js"}]
            [:script {:src "https://cloud.tinymce.com/stable/tinymce.js"}]
            [:script
             {:src "https://cdnjs.cloudflare.com/ajax/libs/tinymce/4.7.4/plugins/table/plugin.js"}]
            [:script
             {:src "https://cdnjs.cloudflare.com/ajax/libs/tinymce/4.7.4/plugins/image/plugin.js"}]
            [:script {:src
                      "https://cdnjs.cloudflare.com/ajax/libs/tinymce/4.7.4/plugins/code/plugin.js"}]]
           [:script {:src "/js/app.js"}]]))



(defn home-page [req]
  (-> (preload-html (:anti-forgery-token req))
     response/ok
     (response/header "Content-Type" "text/html"))) 

(defn site [_]
  (routes
   (GET "/" req (home-page req))
   (ANY "*" req (home-page req))))
