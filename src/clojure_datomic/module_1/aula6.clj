(ns clojure-datomic.module-1.aula6
  (:use clojure.pprint)
  (:require [clojure-datomic.module-1.db :as db]
            [clojure-datomic.module-1.model :as model]
            [datomic.api :as d]
            [clojure.string :as str]))

(db/apaga-banco)

(def conn (db/abre-conexao))

(db/cria-schema conn)

(let [computador            (model/novo-produto "Computador Novo", "/computador-novo", 2500.10M)
      celular               (model/novo-produto "Celular brilhante", "/celular-brilhante", 5000.98M)
      resultado-transacao   @(d/transact conn [computador, celular])]
  (println "")
  (println "==========================")
  (println "Adicionando entidades e seus atributos primeira fase...")
  (println "")
  (pprint resultado-transacao))

(println "Data insercao: " data-primeira-transacao)


(def estado-do-banco-com-dois-registros (d/db conn))

(let [calculadora         {:produto/nome "Calculadora"}
      celular-barato      (model/novo-produto "Celular barato", "/celular-barato", 10.19M)
      resultado-transacao @(d/transact conn [calculadora, celular-barato])]
  (println "")
  (println "==========================")
  (println "Adicionando entidades e seus atributos segunda fase...")
  (println "")
  (pprint resultado-transacao))


