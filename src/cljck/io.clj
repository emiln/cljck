(ns cljck.io
  (:require
   [cljck.io
    [keyboard :refer [press release]]
    [mouse :refer [click move-to mouse-pointer scroll-down scroll-up]]]
   [cljck.utils :refer [multi-functions]]
   [clojure.core.async :refer [<! <!! chan go go-loop timeout]]
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

  The argument is expected to be a seq where the first element is either a
  keyword or a symbol. Examples include: [:click] (move-to 100 100)."
  (comp name first))

(defmethod process-event :default
  [exp]
  (throw (IllegalArgumentException.
          (with-out-str
            (println "The following expression wasn't recognized:")
            (clojure.pprint/pprint exp)
            (println "No such command:" (first exp))
            (println "Valid commands:" (multi-functions process-event))
            (println "Check the API in the README for help.")))))

(defmethod process-event "click"
  [_]
  (click :left))

(defmethod process-event "holding"
  [[_ key-string & body]]
  (press key-string)
  (apply process-event body)
  (release key-string))

(defmethod process-event "if"
  [[_ condition then else]]
  (if (process-event condition)
    (process-event then)
    (process-event else)))

(defmethod process-event "move-to"
  [[_ x y]]
  (move-to x y))

(defmethod process-event "pointer-near"
  [[_ x y distance]]
  (let [[mouse-x mouse-y] (mouse-pointer)]
    (and (< (- x distance) mouse-x (+ x distance))
         (< (- y distance) mouse-y (+ y distance)))))

(defmethod process-event "press"
  [[_ key-string]]
  (press key-string)
  (release key-string))

(defmethod process-event "repeat"
  [[_ n & commands]]
  (dotimes [_ n]
    (doseq [command commands]
      (process-event command))))

(defmethod process-event "repeatedly"
  [[_ & commands]]
  (loop []
    (doseq [command commands]
      (process-event command))
    (recur)))

(defmethod process-event "scroll-down"
  [[_ & amount]]
  (apply scroll-down amount))

(defmethod process-event "scroll-up"
  [[_ & amount]]
  (apply scroll-up amount))

(defmethod process-event "wait"
  [[_ miliseconds]]
  (<!! (timeout miliseconds)))

(defmethod process-event "when"
  [[_ condition & body]]
  (when (process-event condition)
    (apply process-event body)))

(def
  ^{:arglists '([file-name])}
  process-file
  "Takes a file name, reads the content of the file, parses it as EDN, and
  attempts to process it as a Cljck command."
  (comp process-event edn/read-string slurp))

(defn -main
  [& file-names]
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

  (doseq [file-name file-names]
    (go (process-file file-name))))
