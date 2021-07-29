package red.dark.disastia_mc;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
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
import org.bukkit.event.player.PlayerRespawnEvent;
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

    @EventHandler(ignoreCancelled = true)
    public void sendServerMotd(ServerListPingEvent ev) {
        ArrayList<String> bans = new ArrayList<String>();

        for (BanEntry entry : Bukkit.getBanList(Type.NAME).getBanEntries()) {
            if (entry.getSource().equals(BAN_SOURCE)) {
                bans.add(entry.getTarget());
            }
        }

        String rip = bans.isEmpty() ? "" : "RIP ";

        ev.setMotd(Bukkit.getMotd() + "\n"
                + ChatColor.DARK_RED + rip + String.join(", ", bans));
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
        int deaths = player.getStatistic(Statistic.DEATHS);

        Bukkit.getLogger().info("%DEATH " + player.getName() + ":" + ev.getDeathMessage());
        Bukkit.broadcastMessage(ChatColor.RED + ev.getDeathMessage());
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Deaths: " + (deaths + 1));
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Lost: " + player.getLevel() + " levels");
        broadcastLostInventory(player.getInventory());

        Bukkit.getBanList(Type.NAME)
            .addBan(
                player.getName(),
                "You're still dead.",
                Date.from(Instant.now().plus(Duration.ofHours(9 + deaths * 24))),
                BAN_SOURCE
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
}
