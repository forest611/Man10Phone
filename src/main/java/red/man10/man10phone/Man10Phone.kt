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


    private val apps = HashMap<Int,App>()
    lateinit var menu : Inventory
    private val version = "mOS 1.2"
    private val slot = mutableListOf(20,22,24,29,31,33,38,40,42,47,49,51)

    override fun onEnable() {
        saveDefaultConfig()
        server.pluginManager.registerEvents(this,this)

        menu = createHomeMenu()

        loadConfig()

    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

        if (sender !is Player){
            return false
        }

        if (!sender.hasPermission("mphone.op"))return true

        if (args.isEmpty()){
            openPhone(sender)
            return true
        }

        if (args[0] == "reload"){
            Thread {
                loadConfig()
                sender.sendMessage("§a§lリロード完了！")
            }.start()
            return true
        }

        return false
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    private fun openPhone(p: Player){

        val inv = menu

        for (app in apps){
            val data = app.value
            val icon = ItemStack(data.type,1)
            val meta = icon.itemMeta
            meta.setDisplayName(data.title)
            meta.setCustomModelData(data.customModelData)
            meta.lore = data.lore
            icon.itemMeta =meta
            inv.setItem(slot[app.key],icon)
        }

        p.openInventory(inv)

    }

    private fun loadConfig(){
        apps.clear()
        reloadConfig()
        for (i in 0..11){
            val app = App()

            app.title = config.getString("$i.title")!!
            app.lore = config.getStringList("$i.lore")
            app.type = Material.valueOf(config.getString("$i.type")!!)
            app.customModelData = config.getInt("$i.customModelData",0)
            app.cmd = config.getString("$i.cmd")!!
            app.serverCmd = config.getString("$i.serverCmd")!!

            apps[i] = app
        }
    }

    private fun createHomeMenu():Inventory{
        val inv = Bukkit.createInventory(null,54,version)

        val pane1 = ItemStack(Material.GRAY_STAINED_GLASS_PANE,1)
        val pane2 = ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE,1)


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
        if (e.view.title != version)return
        val player = e.whoClicked as Player
        e.isCancelled = true

        val item = e.currentItem ?:return

        if (item.itemMeta == null)return
        for (a in apps.values){
            if (a.title == item.itemMeta.displayName){
                if (!player.isOp){
                    player.isOp = true
                    player.performCommand(a.cmd)
                    player.isOp = false
                    break
                }
                player.performCommand(a.cmd)
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),a.serverCmd)
                player.closeInventory()
                break
            }
        }
    }

    class App{
        var title = ""
        var lore = mutableListOf<String>()
        var type = Material.STONE
        var customModelData = 0
        var cmd = ""
        var serverCmd = ""

    }
}