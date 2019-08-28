package online.christopherstocks.pugna.commands

import online.christopherstocks.pugna.libs.ChatOptions
import online.christopherstocks.pugna.libs.Config
import online.christopherstocks.pugna.libs.Storage
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.logging.Level

class PugnaCommand: CommandExecutor {
    override fun onCommand(sender: CommandSender, cmd: Command, s: String, args: Array<String>): Boolean {
        val config = Config()
        val chatOptions = ChatOptions(sender)

        if (sender !is Player) { chatOptions.sendMessage(config.getString("sender")); return false }

        val player: Player = sender; val storage: Storage = Config().getStorage()

        if (!player.hasPermission("pugna.use")){ chatOptions.sendMessage(config.getString("permission")); return false }
        if (!storage.checkPlayerExists(player.name)) storage.createPlayer(player)
        storage.updatePlayer(player)

        if (args.size == 0) { chatOptions.sendMessage(config.getString("help")); return true }

        // /pugna debug [paramaters] ...
        if (args[0].equals("debug", ignoreCase = true)){
            if (!player.hasPermission("pugna.debug")){ chatOptions.sendMessage(config.getString("permission")); return false }
            if (args.size < 2) { chatOptions.sendMessage(config.getString("help")); return false }
            for (i in 1 until args.size) { chatOptions.sendMessage(config.getString(args[i])) }; return false
        }

        // /pugna slot [o:player]<->[o:slot]
        if (args[0].equals("slot", ignoreCase = true)){
            if (!player.hasPermission("pugna.slot")){ chatOptions.sendMessage(config.getString("permission")); return false }
            if (config.getBoolean("slot-handler")) { chatOptions.sendMessage(config.getString("slot-disabled")); return false }
            if (config.getInt("slots") == 1) { chatOptions.sendMessage(config.getString("slot-singular")); return false }
            if (args.size == 1) { chatOptions.sendMessage(config.getString("slot")); return true }
            if (storage.checkPlayerExists(args[1])) {
                if (!player.hasPermission("pugna.slot.other")) { chatOptions.sendMessage(config.getString("permission")); return false }
                if (args.size == 2 && config.isInteger(args[2])) {
                    if (!config.checkSlot(args[2].toInt() - 1)) { chatOptions.sendMessage(config.getString("slot-invalid")); return false }
                    storage.setActive(args[1], args[2].toInt() - 1)
                    chatOptions.sendTargetMessage(args[1], config.getString("switch-other"))
                    chatOptions.attemptMessage(args[1], config.getString("switch"))
                    return true
                } else { chatOptions.sendTargetMessage(args[1], config.getString("slot")); return true }
            }
            if (config.isInteger(args[1])) {
                if (!config.checkSlot(args[1].toInt() - 1)) { chatOptions.sendMessage(config.getString("slot-invalid")); return false }
                if (args.size == 2 && storage.checkPlayerExists(args[2])){
                    if (!player.hasPermission("pugna.slot.other")) { chatOptions.sendMessage(config.getString("permission")); return false }
                    storage.setActive(args[2], args[1].toInt() - 1)
                    chatOptions.sendTargetMessage(args[2], config.getString("switch-other"))
                    chatOptions.attemptMessage(args[2], config.getString("switch"))
                    return true
                }else{ storage.setActive(player.name, args[1].toInt() - 1); chatOptions.sendMessage(config.getString("switch")); return true }
            }
            else { chatOptions.sendMessage(config.getString("help")); return false }
        }

         // /pugna reload
        if (args[0].equals("reload", ignoreCase = true)){
            if (!player.hasPermission("pugna.reload")) { chatOptions.sendMessage(config.getString("permission")); return false }
            config.reloadConfig(); chatOptions.sendMessage(config.getString("reload")); return true
        }

        // /pugna race [o:race]<->[o:player]
        if (args[0].equals("race", ignoreCase = true)){
            if (!player.hasPermission("pugna.race")) { chatOptions.sendMessage(config.getString("permission")); return false }
            if (args.size == 1) { chatOptions.sendMessage(config.getString("race")); return true }
            if (storage.checkPlayerExists(args[1])) {
                if (!player.hasPermission("pugna.race.view")) { chatOptions.sendMessage(config.getString("permission")); return false }
                chatOptions.sendTargetMessage(args[1], config.getString("race")); return true
            }
            config.getStringList("RaceList").forEach { race ->
                if (args[1].equals(race, ignoreCase = true) || (args.size == 3 && args[2].equals(race, ignoreCase = true))) {
                    if (!player.hasPermission("pugna.race.*")){
                        if (player.hasPermission("pugna.race.$race")) return raceAssign(storage, args, race, chatOptions, config, player)
                        else { chatOptions.sendMessage(config.getString("permission")); return false }
                    }
                    else return raceAssign(storage, args, race, chatOptions, config, player)
                }
            }
            chatOptions.sendMessage(config.getString("race-invalid")); return false
        }

        // /pugna class [o:class]<->[o:player]
        if (args[0].equals("class", ignoreCase = true)){
            if (!player.hasPermission("pugna.class")) { chatOptions.sendMessage(config.getString("permission")); return false }
            if (args.size == 1) { chatOptions.sendMessage(config.getString("class")); return true }
            if (storage.checkPlayerExists(args[1])) {
                if (!player.hasPermission("pugna.class.view")) { chatOptions.sendMessage(config.getString("permission")); return false }
                chatOptions.sendTargetMessage(args[1], config.getString(config.getString("class"))); return true
            }
            config.getStringList("ClassList").forEach { c ->
                if (args[1].equals(c, ignoreCase = true) || (args.size == 3 && args[2].equals(c, ignoreCase = true))) {
                    if (!player.hasPermission("pugna.class.*")){
                        if (player.hasPermission("pugna.class.$c")) return classAssign(storage, args, c, chatOptions, config, player)
                        else { chatOptions.sendMessage(config.getString("permission")); return false }
                    }
                    else return classAssign(storage, args, c, chatOptions, config, player)
                }
            }
            chatOptions.sendMessage(config.getString("class-invalid")); return false
        }

        // /pugna display [o:slot]<->[o:player]
        if (args[0].equals("display", ignoreCase = true)){
            if (!player.hasPermission("pugna.display")) { chatOptions.sendMessage(config.getString("permission")); return false }
            if (args.size == 1) { config.getStringList("display").forEach {display-> chatOptions.sendMessage(display)}; return true }
            if (storage.checkPlayerExists(args[1])) {
                if (!player.hasPermission("pugna.display.other")) { chatOptions.sendMessage(config.getString("permission")); return false }
                if (args.size == 3 && config.isInteger(args[2])){
                    if (!config.checkSlot(args[2].toInt() - 1)) { chatOptions.sendMessage(config.getString("slot-invalid")); return false }
                    config.getStringList("display").forEach {display-> chatOptions.sendTargetMessage(args[1], display, args[2].toInt() - 1)}; return true
                }
                else { config.getStringList("display").forEach {display-> chatOptions.sendTargetMessage(args[1], display)}; return true }
            }
            if (config.isInteger(args[1])){
                if (!config.checkSlot(args[1].toInt() - 1)) { chatOptions.sendMessage(config.getString("slot-invalid")); return false }
                if (args.size == 3 && storage.checkPlayerExists(args[2])){
                    if (!player.hasPermission("pugna.display.other")) { chatOptions.sendMessage(config.getString("permission")); return false }
                    config.getStringList("display").forEach {display-> chatOptions.sendTargetMessage(args[2], display, args[1].toInt() - 1)}; return true
                }
                else { config.getStringList("display").forEach {display-> chatOptions.sendMessage(display, args[1].toInt() - 1)} }; return true
            }
            else { chatOptions.sendMessage(config.getString("help")); return false }
        }

        // /pugna help [o:page]
        if (args[0].equals("help", ignoreCase = true)){
            var page = 1; val items = config.getInt("help-items"); val pageItems = config.getStringList("help-pages")
            if (args.size == 2 && config.isInteger(args[1])) page = args[1].toInt()
            val pages = Math.ceil(pageItems.size.toDouble() / items)
            if (page > pages || page <= 0) { chatOptions.sendMessage(config.getString("help-invalid")); return true }
            for (i in (page - 1) * items until page * items){
                if (i >= pageItems.size) break
                if (pageItems.get(i).equals("empty", ignoreCase = true)) continue
                if (pageItems.get(i).equals("spacer", ignoreCase = true)) chatOptions.sendMessage("")
                else chatOptions.sendMessage(pageItems.get(i).replace(":page:".toRegex(), page.toString()).replace(":pages:".toRegex(), pages.toInt().toString()))
            }
            return true
        }

        // /pugna reset [o:slot]<->[o:player]
        if (args[0].equals("reset", ignoreCase = true)){
            if (!player.hasPermission("pugna.reset")) { chatOptions.sendMessage(config.getString("permission")); return false }
            if (args.size == 1){ storage.reset(player.name); chatOptions.sendMessage(config.getString("reset")); return true }
            if (storage.checkPlayerExists(args[1])) {
                if (!player.hasPermission("pugna.reset.other")) { chatOptions.sendMessage(config.getString("permission")); return false }
                if (args.size == 3 && config.isInteger(args[2])){
                    if (!config.checkSlot(args[2].toInt() - 1)) { chatOptions.sendMessage(config.getString("slot-invalid")); return false }
                    storage.reset(args[1], args[2].toInt() - 1)
                    if (args[1] != player.name) {
                        chatOptions.sendTargetMessage(args[1], config.getString("reset-other").replace(":slot:", args[2]))
                        chatOptions.attemptMessage(args[1], config.getString("reset").replace(":slot:", args[2]))
                        return true
                    } else { chatOptions.sendMessage(config.getString("reset").replace(":slot:", args[2])); return true }
                }
                else {
                    storage.reset(args[1])
                    if (args[1] != player.name) {
                        chatOptions.sendTargetMessage(args[1], config.getString("reset-other"))
                        chatOptions.attemptMessage(args[1], config.getString("reset"))
                        return true
                    } else { chatOptions.sendMessage(config.getString("reset")); return true }
                }
            }
            if (config.isInteger(args[1])){
                if (!config.checkSlot(args[1].toInt() - 1)) { chatOptions.sendMessage(config.getString("slot-invalid")); return false }
                if (args.size == 3 && storage.checkPlayerExists(args[2])){
                    if (!player.hasPermission("pugna.reset.other")) { chatOptions.sendMessage(config.getString("permission")); return false }
                    storage.reset(args[2], args[1].toInt() - 1)
                    if (args[2] != player.name) {
                        chatOptions.sendTargetMessage(args[2], config.getString("reset-other").replace(":slot:", args[1]))
                        chatOptions.attemptMessage(args[2], config.getString("reset").replace(":slot:", args[1]))
                        return true
                    } else { chatOptions.sendMessage(config.getString("reset").replace(":slot:", args[1])); return true }
                }
                else {
                    storage.reset(player.name, args[1].toInt() - 1)
                    chatOptions.sendMessage(config.getString("reset").replace(":slot:", args[1]), args[1].toInt() - 1)
                    return true
                }
            }
        }

        // /pugna delete - pugna.delete
        if (args[0].equals("delete", ignoreCase = true)){
            if (!player.hasPermission("pugna.delete")) { chatOptions.sendMessage(config.getString("permission")); return false }
            if (config.getDouble("inactive-days") <= 0.00) { chatOptions.sendMessage(config.getString("delete-0")); return false }
            storage.deleteInactivePlayers(); chatOptions.sendMessage(config.getString("delete")); return true
        }

        // /pugna points [spend/reset/remove/add/o:player] [ao:stat}<->[ao:amount]<->[ao:player]
        if (args[0].equals("points", ignoreCase = true)){
            if (!player.hasPermission("pugna.points")) { chatOptions.sendMessage(config.getString("permission")); return false }
            if (args.size == 1) { chatOptions.sendMessage(config.getString("points")); return true }
            if (storage.checkPlayerExists(args[1])) {
                if (!player.hasPermission("pugna.points.view")) { chatOptions.sendMessage(config.getString("permission")); return false }
                chatOptions.sendTargetMessage(args[1], config.getString("points")); return true
            }
            var stat = "@~Stat~@"; var target = player.name; var number = 1
            if (args.size == 3 && storage.checkPlayerExists(args[2])){ target = args[2] }
            if (args.size == 4 && storage.checkPlayerExists(args[3])){ target = args[3] }
            if (args.size == 5 && storage.checkPlayerExists(args[4])){ target = args[4] }
            if (args.size == 3 && config.isInteger(args[2])){ number = args[2].toInt() }
            if (args.size == 4 && config.isInteger(args[3])){ number = args[3].toInt() }
            if (args.size == 5 && config.isInteger(args[4])){ number = args[4].toInt() }
            config.getStringList("stats").forEach {statC-> if (args.contains(statC)){ stat = statC } }
            config.getInstance().logger.log(Level.WARNING, player.name + " has initiated a " + args[1] + " action on $target, slot/amount $number - (Stat: $stat)")

            if (args[1].equals("spend", ignoreCase = true)){
                if (!player.hasPermission("pugna.points.spend")) { chatOptions.sendMessage(config.getString("permission")); return false }
                if (args.size == 2) { chatOptions.sendMessage(config.getString("help")); return false }
                if (stat == "@~Stat~@") { chatOptions.sendMessage(config.getString("stat-invalid")); return false }
                if (storage.getPoints(player.name) - number >= 0) {
                    if (number == 0) { chatOptions.sendMessage(config.getString("points-invalid")); return false }
                    storage.setPoints(player.name, storage.getPoints(player.name) - number)
                    storage.setStat(player.name, stat, storage.getStat(player.name, stat) + number)
                    chatOptions.sendMessage(config.getString("points-spend").replace(":stat:", stat).replace(":amount:", number.toString()))
                    return true
                } else { chatOptions.sendMessage(config.getString("points-empty")); return true }
            }
            if (args[1].equals("reset", ignoreCase = true)){
                if (!player.hasPermission("pugna.points.reset")) { chatOptions.sendMessage(config.getString("permission")); return false }
                if (args.size == 2){ storage.reset(target) }
                if (!config.checkSlot(number - 1)) { chatOptions.sendMessage(config.getString("slot-invalid")); return false }
                storage.resetPoints(target, number)

                if (target != player.name) {
                    chatOptions.sendTargetMessage(target, config.getString("points-reset-other").replace(":slot:", number.toString()))
                    chatOptions.attemptMessage(target, config.getString("points-reset").replace(":slot:", number.toString()))
                    return true
                } else { chatOptions.sendMessage(config.getString("points-reset").replace(":slot:", number.toString())); return true }

            }
            if (args[1].equals("remove", ignoreCase = true)){
                if (!player.hasPermission("pugna.points.remove")) { chatOptions.sendMessage(config.getString("permission")); return false }
                storage.resetPoints(target)
                storage.setPoints(target, storage.getPoints(target) - number)
                if (target != player.name) {
                    chatOptions.sendTargetMessage(target, config.getString("points-alter-other"))
                    chatOptions.attemptMessage(target, config.getString("points-alter"))
                    return true
                } else { chatOptions.sendMessage(config.getString("points-alter")); return true }
            }
            if (args[1].equals("add", ignoreCase = true)){
                if (!player.hasPermission("pugna.points.add")) { chatOptions.sendMessage(config.getString("permission")); return false }
                storage.setPoints(target, storage.getPoints(target) + number)
                if (target != player.name) {
                    chatOptions.sendTargetMessage(target, config.getString("points-alter-other"))
                    chatOptions.attemptMessage(target, config.getString("points-alter"))
                    return true
                } else { chatOptions.sendMessage(config.getString("points-alter")); return true }
            }
        }

        chatOptions.sendMessage(config.getString("help"))
        return true
    }

