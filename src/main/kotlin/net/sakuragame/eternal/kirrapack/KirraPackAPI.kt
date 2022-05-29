package net.sakuragame.eternal.kirrapack

import net.sakuragame.eternal.gemseconomy.currency.EternalCurrency
import net.sakuragame.eternal.justinventory.api.event.WarehouseOpenEvent.LockLevel
import net.sakuragame.eternal.kirrapack.pack.unlock.UnlockData
import taboolib.module.configuration.util.getMap

@Suppress("SpellCheckingInspection", "UNCHECKED_CAST")
object KirraPackAPI {

    fun save() {
        Profile.i()
    }

    fun getConditionByIndex(index: Int): Map<LockLevel, UnlockData> {
        val mapping = when (index) {
            1 -> KirraPack.conf.getMap<String, String>("settings.lock-progress.default")
            2 -> KirraPack.conf.getMap("settings.lock-progress.money")
            3 -> KirraPack.conf.getMap("settings.lock-progress.coins")
            4 -> KirraPack.conf.getMap("settings.lock-progress.vip")
            5 -> KirraPack.conf.getMap("settings.lock-progress.svp")
            6 -> KirraPack.conf.getMap("settings.lock-progress.mvp")
            else -> error("index went error, it must be in 1 - 6")
        }
        return getConditionByProgressMap(mapping)
    }

    private fun getConditionByProgressMap(progressMap: Map<String, String>): Map<LockLevel, UnlockData> {
        val toReturn = mutableMapOf<LockLevel, UnlockData>()
        var index = 0
        progressMap.forEach { (description, str) ->
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
        val currency = EternalCurrency.valueOf(split[0])
        val value = split[1].toDouble()
        return currency to value
    }

    private fun getLockLevelByIndex(index: Int): LockLevel {
        return when (index) {
            1 -> LockLevel.C
            2 -> LockLevel.B
            3 -> LockLevel.A
            else -> error("index went error, it must be in 1 - 3")
        }
    }
}