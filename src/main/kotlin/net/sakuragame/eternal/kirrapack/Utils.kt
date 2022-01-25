package net.sakuragame.eternal.kirrapack

import net.sakuragame.eternal.kirrapack.pack.PackType
import taboolib.module.configuration.util.getStringColored

fun PackType.getLockMessage(): String {
    return when (index) {
        1 -> KirraPack.conf.getStringColored("settings.lock-description.coins")!!
        2 -> KirraPack.conf.getStringColored("settings.lock-description.points")!!
        3 -> KirraPack.conf.getStringColored("settings.lock-description.vip")!!
        4 -> KirraPack.conf.getStringColored("settings.lock-description.svp")!!
        5 -> KirraPack.conf.getStringColored("settings.lock-description.mvp")!!
        else -> ""
    }
}