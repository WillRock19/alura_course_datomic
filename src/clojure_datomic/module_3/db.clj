(ns clojure-datomic.module-3.db
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
              :db/doc         "O identificador único de uma categoria"}

             {:db/ident       :tx-data/ip
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one
              :db/doc         "O endereco IP que realizou uma determinada transacao"}])

(defn cria-schema [conn]
      (d/transact conn schema))

(defn produto-por-datomic-id [snapshot-db, datomic-id]
      (d/pull snapshot-db '[*] datomic-id))

(defn produto-por-produto-id [snapshot-db, produto-id]
      (d/pull snapshot-db '[*] [:produto/id produto-id]))

(defn todos-os-produtos-por-id-do-datomic [snapshot-db, ids]
  (letfn [(obter-produto-por-id [datomic-id]
            (d/pull snapshot-db '[*] datomic-id))]
    (mapv obter-produto-por-id ids)))

(defn adiciona-produtos!
  ([connection, produtos]
   (d/transact connection produtos))
  ([connection, produtos, endereco-ip]
   (let [db-add-ip [:db/add "datomic.tx" :tx-data/ip endereco-ip]]        ;Aqui estamos criando um db/add associado à transacao que será efetuada (comando datomix.tx), no attributo :tx-data/ip com o valor endereco-ip
     (d/transact connection (conj produtos db-add-ip)))))

(defn adiciona-categorias! [connection, categorias]
      (d/transact connection categorias))

(defn atribui-categorias [connection produtos categoria]
  (let [a-transacionar (comandos-db-adds-que-atribui-categoria-ao-produto categoria produtos)]
    (d/transact connection a-transacionar)))

(defn todos-nomes-produtos-com-suas-categorias [snapshot-db]
      (d/q '[:find ?nome, ?nome-da-categoria
             :keys produto, categoria
             :where [?produto :produto/nome ?nome]
                    [?produto :produto/categoria ?categoria]
                    [?categoria :categoria/nome ?nome-da-categoria]],
           snapshot-db))

(defn todos-nomes-produtos-sem-categorias [snapshot-db]
      (d/q '[:find ?nome
             :keys produto
             :where [?produto :produto/nome ?nome]
                    [(missing? $ ?produto :produto/categoria)]],
           snapshot-db))

(defn todos-nomes-produtos-da-categoria [snapshot-db, nome-categoria]
      (d/q '[:find (pull ?produto [:produto/nome :produto/slug { :produto/categoria [*] }])
             :in $, ?nome
             :where [?categoria :categoria/nome ?nome]
                    [?produto :produto/categoria ?categoria]],
           snapshot-db, nome-categoria))

(defn todos-nomes-produtos-da-categoria-backwards [snapshot-db, nome-categoria]
  (d/q '[:find (pull ?categoria [:categoria/nome { :produto/_categoria [:produto/nome :produto/slug] }])
         :in $ ?nome
         :where [?categoria :categoria/nome ?nome]]
       snapshot-db, nome-categoria))

(defn maior-e-menor-precos-junto-da-quantidade-de-precos [snapshot-db]
  (d/q '[:find  (min ?preco) (max ?preco) (count ?preco)
         :keys  preco-minimo, preco-maximo, total-de-precos
         :with ?produto                                        ;Essa linha faz isso
         :where [?produto :produto/preco ?preco]]
       snapshot-db))

(defn maior-e-menor-precos-junto-da-quantidade-de-precos-por-categoria [snapshot-db]
  (d/q '[:find  ?nome-categoria (min ?preco) (max ?preco) (count ?preco)
         :keys  categoria, preco-minimo, preco-maximo, total-de-precos
         :with ?produto
         :where [?produto :produto/preco ?preco]
                [?produto :produto/categoria ?categoria]
                [?categoria :categoria/nome ?nome-categoria]]
       snapshot-db))

(defn produtos-mais-caros [snapshot-db]
  (d/q '[:find  (pull ?produto [*])
         :where [(q '[:find (max ?preco)
                      :where [_ :produto/preco ?preco]]
                    ,$) [[?preco]]]
                [?produto :produto/preco ?preco]]
       , snapshot-db))

(defn produtos-mais-baratos [snapshot-db]
  (d/q '[:find  (pull ?produto [*])
         :where [(q '[:find (min ?preco)
                      :where [_ :produto/preco ?preco]]
                    ,$) [[?preco]]]
         [?produto :produto/preco ?preco]]
       , snapshot-db))

(defn todos-produtos-do-ip [snapshot-db, endereco-ip]
  (d/q '[:find  (pull ?produto [*])
         :in $, ?ip-buscado
         :where [?transacao :tx-data/ip ?ip-buscado]
                [?produto :produto/id _ ?transacao]]
       snapshot-db, endereco-ip))