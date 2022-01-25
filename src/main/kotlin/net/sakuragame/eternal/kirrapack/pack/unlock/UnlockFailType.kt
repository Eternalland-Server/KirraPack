package net.sakuragame.eternal.kirrapack.pack.unlock

/**
 * 解锁失败类型
 *
 * @property NOT_ENOUGH 所需货币不够.
 * @property TYPE_WRONG 解锁方式错误.
 *
 * @constructor Create empty Unlock fail type
 */
enum class UnlockFailType {
    NOT_ENOUGH, TYPE_WRONG
}