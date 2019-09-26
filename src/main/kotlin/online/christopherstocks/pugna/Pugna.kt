package online.christopherstocks.pugna

import online.christopherstocks.pugna.commands.PugnaCommand
import online.christopherstocks.pugna.commands.RollCommand
import online.christopherstocks.pugna.external.Placeholders
import online.christopherstocks.pugna.libs.Config
import online.christopherstocks.pugna.libs.Storage
import online.christopherstocks.pugna.listeners.PlayerJoin
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

class Pugna : JavaPlugin() {

    companion object {
        lateinit var instance: Plugin
    }

    override fun onEnable() {
        saveDefaultConfig(); instance = this
        val pluginManager = server.pluginManager; val config = Config(); val storage: Storage = config.getStorage()
        config.enableSlots(); storage.createDatabase()
        pluginManager.registerEvents(PlayerJoin(), this)
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            getConfig().set("placeholders", true)
            Placeholders().register()
        } else getConfig().set("placeholders", false)
        if (config.getInt("slots") < 1) config.set("slots", 1)
        getCommand("roll")!!.setExecutor(RollCommand())
        getCommand("pugna")!!.setExecutor(PugnaCommand())
        for (p in Bukkit.getOnlinePlayers()) {
            if (storage.checkPlayerExists(p.name)) storage.updatePlayer(p)
            else if (p.hasPermission("pugna.use")) { storage.createPlayer(p); storage.updatePlayer(p) }
        }
    }

}