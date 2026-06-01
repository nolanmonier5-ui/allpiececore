package dev.serverforge.craft.gui;

import dev.serverforge.ServerForgePlugin;
import dev.serverforge.craft.model.CraftCategory;
import dev.serverforge.craft.model.Recipe;
import dev.serverforge.util.Gui;
import dev.serverforge.util.ItemBuilder;
import dev.serverforge.util.Text;
import net.kyori.adventure.text.Component;
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
        super(plugin.craft().get(categoryId) != null ? "&8" + plugin.craft().get(categoryId).getName() : "&8Craft", 6);
        this.plugin = plugin; this.categoryId = categoryId; this.admin = admin;
        build();
    }

    @Override
    public void build() {
        clear();
        CraftCategory cat = plugin.craft().get(categoryId);
        if (cat == null) { setButton(22, new ItemBuilder(Material.BARRIER).name("&cIntrouvable").build(), null); return; }

        boolean[] used = new boolean[54];
        used[49] = true;
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
        setButton(49, new ItemBuilder(Material.ARROW).name("&eRetour").build(),
                (p, c) -> new CraftMainGui(plugin, p, admin).open(p));
    }

    private void place(CraftCategory cat, Recipe r, int slot) {
        ItemStack disp = r.getResult() != null ? r.getResult().clone() : new ItemBuilder(Material.BARRIER).name("&c" + r.getName()).build();
        ItemMeta meta = disp.getItemMeta();
        List<Component> lore = meta.hasLore() ? new ArrayList<>(meta.lore()) : new ArrayList<>();
        lore.add(Text.item("&8&m                    "));
        lore.add(Text.item("&7Ingredients:"));
        for (ItemStack ing : r.getIngredients())
            lore.add(Text.item(" &8- &f" + ing.getAmount() + "x " + pretty(ing)));
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

    private String pretty(ItemStack s) {
        int cmd = dev.serverforge.util.ItemUtil.readModelData(s);
        String n = s.getType().name().toLowerCase().replace('_', ' ');
        return cmd >= 0 ? n + " (cmd " + cmd + ")" : n;
    }
}
