(ns dev
  (:require [clojure.java.io :as io]
            [badigeon.javac :as j]
            [clojure.tools.namespace.repl :refer [refresh refresh-all]])
  )

(def ^:dynamic *import-to-namespace* true)

(defn to-byte-array [^java.io.File x]
  (with-open [buffer (java.io.ByteArrayOutputStream.)]
    (io/copy x buffer)
    (.toByteArray buffer)))

(defn import-to-ns [class]
  (if *import-to-namespace*
    (try (.importClass @#'clojure.core/*ns* class)
         true
         (catch IllegalStateException e
           (println class " not interned to namespace: " (.getMessage e))
           false))
    false))

(defn reimport-from-file
  ([classname f]
   (.defineClass (clojure.lang.DynamicClassLoader.)
                 classname
                 (to-byte-array f)
                 nil)
   (println (format "'%s' imported from %s"  classname (.getPath f)))
   (let [class (Class/forName classname)
         in-ns (import-to-ns class)]
     [class in-ns])))

(defn reimport-path-from-dir
  [dir path]
  (println dir path)
  (let [classname (->> (clojure.string/split
                        (-> (re-find #"(.*).class" path)
                            second)
                        ;java.io.File/separator
                        #"\\"
                        )
                       (clojure.string/join "."))
        f (io/file (str dir java.io.File/separator path))]
    (reimport-from-file classname f)))

(defn reimport-class-from-dir
  [dir classname]
  (let [file-path (->> (clojure.string/split classname #"\\.")
                       (clojure.string/join java.io.File/separator))
        f (io/file (str dir java.io.File/separator file-path ".class"))]
    (reimport-from-file classname f)))

(defn reimport-reload
  [dir]
  (let [paths (->> (file-seq (io/file dir))
                   (map #(.getPath %))
                   (filter #(re-find #".class$" %))
                   (map #(subs % (inc (count dir)))))]
    (mapv #(reimport-path-from-dir dir %) paths)))

(defn javac []
  (println "Compiling Java")
  (j/javac "src" {:compile-path     "classes"
                  :compiler-options ["-cp" "src:classes" "-target" "11"
                                     "-source" "11" "-Xlint:-options"]})
  (println "Compilation Completed"))

(defn reload-java []
  (javac)
  (reimport-reload "classes")
  (refresh-all))

(defn -main []
  (javac))