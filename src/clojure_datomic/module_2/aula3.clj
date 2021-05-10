(ns clojure-datomic.module-2.aula3
    (:use clojure.pprint)
    (:require [clojure-datomic.module-2.db :as db]
      [clojure-datomic.module-2.model :as model]
      [datomic.api :as d]))

(db/apaga-banco)
(def conn (db/abre-conexao))
(db/cria-schema conn)

(defn- imprimir-itens-multiline-println [items]
       (mapv #(println "->" %) items))

(defn- imprimir-itens-multiline-pprint [items]
       (mapv #(pprint %) items))

(def eletronicos (model/nova-categoria "Eletrônicos"))
(def esportes (model/nova-categoria "Esportes"))

(println "=======================================================")
(println "")
(println "Cadastrando categorias...")
(pprint @(d/transact conn [eletronicos, esportes]))

(println "=======================================================")
(println "")
(println "Categorias cadastradas...")
(pprint (db/todas-as-categorias (d/db conn)))
(println "")

(let [computador (model/novo-produto (model/uuid) "Computador Novo", "/computador-novo", 2500.10M)
      celular (model/novo-produto (model/uuid) "Celular brilhante", "/celular-brilhante", 5000.98M)
      celular-barato (model/novo-produto (model/uuid) "Celular barato", "/celular-barato", 10.19M)
      bola-futebol (model/novo-produto (model/uuid) "Bola de futebol", "/bola-futebol", 97.89M)
      resultado-transacao @(d/transact conn [bola-futebol, computador, celular, celular-barato])]
     (println "==================================================================================")
     (println "Db/ids das entidades inseridas:")
     (println "")
     (let [ids-entidades (mapv #(second %) (-> resultado-transacao :tempids))]
          (imprimir-itens-multiline-println ids-entidades)
          (println "")
          (println "==================================================================================")
          (println "Imprimindo itens inseridos...")
          (imprimir-itens-multiline-pprint (db/todos-os-produtos-por-id-do-datomic (d/db conn) ids-entidades)))
     (println "")
     (println "==================================================================================")
     (println "Adicionando categorias aos registros cadastrados...")
     (pprint (d/transact conn [[:db/add
                                [:produto/id (:produto/id computador)]          ;Obtém o id do computador
                                :produto/categoria                              ;Nome da propriedade que será adicionada
                                [:categoria/id (:categoria/id eletronicos)]]])) ;Referencia ao registro que será associado à propriedade anterior
     (pprint (d/transact conn [[:db/add
                                [:produto/id (:produto/id celular)]
                                :produto/categoria
                                [:categoria/id (:categoria/id eletronicos)]]]))
     (pprint (d/transact conn [[:db/add
                                [:produto/id (:produto/id celular-barato)]
                                :produto/categoria
                                [:categoria/id (:categoria/id eletronicos)]]]))
     (pprint (d/transact conn [[:db/add
                                [:produto/id (:produto/id bola-futebol)]
                                :produto/categoria
                                [:categoria/id (:categoria/id esportes)]]]))
     (println "")
     (println "==================================================================================")
     (println "Verificando se produtos possuem as categorias...")
     (imprimir-itens-multiline-pprint (db/produto-por-produto-id (d/db conn) (:produto/id computador)))
     (imprimir-itens-multiline-pprint (db/produto-por-produto-id (d/db conn) (:produto/id celular)))
     (imprimir-itens-multiline-pprint (db/produto-por-produto-id (d/db conn) (:produto/id celular-barato)))
     (imprimir-itens-multiline-pprint (db/produto-por-produto-id (d/db conn) (:produto/id bola-futebol))))



