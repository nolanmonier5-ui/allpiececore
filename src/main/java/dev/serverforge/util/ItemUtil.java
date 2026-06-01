package dev.serverforge.util;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

/** Helpers items : CustomModelData, don, comptage et retrait par similarite (type+CMD). */
public final class ItemUtil {
    private ItemUtil() {}

    public static int readModelData(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) return -1;
        ItemMeta meta = stack.getItemMeta();
        try { if (meta.hasCustomModelData()) return meta.getCustomModelData(); }
        catch (Throwable ignored) {}
        return -1;
    }

    public static void give(Player player, ItemStack item) {
        if (item == null) return;
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(item.clone());
        for (ItemStack rest : leftover.values())
            player.getWorld().dropItemNaturally(player.getLocation(), rest);
    }

    public static int count(Player player, ItemStack like) {
        int total = 0; int cmd = readModelData(like);
        for (ItemStack s : player.getInventory().getStorageContents()) {
            if (s == null || s.getType() != like.getType() || readModelData(s) != cmd) continue;
            total += s.getAmount();
        }
        return total;
    }

    public static boolean remove(Player player, ItemStack like, int amount) {
        if (count(player, like) < amount) return false;
        int cmd = readModelData(like); int remaining = amount;
        ItemStack[] contents = player.getInventory().getStorageContents();
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack s = contents[i];
            if (s == null || s.getType() != like.getType() || readModelData(s) != cmd) continue;
            int take = Math.min(remaining, s.getAmount());
            s.setAmount(s.getAmount() - take); remaining -= take;
            if (s.getAmount() <= 0) contents[i] = null;
        }
        player.getInventory().setStorageContents(contents);
        return true;
    }
}
