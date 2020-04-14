(ns lwjgl-test.core
  (:require [lwjgl-test.init :as init]
            [lwjgl-test.db :as db :refer [globals]])
  (:import (org.lwjgl.opengl GL11)))

(defn draw []
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