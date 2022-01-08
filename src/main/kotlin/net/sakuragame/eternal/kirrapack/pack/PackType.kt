package net.sakuragame.eternal.kirrapack.pack

import taboolib.module.chat.colored

/**
 * KirraPack
 * net.sakuragame.eternal.kirrapack.pack.Type
 *
 * @author kirraObj
 * @since 2022/1/9 2:35
 */
enum class PackType {

    DEFAULT(0, "DEFAULT_WARE_HOUSE", "&6默认仓库".colored(), ""),
    COINS(1, "COINS_WARE_HOUSE", "&6金币仓库".colored(), ""),
    POINTS(2, "POINTS_WARE_HOUSE", "&6点券仓库".colored(), ""),
    VIP(3, "VIP_WARE_HOUSE", "&6VIP 仓库".colored(), ""),
    SVP(4, "SVP_WARE_HOUSE", "&6SVP 仓库".colored(), ""),
    MVP(5, "MVP_WARE_HOUSE", "&6MVP 仓库".colored(), "");

    val index: Int
    val identifier: String
    val internalName: String
    val lockMessage: String

    constructor(index: Int, identifier: String, internalName: String, lockMessage: String) {
        this.index = index
        this.identifier = identifier
        this.internalName = internalName
        this.lockMessage = lockMessage
    }
}