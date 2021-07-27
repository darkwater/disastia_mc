package red.dark.disastia_mc;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.BanList.Type;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class DeathListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent ev) {
        Player player = ev.getPlayer();

        if (player.isDead()) {
            player.spigot().respawn();
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 15 * 20, 4));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerRespawn(PlayerRespawnEvent ev) {
        if (ev.getPlayer().isBanned()) {
            ev.getPlayer().kickPlayer("You died. You can respawn tomorrow.");
        }
        else {
            List<World> worlds = Bukkit.getWorlds();
            worlds.sort((World a, World b) -> a.getName().length() - b.getName().length());

            ev.setRespawnLocation(worlds.get(0).getSpawnLocation());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent ev) {
        Player player = ev.getEntity();

        Bukkit.getLogger().info("%DEATH " + player.getName() + ":" + ev.getDeathMessage());
        Bukkit.broadcastMessage(ChatColor.RED + ev.getDeathMessage());
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Deaths: " + player.getStatistic(Statistic.DEATHS) + 1);
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Levels lost: " + player.getLevel());

        Bukkit.getBanList(Type.NAME)
            .addBan(
                player.getName(),
                "You're still dead.",
                Date.from(Instant.now().plus(Duration.ofHours(9))),
                "disastia mediumcore"
            );

        ev.setKeepInventory(true);
        player.getInventory().clear();
        ev.getDrops().clear();
        ev.setDeathMessage(null);

        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwningPlayer(player);
        skull.setItemMeta(meta);

        ev.getDrops().add(skull);
    }
}
