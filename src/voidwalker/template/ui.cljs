(ns voidwalker.template.ui
  (:require-macros [hiccups.core :refer [html]])
  (:require [re-frame.core :as rf]
            [cljs.tools.reader :refer [read-string]]
            [reagent.core :as r]
            [snow.files.ui :as files]
            [snow.ui.components :as ui]
            [hickory.core :as h]
            [voidwalker.template.ranklist :as rk]
            [voidwalker.source.csv :refer [csv->map]]
            [voidwalker.images.core :as i]
            [voidwalker.images.thumbnail :as thumb]
            [cljs.js :refer [eval empty-state js-eval]]
            ["@tinymce/tinymce-react" :refer (Editor)]))

(rf/reg-sub
 :voidwalker.template/tmpl
 (fn [db _] (:voidwalker.template/tmpl db)))

(def datasource-sub [::files/files ::new :datasource])

(rf/reg-sub
 :voidwalker.template/fn
 (fn [_ _] (rf/subscribe [:voidwalker.template/tmpl]))
 (fn [templates [_ name]]
   (->> templates
        (filter #(= (:voidwalker.template/name %) name))
        first)))

(def f @(rf/subscribe [:voidwalker.template/fn "Ranklist"]))

;; ((-> f :voidwalker.template/fn symbol))

(rf/reg-event-db
 :editor-change
 (fn [db [_ id content]]
   (assoc-in db [:articles id :content] content)))

;; used to hold refs to the editable document
(def editor-component (r/atom 1))

(defn html->hiccup [html]
  (->> html
       h/parse-fragment
       (map h/as-hiccup)
       (conj [:div])))

(def *editor-content* (fn [] (some-> @editor-component
                                    .-innerHTML
                                    html->hiccup)))

(defn root-tmpl [{:keys [template on-change content]}]
  ;; (on-change)
  (r/create-class {:should-component-update (fn [this
                                                [_ {d0 :data c0 :content}]
                                                [_ {d1 :data c1 :content}]]
                                              (if (or (not= d0 d1)
                                                      (and (nil? c0)
                                                           (some? c1)))
                                                (do (println "re rendering editor")
                                                    true)
                                                false))
                   :display-name "inline-editor"
                   :reagent-render (fn [{:keys [template on-change content]}]
                                     [:div {:ref #(reset! editor-component %)
                                            :on-input on-change
                                            :on-blur on-change
                                            :content-editable true
                                            :dangerouslySetInnerHTML {:__html (html (template content))}}])}))

(defn content! [id]
  (rf/dispatch [:editor-change id (*editor-content*)]))

(rf/reg-event-db
 ::update
 (fn [db [_ id tmpl]]
   (assoc-in db [:articles id :tmpl] tmpl)))

(def tmpl :ranklist)
(def id :620f64b6-8811-42c8-ab4b-0ae703b9ecd7)
(def article (rf/subscribe [:article id]))

(-> @article :content)

;; [root-tmpl {:template rk/article-template
;;             :data @data
;;             :on-change (partial content! id)
;;             :content @content}]

(defn tinymce-editor
  [content {id :id}]
  (r/create-class {:should-component-update (fn [this [_ c0 _] [_ c1 _]]
                                              (and (nil? c0)
                                                   (not= id :new)))
                   :display-name "tinymce-wrapper"
                   :reagent-render (fn [content {id :id}]
                                     [(r/adapt-react-class Editor)
                                      {:value (html content)
                                       :init  {"plugins" "link image table"
                                               "height" 200}
                                       :on-editor-change (fn [content]
                                                           (println "on-change " )
                                                           (rf/dispatch [:editor-change id  (->> content
                                                                                                 html->hiccup)]))}])}))

(defn render [{:keys [id data tmpl on-change content]}]
  (rf/dispatch [::update id tmpl])
  (case tmpl
    (or :ranklist :examlist) [root-tmpl {:template  rk/template
                                         :data @data
                                         :on-change (partial content! id)
                                         :content @content}]
    :blog [tinymce-editor @content {:id id}]
    [:div "Could not parase article"]))

(defn progress-info
  "element to state wether the article was saved or not"
  [data]
  (let [{:keys [class value]} (case data
                                :loading {:class "alert-info"
                                          :value "Saving Boss.."}
                                :success {:class "alert-success"
                                          :value "Saved.."}
                                :error {:class "alert-fail"
                                        :value "Failed to save"}
                                {:class "is-hidden"
                                 :value "Should not be seen.."})]
    [:div>div.alert {:class class
                     :role "alert"} value]))


(rf/reg-event-db
 :data-change 
 (fn [db [_ {:keys [id file-data template]}]]
   (println "data change" id template)
   (let [map-fn (case template
                  :ranklist rk/data-tmpl
                  :examlist rk/engg-list-tmpl
                  rk/data-tmpl)]
     (update-in db
                [:articles id :content]
                #(cond-> %
                   (some? file-data)  (conj [:section.exam-list
                                             (map-indexed map-fn file-data)]))))))

