package online.christopherstocks.pugna.external

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import online.christopherstocks.pugna.libs.Config
import online.christopherstocks.pugna.libs.Storage
import org.bukkit.entity.Player

class Placeholders : PlaceholderExpansion() {
    val config = Config()
    val plugin = config.getInstance();

    override fun persist(): Boolean { return true }
    override fun canRegister(): Boolean { return true }
    override fun getAuthor(): String { return plugin.getDescription().getAuthors().toString() }
    override fun getIdentifier(): String { return "pugna" }
    override fun getVersion(): String { return plugin.getDescription().getVersion() }

    override fun onPlaceholderRequest(player: Player?, identifier: String?): String? {
        if (player == null){ return "" }
        val storage: Storage = config.getStorage()
        if (storage.checkPlayerExists(player.name)) storage.updatePlayer(player)
        else storage.createPlayer(player)

        if (identifier.equals("slot", ignoreCase = true)) return (storage.getActive(player.name) + 1).toString()
        if (identifier.equals("race", ignoreCase = true)) return Config().getConfig().getString(storage.getRace(player.name) + ".name")
        if (identifier.equals("class", ignoreCase = true)) return Config().getConfig().getString(storage.getClass(player.name) + ".name")
        if (identifier.equals("points", ignoreCase = true)) return storage.getPoints(player.name).toString()
        config.getConfig().getStringList("stats").forEach {if (identifier.equals(it, ignoreCase = true)) return storage.getStat(player.name, it).toString()}
        config.getConfig().getStringList("stats").forEach {if (identifier.equals(it + "Total", ignoreCase = true)) return ((storage.getStat(player.name, it)) + config.getInt(storage.getRace(player.name) + ".$it") + config.getInt(storage.getClass(player.name) + ".$it")).toString()}

        return "Empty"
    }
}