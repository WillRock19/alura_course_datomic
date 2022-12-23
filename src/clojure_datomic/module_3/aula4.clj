(ns clojure-datomic.module-3.aula4
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

(let [computador      (model/novo-produto (model/uuid) "Computador Novo", "/computador-novo", 2500.10M, 10)
      celular         (model/novo-produto (model/uuid) "Celular brilhante", "/celular-brilhante", 5000.98M)
      celular-barato  (model/novo-produto (model/uuid) "Celular barato", "/celular-barato", 10.19M, 99)
      bola-futebol    (model/novo-produto (model/uuid) "Bola de futebol", "/bola-futebol", 97.89M, 9)
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


