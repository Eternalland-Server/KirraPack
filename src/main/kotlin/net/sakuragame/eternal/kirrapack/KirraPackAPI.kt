package net.sakuragame.eternal.kirrapack

@Suppress("SpellCheckingInspection")
object KirraPackAPI {

    fun save() {
        Profile.profiles.values.forEach {
            it.save()
        }
    }
}