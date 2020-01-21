(ns double-booked.core-test
  (:require [clojure.test :refer :all]
            [double-booked.core :refer :all]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [double-booked.spec :as dbs]))

(deftest test-double-booked
  (testing "Double-booked events"
    (let [events-1 [{::dbs/id 1 ::dbs/start-time 10 ::dbs/end-time 15}
                    {::dbs/id 2 ::dbs/start-time 10 ::dbs/end-time 12}
                    {::dbs/id 3 ::dbs/start-time 0 ::dbs/end-time 3}
                    {::dbs/id 4 ::dbs/start-time 5 ::dbs/end-time 9}
                    {::dbs/id 5 ::dbs/start-time 0 ::dbs/end-time 11}]]
      (is (= (get-double-booked-events events-1)
             '([3 5] [5 4] [5 1] [1 2] [5 2])))))

  (testing "double-booked events with full overlap"
    (let [events-2 [{::dbs/id 1 ::dbs/start-time 10 ::dbs/end-time 15}
                    {::dbs/id 2 ::dbs/start-time 10 ::dbs/end-time 15}
                    {::dbs/id 3 ::dbs/start-time 15 ::dbs/end-time 20}]]
      (is (= (get-double-booked-events events-2)
             '([1 2] [1 3] [2 3])))))

  (testing "No double-booked events"
    (let [events-3 [{::dbs/id 1 ::dbs/start-time 1 ::dbs/end-time 5}
                    {::dbs/id 3 ::dbs/start-time 6 ::dbs/end-time 10}
                    {::dbs/id 2 ::dbs/start-time 11 ::dbs/end-time 15}]]
      (is (empty? (get-double-booked-events events-3))))))

(deftest test-double-booked-n²
  (testing "Double-booked events - worse case scenario"
    (let [events [{::dbs/id 1 ::dbs/start-time 0 ::dbs/end-time 10}
                  {::dbs/id 2 ::dbs/start-time 0 ::dbs/end-time 9}
                  {::dbs/id 3 ::dbs/start-time 1 ::dbs/end-time 8}
                  {::dbs/id 4 ::dbs/start-time 2 ::dbs/end-time 7}
                  {::dbs/id 5 ::dbs/start-time 4 ::dbs/end-time 6}]]
      (is (= (set (get-double-booked-events events))
             (set '([1 2] [1 3] [1 4] [1 5] [2 3] [2 4] [2 5] [3 4] [3 5] [4 5])))))))

(deftest check-if-simplified-function
  (testing "Verify that simplified function returns the same result as the 'main' one"
    (let [test-data (gen/generate (s/gen (s/and ::dbs/events
                                                #(= (count %) 10))))]
      (is (= (set (simpler-solution test-data))
             (set (get-double-booked-events test-data)))))))
