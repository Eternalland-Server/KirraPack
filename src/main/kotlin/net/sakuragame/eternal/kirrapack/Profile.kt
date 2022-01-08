package net.sakuragame.eternal.kirrapack

import net.sakuragame.eternal.kirrapack.pack.Pack
import net.sakuragame.eternal.kirrapack.pack.PackType
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit

/**
 * KirraPack
 * net.sakuragame.eternal.kirrapack.Profile
 *
 * @author kirraObj
 * @since 2022/1/9 2:43
 */
class Profile(val player: Player) {

    val currentPacks = mutableMapOf<PackType, Pack>()

    companion object {

        val profiles = mutableMapOf<String, Profile>()

        fun Player.profile() = profiles.values.firstOrNull { it.player.uniqueId == uniqueId }

        @Awake(LifeCycle.ACTIVE)
        fun i() {
            submit(period = 10000L, async = true) {
                profiles.values.forEach {
                    it.save()
                }
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        fun e(e: PlayerJoinEvent) {
            val player = e.player
            profiles[player.name] = Profile(player).apply {
                read()
            }
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        fun e(e: PlayerKickEvent) {
            dataRecycle(e.player)
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        fun e(e: PlayerQuitEvent) {
            dataRecycle(e.player)
        }

        private fun dataRecycle(player: Player) {
            player.profile()?.apply {
                save()
                drop()
            }
        }
    }

    // read from database.
    fun read() {
        submit(async = true) {
            PackType.values().forEach {
                val itemMapping = Database.getItemsByPack(player, it.internalName)
                // 数据库内无数据，进行初始化。
                if (itemMapping.isEmpty()) {
                    Bukkit.broadcastMessage("preInit")
                    init()
                    return@submit
                }
                currentPacks[it] = Pack(it, itemMapping)
            }
        }
    }

    // init.
    fun init() {
        PackType.values().forEach {
            if (!player.hasPermission(it.identifier)) {
                return@forEach
            }
            currentPacks[it] = Pack(it)
        }
        save()
    }

    // save to database.
    fun save() {
        submit(async = true) {
            currentPacks.forEach { (type, pack) ->
                pack.get().forEach { (key, item) ->
                    Database.setItem(player, type.identifier, key, item)
                }
            }
        }
    }

    fun getPackByIndex(num: Int): Pack? {
        return currentPacks.values.find { it.type.index == num }
    }

    fun drop() {
        profiles.remove(player.name)
    }
}