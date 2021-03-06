(ns clojure-datomic.module-1.db
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

; id_entidade    nome_atributo          valor              ID_TRANSACAO         OPERACAO
;     15            nome          Computador Novo         12527512212122          true
;     15            slug          /computador_novo        12527512212122          true
;     15            preco             3500.10             00827512212122          false

;Para isso, primeiro precisamos definir o schema do banco, a estrutura que nossos registros terao

(def schema [{:db/ident       :produto/nome
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one           ;quantidade dessa propriedade que existirá para cada entidade)
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
              :db/cardinality :db.cardinality/many}])

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

(defn todos-os-ids-produtos-com-nome [snapshot-db]
  (d/q '[:find ?entidade
         :where [?entidade :produto/nome]], snapshot-db))

(defn todos-os-ids-produtos-por-slug-brilhante [snapshot-db]
  (d/q '[:find ?entidade
         :where [?entidade :produto/slug "/celular-brilhante"]], snapshot-db))

(defn todos-os-ids-produtos-por-slug [snapshot-db, slug-para-busca]
  (d/q '[:find ?entidade
         :in $, ?slug-para-busca                            ;O $ é um nome que representa o banco de dados que estamos passando para ser usada na query
         :where [?entidade :produto/slug ?slug-para-busca]], snapshot-db, slug-para-busca))

; entity id = ?entidade = ?e
(defn todos-slugs-com-ids [snapshot-db]
  (d/q '[:find ?e, ?valor-propriedade-slug
         :where [?e :produto/slug ?valor-propriedade-slug]], snapshot-db))

(defn todos-slugs [snapshot-db]
  (d/q '[:find ?valor-propriedade-slug
         :where [_ :produto/slug ?valor-propriedade-slug]], snapshot-db)) ;O _ simboliza um elemento que não nos interessa porque não será usado

;Agora, e se quisermos fazer um where que analisa multiplas propriedades? Bom, poderiamos fazer como o código abaixo:
;(defn todos-nomes-e-precos-de-produtos [snapshot-db]
;  (d/q '[:find ?nome, ?preco
;         :where [_ :produto/nome  ?nome]
;                [_ :produto/preco ?preco]], snapshot-db))

;Mas, se fizermos isso, ele ira fazer uma concatenacao do resultado. Primeiro ira buscar todos os nomes e tratar como um vetor ([nomeA, nomeB, nomeC])
;e depois todos os precos, tratando como outro vetor ([precoA, precoC]). Então, retornará um vetor com a união dos dois, ou seja:
;[{nomeA, precoA}, {nomeB, precoA}, {nomeC, precoA}, {nomeA, precoC},{nomeB, precoC},  {nomeC, precoC}]

;Claramente, não é o que queremos. Para retornar apenas os precos e seus respectivos nomes, precisamos dizer ao Datomic que queremos os precos e nomes
;das mesmas entidades retornados juntos. Para isso, usamos:

(defn todos-nomes-e-precos-de-produtos [snapshot-db]
  (d/q '[:find ?nome, ?preco
         :keys produto/nome, produto/preco                  ;Define que na saida os dados dentro do find serão exibidos em um mapa com estas keys
         :where [?produto :produto/nome ?nome]
         [?produto :produto/preco ?preco]], snapshot-db))


;Também podemos especificar quais os dados que queremos retornar de uma entidade SEM definir eles no Where. Para fazer isso,
;precisamos fazer um *pull* dentro da entidade encontrada pelo Where, e nesse *pull* especificamos quais os atributos
;dessa entidade que queremos obter. Veja abaixo:
(defn todos-produtos-com-nome [snapshot-db]
  (d/q '[:find (pull ?entidade [:produto/nome, :produto/slug, :produto/preco])
         :where [?entidade :produto/nome ?nome]], snapshot-db))

