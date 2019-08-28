package online.christopherstocks.pugna.libs

import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ChatOptions(private var sender: CommandSender) {
    private val config: Config = Config()
    private val storage: Storage = config.getStorage()
    private val isSenderPlayer: Boolean = sender is Player
    private var target: String = "@-sender-@"
    private var slot: Int = 0

    fun sendMessage(message: String, slot: Int = 0) {
        if (isSenderPlayer) this.target = (sender as Player).name
        this.slot = slot
        sender.sendMessage(formatMessage(message))
    }

    fun sendTargetMessage(target: String, message: String, slot: Int = 0){
        this.target = target
        this.slot = slot
        sender.sendMessage(formatMessage(message))
    }

    fun broadcastMessage(message: String, slot: Int = 0) {
        if (isSenderPlayer) this.target = (sender as Player).name
        this.slot = slot
        val distance: Double = config.getDouble("range")
        if (distance <= 0.0) Bukkit.getOnlinePlayers().forEach {p->
            println(p)
            ChatOptions(p).sendTargetMessage(target, message, slot)
        }
        else {
            Bukkit.getPlayer(target)!!.getNearbyEntities(distance, distance, distance).forEach {e->
                println(e.javaClass.name)
                if (e is Player) {
                    ChatOptions(e).sendTargetMessage(target, message, slot)
                }
            }
            sendMessage(message, slot)
        }
    }

    fun broadcastMessage(target: String, message: String, slot: Int = 0) {
        this.target = target
        this.slot = slot
        val distance: Double = config.getDouble("range")
        if (distance <= 0.0) Bukkit.getOnlinePlayers().forEach {p-> ChatOptions(p).sendTargetMessage(target, message, slot)}
        else {
            Bukkit.getPlayer(target)!!.getNearbyEntities(distance, distance, distance).forEach {e->
                if (e is Player) {
                    ChatOptions(e).sendTargetMessage(target, message, slot)
                }
            }
            sendMessage(message, slot)
        }
    }

    fun attemptMessage(target: String, message: String, slot: Int = 0) {
        if (isSenderPlayer && target.equals((sender as Player).name)) return
        else {
            Bukkit.getOnlinePlayers().forEach { p ->
                if (p.name.equals(target)) {
                    ChatOptions(p).sendMessage(message, slot)
                }
            }
        }
    }

    fun formatMessage(input: String): String {
        var message: String = input

        if (message.isBlank()) return ""

        message = message.replace(":player:", target, ignoreCase = true)

        if (storage.checkPlayerExists(target)){
            message = message.replace(":slot:", (slot + 1).toString())
            message = message.replace(":race:", config.getString(storage.getRace(target, slot) + ".name"), ignoreCase = true)
            message = message.replace(":class:", config.getString(storage.getClass(target, slot) + ".name"), ignoreCase = true)
            message = message.replace(":points:", storage.getPoints(target, slot).toString(), ignoreCase = true)

            config.getStringList("stats").forEach {stat->
                message = message.replace(":$stat:", storage.getStat(target, stat, slot).toString(), ignoreCase = true)
                message = message.replace(":$stat" + "Total:", (storage.getStat(target, stat) + config.getInt(storage.getRace(target) + ".$stat") + config.getInt(storage.getClass(target) + ".$stat")).toString())
            }

            if (config.getBoolean("placeholders")) Bukkit.getOnlinePlayers().forEach {p-> if (p.name.equals(target)){ message = PlaceholderAPI.setPlaceholders(Bukkit.getPlayer(target), message) } }
        }

        return ChatColor.translateAlternateColorCodes('&', message)
    }
}