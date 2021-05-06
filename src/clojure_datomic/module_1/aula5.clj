(ns clojure-datomic.module-1.aula5
  (:use clojure.pprint)
  (:require [clojure-datomic.module-1.db :as db]
            [clojure-datomic.module-1.model :as model]
            [datomic.api :as d]
            [clojure.string :as str]))

(db/apaga-banco)

(def conn (db/abre-conexao))

(db/cria-schema conn)

;Com Datomic, podemos obter snapshots do banco de dados em momentos especificos do tempo, o que favorece algumas
;situacoes. Podemos:
;
; 1. Usar isso para simular bugs especificos;
; 2. Usar isso para trabalhar com auditorias;
;
;Veja abaixo:

(let [computador            (model/novo-produto "Computador Novo", "/computador-novo", 2500.10M)
      celular               (model/novo-produto "Celular brilhante", "/celular-brilhante", 5000.98M)
      resultado-transacao   @(d/transact conn [computador, celular])]
  (println "")
  (println "==========================")
  (println "Adicionando entidades e seus atributos primeira fase...")
  (println "")
  (pprint resultado-transacao)
  (def data-primeira-transacao (-> resultado-transacao :tx-data first (nth 2))))

(println "Data insercao: " data-primeira-transacao)


(def estado-do-banco-com-dois-registros (d/db conn))

(let [calculadora         {:produto/nome "Calculadora"}
      celular-barato      (model/novo-produto "Celular barato", "/celular-barato", 10.19M)
      resultado-transacao @(d/transact conn [calculadora, celular-barato])]
  (println "")
  (println "==========================")
  (println "Adicionando entidades e seus atributos segunda fase...")
  (println "")
  (pprint resultado-transacao)
  (def data-segunda-transacao (-> resultado-transacao :tx-data first (nth 2))))

;Imprimindo um snapshot dos registros dentro do banco no momento
(println "")
(println "==========================")
(println "Buscando dados no banco no momento atual")
(pprint (db/todos-produtos-com-nome (d/db conn)))

;imprimindo um snapshot dos registros dentro do banco em um instante do passado
(println "")
(println "==========================")
(println "Registros no banco após primeira transacao")
(pprint (db/todos-produtos-com-nome estado-do-banco-com-dois-registros))
(println "")
(println "==========================")
(println "Registros no banco após segunda transacao")
(pprint (db/todos-produtos-com-nome (d/db conn)))

;Outra forma de obter as transacoes é usando o comando (db-as) e passando o instante onde queremos
;os dados
(println "")
(println "==========================")
(println "Instante primeira transacao:" data-primeira-transacao)
(println "Instante segunda transacao:" data-segunda-transacao)

(println "")
(println "==========================")
(println "Registros no banco no instante da primeira transacao...")
(pprint (db/todos-produtos-com-nome (d/as-of (d/db conn) data-primeira-transacao)))
(println "")
(println "==========================")
(println "Registros no banco no instante da segunda transacao...")
(pprint (db/todos-produtos-com-nome (d/as-of (d/db conn) data-segunda-transacao)))
