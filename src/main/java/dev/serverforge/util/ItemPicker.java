package dev.serverforge.util;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Selecteur d'item : affiche l'inventaire du joueur, il clique l'item voulu. */
public class ItemPicker extends Gui {
    private final Player viewer;
    private final Consumer<ItemStack> onPick;
    private final Runnable onCancel;

    public ItemPicker(Player viewer, String title, Consumer<ItemStack> onPick, Runnable onCancel) {
        super(title, 6);
        this.viewer = viewer;
        this.onPick = onPick;
        this.onCancel = onCancel;
        build();
    }

    @Override
    public void build() {
        clear();
        ItemStack[] contents = viewer.getInventory().getStorageContents();
        boolean any = false;
        for (int i = 0; i < 36 && i < contents.length; i++) {
            ItemStack src = contents[i];
            if (src == null || src.getType() == Material.AIR) continue;
            any = true;
            final ItemStack chosen = src.clone();
            setButton(i, withHint(src), (p, c) -> onPick.accept(chosen));
        }
        if (!any) setButton(22, new ItemBuilder(Material.BARRIER)
                .name("&cInventaire vide").lore("&7Prends l'item voulu puis reviens.").build(), null);
        setButton(49, new ItemBuilder(Material.BARRIER).name("&cAnnuler").build(), (p, c) -> onCancel.run());
    }

    private ItemStack withHint(ItemStack src) {
        ItemStack s = src.clone();
        ItemMeta meta = s.getItemMeta();
        List<Component> lore = meta.hasLore() ? new ArrayList<>(meta.lore()) : new ArrayList<>();
        lore.add(Text.item("&e\u25B6 Clique pour choisir"));
        meta.lore(lore);
        s.setItemMeta(meta);
        return s;
    }
}
