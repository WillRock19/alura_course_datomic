(ns clojure-datomic.module-2.db
    (:require [datomic.api :as d]))

(def db-uri "datomic:dev://localhost:4334/ecommerce")

(defn- comandos-db-adds-que-atribui-categoria-ao-produto [categoria, produtos]
       (reduce (fn [db-adds produto] (conj db-adds [:db/add
                                                    [:produto/id (:produto/id produto)] ;obtém o id do produto ao qual adicionaremos a propriedade
                                                    :produto/categoria ;nome da propriedade que vamos adicionar
                                                    [:categoria/id (:categoria/id categoria)]])) ;obtem o id da categoria que será adicionada como valor da propriedade
               []
               produtos))

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
              :db/unique      :db.unique/identity
              :db/doc         "O identificador único de um produto"},
             {:db/ident       :produto/categoria
              :db/valueType   :db.type/ref
              :db/cardinality :db.cardinality/one
              :db/doc         "A categoria do produto"}

             {:db/ident       :categoria/nome
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one
              :db/doc         "O nome de uma categoria"},
             {:db/ident       :categoria/id
              :db/valueType   :db.type/uuid
              :db/cardinality :db.cardinality/one
              :db/unique      :db.unique/identity
              :db/doc         "O identificador único de uma categoria"}])

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

(defn todos-os-ids-produtos-com-nome [snapshot-db]
      (d/q '[:find ?entidade
             :where [?entidade :produto/nome]], snapshot-db))

(defn todas-as-categorias [snapshot-db]
      (d/q '[:find (pull ?categoria [*])
             :where [?categoria :categoria/id]], snapshot-db))

(defn atribui-categorias [connection produtos categoria]
      (let [a-transacionar (comandos-db-adds-que-atribui-categoria-ao-produto categoria produtos)]
           (d/transact connection a-transacionar)))

(defn adiciona-produtos! [connection, produtos]
      (d/transact connection produtos))

(defn adiciona-categorias! [connection, categorias]
      (d/transact connection categorias))