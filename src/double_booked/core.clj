(ns double-booked.core
  (:require [clojure.spec.alpha :as s]
            [double-booked.spec :as dbs]))

;; Here’s the problem:
;;
;; Double Booked
;; When maintaining a calendar of events, it is important to know if an event overlaps with another event.
;;
;; Given a sequence of events, each having a start and end time, write a program that will return the sequence of all pairs of overlapping events.

;; --------------------------------
;; Assumptions:
;; 1) I've assumed that this is not code golf, and shortest solution is not the main goal.
;; based on (1) I've aimed for optimal solution with expected O(n logn) time.
;; However easier to read, O(n²) solution I've also written (as 'simpler-solution)
;; 2) events with same start-time and end-time *are* overlapping
;; 3) for simplicity I deal with times as ints, not time objects.
;; 4) if events are going to be constantly added better solution would be to keep them in interval tree
;;   (eg. read-back or AVL based) - requires just O(log n) time to verify each newly added event.

(defn simpler-solution [events]
  "Simpler approach - Sort events by :end-time (in case we deal with events of duration 0) and :start-time.
  Go over sorted events and accumulate all which have :start-time before :end-time of current-event.
  Check conflicting times only for remaining sequence."
  {:pre [(s/valid? ::dbs/events events)]}
  (let [sorted-events (sort-by ::dbs/start-time (sort-by ::dbs/end-time events))
        events-groups (take-while #(not (empty? %)) (iterate rest sorted-events))]
    (mapcat (fn [[current & rest]]
              (reduce (fn [acc event]
                        (if (> (::dbs/start-time event) (::dbs/end-time current))
                          (reduced acc)
                          (conj acc [(::dbs/id current) (::dbs/id event)]))) [] rest))
            events-groups)))

;; Few events which I've used while working with REPL
(def events [{::dbs/id 1 ::dbs/start-time 10 ::dbs/end-time 15}
             {::dbs/id 2 ::dbs/start-time 10 ::dbs/end-time 12}
             {::dbs/id 3 ::dbs/start-time 0 ::dbs/end-time 3}
             {::dbs/id 4 ::dbs/start-time 5 ::dbs/end-time 9}
             {::dbs/id 5 ::dbs/start-time 0 ::dbs/end-time 11}])

(defn split-event [event]
  "Split time-event into 2 events, with type :start, and :end."
  {:pre [(s/valid? ::dbs/event event)]}
  [{::dbs/id (::dbs/id event) ::dbs/type ::dbs/start ::dbs/time (::dbs/start-time event)}
   {::dbs/id (::dbs/id event) ::dbs/type ::dbs/end ::dbs/time (::dbs/end-time event)}])

(defn split-events [events]
  {:pre [(s/valid? ::dbs/events events)]}
  (mapcat split-event events))

(defn sort-split-events
  "Sort them by ::start first, so events which start/end with the same time are in conflict"
  [events]
  (sort-by ::dbs/time (sort (fn [x y] (if (= ::dbs/start (::dbs/type y)) false true)) events)))

(defn get-double-booked-events [events]
  "Main function.
  1) Splits and sorts events
  2) Collect each started events into events-in-progress
  3) For each started event collect pair of given event with other started events (as requested)
  4) End of event removes it from events-in-progress
  3) Finally returns collected pairs of event-ids"
  {:pre [(s/valid? ::dbs/events events)]}
  (loop [preprocessed-events (sort-split-events (split-events events))
         events-in-progress (hash-set)
         result []]
    (let [event (first preprocessed-events)
          event-id (::dbs/id event)]
      (cond
        ;; no more events - return doubled-booked-pairs-ids
        (nil? event) result
        ;; start of event - add it to events-in-progress, add pairs between event and events in progress to result
        (= ::dbs/start (::dbs/type event)) (recur (next preprocessed-events)
                                                  (conj events-in-progress event-id)
                                                  (concat result (map #(vector % event-id) events-in-progress)))
        ;; end of event - remove it from events-in-progress set.
        :else (recur (next preprocessed-events) (disj events-in-progress event-id) result)))))

(defn pretty-print-double-booked-events
  "Calls get-double-booked-events, however pretty prints event id pair as event"
  [events]
  {:pre [(s/valid? ::dbs/events events)]}
  (let [events-ht (reduce (fn [acc v]
                            (merge acc {(::dbs/id v) v}))
                          {} events)
        pairs (get-double-booked-events events)]
    (doseq [[a b] pairs]
      (print (format "%s & %s\n" (get events-ht a) (get events-ht b))))))

(defn -main
  "Run with default events seq"
  []
  (print "Events: \n")
  (doseq [e events]
    (print e "\n"))
  (print "Result: \n")
  (pretty-print-double-booked-events events))
