(ns lwjgl-test.db)

(defonce state
  (atom {:errorCallback nil
         :keyCallback   nil
         :window        nil
         :width         0
         :height        0
         :title         "none"
         :angle         0.0
         :last-time     0

         :bg nil
         :player-x -300
         :player-y 220
         :dir 259
         :speed 16}))