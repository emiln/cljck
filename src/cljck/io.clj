(ns cljck.io
  (:require [clojure.core.async :as async])
  (:import  [java.awt Robot]
            [java.awt.event InputEvent]))
(def button-map
  "A mapping from Clojure keywords to more long-winded Java enums."
  {:left   InputEvent/BUTTON1_DOWN_MASK
   :middle InputEvent/BUTTON2_DOWN_MASK
   :right  InputEvent/BUTTON3_DOWN_MASK})

(def robot
  "You need a robot to do stuff with the mouse. This is that robot."
  (Robot.))


(defn click [k]
  "Simulates a mouse click of the key denoted by the keyword k in the
  button-map. A click is really a press and a release in succession."
  (let [i (get button-map k InputEvent/BUTTON1_DOWN_MASK)]
    (doto robot
      (.mousePress i)
      (.mouseRelease i))))
