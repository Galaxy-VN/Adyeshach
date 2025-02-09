package ink.ptms.adyeshach.common.entity.type

import ink.ptms.adyeshach.Adyeshach
import ink.ptms.adyeshach.api.event.AdyeshachGameProfileGenerateEvent
import ink.ptms.adyeshach.api.nms.NMS
import ink.ptms.adyeshach.common.bukkit.BukkitAnimation
import ink.ptms.adyeshach.common.bukkit.BukkitPose
import ink.ptms.adyeshach.common.bukkit.data.GameProfile
import ink.ptms.adyeshach.common.editor.Editor
import ink.ptms.adyeshach.common.editor.Editor.toDisplay
import ink.ptms.adyeshach.common.entity.EntityTypes
import ink.ptms.adyeshach.common.util.mojang.MojangAPI
import com.google.gson.annotations.Expose
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.NumberConversions
import taboolib.common.platform.function.info
import taboolib.common.platform.function.submit
import taboolib.module.chat.colored
import taboolib.module.nms.inputSign
import java.util.*

/**
 * @Author sky
 * @Since 2020-08-04 15:44
 */
class AdyHuman : AdyEntityLiving(EntityTypes.PLAYER) {

    private val playerUUID = UUID.randomUUID()

    @Expose
    private val gameProfile = GameProfile()

    @Expose
    private var isSleepingLegacy = false

    @Expose
    var isHideFromTabList = true
        set(value) {
            if (value) {
                forViewers { removePlayerInfo(it) }
            } else {
                forViewers { addPlayerInfo(it) }
            }
            field = value
        }

    init {
        /**
         * 1.15 -> 16
         * 1.14 -> 15
         * 1.10 -> 13
         * 1.9 -> 12
         */
        registerMetaByteMask(at(11700 to 17, 11500 to 16, 11400 to 15, 11000 to 13, 10900 to 12), "skinCape", 0x01, true)
        registerMetaByteMask(at(11700 to 17, 11500 to 16, 11400 to 15, 11000 to 13, 10900 to 12), "skinJacket", 0x02, true)
        registerMetaByteMask(at(11700 to 17, 11500 to 16, 11400 to 15, 11000 to 13, 10900 to 12), "skinLeftSleeve", 0x04, true)
        registerMetaByteMask(at(11700 to 17, 11500 to 16, 11400 to 15, 11000 to 13, 10900 to 12), "skinRightSleeve", 0x08, true)
        registerMetaByteMask(at(11700 to 17, 11500 to 16, 11400 to 15, 11000 to 13, 10900 to 12), "skinLeftPants", 0x10, true)
        registerMetaByteMask(at(11700 to 17, 11500 to 16, 11400 to 15, 11000 to 13, 10900 to 12), "skinRightPants", 0x20, true)
        registerMetaByteMask(at(11700 to 17, 11500 to 16, 11400 to 15, 11000 to 13, 10900 to 12), "skinHat", 0x40, true)
        registerEditor("isSleepingLegacy")
                .reset { _, _ ->
                    setSleeping(false)
                }
                .modify { player, entity, _ ->
                    setSleeping(!isSleeping())
                    Editor.open(player, entity)
                }
                .display { _, _, _ ->
                    isSleeping().toDisplay()
                }
        registerEditor("isHideFromTabList")
                .reset { _, _ ->
                    isHideFromTabList = true
                }
                .modify { player, entity, _ ->
                    isHideFromTabList = !isHideFromTabList
                    Editor.open(player, entity)
                }
                .display { _, _, _ ->
                    isHideFromTabList.toDisplay()
                }
        registerEditor("playerName")
                .reset { _, _ ->
                    setName("Adyeshach NPC")
                }
                .modify { player, entity, _ ->
                    player.inputSign(arrayOf(getName(), "", "请在第一行输入内容")) {
                        if (it[0].isNotEmpty()) {
                            val name = "${it[0]}${it[1]}"
                            setName(if (name.length > 16) name.substring(0, 16) else name)
                        }
                        Editor.open(player, entity)
                    }
                }
                .display { _, _, _ ->
                    if (getName().isEmpty()) "§7_" else Editor.toSimple(getName())
                }
        registerEditor("playerPing")
                .reset { _, _ ->
                    setPing(60)
                }
                .modify { player, entity, _ ->
                    player.inputSign(arrayOf("${getPing()}", "", "请在第一行输入内容")) {
                        if (it[0].isNotEmpty()) {
                            setPing(NumberConversions.toInt(it[0]))
                        }
                        Editor.open(player, entity)
                    }
                }
                .display { _, _, _ ->
                    getPing().toString()
                }
        registerEditor("playerTexture")
                .reset { _, _ ->
                    resetTexture()
                }
                .modify { player, entity, _ ->
                    player.inputSign(arrayOf(getTextureName(), "", "请在第一行输入内容")) {
                        if (it[0].isNotEmpty()) {
                            setTexture(it[0])
                        }
                        Editor.open(player, entity)
                    }
                }
                .display { _, _, _ ->
                    if (gameProfile.textureName.isEmpty()) "§7_" else Editor.toSimple(gameProfile.textureName)
                }
        // refresh skin
        submit(async = true, delay = 200, period = 200) {
            if (manager != null) {
                forViewers {
                    refreshPlayerInfo(it)
                }
            } else {
                cancel()
            }
        }
    }

