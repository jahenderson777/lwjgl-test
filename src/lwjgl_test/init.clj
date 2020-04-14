(ns lwjgl-test.init
  (:require [lwjgl-test.db :as db :refer [globals]])
  (:import (org.lwjgl.opengl GL GL11)
           (org.lwjgl BufferUtils)
           (org.lwjgl.glfw GLFW GLFWErrorCallback GLFWKeyCallback)))

(defn init-window [width height title]
  (swap! globals assoc
         :width     width
         :height    height
         :title     title
         :last-time (System/currentTimeMillis))

  (swap! globals assoc
         :errorCallback (GLFWErrorCallback/createPrint System/err))
  (GLFW/glfwSetErrorCallback (:errorCallback @globals))
  (when-not (GLFW/glfwInit)
    (throw (IllegalStateException. "Unable to initialize GLFW")))

  (GLFW/glfwDefaultWindowHints)
  (GLFW/glfwWindowHint GLFW/GLFW_VISIBLE GLFW/GLFW_FALSE)
  (GLFW/glfwWindowHint GLFW/GLFW_RESIZABLE GLFW/GLFW_TRUE)
  (swap! globals assoc
         :window (GLFW/glfwCreateWindow width height title 0 0))
  (when (= (:window @globals) nil)
    (throw (RuntimeException. "Failed to create the GLFW window")))

  (swap! globals assoc
         :keyCallback
         (proxy [GLFWKeyCallback] []
           (invoke [window key scancode action mods]
             (when (and (= key GLFW/GLFW_KEY_ESCAPE)
                        (= action GLFW/GLFW_RELEASE))
               (GLFW/glfwSetWindowShouldClose (:window @globals) true)))))
  (GLFW/glfwSetKeyCallback (:window @globals) (:keyCallback @globals))

  (let [vidmode (GLFW/glfwGetVideoMode (GLFW/glfwGetPrimaryMonitor))]
    (GLFW/glfwSetWindowPos
     (:window @globals)
     (/ (- (.width vidmode) width) 2)
     (/ (- (.height vidmode) height) 2))
    (GLFW/glfwMakeContextCurrent (:window @globals))
    (GLFW/glfwSwapInterval 1)
    (GLFW/glfwShowWindow (:window @globals))))

(defn init-gl []
  (GL/createCapabilities)
  (println "OpenGL version:" (GL11/glGetString GL11/GL_VERSION))
  (GL11/glClearColor 0.0 0.0 0.0 0.0)
  (GL11/glMatrixMode GL11/GL_PROJECTION)
  (GL11/glOrtho 0.0 (:width @globals)
                0.0 (:height @globals)
                -1.0 1.0)
  (GL11/glMatrixMode GL11/GL_MODELVIEW))

(def pixels (BufferUtils/createByteBuffer 16))

(defn main-loop [{:keys [update-globals draw]}]
  (while (not (GLFW/glfwWindowShouldClose (:window @globals)))
    (update-globals)
    (draw)
    (GLFW/glfwSwapBuffers (:window @globals))
    (GLFW/glfwPollEvents)
    (GL11/glReadPixels 300 300 1 1 GL11/GL_RGBA, GL11/GL_UNSIGNED_BYTE
                       pixels)))

(defn run [opts]
  (try
    (init-window 800 600 "alpha")
    (init-gl)
    (main-loop opts)
    (.free (:errorCallback @globals))
    (.free (:keyCallback @globals))
    (GLFW/glfwDestroyWindow (:window @globals))
    (finally
      (GLFW/glfwTerminate))))