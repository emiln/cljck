(ns cljck.io
  (:gen-class)
  (:require
   [clojure.core.async :refer [<! <!! chan go-loop timeout]]
   [clojure.edn :as edn]
   [clojure.string :refer [upper-case]])
  (:import
   [javax.imageio ImageIO]
   [java.awt Robot]
   [java.awt.event KeyEvent InputEvent]
   [java.io File]
   [java.util.logging Level Logger]
   [org.jnativehook GlobalScreen]
   [org.jnativehook.keyboard NativeKeyEvent NativeKeyListener]))

(doto (Logger/getLogger "org.jnativehook")
  (.setLevel Level/OFF))

(GlobalScreen/registerNativeHook)

(GlobalScreen/addNativeKeyListener
 (proxy [NativeKeyListener] []
   (nativeKeyTyped [event])
   (nativeKeyReleased [event])
   (nativeKeyPressed [event]
     (when (= (.getKeyCode event) NativeKeyEvent/VC_ESCAPE)
       (System/exit 1337)))))

(defn buffered-image
  "Takes a path to an image file, which is assumed available on the class path,
  and constructs and returns a BufferedImage from it."
  [path]
  (ImageIO/read (File. ^String path)))

(def button-map
  "A mapping from Clojure keywords to more long-winded Java enums."
  {:left   InputEvent/BUTTON1_DOWN_MASK
   :middle InputEvent/BUTTON2_DOWN_MASK
   :right  InputEvent/BUTTON3_DOWN_MASK})

(def robot
  "You need a robot to do stuff with the mouse. This is that robot."
  (Robot.))

(defn click
  "Simulates a mouse click of the key denoted by the keyword k in the
  button-map. A click is really a press and a release in succession."
  [k]
  (let [i (get button-map k InputEvent/BUTTON1_DOWN_MASK)]
    (doto robot
      (.mousePress i)
      (.mouseRelease i))))

(defn str->key
  "Takes a string like 'A', 'B', 'ESCAPE', etc. and returns the corresponding
  KeyEvent/VK_{string} constant."
  [string]
  (eval `(. KeyEvent ~(symbol (str "VK_" string)))))

(defn press
  "Simulates typing the given key. Note that the keyword should comprise a
  single key to be inserted as the end of InputKey/VK_{keyword}. The full list
  is available here:

  http://docs.oracle.com/javase/7/docs/api/java/awt/event/KeyEvent.html

  You can only use the keys beginning with VK_.

  Examples: :5 :a :colon :f10"
  [key-keyword]
  (let [code (str->key (upper-case (name key-keyword)))]
    (doto robot
      (.keyPress code)
      (.keyRelease code))))

(defn move-to
  "Moves the mouse cursor to the absolute position [x y] on the screen."
  [x y]
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
  (loop []
    (doseq [command commands]
      (process-event command))
    (recur)))

(defmethod process-event :type
  [[_ key-keyword]]
  (press key-keyword))

(defmethod process-event :wait
  [[_ miliseconds]]
  (<!! (timeout miliseconds)))

;; Process events as they arrive on the event channel.
(go-loop []
  (process-event (<! event-channel))
  (recur))

(def
  ^{:arglists '([file-name])}
  process-file
  "Takes a file name, reads the content of the file, parses it as EDN, and
  attempts to process it as a Cljck command."
  (comp process-event edn/read-string slurp))

(defn -main
  [file-name]
  (process-file file-name))
