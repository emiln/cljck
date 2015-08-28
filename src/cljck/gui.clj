(ns cljck.gui
  (:import
    [java.awt AWTEvent Toolkit]
    [java.awt.event AWTEventListener])
  (:require
    [cljck.timing
     :refer [set-clicks-per-second!]]
    [seesaw.core
     :refer [button config frame grid-panel listen native! pack! show! spinner
             spinner-model]]))

(native!)

(defn make-frame
  "Creates the Cljck window with all its bells and whistles."
  []
  (let [cps    (spinner :model (spinner-model 5 :from 0 :to 100 :by 1))
        start  (button :text "Start")
        stop   (button :text "Stop")
        grid   (grid-panel :rows 1 :columns 3 :items [cps start stop])
        window (frame :title "Cljck" :content grid :on-close :exit)]
    (listen
     start
     :mouse-clicked
     (fn [_]
       (set-clicks-per-second! (-> cps (config :model) (.getValue)))))
    (listen
     stop
     :mouse-clicked
     (fn [_]
       (set-clicks-per-second! 0)))
    (-> window pack! show! (.setAlwaysOnTop true))))
