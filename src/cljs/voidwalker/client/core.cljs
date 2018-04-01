(ns voidwalker.client.core)

(println "Go away: A print inside client/core.cljs")
;   (:require [reagent.core :as r]
;             [secretary.core :as secretary]
;             [goog.events :as events]
;             [goog.history.EventType :as HistoryEventType]
;             [accountant.core :as accountant])
;   (:import goog.History))
;
; (defn page []
;   [:div "There will be magic here"])
;
; ;;;;;;;;;;;;
; ;; routes ;;
; ;;;;;;;;;;;;
;
; (secretary/set-config! :prefix "")
;
; ;; (secretary/defroute "/" []
; ;;   (rf/dispatch [:set-active-page :home]))
;
; ;; (secretary/defroute "/add" []
; ;;   (rf/dispatch [:set-active-page :add]))
;
; ;; (secretary/defroute "/about" []
; ;;   (rf/dispatch [:set-active-page :about]))
;
; ;; (secretary/defroute "/edit/:id" {id :id}
; ;;   (rf/dispatch [:set-active-page :edit id]))
;
; (accountant/configure-navigation! {:nav-handler  (fn [path]
;                                                    (secretary/dispatch! path))
;                                    :path-exists? (fn [path]
;                                                    (secretary/locate-route path))})
; ;; -------------------------
; ;; History
; ;; must be called after routes have been defined
; (defn hook-browser-navigation! []
;   (doto (History.)
;     (events/listen
;       HistoryEventType/NAVIGATE
;       (fn [event]
;         (secretary/dispatch! (.-token event))))
;     (.setEnabled true)))
;
; (.addEventListener js/document
;                    "DOMContentLoaded"
;                    #(do (println "dom loaded" (.. js/document -location -pathname))
;                         (secretary/dispatch! (.. js/document -location -pathname))))
;
;
;
; (defn mount-components []
;   (rf/clear-subscription-cache!)
;   (r/render [#'page] (.getElementById js/document "app")))
;
; (defn init! []
;   (hook-browser-navigation!)
;   (mount-components))
