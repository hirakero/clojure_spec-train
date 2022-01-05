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


(defn test-fn [m])

;; https://clojure.org/guides/spec
;; Predicates

; Clojureの述語関数は、暗黙的にspecに変換される
; confirmは関数は、specとデータ値を取ります。
; 戻り値は適合値
; 値が適合していない場合は、特別な値:clojure.spec.alpha/invalidが返されます。
(s/conform even? 100) ;-> 100

;valid? ブール値を返す
(s/valid? even? 10) ;;=> true

(s/valid? nil? nil)  ;; true
(s/valid? string? "abc")  ;; true

(s/valid? #(> % 5) 10) ;; true
(s/valid? #(> % 5) 0) ;; false

(import java.util.Date)
(s/valid? inst? (Date.))  ;; true

;セットは、1つ以上のリテラル値に一致する述語としても使用できます。
(s/valid? #{:club :diamond :heart :spade} :club) ;; true
(s/valid? #{:club :diamond :heart :spade} 42) ;; false

(s/valid? #{42} 42) ;; true

;;Registry

;specは、再利用可能な仕様をグローバルに宣言するための中央レジストリを提供します。
;レジストリは、名前空間付きキーワードをspecに関連付けます。
;名前空間を使用すると、ライブラリまたはアプリケーション間で再利用可能な競合しないspecを確実に定義できます。

;specはs / defを使用して登録されます。
(s/def :order/date inst?)
(s/def :deck/suit #{:club :diamond :heart :spade})

(s/valid? :order/date (Date.));;=> true
(s/conform :deck/suit :club);;=> :club

;docでspecの内容を確認できます。
(clojure.repl/doc :order/date)
"-------------------------
:order/date
Spec
  inst?"
(clojure.repl/doc :deck/suit)
":deck/suit
Spec
  #{:spade :heart :diamond :club}"

;; Composing predicates
; 仕様を作成する最も簡単な方法は、andおよびorを使用することです。
(s/def :num/big-even (s/and even? #(> % 1000)))
(s/valid? :num/big-even :foo) ;; ->false
(s/valid? :num/big-even 10) ;; ->false
(s/valid? :num/big-even 100000) ;; ->true

(s/def :domain/name-or-id (s/or :name string? :id   int?))
(s/valid? :domain/name-or-id "abc") ;; ->true
(s/valid? :domain/name-or-id 100) ;; ->true
(s/valid? :domain/name-or-id :foo) ;; ->false

; 各選択肢にはタグ（ここでは：nameと：idの間）が注釈として付けられ、
;これらのタグは、conformおよびその他のspec関数から返される
(s/conform :domain/name-or-id "abc");;=> [:name "abc"]
(s/conform :domain/name-or-id 100);;=> [:id 100]

;インスタンスのタイプをチェックする多くの述語は、有効な値としてnilを許可しません
;有効な値としてnilを含めるには、提供されている関数nilableを使用して仕様を作成します。
(s/valid? string? nil);;=> false
(s/valid? (s/nilable string?) nil);;=> true

;; Explain
;Explainは、値が仕様に準拠していない理由を *out* に出力します。
(s/explain :deck/suit 42)
; 42 - failed: #{:spade :heart :diamond :club} spec: :deck/suit
(s/explain :num/big-even 5)
;; 5 - failed: even? spec: :num/big-even
(s/explain :num/big-even 6)
;; 6 - failed: (> % 1000) spec: :num/big-even
(s/explain :domain/name-or-id :foo)
;; :foo - failed: string? at: [:name] spec: :domain/name-or-id
;; :foo - failed: int? at: [:id] spec: :domain/name-or-id

