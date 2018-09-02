package ru.frostdelta.bungeereports.executor;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.frostdelta.bungeereports.CanReport;
import ru.frostdelta.bungeereports.Loader;
import ru.frostdelta.bungeereports.Network;
import ru.frostdelta.bungeereports.NonBungee;
import ru.frostdelta.bungeereports.action.Action;
import ru.frostdelta.bungeereports.gui.GetReportsUI;
import ru.frostdelta.bungeereports.hash.HashedLists;
import ru.frostdelta.bungeereports.pluginMessage.GetPlayerCount;
import ru.frostdelta.bungeereports.spectate.SpectateManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Executor extends SpectateManager implements CommandExecutor {

    private static HashMap<String, Action> actionQueue = new HashMap<>();
    private static HashMap<String, String> requestQueue = new HashMap<>();

    private Loader plugin;


    public Executor(Loader instance){

        plugin = instance;

    }
    public boolean bungee;
    private static List<Player> senders = new ArrayList<>();
    private boolean isBungee(){
        return bungee;
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String st, String[] args) {

        Network db = new Network();
        Player player = (Player) s;
        Player targetPlayer = plugin.getServer().getPlayer(args[0]);

        if (cmd.getName().equalsIgnoreCase("screen") && targetPlayer != null) {
            if (targetPlayer.isOnline()) {
                if (cmd.getName().equalsIgnoreCase("screen")) {
                    requestQueue.put(targetPlayer.getName(), player.getName());
                    actionQueue.put(targetPlayer.getName(), Action.SCREENSHOT);
                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    out.writeUTF(Action.SCREENSHOT.getActionName());
                    out.writeUTF(plugin.getConfig().getString("mod.clientID"));
                    plugin.sendMessage(targetPlayer, out);
                    return true;
                }
            } else {
               sendMessage(player, "&cИгрок " + targetPlayer + " оффлайн!");
            }
        }

        if (cmd.getName().equalsIgnoreCase("getscreens")) {
            s.sendMessage("ну как?");
            String screenshots = db.getScreenshots(args[0]);
            s.sendMessage("опа "+ screenshots);
            if (!screenshots.isEmpty()) {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF(Action.SCREENSHOTS.getActionName());
                out.writeUTF(args[0]);
                out.writeUTF(db.getScreenshots(args[0]));
                plugin.sendMessage(player, out);

            } else {
                sendMessage(player, "&cДля начала сделай скрин его экрана.");
            }
        }

        if(cmd.getName().equalsIgnoreCase("br") && args.length == 1 && args[0].equals("reload")){

            plugin.loadConfig();
            plugin.getConfig().options().copyHeader(true);

            plugin.loadConfig();

            s.sendMessage(ChatColor.GREEN + "Конфиг перезагружен!");
            return true;
        }

        if(s instanceof Player && plugin.isEnabled()) {
            if (cmd.getName().equalsIgnoreCase("getreports")) {

                GetReportsUI getReportsUI = new GetReportsUI(plugin);

                getReportsUI.openGUI((Player) s,
                        HashedLists.getTotalRepors(),
                        HashedLists.getSenderList(),
                        HashedLists.getReasonList(),
                        HashedLists.getReportList(),
                        HashedLists.getCommentList());
                return true;
            }

            if(cmd.getName().equalsIgnoreCase("spectateoff")){
                if(super.isSpectate((Player)s)){
                    super.spectateOff((Player)s);
                }else s.sendMessage(ChatColor.RED + "Ошибка. Вы ни за кем не наблюдаете!");
                return true;
            }

            if(cmd.getName().equalsIgnoreCase("spectate") && args.length == 1 && plugin.isSpectateEnabled()){

                if(plugin.getServer().getPlayer(args[0]) != null){
                    super.setSpectate((Player)s, plugin.getServer().getPlayer(args[0]));
                }else s.sendMessage(ChatColor.DARK_RED + "Игрок не найден!");
                return true;
            }

            if (cmd.getName().equalsIgnoreCase("report")) {
                senders.add((Player)s);
                GetPlayerCount getPlayerCount = new GetPlayerCount(plugin);
                NonBungee nonBungee = new NonBungee(plugin);
                CanReport canReport = new CanReport(plugin);

                canReport.needReward(s.getName());

                if (!plugin.isLimitEnabled() || plugin.isLimitEnabled() && canReport.limit(s.getName())) {

                    if (isBungee()) {
                        getPlayerCount.sendMessage((Player) s);
                    } else nonBungee.getNonBungeePlayerlist((Player) s);
                }
                return true;
            }
        }else plugin.getLogger().severe("For players only!");
        return true;
    }

    public static HashMap<String, Action> getActionQueue() {
        return actionQueue;
    }

    public static HashMap<String, String> getRequestQueue() {
        return requestQueue;
    }

    public void sendMessage(Player p, String msg) {
        p.sendMessage(("&f[&bBungeeReports&f] " + msg).replace("&", "§"));
    }


    public static List<Player> getSenders(){
        return senders;
    }
}
