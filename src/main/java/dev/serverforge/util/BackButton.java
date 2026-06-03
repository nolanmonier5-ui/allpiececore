package dev.serverforge.util;

import dev.serverforge.ServerForgePlugin;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/** Construit la fleche "retour" configurable (partagee shop + craft). */
public final class BackButton {
    private BackButton() {}

    public static int slot(ServerForgePlugin plugin) { return plugin.backSlot(); }

    public static ItemStack item(ServerForgePlugin plugin) {
        Material mat = Material.matchMaterial(plugin.backMaterial());
        return new ItemBuilder(mat != null ? mat : Material.ARROW)
                .name(plugin.backName())
                .modelData(plugin.backModelData())
                .hideAttributes()
                .build();
    }
}
