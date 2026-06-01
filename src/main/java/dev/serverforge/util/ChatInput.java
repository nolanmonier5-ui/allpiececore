package dev.serverforge.util;

import dev.serverforge.ServerForgePlugin;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/** Saisies au chat pour les editeurs (nom, prix, %, etc.). */
public class ChatInput {
    private record Pending(Consumer<String> cb, Runnable cancel) {}
    private final ServerForgePlugin plugin;
    private final ConcurrentHashMap<UUID, Pending> waiting = new ConcurrentHashMap<>();

    public ChatInput(ServerForgePlugin plugin) { this.plugin = plugin; }

    public void request(Player p, String prompt, Consumer<String> cb, Runnable cancel) {
        waiting.put(p.getUniqueId(), new Pending(cb, cancel));
        p.closeInventory();
        p.sendMessage(Text.color("&e" + prompt));
        p.sendMessage(Text.color("&7Reponds dans le chat, ou &ccancel &7pour annuler."));
    }
    public boolean isWaiting(UUID u) { return waiting.containsKey(u); }
    public boolean consume(Player p, String msg) {
        Pending pend = waiting.remove(p.getUniqueId());
        if (pend == null) return false;
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (msg.equalsIgnoreCase("cancel")) {
                p.sendMessage(Text.color("&cAnnule."));
                if (pend.cancel() != null) pend.cancel().run();
            } else pend.cb().accept(msg);
        });
        return true;
    }
    public void clear(UUID u) { waiting.remove(u); }
}
