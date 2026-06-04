package dev.serverforge.shop.gui;

import dev.serverforge.ServerForgePlugin;
import dev.serverforge.shop.model.ShopCategory;
import dev.serverforge.util.Gui;
import dev.serverforge.util.ItemBuilder;
import org.bukkit.Material;

import java.util.function.Consumer;

/** Selecteur de categorie (pour rattacher une icone du menu principal). */
public class ShopPickCategoryGui extends Gui {
    private final ServerForgePlugin plugin;
    private final Consumer<ShopCategory> onPick;
    private final Runnable onCancel;

    public ShopPickCategoryGui(ServerForgePlugin plugin, Consumer<ShopCategory> onPick, Runnable onCancel) {
        super("&8Choisir la categorie", 6);
        this.plugin = plugin; this.onPick = onPick; this.onCancel = onCancel;
        build();
    }

    @Override
    public void build() {
        clear();
        int slot = 0;
        for (ShopCategory cat : plugin.shop().all()) {
            if (slot >= 45) break;
            Material mat = Material.matchMaterial(cat.getIconMaterial());
            setButton(slot, new ItemBuilder(mat != null ? mat : Material.CHEST)
                    .name("&6" + cat.getName())
                    .modelData(cat.getIconModelData()).hideAttributes()
                    .lore("&7Id: &f" + cat.getId(), "&eClique pour choisir").build(),
                    (p, c) -> onPick.accept(cat));
            slot++;
        }
        setButton(49, new ItemBuilder(Material.BARRIER).name("&cAnnuler").build(), (p, c) -> onCancel.run());
    }
}
