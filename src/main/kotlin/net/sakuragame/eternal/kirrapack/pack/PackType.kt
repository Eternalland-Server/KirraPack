package net.sakuragame.eternal.kirrapack.pack

import net.sakuragame.eternal.gemseconomy.currency.EternalCurrency
import net.sakuragame.eternal.kirrapack.pack.unlock.UnlockCondition
import taboolib.module.chat.colored

/**
 * KirraPack
 * net.sakuragame.eternal.kirrapack.pack.Type
 *
 * @author kirraObj
 * @since 2022/1/9 2:35
 */
enum class PackType(val index: Int, val identifier: String, val internalName: String, condition: UnlockCondition?) {

    DEFAULT(0, "DEFAULT_WARE_HOUSE", "&6默认仓库".colored(), null),
    COINS(1, "COINS_WARE_HOUSE", "&6金币仓库".colored(), UnlockCondition(currencyMap = mapOf(Pair(EternalCurrency.Money, 100000000.0)))),
    POINTS(2, "POINTS_WARE_HOUSE", "&6点券仓库".colored(), UnlockCondition(currencyMap = mapOf(Pair(EternalCurrency.Coins, 5000000.0)))),
    VIP(3, "VIP_WARE_HOUSE", "&6VIP 仓库".colored(), null),
    SVP(4, "SVP_WARE_HOUSE", "&6SVP 仓库".colored(), null),
    MVP(5, "MVP_WARE_HOUSE", "&6MVP 仓库".colored(), null);

    var packUnlockCondition: UnlockCondition? = condition
}