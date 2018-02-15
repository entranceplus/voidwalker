(ns voidwalker.source.routes
  (:require [bide.core :as b]))

(defn transform-map [rmap]
  (into []
        (for [[r u] rmap]
          [u r])))

(defn make-bide-router [rmap]
  (b/router (transform-map rmap)))

(def route-map {:voidwalker.home "/home"
                :voidwalker.add "/add"
                :voidwalker.edit "/edit/:id"})

(def router (make-bide-router route-map))

(defn nav-link [{:keys [route text params]}]
  [:a {:href (route route-map)
       :on-click (fn [e]
                   (do
                     (-> e .preventDefault)
                     (println "clicked " route router)
                     (b/navigate! router route params)))}
   text])

