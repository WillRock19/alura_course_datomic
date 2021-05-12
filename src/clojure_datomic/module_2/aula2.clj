(ns clojure-datomic.module-2.aula2
  (:use clojure.pprint)
  (:require [clojure-datomic.module-2.db :as db]
            [clojure-datomic.module-2.model :as model]
            [datomic.api :as d]))

(db/apaga-banco)
(def conn (db/abre-conexao))
(db/cria-schema conn)
;Caso eu use o UUID, um campo do tipo identidade, de um produto com outro, ao invés de
;adicionar outro produto com o mesmo UUID o Datomic irá atualizar TODAS as propriedades
;do registro existente para terem as propriedades do novo que está sendo inserido

(defn imprimir-itens-multiline-pprint [items]
  (mapv #(pprint %) items))

(let [produto-id                   (model/uuid)
      celular                      (model/novo-produto produto-id "Celular brilhante", "/celular-brilhante", 5000.98M)
      celular-barato               (model/novo-produto produto-id "Celular barato", "/celular-barato", 10.19M)
      resultado-primeira-transacao @(d/transact conn [celular])
      resultado-segunda-transacao  @(d/transact conn [celular-barato])]
  (println "==================================================================================")
  (println "Primeira transacao: ")
  (pprint resultado-primeira-transacao)
  (println "")
  (println "Segunda transacao: ")
  (pprint resultado-segunda-transacao)
  (println "")
  (println "Imprimindo itens inseridos...")
  (imprimir-itens-multiline-pprint (db/produto-por-produto-id (d/db conn) produto-id))
  (println "")
  (println "==================================================================================")
  (println "Imprimindo todos os produtos com nome:")
  (pprint (db/todos-os-ids-produtos-com-nome (d/db conn))))



