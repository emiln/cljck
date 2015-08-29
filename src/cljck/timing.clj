(ns cljck.timing
  (:require [cljck.io :refer [event-channel]]
            [clojure.core.async :refer [<! >! chan go-loop timeout]]))

(def click-state
  "This is the state of the clicking and controls such things as clicks per
  second and the various open channels."
  (atom {:active false
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

;; Keep adding click commands to the event queue while active.
(go-loop []
  (when (:active @click-state)
    (>! event-channel [:click]))
  (<! (timeout (/ 1000 (:cps @click-state))))
  (recur))
