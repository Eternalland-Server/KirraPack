package net.sakuragame.eternal.kirrapack

import net.sakuragame.eternal.gemseconomy.api.GemsEconomyAPI
import net.sakuragame.eternal.kirrapack.function.FunctionListener
import net.sakuragame.eternal.kirrapack.pack.Pack
import net.sakuragame.eternal.kirrapack.pack.PackType
import net.sakuragame.eternal.kirrapack.pack.unlock.UnlockFailType
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.Schedule
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

    private val currentPacks = mutableMapOf<PackType, Pack>()

    companion object {

        val profiles = mutableMapOf<String, Profile>()

        fun Player.profile() = profiles.values.firstOrNull { it.player.uniqueId == uniqueId }

        @Schedule(async = true, period = 4000L)
        fun i() {
            if (profiles.isEmpty()) {
                return
            }
            debug("正在保存所有的玩家数据...")
            profiles.values.forEach {
                it.save()
            }
            debug("保存完毕.")
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
            val defaultMapping = Database.getItemsByPack(player, PackType.DEFAULT.index)
            if (defaultMapping.isEmpty()) {
                init()
                return@submit
            }
            currentPacks[PackType.DEFAULT] = Pack(PackType.DEFAULT, defaultMapping)
            if (player.hasPermission("vip")) {
                val vipMapping = Database.getItemsByPack(player, PackType.VIP.index)
                currentPacks[PackType.VIP] = Pack(PackType.VIP, vipMapping)
            }
            if (player.hasPermission("svp")) {
                val svpMapping = Database.getItemsByPack(player, PackType.SVP.index)
                currentPacks[PackType.SVP] = Pack(PackType.SVP, svpMapping)
            }
            if (player.hasPermission("mvp")) {
                val mvpMapping = Database.getItemsByPack(player, PackType.MVP.index)
                currentPacks[PackType.MVP] = Pack(PackType.MVP, mvpMapping)
            }
            val lastPackTypes = listOf(PackType.COINS, PackType.POINTS)
            lastPackTypes.forEach {
                val lastMapping = Database.getItemsByPack(player, it.index)
                if (lastMapping.isEmpty()) {
                    return@forEach
                }
                currentPacks[it] = Pack(it, lastMapping)
            }
        }
    }

    // init.
    private fun init() {
        PackType.values().forEach {
            if (!player.hasPermission(it.identifier)) {
                return@forEach
            }
            currentPacks[it] = Pack(it)
        }
    }

    // save to database.
    fun save() {
        submit(async = true) {
            currentPacks.forEach { (type, pack) ->
                pack.get().forEach { (key, item) ->
                    Database.setItem(player, type.index, key, item)
                }
            }
        }
    }

    fun getPackByIndex(num: Int): Pack? {
        return currentPacks.values.find { it.type.index == num }
    }

    fun unlock(packType: PackType, forceUnlock: Boolean = false): UnlockFailType? {
        if (currentPacks.keys.contains(packType)) {
            return UnlockFailType.ALREADY_UNLOCKED
        }
        if (forceUnlock) {
            doUnlock(packType)
        }
        val unlockCondition = packType.packUnlockCondition ?: return UnlockFailType.TYPE_WRONG
        unlockCondition.currencyMap.forEach { (currency, value) ->
            if (GemsEconomyAPI.getBalance(player.uniqueId, currency) < value) {
                return UnlockFailType.NOT_ENOUGH
            }
            doUnlock(packType)
            GemsEconomyAPI.withdraw(player.uniqueId, value, currency, "解锁 ${packType.internalName} 背包扣款")
        }
        return null
    }

    private fun doUnlock(packType: PackType) {
        currentPacks[packType] = Pack(packType)
        save()
    }

    fun drop() {
        FunctionListener.baffle.reset(player.name)
        profiles.remove(player.name)
    }
}