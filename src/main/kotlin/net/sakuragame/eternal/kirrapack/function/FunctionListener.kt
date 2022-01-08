package net.sakuragame.eternal.kirrapack.function

import com.taylorswiftcn.megumi.uifactory.event.comp.UIFCompSubmitEvent
import net.sakuragame.eternal.dragoncore.api.event.slot.PlayerSlotClickEvent
import net.sakuragame.eternal.justinventory.JustInventory
import net.sakuragame.eternal.justinventory.api.event.WarehouseOpenEvent
import net.sakuragame.eternal.justinventory.ui.screen.WarehouseScreen
import net.sakuragame.eternal.kirrapack.Profile.Companion.profile
import net.sakuragame.eternal.kirrapack.pack.Pack
import net.sakuragame.eternal.kirrapack.pack.PackType
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.event.SubscribeEvent
import java.util.*


/**
 * KirraPack
 * net.sakuragame.eternal.kirrapack.function.FunctionListener
 *
 * @author kirraObj
 * @since 2021/12/31 18:44
 */
object FunctionListener {

    val dependInstance by lazy {
        JustInventory.getInstance()!!
    }

    @SubscribeEvent
    fun e(e: UIFCompSubmitEvent) {
        val player = e.player
        val screenID = e.screenID

        if (screenID != WarehouseScreen.screenID) return
        val name = e.params.getParam(0)

        if (name != dependInstance.name) return
        val key = e.params.getParam(1)

        if (key != "warehouse_page") return
        val page = e.params.getParamI(2)

        WarehouseScreen.open(player, page)
    }

    @SubscribeEvent
    fun e(e: PlayerSlotClickEvent) {
        val ident = e.identifier

        if (!ident.startsWith("warehouse_")) return

        val slot = ident.substring(10).toIntOrNull() ?: return

        Bukkit.broadcastMessage("identifier: ${e.identifier}")
        Bukkit.broadcastMessage("clickType: ${e.clickType}")
        Bukkit.broadcastMessage("slotItem: ${e.slotItem}")
//        e.slotItem = ItemStack(Material.APPLE)
    }

    @SubscribeEvent
    fun e(e: WarehouseOpenEvent) {
        val player = e.player
        val profile = player.profile() ?: return
        val actualIndex = e.page - 1
        val pack = profile.getPackByIndex(actualIndex)
        if (pack == null) {
            e.name = PackType.values().first { it.index == actualIndex }.internalName
            e.stock = "&a库存: &f---"
            e.setLock("")
            return
        }
        e.name = pack.type.internalName
        e.contents = getContents(pack)
        e.stock = getStock(pack)
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