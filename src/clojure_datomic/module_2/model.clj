(ns clojure-datomic.module-2.model)

(defn novo-produto [nome, slug, preco]
  { :produto/nome nome,
   :produto/slug slug,
   :produto/preco preco })
