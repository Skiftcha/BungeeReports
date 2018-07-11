package ru.frostdelta.bungeereports;

import org.bukkit.plugin.java.JavaPlugin;
import ru.frostdelta.bungeereports.executor.Executor;
import ru.frostdelta.bungeereports.modules.VaultLoader;
import ru.frostdelta.bungeereports.pluginMessage.PluginMessage;

import java.sql.SQLException;

public class Loader extends JavaPlugin {

    public static void main(String args[]){
    }

    Network db = new Network();
    Executor executor = new Executor(this);
    public boolean vaultEnabled;
    public boolean rewardsEnabled;
    public boolean customEnabled;
    public boolean limitEnabled;
    public boolean uuid;

    public int rewardAmount;
    public int customRewardAmount;


    public void onEnable(){

        this.saveDefaultConfig();

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new PluginMessage(this));

        vaultEnabled = getConfig().getBoolean("vault.enabled");
        rewardsEnabled = getConfig().getBoolean("reward.enabled");
        customEnabled = getConfig().getBoolean("customreward.enabled");
        limitEnabled = getConfig().getBoolean("limit.enabled");
        rewardAmount = getConfig().getInt("reward.amount");
        customRewardAmount = getConfig().getInt("customreward.amount");
        uuid = getConfig().getBoolean("customreward.uuid");

        if(vaultEnabled){

            VaultLoader VaultLoader = new VaultLoader();
            VaultLoader.setupChat();
            VaultLoader.setupEconomy();
            VaultLoader.setupPermissions();

        }else {
            getLogger().info("Vault disabled!");
            getLogger().info("Выдача наград невозможна!");
        }

        db.url = getConfig().getString("url");
        db.username = getConfig().getString("username");
        db.password = getConfig().getString("password");
        executor.bungee = getConfig().getBoolean("bungee.enabled");

        try {
            db.openConnection();
            db.createDB();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        getServer().getPluginManager().registerEvents(new EventHandler(this), this);
        getCommand("report").setExecutor(executor);
        getCommand("getreports").setExecutor(executor);
        getCommand("br").setExecutor(executor);

    }

    public String getPassword(){

        return getConfig().getString("password");

    }

    public String getUsername(){

        return getConfig().getString("username");

    }

    public String getUrl(){

        return getConfig().getString("url");

    }

    public boolean isUuid() {

        return uuid;

    }

    public int getCustomRewardAmount(){

        return customRewardAmount;
    }

    public int getRewardAmount(){

        return rewardAmount;
    }

    public boolean isLimitEnabled() {

        return limitEnabled;
    }

    public boolean isRewardsEnabled() {

        return rewardsEnabled;
    }

    public boolean isCustomEnabled() {

        return customEnabled;
    }

    public void onDisable(){
    }

}