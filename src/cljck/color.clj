(ns cljck.color
  (:import [java.awt.image BufferedImage Raster]
           [java.io File]
           [javax.imageio ImageIO]))

(defn image->pixels
  "Converts a BufferedImage into a flat sequence of pixels. A pixel is really
  just an int array with 3 elements.

  The 'dummy' is needed for confusing Java reasons."
  [^BufferedImage image]
  (let [dummy (int-array 3)
        raster (.getData image)]
    (for [^int x (range (.getWidth image))
          ^int y (range (.getHeight image))]
      (.getPixel raster x y dummy))))

(defn update-buckets
  "Takes an integer between 0 and 255 and updates the corresponding bucket.
  Updating a bucket is done by increasing its value by 1. The 'corresponding
  bucket' is as follows:

   0 to 31 -> bucket 0
  31 to 63 -> bucket 1
  64 to 95 -> bucket 2
  etc."
  [buckets value]
  (let [index (quot value 32)]
    (update buckets index inc)))

(defn update-rgb-buckets
  "Takes three colors of eight buckets and a pixel and updates all the buckets.
  See 'update-buckets' for details about the updating. The input to this
  function is something like:
  [reds greens blues] = [[0 0 0 0 0 0 0 0] [0 0 0 0 0 0 0 0] [0 0 0 0 0 0 0 0]]
     [red green blue] = [250 127 18]
  
  The result would increment the buckets as follows:
  [[0 0 0 0 0 0 0 1] [0 0 0 1 0 0 0 0] [1 0 0 0 0 0 0 0]]"
  [[reds greens blues] [red green blue]]
  [(update-buckets reds   red)
   (update-buckets greens green)
   (update-buckets blues  blue)])

(defn normalize-bucket
  "Takes a bucket count, divides it by the total number of pixels and multiplies
  by 255. This ensures all buckets are representable as 2 hexadecimal values and
  lie in the inclusive range [0 255]."
  [bucket total]
  (-> bucket
      (/ total)
      (* 255)
      double
      (Math/round)
      int))

(defn histogram
  "Constructs a histogram of a given BufferedImage, which is a measure of where
  in the color spectrum the pixels comprising the image fall. This is an image
  fingerprint of sorts and can be used to give an indication of image
  similarity."
  [^BufferedImage image]
  (let [zeros  [0 0 0 0 0 0 0 0]
        pixels (image->pixels image)
        total  (* (.getWidth image) (.getHeight image))]
    (for [color (reduce update-rgb-buckets [zeros zeros zeros] pixels)]
      (for [bucket color]
        (normalize-bucket bucket total)))))

(defn histogram-diff
  "Compares two histograms and returns a value between 0 (no diff at all) and 1
  (as different as possible) representing the degree of difference between
  them."
  [[red-a green-a blue-a] [red-b green-b blue-b]]
  (letfn [(abs [a] (if (pos? a) a (- a)))
          (diff [a b] (abs (- a b)))]
    (let [diff-red   (reduce + (map diff   red-a   red-b))
          diff-green (reduce + (map diff green-a green-b))
          diff-blue  (reduce + (map diff  blue-a  blue-b))]
      (/ (+ diff-red diff-green diff-blue) 255 3))))
