(ns cljck.io
  (:gen-class)
  (:require
   [cljck.io
    [keyboard :refer [press]]
    [mouse :refer [click move-to]]]
   [clojure.core.async :refer [<! <!! chan go-loop timeout]]
   [clojure.edn :as edn])
  (:import
   [java.util.logging Level Logger]
   [org.jnativehook GlobalScreen]
   [org.jnativehook.keyboard NativeKeyEvent NativeKeyListener]))

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

(defmethod process-event :press
  [[_ key-string]]
  (press key-string))

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

(defmethod process-event :wait
  [[_ miliseconds]]
  (<!! (timeout miliseconds)))

(def
  ^{:arglists '([file-name])}
  process-file
  "Takes a file name, reads the content of the file, parses it as EDN, and
  attempts to process it as a Cljck command."
  (comp process-event edn/read-string slurp))

(defn -main
  [file-name]
  (doto (Logger/getLogger "org.jnativehook")
    (.setLevel Level/OFF))
  
  (GlobalScreen/registerNativeHook)
  
  (GlobalScreen/addNativeKeyListener
   (proxy [NativeKeyListener] []
     (nativeKeyTyped [event])
     (nativeKeyReleased [event])
     (nativeKeyPressed [event]
       (when (= (.getKeyCode event) NativeKeyEvent/VC_ESCAPE)
         (GlobalScreen/unregisterNativeHook)
         (System/exit 1337)))))
  
  (go-loop []
    (process-event (<! event-channel))
    (recur))

  (process-file file-name))
