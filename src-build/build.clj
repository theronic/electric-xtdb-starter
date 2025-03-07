(ns build
  "build electric.jar library artifact and demos"
  (:require [clojure.tools.build.api :as b]
            [org.corfield.build :as bb]
            [shadow.cljs.devtools.api :as shadow-api] ; so as not to shell out to NPM for shadow
            [shadow.cljs.devtools.server :as shadow-server]))


(def lib 'com.hyperfiddle/electric)
(def version (b/git-process {:git-args "describe --tags --long --always --dirty"}))
(def basis (b/create-basis {:project "deps.edn"}))

(defn clean [opts]
  (bb/clean opts))

(def class-dir "target/classes")
(defn default-jar-name [{:keys [version] :or {version version}}]
  (format "target/%s-%s-standalone.jar" (name lib) version))

(defn clean-cljs [_]
  (b/delete {:path "resources/public/js"}))

(defn build-client [{:keys [optimizations debug verbose version]
                     :or {optimizations :simple, debug false, verbose false, version version}}]
  (println "Building client. Version:" version)
  (shadow-server/start!)
  (if (= :none optimizations)
    (shadow-api/compile :prod {:debug        debug,
                               :verbose      verbose,
                               :config-merge [{:compiler-options {:optimizations optimizations}
                                               :closure-defines  {'hyperfiddle.electric-client/VERSION version}}]})
    (shadow-api/release :prod {:debug        debug,
                               :verbose      verbose,
                               :config-merge [{:compiler-options {:optimizations optimizations}
                                               :closure-defines  {'hyperfiddle.electric-client/VERSION version}}]}))
  (shadow-server/stop!))

(defn uberjar [{:keys [jar-name version optimizations debug verbose]
                :or   {version version, optimizations :simple, debug false, verbose false}}]
  (println "Cleaning up before build")
  (clean nil)

  (println "Cleaning cljs compiler output")
  (clean-cljs nil)

  ; temp. do no optimizations to fix prod.
  (build-client {:optimizations optimizations, :debug debug, :verbose verbose, :version version})

  (println "Bundling sources")
  (b/copy-dir {:src-dirs   ["src" "resources"]
               :target-dir class-dir})

  (println "Compiling server. Version:" version)
  (b/compile-clj {:basis      basis
                  :src-dirs   ["src"]
                  :ns-compile '[prod]
                  :class-dir  class-dir})

  (println "Building uberjar")
  (b/uber {:class-dir class-dir
           :uber-file (str (or jar-name (default-jar-name {:version version})))
           :basis     basis
           :main      'prod}))

(defn noop [_])                         ; run to preload mvn deps
