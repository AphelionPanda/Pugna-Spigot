package online.christopherstocks.pugna.external

import me.clip.placeholderapi.external.EZPlaceholderHook
import online.christopherstocks.pugna.libs.Config
import online.christopherstocks.pugna.libs.Storage
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class Placeholders(instance: Plugin) : EZPlaceholderHook(instance, "pugna") {

    override fun onPlaceholderRequest(player: Player, s: String?): String? {
        val config = Config()
        val storage: Storage = config.getStorage()
        if (storage.checkPlayerExists(player.name)) storage.updatePlayer(player)
        else storage.createPlayer(player)

        if (s.equals("slot", ignoreCase = true)) return (storage.getActive(player.name) + 1).toString()
        if (s.equals("race", ignoreCase = true)) return Config().getConfig().getString(storage.getRace(player.name) + ".name")
        if (s.equals("class", ignoreCase = true)) return Config().getConfig().getString(storage.getClass(player.name) + ".name")
        if (s.equals("points", ignoreCase = true)) return storage.getPoints(player.name).toString()
        config.getConfig().getStringList("stats").forEach {if (s.equals(it, ignoreCase = true)) return storage.getStat(player.name, it).toString()}
        config.getConfig().getStringList("stats").forEach {if (s.equals(it + "Total", ignoreCase = true)) return ((storage.getStat(player.name, it)) + config.getInt(storage.getRace(player.name) + ".$it") + config.getInt(storage.getClass(player.name) + ".$it")).toString()}

        return "Empty"
    }
}