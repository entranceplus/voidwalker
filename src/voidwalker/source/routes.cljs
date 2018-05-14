(ns voidwalker.source.routes
  (:require [re-frame.core :as rf]))

(def route-map {:voidwalker.home "/home"
                :voidwalker.add "/add"
                :voidwalker.edit "/edit/:id"
                :voidwalker.template "/template"
                :voidwalker.template.edit "/template/edit/:name"
                :voidwalker.inline-editor "/inline/editor"})

(defn nav-link [{:keys [route text params image nav? class]}]
  (println "texts is " text)
  [:a {:href (route route-map)
       :class (or class
                  (when nav? "navbar-item is-hoverable"))
       :on-click (fn [e]
                   (do
                     (-> e .preventDefault)
                     (println "cliscked " route)
                     (rf/dispatch [:navigate {:route route
                                              :param params}])))}
   (if (some? text)
     text
     [:img image])])

