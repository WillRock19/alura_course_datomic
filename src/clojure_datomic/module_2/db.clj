(ns clojure-datomic.module-2.db
  (:require [datomic.api :as d]))

(def db-uri "datomic:dev://localhost:4334/ecommerce")

(defn abre-conexao []
  (d/create-database db-uri)
  (d/connect db-uri))

(defn apaga-banco []
  (d/delete-database db-uri))

(def schema [{:db/ident       :produto/nome
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one
              :db/doc         "O nome de um produto"},
             {:db/ident       :produto/slug
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one
              :db/doc         "O caminho para acessar o produto via http"},
             {:db/ident       :produto/preco
              :db/valueType   :db.type/bigdec
              :db/cardinality :db.cardinality/one
              :db/doc         "O preco de um produto com precisão monetária"},
             {:db/ident       :produto/palavra-chave
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/many},
             {:db/ident       :produto/id
              :db/valueType   :db.type/uuid
              :db/cardinality :db.cardinality/one
              :db/unique      :db.unique/identity }])

(defn cria-schema [conn]
  (d/transact conn schema))

;Uma das formas de buscar por um produto específico é utilizando o comando d/pull. Aqui, passamos o
;banco de dados, informamos que queremos TODAS as propriedades da entidade e então passamos o produto-id
;para ser buscado.

(defn produto-por-datomic-id [snapshot-db, datomic-id]
  (d/pull snapshot-db '[*] datomic-id))

(defn todos-os-produtos-por-id-do-datomic [snapshot-db, ids]
  (letfn [(obter-produto-por-id [datomic-id]
            (d/pull snapshot-db '[*] datomic-id))]
    (mapv obter-produto-por-id ids)))

(defn produto-por-produto-id [snapshot-db, produto-id]
  (d/pull snapshot-db '[*] [:produto/id produto-id]))
