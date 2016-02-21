(ns cljck.io.keyboard
  (:import [java.awt AWTKeyStroke]
           [java.awt.event InputEvent KeyEvent])
  (:require [cljck.io.sync :refer [robot]]))

(def bitmask-map
  {(bit-or InputEvent/ALT_DOWN_MASK InputEvent/ALT_MASK)
   KeyEvent/VK_ALT

   (bit-or InputEvent/CTRL_DOWN_MASK InputEvent/CTRL_MASK)
   KeyEvent/VK_CONTROL

   (bit-or InputEvent/SHIFT_DOWN_MASK InputEvent/SHIFT_MASK)
   KeyEvent/VK_SHIFT})

(defn str->key-stroke
  [string]
  ((juxt (memfn getKeyCode) (memfn getModifiers))
   (AWTKeyStroke/getAWTKeyStroke string)))

(defn bitmask->key-events
  [bitmask]
  (->>
   (filter
    (fn [mask] (= mask (bit-and mask bitmask)))
    (keys bitmask-map))
   (map (partial get bitmask-map))))

(defn key-string->key-seq
  [key-string]
  (let [[key-code mod-mask] (str->key-stroke key-string)
        mod-seq (vec (bitmask->key-events mod-mask))]
    (conj mod-seq key-code)))

(defn press
  [key-string]
  (doseq [k (key-string->key-seq key-string)]
    (.keyPress robot k)))

(defn release
  [key-string]
  (doseq [k (reverse (key-string->key-seq key-string))]
    (.keyRelease robot k)))
