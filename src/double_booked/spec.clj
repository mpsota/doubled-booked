(ns double-booked.spec
  (:require [clojure.spec.alpha :as s]))

;; Modeling input sequence as sequence of maps, where each map has: :id, :start-time :end-time.
;; and for each sequence :id is unique.

(s/def ::id (s/and int?
                   #(<= 0 %)))
(s/def ::time (s/and int?
                     #(<= 0 %)))                            ;; For now, just int, no time object.
(s/def ::start-time ::time)
(s/def ::end-time ::time)

(s/def ::event (s/and
                 (s/keys :req [::id ::start-time ::end-time])
                 #(<= (::start-time %) (::end-time %))))
(s/def ::events (s/and (s/* ::event)
                       #(or (empty? %) (apply distinct? (map ::id %)))))