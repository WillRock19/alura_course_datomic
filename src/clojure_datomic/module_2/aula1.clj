(ns clojure-datomic.module-2.aula1
  (:use clojure.pprint)
  (:require [clojure-datomic.module-2.db :as db]
            [clojure-datomic.module-2.model :as model]
            [datomic.api :as d]))

(db/apaga-banco)

(def conn (db/abre-conexao))

(db/cria-schema conn)

;Se quisermos verificar os dados dentro do banco sem ficar pesquisando na unha, podemos usar o comando
;que faz esses dados serem acessíveis pelo navegador. Um exemplo seria:
;
;bin/console -p 8080 dev datomic:dev://localhost:4334
;
;Então vá em: http://localhost:8080/browse

(defn imprimir-itens-multiline-println [items]
  (mapv #(println "->" %) items))

(defn imprimir-itens-multiline-pprint [items]
  (mapv #(pprint %) items))

(let [computador          (model/novo-produto "Computador Novo", "/computador-novo", 2500.10M)
      celular             (model/novo-produto "Celular brilhante", "/celular-brilhante", 5000.98M)
      calculadora         {:produto/nome "Calculadora"}
      celular-barato      (model/novo-produto "Celular barato", "/celular-barato", 10.19M)
      resultado-transacao @(d/transact conn [computador, celular, calculadora, celular-barato])]
  (println "==================================================================================")
  (println "Ids das entidades inseridas:")
  (println "")
  (let [ids-entidades (mapv #(second %) (-> resultado-transacao :tempids))]
    (imprimir-itens-multiline-println ids-entidades)
    (println "")
    (println "==================================================================================")
    (println "Imprimindo itens inseridos...")
    (imprimir-itens-multiline-pprint (db/todos-os-produtos-por-id (d/db conn) ids-entidades))))

(println "==================================================================================")
(println "Criando e buscando um único item...")

(let [ipad                (model/novo-produto "Computador Novo", "/computador-novo", 2500.10M)
      resultado-transacao @(d/transact conn [ipad])]
  (println "")
  (println "Ids das entidades inseridas:")
  (let [ids-entidades (mapv #(second %) (-> resultado-transacao :tempids))]
    (imprimir-itens-multiline-println ids-entidades)
    (println "")
    (println "==================================================================================")
    (println "Imprimindo item inserido...")
    (pprint (db/produto-por-id (d/db conn) (first ids-entidades)))))
