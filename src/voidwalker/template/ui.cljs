(ns voidwalker.template.ui
  (:require [re-frame.core :as rf]
            [cljs.tools.reader :refer [read-string]]
            [voidwalker.template.ranklist :as r]
            [snow.files.ui :as files]
            [voidwalker.source.core :refer [csv->map]]
            [cljs.js :refer [eval empty-state js-eval]]))


(rf/reg-sub
 :voidwalker.template/tmpl
 (fn [db _] (:voidwalker.template/tmpl db)))

(def datasource-sub [::files/files :articles ::new :datasource])

(defn template-view []
  [:div
   [:section.section>div.container [r/root-tmpl (rf/subscribe datasource-sub)]]
   [:section.section>div.container [files/view {:type :datasource
                                                :id ::new
                                                :db-key :articles
                                                :process csv->map
                                                :placeholder "Add a datasource"}]]])


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


;; (eval-str (-> @(rf/subscribe [:voidwalker.template/tmpl])
;;               first
;;               :voidwalker.template/fn))
;; (println "string/: " (read-string "(+ 1 1)"))
;; (eval-str (read-string "(+ 1 1)"))

;; (eval (empty-state)
;;       [1 2 3]
;;       {:eval       js-eval
;;        :source-map true
;;        :context    :expr}
;;       (fn [x] (prn "*****" x)))
