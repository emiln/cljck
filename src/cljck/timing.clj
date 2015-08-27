(ns cljck.timing
  (:require [cljck.io :refer [click]]
            [clojure.core.async :refer [<!! go-loop timeout]]))

(defn timed-clicking
  "Starts clicking the given button n times per second.
  Button should be one of: :left :middle :right"
  [n button]
  (let [interval (/ 1000 n)]
    (go-loop []
      (<!! (timeout interval))
      (click button)
      (recur))))
