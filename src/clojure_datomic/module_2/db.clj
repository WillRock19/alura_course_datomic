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
              :db/doc         "O identificador único de uma categoria"}

              ;Schema para armazenar dados do IP que gerou uma transacao
             {:db/ident       :tx-data/ip                     ;Usamos o padrão tx-data quando queremos armazenar os dados de uma transacao
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one
              :db/doc         "O endereco IP que realizou uma determinada transacao"}])

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

(defn adiciona-produtos!
  ([connection, produtos]
   (d/transact connection produtos))
  ([connection, produtos, endereco-ip]
   (let [db-add-ip [:db/add "datomic.tx" :tx-data/ip endereco-ip]]        ;Aqui estamos criando um db/add associado à transacao que será efetuada (comando datomix.tx), no attributo :tx-data/ip com o valor endereco-ip
     (d/transact connection (conj produtos db-add-ip)))))

(defn adiciona-categorias! [connection, categorias]
      (d/transact connection categorias))

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

;Aqui, fazemos uma busca dos dados do produto e da categoria através de um foward navigation (buscamos o produto, e a
;partir dele buscamos os dados associados a ele - no caso, a categoria). Basicamente: varremos os dados uma vez para
;achar os produtos, e então varremos as categorias, varremos os produtos e então varremos mais uma vez as categorias
;para saber quais estão relacionadas.
(defn todos-nomes-produtos-da-categoria [snapshot-db, nome-categoria]
      (d/q '[:find (pull ?produto [:produto/nome :produto/slug { :produto/categoria [*] }])
             :in $, ?nome
             :where [?categoria :categoria/nome ?nome]
                    [?produto :produto/categoria ?categoria]],
           snapshot-db, nome-categoria))


;Aqui, buscamos os dados do produtos e da categoria através de um um backward navigation (buscamos uma categoria e a
;partir dela verificamos os produtos associados a ela). Basicamente: varremos os dados da categoria e então varremos
;outra vez para buscar os produtos associados a ela. A vantagem de usá-lo aqui é que varremos os dados menos vezes e
;conseguimos descobrir quem referencia nossas categorias de uma maneira mais direta.
(defn todos-nomes-produtos-da-categoria-backwards [snapshot-db, nome-categoria]
  (d/q '[:find (pull ?categoria [:categoria/nome { :produto/_categoria [:produto/nome :produto/slug] }])
         :in $ ?nome
         :where [?categoria :categoria/nome ?nome]]
       snapshot-db, nome-categoria))


;O código abaixo funciona, mas retorna um caso inesperado. Caso tenhamos cinco produtos, sendo que dois deles possuem
;o mesmo preco, o count retornará apenas quatro. Por quê? Porque Datomic trabalha com conjuntos de valores, e em um
;conjunto matemático valores iguais não são considerados.
;
  ;(defn maior-e-menor-precos-junto-da-quantidade-de-precos [snapshot-db]
  ;  (d/q '[:find  (min ?preco) (max ?preco) (count ?preco)
  ;         :keys  preco-minimo, preco-maximo, total-de-precos
  ;         :where [_ :produto/preco ?preco]]
  ;       snapshot-db))
;
;Se quisermos considerar valores repetidos no count, precisamos informar para ele algum outro atributo que ele possa
;usar para saber que, mesmo que os precos sejam iguais, eles não devem ser considerados valores iguais no retorno.
;Para isso, podemos usar o identificador do atributo da entidade dentro do banco (ou seja, se o preco for o mesmo mas
;pertencer a diferentes entidades, deve ser considerado no count)

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

;Podemos encadear queries para retornar valores mais específicos. Abaixo, por exemplo, encadeio duas queries:
;a primeira tenta obter o maior preco de todos e o segundo busca os dados do produto com o maior preco. Ela
;funciona, mas tem um problema: estamos buscando no banco duas vezes. Se o banco for local, não há problema; se
;ele for em outro servidor, são duas requisicoes (e o delay de cada uma) para ter a resposta final. Dificil.

  ;(defn produtos-mais-caro [snapshot-db]
  ;  (let [maior-preco (ffirst (d/q '[:find (max ?preco)
  ;                                   :where [_ :produto/preco ?preco]]
  ;                                 snapshot-db))]
  ;    (d/q '[:find (pull ?produto [*])
  ;           :in $, ?preco
  ;           :where [?produto :produto/preco ?preco]]
  ;         snapshot-db, maior-preco)))

;Uma alternativa seria fazer as duas queries em uma só, deixando para o banco a tarefa de otimizar as requisicoes
;e decidir como fazer acontecer. Abaixo, um exemplo onde fazemos as duas queries em uma. Note que o valor retornado
;pela query em (q) será armazenado dentro de ?preco, e ?preco será repassado para a segunda linha como um parametro
;do where. Note ainda que, dentro de (q), usamos o banco de dados como $, j[a que ele é passado como parametro na
;query pai, definida em (d/q):
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