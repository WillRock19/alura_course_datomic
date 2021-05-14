(ns clojure-datomic.module-3.db
  (:require [clojure-datomic.module-3.model :as model]
            [clojure.walk :as walk]
            [datomic.api :as d]
            [schema.core :as s]))

(def db-uri "datomic:dev://localhost:4334/ecommerce")

(defn- comandos-db-adds-que-atribui-categoria-ao-produto [categoria, produtos]
       (reduce (fn [db-adds produto] (conj db-adds [:db/add
                                                    [:produto/id (:produto/id produto)] ;obtém o id do produto ao qual adicionaremos a propriedade
                                                    :produto/categoria ;nome da propriedade que vamos adicionar
                                                    [:categoria/id (:categoria/id categoria)]])) ;obtem o id da categoria que será adicionada como valor da propriedade
               []
               produtos))

(defn- dissoc-datomic-id [entidade]
  (if (map? entidade)
    (dissoc entidade :db/id)
    entidade))

(defn- registros-datomic->entidades [registros-datomic]
  (walk/prewalk dissoc-datomic-id registros-datomic)) ;Prewalk vai navegar por cada nó do meu mapa (entidade) e aplica a funcao que eu passei para ele

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

(s/defn produto-por-id :- model/Produto
  [snapshot-db, produto-id]
  (registros-datomic->entidades (d/pull snapshot-db '[*] [:produto/id produto-id])))

(s/defn adiciona-ou-atualiza-produtos!
  ([connection, produtos :- [model/Produto]]
   (d/transact connection produtos))
  ([connection, produtos :- [model/Produto], endereco-ip :- s/Str]
   (let [db-add-ip [:db/add "datomic.tx" :tx-data/ip endereco-ip]]        ;Aqui estamos criando um db/add associado à transacao que será efetuada (comando datomix.tx), no attributo :tx-data/ip com o valor endereco-ip
     (d/transact connection (conj produtos db-add-ip)))))

(s/defn adiciona-ou-atualiza-categorias! [connection, categorias :- [model/Categoria]]
      (d/transact connection categorias))

(defn atribui-categorias [connection produtos categoria]
  (let [a-transacionar (comandos-db-adds-que-atribui-categoria-ao-produto categoria produtos)]
    (d/transact connection a-transacionar)))

(defn todos-produtos-do-ip [snapshot-db, endereco-ip]
  (d/q '[:find  (pull ?produto [*])
         :in $, ?ip-buscado
         :where [?transacao :tx-data/ip ?ip-buscado]
                [?produto :produto/id _ ?transacao]]
       snapshot-db, endereco-ip))

;Abaixo, vamos tentar definir o tipo de retorno da funcao. Ele dá erro.
;(s/defn todas-categorias :- [model/Categoria]
;  [snapshot-db]
;  (d/q '[:find  (pull ?categoria [*])
;         :where [?categoria :categoria/id _]],
;       snapshot-db))

;Quando estamos buscando no datomic, ele constrói o retorno de uma forma bem específica.
;Veja abaixo:
;
; *:find nome, preco* é retornado como
;
;[
; [nome1, preco1]
; [nome2, preco2]
;]
;
;Em outras palavras: eu não retorno uma lista de mapas, mas uma lista de listas com os dados desejados.
;Se quisermos fazer ele retornar os dados fora dessas sequencias mais internas, precisamos escrever assim:

;(s/defn todas-categorias :- [model/Categoria]
;  [snapshot-db]
;  (d/q '[:find  [(pull ?categoria [*]) ...]  ;Por padrão, quando colocamos a query do find dentro do colchetes ele vai retornar APENAS UM dos registros (qual? Sei la; pode ser primeiro, quinto, vigésimo, tanto faz). Para deixar claro que queremos TODOS, precisamos colocar as reticencias.
;         :where [?categoria :categoria/id _]],
;       snapshot-db))

;O problema é que o código acima AINDA quebra, mas por outro motivo: ele retorna um registro com uma propriedade
;:db/id, e nosso schema não espera isso. Então, para lidar com isso, iremos criar uma funcao auxiliar que vai
;mapear os registros do datomic e remover a propridade :db/id

(s/defn todas-categorias :- [model/Categoria]
  [snapshot-db]
  (registros-datomic->entidades (d/q '[:find [(pull ?categoria [*]) ...] ;Por padrão, quando colocamos a query do find dentro do colchetes ele vai retornar APENAS UM dos registros (qual? Sei la; pode ser primeiro, quinto, vigésimo, tanto faz). Para deixar claro que queremos TODOS, precisamos colocar as reticencias.
                                     :where [?categoria :categoria/id _]],
                                     snapshot-db)))

;Podemos também buscar o produto, mas dará erro. Por quê? Por que ele trará também os dados da categoria, mas  único dado que
;ele tentará trazer é o id... e nós sabemos que o produto possui uma categoria completa.

;(s/defn todos-produtos :- [model/Produto]
;  [snapshot-db]
;  (registros-datomic->entidades (d/q '[:find [(pull ?produto [*]) ...]
;                                       :where [?produto :produto/id _]],
;                                     snapshot-db)))
;
;Para resolver isso, precisamos deixar claro que queremos buscar os dados da categoria também. Veja:

(s/defn todos-produtos :- [model/Produto]
  [snapshot-db]
  (registros-datomic->entidades (d/q '[:find [(pull ?produto [* {:produto/categoria [*]}]) ...]
                                       :where [?produto :produto/id _]],
                                     snapshot-db)))
