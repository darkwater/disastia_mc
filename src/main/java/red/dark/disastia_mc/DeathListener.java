package red.dark.disastia_mc;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.bukkit.BanEntry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.BanList.Type;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ItemTag;
import net.md_5.bungee.api.chat.hover.content.Item;

public class DeathListener implements Listener {
    private static final String BAN_SOURCE = "mediumcore death";

    private HashMap<InetAddress, Instant> unbansByIp = new HashMap<>();

    @EventHandler(ignoreCancelled = true)
    public void sendServerMotd(ServerListPingEvent ev) {
        Instant unbanTime = unbansByIp.get(ev.getAddress());
        if (unbanTime != null && unbanTime.isAfter(Instant.now())) {

            ev.setMotd(Bukkit.getMotd() + "\n"
                    + ChatColor.DARK_RED + respawnInText(unbanTime));
            }
        }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent ev) {
        if (ev.getResult() != Result.KICK_BANNED)
            return;

        BanEntry entry = Bukkit.getBanList(Type.NAME).getBanEntry(ev.getPlayer().getUniqueId().toString());

        if (entry == null || entry.getExpiration() == null || !entry.getSource().equals(BAN_SOURCE))
            return;

        Instant unbanInstant = entry.getExpiration().toInstant();
        String reason = entry.getReason() + "\n\n" + ChatColor.GOLD + respawnInText(unbanInstant);

        ev.setKickMessage(reason);

        unbansByIp.put(ev.getAddress(), unbanInstant);
    }

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
        int deathsBefore = player.getStatistic(Statistic.DEATHS);
        int deathsAfter = deathsBefore + 1;
        String ord = ordinalSuffix(deathsAfter);
        Bukkit.getLogger().info("%DEATH " + player.getName() + ":" + ev.getDeathMessage());
        Bukkit.broadcastMessage(ChatColor.RED + ev.getDeathMessage());
        Bukkit.broadcastMessage(ChatColor.RED + player.getName() + " has died for the " + deathsAfter + ord + " time.");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Lost: " + player.getLevel() + " levels");
        broadcastLostInventory(player.getInventory());

        String reason = ChatColor.RED + ev.getDeathMessage() + "\n" + ChatColor.WHITE + "Deaths: " + deathsAfter;

        Instant unbanInstant = Instant.now().plus(Duration.ofHours(12 + Math.min(1, deathsBefore) * 24));

        Bukkit.getBanList(Type.NAME)
            .addBan(
                player.getName(),
                reason,
                Date.from(unbanInstant),
                BAN_SOURCE
            );

        unbansByIp.put(player.getAddress().getAddress(), unbanInstant);

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

    public void broadcastLostInventory(PlayerInventory inventory) {
        ArrayList<DeathHighlight> highlights = new ArrayList<DeathHighlight>();

        inventory.forEach((ItemStack item) -> {
            if (item == null)
                return;

            String type = item.getType().getKey().toString();
            DeathHighlight highlight = DeathHighlight.highlights.get(type);

            if (highlight == null)
                return;

            if (highlight.enchantedOnly && item.getEnchantments().isEmpty())
                return;

            highlights.add(highlight.withItem(item));
        });

        highlights.sort((DeathHighlight a, DeathHighlight b) -> a.order - b.order);

        for (DeathHighlight highlight : highlights) {
            broadcastLostItem(highlight);
        }
    }

    public void broadcastLostItem(DeathHighlight highlight) {
        ItemStack item = highlight.item;
        String json = convertItemStackToJson(item);

        Item itemContent = new Item(item.getType().getKey().toString(), 1, ItemTag.ofNbt(json));
        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_ITEM, itemContent);

        String itemName;
        if (item.getItemMeta().hasDisplayName()) {
            itemName = item.getItemMeta().getDisplayName();
        }
        else {
            itemName = highlight.name;
        }

        ComponentBuilder builder = new ComponentBuilder()
            .append("Lost: ").color(ChatColor.YELLOW)
            .append(itemName).color(highlight.color).event(hoverEvent);

        if (item.getAmount() > 1) {
            builder = builder.append(" x" + item.getAmount()).color(ChatColor.WHITE);
        }

        BaseComponent[] component = builder.create();

        Bukkit.getLogger().info("Lost: " + item.getType().getKey().toString() + " " + json);
        Bukkit.spigot().broadcast(component);
    }

    /**
    * Converts an {@link org.bukkit.inventory.ItemStack} to a Json string
    * for sending with {@link net.md_5.bungee.api.chat.BaseComponent}'s.
    *
    * @param itemStack the item to convert
    * @return the Json string representation of the item
    */
    public String convertItemStackToJson(ItemStack itemStack) {
        // ItemStack methods to get a net.minecraft.server.ItemStack object for serialization
        Class<?> craftItemStackClazz = ReflectionUtil.getOBCClass("inventory.CraftItemStack");
        Method asNMSCopyMethod = ReflectionUtil.getMethod(craftItemStackClazz, "asNMSCopy", ItemStack.class);

        // NMS Method to serialize a net.minecraft.server.ItemStack to a valid Json string
        Class<?> nmsItemStackClazz = ReflectionUtil.getNMClass("world.item.ItemStack");
        Class<?> nbtTagCompoundClazz = ReflectionUtil.getNMClass("nbt.NBTTagCompound");
        Method saveNmsItemStackMethod = ReflectionUtil.getMethod(nmsItemStackClazz, "save", nbtTagCompoundClazz);
        Method getCompoundMethod = ReflectionUtil.getMethod(nbtTagCompoundClazz, "getCompound", String.class);

        Object nmsNbtTagCompoundObj; // This will just be an empty NBTTagCompound instance to invoke the saveNms method
        Object nmsItemStackObj; // This is the net.minecraft.server.ItemStack object received from the asNMSCopy method
        Object itemAsJsonObject; // This is the net.minecraft.server.ItemStack after being put through saveNmsItem method
        Object tagAsJsonObject;

        try {
            nmsNbtTagCompoundObj = nbtTagCompoundClazz.getDeclaredConstructor().newInstance();
            nmsItemStackObj = asNMSCopyMethod.invoke(null, itemStack);
            itemAsJsonObject = saveNmsItemStackMethod.invoke(nmsItemStackObj, nmsNbtTagCompoundObj);
            tagAsJsonObject = getCompoundMethod.invoke(itemAsJsonObject, "tag");
        } catch (Throwable t) {
            Bukkit.getLogger().warning("failed to serialize itemstack to nms item");
            return null;
        }

        // Return a string representation of the serialized object
        return tagAsJsonObject.toString();
    }

    private String respawnInText(Instant unbanTime) {
        long hours = Instant.now().until(unbanTime, ChronoUnit.HOURS) + 1;

        if (hours == 1) {
            long minutes = Instant.now().until(unbanTime, ChronoUnit.MINUTES) + 1;
            return "Respawn in " + minutes + " minutes.";
        } else {
            return "Respawn in " + hours + " hours.";
        }
    }

    private String ordinalSuffix(int n) {
        switch (n % 100) {
            case 11:
            case 12:
            case 13: return "th";
            default: switch (n % 10) {
                case 1:  return "st";
                case 2:  return "nd";
                case 3:  return "rd";
                default: return "th";
            }
        }
    }
}
