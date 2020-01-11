(ns UndeadReborn.URListener
  (:gen-class
    :init init
    :state state
    :implements [org.bukkit.event.Listener]
    :methods [[^{org.bukkit.event.EventHandler true} onDeath [org.bukkit.event.entity.EntityDeathEvent] void]
              [^{org.bukkit.event.EventHandler true} onRespawn [org.bukkit.event.player.PlayerRespawnEvent] void]
              [^{org.bukkit.event.EventHandler true} onEnable [org.bukkit.event.server.PluginEnableEvent] void]]
    :constructors {[hm.moe.pokkedoll.undeadreborn.Main] []})
  (:import (hm.moe.pokkedoll.undeadreborn Main)
           (org.bukkit.event.player PlayerRespawnEvent)
           (org.bukkit.event.entity EntityDeathEvent)
           (org.bukkit.entity EntityType Player Zombie LivingEntity)
           (org.bukkit GameMode Material Bukkit World ChatColor)
           (java.util HashMap ArrayList)
           (org.bukkit.inventory ItemStack)
           (org.bukkit.inventory.meta SkullMeta)
           (org.bukkit.event.server PluginEnableEvent)
           (org.bukkit.attribute Attribute)))

(def entityType {"ZOMBIE" EntityType/ZOMBIE, "PLAYER" EntityType/PLAYER})

; ゾンビのインベントリマップ
(def inventory-map (HashMap.))
; Undroppableなアイテムマップ
(def undroppable-map (HashMap.))

(defn getField [this key] (@(.state this) key))

(defn -init [^Main plugin]
  [[] (atom {:plugin plugin :world (.getString (.getConfig plugin) "world")})])

(defn playerSkull [^Player player]
  (let [item (ItemStack. Material/SKULL_ITEM 1 (short 3))]
    (let [sm ^SkullMeta(.getItemMeta item)]
      (.setOwner sm (.getName player))
      (.setItemMeta item sm)
      item))
  )

(defn -onDeath [this #^EntityDeathEvent event]
  (when (some? (getField this :world))
    (cond
      (= (.getEntityType event) (entityType "ZOMBIE"))
      (let [uuid (.getUniqueId (.getEntity event))]
        (if (.containsKey inventory-map (.getUniqueId (.getEntity event)))
          (doseq [drop (.get inventory-map (.getUniqueId (.getEntity event)))]
            (.dropItem (.getWorld (.getEntity event)) (.add (.getLocation (.getEntity event)) 0 1 0) drop)
            (.remove inventory-map (.getUniqueId (.getEntity event))))
          )
        )
      (= (.getEntityType event) (entityType "PLAYER"))
      (when (and (= [(.getName (.getWorld (.getEntity event))) (getField this :world)]) (= [(.getGameMode #^Player(.getEntity event)) (GameMode/SURVIVAL)]))
        (let [zombie ^Zombie(.spawnEntity (.getWorld (.getEntity event)) (.getLocation (.getEntity event)) (entityType "ZOMBIE")) player ^Player(.getEntity event)]
          (do
            (.setHealth zombie 5.0)
            (.setBaseValue (.getAttribute zombie (Attribute/GENERIC_MOVEMENT_SPEED)) 0.35)
            (.setCustomName zombie (str (.getName player) "'s Zombie"))
            (.setRemoveWhenFarAway zombie false)
            ; プレイヤーゾンビを作成するための処理
            (let [cloned (ArrayList. (filter #(instance? ItemStack %) (.getDrops event))) equip (.getEquipment (cast LivingEntity zombie))]
              (.setItemInHand equip (.getItemInMainHand (.getEquipment player))) (.setItemInMainHandDropChance equip 0)
              (.setItemInOffHand equip (.getItemInOffHand (.getEquipment player))) (.setItemInOffHandDropChance equip 0)
              (.setHelmet equip (playerSkull player)) (.setHelmetDropChance equip 0)
              (.setChestplate equip (.getChestplate (.getEquipment player))) (.setChestplateDropChance equip 0)
              (.setLeggings equip (.getLeggings (.getEquipment player))) (.setLeggingsDropChance equip 0)
              (.setBoots equip (.getBoots (.getEquipment player))) (.setBootsDropChance equip 0)
              ; Undroppableなアイテム
              (def undroppable (filter #(and (.hasItemMeta %) (.hasLore (.getItemMeta %)) (.contains (.getLore (.getItemMeta %)) (ChatColor/translateAlternateColorCodes (char (int \&)) "&6Undroppable"))) cloned))
              (when (not-empty undroppable)
                (.removeAll cloned undroppable)
                (.put undroppable-map (.getUniqueId player) (ArrayList. undroppable))
                )
              (.put inventory-map (.getUniqueId zombie) cloned)
              (.clear (.getDrops event))
              )
            )
          )
        )
      )
    )
  )

(defn -onRespawn [this #^PlayerRespawnEvent event]
  (let [uuid (.getUniqueId (.getPlayer event)) world (.getWorld (.getPlayer event))]
    (when (.containsKey undroppable-map uuid)
      (.addItem (.getInventory (.getPlayer event)) (into-array ItemStack (.get undroppable-map uuid)))
      (.remove undroppable-map uuid))
    )
  )

(defn cleanUp [this]
  (if (some? (getField this :world))
    (let [w (getField this :world)]
      (println (str "world is " w))
      (doseq [zombie (filter #(and (some? (.getCustomName %)) (.contains (.getCustomName %) "'s Zombie")) (.getEntities ^World (Bukkit/getWorld w)))]
        (.remove zombie)
        (println (str (.getCustomName zombie) " has removed!"))
        )
      )
    (println "CleanUp stopped")
    )
  )

(defn -onEnable [this #^PluginEnableEvent event]
  (do
    (println "Cleanup zombies...")
    (cleanUp this))
  )
