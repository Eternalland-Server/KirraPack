package net.sakuragame.eternal.kirrapack

import net.sakuragame.eternal.gemseconomy.currency.EternalCurrency
import net.sakuragame.eternal.justinventory.api.event.WarehouseOpenEvent.LockLevel
import net.sakuragame.eternal.justinventory.api.event.WarehouseOpenEvent.LockLevel.*
import net.sakuragame.eternal.kirrapack.pack.unlock.UnlockData
import taboolib.module.configuration.Configuration

@Suppress("SpellCheckingInspection", "UNCHECKED_CAST")
object KirraPackAPI {

    fun save() {
        Profile.i()
    }

    fun getToNextLevel(lockLevel: LockLevel): LockLevel {
        return when (lockLevel) {
            D -> C
            C -> B
            B -> A
            A -> A
        }
    }

    fun getConditionByIndex(index: Int): MutableMap<LockLevel, UnlockData> {
        val mapping = when (index) {
            1 -> KirraPack.conf.getRawMapping("settings.lock-progress.default")
            2 -> KirraPack.conf.getRawMapping("settings.lock-progress.money")
            3 -> KirraPack.conf.getRawMapping("settings.lock-progress.coins")
            4 -> KirraPack.conf.getRawMapping("settings.lock-progress.vip")
            5 -> KirraPack.conf.getRawMapping("settings.lock-progress.svp")
            6 -> KirraPack.conf.getRawMapping("settings.lock-progress.mvp")
            else -> error("index goes error, it must be in 1 - 6")
        }
        return getConditionByProgressMap(mapping)
    }

    private fun Configuration.getRawMapping(node: String): MutableMap<String, String> {
        val toReturn = mutableMapOf<String, String>()
        val sections = arrayOf("first", "second", "third")
        sections.forEach {
            val description = getString("$node.$it.description")!!
            val required = getString("$node.$it.required")!!
            toReturn += description to required
        }
        return toReturn
    }

    private fun getConditionByProgressMap(mapping: MutableMap<String, String>): MutableMap<LockLevel, UnlockData> {
        val toReturn = mutableMapOf<LockLevel, UnlockData>()
        var index = 0
        mapping.forEach { (description, str) ->
            index++
            val lockLevel = getLockLevelByIndex(index)
            val currencyPair = getCurrencyPairByStr(str) ?: return@forEach
            toReturn += lockLevel to UnlockData(description, currencyPair)
        }
        return toReturn
    }

    private fun getCurrencyPairByStr(str: String): Pair<EternalCurrency, Double>? {
        val split = str.split(" @ ")
        if (split.size != 2) {
            return null
        }
        val currency = EternalCurrency.values().find { it.name.equals(split[0], ignoreCase = true) } ?: return null
        val value = split[1].toDouble()
        return currency to value
    }

    private fun getLockLevelByIndex(index: Int): LockLevel {
        return when (index) {
            1 -> C
            2 -> B
            3 -> A
            else -> error("index went error, it must be in 1 - 3")
        }
    }
}