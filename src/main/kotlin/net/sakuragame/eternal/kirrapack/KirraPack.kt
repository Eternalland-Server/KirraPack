package net.sakuragame.eternal.kirrapack

import net.sakuragame.eternal.justinventory.JustInventory
import taboolib.common.platform.Plugin
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.platform.BukkitPlugin

@Suppress("SpellCheckingInspection")
object KirraPack : Plugin() {

    @Config
    lateinit var conf: Configuration
        private set

    val plugin by lazy {
        BukkitPlugin.getInstance()
    }

    val justInventory by lazy {
        JustInventory.getInstance()!!
    }
}