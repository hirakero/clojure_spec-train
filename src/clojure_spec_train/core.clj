(ns clojure-spec-train.core
  (:require [clojure.spec.alpha :as s])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

;; マップのキーセットのためのspec
; keys  
;  キー名のベクターに対応づけたキーワード引数に対応
;  :req 必須のキーを指定 and,orに対応
(s/def ::req-test (s/keys :req [::x]))
(s/conform ::req-test {::x 1})
;-> #:clojure-spec-train.core{:x 1}
(s/conform ::req-test {::x 1 ::y 2})
;-> #:clojure-spec-train.core{:x 1, :y 2}
(s/conform ::req-test {::y 2})
;-> :clojure.spec.alpha/invalid

;  :opt ドキュメントとして機能し、 ジェネレータによって使用される場合があります。
;  :req-un :opt-un 名前空間が付いて再利用可能なspecとの接続をサポートする
(s/keys :req-un [:my.ns/a :my.ns/b])
;  :gen ：gengenerator-fnを取ります。これは引数のないfnでなければなりません。 test.checkジェネレーターを返します。

(s/def ::sample--map-spec (s/keys :req [::x ::y (or ::secret (and ::user ::password))] :opt [::z]))
(s/conform ::sample--map-spec {::x 1 ::y 2})
;-> :clojure.spec.alpha/invalid
(s/conform ::sample--map-spec {::x 1 ::y 2 ::secret 3})
;-> #:clojure-spec-train.core{:x 1, :y 2, :secret 3}
(s/conform ::sample--map-spec {::x 1 ::y 2 ::user 3})
;-> :clojure.spec.alpha/invalid
(s/conform ::sample--map-spec {::x 1 ::y 2 ::user 3 ::password 4})
;-> #:clojure-spec-train.core{:x 1, :y 2, :user 3, :password 4}

(s/valid? ::sample--map-spec {::x 1 ::y 2 ::user 3});f->alse
(s/valid? ::sample--map-spec {::x 1 ::y 2 ::user 3 ::password 4}) ;->true
(s/valid? ::sample--map-spec {::x 1 ::y 2 ::user 3});f->alse
(s/valid? ::sample--map-spec {::x 1 ::y 2 ::user 3});f->alse

(s/explain ::sample--map-spec {::x 1 ::y 2 ::user 3});f->alse
;#:clojure-spec-train.core{:x 1, :y 2, :user 3} - failed: (or (contains? % :clojure-spec-train.core/secret) (and (contains? % :clojure-spec-train.core/user) (contains? % :clojure-spec-train.core/password))) spec: :clojure-spec-train.core/sample--map-spec
(s/explain-data ::sample--map-spec {::x 1 ::y 2 ::user 3});f->alse
;#:clojure.spec.alpha{:problems ({:path [], :pred (clojure.core/fn [%] (clojure.core/or (clojure.core/contains? % :clojure-spec-train.core/secret) (clojure.core/and (clojure.core/contains? % :clojure-spec-train.core/user) (clojure.core/contains? % :clojure-spec-train.core/password)))), :val #:clojure-spec-train.core{:x 1, :y 2, :user 3}, :via [:clojure-spec-train.core/sample--map-spec], :in []}), :spec :clojure-spec-train.core/sample--map-spec, :value #:clojure-spec-train.core{:x 1, :y 2, :user 3}}


;; シーケンス
(s/def ::even? (s/and integer? even?))
(s/def ::odd? (s/and integer? odd?))
(s/def ::a integer?)
(s/def ::b integer?)
(s/def ::c integer?)
(def s1 (s/cat :forty-two #{42}
               :odds (s/+ ::odd?)
               :m (s/keys :req-un [::a ::b ::c])
               :oes (s/* (s/cat :o ::odd? :e ::even?))
               :ex (s/alt :odd ::odd? :even ::even?)))
(s/conform s1 [42 11 13 15 {:a 1 :b 2 :c 3} 1 2 3 42 43 44 11])
#_{:forty-two 42
 :odds [11 13 15]
 :m {:a 1, :b 2, :c 3}
 :oes [{:o 1, :e 2} {:o 3, :e 42} {:o 43, :e 44}]
 :ex [:odd 11]}

