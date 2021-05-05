(ns clojure-datomic.core
  (:use clojure.pprint)
  (:require [clojure-datomic.db :as db]
            [clojure-datomic.model :as model]
            [datomic.api :as d]))

(def conn (db/abre-conexao))

;Transacionando o schema no banco para ensinar a ele o tipo de estrutura que queremos que ele tenha
(db/cria-schema conn)

;Inserindo primeiro registro no banco
(pprint
  (let [computador (model/novo-produto "Computador Novo", "/computador_novo", 2500.10M)] ;M é para tornar o floar um BigDecimal
    (d/transact conn [computador])))