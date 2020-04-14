(ns lwjgl-test.core
  (:require [lwjgl-test.init :as init]
            [lwjgl-test.logo :as logo :refer [setdir setscale setsat setps setbri setalpha defproc rpt run reset
                                              ;t 
                                              bk fd lt rt pd pu setxy
                                              ;pc fc
                                              ]]
            [lwjgl-test.db :as db :refer [globals]])
  (:import CosSineTable
           (org.lwjgl BufferUtils)
           (org.lwjgl.opengl GL11)))

(defn draw2 []
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

(defproc heart []
  pd
  lt 30

  rpt 4 [fd 1 rt 10]
  rpt 10 [fd 0.52 rt 20]
  lt 160
  rpt 10 [fd 0.52 rt 20]
  lt 10
  rpt 4 [fd 1 rt 10]
  rt 50
  pu)

(defproc slope []
  lt -70 bk 8 pd fd 16 rt 120 fd 16 pu 40)

(def state (atom {:bg nil
                  :player-x -300
                  :player-y 220
                  :dir 259
                  :hit-table (logo/hit-table 20)
                  :speed 2}))

(defn draw-player []
  (let [{:keys [player-x player-y]} @state]
    
    (setxy player-x player-y)
    (pd)
    (dotimes [_ 10]
      (fd 1)
      (bk 1)
      (rt 36))
    (pu)))

(defn update-state []
  (swap! state (fn [{:keys [speed dir] :as state}]
                 (-> state
                     (update :player-x + (* speed (CosSineTable/cos dir)))
                     (update :player-y + (* speed (CosSineTable/sin dir)))))))


(def pixels (BufferUtils/createByteBuffer (* 41 41 4)))

(defn get-pixel [x y]
  (let [idx (int (+ (* 4 x) (* y (* 4 40))))]
    ;(println "idx " idx)
    (.get pixels idx)))

(defproc border []
  setxy -350 250
  setdir 0
  pd
  setscale 1
  fd 700 rt 90 fd 500 rt 90 fd 700 rt 90 fd 500
  setscale 20
  pu)

(defn draw [{:keys [angle width height]}]
  (reset)
  ;(lt angle)
  (setxy 0 0)
  (dotimes [_ 2]
      (lt 7.3)
      (heart))
  (lt 180)
  (setxy 0 0)
  (border)
  (setxy 0 0)
  (setdir 0)
  (heart)
  (update-state)
  (let [{:keys [player-x player-y]} @state]
    (GL11/glReadPixels (int (+ (/ width 2) player-x -20)) 
                       (int (+ (/ height 2) player-y -20)) 
                       40 40 GL11/GL_RGBA, GL11/GL_UNSIGNED_BYTE
                       pixels))
  #_(let [p (get-pixel 20 20)]
      (when (not (zero? p))
        (println p)))
  (let [{:keys [player-x player-y bg hit-table dir]} @state]
    (when-not bg
      (swap! state assoc :bg
             (get-pixel 20 20)))
    
    (let [bg (:bg @state)
          {:keys [sum-cx sum-cy cnt]}
          (reduce (fn [m {:keys [dx dy cx cy]}]
                    (let [
                          test-pixel (get-pixel 
                                      (+ 20 dx)
                                      (+ 20 dy))
                          ]
                      (if (and test-pixel (not= test-pixel bg))
                        (do ;(println dx dy cx cy)
                          (-> m
                              (update :sum-cx + cx)
                              (update :sum-cy + cy)
                              (update :cnt inc)))
                        m)))
                  {:sum-cx 0
                   :sum-cy 0
                   :cnt 0}
                  hit-table)
          
          angle (* 180 (/ (Math/atan2 sum-cy sum-cx) logo/PI))
          new-dir (mod (- (* 2 (+ angle 90))
                          dir) 360)]
      (draw-player)
      (when (> cnt 4)
        (println angle new-dir)
        (swap! state assoc :dir new-dir)
        (update-state)
        )))
  )


(defn update-globals []
  (let [{:keys [width height angle last-time]} @globals
        cur-time (System/currentTimeMillis)
        delta-time (- cur-time last-time)
        next-angle (+ (* delta-time 0.05) angle)
        next-angle (if (>= next-angle 360.0)
                     (- next-angle 360.0)
                     next-angle)]
    (swap! globals assoc
           :angle next-angle
           :last-time cur-time)))

(defn main []
  (init/run {:draw #'draw
             :update-globals #'update-globals}))