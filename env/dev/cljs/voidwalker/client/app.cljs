(ns ^:figwheel-no-load voidwalker.client.app
  (:require [voidwalker.client.core :as core]
            [devtools.core :as devtools]))

(enable-console-print!)

(devtools/install!)

(core/init!)
