package dev.serverforge.chest;

import dev.serverforge.ServerForgePlugin;
import dev.serverforge.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

/** L'outil de configuration des coffres a butin (un blaze rod tague). */
public class ChestTool {
    private final ServerForgePlugin plugin;
    private final NamespacedKey key;

    public ChestTool(ServerForgePlugin plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "loot_tool");
    }

    public ItemStack create() {
        ItemStack tool = new ItemBuilder(Material.BLAZE_ROD)
                .name("&6Outil Coffre a Butin")
                .lore("&7Clic droit sur un coffre &f> configurer / remplir",
                      "&7Clic gauche sur un coffre &f> copier la config",
                      "&7Shift + clic gauche &f> coller la config",
                      "&8Le coffre se reinitialise selon le delai choisi.")
                .glow(true).build();
        var meta = tool.getItemMeta();
        meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
        tool.setItemMeta(meta);
        return tool;
    }

    public boolean isTool(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) return false;
        Byte b = stack.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.BYTE);
        return b != null && b == 1;
    }
}
