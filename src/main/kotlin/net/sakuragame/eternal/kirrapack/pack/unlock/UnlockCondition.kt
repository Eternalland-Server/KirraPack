package net.sakuragame.eternal.kirrapack.pack.unlock

import net.sakuragame.eternal.gemseconomy.currency.EternalCurrency

data class UnlockCondition(val currencyMap: Map<EternalCurrency, Double>)
