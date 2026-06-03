package dev.serverforge.shop;

import dev.serverforge.ServerForgePlugin;
import dev.serverforge.shop.model.ShopCategory;
import dev.serverforge.shop.model.ShopItem;
import dev.serverforge.util.ItemUtil;
import dev.serverforge.util.Text;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** Logique d'achat / vente avec prix dynamique. */
public class ShopService {
    private final ServerForgePlugin plugin;
    public ShopService(ServerForgePlugin plugin) { this.plugin = plugin; }

    public void buy(Player p, ShopCategory cat, ShopItem it, int qty) {
        if (!plugin.vault().isEnabled()) { p.sendMessage(Text.color("&cEconomie indisponible (Vault).")); return; }
        if (qty <= 0) return;

        double min = plugin.shop().minMultiplier(), max = plugin.shop().maxMultiplier();
        // Cout total : on simule l'achat unite par unite (le prix monte au fur et a mesure).
        double total = 0; double mult = it.getMultiplier();
        for (int i = 0; i < qty; i++) {
            total += it.getBasePrice() * mult;
            mult = Math.min(max, mult + it.getBuyStep());
        }
        if (!plugin.vault().has(p, total)) {
            p.sendMessage(Text.color("&cIl te faut &f" + plugin.vault().format(total) + " &c(solde: "
                    + plugin.vault().format(plugin.vault().balance(p)) + ")."));
            return;
        }
        plugin.vault().withdraw(p, total);
        ItemStack give = it.getDisplay().clone();
        give.setAmount(qty);
        ItemUtil.give(p, give);
        it.onBuy(qty, min, max);
        plugin.shop().saveAll();
        p.sendMessage(Text.color("&aAchat: &f" + qty + "x &apour &f" + plugin.vault().format(total)));
    }

    public void sell(Player p, ShopCategory cat, ShopItem it, int qty) {
        if (!plugin.vault().isEnabled()) { p.sendMessage(Text.color("&cEconomie indisponible (Vault).")); return; }
        int have = ItemUtil.count(p, it.getDisplay());
        if (have <= 0) { p.sendMessage(Text.color("&cTu n'as pas cet item.")); return; }
        int sellQty = Math.min(qty, have);

        double min = plugin.shop().minMultiplier(), max = plugin.shop().maxMultiplier();
        double ratio = plugin.shop().sellRatio();
        // Gain total : prix baisse au fur et a mesure.
        double total = 0; double mult = it.getMultiplier();
        for (int i = 0; i < sellQty; i++) {
            total += it.getBasePrice() * mult * ratio;
            mult = Math.max(min, mult - it.getSellStep());
        }
        if (!ItemUtil.remove(p, it.getDisplay(), sellQty)) {
            p.sendMessage(Text.color("&cVente impossible.")); return;
        }
        plugin.vault().deposit(p, total);
        it.onSell(sellQty, min, max);
        plugin.shop().saveAll();
        p.sendMessage(Text.color("&cVente: &f" + sellQty + "x &cpour &f" + plugin.vault().format(total)));
    }
}
