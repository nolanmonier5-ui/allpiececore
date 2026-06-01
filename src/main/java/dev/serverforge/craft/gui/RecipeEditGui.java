package dev.serverforge.craft.gui;

import dev.serverforge.ServerForgePlugin;
import dev.serverforge.craft.model.CraftCategory;
import dev.serverforge.craft.model.Recipe;
import dev.serverforge.util.Gui;
import dev.serverforge.util.ItemBuilder;
import dev.serverforge.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Edition d'une recette EN PLACANT les items :
 * - Slots 10-16 : grille d'ingredients (place tes items depuis l'inventaire)
 * - Slot 24     : item de sortie (place l'item a fabriquer, CMD/lore conserves)
 * Le contenu place dans ces slots est lu et sauvegarde au clic "Enregistrer" ou a la fermeture.
 *
 * La quantite de chaque ingredient = la taille de la pile placee.
 */
public class RecipeEditGui extends Gui {

    private final ServerForgePlugin plugin;
    private final CraftCategory cat;
    private final Recipe recipe;

    private static final int[] INGREDIENT_SLOTS = {10, 11, 12, 13, 14, 15, 16};
    private static final int RESULT_SLOT = 24;

    public RecipeEditGui(ServerForgePlugin plugin, CraftCategory cat, Recipe recipe) {
        super("&8Recette: &7" + recipe.getName(), 4);
        this.plugin = plugin; this.cat = cat; this.recipe = recipe;
        build();
    }

    @Override
    public void build() {
        clear();

        // Pre-remplit la grille avec les ingredients existants (tagues "fantome" : non restituables)
        for (int i = 0; i < INGREDIENT_SLOTS.length; i++) {
            ItemStack ing = i < recipe.getIngredients().size() ? recipe.getIngredients().get(i) : null;
            inventory.setItem(INGREDIENT_SLOTS[i], ing == null ? null : ghost(ing.clone()));
        }
        ItemStack res = recipe.getResult();
        inventory.setItem(RESULT_SLOT, res == null ? null : ghost(res.clone()));

        // Etiquettes (non cliquables, slots libres pour le depot restent vides au-dessus)
        setButton(1, new ItemBuilder(Material.CRAFTING_TABLE)
                .name("&ePlace les INGREDIENTS").lore("&7Dans les cases ci-dessous (10-16).",
                      "&7La quantite = taille de la pile.").build(), null);
        setButton(20, new ItemBuilder(Material.ARROW).name("&7=>").build(), null);
        setButton(22, new ItemBuilder(Material.PAPER)
                .name("&ePlace l'ITEM A FABRIQUER").lore("&7Dans la case a droite (slot 24).",
                      "&7CMD / nom / lore conserves.").build(), null);

        setButton(28, new ItemBuilder(Material.CLOCK)
                .name("&aTemps de craft: &f" + recipe.getCraftSeconds() + "s")
                .lore("&eClique pour definir (ex: 5, 30, 60)").build(),
                (p, c) -> { sync(); plugin.chat().request(p, "Temps de fabrication en secondes :",
                        v -> { try { recipe.setCraftSeconds(Long.parseLong(v.trim())); } catch (Exception ignored) {}
                               plugin.craft().saveAll(); reopen(p); }, () -> reopen(p)); });

        setButton(29, new ItemBuilder(Material.NAME_TAG)
                .name("&aNom: &f" + recipe.getName()).build(),
                (p, c) -> { sync(); plugin.chat().request(p, "Nom de la recette :",
                        v -> { recipe.setName(v); plugin.craft().saveAll(); reopen(p); }, () -> reopen(p)); });

        setButton(31, new ItemBuilder(Material.LIME_CONCRETE)
                .name("&aEnregistrer").lore("&7Lit les items places et sauvegarde.").build(),
                (p, c) -> { sync(); p.sendMessage(Text.color("&aRecette enregistree.")); reopen(p); });

        setButton(33, new ItemBuilder(Material.ITEM_FRAME)
                .name("&aEmplacement: &f" + (recipe.getSlot() < 0 ? "auto" : recipe.getSlot())).build(),
                (p, c) -> { sync(); new dev.serverforge.util.SlotPicker("&8Emplacement recette", recipe.getSlot(),
                        s -> { recipe.setSlot(s); plugin.craft().saveAll(); reopen(p); }, () -> reopen(p)).open(p); });

        setButton(34, new ItemBuilder(Material.BARRIER)
                .name("&cSupprimer / Retour")
                .lore("&cShift+clic &7> supprimer la recette", "&eClic &7> retour (sauvegarde)").build(),
                (p, c) -> {
                    if (c == ClickType.SHIFT_LEFT || c == ClickType.SHIFT_RIGHT) {
                        cat.removeRecipe(recipe.getId()); plugin.craft().saveAll();
                        new CraftCategoryGui(plugin, p, cat.getId(), true).open(p);
                    } else {
                        sync();
                        new CraftCategoryGui(plugin, p, cat.getId(), true).open(p);
                    }
                });
    }

    /** Slots ou l'admin peut deposer/retirer librement des items. */
    @Override
    public boolean allowsRawInteraction(int slot) {
        if (slot == RESULT_SLOT) return true;
        for (int s : INGREDIENT_SLOTS) if (s == slot) return true;
        return false;
    }

    /** Lit les items places dans la grille et les enregistre dans la recette (sans le tag fantome). */
    public void sync() {
        recipe.getIngredients().clear();
        for (int s : INGREDIENT_SLOTS) {
            ItemStack it = inventory.getItem(s);
            if (it != null && it.getType() != Material.AIR) recipe.getIngredients().add(unghost(it.clone()));
        }
        ItemStack res = inventory.getItem(RESULT_SLOT);
        recipe.setResult(res != null && res.getType() != Material.AIR ? unghost(res.clone()) : null);
        plugin.craft().saveAll();
    }

    /** true si l'item du slot est un "fantome" (pre-rempli) -> ne pas le restituer au joueur. */
    public boolean isGhost(int slot) {
        ItemStack it = inventory.getItem(slot);
        return it != null && it.hasItemMeta()
                && it.getItemMeta().getPersistentDataContainer()
                    .has(new org.bukkit.NamespacedKey(plugin, "recipe_ghost"),
                         org.bukkit.persistence.PersistentDataType.BYTE);
    }

    private ItemStack ghost(ItemStack s) {
        var meta = s.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(new org.bukkit.NamespacedKey(plugin, "recipe_ghost"),
                    org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
            s.setItemMeta(meta);
        }
        return s;
    }
    private ItemStack unghost(ItemStack s) {
        var meta = s.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().remove(new org.bukkit.NamespacedKey(plugin, "recipe_ghost"));
            s.setItemMeta(meta);
        }
        return s;
    }

    private void reopen(Player p) { new RecipeEditGui(plugin, cat, recipe).open(p); }
}