    override fun visible(viewer: Player, visible: Boolean) {
        if (visible) {
            addPlayerInfo(viewer)
            spawn(viewer) {
                NMS.INSTANCE.spawnNamedEntity(viewer, index, playerUUID, position.toLocation())
            }
            submit(delay = 1) {
                updateEquipment()
            }
            submit(delay = 5) {
                if (isDie) {
                    die(viewer)
                }
                if (isSleepingLegacy) {
                    setSleeping(true)
                }
                if (isHideFromTabList) {
                    removePlayerInfo(viewer)
                }
            }
        } else {
            removePlayerInfo(viewer)
            destroy(viewer) {
                NMS.INSTANCE.destroyEntity(viewer, index)
            }
        }
    }

    fun setName(name: String) {
        gameProfile.name = name.colored()
        respawn()
    }

    fun getName(): String {
        return gameProfile.name
    }

    fun setPing(ping: Int) {
        gameProfile.ping = ping
        respawn()
    }

    fun getPing(): Int {
        return gameProfile.ping
    }

    fun setTexture(name: String) {
        gameProfile.textureName = name
        submit(async = true) {
            try {
                MojangAPI.get(name)?.run {
                    setTexture(value, signature)
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }

    fun setTexture(texture: String, signature: String) {
        gameProfile.texture = arrayOf(texture, signature)
        respawn()
    }

    fun getTexture(): Array<String> {
        return gameProfile.texture
    }

    fun getTextureName(): String {
        return gameProfile.textureName
    }

    fun resetTexture() {
        gameProfile.texture = arrayOf("")
        respawn()
    }

    fun setSkinCapeEnabled(value: Boolean) {
        setMetadata("skinCapeEnabled", value)
    }

    fun isSkinCapeEnabled() {
        return getMetadata("skinCapeEnabled")
    }

    fun setSkinJacketEnabled(value: Boolean) {
        setMetadata("skinJacketEnabled", value)
    }

    fun isSkinJacketEnabled() {
        return getMetadata("skinJacketEnabled")
    }

    fun setSkinLeftSleeveEnabled(value: Boolean) {
        setMetadata("skinLeftSleeveEnabled", value)
    }

    fun isSkinLeftSleeveEnabled() {
        return getMetadata("skinLeftSleeveEnabled")
    }

    fun setSkinRightSleeveEnabled(value: Boolean) {
        setMetadata("skinRightSleeveEnabled", value)
    }

    fun isSkinRightSleeveEnabled() {
        return getMetadata("skinRightSleeveEnabled")
    }

    fun setSkinLeftPantsEnabled(value: Boolean) {
        setMetadata("skinLeftPantsEnabled", value)
    }

    fun isSkinLeftPantsEnabled() {
        return getMetadata("skinLeftPantsEnabled")
    }

    fun setSkinRightPantsEnabled(value: Boolean) {
        setMetadata("skinRightPantsEnabled", value)
    }

    fun isSkinRightPantsEnabled() {
        return getMetadata("skinRightPantsEnabled")
    }

    fun setSkinHatEnabled(value: Boolean) {
        setMetadata("skinHatEnabled", value)
    }

    fun isSkinHatEnabled() {
        return getMetadata("skinHatEnabled")
    }

    fun setSleeping(value: Boolean) {
        if (value) {
            if (version >= 11400) {
                setPose(BukkitPose.SLEEPING)
            } else {
                forViewers {
                    NMS.INSTANCE.sendPlayerSleeping(it, index, position.toLocation())
                }
            }
        } else {
            if (version >= 11400) {
                setPose(BukkitPose.STANDING)
            } else {
                displayAnimation(BukkitAnimation.LEAVE_BED)
            }
            teleport(position)
        }
        isSleepingLegacy = value
    }

    fun isSleeping(): Boolean {
        return if (version >= 11400) {
            getPose() == BukkitPose.SLEEPING
        } else {
            isSleepingLegacy
        }
    }

    fun refreshPlayerInfo(viewer: Player) {
        removePlayerInfo(viewer)
        addPlayerInfo(viewer)
        submit(delay = 5) {
            if (isHideFromTabList) {
                removePlayerInfo(viewer)
            }
        }
    }

    private fun addPlayerInfo(viewer: Player) {
        val event = AdyeshachGameProfileGenerateEvent(this, viewer, gameProfile.clone())
        event.call()
        NMS.INSTANCE.addPlayerInfo(viewer, playerUUID, event.gameProfile.name, event.gameProfile.ping, event.gameProfile.texture)
    }

    private fun removePlayerInfo(viewer: Player) {
        NMS.INSTANCE.removePlayerInfo(viewer, playerUUID)
    }
}