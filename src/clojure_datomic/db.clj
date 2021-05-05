(ns clojure-datomic.db
  (:require [datomic.api :as d]))

(def db-uri "datomic:dev://localhost:4334/hello")

(defn abre-conexao []
  (d/create-database db-uri)
  (d/connect db-uri))

(defn apaga-banco []
  (d/delete-database db-uri))

;Estrutura dos registros que usaremos:

;Entidade: Produto
;Propriedades      Tipo     Quantidade      Valor                           ID_TRANSACAO                                                 OPERACAO
    ;nome          String        1       ==> Computador novo        Valor adicionado pelo Datomic     Valor que informa se inseriu (true) ou removeu (false) o valor do banco
    ;slug          String        1       ==> /computador_novo       Valor adicionado pelo Datomic     Valor que informa se inseriu (true) ou removeu (false) o valor do banco
    ;preco      Ponto Flutuan.   1       ==> 3500.10                Valor adicionado pelo Datomic     Valor que informa se inseriu (true) ou removeu (false) o valor do banco

;O Datomic cria essa estrutura por linha. Cada propriedade sera uma linha diferente dentro da tabela (chamada de DATOM),
;identificadas pelo id da entidade. Exemplo:

; id_entidade    nome_atributo          valor
;     15            nome          Computador Novo
;     15            slug          /computador_novo
;     15            preco             3500.10

;Para isso, primeiro precisamos definir o schema do banco, a estrutura que nossos registros terao

(def schema [{:db/ident         :produto/nome
              :db/valueType     :db.type/string
              :db/cardinality   :db.cardinality/one         ;quantidade dessa propriedade que existirá para cada entidade)
              :db/doc           "O nome de um produto"},
             {:db/ident         :produto/slug
              :db/valueType     :db.type/string
              :db/cardinality   :db.cardinality/one
              :db/doc           "O caminho para acessar o produto via http"},
             {:db/ident         :produto/preco
              :db/valueType     :db.type/bigdec
              :db/cardinality   :db.cardinality/one
              :db/doc           "O preco de um produto com precisão monetária"}])

;Uma vez criado o schema, precisamos transacionar ele no banco para garantir que ele entenderá o tipo de estrutura que
;usaremos.

(defn cria-schema [conn]
  (d/transact conn schema))

;Uma vez que a transacao impressa, teremos algo como:

;:tx-data [#datom[13194139534312 50 #inst"2021-05-05T12:07:03.484-00:00" 13194139534312 true]
;          #datom[72 10 :produto/nome 13194139534312 true]
;          #datom[72 40 23 13194139534312 true]
;          #datom[72 41 35 13194139534312 true]

;Acima, mostro uma transacao de quando rodei o schema na transaction. As propriedades que temos na segunda linha são:
;  -> 72             = id_entidade
;  -> 10             = nome do atributo (no caso, db/ident)
;  -> :produto/nome  = valor do atributo
;  -> 13194139534312 = identificador da transacao
;  -> true           = operacao que foi realizada é de insercao

