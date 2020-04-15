(ns lwjgl-test.init
  (:require [lwjgl-test.db :as db :refer [state]])
  (:import (org.lwjgl.opengl GL GL11)
           (org.lwjgl BufferUtils)
           (org.lwjgl.glfw GLFW GLFWErrorCallback GLFWKeyCallback)))

(defn init-window [width height title]
  (swap! state assoc
         :width     width
         :height    height
         :title     title
         :last-time (System/currentTimeMillis))

  (swap! state assoc
         :errorCallback (GLFWErrorCallback/createPrint System/err))
  (GLFW/glfwSetErrorCallback (:errorCallback @state))
  (when-not (GLFW/glfwInit)
    (throw (IllegalStateException. "Unable to initialize GLFW")))

  (GLFW/glfwDefaultWindowHints)
  (GLFW/glfwWindowHint GLFW/GLFW_VISIBLE GLFW/GLFW_FALSE)
  (GLFW/glfwWindowHint GLFW/GLFW_RESIZABLE GLFW/GLFW_TRUE)
  (swap! state assoc
         :window (GLFW/glfwCreateWindow width height title 0 0))
  (when (= (:window @state) nil)
    (throw (RuntimeException. "Failed to create the GLFW window")))

  (swap! state assoc
         :keyCallback
         (proxy [GLFWKeyCallback] []
           (invoke [window key scancode action mods]
             (when (and (= key GLFW/GLFW_KEY_ESCAPE)
                        (= action GLFW/GLFW_RELEASE))
               (GLFW/glfwSetWindowShouldClose (:window @state) true)))))
  (GLFW/glfwSetKeyCallback (:window @state) (:keyCallback @state))

  (let [vidmode (GLFW/glfwGetVideoMode (GLFW/glfwGetPrimaryMonitor))]
    (GLFW/glfwSetWindowPos
     (:window @state)
     (/ (- (.width vidmode) width) 2)
     (/ (- (.height vidmode) height) 2))
    (GLFW/glfwMakeContextCurrent (:window @state))
    (GLFW/glfwSwapInterval 1)
    (GLFW/glfwShowWindow (:window @state))))

(defn init-gl []
  (GL/createCapabilities)
  (println "OpenGL version:" (GL11/glGetString GL11/GL_VERSION))
  (GL11/glClearColor 0.0 0.0 0.0 0.0)
  (GL11/glMatrixMode GL11/GL_PROJECTION)
  (GL11/glOrtho 0.0 (:width @state)
                0.0 (:height @state)
                -1.0 1.0)
  (GL11/glMatrixMode GL11/GL_MODELVIEW))

(def pixels (BufferUtils/createByteBuffer 16))

(defn main-loop [{:keys [update-state draw]}]
  (while (not (GLFW/glfwWindowShouldClose (:window @state)))
    (swap! state update-state)
    (draw @state)
    (GLFW/glfwSwapBuffers (:window @state))
    (GLFW/glfwPollEvents)
    ))

(defn run [opts]
  (try
    (init-window 800 600 "alpha")
    (init-gl)
    (main-loop opts)
    (.free (:errorCallback @state))
    (.free (:keyCallback @state))
    (GLFW/glfwDestroyWindow (:window @state))
    (finally
      (GLFW/glfwTerminate))))