package dev.serverforge.craft.gui;

import dev.serverforge.ServerForgePlugin;
import dev.serverforge.craft.model.CraftCategory;
import dev.serverforge.craft.model.Recipe;
import dev.serverforge.util.Gui;
import dev.serverforge.util.ItemBuilder;
import dev.serverforge.util.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/** Menu d'une categorie : recettes craftables. */
public class CraftCategoryGui extends Gui {
    private final ServerForgePlugin plugin;
    private final String categoryId;
    private final boolean admin;

    public CraftCategoryGui(ServerForgePlugin plugin, Player viewer, String categoryId, boolean admin) {
        super(plugin.craft().get(categoryId) != null ? plugin.craft().get(categoryId).getMenuTitle() : "&8Craft", 6);
        this.plugin = plugin; this.categoryId = categoryId; this.admin = admin;
        build();
    }

    @Override
    public void build() {
        clear();
        CraftCategory cat = plugin.craft().get(categoryId);
        if (cat == null) { setButton(22, new ItemBuilder(Material.BARRIER).name("&cIntrouvable").build(), null); return; }

        boolean[] used = new boolean[54];
        int backSlot = dev.serverforge.util.BackButton.slot(plugin);
        used[backSlot] = true;
        if (admin) used[48] = true;
        List<Recipe> recipes = cat.recipeList();
        for (Recipe r : recipes) {
            int slot = r.getSlot();
            if (slot >= 0 && slot <= 47 && !used[slot]) { place(cat, r, slot); used[slot] = true; }
        }
        for (Recipe r : recipes) {
            if (r.getSlot() >= 0 && r.getSlot() <= 47) continue;
            int free = -1; for (int i = 0; i <= 47; i++) if (!used[i]) { free = i; break; }
            if (free < 0) break;
            place(cat, r, free); used[free] = true;
        }
        if (recipes.isEmpty())
            setButton(22, new ItemBuilder(Material.BARRIER).name("&7Aucune recette").build(), null);

        if (admin) {
            setButton(48, new ItemBuilder(Material.NETHER_STAR).name("&aCreer une recette").glow(true).build(),
                    (p, c) -> plugin.chat().request(p, "Nom de la recette :",
                            name -> { Recipe r = Recipe.create(name); cat.addRecipe(r); plugin.craft().saveAll();
                                      new RecipeEditGui(plugin, cat, r).open(p); },
                            () -> new CraftCategoryGui(plugin, p, categoryId, true).open(p)));
        }
        setButton(backSlot, dev.serverforge.util.BackButton.item(plugin),
                (p, c) -> new CraftMainGui(plugin, p, admin).open(p));
    }

    private void place(CraftCategory cat, Recipe r, int slot) {
        ItemStack disp = r.getResult() != null ? r.getResult().clone() : new ItemBuilder(Material.BARRIER).name("&c" + r.getName()).build();
        ItemMeta meta = disp.getItemMeta();
        List<Component> lore = meta.hasLore() ? new ArrayList<>(meta.lore()) : new ArrayList<>();
        lore.add(Text.item("&8&m                    "));
        lore.add(Text.item("&7Ingredients:"));
        for (ItemStack ing : r.getIngredients())
            lore.add(Text.item(" &8- &f" + ing.getAmount() + "x " + displayName(ing)));
        if (r.getIngredients().isEmpty()) lore.add(Text.item(" &8(aucun)"));
        if (r.getCraftSeconds() > 0) lore.add(Text.item("&7Temps: &f" + r.getCraftSeconds() + "s"));
        lore.add(Text.item(""));
        lore.add(Text.item("&aClic gauche &7> fabriquer"));
        if (admin) lore.add(Text.item("&dClic droit &7> editer"));
        meta.lore(lore);
        disp.setItemMeta(meta);

        setButton(slot, disp, (p, c) -> {
            if (admin && c.isRightClick()) { new RecipeEditGui(plugin, cat, r).open(p); return; }
            plugin.craftService().craft(p, r, () -> new CraftCategoryGui(plugin, p, categoryId, admin).open(p));
        });
    }

    /** Nom affiche d'un ingredient : son nom custom s'il en a un, sinon le materiau. */
    private String displayName(ItemStack s) {
        if (s != null && s.hasItemMeta()) {
            ItemMeta meta = s.getItemMeta();
            if (meta.hasDisplayName()) {
                Component dn = meta.displayName();
                if (dn != null) {
                    // Reconvertit le nom custom en codes '&' pour l'afficher dans la lore.
                    return LegacyComponentSerializer.legacyAmpersand().serialize(dn);
                }
            }
        }
        return pretty(s);
    }

    private String pretty(ItemStack s) {
        int cmd = dev.serverforge.util.ItemUtil.readModelData(s);
        String n = s.getType().name().toLowerCase().replace('_', ' ');
        return cmd >= 0 ? n + " (cmd " + cmd + ")" : n;
    }
}
