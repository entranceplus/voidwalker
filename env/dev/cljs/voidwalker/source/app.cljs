(ns ^:figwheel-no-load voidwalker.source.app
  (:require [voidwalker.source.core :as core]
            [devtools.core :as devtools]))

(enable-console-print!)

(devtools/install!)

(core/init!)
