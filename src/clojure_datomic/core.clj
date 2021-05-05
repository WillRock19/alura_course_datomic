(ns clojure-datomic.core
  (:use clojure.pprint)
  (:require [clojure-datomic.db :as db]
            [clojure-datomic.model :as model]
            [datomic.api :as d]))