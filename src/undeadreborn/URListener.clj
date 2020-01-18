(ns undeadreborn.URListener
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
(def attribute {"GENERIC_MOVEMENT_SPEED" Attribute/GENERIC_MOVEMENT_SPEED, "GENERIC_MAX_HEALTH" Attribute/GENERIC_MAX_HEALTH})
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
      item)))

(defn -onDeath [this #^EntityDeathEvent event]
  (when (some? (getField this :world))
    (cond
      (= (.getEntityType event) (entityType "ZOMBIE"))
      (let [entity (.getEntity event) uuid (.getUniqueId (.getEntity event))]
        (if (.containsKey inventory-map uuid)
          (let [world (-> entity .getWorld) location (-> (-> entity .getLocation) (.add 0 1 0))]
            (doseq [drop (.get inventory-map uuid)]
              (.dropItem world location drop)
              (.remove inventory-map uuid)))))
      (= (.getEntityType event) (entityType "PLAYER"))
      (let [player ^Player(.getEntity event)]
        (when (and (= [(.getName (.getWorld player)) (getField this :world)]) (= [(.getGameMode player) (GameMode/SURVIVAL)]) (not (.isEmpty (.getContents (.getInventory player)))))
          (let [zombie ^Zombie(.spawnEntity (.getWorld (.getEntity event)) (.getLocation (.getEntity event)) (entityType "ZOMBIE"))]
            (-> zombie (.getAttribute (attribute "GENERIC_MOVEMENT_SPEED")) (.setBaseValue 0.35))
            (-> zombie (.getAttribute (attribute "GENERIC_MAX_HEALTH")) (.setBaseValue 5.0))
            (.setHealth zombie 5.0)
            (.setCustomName zombie (str (.getName player) "'s Zombie"))
            (.setCustomNameVisible zombie true)
            (.setRemoveWhenFarAway zombie false)
            ; プレイヤーゾンビを作成するための処理
            (let [cloned (ArrayList. (filter #(instance? ItemStack %) (.getDrops event))) equip (.getEquipment (cast LivingEntity zombie)) pEquip (.getEquipment player)]
              (-> equip (.setItemInMainHand (.getItemInMainHand pEquip))) (.setItemInMainHandDropChance equip 0)
              (-> equip (.setItemInOffHand (.getItemInOffHand pEquip))) (.setItemInOffHandDropChance equip 0)
              (-> equip (.setHelmet (playerSkull player))) (.setHelmetDropChance equip 0)
              (-> equip (.setChestplate (.getChestplate pEquip))) (.setChestplateDropChance equip 0)
              (-> equip (.setLeggings (.getLeggings pEquip))) (.setLeggingsDropChance equip 0)
              (-> equip (.setBoots (.getBoots pEquip))) (.setBootsDropChance equip 0)
              ; Undroppableなアイテム
              (def undroppable (filter #(and (.hasItemMeta %) (.hasLore (.getItemMeta %)) (.contains (.getLore (.getItemMeta %)) (ChatColor/translateAlternateColorCodes (char (int \&)) "&6Undroppable"))) cloned))
              (when (not-empty undroppable)
                (.removeAll cloned undroppable)
                (.put undroppable-map (.getUniqueId player) (ArrayList. undroppable)))
              (.put inventory-map (.getUniqueId zombie) cloned)
              ; ClojureはSeqは遅延するのに注意
              (.clear (.getDrops event)))))))))

(defn -onRespawn [this #^PlayerRespawnEvent event]
  (let [uuid (.getUniqueId (.getPlayer event))]
    (when (.containsKey undroppable-map uuid)
      (-> event .getPlayer .getInventory (.addItem (into-array ItemStack (.get undroppable-map uuid))))
      (.remove undroppable-map uuid))))

(defn cleanUp [this]
  (if (some? (getField this :world))
    (let [w (getField this :world)]
      (println (str "world is " w))
      (doseq [zombie (filter #(and (some? (.getCustomName %)) (.contains (.getCustomName %) "'s Zombie")) (.getEntities ^World (Bukkit/getWorld w)))]
        (.remove zombie)
        (println (str (.getCustomName zombie) " has removed!"))))
    (println "CleanUp stopped")))

(defn -onEnable [this #^PluginEnableEvent event]
  (do
    (println "Cleanup zombies...")
    (cleanUp this)))

