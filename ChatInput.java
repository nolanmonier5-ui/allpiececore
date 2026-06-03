package dev.serverforge.hook;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.lang.reflect.Method;

/** Economie via Vault (reflection : aucune dependance Maven). */
public class VaultHook {
    private boolean enabled = false;
    private Object economy;
    private Method deposit, withdraw, has, getBalance, format;

    public VaultHook(Plugin plugin) {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) return;
        try {
            Class<?> ec = Class.forName("net.milkbowl.vault.economy.Economy");
            RegisteredServiceProvider<?> rsp = Bukkit.getServicesManager().getRegistration(ec);
            if (rsp == null) { plugin.getLogger().warning("Vault present mais aucune economie enregistree."); return; }
            economy = rsp.getProvider();
            deposit = ec.getMethod("depositPlayer", OfflinePlayer.class, double.class);
            withdraw = ec.getMethod("withdrawPlayer", OfflinePlayer.class, double.class);
            has = ec.getMethod("has", OfflinePlayer.class, double.class);
            getBalance = ec.getMethod("getBalance", OfflinePlayer.class);
            format = ec.getMethod("format", double.class);
            enabled = true;
            plugin.getLogger().info("Hook Vault actif.");
        } catch (Throwable t) {
            plugin.getLogger().warning("Echec hook Vault: " + t.getMessage());
        }
    }

    public boolean isEnabled() { return enabled; }

    public boolean has(OfflinePlayer p, double amount) {
        if (!enabled) return false;
        try { return (boolean) has.invoke(economy, p, amount); } catch (Throwable t) { return false; }
    }
    public double balance(OfflinePlayer p) {
        if (!enabled) return 0;
        try { return (double) getBalance.invoke(economy, p); } catch (Throwable t) { return 0; }
    }
    public boolean withdraw(OfflinePlayer p, double amount) {
        if (!enabled || amount <= 0) return false;
        try { withdraw.invoke(economy, p, amount); return true; } catch (Throwable t) { return false; }
    }
    public void deposit(OfflinePlayer p, double amount) {
        if (!enabled || amount <= 0) return;
        try { deposit.invoke(economy, p, amount); } catch (Throwable ignored) {}
    }
    public String format(double amount) {
        if (!enabled) return String.valueOf(amount);
        try { Object r = format.invoke(economy, amount); return r == null ? String.valueOf(amount) : r.toString(); }
        catch (Throwable t) { return String.valueOf(amount); }
    }
}
