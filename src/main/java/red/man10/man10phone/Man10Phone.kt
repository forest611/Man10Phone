package red.man10.man10phone

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

class Man10Phone : JavaPlugin() ,Listener {


    val apps = HashMap<Int,App>()
    lateinit var menu : Inventory
    val version = "mOS 1.2"

    override fun onEnable() {
        saveDefaultConfig()
        server.pluginManager.registerEvents(this,this)

        menu = createHomeMenu()

        loadConfig()

    }

    override fun onCommand(sender: CommandSender?, command: Command?, label: String?, args: Array<out String>?): Boolean {

        if (sender !is Player){
            return false
        }

        if (!sender.hasPermission("mphone.op"))return true

        if (args == null || args.isEmpty()){
            openPhone(sender)
            return true
        }

        if (args[0] == "reload"){
            Thread(Runnable {
                loadConfig()
                sender.sendMessage("§a§lリロード完了！")
            }).start()
            return true
        }

        return false
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    fun openPhone(p: Player){

        val inv = menu

        val slot = mutableListOf(20,22,24,29,31,33,38,40,42,47,49,51)

        for (app in apps){
            val a = app.value
            val icon = ItemStack(a.type,1,a.damage.toShort())
            val meta = icon.itemMeta
            meta.displayName = a.title
            meta.lore = a.lore
            icon.itemMeta =meta
            inv.setItem(slot[app.key],icon)
        }

        p.openInventory(inv)

    }

    fun loadConfig(){
        apps.clear()
        reloadConfig()
        for (i in 0..11){
            val app = App()

            app.title = config.getString("$i.title")
            app.lore = config.getStringList("$i.lore")
            app.type = Material.valueOf(config.getString("$i.type"))
            app.damage = config.getInt("$i.damage",0)
            app.cmd = config.getString("$i.cmd")

            apps[i] = app
        }
    }

    fun createHomeMenu():Inventory{
        val inv = Bukkit.createInventory(null,54,version)

        val pane1 = ItemStack(Material.STAINED_GLASS_PANE,1,7)
        val pane2 = ItemStack(Material.STAINED_GLASS_PANE,1,3)


        val list1 = mutableListOf(0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,44,45,53)

        for (i in 0..53){
            if (list1.indexOf(i) != -1){
                inv.setItem(i,pane1)
                continue
            }
            inv.setItem(i,pane2)
        }

        return inv
    }

    @EventHandler
    fun inventoryClick(e:InventoryClickEvent){
        if (e.inventory.title != version)return
        val player = e.whoClicked as Player
        e.isCancelled = true
        if (e.currentItem == null)return
        if (e.currentItem.itemMeta == null)return
        for (a in apps.values){
            if (a.title == e.currentItem.itemMeta.displayName){
                if (!player.isOp){
                    player.isOp = true
                    player.performCommand(a.cmd)
                    player.isOp = false
                    break
                }
                player.performCommand(a.cmd)
                player.closeInventory()
                break
            }
        }
    }

    class App{
        var title = ""
        var lore = mutableListOf<String>()
        var type = Material.STONE
        var damage = 0
        var cmd = ""

    }
}
