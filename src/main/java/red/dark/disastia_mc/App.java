package red.dark.disastia_mc;

import org.bukkit.plugin.java.JavaPlugin;

public class App extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info("Hello, SpigotMC!");
        getServer().getPluginManager().registerEvents(new DeathListener(), this);
        getServer().getPluginManager().registerEvents(new DoubleDoors(), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("See you again, SpigotMC!");
    }
}
