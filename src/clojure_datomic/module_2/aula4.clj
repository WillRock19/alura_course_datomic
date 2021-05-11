(ns clojure-datomic.module-2.aula4
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

(defn- buscar-e-imprimir-produtos [produtos]
       (mapv (fn[produto]
               (println "")
               (imprimir-itens-multiline-pprint (db/produto-por-produto-id (d/db conn) (:produto/id produto))))
            produtos))

(def eletronicos (model/nova-categoria "Eletrônicos"))
(def esportes (model/nova-categoria "Esportes"))

(println "=======================================================")
(println "")
(println "Cadastrando categorias...")
(pprint (db/adiciona-categorias! conn [eletronicos, esportes]))

(println "=======================================================")
(println "")
(println "Categorias cadastradas...")
(pprint (db/todas-as-categorias (d/db conn)))
(println "")

(let [computador (model/novo-produto (model/uuid) "Computador Novo", "/computador-novo", 2500.10M)
      celular (model/novo-produto (model/uuid) "Celular brilhante", "/celular-brilhante", 5000.98M)
      celular-barato (model/novo-produto (model/uuid) "Celular barato", "/celular-barato", 10.19M)
      bola-futebol (model/novo-produto (model/uuid) "Bola de futebol", "/bola-futebol", 97.89M)
      resultado-transacao (db/adiciona-produtos! conn [bola-futebol, computador, celular, celular-barato])]
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
     (pprint (db/atribui-categorias conn [computador, celular-barato] eletronicos))
     (pprint (db/atribui-categorias conn [bola-futebol] esportes))
     (println "")
     (println "==================================================================================")
     (println "Imprimindo nomes e categorias dos produtos que possuem categorias...")
     (imprimir-itens-multiline-pprint (db/todos-nomes-produtos-com-suas-categorias (d/db conn)))
     (println "")
     (println "==================================================================================")
     (println "Imprimindo produtos que não possuem categorias...")
     (imprimir-itens-multiline-pprint (db/todos-nomes-produtos-sem-categorias (d/db conn)))
     (println "")
     (println "==================================================================================")
     (println "Buscando produtos pela categoria (forward navigation):" (:categoria/nome eletronicos) "...")
     (imprimir-itens-multiline-pprint (db/todos-nomes-produtos-da-categoria (d/db conn) (:categoria/nome eletronicos)))
     (println "")
     (println "Buscando produtos pela categoria (forward navigation):" (:categoria/nome esportes) "...")
     (imprimir-itens-multiline-pprint (db/todos-nomes-produtos-da-categoria (d/db conn) (:categoria/nome esportes)))
     (println "")
     (println "==================================================================================")
     (println "Buscando produtos pela categoria (backwards navigation):" (:categoria/nome eletronicos) "...")
     (imprimir-itens-multiline-pprint (db/todos-nomes-produtos-da-categoria-backwards (d/db conn) (:categoria/nome eletronicos)))
     (println "")
     (println "Buscando produtos pela categoria (backwards navigation):" (:categoria/nome esportes) "...")
     (imprimir-itens-multiline-pprint (db/todos-nomes-produtos-da-categoria-backwards (d/db conn) (:categoria/nome esportes))))
