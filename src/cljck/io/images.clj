(ns cljck.io.images
  (:import
   [java.io File]
   [javax.imageio ImageIO]))

(defn buffered-image
  "Takes a path to an image file, which is assumed available on the class path,
  and constructs and returns a BufferedImage from it."
  [path]
  (ImageIO/read (File. ^String path)))
