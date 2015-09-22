(set-env!
  :dependencies
  '[[com.1stleg/jnativehook "2.0.2"]
    [org.clojure/clojure "1.7.0"]
    [org.clojure/core.async "0.1.346.0-17112a-alpha"]
    [seesaw "1.4.5"]]
  :source-paths #{"src"})

(deftask build
  "Builds an uberjar capable of processing files passed to it on the command
  line."
  []
  (comp
   (aot :all true)
   (pom :project 'cljck
        :version "0.6.0")
   (uber)
   (jar :main 'cljck.io)))
