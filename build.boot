(def project 'voidwalker)
(def version "0.1.0-SNAPSHOT")

(set-env! :resource-paths #{"resources" "src/clj" "src/cljs"}
          :checkouts '[[snow "0.1.0-SNAPSHOT"]]
          :dependencies   '[[org.clojure/clojure "1.9.0"]
                            [org.clojure/core.async "0.4.474"]
                            [org.clojure/clojurescript "1.10.238"]
                            [org.immutant/immutant "2.1.9"]
                            [org.danielsz/system "0.4.2-SNAPSHOT"]
                            [org.clojure/java.jdbc "0.7.3"]
                            [org.clojure/tools.cli "0.3.5"]
                            [org.clojure/tools.logging "0.4.0"]
                            [metosin/ring-http-response "0.9.0"]
                            [compojure "1.6.0"]
                            [environ "1.1.0"]
                            [boot-environ "1.1.0"]
                            [ring "1.6.3"]
                            [org.clojure/tools.nrepl "0.2.12"]
                            [ring/ring-defaults "0.3.1"]
                            [ring-middleware-format "0.7.2"]
                            [adzerk/boot-reload "0.5.2" :scope "test"]
                            [adzerk/boot-test "1.2.0" :scope "test"]
                            [reagent "0.8.0-alpha2"]
                            [reagi "0.10.1"]
                            [io.replikativ/konserve "0.5-beta1"]
                            [proto-repl "0.3.1"]
                            [expound "0.5.0"]
                            [nightlight "RELEASE" :scope "test"]
                            [funcool/bide "1.6.0"]
                            [cljs-ajax "0.6.0"]
                            [thheller/shadow-cljs "2.2.21"]
                            [day8.re-frame/http-fx "0.1.3"]
                            [re-frame "0.10.5"]
                            [adzerk/boot-reload "0.5.2" :scope "test"]
                            [adzerk/boot-test "1.2.0" :scope "test"]
                            [adzerk/boot-cljs "2.1.4" :scope "test"]
                            [adzerk/boot-cljs-repl "0.3.3" :scope "test"]
                            [adzerk/boot-test "1.2.0" :scope "test"]
                            [adzerk/boot-reload "0.5.2" :scope "test"]
                            [com.cemerick/piggieback "0.2.1" :scope "test"]
                            [binaryage/devtools "0.9.4" :scope "test"]
                            [snow "0.1.0-SNAPSHOT"]
                            [adzerk/bootlaces "0.1.13"]
                            [amazonica "0.3.121"]
                            [figwheel-sidecar "0.5.7" :scope "test"]
                            [ajchemist/boot-figwheel "0.5.4-6"]
                            [weasel "0.7.0" :scope "test"]])

(require '[system.boot :refer [system run]]
         '[voidwalker.systems :refer [dev-system]]
         '[clojure.edn :as edn]
         '[environ.core :refer [env]]
         '[environ.boot :refer [environ]]
         '[snow.boot :refer [profile migrate rollback]])

(require '[adzerk.boot-cljs :refer :all]
         '[adzerk.boot-cljs-repl :refer :all]
         '[adzerk.boot-reload :refer :all])

(require '[nightlight.boot :refer [nightlight]])

(require 'boot-figwheel)
(refer 'boot-figwheel :rename '{cljs-repl fw-cljs-repl})

(require '[adzerk.bootlaces :refer :all])
(bootlaces! version :dont-modify-paths? true)

(task-options!
 pom {:project     project
      :version     version
      :description "Voidwalker"
      :url         "http://content.entranceplus.in"
      :scm         {:url "https://github.com/entranceplus/voidwalker"}
      :license     {"Eclipse Public License"
                    "http://www.eclipse.org/legal/epl-v10.html"}}
 figwheel
 {:build-ids  ["dev"]
  :all-builds [{:id "dev"
                :source-paths ["src"]   ; cljs(cljc) directories
                :compiler {:main 'voidwalker.source.app
                           :output-to "resources/public/js/app.js"}
                :figwheel {:build-id  "dev"
                           :on-jsload 'voidwalker.source.app/main!
                           :heads-up-display true
                           :autoload true
                           :debug false}}]
  :figwheel-options {:open-file-command "emacsclient"
                     :repl true}})

(deftask dev
  "run a restartable system"
  []
  (comp
   (environ :env (snow.boot/profile))
   (watch :verbose true)
   (system :sys #'dev-system
           :auto true
           :files ["routes.clj" "systems.clj" "content.clj"])
   (reload :asset-path "public")
   ; (figwheel)
   (repl :server true
         :port 8001
         :bind "0.0.0.0")
   (nightlight :port 5000)
   ; (cljs-repl)
   ; (cljs :source-map true
   ;       :optimizations :none)
   (notify)))

(deftask build
  "Build the project locally as a JAR."
  []
  (comp (cljs :source-map true
              :optimizations :none)
     (aot :namespace #{'voidwalker.core})
     (uber)
     (jar :main 'voidwalker.core
          :file "voidwalker.jar")
     (sift :include #{#".*\.jar"})
     (target)
     (notify)))
;
; (cljs :source-map true
;               :optimizations :none)

(deftask deps [])

(deftask publish []
  (comp
   (build-jar)
   (push-snapshot)))

(deftask install-local
  "Install jar locally"
  []
  (comp
     (pom)
     (jar)
     (install)))

(deftask run-project
  "Run the project."
  [a args ARG [str] "the arguments for the application."]
  (require '[kongauth.core :as app])
  (apply (resolve 'app/-main) args))

(require '[adzerk.boot-test :refer [test]])
