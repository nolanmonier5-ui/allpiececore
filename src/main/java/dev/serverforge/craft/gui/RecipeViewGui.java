package dev.serverforge.craft.gui;

import dev.serverforge.ServerForgePlugin;
import dev.serverforge.craft.model.CraftCategory;
import dev.serverforge.craft.model.Recipe;
import dev.serverforge.util.BackButton;
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

/**
 * Menu dedie a UNE arme/recette. Affiche les ingredients requis, l'arme produite,
 * et permet de la fabriquer en cliquant dessus. Contient le bouton retour configurable
 * (qui ramene au menu de la categorie).
 */
public class RecipeViewGui extends Gui {

    private final ServerForgePlugin plugin;
    private final CraftCategory cat;
    private final Recipe recipe;
    private final boolean admin;

    private static final int[] INGREDIENT_SLOTS = {10, 11, 12, 13, 14, 15, 16};
    private static final int RESULT_SLOT = 31;

    public RecipeViewGui(ServerForgePlugin plugin, Player viewer, CraftCategory cat, Recipe recipe, boolean admin) {
        super(title(recipe), 6);
        this.plugin = plugin; this.cat = cat; this.recipe = recipe; this.admin = admin;
        build();
    }

    private static String title(Recipe r) {
        return r.getMenuTitle();
    }

    @Override
    public void build() {
        clear();

        // Ingredients (affichage informatif : nom custom + quantite + possede/requis)
        List<ItemStack> ings = recipe.getIngredients();
        for (int i = 0; i < INGREDIENT_SLOTS.length; i++) {
            if (i >= ings.size()) continue;
            ItemStack ing = ings.get(i);
            setButton(INGREDIENT_SLOTS[i], ingredientDisplay(ing), null);
        }

        // L'arme produite : clic = fabriquer
        ItemStack result = recipe.getResult();
        if (result == null) {
            setButton(RESULT_SLOT, new ItemBuilder(Material.BARRIER)
                    .name("&cRecette incomplete").lore("&7Aucun item de sortie defini.").build(), null);
        } else {
            ItemStack disp = result.clone();
            ItemMeta meta = disp.getItemMeta();
            List<Component> lore = meta.hasLore() ? new ArrayList<>(meta.lore()) : new ArrayList<>();
            lore.add(Text.item("&8&m                    "));
            if (recipe.getCraftSeconds() > 0)
                lore.add(Text.item("&7Temps de fabrication: &f" + recipe.getCraftSeconds() + "s"));
            lore.add(Text.item("&aClique pour fabriquer"));
            meta.lore(lore);
            disp.setItemMeta(meta);
            setButton(RESULT_SLOT, disp, (p, c) ->
                    plugin.craftService().craft(p, recipe,
                            () -> new RecipeViewGui(plugin, p, cat, recipe, admin).open(p)));
        }

        // Bouton "Fabriquer" explicite (en plus du clic sur l'arme)
        setButton(40, new ItemBuilder(Material.ANVIL)
                .name("&aFabriquer").lore("&7Consomme les ingredients requis.").build(),
                (p, c) -> plugin.craftService().craft(p, recipe,
                        () -> new RecipeViewGui(plugin, p, cat, recipe, admin).open(p)));

        if (admin) {
            setButton(8, new ItemBuilder(Material.WRITABLE_BOOK)
                    .name("&dEditer cette recette").build(),
                    (p, c) -> new RecipeEditGui(plugin, cat, recipe).open(p));
        }

        // Bouton retour configurable -> menu de la categorie
        setButton(BackButton.slot(plugin), BackButton.item(plugin),
                (p, c) -> new CraftCategoryGui(plugin, p, cat.getId(), admin).open(p));
    }

    private ItemStack ingredientDisplay(ItemStack ing) {
        ItemStack disp = ing.clone();
        ItemMeta meta = disp.getItemMeta();
        List<Component> lore = meta.hasLore() ? new ArrayList<>(meta.lore()) : new ArrayList<>();
        lore.add(Text.item("&7Requis: &f" + ing.getAmount()));
        meta.lore(lore);
        disp.setItemMeta(meta);
        return disp;
    }
}
