package net.sakuragame.eternal.kirrapack

import com.google.common.util.concurrent.Atomics
import com.google.gson.JsonObject
import ink.ptms.zaphkiel.ZaphkielAPI
import net.sakuragame.serversystems.manage.client.api.ClientManagerAPI
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.library.xseries.XMaterial
import taboolib.module.database.*
import taboolib.platform.util.buildItem
import taboolib.platform.util.isAir

@Suppress("SpellCheckingInspection")
object Database {

    data class ZaphkielItemData(val id: String, val value: String, val unique: String)

    const val PREFIX = "kirrapack"

    val host = KirraPack.conf.getHost("settings.database")

    val tableItem = Table("${PREFIX}_table_item", host) {
        add { id() }
        add("uid") {
            type(ColumnTypeSQL.INT)
        }
        add("pack_id") {
            type(ColumnTypeSQL.VARCHAR, 64)
        }
        add("slot") {
            type(ColumnTypeSQL.INT)
        }
        add("zap_id") {
            type(ColumnTypeSQL.VARCHAR, 64)
        }
        add("zap_data") {
            type(ColumnTypeSQL.VARCHAR, 512)
        }
        add("zap_unique") {
            type(ColumnTypeSQL.VARCHAR, 128)
        }
        add("amount") {
            type(ColumnTypeSQL.INT)
        }
    }

    val dataSource = host.createDataSource()

    init {
        tableItem.createTable(dataSource)
    }

    fun getItemsByPack(player: Player, packIdentifier: String): HashMap<Int, ItemStack> {
        val uid = ClientManagerAPI.getUserID(player.uniqueId)
        val itemMap = hashMapOf<Int, ItemStack>()
        tableItem.select(dataSource) {
            where("uid" eq uid and ("pack_id" eq packIdentifier))
        }.map {
            itemMap[getInt("slot")] = getItemFromParameters(player, getString("zap_id"), getString("zap_data"), getString("zap_unique"), getInt("amount"))
        }
        return itemMap
    }

    fun getItem(player: Player, packIdentifier: String, slot: Int): ItemStack? {
        val uid = ClientManagerAPI.getUserID(player.uniqueId)
        val atomicItem = Atomics.newReference<ItemStack>()
        if (uid == -1) return null
        tableItem.select(dataSource) {
            where("uid" eq uid and ("pack_id" eq packIdentifier) and ("slot" eq slot))
        }.first {
            val item = getItemFromParameters(player, getString("zap_id"), getString("zap_data"), getString("zap_unique"), getInt("amount"))
            atomicItem.set(item)
        }
        return atomicItem.get()
    }

    fun setItem(player: Player, packIdentifier: String, slot: Int, item: ItemStack) {
        val uid = ClientManagerAPI.getUserID(player.uniqueId)
        if (uid == -1) return
        val isFind = tableItem.find(dataSource) {
            where { where("uid" eq uid and ("pack_id" eq packIdentifier) and ("slot" eq slot)) }
        }
        val data = getDataFromItem(item)
        val amount = if (item.isAir) 0 else item.amount
        if (isFind) {
            // 更新.
            tableItem.update(dataSource) {
                where { where("uid" eq uid and ("pack_id" eq packIdentifier) and ("slot" eq slot)) }
                set("zap_id", data.id)
                set("zap_data", data.value)
                set("zap_unique", data.unique)
                set("amount", amount)
            }
            return
        }
        // 狂暴轰入.
        tableItem.insert(dataSource, "uid", "pack_id", "slot", "zap_id", "zap_data", "zap_unique", "amount") {
            value(uid, packIdentifier, slot, data.id, data.value, data.unique, amount)
        }
    }

    private fun getItemFromParameters(player: Player, id: String, data: String, unique: String, amount: Int): ItemStack {
        val deserializeStream = ZaphkielAPI.deserialize(JsonObject().also { obj ->
            if (id == "null") {
                return buildItem(XMaterial.AIR)
            }
            obj.addProperty("id", id)
            obj.addProperty("data", data)
            obj.addProperty("unique", unique)
        })
        return deserializeStream.rebuildToItemStack(player).also {
            it.amount = amount
        }
    }

    private fun getDataFromItem(item: ItemStack): ZaphkielItemData {
        if (item.isAir || ZaphkielAPI.getItem(item) == null) return ZaphkielItemData("null", "", "")
        val serialized = ZaphkielAPI.serialize(item)
        return ZaphkielItemData(serialized.get("id").asString, serialized.get("data").asString, serialized.get("unique").asString)
    }
}