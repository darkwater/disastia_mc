package red.dark.disastia_mc;

import static java.util.Map.entry;

import java.util.Map;

import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.ChatColor;

public class DeathHighlight {
    static ChatColor tier1Color = ChatColor.LIGHT_PURPLE;
    static ChatColor tier2Color = ChatColor.AQUA;
    static ChatColor tier3Color = ChatColor.GOLD;
    static ChatColor defaultColor = ChatColor.WHITE;

    static Map<String, DeathHighlight> highlights = Map.ofEntries(
        entry("minecraft:totem_of_undying",         new DeathHighlight( 0, "Totem of Undying", tier1Color)),
        entry("minecraft:enchanted_golden_apple",   new DeathHighlight( 1, "Enchanted Golden Apple", tier1Color)),
        entry("minecraft:nether_star",              new DeathHighlight( 2, "Nether Star", tier1Color)),
        entry("minecraft:dragon_breath",            new DeathHighlight( 3, "Dragon's Breath", tier1Color)),
        entry("minecraft:elytra",                   new DeathHighlight( 4, "Elytra", tier1Color)),
        entry("minecraft:netherite_helmet",         new DeathHighlight( 5, "Netherite Helmet", tier1Color)),
        entry("minecraft:netherite_chestplate",     new DeathHighlight( 6, "Netherite Chestplate", tier1Color)),
        entry("minecraft:netherite_leggings",       new DeathHighlight( 7, "Netherite Leggings", tier1Color)),
        entry("minecraft:netherite_boots",          new DeathHighlight( 8, "Netherite Boots", tier1Color)),
        entry("minecraft:netherite_sword",          new DeathHighlight( 9, "Netherite Sword", tier1Color)),
        entry("minecraft:netherite_pickaxe",        new DeathHighlight(10, "Netherite Pickaxe", tier1Color)),
        entry("minecraft:netherite_shovel",         new DeathHighlight(11, "Netherite Shovel", tier1Color)),
        entry("minecraft:netherite_axe",            new DeathHighlight(12, "Netherite Axe", tier1Color)),
        entry("minecraft:netherite_hoe",            new DeathHighlight(13, "Netherite Hoe", tier1Color)),
        entry("minecraft:netherite_ingot",          new DeathHighlight(14, "Netherite Ingot", tier1Color)),
        entry("minecraft:netherite_scrap",          new DeathHighlight(15, "Netherite Scrap", tier1Color)),
        entry("minecraft:diamond_helmet",           new DeathHighlight(16, "Diamond Helmet", tier2Color, true)),
        entry("minecraft:diamond_chestplate",       new DeathHighlight(17, "Diamond Chestplate", tier2Color, true)),
        entry("minecraft:diamond_leggings",         new DeathHighlight(18, "Diamond Leggings", tier2Color, true)),
        entry("minecraft:diamond_boots",            new DeathHighlight(19, "Diamond Boots", tier2Color, true)),
        entry("minecraft:diamond_sword",            new DeathHighlight(20, "Diamond Sword", tier2Color, true)),
        entry("minecraft:diamond_pickaxe",          new DeathHighlight(21, "Diamond Pickaxe", tier2Color, true)),
        entry("minecraft:diamond_shovel",           new DeathHighlight(22, "Diamond Shovel", tier2Color, true)),
        entry("minecraft:diamond_axe",              new DeathHighlight(23, "Diamond Axe", tier2Color, true)),
        entry("minecraft:diamond_hoe",              new DeathHighlight(24, "Diamond Hoe", tier2Color, true)),
        entry("minecraft:shield",                   new DeathHighlight(25, "Shield", tier2Color, true)),
        entry("minecraft:trident",                  new DeathHighlight(26, "Trident", tier2Color, true)),
        entry("minecraft:crossbow",                 new DeathHighlight(27, "Crossbow", tier2Color, true)),
        entry("minecraft:bow",                      new DeathHighlight(28, "Bow", tier2Color, true)),
        entry("minecraft:diamond",                  new DeathHighlight(29, "Diamond", tier3Color)),
        entry("minecraft:emerald",                  new DeathHighlight(30, "Emerald", tier3Color)),
        entry("minecraft:golden_apple",             new DeathHighlight(31, "Golden Apple", tier3Color)),
        entry("minecraft:enchanted_book",           new DeathHighlight(32, "Enchanted Book", tier3Color)),
        entry("minecraft:blaze_rod",                new DeathHighlight(33, "Blaze Rod")),
        entry("minecraft:raw_gold",                 new DeathHighlight(34, "Raw Gold")),
        entry("minecraft:raw_iron",                 new DeathHighlight(35, "Raw Iron")),
        entry("minecraft:tipped_arrow",             new DeathHighlight(36, "Tipped Arrow")),
        entry("minecraft:potion",                   new DeathHighlight(37, "Potion")),
        entry("minecraft:splash_potion",            new DeathHighlight(38, "Splash Potion")),
        entry("minecraft:lingering_potion",         new DeathHighlight(39, "Lingering Potion"))
    );

    public int order;
    public String name;
    public ChatColor color;
    public boolean enchantedOnly;

    public ItemStack item;

    DeathHighlight(int order, String name, ChatColor color, boolean enchantedOnly, ItemStack item) {
        this.order = order;
        this.name = name;
        this.color = color;
        this.enchantedOnly = enchantedOnly;
        this.item = item;
    }

    DeathHighlight(int order, String name, ChatColor color, boolean enchantedOnly) {
        this(order, name, color, enchantedOnly, null);
    }

    DeathHighlight(int order, String name, ChatColor color) {
        this(order, name, color, false);
    }

    DeathHighlight(int order, String name) {
        this(order, name, defaultColor, false);
    }

    DeathHighlight withItem(ItemStack item) {
        return new DeathHighlight(order, name, color, enchantedOnly, item);
    }
}
