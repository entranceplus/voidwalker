(ns voidwalker.source.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [clojure.string :as str]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [goog.object :as gobj]
            [ajax.core :refer [GET POST]]
            [voidwalker.source.ajax :refer [load-interceptors!]]
            [voidwalker.source.handlers]
            [cljs.core.async :refer [>! <! chan]]
            [clojure.string :as str]
            [voidwalker.source.subscriptions]
            [voidwalker.source.util :refer [get-value]]
            [snow.files.ui :as file]
            [snow.ui.components :refer [input]]
            [hickory.core :as h]
            [voidwalker.source.routes :refer [nav-link]]
            [voidwalker.source.csv :refer [csv->map]])
  (:require ["@tinymce/tinymce-react" :refer (Editor)])
  (:import goog.History))

(defn about-page []
  [:div.container
   [:div.row
    [:div.col-md-12
     [:img {:src (str js/context "/img/warning_clojure.png")}]]]])

(defn navbar []
  [:nav.navbar.is-black
   [:div.navbar-brand
    [nav-link {:route :voidwalker.home
               :nav? true
               :image {:src "https://entranceplus.in/images/header/ep-logo-white.svg"
                       :width "112"
                       :height "48"}}]]
                                        ;
                                        ; [:div {:class (when (= @(rf/subscribe [:page]) :add
                                        ;                             "is-active"))}
   [:div.navbar-menu>div.navbar-start
    [nav-link {:route :voidwalker.template
               :text "Templates"
               :nav? true}]
    [nav-link {:route :voidwalker.inline-editor
               :text "Inline editor"
               :nav? true}]]])

(def fcon (r/atom {:id 1}))


(defn editor
  [content wb-atom]
  "content is the atom from where content is being sent to the editor,
   wb-atom is the atom to which editor writes its changes. They can't be
   one atom because then it will re render after each change causing
   cursor to jump to start"
  (fn []
    [(r/adapt-react-class Editor)
     {:value @wb-atom
      :init  {"plugins" "link image table"
              "height" 200}
      :on-change (fn [e]
                   (reset! wb-atom (-> e .-target .getContent)))}]))

;;;;;;;;;;;;;;;;;;;;;;
;; new article form ;;
;;;;;;;;;;;;;;;;;;;;;;


;; (:content @new-article)

;; (def articles @(rf/subscribe [:articles]))

;; (def t (->> articles
;;             (filter #(= (:id %) "f7f977dd-20dd-4193-aea5-826682c8a9c4"))
;;             first
;;             :tags))

;;;;;;;;;;;;;;;
;; home-page ;;
;;;;;;;;;;;;;;;

(defn delete-post
  [id]
  (rf/dispatch [:delete-article id]))

(defn article-view
  [id title]
  [:div.box {:key id}
   [:ul
    [nav-link {:route :voidwalker.template.edit
               :params {:id id
                        :fun :ranklist}
               :text title}]
    [:span.icon.is-medium.is-pulled-right  {:on-click #(delete-post id)}
     [:i.fas.fa-trash-alt]]]])

(defn home-page []
  [:section.section>div.container
   [:h1.title "List of Posts"]
   (for [[id {:keys [title]}] @(rf/subscribe [:articles])]
     (article-view id title))])


