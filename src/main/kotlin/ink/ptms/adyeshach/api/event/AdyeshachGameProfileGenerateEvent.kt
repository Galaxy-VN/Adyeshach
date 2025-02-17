package ink.ptms.adyeshach.api.event

import ink.ptms.adyeshach.common.bukkit.data.GameProfile
import ink.ptms.adyeshach.common.entity.type.AdyHuman
import org.bukkit.entity.Player
import taboolib.platform.type.BukkitProxyEvent

/**
 * @Author sky
 * @Since 2020-08-14 19:21
 */
class AdyeshachGameProfileGenerateEvent(val entity: AdyHuman, val player: Player, var gameProfile: GameProfile) : BukkitProxyEvent() {

    override val allowCancelled: Boolean
        get() = false
}