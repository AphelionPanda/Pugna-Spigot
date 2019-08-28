package online.christopherstocks.pugna.listeners

import online.christopherstocks.pugna.external.Updater
import online.christopherstocks.pugna.libs.ChatOptions
import online.christopherstocks.pugna.libs.Config
import online.christopherstocks.pugna.libs.Storage
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoin : Listener {

    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        val storage: Storage = Config().getStorage(); val player: Player = e.player
        if (storage.checkPlayerExists(player.name)) storage.updatePlayer(player)
        else if (player.hasPermission("pugna.use")) storage.createPlayer(player); storage.updatePlayer(player)
        // Not fixed yet
        /*if (player.hasPermission("pugna.update")){
            if (Updater().check()) ChatOptions(player).sendMessage(Config().getString("update"))
            else ChatOptions(player).sendMessage(Config().getString("no-update"))
        }*/
    }

}