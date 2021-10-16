(ns undeadreborn.listeners
  (:gen-class
    :init init
    :state state
    :implements [org.bukkit.event.Listener]
    :methods [[^{org.bukkit.event.EventHandler true} onDeath [org.bukkit.event.entity.EntityDeathEvent] void]
              [^{org.bukkit.event.EventHandler true} onRespawn [org.bukkit.event.player.PlayerRespawnEvent] void]]
    :constructors {[hm.moe.pokkedoll.undeadreborn.Main] []})
  (:import (hm.moe.pokkedoll.undeadreborn Main)
           (org.bukkit.event.entity EntityDeathEvent)
           (org.bukkit.event.player PlayerRespawnEvent)
           (org.bukkit.entity EntityType Player Zombie Entity LivingEntity)
           (org.bukkit GameMode)
           (org.bukkit.attribute Attribute)
           (org.bukkit.potion PotionEffect)
           (org.bukkit.scheduler BukkitRunnable))
  (:require [undeadreborn.api :as api]
            [clojure.core.match :refer [match]]
            ))

(defn getField
  "docstring"
  [this key]
  (@(.state this) key))

(defn -init [#^Main main] [[] (atom {:plugin main :world (-> main .getConfig (.getStringList "worlds"))})])

(defn -onDeath
  "死んだときにゾンビを呼び出す"
  [this #^EntityDeathEvent e]
  (when (some? (getField this :world))
    (cond
      (= (-> e .getEntity) (EntityType/ZOMBIE))
        (let [entity (-> e .getEntity) uuid (-> entity .getUniqueId)]
          (if-let [zombie (filter (fn [z] (= uuid (:zombieUUID z))) api/zombies)]
            (let [world (-> entity .getWorld) loc (-> entity .getLocation (.add 0 1 0))]
             (doseq [drop (:contents zombie)]
               (-> world (.dropItem loc drop)))
             (def api/zombies (dissoc api/zombies zombie)))))
      (= (-> e .getEntity) (EntityType/PLAYER))
        (let [#^Player player (-> e .getEntity)]
          (when (= (-> player .getGameMode) GameMode/SURVIVAL)
            ; ゾンビを作るための処理
            (let [#^Zombie zombie (-> player .getWorld (.spawnEntity (-> player .getLocation) EntityType/ZOMBIE))]
              (-> zombie (.getAttribute Attribute/GENERIC_MOVEMENT_SPEED) (.setBaseValue 0.30))
              (-> zombie (.getAttribute Attribute/GENERIC_MAX_HEALTH) (.setBaseValue 5.0))
              (-> zombie (.setHealth 5.0))
              (-> zombie (.setCustomName (str (-> player .getName) "'s Zombie")))
              (-> zombie (.setCustomNameVisible false))
              (-> zombie (.setAI false))
              (.runTaskLater (reify BukkitRunnable (run [this] (-> zombie (.setAI true)))) api/plugin 20)
          )
      )
    )
  )))

(defn -onDeath
  "ゾンビを呼び出す"
  [this ^EntityDeathEvent e]
  (when (getField this :world)
    (let [^LivingEntity entity (-> e .getEntity)]
      (match [-> entity .getType]
             [(EntityType/ZOMBIE)] ""
             [(EntityType/PLAYER)]
             (when-let [^Player player ((fn [^Player player] (if (= (-> player .getGameMode) (GameMode/SURVIVAL)) player nil)) entity)]
               (let [^Zombie zombie (-> player .getWorld (.spawnEntity (-> player .getLocation) (EntityType/ZOMBIE)))]
                 (-> zombie (.getAttribute Attribute/GENERIC_MOVEMENT_SPEED) (.setBaseValue 0.30))
                 (-> zombie (.getAttribute Attribute/GENERIC_MAX_HEALTH) (.setBaseValue 5.0))
                 (-> zombie (.setHealth 5.0))
                 (-> zombie (.setCustomName (str (-> player .getName) "'s Zombie")))
                 (-> zombie (.setCustomNameVisible false))
                 (-> zombie (.setAI false))
                 (.runTaskLater (reify BukkitRunnable (run [_] (-> zombie (.setAI true)))) api/plugin 20)
                 )
               )
             :else nil
             )
      (cond (= (-> entity .getType) (EntityType/ZOMBIE))
            (= (-> entity .getType) (EntityType/ZOMBIE))

            )
      )
    )
  )
