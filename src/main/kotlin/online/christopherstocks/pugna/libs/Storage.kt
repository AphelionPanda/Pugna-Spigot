package online.christopherstocks.pugna.libs

import org.bukkit.entity.Player
import java.sql.Connection

abstract class Storage {

    val config = Config()
    val database = config.getConfig().getString("database")!!
    val table_prefix = config.getConfig().getString("table_prefix")!!
    val inactive = config.getConfig().getDouble("inactive-days")

    // Async
    abstract fun createDatabase()
    abstract fun updatePlugin()
    abstract fun updateStats()
    abstract fun deleteInactivePlayers()
    abstract fun createPlayer(player: Player)
    abstract fun updatePlayer(player: Player)

    // Sync
    abstract fun openConnection(): Connection

    abstract fun getRace(name: String, slot: Int = getActive(name)): String
    abstract fun getClass(name: String, slot: Int = getActive(name)): String
    abstract fun getPoints(name: String, slot: Int = getActive(name)): Int
    abstract fun getStat(name: String, stat: String, slot: Int = getActive(name)): Int

    abstract fun setRace(name: String, race: String, slot: Int = getActive(name))
    abstract fun setClass(name: String, c: String, slot: Int = getActive(name))
    abstract fun setPoints(name: String, amount: Int, slot: Int = getActive(name))
    abstract fun setStat(name: String, stat: String, amount: Int, slot: Int = getActive(name))

    abstract fun checkPlayerExists(name: String): Boolean
    abstract fun getActive(name: String): Int
    abstract fun setActive(name: String, value: Int)
    abstract fun reset(name: String, slot: Int = getActive(name))
    abstract fun resetPoints(name: String, slot: Int = getActive(name))
}