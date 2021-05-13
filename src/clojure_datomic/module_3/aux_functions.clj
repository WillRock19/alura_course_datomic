(ns clojure-datomic.module-3.aux-functions
  (:use clojure.pprint))

(defn imprimir-itens-multiline-println [items]
  (mapv #(println "->" %) items))

(defn imprimir-itens-multiline-pprint [items]
  (mapv (fn [item]
          (println "")
          (pprint item)) items))
