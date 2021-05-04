(ns clojure-datomic.core
  (:use
    clojure.pprint)
  (:require
    [datomic.api :as d]))

(def db-uri "datomic:dev://localhost:4334/hello")

(pprint (d/create-database db-uri))
(def conn (d/connect db-uri))

(println "Teste")
