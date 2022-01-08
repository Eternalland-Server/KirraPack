package net.sakuragame.eternal.kirrapack.pack

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

data class Pack(val type: PackType, private val itemMapping: HashMap<Int, ItemStack> = getEmptyItemMapping()) {

    companion object {

        val air by lazy {
            ItemStack(Material.AIR)
        }

        fun getEmptyItemMapping(): HashMap<Int, ItemStack> {
            val itemMapping = hashMapOf<Int, ItemStack>()
            for (index in 0..53) {
                itemMapping[index] = air
            }
            return itemMapping
        }
    }

    fun setItem(index: Int, item: ItemStack) {
        if (index < 0 || index > 53) error("index went error, it must be in 0 - 53")
        itemMapping[index] = item
    }

    fun getItem(index: Int): ItemStack? {
        if (!itemMapping.containsKey(index)) return null
        return itemMapping[index]
    }

    fun get() = hashMapOf<Int, ItemStack>().apply {
        putAll(itemMapping)
    }
}
