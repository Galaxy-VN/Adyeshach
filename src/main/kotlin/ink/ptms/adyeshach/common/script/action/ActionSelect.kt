package ink.ptms.adyeshach.common.script.action

import ink.ptms.adyeshach.common.script.ScriptHandler.getManager
import ink.ptms.adyeshach.common.script.ScriptHandler.loadError
import ink.ptms.adyeshach.common.script.ScriptHandler.setEntities
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionSelect(val value: ParsedAction<*>, val byId: Boolean): ScriptAction<Void>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Void> {
        val s = frame.script()
        if (s.getManager() == null) {
            error("No manager selected.")
        }
        return frame.newFrame(value).run<Any>().thenAccept {
            s.setEntities(if (byId) s.getManager()!!.getEntityById(it.toString()) else listOf(s.getManager()!!.getEntityByUniqueId(it.toString())))
        }
    }

    internal object Parser {

        @KetherParser(["select"], namespace = "adyeshach", shared = true)
        fun parser() = scriptParser {
            val value = it.next(ArgTypes.ACTION)
            var byId = true
            if (it.hasNext()) {
                it.mark()
                if (it.nextToken() == "by" && it.hasNext()) {
                    byId = when (val type = it.nextToken().lowercase(Locale.getDefault())) {
                        "id" -> true
                        "uniqueid", "uuid" -> false
                        else -> throw loadError("Unknown select type $type")
                    }
                } else {
                    it.reset()
                }
            }
            ActionSelect(value, byId)
        }
    }
}