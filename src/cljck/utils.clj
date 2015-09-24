(ns cljck.utils)

(defn multi-functions
  "Returns a sorted list of the functions contained in the multimethod."
  [multimethod]
  (->> multimethod
       (.getMethodTable)
       (map first)
       (remove (partial = :default))
       (sort)))
