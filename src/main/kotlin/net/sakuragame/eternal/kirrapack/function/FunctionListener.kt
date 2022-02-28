package net.sakuragame.eternal.kirrapack.function

import com.taylorswiftcn.megumi.uifactory.event.comp.UIFCompSubmitEvent
import net.sakuragame.eternal.dragoncore.api.event.PlayerSlotUpdateEvent
import net.sakuragame.eternal.dragoncore.api.event.slot.PlayerSlotClickEvent
import net.sakuragame.eternal.justinventory.JustInventory
import net.sakuragame.eternal.justinventory.api.event.WarehouseOpenEvent
import net.sakuragame.eternal.justinventory.ui.screen.WarehouseScreen
import net.sakuragame.eternal.justmessage.api.MessageAPI
import net.sakuragame.eternal.kirrapack.Profile.Companion.profile
import net.sakuragame.eternal.kirrapack.getLockMessage
import net.sakuragame.eternal.kirrapack.pack.Pack
import net.sakuragame.eternal.kirrapack.pack.PackType
import net.sakuragame.eternal.kirrapack.pack.unlock.UnlockFailType
import net.sakuragame.eternal.kirrapack.pack.unlock.UnlockFailType.*
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common5.Baffle
import taboolib.platform.util.asLangText
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

    data class SlotData(val index: Int, val slot: Int)

    val depend by lazy {
        JustInventory.getInstance()!!
    }

    val baffle by lazy {
        Baffle.of(1, TimeUnit.SECONDS)
    }

    @SubscribeEvent
    fun e(e: UIFCompSubmitEvent) {
        val player = e.player
        val profile = player.profile() ?: return
        val screenID = e.screenID

        if (screenID != WarehouseScreen.screenID) return
        val name = e.params.getParam(0)

        if (name != depend.name) return

        when (e.params.getParam(1)) {
            "warehouse_page" -> {
                val page = e.params.getParamI(2)
                WarehouseScreen.open(player, page)
            }
            "warehouse_unlock" -> {
                if (!baffle.hasNext(player.name)) {
                    MessageAPI.sendActionTip(player, player.asLangText("fail-unlock-pack-by-cooldown"))
                    return
                }
                baffle.next(player.name)
                val page = e.params.getParamI(2) - 1
                val packType = PackType.values().find { it.index == page }!!
                when (profile.unlock(packType)) {
                    null -> {
                        MessageAPI.sendActionTip(player, player.asLangText("succ-unlock-pack"))
                        player.closeInventory()
                    }
                    NOT_ENOUGH -> MessageAPI.sendActionTip(player, player.asLangText("fail-unlock-pack-by-coins-not-enough"))
                    TYPE_WRONG -> MessageAPI.sendActionTip(player, player.asLangText("fail-unlock-pack-by-type-wrong"))
                    ALREADY_UNLOCKED -> {
                        // GO FUCK YOURSELF
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun e(e: PlayerSlotClickEvent) {
        val player = e.player
        val ident = e.identifier

        if (e.isCancelled) return

        val slotData = getSlotData(player, ident) ?: return
        val profile = player.profile() ?: return
        val pack = profile.getPackByIndex(slotData.index) ?: return
        val item = pack.getItem(slotData.slot)

        e.slotItem = item
        updateStock(player, pack)
    }

    @SubscribeEvent
    fun e(e: PlayerSlotUpdateEvent) {
        val player = e.player
        val ident = e.identifier

        val slotData = getSlotData(player, ident) ?: return
        val profile = player.profile() ?: return
        val pack = profile.getPackByIndex(slotData.index) ?: return
        val item = e.itemStack

        pack.setItem(slotData.slot, item)
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
            e.name = packType.internalName
            e.stock = "&a库存: &f---"
            e.setLock(packType.getLockMessage())
            return
        }
        e.name = pack.type.internalName
        e.contents = getContents(pack)
        e.stock = getStock(pack)
    }

    private fun updateStock(player: Player, pack: Pack) {
        WarehouseScreen.setStock(player, getStock(pack))
    }

    private fun getSlotData(player: Player, identifier: String): SlotData? {
        if (!identifier.startsWith("warehouse_")) return null
        val index = WarehouseScreen.warehousePage.getOrDefault(player.uniqueId, -1) - 1
        val slot = identifier.substring(10).toIntOrNull() ?: return null
        if (index == -1) {
            return null
        }
        return SlotData(index, slot)
    }

    private fun getStock(pack: Pack): String {
        val count = pack.get().values.count { it.type != Material.AIR }
        return "&a库存: &f${count}/54"
    }

    private fun getContents(pack: Pack): LinkedList<ItemStack> {
        return LinkedList<ItemStack>().apply {
            addAll(pack.get().values)
        }
    }
}