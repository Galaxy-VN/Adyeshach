package ink.ptms.adyeshach.common.util

import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerEditBookEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.platform.util.buildBook
import taboolib.platform.util.giveItem
import taboolib.platform.util.hasLore
import taboolib.platform.util.takeItem
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

object Inputs {

    private val bookData = ConcurrentHashMap<String, Consumer<List<String>>>()

    fun Player.inputBook(lines: List<String> = emptyList(), listen: Consumer<List<String>>) {
        inventory.takeItem(99) { it.hasLore("§7Adyeshach Inputs") }
        giveItem(buildBook {
            write(lines.joinToString("\n"))
            material = Material.WRITABLE_BOOK
            name = "§fAdyeshach Book"
            lore += "§7Adyeshach Inputs"
        })
        bookData[name] = listen
    }

    @SubscribeEvent
    fun e(e: PlayerQuitEvent) {
        bookData.remove(e.player.name)
    }

    @SubscribeEvent
    fun e(e: PlayerEditBookEvent) {
        if (e.newBookMeta.lore?.get(0) == "§7Adyeshach Inputs") {
            val listen = bookData.remove(e.player.name) ?: return
            val lines = e.newBookMeta.pages.flatMap {
                TextComponent(it).toPlainText().replace("§0", "").split("\n")
            }
            listen.accept(lines)
            submit(delay = 1) {
                e.player.inventory.takeItem(99) { it.hasLore("§7Adyeshach Inputs") }
            }
        }
    }
}