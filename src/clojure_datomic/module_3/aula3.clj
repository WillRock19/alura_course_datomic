(ns clojure-datomic.module-3.aula3
  (:use clojure.pprint)
  (:require [clojure-datomic.module-3.aux-functions :as aux]
            [clojure-datomic.module-3.db :as db]
            [clojure-datomic.module-3.model :as model]
            [datomic.api :as d]
            [schema.core :as s]))

(s/set-fn-validation! true)

(db/apaga-banco)
(def conn (db/abre-conexao))
(db/cria-schema conn)

(defn- products-have-valid-schema [products]
  (doall (map-indexed (fn [index, product]
                        (println "")
                        (println index "." (:produto/nome product) ":")
                        (pprint (s/validate model/Produto product))) products)))

(def eletronicos (model/nova-categoria "Eletrônicos"))
(def esportes (model/nova-categoria "Esportes"))
(def jogos (model/nova-categoria "Jogos"))
(def endereco-ip-de-exemplo "200.219.877.555")

(println "=======================DEFAULT BEHAVIOUR FOR EACH CLASS================================")
(println "")
(println "Cadastrando categorias...")
(pprint (db/adiciona-ou-atualiza-categorias! conn [eletronicos, esportes, jogos]))

(let [computador      (model/novo-produto (model/uuid) "Computador Novo", "/computador-novo", 2500.10M)
      celular         (model/novo-produto (model/uuid) "Celular brilhante", "/celular-brilhante", 5000.98M)
      celular-barato  (model/novo-produto (model/uuid) "Celular barato", "/celular-barato", 10.19M)
      bola-futebol    (model/novo-produto (model/uuid) "Bola de futebol", "/bola-futebol", 97.89M)
      jogo-de-xadrez {:produto/id    (model/uuid),
                      :produto/nome  "Tabuleiro de Xadrez Mattel",
                      :produto/slug  "/tabuleiro-xadrez-mattel",
                      :produto/preco  10.19M,
                      :produto/categoria { :categoria/id   (:categoria/id   jogos),
                                          :categoria/nome  (:categoria/nome jogos) }}]
  (println "")
  (println "==================================================================================")
  (println "Vamos validar os produtos antes de salvá-los?")
  (println "")
  (products-have-valid-schema [computador, celular, celular-barato, bola-futebol, jogo-de-xadrez])
  (println "==================================================================================")
  (println "Salvando produtos...")
  (db/adiciona-ou-atualiza-produtos! conn [celular, celular-barato])
  (db/adiciona-ou-atualiza-produtos! conn [bola-futebol, computador, jogo-de-xadrez] endereco-ip-de-exemplo)
  (println "==================================================================================")
  (println "Adicionando categorias aos registros cadastrados...")
  (pprint @(db/atribui-categorias conn [computador, celular-barato] eletronicos))
  (pprint @(db/atribui-categorias conn [bola-futebol] esportes))
  (println "")
  (println "==================================================================================")
  (println "Todos os produtos e suas categorias...")
  (aux/imprimir-itens-multiline-pprint (db/todos-produtos (d/db conn)))
  (println ""))

(println "===================================OBTENDO DADOS DOS PRODUTOS===============================================")
(let [todos-produtos    (db/todos-produtos (d/db conn))
      primeiro-produto  (first todos-produtos)]
  (println "Todos os produtos:")
  (pprint todos-produtos)
  (println " ")
  (println "Primeiro da lista:")
  (pprint primeiro-produto)
  (println " ")
  (println "Dados do primeiro obtidos diretamente do banco: ")
  ;(pprint (db/produto-por-id (d/db conn) (:produto/id primeiro-produto)))
  ; A funcao acima irá lancar uma excecao. Quando tentamos obter os dados do banco, o datomic irá obter a propriedade
  ; *categoria* do produto, mas irá retornar apenas o :db/id do registro, e temos uma funcao que remove o :db/id dos nossos
  ; registros, lembra? Dessa forma, como nosso schema exige que uma categoria tenha um :nome e um :id mas recebe uma categoria
  ; vazia {}, ele irá lancar um erro. Para resolver, precisamos deixar claro COMO queremos obter os dados
  ; associados ao nosso produto. Assim, podemos:
  ;
  ;     1. Retornar TODOS os dados da categoria sempre que obtivermos nosso produto;
  ;     2. Modificar a funcao que remove o :db/id para remover TODA a entidade caso ela só possua esse atributo.
  ;
  ; Nas aulas, optamos por ir pela primeira opcao. Para tanto, criei a funcao abaixo:
  (pprint (db/produto-por-id-com-categoria (d/db conn) (:produto/id primeiro-produto)))


  )







