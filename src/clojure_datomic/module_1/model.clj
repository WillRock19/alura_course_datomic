(ns clojure-datomic.module-1.model)

(defn novo-produto [nome, slug, preco]
  { :produto/nome nome,
   :produto/slug slug,
   :produto/preco preco })