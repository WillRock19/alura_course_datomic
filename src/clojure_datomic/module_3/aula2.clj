(ns clojure-datomic.module-3.aula2
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

(println "=======================================================")
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

(println "==================================================================================")
(println "==================================================================================")
(println "=====================INSERINDO/ATUALIZANDO PRODUTOS EM TRANSACOES==================")
(println "==================================================================================")
(println "==================================================================================")
(println "")

(def dama (model/novo-produto (model/uuid) "Jogo de dama", "/dama", 19.89M))
(db/adiciona-ou-atualiza-produtos! conn [dama])

(defn atualiza-preco []
  (println "Atualizando preco...")
  (let [produto (db/produto-por-id (d/db conn) (:produto/id dama))
        produto (assoc produto :produto/preco 29.10M)]
    (db/adiciona-ou-atualiza-produtos! conn [produto])
    (println "... preco atualizado :)")
    produto))

(defn atualiza-slug []
  (println "Atualizando slug...")
  (let [produto (db/produto-por-id (d/db conn) (:produto/id dama))]
    (Thread/sleep 3000)
    (let [produto (assoc produto :produto/slug "/novo-endpoint-dama")]
      (db/adiciona-ou-atualiza-produtos! conn [produto])
      (println "... slug atualizado! ^^")
      produto)))

(defn roda-transacoes [tx]
  (let [futuros (mapv #(future (%)) tx)]
    (println "")
    (println "O que nossas futures fizeram?")
    (pprint (map deref futuros))
    (println "")
    (pprint "Resultado final:")
    (pprint (db/produto-por-id (d/db conn) (:produto/id dama)))))

(roda-transacoes [atualiza-preco, atualiza-slug])




