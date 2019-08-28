package online.christopherstocks.pugna.commands

import online.christopherstocks.pugna.libs.ChatOptions
import online.christopherstocks.pugna.libs.Config
import online.christopherstocks.pugna.libs.Storage
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.security.SecureRandom

class RollCommand : CommandExecutor {

    override fun onCommand(sender: CommandSender, cmd: Command, s: String, args: Array<String>): Boolean {
        val config = Config()
        val chatOptions = ChatOptions(sender)

        if (sender !is Player) {
            chatOptions.sendMessage(config.getString("sender"))
            return false
        }

        val player: Player = sender
        val storage: Storage = Config().getStorage()

        if (storage.checkPlayerExists(player.name)) storage.updatePlayer(player)
        else if (player.hasPermission("pugna.use")) storage.createPlayer(player)

        val enabledStats: Boolean = config.getBoolean("stats-enabled")
        val stats: List<String> = config.getStringList("stats")
        var stat: String? = null

        var d: List<String> = config.getString("dice-default").split("d")
        val m: List<String> = config.getString("dice-max").split("d")
        val mm: Int = config.getInt("mod-max")
        var mod = 0
        var statMod = 0

        for (input: String in args){
            val check: MutableList<String> = input.split("d").toMutableList()
            if (input.contains("d") && config.isInteger(check[0]) && config.isInteger(check[1])) {
                val die: Int = check[0].toInt()
                val sides: Int = check[1].toInt()
                if (die > m[0].toInt()) check[0] = m[0]
                if (sides > m[1].toInt()) check[1] = m[1]
                if (die <= 0) check[0] = "1"
                if (sides <= 0) check[1] = "1"
                d = check.toList()
            }
            if (config.isInteger(input)) {
                val temp: Int = Integer.parseInt(input)
                if (temp > mm) mod = mm
                else if (temp < -mm) mod = -mm
                else mod = temp
            }
            if (enabledStats) {
                for (statIn: String in stats)
                    if (statIn.equals(input, ignoreCase = true)) {
                        val race: String = storage.getRace(player.name); val c: String = storage.getClass(player.name)
                        val raceMod: Int; val classMod: Int
                        if (race.equals("Empty", ignoreCase = true) || !config.getStringList("RaceList").contains(race)) raceMod = 0
                        else raceMod = config.getInt("$race.$statIn")
                        if (c.equals("Empty", ignoreCase = true) || !config.getStringList("ClassList").contains(c)) classMod = 0
                        else classMod = config.getInt("$c.$statIn")
                        statMod = raceMod + classMod + storage.getStat(player.name, statIn)
                        stat = statIn
                    }
            }
        }

        val modString: String = if (mod >= 0) "+" + mod else mod.toString()
        val statModString: String = if (statMod >= 0) "+$statMod" else statMod.toString()
        var total = 0

        for (i: Int in 0 until Integer.parseInt(d[0])) total += SecureRandom().nextInt(Integer.parseInt(d[1])) + 1

        if (stat != null) chatOptions.broadcastMessage(config.getString("stat-rolling").replace(":display:", player.displayName).replace(":result:", (total + mod + statMod).toString()).replace(":dice:", d[0] + "d" + d[1]).replace(":mod:", modString).replace(":statMod:", statModString).replace(":stat:", stat), storage.getActive(player.name))
        else chatOptions.broadcastMessage(config.getString("rolling").replace(":display:", player.displayName).replace(":result:", (total + mod).toString()).replace(":dice:", d[0] + "d" + d[1]).replace(":mod:", modString), storage.getActive(player.name))
        return true
    }

}