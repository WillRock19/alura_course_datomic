(ns clojure-datomic.module-3.model
  (:require [schema.core :as s]))

(def Categoria
  {:categoria/id    java.util.UUID
   :categoria/nome  s/Str})

(def Produto
  {:produto/id                              java.util.UUID,
   :produto/nome                            s/Str,
   :produto/slug                            s/Str,
   :produto/preco                           BigDecimal,
   (s/optional-key :produto/palavra-chave)  [s/Str],
   (s/optional-key :produto/categoria)      Categoria})

(defn uuid []
  (java.util.UUID/randomUUID))

(defn novo-produto
  ([nome, slug, preco]
   (novo-produto (uuid) nome slug preco))
  ([uuid, nome, slug, preco]
   {:produto/id    uuid,
    :produto/nome  nome,
    :produto/slug  slug,
    :produto/preco preco}))

(defn nova-categoria
  ([nome]
   (nova-categoria (uuid) nome))
  ([uuid nome]
   {:categoria/id   uuid,
    :categoria/nome nome}))
