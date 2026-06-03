package dev.serverforge.util;

import dev.serverforge.ServerForgePlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

/** Configure la fleche retour (item, CustomModelData, nom, slot). Partagee shop + craft. */
public class BackButtonGui extends Gui {
    private final ServerForgePlugin plugin;
    private final Runnable back;

    public BackButtonGui(ServerForgePlugin plugin, Runnable back) {
        super("&8Fleche retour", 3);
        this.plugin = plugin;
        this.back = back;
        build();
    }

    @Override
    public void build() {
        clear();

        Material mat = Material.matchMaterial(plugin.backMaterial());
        setButton(11, new ItemBuilder(mat != null ? mat : Material.ARROW)
                .modelData(plugin.backModelData()).hideAttributes()
                .name("&dItem: &f" + plugin.backMaterial())
                .lore("&7CustomModelData: &f" + (plugin.backModelData() < 0 ? "aucun" : plugin.backModelData()),
                      "&eClic gauche &7> choisir dans l'inventaire",
                      "&cClic droit &7> defaut (ARROW)").build(),
                (p, c) -> {
                    if (c == ClickType.RIGHT) { plugin.setBackMaterial("ARROW"); plugin.setBackModelData(-1); reopen(p); return; }
                    new ItemPicker(p, "&8Item de la fleche retour",
                            s -> { plugin.setBackMaterial(s.getType().name());
                                   plugin.setBackModelData(ItemUtil.readModelData(s)); reopen(p); },
                            () -> reopen(p)).open(p);
                });

        setButton(13, new ItemBuilder(Material.NAME_TAG)
                .name("&aNom: &f" + plugin.backName()).lore("&eClique pour modifier").build(),
                (p, c) -> plugin.chat().request(p, "Nom du bouton retour :",
                        v -> { plugin.setBackName(v); reopen(p); }, () -> reopen(p)));

        setButton(15, new ItemBuilder(Material.ITEM_FRAME)
                .name("&aEmplacement: &f" + plugin.backSlot())
                .lore("&7Slot du bouton retour dans les menus de categorie.",
                      "&eClique pour choisir").build(),
                (p, c) -> new SlotPicker("&8Emplacement fleche retour", plugin.backSlot(),
                        s -> { plugin.setBackSlot(s < 0 ? 49 : s); reopen(p); }, () -> reopen(p)).open(p));

        setButton(22, new ItemBuilder(Material.BARRIER).name("&cRetour").build(),
                (p, c) -> { if (back != null) back.run(); });
    }
    private void reopen(Player p) { new BackButtonGui(plugin, back).open(p); }
}
