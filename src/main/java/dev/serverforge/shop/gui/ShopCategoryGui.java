package dev.serverforge.shop.gui;

import dev.serverforge.ServerForgePlugin;
import dev.serverforge.shop.model.ShopCategory;
import dev.serverforge.shop.model.ShopItem;
import dev.serverforge.util.Gui;
import dev.serverforge.util.ItemBuilder;
import dev.serverforge.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/** Menu d'une categorie : acheter / vendre avec prix dynamiques. */
public class ShopCategoryGui extends Gui {
    private final ServerForgePlugin plugin;
    private final String categoryId;
    private final boolean admin;

    public ShopCategoryGui(ServerForgePlugin plugin, Player viewer, String categoryId, boolean admin) {
        super(plugin.shop().get(categoryId) != null
                ? plugin.shop().get(categoryId).getMenuTitle() : "&8Shop", 6);
        this.plugin = plugin;
        this.categoryId = categoryId;
        this.admin = admin;
        build();
    }

    @Override
    public void build() {
        clear();
        ShopCategory cat = plugin.shop().get(categoryId);
        if (cat == null) { setButton(22, new ItemBuilder(Material.BARRIER).name("&cIntrouvable").build(), null); return; }

        double ratio = plugin.shop().sellRatio();
        boolean[] used = new boolean[54];
        used[49] = true; // retour

        List<ShopItem> items = cat.itemList();
        for (ShopItem it : items) {
            int slot = it.getSlot();
            if (slot >= 0 && slot <= 47 && !used[slot]) { place(cat, it, slot, ratio); used[slot] = true; }
        }
        for (ShopItem it : items) {
            if (it.getSlot() >= 0 && it.getSlot() <= 47) continue;
            int free = -1; for (int i = 0; i <= 47; i++) if (!used[i]) { free = i; break; }
            if (free < 0) break;
            place(cat, it, free, ratio); used[free] = true;
        }

        if (items.isEmpty())
            setButton(22, new ItemBuilder(Material.BARRIER).name("&7Categorie vide").build(), null);

        if (admin) {
            setButton(48, new ItemBuilder(Material.NETHER_STAR).name("&aAjouter un item").glow(true)
                    .lore("&7Choisis un item de ton inventaire").build(),
                    (p, c) -> new ShopItemPickerGui(plugin, p, cat).open(p));
            setButton(50, new ItemBuilder(Material.COMPARATOR).name("&aReglages categorie").build(),
                    (p, c) -> new ShopCategoryEditGui(plugin, cat).open(p));
        }

        setButton(49, new ItemBuilder(Material.ARROW).name("&eRetour").build(),
                (p, c) -> new ShopMainGui(plugin, p, admin).open(p));
    }

    private void place(ShopCategory cat, ShopItem it, int slot, double ratio) {
        double buy = it.currentBuyPrice();
        double sell = it.currentSellPrice(ratio);
        ItemStack disp = it.getDisplay().clone();
        ItemMeta meta = disp.getItemMeta();
        List<Component> lore = meta.hasLore() ? new ArrayList<>(meta.lore()) : new ArrayList<>();
        lore.add(Text.item("&8&m                    "));
        if (it.isBuyable()) lore.add(Text.item("&aAchat: &f" + fmt(buy) + " &7(clic gauche)"));
        else lore.add(Text.item("&8Achat indisponible"));
        if (it.isSellable()) lore.add(Text.item("&cVente: &f" + fmt(sell) + " &7(clic droit)"));
        else lore.add(Text.item("&8Vente indisponible"));
        lore.add(Text.item("&8Shift = x64  |  prix dynamique"));
        int pct = (int) Math.round((it.getMultiplier() - 1.0) * 100);
        lore.add(Text.item("&7Tendance: " + (pct >= 0 ? "&c+" : "&a") + pct + "%"));
        if (admin) {
            lore.add(Text.item("&8&m                    "));
            lore.add(Text.item("&dAdmin: &7touche &fQ &7(drop) > editer"));
        }
        meta.lore(lore);
        disp.setItemMeta(meta);

        setButton(slot, disp, (p, c) -> {
            if (admin && (c == ClickType.DROP || c == ClickType.CONTROL_DROP)) {
                new ShopItemEditGui(plugin, cat, it).open(p);
                return;
            }
            int qty = (c.isShiftClick()) ? 64 : 1;
            if (c.isLeftClick() && it.isBuyable()) plugin.shopService().buy(p, cat, it, qty);
            else if (c.isRightClick() && it.isSellable()) plugin.shopService().sell(p, cat, it, qty);
            build();
        });
    }

    private String fmt(double v) {
        return plugin.vault().isEnabled() ? plugin.vault().format(v) : String.format("%.2f", v);
    }
}