;Também podemos usar um *pull* generíco, trazendo TUDO que a entidade possui.
(defn todos-produtos-com-nome-pull-generico [snapshot-db]
  (d/q '[:find (pull ?entidade [*])
         :where [?entidade :produto/nome ?nome]], snapshot-db))


;Imagine agora que queremos fazer uma query buscando o nome e preco de um produto, mas filtrar por onde o preco é maior do que
;1000. Para isso, podemos passar um predicado dentro da estrutura do :where que nosso banco recebe. Veja abaixo:

(defn todos-produtos-com-preco-maior-que-mil [snapshot-db]
  (d/q '[:find ?nome, ?preco
         :where [?entidade :produto/nome ?nome]
         [?entidade :produto/preco ?preco]
         [(> ?preco 1000)]], snapshot-db))

;Poderíamos ir além, entretanto. Poderíamos passar o preco do produto e então exibir apenas aqueles cujo preco for maior
;do que este número parametrizado. Ficaria algo assim:

(defn todos-produtos-com-preco-maior-que [snapshot-db preco-alvo]
  (d/q '[:find ?nome, ?preco
         :in $, ?preco-minimo
         :where [?entidade :produto/nome ?nome]
         [?entidade :produto/preco ?preco]
         [(> ?preco ?preco-minimo)]], snapshot-db preco-alvo))

;Poderíamos, ainda, adicionar outros parâmetros e pesquisas (por exemplo, pesquisar pelos produtos cujo preco é maior que
;X e a quantidade é maior que 10. Há, entretanto, algo que precisamos considerar aqui: quando pesquisa os registros no
;banco, o Datomic analisará TODOS os registros para cada condicao definida. No exemplo abaixo...
;
;(defn todos-produtos-com-preco-maior-que-alvo-e-quantidade-maior-que-dez [snapshot-db]
;  (d/q '[:find ?nome, ?preco, ?quantidade
;         :where [?entidade :produto/nome ?nome]
;                [?entidade :produto/preco ?preco]
;                [?entidade :produto/quantidade ?quantidade]
;                [(> ?preco 1000)]
;                [(> ?quantidade 10)]], snapshot-db))
;
;...iremos primeiro pesquisar TODOS os registros, encontrar os nomes de todos e fazer binding na variável entidade, então
;pesquisar todos os retornados e fazer binding na variável preco, então pesquisar todos retornados e fazer binding na quantidade,
;então novamente aplicando a funcao de preco-minimo e, por fim, de novo aplicando a funcao de quantidade. Agora, pense
;conosco:
;
;     1. E se houverem 5000 registros com preco maior do que 1000, mas apenas 5 registros com a quantidde > 10?
;
;Nesse caso, iremos pesquisar 5000 registros para depois pegar apenas dez deles. Note que uma condicao é MUITO mais restritiva
;que a outra quando pensamos na quantidade de registros retornados. Quando pensamos nisso, percebemos que podemos nós próprios
;otimizar o banco. Para isso, podemos primeiro filtrar pela condicao mais restritiva e, posteriormente, pela menos. Com isso,
;como o banco vai encadeando as buscas uma atrás da outra, nós conseguiríamos trazer menos registros e buscar pelo restante dos
;dados de maneira mais rápida.
;
;Uma maneira otimizada para a condicao citada acima seria:
;
;(defn todos-produtos-com-preco-maior-que-alvo-e-quantidade-maior-que-dez-2 [snapshot-db]
;  (d/q '[:find ?nome, ?preco, ?quantidade
;         :where [?entidade :produto/quantidade ?quantidade]
;                [(> ?quantidade 10)]]
;                [?entidade :produto/preco ?preco]
;                [(> ?preco 1000)]
;                [?entidade :produto/nome ?nome], snapshot-db))
;

(defn todos-produtos-por-palavra-chave [snapshot-db, palavra-chave]
  (d/q '[:find (pull ?entidade [*])
         :in $ ?palavra-chave-buscada
         :where [?entidade :produto/palavra-chave ?palavra-chave-buscada]],
       snapshot-db, palavra-chave))