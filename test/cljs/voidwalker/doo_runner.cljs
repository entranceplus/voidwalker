(ns voidwalker.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [voidwalker.core-test]))

(doo-tests 'voidwalker.core-test)

