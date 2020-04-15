(ns lwjgl-test.logo
  (:require [lwjgl-test.init :as init]
            [lwjgl-test.db :as db :refer [state]])
  (:import CosSineTable
           (org.lwjgl.opengl GL11)))

(def PI (Math/PI))
(def HALF-PI (/ (Math/PI) 2))

(def dir-offset 0)
(def dir (volatile! 0))
(def x (volatile! 600))
(def y (volatile! 600))
(def scale (volatile! 20))

(def hue (volatile! 0))
(def sat (volatile! 0))
(def bri (volatile! 0))
(def alpha (volatile! 0))

(def pen-down (volatile! false))
(def shape-x (volatile! 0))
(def shape-y (volatile! 0))
(def pen-size (volatile! 1))

(defn setpc [c]
  (vreset! hue c))

(defn setscale [s]
  (vreset! scale s))

(defn setps [s]
  (vreset! pen-size s))

(defn setalpha [a]
  (vreset! alpha a))

(defn setsat [s]
  (vreset! sat s))

(defn setbri [b]
  (vreset! bri b))

(defn setdir [d]
  (vreset! dir d))

(defn setxy [nx ny]
  (vreset! x nx)
  (vreset! y ny)
  (vreset! shape-x nx)
  (vreset! shape-y ny))

(defn reset []
  (let [{:keys [width height]} @state]
    (GL11/glClear (bit-or GL11/GL_COLOR_BUFFER_BIT  GL11/GL_DEPTH_BUFFER_BIT))
    (GL11/glLoadIdentity)
    (GL11/glColor3f 1.0 1.0 1.0)
    (vreset! x (/ width 2))
    (vreset! y (/ height 2))
    (GL11/glTranslatef @x @y 0)
    (vreset! shape-x @x)
    (vreset! shape-y @y)
    (vreset! dir 0)
    (vreset! scale 20)
    (vreset! pen-size 1)
    (vreset! hue 0)
    (vreset! sat 70)
    (vreset! bri 80)
    (vreset! alpha 10)))

(defn fd [l]
  (vswap! x + (* @scale l (CosSineTable/cos (+ @dir dir-offset))))
  (vswap! y + (* @scale l (CosSineTable/sin (+ @dir dir-offset))))
  (GL11/glVertex2i @x @y))

#_(defn fc [l cdir cl cdir2 cl2]
  (let [new-x (+ @x (* @scale l (CosSineTable/cos (+ @dir dir-offset))))
        new-y (+ @y (* @scale l (CosSineTable/sin (+ @dir dir-offset))))

        cx (+ @x (* @scale cl (CosSineTable/cos (q/radians (+ cdir dir-offset)))))
        cy (+ @y (* @scale cl (CosSineTable/sin (q/radians (+ cdir dir-offset)))))

        cx2 (+ new-x (* @scale cl2 (q/cos (q/radians (+ cdir2 dir-offset)))))
        cy2 (+ new-y (* @scale cl2 (q/sin (q/radians (+ cdir2 dir-offset)))))]
    (q/bezier-vertex cx cy cx2 cy2 new-x new-y)
    (vreset! x new-x)
    (vreset! y new-y)))

(defn bk [l]
  (fd (* -1 l)))

(def rpt identity)

(defn lt [a]
  (vswap! dir + a))

(defn rt [a]
  (vswap! dir - a))

(defn pu []
  ;(q/no-fill)
  ;(q/end-shape)
  (GL11/glEnd)
  (vreset! pen-down false))

#_(defn pc [c]
  (when @pen-down
    (q/vertex @shape-x @shape-y)
    (when (or (not= @x @shape-x)
              (not= @y @shape-y))
      (vreset! x @shape-x)
      (vreset! y @shape-y))
    (q/fill c @sat @bri @alpha)
    (q/end-shape)
    (vreset! pen-down false)))

(defn pd [& [pen-col]]
  (when-not @pen-down
    (GL11/glBegin ;GL11/GL_LINE_LOOP
                  GL11/GL_LINE_STRIP)
    #_(if (pos? @pen-size)
      (q/stroke (if pen-col
                  pen-col
                  @hue) 100 80)
      (q/no-stroke))
    (vreset! shape-x @x)
    (vreset! shape-y @y)
    (GL11/glVertex2i @shape-x @shape-y)
    (vreset! pen-down true)))

#_(defn t []
  (/ (q/millis) 1000.0))

(defn chop-up-cmds [cmds local-vars]
  (loop [split-cmds []
         cmd [(first cmds)]
         cmds (rest cmds)]
    (if (or (not (seq cmd))
            (not (seq cmds)))
      (map seq (if (seq cmd)
                 (conj split-cmds cmd)
                 split-cmds))

      (if (= (first cmd) 'rpt)
        (let [[iterations v next-cmd & rest-cmds] cmds]
          (recur (conj split-cmds (conj
                                   (chop-up-cmds v local-vars)
                                   ['i iterations]
                                   'dotimes))
                 (if next-cmd [next-cmd] [])
                 rest-cmds))
        (let [next-cmd (first cmds)]
          (if (or (fn? next-cmd)
                  (and (symbol? next-cmd)
                       (not (contains? (into #{} local-vars) next-cmd))))
            (recur (conj split-cmds cmd)
                   [next-cmd]
                   (rest cmds))
            (recur split-cmds
                   (conj cmd next-cmd)
                   (rest cmds))))))))

(defmacro run [& cmds]
  `(do ~@(chop-up-cmds cmds [])))


(defmacro defproc [name args & cmds]
  `(defn ~name ~args
     (do ~@(chop-up-cmds cmds args))))
