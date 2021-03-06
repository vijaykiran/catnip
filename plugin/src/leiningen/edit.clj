(ns leiningen.edit
  (:require [leiningen.core.eval :as eval])
  (:import [java.util.concurrent CountDownLatch]
           [java.net ServerSocket]))

(defn- with-catnip-dep [project]
  (assoc project :dependencies
         (conj (:dependencies project)
               '[catnip "0.2.0-SNAPSHOT"])))

(defn- start-server-form [port]
  `(let [url# (str (catnip.server/start ~port))]
     (println "Catnip running on" url#)
     (clojure.java.browse/browse-url url#)))

(defn- server-in-project [project port]
  (eval/eval-in-project project
                        (start-server-form port)
                        `(require 'clojure.java.browse
                                  'catnip.server)))

(defn- free-port []
  (with-open [socket (ServerSocket. 0)]
    (.getLocalPort socket)))

(defn edit
  "Launch a Catnip server."
  [project & args]
  (server-in-project (with-catnip-dep project) (free-port))
  (.await (CountDownLatch. 1)))
