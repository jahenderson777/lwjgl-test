(ns lwjgl-test.core
  (:require [lwjgl-test.init :as init]
            [lwjgl-test.logo :as logo :refer [setdir setscale setsat setps setbri setalpha defproc rpt run reset
                                              ;t 
                                              bk fd lt rt pd pu setxy pdtri
                                              ;pc fc
                                              ]]
            [lwjgl-test.db :as db :refer [state]])
  (:import CosSineTable HitTester Util
           (org.lwjgl BufferUtils)
           (org.lwjgl.opengl GL11)))


#_(defn draw2 []
  (let [{:keys [width height angle]} @globals
        w2 (/ width 2.0)
        h2 (/ height 2.0)]
    (GL11/glClear (bit-or GL11/GL_COLOR_BUFFER_BIT  GL11/GL_DEPTH_BUFFER_BIT))
    (GL11/glLoadIdentity)
    (GL11/glTranslatef w2 h2 0)
    (GL11/glScalef 2 2 1)
    (dotimes [i 3]
      (GL11/glRotatef (+ (* i 1) angle) 0 0 1)
      (GL11/glBegin GL11/GL_LINE_LOOP)
      (GL11/glColor3f 1.0 0.0 0.0)
      (GL11/glVertex2i 100 0)
      (GL11/glColor3f 0.0 1.0 0.0)
      (GL11/glVertex2i -50 86.6)
      (GL11/glColor3f 0.0 0.0 1.0)
      (GL11/glVertex2i -50 -86.6)
      (GL11/glEnd))))

(defproc heart [size]
  GL11/glColor3f 0.9 0.6 0.6
  pdtri
  lt 30
  rpt 4 [fd (* size 1) rt 10]
  rpt 10 [fd (* size 0.52) rt 20]
  lt 160
  rpt 10 [fd (* size 0.52) rt 20]
  lt 10
  rpt 4 [fd (* size 1) rt 10]
  rt 50
  pu)

(defproc slope []
  lt -70 bk 8 pd fd 16 rt 120 fd 16 pu 40)

(defproc draw-player [player-x player-y]
  GL11/glColor3f 0.0 1.0 0.0
  setxy player-x player-y
  pd
  rpt 50 [fd 1 bk 1 rt 13]
  pu)

(defproc border []
  setxy -350 250
  setdir 0
  pd
  setscale 1
  
  fd 700 rt 90 fd 500 rt 90 fd 700 rt 90 fd 500
  setscale 20
  pu)

(def hit-tester (HitTester. 20))

(defn hit-test [{:keys [width height player-x player-y bg dir]}]
  (GL11/glReadPixels (int (+ (/ width 2) player-x -20))
                     (int (+ (/ height 2) player-y -20))
                     40 40 GL11/GL_RGB, GL11/GL_UNSIGNED_BYTE
                     (.-pixels hit-tester))
  (when-not bg
    (.setBackground hit-tester)
    (swap! state assoc :bg
           (.getPixel hit-tester 20 20)))

  (let [new-dir (.findHit hit-tester dir)]
    
    (when new-dir
      (swap! state assoc :dir new-dir))))


(defn draw [{:keys [angle player-x player-y] :as state}]
  (reset)
  (border)
  (setxy 0 0)
  (run GL11/glColor3f 0.0 0.5 0.5 pd lt 45 fd 10 bk 10 rt 90 fd 10 bk 20 fd 10 lt 90 bk 10 fd 10 pu)
  ;(setdir angle)
  (heart (/ angle  180))
  (hit-test state)
  (draw-player player-x player-y))

(defn update-state [{:keys [speed dir angle last-time] :as state}]
  (let [cur-time (System/currentTimeMillis)
        delta-time (- cur-time last-time)
        next-angle (+ (* delta-time 0.05) angle)
        next-angle (if (>= next-angle 360.0)
                     (- next-angle 360.0)
                     next-angle)]
    (-> state
        (assoc :angle next-angle
               :last-time cur-time)
        (update :player-x + (* speed (CosSineTable/cos dir)))
        (update :player-y + (* speed (CosSineTable/sin dir))))))

(defn main []
  (init/run {:update-state #'update-state
             :draw #'draw}))