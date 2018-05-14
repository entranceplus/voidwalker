(ns voidwalker.template.ui
  (:require [re-frame.core :as rf]
            [cljs.tools.reader :refer [read-string]]
            [reagent.core :as r]
            [snow.files.ui :as files]
            [voidwalker.template.ranklist :as rk :refer-macros [converter]]
            [voidwalker.source.csv :refer [csv->map]]
            [cljs.js :refer [eval empty-state js-eval]]))


(rf/reg-sub
 :voidwalker.template/tmpl
 (fn [db _] (:voidwalker.template/tmpl db)))

(def datasource-sub [::files/files :articles ::new :datasource])

(rf/reg-sub
 :voidwalker.template/fn
 (fn [_ _] (rf/subscribe [:voidwalker.template/tmpl]))
 (fn [templates [_ name]]
   (->> templates
        (filter #(= (:voidwalker.template/name %) name))
        first)))

(def f @(rf/subscribe [:voidwalker.template/fn "Ranklist"]))

;; ((-> f :voidwalker.template/fn symbol))

(defn inline-editor [template-name]
  (r/with-let [tmpl (rf/subscribe [:voidwalker.template/fn "Ranklist"])]
    (println "tmple " @tmpl)
    [:div
     [:section.section>div.container (rk/render :ranklist nil)]
     
     [:section.section>div.container [files/view {:type :datasource
                                                  :id ::new
                                                  :db-key :articles
                                                  :process csv->map
                                                  :placeholder "Add a datasource"}]]]))

(defn template-view []
  (r/with-let [tmpls (rf/subscribe [:voidwalker.template/tmpl])]
    (println "tmpls s " tmpls)
    [:div>section.section>div.columns
     (for [{:keys [voidwalker.template/name]} @tmpls]
       [:div.column.is-one-quarter>div.box.template
        {:on-click #(rf/dispatch [:navigate {:route :voidwalker.template.edit 
                                             :params {:name name}}])}
        name])]))



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
