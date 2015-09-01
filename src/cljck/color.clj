(ns cljck.color
  (:import [java.awt.image BufferedImage Raster]
           [java.io File]
           [javax.imageio ImageIO]))

(defn buffered-image
  [path]
  (ImageIO/read (File. ^String path)))

(defn image->pixels
  [^BufferedImage image]
  (let [dummy (int-array 3)
        raster (.getData image)]
    (for [^int x (range (.getWidth image))
          ^int y (range (.getHeight image))]
      (.getPixel raster x y dummy))))

(defn update-buckets
  [buckets value]
  (let [index (quot value 32)]
    (update buckets index inc)))

(defn update-rgb-buckets
  [[reds greens blues] [red green blue]]
  [(update-buckets reds   red)
   (update-buckets greens green)
   (update-buckets blues  blue)])

(defn normalize-bucket
  [bucket total]
  (-> bucket
      (/ total)
      (* 256)
      double
      (Math/round)
      int))

(defn histogram
  [^BufferedImage image]
  (let [zeros  [0 0 0 0 0 0 0 0]
        pixels (image->pixels image)
        total  (* (.getWidth image) (.getHeight image))]
    (for [color (reduce update-rgb-buckets [zeros zeros zeros] pixels)]
      (for [bucket color]
        (normalize-bucket bucket total)))))
