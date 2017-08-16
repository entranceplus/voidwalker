(defproject voidwalker "0.1.0-SNAPSHOT"

  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  :dependencies [[clj-time "0.13.0"]
                 [cljs-ajax "0.6.0"]
                 [compojure "1.6.0"]
                 [cheshire "5.7.1"]
                 [cprop "0.1.10"]
                 [funcool/struct "1.0.0"]
                 [luminus-immutant "0.2.3"]
                 [luminus-nrepl "0.1.4"]
                 [luminus/ring-ttl-session "0.3.2"]
                 [markdown-clj "0.9.99"]
                 [metosin/muuntaja "0.2.1"]
                 [metosin/ring-http-response "0.9.0"]
                 [venantius/accountant "0.2.0"]
                 [korma "0.4.3"]
                 [mysql/mysql-connector-java "6.0.5"]
                 [cljsjs/slate "0.20.3-0"]
                 [mount "0.1.11"]
                 [migratus "0.9.7"]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/core.match "0.3.0-alpha5"]
                 [cljsjs/quill "1.2.5-4"]
                 [day8.re-frame/http-fx "0.1.3"]
                 [org.clojure/core.async "0.3.443"]
                 [org.clojure/clojurescript "1.9.562" :scope "provided"]
                 [org.clojure/tools.cli "0.3.5"]
                 [org.clojure/tools.logging "0.4.0"]
                 [org.webjars.bower/tether "1.4.0"]
                 [org.webjars/bootstrap "4.0.0-alpha.5"]
                 [org.webjars/font-awesome "4.7.0"]
                 [re-frame "0.9.4"]
                 [reagent "0.6.2" :exclusions [cljsjs/react cljsjs/react-dom]]
                 [reagent-utils "0.2.1"]
                 [ring-webjars "0.2.0"]
                 [ring/ring-core "1.6.1"]
                 [ring/ring-defaults "0.3.0"]
                 [secretary "1.2.3"]
                 [selmer "1.10.7"]]

  :min-lein-version "2.0.0"

  :jvm-opts ["-server"]
  :source-paths ["src/clj" "src/cljc"]
  :test-paths ["test/clj"]
  :resource-paths ["resources" "target/cljsbuild"]
  :target-path "target/%s/"
  :main ^:skip-aot voidwalker.core

  :migratus {:store :database
             :migration-dir "migrations"
             :db {:classname "com.mysql.jdbc.Driver"
                  :subprotocol "mysql"
                  :subname "//localhost/voidwalker"
                  :user "root"
                  :password ""}}

  :plugins [[lein-cljsbuild "1.1.6"]
            [lein-immutant "2.1.0"]
            [lein-re-frisk "0.4.8"]]
  :clean-targets ^{:protect false}
  [:target-path
   [:cljsbuild :builds :app :compiler :output-dir]
   [:cljsbuild :builds :app :compiler :output-to]]
  :figwheel
  {:http-server-root "public"
   :nrepl-port 7002
   :css-dirs ["resources/public/css"]
   :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
  :profiles
  {:uberjar {:omit-source true
             :prep-tasks ["compile" ["cljsbuild" "once" "source" "client"]]
             :cljsbuild
             {:builds
                   {:source
                    {:source-paths ["src/cljs"
                                    "src/cljc"
                                    "env/prod/cljs"]
                     :figwheel {:on-jsload "voidwalker.source.core/mount-components"}
                     :compiler
                     {:main "voidwalker.source.app"
                      :asset-path "/js/out/source"
                      :output-to "target/cljsbuild/public/js/source.js"
                      :output-dir "target/cljsbuild/public/js/out/source"
                      :foreign-libs [{:file "public/js/bundle.js"
                                      :provides ["cljsjs.react"
                                                 "cljsjs.react.dom"
                                                 "webpack.bundle"]}]
                      :optimizations :whitespace
                      :pretty-print false}}
                    :client
                    {:source-paths ["src/cljs/voidwalker/client"
                                    "src/cljc"
                                    "env/prod/cljs/voidwalker/client"]
                     :figwheel {:on-jsload "voidwalker.client.core/mount-components"}
                     :compiler
                     {:main "voidwalker.client.app"
                      :asset-path "/js/out/client"
                      :output-to "target/cljsbuild/public/js/client.js"
                      :output-dir "target/cljsbuild/public/js/out/client"
                      :source-map true
                      :foreign-libs [{:file "public/js/bundle.js"
                                      :provides ["cljsjs.react"
                                                 "cljsjs.react.dom"
                                                 "webpack.bundle"]}]
                      :optimizations :none
                      :pretty-print true}}}}
             :aot :all
             :uberjar-name "voidwalker.jar"
             :source-paths ["env/prod/clj"]
             :resource-paths ["env/prod/resources"]}

   :dev           [:project/dev :profiles/dev]
   :test          [:project/dev :project/test :profiles/test]

   :project/dev  {:dependencies [[prone "1.1.4"]
                                 [re-frisk-remote "0.4.2"]
                                 [ring/ring-mock "0.3.0"]
                                 [ring/ring-devel "1.6.1"]
                                 [pjstadig/humane-test-output "0.8.2"]
                                 [metosin/muuntaja "0.3.1"]
                                 [binaryage/devtools "0.9.4"]
                                 [com.cemerick/piggieback "0.2.2-SNAPSHOT"]
                                 [doo "0.1.7"]
                                 [figwheel-sidecar "0.5.10"]]
                  :plugins      [[com.jakemccrary/lein-test-refresh "0.19.0"]
                                 [lein-doo "0.1.7"]
                                 [migratus-lein "0.5.0"]
                                 [lein-figwheel "0.5.10"]
                                 [org.clojure/clojurescript "1.9.562"]]
                  :cljsbuild
                  {:builds
                   {:source
                    {:source-paths ["src/cljs/voidwalker/source"
                                    "src/cljc"
                                    "env/dev/cljs/voidwalker/source"]
                     :figwheel {:on-jsload "voidwalker.source.core/mount-components"}
                     :compiler
                     {:main "voidwalker.source.app"
                      :asset-path "/js/out/source"
                      :output-to "target/cljsbuild/public/js/source.js"
                      :output-dir "target/cljsbuild/public/js/out/source"
                      :source-map true
                      :source-map-path "js/out"
                      :foreign-libs [{:file "public/js/bundle.js"
                                      :provides ["cljsjs.react"
                                                 "cljsjs.react.dom"
                                                 "webpack.bundle"]}]
                      :optimizations :none
                      :pretty-print true}}
                    :client
                    {:source-paths ["src/cljs/voidwalker/client"
                                    "src/cljc"
                                    "env/dev/cljs/voidwalker/client"]
                     :figwheel {:on-jsload "voidwalker.client.core/mount-components"}
                     :compiler
                     {:main "voidwalker.client.app"
                      :asset-path "/js/out/client"
                      :output-to "target/cljsbuild/public/js/client.js"
                      :output-dir "target/cljsbuild/public/js/out/client"
                      :source-map true
                      :optimizations :none
                      :pretty-print true}}}}
                  :doo {:build "test"}
                  :source-paths ["env/dev/clj"]
                  :resource-paths ["env/dev/resources"]
                  :repl-options {:init-ns user}
                  :injections [(require 'pjstadig.humane-test-output)
                               (pjstadig.humane-test-output/activate!)]}
   :project/test {:resource-paths ["env/test/resources"]
                  :cljsbuild
                  {:builds
                   {:test
                    {:source-paths ["src/cljc" "src/cljs" "test/cljs"]
                     :compiler
                     {:output-to "target/test.js"
                      :main "voidwalker.doo-runner"
                      :optimizations :whitespace
                      :pretty-print true}}}}}
   :profiles/dev {}
   :profiles/test {}})
