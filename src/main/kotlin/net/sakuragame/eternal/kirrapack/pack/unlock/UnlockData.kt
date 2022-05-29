package net.sakuragame.eternal.kirrapack.pack.unlock

import net.sakuragame.eternal.gemseconomy.currency.EternalCurrency

data class UnlockData(val description: String, val required: Pair<EternalCurrency, Double>)
