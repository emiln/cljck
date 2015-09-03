(ns cljck.io.keyboard
  (:import [java.awt AWTKeyStroke]
           [java.awt.event InputEvent KeyEvent])
  (:require [cljck.io :refer [robot]]))

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

(defn press
  [key-string]
  (let [[key-code mod-mask] (str->key-stroke key-string)
        mod-seq (vec (bitmask->key-events mod-mask))
        key-seq (conj mod-seq key-code)]
    (doseq [k key-seq]
      (.keyPress robot k))
    (doseq [k (reverse key-seq)]
      (.keyRelease robot k))))