(defn inline-editor
  "fun is the template fn and id is articles id..
  both are strings and have to be converted to keyword eventually"
  [fun id]
  (r/with-let [file-data (rf/subscribe [::files/files ::new :datasource])
               post-status (rf/subscribe [:new/post-status])
               article  (rf/subscribe [:article (keyword id)])
               content (rf/subscribe [:article-content (keyword id)])]
    [:div
     [:section.section>div.container [files/view {:type :datasource
                                                  :id ::new
                                                  :dispatch [:data-change {:id (keyword id)
                                                                           :template (keyword fun)}]
                                                  :process csv->map
                                                  :placeholder "Add a datasource"}]]
     [thumb/ui (keyword id)]
     [i/image]
     [:section.section>div.container
      [ui/rx-input {:db-key [:articles
                             (keyword id)
                             :url]
                    :placeholder "Enter url"}]
      [ui/rx-input {:db-key [:articles
                             (keyword id)
                             :title]
                    :placeholder "Enter title"}]]
     [:section.section>div.container
      [:div.button.is-medium.is-primary
       {:on-click #(rf/dispatch [:save-article (keyword id)])} "Save article"]
      [:div [progress-info @post-status]]]
     
     [:section.section>div.container [render {:id (keyword id)
                                              :tmpl (or (:tmpl @article)
                                                        (keyword fun))
                                              :data file-data
                                              :content content}]]]))

(defn template-view
  "card view for selecting templates"
  []
  (r/with-let [tmpls (rf/subscribe [:voidwalker.template/tmpl])]
    [:div>section.section>div.columns
     (for [{:keys [voidwalker.template/fun]} @tmpls]
       [:div.column.is-one-quarter>div.box.template
        {:key fun
         :on-click #(rf/dispatch [:navigate {:route :voidwalker.template.edit 
                                             :params {:fun fun
                                                      :id :voidwalker.content.ui/new}
                                             :perform? true}])}
        (name fun)])]))



;; (e/code->results ["(+ 1 1)"] (fn [res] (println "res is " res)))
;; (e/code->results ['(def n 4) '(conj [1 2 3] n)] println) 
(println "Tyrying")

;; (defn eval-str [s]
;;   (eval (empty-state)
;;         s
;;         {:eval       js-eval
;;          :source-map true
;;          :context    :expr}
;;         (fn [x] (prn "****|||" x))))


;; (eval-str (-> f
;;               :voidwalker.template/fn))
;; (println "string/: " (read-string "(+ 1 1)"))
;; (eval-str (read-string "(+ 1 1)"))

;; (eval (empty-state)
;;       [1 2 3]
;;       {:eval       js-eval
;;        :source-map true
;;        :context    :expr}
;;       (fn [x] (prn "*****" x)))
