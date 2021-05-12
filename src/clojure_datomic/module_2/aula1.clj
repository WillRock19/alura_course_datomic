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

;Inserindo itens com um uuid gerado previamente e buscando os dados pelo datomic-id
(let [computador           (model/novo-produto (model/uuid) "Computador Novo", "/computador-novo", 2500.10M)
      celular              (model/novo-produto (model/uuid) "Celular brilhante", "/celular-brilhante", 5000.98M)
      calculadora          {:produto/nome "Calculadora"}
      celular-barato       (model/novo-produto (model/uuid) "Celular barato", "/celular-barato", 10.19M)
      resultado-transacao @(d/transact conn [computador, celular, calculadora, celular-barato])]
  (println "==================================================================================")
  (println "Ids das entidades inseridas:")
  (println "")
  (let [ids-entidades (mapv #(second %) (-> resultado-transacao :tempids))]
    (imprimir-itens-multiline-println ids-entidades)
    (println "")
    (println "==================================================================================")
    (println "Imprimindo itens inseridos...")
    (imprimir-itens-multiline-pprint (db/todos-os-produtos-por-id-do-datomic (d/db conn) ids-entidades))))

(println "==================================================================================")
(println "Criando e buscando um único item...")

;Inserindo um ipad sem UUID previamente estabelecido e buscando pelo datomic-id
(let [ipad                (model/novo-produto "Ipad", "/ipad", 8900.10M)
      resultado-transacao @(d/transact conn [ipad])]
  (println "")
  (println "Ids das entidades inseridas:")
  (let [ids-entidades (mapv #(second %) (-> resultado-transacao :tempids))]
    (imprimir-itens-multiline-println ids-entidades)
    (println "")
    (println "==================================================================================")
    (println "Imprimindo item inserido...")
    (pprint (db/produto-por-datomic-id (d/db conn) (first ids-entidades)))))

;Inserindo um teclado com um UUID previamente conhecido e buscando por ele
(let [produto-id          (model/uuid)
      teclado-mecanico    (model/novo-produto produto-id "Teclado mecanico", "/teclado-mecanico-1", 756.10M)
      resultado-transacao @(d/transact conn [teclado-mecanico])]
  (println "")
  (println "==================================================================================")
  (println "Ids do teclado mecanico:")
  (println "-> Datomic-id  :" (-> resultado-transacao :tempids vals first))
  (println "-> Produto UUID:" produto-id)
  (println "Imprimindo dados do teclado mecanico...")
  (pprint (db/produto-por-produto-id (d/db conn) produto-id)))