    private fun raceAssign(storage: Storage, args: Array<String>, race: String, chatOptions: ChatOptions, config: Config, player: Player): Boolean {
        if (player.hasPermission("pugna.race.other")){
            if (storage.checkPlayerExists(args[1])) {
                storage.setRace(args[1], race)
                if (args[1] != player.name) {
                    chatOptions.sendTargetMessage(args[1], config.getString("race-switch-other"))
                    chatOptions.attemptMessage(args[1], config.getString("race-switch"))
                    return true
                } else { chatOptions.sendMessage(config.getString("race-switch")); return true }
            }

            if (args.size == 3 && storage.checkPlayerExists(args[2])) {
                storage.setRace(args[2], race)
                if (args[2] != player.name) {
                    chatOptions.sendTargetMessage(args[2], config.getString("race-switch-other"))
                    chatOptions.attemptMessage(args[2], config.getString("race-switch"))
                    return true
                } else { chatOptions.sendMessage(config.getString("race-switch")); return true }
            }
        }

        storage.setRace(player.name, race); chatOptions.sendMessage(config.getString("race-switch")); return true
    }

    private fun classAssign(storage: Storage, args: Array<String>, c: String, chatOptions: ChatOptions, config: Config, player: Player): Boolean {
        if (player.hasPermission("pugna.class.other")){
            if (storage.checkPlayerExists(args[1])) {
                storage.setClass(args[1], c)
                if (args[1] != player.name) {
                    chatOptions.sendTargetMessage(args[1], config.getString("class-switch-other"))
                    chatOptions.attemptMessage(args[1], config.getString("class-switch"))
                    return true
                } else { chatOptions.sendMessage(config.getString("class-switch")); return true }
            }

            if (args.size == 3 && storage.checkPlayerExists(args[2])) {
                storage.setClass(args[2], c)
                if (args[2] != player.name) {
                    chatOptions.sendTargetMessage(args[2], config.getString("class-switch-other"))
                    chatOptions.attemptMessage(args[2], config.getString("class-switch"))
                    return true
                } else { chatOptions.sendMessage(config.getString("class-switch")); return true }
            }
        }

        storage.setClass(player.name, c); chatOptions.sendMessage(config.getString("class-switch")); return true
    }
}