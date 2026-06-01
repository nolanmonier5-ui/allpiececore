package dev.serverforge.shop.gui;

import dev.serverforge.ServerForgePlugin;
import dev.serverforge.shop.model.ShopCategory;
import dev.serverforge.shop.model.ShopItem;
import dev.serverforge.util.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

/** Edition d'un item du shop : prix de base, % achat/vente, achat/vente on/off, slot, item, reset. */
public class ShopItemEditGui extends Gui {
    private final ServerForgePlugin plugin;
    private final ShopCategory cat;
    private final ShopItem it;

    public ShopItemEditGui(ServerForgePlugin plugin, ShopCategory cat, ShopItem it) {
        super("&8Item du shop", 3);
        this.plugin = plugin; this.cat = cat; this.it = it;
        build();
    }

    @Override
    public void build() {
        clear();
        double ratio = plugin.shop().sellRatio();

        setButton(4, new ItemBuilder(it.getDisplay().getType())
                .modelData(it.customModelData()).hideAttributes()
                .name("&fApercu de l'item")
                .lore("&7Prix achat actuel: &f" + fmt(it.currentBuyPrice()),
                      "&7Prix vente actuel: &f" + fmt(it.currentSellPrice(ratio)),
                      "&eClique pour remplacer l'item (inventaire)").build(),
                (p, c) -> new ItemPicker(p, "&8Nouvel item",
                        s -> { it.setDisplay(s.clone()); plugin.shop().saveAll(); reopen(p); },
                        () -> reopen(p)).open(p));

        setButton(10, new ItemBuilder(Material.GOLD_INGOT)
                .name("&aPrix de base (achat): &f" + fmt(it.getBasePrice()))
                .lore("&7Prix initial, multiplicateur = 1.0",
                      "&7Vente = " + (int)(ratio*100) + "% de l'achat courant",
                      "&eClique pour definir").build(),
                (p, c) -> plugin.chat().request(p, "Prix de base (achat) :",
                        v -> { it.setBasePrice(parse(v)); plugin.shop().saveAll(); reopen(p); }, () -> reopen(p)));

        setButton(12, new ItemBuilder(Material.RED_DYE)
                .name("&c% hausse a l'achat: &f" + pct(it.getBuyStep()))
                .lore("&7A chaque unite achetee, le prix monte de ce %.",
                      "&eClique pour definir").build(),
                (p, c) -> plugin.chat().request(p, "% de hausse a l'achat (ex: 2) :",
                        v -> { it.setBuyStep(parse(v)/100.0); plugin.shop().saveAll(); reopen(p); }, () -> reopen(p)));

        setButton(14, new ItemBuilder(Material.GREEN_DYE)
                .name("&a% baisse a la vente: &f" + pct(it.getSellStep()))
                .lore("&7A chaque unite vendue, le prix baisse de ce %.",
                      "&eClique pour definir").build(),
                (p, c) -> plugin.chat().request(p, "% de baisse a la vente (ex: 2) :",
                        v -> { it.setSellStep(parse(v)/100.0); plugin.shop().saveAll(); reopen(p); }, () -> reopen(p)));

        setButton(16, new ItemBuilder(Material.CLOCK)
                .name("&aReinitialiser le prix maintenant")
                .lore("&7Remet le multiplicateur a 1.0 pour cet item.").build(),
                (p, c) -> { it.resetPrice(); plugin.shop().saveAll(); reopen(p); });

        setButton(19, new ItemBuilder(it.isBuyable() ? Material.LIME_DYE : Material.GRAY_DYE)
                .name("&aAchetable: " + (it.isBuyable() ? "&aoui" : "&cnon")).build(),
                (p, c) -> { it.setBuyable(!it.isBuyable()); plugin.shop().saveAll(); reopen(p); });

        setButton(20, new ItemBuilder(it.isSellable() ? Material.LIME_DYE : Material.GRAY_DYE)
                .name("&aVendable: " + (it.isSellable() ? "&aoui" : "&cnon")).build(),
                (p, c) -> { it.setSellable(!it.isSellable()); plugin.shop().saveAll(); reopen(p); });

        setButton(22, new ItemBuilder(Material.ITEM_FRAME)
                .name("&aEmplacement: &f" + (it.getSlot() < 0 ? "auto" : it.getSlot())).build(),
                (p, c) -> new SlotPicker("&8Emplacement item", it.getSlot(),
                        s -> { it.setSlot(s); plugin.shop().saveAll(); reopen(p); }, () -> reopen(p)).open(p));

        setButton(24, new ItemBuilder(Material.BARRIER).name("&cSupprimer cet item")
                .lore("&cShift + clic pour confirmer").build(),
                (p, c) -> { if (c == ClickType.SHIFT_LEFT || c == ClickType.SHIFT_RIGHT) {
                        cat.removeItem(it.getId()); plugin.shop().saveAll();
                        new ShopCategoryGui(plugin, p, cat.getId(), true).open(p);
                    } });

        setButton(26, new ItemBuilder(Material.ARROW).name("&eRetour").build(),
                (p, c) -> new ShopCategoryGui(plugin, p, cat.getId(), true).open(p));
    }

    private void reopen(Player p) { new ShopItemEditGui(plugin, cat, it).open(p); }
    private double parse(String v) { try { return Double.parseDouble(v.trim().replace(",", ".")); } catch (Exception e) { return 0; } }
    private String pct(double frac) { return String.format("%.2f", frac * 100) + "%"; }
    private String fmt(double v) { return plugin.vault().isEnabled() ? plugin.vault().format(v) : String.format("%.2f", v); }
}
