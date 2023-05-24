(ns prod
  (:gen-class)
  (:require
    app.todo-list
    clojure.string
    ;electric-server-java11-jetty10
    electric-server-java8-jetty9))

(def electric-server-config
  {:host "0.0.0.0", :port 8080, :resources-path "public"})

(defn -main [& args]                                        ; run with `clj -M -m prod`
  (when (clojure.string/blank? (System/getProperty "HYPERFIDDLE_ELECTRIC_SERVER_VERSION"))
    (throw (ex-info "HYPERFIDDLE_ELECTRIC_SERVER_VERSION jvm property must be set in prod" {})))
  (electric-server-java8-jetty9/start-server! electric-server-config))

; On CLJS side we reuse src/user.cljs for prod entrypoint