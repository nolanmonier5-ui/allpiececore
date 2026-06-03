package dev.serverforge.shop.gui;

import dev.serverforge.ServerForgePlugin;
import dev.serverforge.shop.model.ShopCategory;
import dev.serverforge.shop.model.ShopItem;
import dev.serverforge.util.ItemPicker;
import org.bukkit.entity.Player;

/** Choisit un item de l'inventaire a ajouter au shop, puis ouvre son edition. */
public class ShopItemPickerGui {
    public static void open(ServerForgePlugin plugin, Player p, ShopCategory cat) {
        new ItemPicker(p, "&8Ajouter un item au shop",
                stack -> {
                    ShopItem it = ShopItem.create(stack.clone(), 10,
                            plugin.shop().defaultBuyStep(), plugin.shop().defaultSellStep());
                    cat.addItem(it);
                    plugin.shop().saveAll();
                    new ShopItemEditGui(plugin, cat, it).open(p);
                },
                () -> new ShopCategoryGui(plugin, p, cat.getId(), true).open(p)
        ).open(p);
    }
    // wrapper instance pour appel uniforme
    private final ServerForgePlugin plugin; private final Player p; private final ShopCategory cat;
    public ShopItemPickerGui(ServerForgePlugin plugin, Player p, ShopCategory cat) {
        this.plugin = plugin; this.p = p; this.cat = cat;
    }
    public void open(Player player) { open(plugin, player, cat); }
}
