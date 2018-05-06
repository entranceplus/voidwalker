(ns voidwalker.source.app
  (:require [reagent.core :as r]
            [bide.core :as b]
            [voidwalker.source.core :as v]
            [voidwalker.template.ui :as t]
            [voidwalker.source.routes :refer [router]]
            [snow.comm.core :as comm]
            [re-frame.core :as rf]
            [voidwalker.source.inline :as i]))

(enable-console-print!)


(defn page [name params]
  [:div
   [v/navbar]
   (case name
     :voidwalker.home [v/home-page]
     :voidwalker.add [v/add-post]
     :voidwalker.edit [v/add-post (:id params)]
     :voidwalker.template [t/template-view]
     :voidwalker.inline-editor [i/page])])

(defn on-navigate
  "A function which will be called on each route change."
  [name params query]
  (println "Route change to: " name params query)
  (rf/dispatch [:set-active-page name params])
  (rf/dispatch [:post-status-init])
  (r/render [page name params]
            (js/document.getElementById "app")))


(defn main []
  (println "At least main! called ")
  (rf/dispatch-sync [:initialize-db])
  (comm/start!)
  (rf/clear-subscription-cache!)
  (b/start! router {:default :voidwalker.home
                    :html5? true
                    :on-navigate on-navigate}))


(main)
