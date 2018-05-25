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
            [cljs.js :refer [eval empty-state js-eval]]))


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

(defn set-content [editor-component]
  (->> (.-innerHTML editor-component)
       h/parse-fragment
       (map h/as-hiccup)
       first))

(def *editor-content* (fn [] (some-> @editor-component set-content)))

(defn root-tmpl [{:keys [template on-change content]}]
  ;; (on-change)
  (fn [{:keys [data template on-change content]}]    
    [:div {:ref #(reset! editor-component %)
           :on-input on-change
           :on-blur on-change
           :content-editable true
           :dangerouslySetInnerHTML {:__html (html (rk/template @content))}}]))

(defn content! [id]
  (rf/dispatch [:editor-change id (*editor-content*)]))

(rf/reg-event-db
 ::update
 (fn [db [_ id tmpl]]
   (println "updating ar " id tmpl)
   (assoc-in db [:articles id :tmpl] tmpl)))

(def tmpl :ranklist)
(def article (rf/subscribe [:article :620f64b6-8811-42c8-ab4b-0ae703b9ecd7]))

(defn render [{:keys [id data tmpl on-change content]}]
  ;; (rf/dispatch [::update id tmpl])
  (println "tmpl is " data tmpl content)
  [root-tmpl {;; :data @data
              :template  rk/template
              :on-change (partial content! id)
              :content content}]
  ;; (case tmpl
  ;;   :ranklist 
  ;;   [:div "Could not parse article"])
  )

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
 (fn [db [_ id file-data]]
   (println "content is file " file-data)
   (update-in db
              [:articles id :content]
              #(cond-> %
                 (some? file-data)  (conj [:section.exam-list
                                           (map-indexed rk/data-tmpl file-data)])))))

(defn inline-editor
  "fun is the template fn and id is articles id..
  both are strings and have to be converted to keyword eventually"
  [fun id]
  (r/with-let [file-data (rf/subscribe [::files/files ::new :datasource])
               post-status (rf/subscribe [:new/post-status])
               article  (rf/subscribe [:article (keyword id)])
               content (rf/subscribe [:article-content (keyword id)])]
    (println "Article id is " (keyword id) content)
    [:div
     [:section.section>div.container [files/view {:type :datasource
                                                  :id ::new
                                                  :dispatch [:data-change (keyword id)]
                                                  :process csv->map
                                                  :placeholder "Add a datasource"}]]
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
        {:on-click #(rf/dispatch [:navigate {:route :voidwalker.template.edit 
                                             :params {:fun fun
                                                      :id :voidwalker.content.ui/new}}])}
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
