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
    [nav-link {:route :voidwalker.add
               :text "Add"
               :nav? true}]
    [nav-link {:route :voidwalker.template
               :text "Templates"
               :nav? true}]
    [nav-link {:route :voidwalker.inline-editor
               :text "Inline editor"
               :nav? true}]]])

(def fcon (r/atom {:id 1}))


(defn input [{:keys [state placeholder type class]}]
  (println "State for " placeholder @state)
  [:div.field>div.control [:input.input
                           {:placeholder placeholder
                            :class class
                            :value @state
                            :type (or type "text")
                            :on-change #(reset! state (-> % get-value))}]])

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

(defn progress-info [data]
  (let [{:keys [class value]} (case data
                                :loading {:class "alert-info"
                                          :value "Saving Boss.."}
                                :success {:class "alert-success"
                                          :value "Saved.."}
                                :error {:class "alert-fail"
                                        :value "Failed to save"}
                                {:class "hidden"
                                 :value "Should not be seen.."})]
    [:div>div.alert {:class class
                     :role "alert"} value]))


(defn file-view
  "ch is the channel on which name will be sent when the
  delete button is clicked"
  [name ch]
  [:div
   [:div>span.icon.is-large
    [:i.fab.fa-css3-alt.fa-3x]]
   [:div name
    [:span.icon.is-medium {:on-click #(go (>! ch name))}
     [:i.fas.fa-trash-alt]]]])

(defn delete-css
  [files n]
  "will delete file identified by n from files atom"
  (remove #(= n (:name %)) files))


(def new-article (r/atom {}))

(defn datasource-view [ds]
  [:div
   (println "source " (-> ds first :name))
   (for [{name :name} ds]
     [file/file-view {:name (str "Datasource: " name)
                      :key :datasource}])])

(defn add-post-form [& {[id {:keys [url tags content title css datasource]}] :data}]
  (r/with-let [url (r/atom url)
               tags (r/atom (some->> tags (str/join ",")))
               title (r/atom title)
               content (r/atom content)
               wb (r/atom @content)
               post-status (rf/subscribe [:new/post-status])]
    [:div.section>div.container
     [:div.title "New Article"]
     [:form
      [input {:state url
              :placeholder "Enter url"}]
      [input {:placeholder "Comma separated keywords/tags"
              :state tags}]
      [input {:placeholder "Enter title"
              :state title}]
      [:div.field [(editor content wb)]]
      [file/view {:type :datasource
                  :id id
                  :db-key :articles
                  :process csv->map
                  :placeholder "Add a datasource"}]
      [file/view {:type :css
                  :id id
                  :db-key :articles
                  :placeholder "Upload css"}]
      [:div.field>div.control>div>button.button.is-medium.is-primary
       {:on-click (fn [e]
                    (let [article  {:url @url
                                    :id (when-not (= id ::new) id)
                                    :tags (str/split @tags #",")
                                    :content @wb
                                    :datasource @(rf/subscribe [::file/files ::new :datasource])
                                    :css @(rf/subscribe [::file/files id :css])
                                    :title @title}]
                      (.preventDefault e)                      
                      (when (and (some? id)
                                 (not= id ::new)) (rf/dispatch [:update-article article]))
                      (rf/dispatch [:save-article article])))}
       "Save articleaa"]
      [progress-info @post-status]]]))

(defn add-post
  ([] (add-post-form :data [::new nil]))
  ([id] (r/with-let [article-data @(rf/subscribe [:article id])]
          (add-post-form :data [id article-data]))))

; (:content @new-article)

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
    [nav-link {:route :voidwalker.edit
               :params {:id id}
               :text title}]
    [:span.icon.is-medium.is-pulled-right  {:on-click #(delete-post id)}
     [:i.fas.fa-trash-alt]]]])

(defn home-page []
  (r/with-let [post-ch (chan 10 delete-post)]
    [:section.section>div.container
     [:h1.title "List of Posts"]
     (for [[id {:keys [title]}] @(rf/subscribe [:articles])]
       (article-view id title))]))


