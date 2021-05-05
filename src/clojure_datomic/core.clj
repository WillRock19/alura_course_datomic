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

;Fazendo uma query que retorna o id das entidades que possuam :produto/nome como atributos (segunda coluna de uma linha)
;(d/q '[:find ?entidade_id
;       :where [?entidade_id :produto/nome]], conn)

;O codigo acima não vai funcionar, porque estamos passando uma conexão. Conexões só servem para inserir dados. Para buscar,
;precisamos passar uma cópia do banco para que o Datomic possa fazer sua pesquisa. Para isso, podemos usar o d/db, que
;retorna uma cópia (snapshot) do banco naquele determinado instante do tempo.

(d/q '[:find ?entidade_id
       :where [?entidade_id :produto/nome]], (d/db conn))

