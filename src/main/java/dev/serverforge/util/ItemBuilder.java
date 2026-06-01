package dev.serverforge.util;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Builder fluide pour les items de GUI. */
public class ItemBuilder {
    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material == null ? Material.STONE : material);
        this.meta = item.getItemMeta();
    }
    public ItemBuilder name(String s) { meta.displayName(Text.item(s)); return this; }
    public ItemBuilder lore(String... lines) { return lore(Arrays.asList(lines)); }
    public ItemBuilder lore(List<String> lines) {
        List<Component> out = new ArrayList<>();
        for (String l : lines) out.add(Text.item(l));
        meta.lore(out); return this;
    }
    public ItemBuilder amount(int a) { item.setAmount(Math.max(1, Math.min(64, a))); return this; }
    public ItemBuilder modelData(int cmd) {
        if (cmd >= 0) { try { meta.setCustomModelData(cmd); } catch (Throwable ignored) {} }
        return this;
    }
    public ItemBuilder glow(boolean glow) {
        if (glow) { meta.addEnchant(Enchantment.UNBREAKING, 1, true); meta.addItemFlags(ItemFlag.HIDE_ENCHANTS); }
        return this;
    }
    public ItemBuilder hideAttributes() {
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        return this;
    }
    public ItemStack build() { item.setItemMeta(meta); return item; }
}
