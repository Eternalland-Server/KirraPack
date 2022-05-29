package net.sakuragame.eternal.kirrapack.pack

import net.sakuragame.eternal.justinventory.api.event.WarehouseOpenEvent
import net.sakuragame.eternal.kirrapack.KirraPackAPI
import net.sakuragame.eternal.kirrapack.pack.unlock.UnlockData
import taboolib.module.chat.colored

/**
 * KirraPack
 * net.sakuragame.eternal.kirrapack.pack.Type
 *
 * @author kirraObj
 * @since 2022/1/9 2:35
 */
enum class PackType(val index: Int, val displayName: String, val conditionMap: MutableMap<WarehouseOpenEvent.LockLevel, UnlockData>) {

    DEFAULT(0, "&6默认仓库".colored(), KirraPackAPI.getConditionByIndex(1)),
    MONEY(1, "&6金币仓库".colored(), KirraPackAPI.getConditionByIndex(2)),
    COINS(2, "&6点券仓库".colored(), KirraPackAPI.getConditionByIndex(3)),
    VIP(3, "&6VIP 仓库".colored(), KirraPackAPI.getConditionByIndex(4)),
    SVP(4, "&6SVP 仓库".colored(), KirraPackAPI.getConditionByIndex(5)),
    MVP(5, "&6MVP 仓库".colored(), KirraPackAPI.getConditionByIndex(6));
}