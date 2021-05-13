(ns clojure-datomic.module-3.aula1
  (:use clojure.pprint)
  (:require [clojure-datomic.module-3.aux-functions :as aux]
            [clojure-datomic.module-3.db :as db]
            [clojure-datomic.module-3.model :as model]
            [datomic.api :as d]))

(db/apaga-banco)
(def conn (db/abre-conexao))
(db/cria-schema conn)

(def eletronicos (model/nova-categoria "Eletrônicos"))
(def esportes (model/nova-categoria "Esportes"))
(def jogos (model/nova-categoria "Jogos"))

(def endereco-ip-de-exemplo "200.219.877.555")

(println "=======================================================")
(println "")
(println "Cadastrando categorias...")
(pprint (db/adiciona-categorias! conn [eletronicos, esportes, jogos]))

(let [computador      (model/novo-produto (model/uuid) "Computador Novo", "/computador-novo", 2500.10M)
      celular         (model/novo-produto (model/uuid) "Celular brilhante", "/celular-brilhante", 5000.98M)
      celular-barato  (model/novo-produto (model/uuid) "Celular barato", "/celular-barato", 10.19M)
      bola-futebol    (model/novo-produto (model/uuid) "Bola de futebol", "/bola-futebol", 97.89M)
      jogo-de-xadrez {:produto/id    (model/uuid),
                      :produto/nome  "Tabuleiro de Xadrez Mattel",
                      :produto/slug  "/tabuleiro-xadrez-mattel",
                      :produto/preco  10.19M,
                      :produto/categoria { :categoria/id   (:categoria/id   jogos),
                                          :categoria/nome  (:categoria/nome jogos) }}
      resultado-transacao1 (db/adiciona-produtos! conn [celular, celular-barato])
      resultado-transacao2 (db/adiciona-produtos! conn [bola-futebol, computador, jogo-de-xadrez] endereco-ip-de-exemplo)]
  (let [ids-entidades (mapv #(second %) (-> (conj @resultado-transacao1 @resultado-transacao2) :tempids))]
    (aux/imprimir-itens-multiline-println ids-entidades)
    (println "")
    (println "==================================================================================")
    (println "Imprimindo itens inseridos...")
    (aux/imprimir-itens-multiline-pprint (db/todos-os-produtos-por-id-do-datomic (d/db conn) ids-entidades)))
  (println "")
  (println "==================================================================================")
  (println "Adicionando categorias aos registros cadastrados...")
  (pprint @(db/atribui-categorias conn [computador, celular-barato] eletronicos))
  (pprint @(db/atribui-categorias conn [bola-futebol] esportes))
  (println "")

  )
