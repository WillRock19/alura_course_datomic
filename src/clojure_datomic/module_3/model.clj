(ns clojure-datomic.module-3.model
  (:require [schema.core :as s]))

(def Categoria
  {:categoria/id    java.util.UUID
   :categoria/nome  s/Str})

(def Produto
  {:produto/id                             java.util.UUID,
   (s/optional-key :produto/nome)          s/Str,
   (s/optional-key :produto/slug)          s/Str,
   (s/optional-key :produto/preco)         BigDecimal,
   (s/optional-key :produto/palavra-chave) [s/Str],
   (s/optional-key :produto/categoria)     Categoria
   (s/optional-key :produto/estoque)       s/Int})

(defn uuid []
  (java.util.UUID/randomUUID))

(defn novo-produto
  ([nome, slug, preco]
   (novo-produto (uuid) nome slug preco 0))
  ([uuid, nome, slug, preco]
   (novo-produto uuid nome slug preco 0))
  ([uuid, nome, slug, preco, estoque]
   {:produto/id      uuid,
    :produto/nome    nome,
    :produto/slug    slug,
    :produto/preco   preco
    :produto/estoque estoque}))

(defn nova-categoria
  ([nome]
   (nova-categoria (uuid) nome))
  ([uuid nome]
   {:categoria/id   uuid,
    :categoria/nome nome}))
