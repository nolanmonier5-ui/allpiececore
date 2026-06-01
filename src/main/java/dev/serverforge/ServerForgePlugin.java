package dev.serverforge;

import dev.serverforge.chest.ChestListener;
import dev.serverforge.chest.ChestManager;
import dev.serverforge.chest.ChestTool;
import dev.serverforge.craft.CraftManager;
import dev.serverforge.craft.CraftService;
import dev.serverforge.hook.VaultHook;
import dev.serverforge.shop.ShopManager;
import dev.serverforge.shop.ShopService;
import dev.serverforge.util.ChatInput;
import dev.serverforge.util.Commands;
import dev.serverforge.util.GuiListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/** Point d'entree : shop dynamique + fabrication + coffres a butin. */
public class ServerForgePlugin extends JavaPlugin {

    private VaultHook vault;
    private ChatInput chat;

    private ShopManager shopManager;
    private ShopService shopService;
    private CraftManager craftManager;
    private CraftService craftService;
    private ChestManager chestManager;
    private ChestTool chestTool;

    private String lastEditedChest = null; // pour la fonction "copier la config"

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) getDataFolder().mkdirs();
        saveDefaultConfig();

        this.vault = new VaultHook(this);
        this.chat = new ChatInput(this);

        this.shopManager = new ShopManager(this);
        this.shopService = new ShopService(this);
        this.craftManager = new CraftManager(this);
        this.craftService = new CraftService(this);
        this.chestManager = new ChestManager(this);
        this.chestTool = new ChestTool(this);

        shopManager.loadAll();
        craftManager.loadAll();
        chestManager.loadAll();

        Bukkit.getPluginManager().registerEvents(new GuiListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ChestListener(this), this);

        Commands cmd = new Commands(this);
        for (String c : new String[]{"shop", "shopadmin", "craft", "craftadmin", "lootchest"}) {
            if (getCommand(c) != null) getCommand(c).setExecutor(cmd);
        }

        // Reset periodique des prix du shop
        long resetMin = shopManager.resetMinutes();
        if (resetMin > 0) {
            long ticks = resetMin * 60L * 20L;
            Bukkit.getScheduler().runTaskTimer(this, () -> {
                shopManager.resetAllPrices();
                getLogger().info("Prix du shop reinitialises.");
            }, ticks, ticks);
        }

        if (!vault.isEnabled())
            getLogger().warning("Vault/economie absent : le shop ne pourra pas traiter d'argent.");

        getLogger().info("ServerForge active.");
    }

    @Override
    public void onDisable() {
        if (shopManager != null) shopManager.saveAll();
        if (craftManager != null) craftManager.saveAll();
        if (chestManager != null) chestManager.saveAll();
    }

    public VaultHook vault() { return vault; }
    public ChatInput chat() { return chat; }
    public ShopManager shop() { return shopManager; }
    public ShopService shopService() { return shopService; }
    public CraftManager craft() { return craftManager; }
    public CraftService craftService() { return craftService; }
    public ChestManager chests() { return chestManager; }
    public ChestTool chestTool() { return chestTool; }

    public String getLastEditedChest() { return lastEditedChest; }
    public void setLastEditedChest(String key) { this.lastEditedChest = key; }
}
