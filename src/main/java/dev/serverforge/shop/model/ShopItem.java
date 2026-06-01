package dev.serverforge.shop.model;

import dev.serverforge.util.ItemUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Un item du shop avec prix dynamique.
 * - display    : l'ItemStack vendu/achete (type, CMD, nom, lore... tout est conserve)
 * - basePrice  : prix d'achat initial (multiplicateur = 1.0)
 * - buyStep    : hausse du multiplicateur par unite achetee (ex 0.02 = +2%)
 * - sellStep   : baisse du multiplicateur par unite vendue
 * - multiplier : etat dynamique courant (1.0 = prix initial)
 * sellRatio, min/max multiplier viennent de la config globale.
 */
public class ShopItem {
    private final String id;
    private ItemStack display;
    private double basePrice;
    private double buyStep;
    private double sellStep;
    private double multiplier = 1.0;
    private int slot = -1;
    private boolean buyable = true;
    private boolean sellable = true;

    public ShopItem(String id, ItemStack display, double basePrice, double buyStep, double sellStep) {
        this.id = id;
        this.display = display;
        this.basePrice = basePrice;
        this.buyStep = buyStep;
        this.sellStep = sellStep;
    }

    public static ShopItem create(ItemStack display, double basePrice, double buyStep, double sellStep) {
        return new ShopItem(UUID.randomUUID().toString().substring(0, 8), display, basePrice, buyStep, sellStep);
    }

    public String getId() { return id; }
    public ItemStack getDisplay() { return display; }
    public void setDisplay(ItemStack display) { this.display = display; }
    public double getBasePrice() { return basePrice; }
    public void setBasePrice(double p) { this.basePrice = Math.max(0, p); }
    public double getBuyStep() { return buyStep; }
    public void setBuyStep(double s) { this.buyStep = Math.max(0, s); }
    public double getSellStep() { return sellStep; }
    public void setSellStep(double s) { this.sellStep = Math.max(0, s); }
    public double getMultiplier() { return multiplier; }
    public void setMultiplier(double m) { this.multiplier = m; }
    public int getSlot() { return slot; }
    public void setSlot(int slot) { this.slot = slot; }
    public boolean isBuyable() { return buyable; }
    public void setBuyable(boolean b) { this.buyable = b; }
    public boolean isSellable() { return sellable; }
    public void setSellable(boolean b) { this.sellable = b; }

    public double currentBuyPrice() { return basePrice * multiplier; }
    public double currentSellPrice(double sellRatio) { return currentBuyPrice() * sellRatio; }

    public void onBuy(int qty, double minMult, double maxMult) {
        multiplier = clamp(multiplier + buyStep * qty, minMult, maxMult);
    }
    public void onSell(int qty, double minMult, double maxMult) {
        multiplier = clamp(multiplier - sellStep * qty, minMult, maxMult);
    }
    public void resetPrice() { multiplier = 1.0; }
    private static double clamp(double v, double lo, double hi) { return Math.max(lo, Math.min(hi, v)); }

    public int customModelData() { return ItemUtil.readModelData(display); }

    public void save(ConfigurationSection sec) {
        sec.set("display", display);
        sec.set("base-price", basePrice);
        sec.set("buy-step", buyStep);
        sec.set("sell-step", sellStep);
        sec.set("multiplier", multiplier);
        sec.set("slot", slot);
        sec.set("buyable", buyable);
        sec.set("sellable", sellable);
    }

    public static ShopItem load(String id, ConfigurationSection sec) {
        ItemStack display = sec.getItemStack("display");
        if (display == null) return null;
        ShopItem it = new ShopItem(id, display,
                sec.getDouble("base-price", 0),
                sec.getDouble("buy-step", 0.02),
                sec.getDouble("sell-step", 0.02));
        it.multiplier = sec.getDouble("multiplier", 1.0);
        it.slot = sec.getInt("slot", -1);
        it.buyable = sec.getBoolean("buyable", true);
        it.sellable = sec.getBoolean("sellable", true);
        return it;
    }
}
