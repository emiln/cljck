(ns cljck.io.images
  (:require [cljck.io.sync :refer [robot]])
  (:import
   [java.awt Rectangle Toolkit]
   [java.awt.image BufferedImage]
   [java.io File]
   [javax.imageio ImageIO]))

(defn screen-resolution
  "Returns the current width and height of the screen."
  []
  ((juxt (memfn getWidth) (memfn getHeight))
   (.. Toolkit getDefaultToolkit getScreenSize)))

(defn buffered-image
  "Takes a path to an image file, which is assumed available on the class path,
  and constructs and returns a BufferedImage from it."
  [path]
  (ImageIO/read (File. ^String path)))

(defn fetch-screen
  "Returns a BufferedImage of the pixels currently on the screen. Optionally
  takes a rectangle to limit the fetching to."
  ([]
   (apply fetch-screen 0 0 (screen-resolution)))
  ([x y width height]
   (.createScreenCapture robot (Rectangle. x y width height))))
