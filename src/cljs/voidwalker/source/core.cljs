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
            [hickory.core :as h]
            [voidwalker.source.routes :refer [nav-link]])
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
                :nav? true}]]])

(def fcon (r/atom {:id 1}))

(def ^:private newlines
  {:lf "\n" :cr+lf "\r\n"})

(def ^:private newline-error-message
  (str ":newline must be one of [" (str/join "," (keys newlines)) "]"))

(defn read-csv
  "Reads data from String in CSV-format."
  [data & options]
  (let [{:keys [separator newline] :or {separator "," newline :lf}} options]
    (if-let [newline-char (get newlines newline)]
      (->> (str/split data newline-char)
           (map #(str/split % separator)))
      (throw (js/Error. newline-error-message)))))

(defn add-tag
  [tag coll]
  (let [o-tag (str "<" (name tag) ">")
        c-tag (str "</" (name tag) ">")]
    (reduce (fn [acc e]
              (str acc o-tag e c-tag)) "" coll)))


(defn table-from-csv [csv-data]
  (let [csv (read-csv csv-data :newline :cr+lf)]
    (add-tag :table [(add-tag :tr [(add-tag :th (first csv))])
                     (add-tag :tr (map (fn [row]
                                         (add-tag :td row)) (rest csv)))])))

(defn append-data [current-data file-content]
  (reset! fcon (table-from-csv file-content))
  (str current-data "<br />" (table-from-csv file-content)))

(defn update-content
  [content wb]
  "This is a transducer which will read data from the wb atom and
   write data to content atom"
  (map (fn [{data :file-content}]
         (swap! wb append-data data)
         (reset! content @wb))))

(defn get-files [e]
  (array-seq (.. e -target -files)))

(defn read-file
  "read file and dispatch an event of [:file-content {:id :content}]"
  [file c]
  (println "Trying to read file name " (.-name file))
  (let [reader (js/FileReader.)]
    (gobj/set reader
              "onload"
              (fn [e]
                (println "event called ")
                (go (>! c {:file-content (.. e -target -result)
                           :name (.-name file)}))))
    (.readAsText reader file)))

; (defn process-file
;   [{{:keys [id content]} :file-content :as data}]
;   (println "The id is " id "and content is " content)
;   data)
;
; (def file-processor (map process-file))

(defn file-input [{:keys [placeholder id]} c]
  [:div.field.file.is-boxed>label.file-label
   [:div.field>div.control
    [:input.input.file-input
     {:type "file"
      :on-change (fn [e]
                   (-> e get-files first (read-file c)))}]
    [:span.file-cta
      [:span.file-icon>i.fas.fa-upload]
      [:span.file-label placeholder]]]])


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
      {:value @content
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

; <span class="icon is-large">
;   <span class="fa-stack fa-lg">
;     <i class="fas fa-camera fa-stack-1x"></i>
;     <i class="fas fa-ban fa-stack-2x has-text-danger"></i>
;   </span>
; </span>
(defn file-view [name ch]
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

(defn css-container
  [files]
  "A component containing upload button for css and the corres. file
   indicators. Will have the files in files atom."
  (r/with-let [ch (chan 10 (map #(swap! files conj %)))
               del-ch (chan 10 (map (fn [name]
                                      (swap! files delete-css name))))]
    [:div.columns
     [:div.column [file-input {:placeholder "Upload css"} ch]]
     [:div.column>div.columns
      (for [{:keys [name]} @files]
        [:div.column {:key name} [file-view name del-ch]])]]))


(def new-article (r/atom {}))

(defn add-post-form [& {{:keys [url tags content title id css]} :data}]
  (r/with-let [url (r/atom url)
               tags (r/atom (str/join "," tags))
               title (r/atom title)
               content (r/atom content)
               wb (r/atom @content)
               file-chan (chan 10 (update-content content wb))
               css-files (r/atom (map (fn [url] {:name url}) css))
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
      [file-input {:placeholder "Add a datasource"} file-chan]
      [css-container css-files]
      [:div.field>div.control>div>button.button.is-medium.is-primary
       {:on-click (fn [e]
                    (let [article  {:url @url
                                    :id id
                                    :tags (str/split @tags #",")
                                    :css @css-files
                                    :content @wb
                                    :title @title}]
                      (.preventDefault e)
                      (when (some? id) (rf/dispatch [:update-article article]))
                      (rf/dispatch [:save-article article])))}
       "Save article"]
      [progress-info @post-status]]]))

(defn add-post
  ([] (add-post-form))
  ([id] (r/with-let [article-data @(rf/subscribe [:article id])]
          (add-post-form :data article-data))))

; (:content @new-article)

;; (def articles @(rf/subscribe [:articles]))

;; (def t (->> articles
;;             (filter #(= (:id %) "f7f977dd-20dd-4193-aea5-826682c8a9c4"))
;;             first
;;             :tags))

;;;;;;;;;;;;;;;
;; home-page ;;
;;;;;;;;;;;;;;;

(defn home-page []
  (fn []
    [:section.section>div.container
     [:h1.title "List of Posts"]
     (map (fn [{:keys [title id]}]
            (println "keys are " title id)
            [:ul {:key id}
             [nav-link {:route :voidwalker.edit
                        :params {:id id}
                        :text title}]])
          @(rf/subscribe [:articles]))]))
