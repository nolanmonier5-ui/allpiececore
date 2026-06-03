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
 * Menu dedie a UNE arme : uniquement l'arme (cliquable pour fabriquer) et le bouton retour.
 * Emplacement de l'arme, titre du menu et bouton retour 100% configurables.
 */
public class RecipeViewGui extends Gui {

    private final ServerForgePlugin plugin;
    private final CraftCategory cat;
    private final Recipe recipe;
    private final boolean admin;

    public RecipeViewGui(ServerForgePlugin plugin, Player viewer, CraftCategory cat, Recipe recipe, boolean admin) {
        super(recipe.getMenuTitle(), 6);
        this.plugin = plugin; this.cat = cat; this.recipe = recipe; this.admin = admin;
        build();
    }

    @Override
    public void build() {
        clear();

        int backSlot = BackButton.slot(plugin);
        int weaponSlot = recipe.getViewSlot();
        if (weaponSlot < 0 || weaponSlot > 53) weaponSlot = 22;
        if (weaponSlot == backSlot) weaponSlot = (backSlot == 22) ? 13 : 22; // evite la collision

        ItemStack result = recipe.getResult();
        if (result == null) {
            setButton(weaponSlot, new ItemBuilder(Material.BARRIER)
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
            setButton(weaponSlot, disp, (p, c) ->
                    plugin.craftService().craft(p, recipe,
                            () -> new RecipeViewGui(plugin, p, cat, recipe, admin).open(p)));
        }

        setButton(backSlot, BackButton.item(plugin),
                (p, c) -> new CraftCategoryGui(plugin, p, cat.getId(), admin).open(p));
    }
}
