(ns xtdb
  (:require [xtdb.api :as xt]))

(def !xtdb)

(defn start-xtdb! []                                        ; from XTDB’s getting started: xtdb-in-a-box
  (assert (= "true" (System/getenv "XTDB_ENABLE_BYTEUTILS_SHA1")))
  (letfn [(kv-store [dir] {:kv-store {:xtdb/module 'xtdb.rocksdb/->kv-store
                                      :db-dir      (clojure.java.io/file dir)
                                      :sync?       true}})]
    (xt/start-node
      {:xtdb/tx-log         (kv-store "data/dev/tx-log")
       :xtdb/document-store (kv-store "data/dev/doc-store")
       :xtdb/index-store    (kv-store "data/dev/index-store")})))

(comment
  ; to stop XT:
  (.close node))