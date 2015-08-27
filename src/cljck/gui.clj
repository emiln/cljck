(ns cljck.gui
  (:import [java.awt AWTEvent Toolkit]
           [java.awt.event AWTEventListener])
  (:require [cljck.timing :refer [set-clicks-per-second!]]
   [seesaw.core :as s]
            [seesaw.bind :as b]))

(s/native!)

(defn- make-frame
  []
  (let [cps    (s/spinner
                :model (s/spinner-model 5 :from 0 :to 100 :by 1))
        start  (s/button :text "Start")
        stop   (s/button :text "Stop")
        grid   (s/grid-panel
                :rows 1 :columns 3 :items [cps start stop])
        window (s/frame
                :title "Cljck" :minimum-size [300 :by 100] :content grid)]
    (s/listen
     start
     :mouse-clicked (fn [_]
                      (set-clicks-per-second! 5)))
    (s/listen
     stop
     :mouse-clicked (fn [_]
                      (set-clicks-per-second! 0)))
    (-> window s/pack! s/show! (.setAlwaysOnTop true))))
