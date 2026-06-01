package dev.serverforge.util;

import org.bukkit.Material;
import java.util.function.IntConsumer;

/** Choix d'un slot (0-53) ou Auto. */
public class SlotPicker extends Gui {
    private final int current;
    private final IntConsumer onPick;
    private final Runnable onBack;

    public SlotPicker(String title, int current, IntConsumer onPick, Runnable onBack) {
        super(title, 6);
        this.current = current; this.onPick = onPick; this.onBack = onBack;
        build();
    }

    @Override
    public void build() {
        clear();
        for (int slot = 0; slot <= 53; slot++) {
            if (slot == 46 || slot == 52) continue;
            boolean cur = slot == current;
            int s = slot;
            setButton(slot, new ItemBuilder(cur ? Material.LIME_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE)
                    .name(cur ? "&aActuel (slot " + slot + ")" : "&7Slot " + slot).glow(cur).build(),
                    (p, c) -> onPick.accept(s));
        }
        setButton(46, new ItemBuilder(Material.COMPARATOR).name("&eAuto").build(), (p, c) -> onPick.accept(-1));
        setButton(52, new ItemBuilder(Material.BARRIER).name("&cRetour").build(), (p, c) -> onBack.run());
    }
}
