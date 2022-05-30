package net.sakuragame.eternal.kirrapack

import net.sakuragame.eternal.cargo.CargoAPI
import net.sakuragame.eternal.cargo.value.ValueType
import net.sakuragame.eternal.gemseconomy.api.GemsEconomyAPI
import net.sakuragame.eternal.justinventory.api.event.WarehouseOpenEvent.LockLevel
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
@Suppress("SpellCheckingInspection")
class Profile(val player: Player) {

    private val packs = mutableListOf<Pack>()

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
            submit(async = true) {
                profiles[player.name] = Profile(player).apply {
                    read()
                }
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
            submit(async = true) {
                player.profile()?.apply {
                    save()
                    drop()
                }
            }
        }
    }

    // read from database.
    fun read() {
        // 默认背包为空, 说明玩家第一次进入服务器, 进行初始化
        if (Database.getItemsByPack(player, PackType.DEFAULT.index).isEmpty()) {
            init()
            return
        }
        PackType.values().forEach {
            val mapping = Database.getItemsByPack(player, it.index)
            packs += Pack(it, mapping, getLockLevelByPlayer(player, it))
        }
    }

    // init.
    private fun init() {
        PackType.values().forEach {
            val name = it.name.lowercase()
            val lockLevel = when {
                !player.hasPermission(name) -> LockLevel.D
                player.hasPermission("admin") -> LockLevel.A
                else -> LockLevel.C
            }
            packs += Pack(it, Pack.getEmptyItemMapping(), lockLevel)
            setLockLevelOfPlayer(player, it, lockLevel)
        }
    }

    // save to database.
    fun save() {
        packs.forEach { pack ->
            pack.get().forEach {
                val packIndex = pack.type.index
                val slot = it.key
                val item = it.value
                Database.setItem(player, packIndex, slot, item)
            }
        }
    }

    fun getPackByIndex(index: Int): Pack? {
        return packs.getOrNull(index)
    }

    fun unlock(type: PackType, levelTo: LockLevel, forceUnlock: Boolean = false): UnlockFailType? {
        val currentLevel = getLockLevelByPlayer(player, type)
        if (currentLevel == LockLevel.A) {
            return UnlockFailType.ALREADY_UNLOCKED
        }
        if (forceUnlock) {
            doUnlock(type, levelTo)
            return null
        }
        val condition = type.conditionMap[levelTo]!!
        val required = condition.required
        if (GemsEconomyAPI.getBalance(player.uniqueId, condition.required.first) < condition.required.second) {
            return UnlockFailType.NOT_ENOUGH
        }
        doUnlock(type, levelTo)
        GemsEconomyAPI.withdraw(player.uniqueId, required.second, required.first, "解锁 ${type.name} 背包扣款")
        return null
    }

    private fun doUnlock(type: PackType, levelTo: LockLevel) {
        submit(async = true, delay = 2L) {
            setLockLevelOfPlayer(player, type, levelTo)
            val pack = packs.find { it.type == type } ?: return@submit
            pack.level = levelTo
        }
    }

    private fun getLockLevelByPlayer(player: Player, type: PackType): LockLevel {
        val tier = CargoAPI.getAccountsManager().getAccount(player).get(ValueType.STORAGE, "KirraPack::${type.name.lowercase()}")!!
        return LockLevel.valueOf(tier)
    }

    private fun setLockLevelOfPlayer(player: Player, type: PackType, level: LockLevel) {
        CargoAPI.getAccountsManager().getAccount(player).set(ValueType.STORAGE, "KirraPack::${type.name.lowercase()}", level.name)
    }

    fun drop() {
        FunctionListener.baffle.reset(player.name)
        profiles.remove(player.name)
    }
}