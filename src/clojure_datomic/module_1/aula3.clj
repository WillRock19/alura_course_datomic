(ns clojure-datomic.module-1.aula3
  (:use clojure.pprint)
  (:require [clojure-datomic.module-1.db :as db]
            [clojure-datomic.module-1.model :as model]
            [datomic.api :as d]))

(db/apaga-banco)

(def conn (db/abre-conexao))

(db/cria-schema conn)

(let [computador      (model/novo-produto "Computador Novo", "/computador-novo", 2500.10M)
      celular         (model/novo-produto "Celular brilhante", "/celular-brilhante", 5000.98M)
      calculadora     {:produto/nome "Calculadora"}
      celular-barato  (model/novo-produto "Celular barato", "/celular-barato", 10.19M)]
  (println "Adicionando entidades e seus atributos na base...")
  (println "")
  (pprint @(d/transact conn [computador, celular, calculadora, celular-barato]))
  (println "")
  (println "==================================================================================")
  (println "Imprimindo Id das entidades inseridas...")
  (pprint (db/todos-os-ids-produtos (d/db conn))))

(println "")
(println "==========================")
(println "Buscando todos os ids dos produtos com slug brilhante...")
(pprint (db/todos-os-ids-produtos-por-slug-brilhante (d/db conn)))

(println "")
(println "==========================")
(println "Buscando todos os ids dos produtos pelo slug...")
(pprint (db/todos-os-ids-produtos-por-slug (d/db conn) "/computador-novo"))


(println "")
(println "==========================")
(println "Buscando todos os slugs na base com seus ids...")
(pprint (db/todos-slugs-com-ids (d/db conn)))

(println "")
(println "==========================")
(println "Buscando todos os slugs na base...")
(pprint (db/todos-slugs (d/db conn)))

(println "")
(println "==========================")
(println "Buscando todos os nomes e precos dos produtos...")
(pprint (db/todos-nomes-e-precos-de-produtos (d/db conn)))