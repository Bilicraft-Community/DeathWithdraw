package com.bilicraft.deathwithdraw;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class DeathWithdraw extends JavaPlugin implements Listener {
    private Economy economy;
    private int spilt;
    private double threshold;
    private double max;
    private final OfflinePlayer taxAccount = Bukkit.getOfflinePlayer("Tax");

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        // Delay setup
        Bukkit.getScheduler().runTask(this, this::setupEconomy);

        this.spilt = getConfig().getInt("spilt");
        this.threshold = getConfig().getDouble("threshold");
        this.max = getConfig().getDouble("max");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return;
        }
        economy = rsp.getProvider();
        getLogger().info("Hooked into " + rsp.getPlugin().getName());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        double balance = economy.getBalance(event.getEntity());
        balance -= threshold;
        if (balance <= 0) {
            return;
        }
        balance /= spilt;
        balance = Math.min(balance, max);
        EconomyResponse response = economy.withdrawPlayer(event.getEntity(), balance);
        if (response.transactionSuccess()) {
            EconomyResponse response2 = economy.depositPlayer(taxAccount, balance);
            if (response2.transactionSuccess()) {
                event.getEntity().sendMessage(ChatColor.RED + "死亡惩罚：" + ChatColor.GOLD + economy.format(balance));
            } else {
                economy.depositPlayer(event.getEntity(), balance);
                getLogger().warning("Failed to deposit " + balance + " to tax account: " + response2.errorMessage);
            }
        } else {
            getLogger().warning("Failed to withdraw " + balance + " from player " + event.getEntity().getName() + " : " + response.errorMessage);
        }
    }
}
