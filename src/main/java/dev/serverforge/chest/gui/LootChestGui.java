package dev.serverforge.chest.gui;

import dev.serverforge.ServerForgePlugin;
import dev.serverforge.chest.model.LootChest;
import dev.serverforge.chest.model.LootEntry;
import dev.serverforge.util.*;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/** Config d'un coffre a butin : items + % de drop, delai de reset, remplir maintenant. */
public class LootChestGui extends Gui {
    private final ServerForgePlugin plugin;
    private final Location loc;

    public LootChestGui(ServerForgePlugin plugin, Location loc) {
        super("&8Coffre a butin", 6);
        this.plugin = plugin; this.loc = loc;
        build();
    }

    @Override
    public void build() {
        clear();
        LootChest chest = plugin.chests().get(loc);
        if (chest == null) { setButton(22, new ItemBuilder(Material.BARRIER).name("&cCoffre introuvable").build(), null); return; }

        List<LootEntry> loot = chest.getLoot();
        for (int i = 0; i < loot.size() && i < 45; i++) {
            LootEntry e = loot.get(i);
            ItemStack disp = e.getItem().clone();
            ItemMeta meta = disp.getItemMeta();
            List<Component> lore = meta.hasLore() ? new ArrayList<>(meta.lore()) : new ArrayList<>();
            lore.add(Text.item("&7Chance: &e" + e.getChance() + "%"));
            lore.add(Text.item("&eClic gauche &7> changer la chance"));
            lore.add(Text.item("&cShift + droit &7> retirer"));
            meta.lore(lore); disp.setItemMeta(meta);
            setButton(i, disp, (p, c) -> {
                if (c == ClickType.SHIFT_RIGHT) { loot.remove(e); plugin.chests().saveAll(); build(); }
                else plugin.chat().request(p, "Chance de drop en % (0-100) :",
                        v -> { try { e.setChance(Double.parseDouble(v.trim().replace(",", "."))); } catch (Exception ignored) {}
                               plugin.chests().saveAll(); new LootChestGui(plugin, loc).open(p); },
                        () -> new LootChestGui(plugin, loc).open(p));
            });
        }

        setButton(47, new ItemBuilder(Material.HOPPER).name("&aAjouter un item").glow(true)
                .lore("&7Choisis un item de ton inventaire + sa chance").build(),
                (p, c) -> new ItemPicker(p, "&8Item du coffre",
                        s -> { ItemStack copy = s.clone();
                            plugin.chat().request(p, "Chance de drop en % (0-100) :",
                                v -> { double ch = 100; try { ch = Double.parseDouble(v.trim().replace(",", ".")); } catch (Exception ignored) {}
                                       chest.getLoot().add(new LootEntry(copy, ch)); plugin.chests().saveAll();
                                       new LootChestGui(plugin, loc).open(p); },
                                () -> new LootChestGui(plugin, loc).open(p)); },
                        () -> new LootChestGui(plugin, loc).open(p)).open(p));

        String resetTxt = chest.getResetSeconds() <= 0 ? "&cjamais"
                : "&f" + TimeUtil.format(chest.getResetSeconds() * 1000L);
        setButton(49, new ItemBuilder(Material.CLOCK)
                .name("&aReset: " + resetTxt)
                .lore("&7Delai avant que le coffre se reremplisse.",
                      "&eClic gauche &7> definir (ex: 24h, 30m)",
                      "&cClic droit &7> jamais").build(),
                (p, c) -> {
                    if (c == ClickType.RIGHT) { chest.setResetSeconds(0); plugin.chests().saveAll(); new LootChestGui(plugin, loc).open(p); }
                    else plugin.chat().request(p, "Delai de reset (ex: 24h, 1d, 30m) :",
                            v -> { chest.setResetSeconds(TimeUtil.parseSeconds(v)); plugin.chests().saveAll();
                                   new LootChestGui(plugin, loc).open(p); },
                            () -> new LootChestGui(plugin, loc).open(p));
                });

        setButton(51, new ItemBuilder(Material.LIME_CONCRETE).name("&aRemplir maintenant")
                .lore("&7Force le remplissage du coffre tout de suite.").build(),
                (p, c) -> { plugin.chests().tryFill(loc.getBlock(), true);
                            p.sendMessage(Text.color("&aCoffre rempli.")); });

        setButton(53, new ItemBuilder(Material.BARRIER).name("&cSupprimer ce coffre a butin")
                .lore("&7Le redevient un coffre normal.", "&cShift + clic pour confirmer").build(),
                (p, c) -> { if (c == ClickType.SHIFT_LEFT || c == ClickType.SHIFT_RIGHT) {
                        plugin.chests().remove(loc); p.closeInventory();
                        p.sendMessage(Text.color("&cCoffre a butin supprime."));
                    } });
    }
}
