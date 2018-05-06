(ns voidwalker.core-test
  (:require [cljs.test :refer-macros [is are deftest testing use-fixtures]]
            [snow.comm.core :as comm]
            [re-frame.core :as rf]
            [voidwalker.source.handlers]
            [voidwalker.source.subscriptions]
            [reagent.core :as reagent :refer [atom]]))

(rf/dispatch [:voidwalker/init])

(println @(rf/subscribe [:page]))


(deftest test-home
  (is (= true true))
  (is (nil? (rf/dispatch [::trigger {:snow.comm.core/type :snow.db/add
                                     :data 90}]))))

