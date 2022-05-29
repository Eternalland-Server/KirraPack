package net.sakuragame.eternal.kirrapack.function

import net.sakuragame.eternal.dragoncore.api.event.PlayerSlotUpdateEvent
import net.sakuragame.eternal.dragoncore.api.event.slot.PlayerSlotClickEvent
import net.sakuragame.eternal.dragoncore.network.PacketSender
import net.sakuragame.eternal.justinventory.JustInventory
import net.sakuragame.eternal.justinventory.api.event.WarehouseOpenEvent
import net.sakuragame.eternal.justinventory.api.event.WarehouseOpenEvent.LockLevel.*
import net.sakuragame.eternal.kirrapack.Profile.Companion.profile
import net.sakuragame.eternal.kirrapack.pack.Pack
import net.sakuragame.eternal.kirrapack.pack.PackType
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common5.Baffle
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * KirraPack
 * net.sakuragame.eternal.kirrapack.function.FunctionListener
 *
 * @author kirraObj
 * @since 2021/12/31 18:44
 */
object FunctionListener {

    val baffle by lazy {
        Baffle.of(1, TimeUnit.SECONDS)
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun e(e: PlayerSlotClickEvent) {
        val player = e.player
        val ident = e.identifier

        if (e.isCancelled) return

        val profile = player.profile() ?: return

        val page = getPageOfPlayer(player) ?: return
        val slot = getSlotByIdentifier(ident) ?: return

        val pack = profile.getPackByIndex(page) ?: return
        val item = pack.getItem(slot)

        e.slotItem = item
        updateStock(player, pack)
    }

    @SubscribeEvent
    fun e(e: PlayerSlotUpdateEvent) {
        val player = e.player
        val ident = e.identifier

        val page = getPageOfPlayer(player) ?: return
        val slot = getSlotByIdentifier(ident) ?: return

        val profile = player.profile() ?: return
        val pack = profile.getPackByIndex(page) ?: return
        val item = e.itemStack

        pack.setItem(slot, item)

        PacketSender.putClientSlotItem(player, ident, item)
        updateStock(player, pack)
    }

    @SubscribeEvent
    fun e(e: WarehouseOpenEvent) {
        val player = e.player
        val profile = player.profile() ?: return
        val actualIndex = e.page - 1
        val pack = profile.getPackByIndex(actualIndex)
        if (pack == null) {
            val packType = PackType.values().first { it.index == actualIndex }
            e.name = packType.displayName
            e.stock = "&a库存: &f---"
            return
        }
        e.name = pack.type.displayName
        e.level = pack.level
        if (pack.level != A) {
            e.priceDesc = pack.type.conditionMap[pack.level]!!.description
        }
        e.contents = getContents(pack)
        e.stock = getStock(pack)
    }

    private fun getStock(pack: Pack): String {
        val count = pack.get().values.count { it.type != Material.AIR }
        val totalCount = when (pack.level) {
            D -> 0
            C -> 18
            B -> 36
            A -> 54
        }
        return "&a库存: &f$count/$totalCount"
    }

    private fun getSlotByIdentifier(ident: String): Int? {
        return ident
            .replace("warehouse_", "")
            .toIntOrNull()
    }

    private fun getPageOfPlayer(player: Player): Int? {
        return JustInventory.getWarehouse().cache[player.uniqueId]
    }

    private fun updateStock(player: Player, pack: Pack) {
        val mapping = mutableMapOf<String, String>()
        mapping += "warehouse_stock" to getStock(pack)
        PacketSender.sendSyncPlaceholder(player, mapping)
    }

    private fun getContents(pack: Pack): LinkedList<ItemStack> {
        return LinkedList<ItemStack>().apply {
            addAll(pack.get().values)
        }
    }
}