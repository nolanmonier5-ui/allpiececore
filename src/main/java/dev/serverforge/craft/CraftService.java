package dev.serverforge.craft;

import dev.serverforge.ServerForgePlugin;
import dev.serverforge.craft.model.Recipe;
import dev.serverforge.util.ItemUtil;
import dev.serverforge.util.Text;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Logique de fabrication : verif ingredients, temps de craft, remise de l'item. */
public class CraftService {
    private final ServerForgePlugin plugin;
    private final Set<UUID> crafting = new HashSet<>(); // joueurs en cours de craft

    public CraftService(ServerForgePlugin plugin) { this.plugin = plugin; }

    public boolean hasIngredients(Player p, Recipe r) {
        for (ItemStack ing : r.getIngredients()) {
            if (ItemUtil.count(p, ing) < ing.getAmount()) return false;
        }
        return true;
    }

    public void craft(Player p, Recipe r, Runnable onDone) {
        if (r.getResult() == null) { p.sendMessage(Text.color("&cRecette sans item de sortie.")); return; }
        if (crafting.contains(p.getUniqueId())) { p.sendMessage(Text.color("&cTu fabriques deja quelque chose.")); return; }
        if (!hasIngredients(p, r)) { p.sendMessage(Text.color("&cIngredients insuffisants.")); return; }

        // Consomme les ingredients immediatement
        for (ItemStack ing : r.getIngredients()) {
            if (!ItemUtil.remove(p, ing, ing.getAmount())) {
                p.sendMessage(Text.color("&cErreur: ingredient manquant."));
                return;
            }
        }

        long seconds = r.getCraftSeconds();
        if (seconds <= 0) {
            ItemUtil.give(p, r.getResult().clone());
            p.sendMessage(Text.color("&aFabrique: &f" + r.getName()));
            if (onDone != null) onDone.run();
            return;
        }

        crafting.add(p.getUniqueId());
        p.closeInventory();
        p.sendMessage(Text.color("&7Fabrication de &f" + r.getName() + " &7(" + seconds + "s)..."));
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            crafting.remove(p.getUniqueId());
            if (!p.isOnline()) return;
            ItemUtil.give(p, r.getResult().clone());
            p.sendMessage(Text.color("&aFabrication terminee: &f" + r.getName()));
        }, seconds * 20L);
    }

    public boolean isCrafting(UUID u) { return crafting.contains(u); }
}
