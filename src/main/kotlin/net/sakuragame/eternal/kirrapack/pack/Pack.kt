package net.sakuragame.eternal.kirrapack.pack

import net.sakuragame.eternal.justinventory.api.event.WarehouseOpenEvent.LockLevel
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

data class Pack(val type: PackType, val itemMapping: HashMap<Int, ItemStack> = getEmptyItemMapping(), var level: LockLevel) {

    companion object {

        val air by lazy {
            ItemStack(Material.AIR)
        }

        val interval by lazy {
            0..53
        }

        fun getEmptyItemMapping(): HashMap<Int, ItemStack> {
            val mapping = hashMapOf<Int, ItemStack>()
            for (index in 0..53) {
                mapping[index] = air
            }
            return mapping
        }
    }

    fun setItem(index: Int, item: ItemStack) {
        if (index < interval.first || index > interval.last) error("index went error, it must be in ${interval.first} - ${interval.last}")
        itemMapping[index] = item
    }

    fun getItem(index: Int): ItemStack? {
        if (index < interval.first || index > interval.last) error("index went error, it must be in ${interval.first} - ${interval.last}")
        return itemMapping[index]!!
    }

    fun get() = hashMapOf<Int, ItemStack>().apply {
        putAll(itemMapping)
    }
}
