(ns cljck.io.mouse
  (:require [cljck.io.sync :refer [robot]])
  (:import [java.awt MouseInfo Point PointerInfo]
           [java.awt.event InputEvent]))

(def button-map
  "A mapping from Clojure keywords to more long-winded Java enums."
  {:left   InputEvent/BUTTON1_DOWN_MASK
   :middle InputEvent/BUTTON2_DOWN_MASK
   :right  InputEvent/BUTTON3_DOWN_MASK})

(defn scroll-down
  "Simulates scrolling the mouse wheel downwards."
  ([] (scroll-down 1))
  ([n] (.mouseWheel robot n)))

(defn scroll-up
  "Simulates scrolling the mouse wheel upwards."
  ([] (scroll-up 1))
  ([n] (.mouseWheel robot (- n))))

(defn click
  "Simulates a mouse click of the key denoted by the keyword k in the
  button-map. A click is really a press and a release in succession."
  [k]
  (let [i (get button-map k InputEvent/BUTTON1_DOWN_MASK)]
    (doto robot
      (.mousePress i)
      (.mouseRelease i))))

(defn move-to
  "Moves the mouse cursor to the absolute position [x y] on the screen."
  [x y]
  (.mouseMove robot x y))

(defn mouse-pointer
  "Returns the current absolute position of the mouse pointer."
  []
  (let [point (.. MouseInfo getPointerInfo getLocation)]
    [(.x point)
     (.y point)]))
