(ns cljck.timing
  (:require [cljck.io :refer [click]]
            [clojure.core.async :refer [<! >! chan go-loop timeout]]))

(def click-state
  "This is the state of the clicking and controls such things as clicks per
  second and the various open channels."
  (atom {:active false
         :bucket (chan)
         :cps 1}))

(defn set-clicks-per-second!
  "Starts clicking the left mouse button n times per second. Will stop
  clicking if n is zero."
  [n]
  {:pre [(number? n)
         (<= 0 n 100)]}
  (if (pos? n)
    (do
      (swap! click-state assoc :cps n)
      (swap! click-state assoc :active true))
    (swap! click-state assoc :active false)))

;; Keep adding tokens to the 'click bucket' when actively clicking.
(go-loop []
  (when (:active @click-state)
    (>! (:bucket @click-state) :click))
  (<! (timeout (/ 1000 (:cps @click-state))))
  (recur))

;; Click as soon as a token is added to the 'click bucket'.
(go-loop []
  (<! (:bucket @click-state))
  (click :left)
  (recur))
