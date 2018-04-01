(ns voidwalker.source.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]

            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]

            [ajax.core :refer [GET POST]]
            [voidwalker.source.ajax :refer [load-interceptors!]]
            [voidwalker.source.handlers]
            [voidwalker.source.subscriptions]
            [voidwalker.source.util :refer [get-value]]
            [voidwalker.source.routes :refer [nav-link]])
  (:require ["@tinymce/tinymce-react" :refer (Editor)])
  (:import goog.History))

(defn about-page []
  [:div.container
   [:div.row
    [:div.col-md-12
     [:img {:src (str js/context "/img/warning_clojure.png")}]]]])

(defn navbar []
  [:nav.navbar.navbar-default>div.container-fluid
   [:div.navbar-header>span.navbar-brand [nav-link {:route :voidwalker.home
                                                    :text "Entranceplus"}]]
   [:div.nav.navbar-nav
    [:li {:class (when (= @(rf/subscribe [:page]) :add)
                   "active")}
     [nav-link {:route :voidwalker.add
                :text "Add"}]]]])


(defn input [{:keys [state placeholder type]}]
  (println "State for " placeholder @state)
  [:div.form-group [:input.form-control
                    {:placeholder placeholder
                     :value @state
                     :type (or type "text")
                     :on-change #(reset! state (-> % get-value))}]])

(defn editor [content]
  (fn []
    [(r/adapt-react-class Editor)
     {:content (or @content "Go fuck")
      :initial-value "Welcome"
      :init  {"plugins" "link image table"}
      :on-change (fn [e] (reset! content (-> e .-target .getContent)))}]))

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


(defn add-post-form [& {{:keys [url tags content title id]} :data}]
  (r/with-let [url (r/atom url)
               tags (r/atom tags)
               title (r/atom title)
               content (r/atom content)
               post-status (rf/subscribe [:new/post-status])]
    (println "passed data url: " @url @post-status)
    [:div.container
     [:h1 "New Article"]
     [:form
      [input {:state url
              :placeholder "Enter url"}]
      [input {:placeholder "Comma separated keywords/tags"
              :state tags}]
      [input {:placeholder "Enter title"
              :state title}]
      [:div.form-group [(editor content)]]
      [:div.form-group>button.btn.btn-primary
       {:on-click (fn [e]
                    (.preventDefault e)
                    (println "content " @content)
                    (rf/dispatch [:save-article
                                  {:url @url
                                   :id id
                                   :tags @tags
                                   :content @content
                                   :title @title}]))}
       "Save article"]
      [progress-info @post-status]]]))

(defn add-post
  ([] (add-post-form))
  ([id] (r/with-let [article-data @(rf/subscribe [:article id])]
          (add-post-form :data article-data))))

;;;;;;;;;;;;;;;
;; home-page ;;
;;;;;;;;;;;;;;;

(defn home-page []
  (fn []
    [:div.container
     [:h1 "List of Posts"]
     (map (fn [{:keys [title id]}]
            (println "keys are " title id)
            [:div {:key id}
             [nav-link {:route :voidwalker.edit
                        :params {:id id}
                        :text title}]])
          @(rf/subscribe [:articles]))]))
