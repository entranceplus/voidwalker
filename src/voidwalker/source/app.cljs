(ns voidwalker.source.app
  (:require [reagent.core :as r]
            [bide.core :as b]
            [snow.comm.core :as comm]
            [snow.router :as router]
            [voidwalker.source.routes :refer [route-map]]
            [voidwalker.source.core :as v]
            [voidwalker.template.ui :as t]
            [voidwalker.source.inline :as i]
            ;; to ensure all subscriptions are loaded
            [voidwalker.source.subscriptions]
            [re-frame.core :as rf]))

(enable-console-print!)

(defn page [route params]
  [:div
   [v/navbar]
   (case route
     :voidwalker.home          [v/home-page]
     :voidwalker.template      [t/template-view]
     :voidwalker.template.edit [t/inline-editor (:fun params) (:id params)]
     :voidwalker.inline-editor [i/page ])])


(defn on-navigate
  "A function which will be called on each route change."
  [route params query]
  (println "Route change to: " route params query)
  (rf/dispatch  [:navigate {:route route
                            :params  params
                            :perform? false}])
  (r/render [page route params] 
            (js/document.getElementById "app")))

(defn main []
  (println "At least main! called ")
  (comm/start!)
  (rf/dispatch-sync [:initialize-db])
  (rf/clear-subscription-cache!)
  (router/start! route-map on-navigate))


(main)
