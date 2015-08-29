(ns cljck.io
  (:require [clojure.core.async :refer [<! <!! chan go-loop timeout]])
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

(defn move-to [x y]
  "Moves the mouse cursor to the absolute position [x y] on the screen."
  (.mouseMove robot x y))

(def event-channel (chan))

(defmulti process-event
  "Processes an event pushed onto the event channel. As events can take on
  open-ended shapes as Cljck is extended, this is a multi method free to be
  supplied with additional implementations.

  The argument is expected to be a vector like [:move-to 100 100] or [:click].
  Dispatches on first."
  first)

(defmethod process-event :click
  [_]
  (click :left))

(defmethod process-event :move-to
  [[_ x y]]
  (move-to x y))

(defmethod process-event :repeat
  [[_ n & commands]]
  (dotimes [_ n]
    (doseq [command commands]
      (process-event command))))

(defmethod process-event :repeatedly
  [[_ & commands]]
  (go-loop []
    (doseq [command commands]
      (process-event command))
    (recur)))

(defmethod process-event :wait
  [[_ miliseconds]]
  (<!! (timeout miliseconds)))

;; Process events as they arrive on the event channel.
(go-loop []
  (process-event (<! event-channel))
  (recur))
