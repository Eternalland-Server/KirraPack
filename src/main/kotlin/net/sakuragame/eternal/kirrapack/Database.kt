package net.sakuragame.eternal.kirrapack

import com.google.common.util.concurrent.Atomics
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import ink.ptms.zaphkiel.ZaphkielAPI
import ink.ptms.zaphkiel.taboolib.module.nms.ItemTagSerializer.serializeData
import net.sakuragame.eternal.kirrapack.pack.Pack
import net.sakuragame.serversystems.manage.client.api.ClientManagerAPI
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.module.database.ColumnOptionSQL
import taboolib.module.database.ColumnTypeSQL
import taboolib.module.database.Table
import taboolib.module.database.getHost
import taboolib.platform.util.isAir

@Suppress("SpellCheckingInspection")
object Database {

    private const val PREFIX = "kirrapack"

    private val jsonParser by lazy {
        JsonParser()
    }

    private val host = KirraPack.conf.getHost("settings.database")

    private val tableItem = Table("${PREFIX}_items", host) {
        add { id() }
        add("uid") {
            type(ColumnTypeSQL.INT) {
                options(ColumnOptionSQL.KEY)
            }
        }
        add("pack_id") {
            type(ColumnTypeSQL.INT) {
                options(ColumnOptionSQL.KEY)
            }
        }
        add("slot") {
            type(ColumnTypeSQL.INT) {
                options(ColumnOptionSQL.KEY)
            }
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

    private val dataSource by lazy {
        ClientManagerAPI.getDataManager().dataSource
    }

    init {
        tableItem.createTable(dataSource)
    }

    fun getItemsByPack(player: Player, packId: Int): HashMap<Int, ItemStack> {
        val uid = ClientManagerAPI.getUserID(player.uniqueId)
        val itemMap = hashMapOf<Int, ItemStack>()
        tableItem.select(dataSource) {
            where("uid" eq uid and ("pack_id" eq packId))
        }.map {
            itemMap[getInt("slot")] = getItemFromParameters(
                player, getString("zap_id"), getString("zap_data"), getString("zap_unique"), getInt("amount")
            )
        }
        return itemMap
    }

    fun getItem(player: Player, packId: Int, slot: Int): ItemStack? {
        val uid = ClientManagerAPI.getUserID(player.uniqueId)
        val atomicItem = Atomics.newReference<ItemStack>()
        if (uid == -1) return null
        tableItem.select(dataSource) {
            where("uid" eq uid and ("pack_id" eq packId) and ("slot" eq slot))
        }.first {
            val item = getItemFromParameters(
                player, getString("zap_id"), getString("zap_data"), getString("zap_unique"), getInt("amount")
            )
            atomicItem.set(item)
        }
        return atomicItem.get()
    }

    fun setItem(player: Player, packId: Int, slot: Int, item: ItemStack) {
        val uid = ClientManagerAPI.getUserID(player.uniqueId)
        if (uid == -1) return
        setItem(uid, packId, slot, item)
    }

    private fun setItem(uid: Int, packId: Int, slot: Int, item: ItemStack) {
        val isFind = tableItem.find(dataSource) {
            where { where("uid" eq uid and ("pack_id" eq packId) and ("slot" eq slot)) }
        }
        val data = getDataFromItem(item)
        val amount = if (item.isAir) 0 else item.amount
        if (isFind) {
            // 更新.
            tableItem.update(dataSource) {
                where { where("uid" eq uid and ("pack_id" eq packId) and ("slot" eq slot)) }
                set("zap_id", data.id)
                set("zap_data", data.dataValue)
                set("zap_unique", data.unique)
                set("amount", amount)
            }
            return
        }
        // 狂暴轰入.
        tableItem.insert(dataSource, "uid", "pack_id", "slot", "zap_id", "zap_data", "zap_unique", "amount") {
            value(uid, packId, slot, data.id, data.dataValue, data.unique, amount)
        }
    }

    private fun getItemFromParameters(
        player: Player, id: String, data: String, unique: String, amount: Int,
    ): ItemStack {
        val jsonObj = JsonObject().also { obj ->
            if (id == "null") {
                return Pack.air
            }
            obj.addProperty("id", id)
            obj.add("data", jsonParser.parse(data))
            if (unique.isNotEmpty()) {
                obj.add("unique", jsonParser.parse(unique))
            }
        }
        return ZaphkielAPI.deserialize(jsonObj).rebuildToItemStack(player).also {
            it.amount = amount
        }
    }

    private fun getDataFromItem(item: ItemStack): ZaphkielItemData {
        if (item.isAir || ZaphkielAPI.getItem(item) == null) return ZaphkielItemData("null", "", "")
        val itemStream = ZaphkielAPI.read(item)
        return ZaphkielItemData(
            id = itemStream.getZaphkielName(),
            dataValue = serializeData(itemStream.getZaphkielData()).toString(),
            unique = itemStream.getZaphkielUniqueData()?.let {
                serializeData(it).also { data ->
                    data.asJsonObject.remove("date-formatted")
                }
            }?.toString() ?: ""
        )
    }

    data class ZaphkielItemData(val id: String, val dataValue: String, val unique: String) {

        override fun toString() = "ZaphkielItemData{id: $id, data: $dataValue, unique: ${unique}}"
    }
}