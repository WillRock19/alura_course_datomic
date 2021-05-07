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
  (pprint resultado-transacao)
  (def id-computador (-> resultado-transacao :tempids vals first))
  (def id-celular (-> resultado-transacao :tempids vals second)))

(let [calculadora         {:produto/nome "Calculadora"}
      celular-barato      (model/novo-produto "Celular barato", "/celular-barato", 10.19M)
      resultado-transacao @(d/transact conn [calculadora, celular-barato])]
  (println "")
  (println "==========================")
  (println "Adicionando entidades e seus atributos segunda fase...")
  (println "")
  (pprint resultado-transacao)
  (def id-celular-barato (-> resultado-transacao :tempids vals second)))

;Vamos adicionar tags para definir um produto? Para isso usamos um atributo com relacionamento many (vai criar um
;vetor para armazenar os dados). Adicionarei esses atributos para tres produtos: computador, celular-barato e celular

(println "")
(println "==========================")
(println "Id celular: " id-celular)
(println "Id computador:" id-computador)
(println "Id celular barato:" id-celular-barato)

(println "")
(println "==========================")
(println "Adicionando palavras-chaves...")

(println "")
(pprint @(d/transact conn [[:db/add id-celular :produto/palavra-chave "tela-de-diamante"]]))
(pprint @(d/transact conn [[:db/add id-celular :produto/palavra-chave "mobile-device"]]))
(pprint @(d/transact conn [[:db/add id-celular :produto/palavra-chave "MCelular"]]))

(println "")
(pprint @(d/transact conn [[:db/add id-computador :produto/palavra-chave "desktop"]]))

(println "")
(pprint @(d/transact conn [[:db/add id-celular-barato :produto/palavra-chave "tela-de-vidro"]]))
(pprint @(d/transact conn [[:db/add id-celular-barato :produto/palavra-chave "mobile-device"]]))
(pprint @(d/transact conn [[:db/add id-celular-barato :produto/palavra-chave "Motorola"]]))

(println "")
(println "==========================")

(defn buscar-e-imprimir-dados-produto-por-palavra-chave [palavra-chave]
  (let [produtos (db/todos-produtos-por-palavra-chave (d/db conn) palavra-chave)]
    (println "")
    (println "-> Palavra:" palavra-chave)
    (println "-> Quant. :" (count produtos))
    (println "")
    (pprint produtos)))

(buscar-e-imprimir-dados-produto-por-palavra-chave "desktop")
(buscar-e-imprimir-dados-produto-por-palavra-chave "mobile-device")
(buscar-e-imprimir-dados-produto-por-palavra-chave "Motorola")

