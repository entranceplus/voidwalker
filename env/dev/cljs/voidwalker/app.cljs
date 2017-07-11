(ns ^:figwheel-no-load voidwalker.app
  (:require [voidwalker.core :as core]
            [devtools.core :as devtools]))

(enable-console-print!)

(devtools/install!)

(core/init!)
