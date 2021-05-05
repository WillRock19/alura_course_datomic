(ns clojure-datomic.core
  (:use clojure.pprint)
  (:require [clojure-datomic.db :as db]
            [clojure-datomic.model :as model]
            [datomic.api :as d]))

(def conn (db/abre-conexao))

;Transacionando o schema no banco para ensinar a ele o tipo de estrutura que queremos que ele tenha
(db/cria-schema conn)

;Inserindo primeiro registro no banco
(pprint
  (let [computador (model/novo-produto "Computador Novo", "/computador_novo", 2500.10M)] ;M é para tornar o floar um BigDecimal
    (d/transact conn [computador])))

;Fazendo uma query que retorna o id das entidades que possuam :produto/nome como atributos (segunda coluna de uma linha)
;(d/q '[:find ?entidade_id
;       :where [?entidade_id :produto/nome]], conn)

;O codigo acima não vai funcionar, porque estamos passando uma conexão. Conexões só servem para inserir dados. Para buscar,
;precisamos passar uma cópia do banco para que o Datomic possa fazer sua pesquisa. Para isso, podemos usar o d/db, que
;retorna uma cópia (snapshot) do banco naquele determinado instante do tempo.

(d/q '[:find ?entidade_id
       :where [?entidade_id :produto/nome]], (d/db conn))

;O Datomic não obriga a seguir o esquema, não te forca a colocar valores dentro das estruturas

(let [calculadora { :produto/nome "Calculadora sem demais propriedades" }]
  (d/transact conn [calculadora]))

;Entretanto, ele não permite inserir nulo em uma propriedade. A operacão abaixo, por exemplo, retorna erro
;(let [calculadora-com-nulo { :produto/nome "Calculadora sem demais propriedades" :produto/slug nil }]
;  (d/transact conn [calculadora-com-nulo]))

;Podemos, inclusive, realizar operacoes de update e de delete. Updates literalmente alteram os valores de propriedades,
;mas fazem isso com um "delete-insert" - em outras palavras: ela deleta o atributo (seta a coluna operation do atributo
;para false) antes de inserir uma nova linha com o valor novo. E, como voce ja deve imaginar, para a operacao de delete
;ele apenas seta a coluna operation do attributo como false.
;
;Vejamos um exemplo de cada:

;Funcao de Update
(defn update-register [register-id, attribute-to-update, new-value]
  (d/transact conn [[:db/add register-id attribute-to-update new-value]]))

(let [celular-barato            (model/novo-produto "Celular baratenho", "/celular_baratenho", 88888.10M)
      resultado-insercao        @(d/transact conn, [celular-barato])
      id-elemento-inserido      (first (vals (:tempids resultado-insercao)))
      updated-register-result   @(update-register id-elemento-inserido, :produto/preco, 0.1M)]
  (pprint updated-register-result)
  (println "=================================================")
  (println ""))

;Funcao de Delete
(defn remove-attribute [register-id, attribute-to-remove value-to-be-removed]
  (d/transact conn [[:db/retract register-id, attribute-to-remove, value-to-be-removed]]))
s
(let [celular-barato            (model/novo-produto "Celular baratenho 2", "/celular_baratenho_2", 88888.10M)
      resultado-insercao        @(d/transact conn, [celular-barato])
      id-elemento-inserido      (first (vals (:tempids resultado-insercao)))
      removed-register-result   @(remove-attribute id-elemento-inserido, :produto/slug "/celular_baratenho_2")]
  (pprint removed-register-result)
  (println "=================================================")
  (println ""))