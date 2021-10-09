package xyz.dm1lk.timer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public final class Main extends JavaPlugin implements Listener {
    private static BukkitTask timerTask;
    private static BossBar bossBar;

    @Override
    public void onEnable() {
        this.getCommand("timer").setExecutor(this);
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        if(timerTask != null && !timerTask.isCancelled()){
            bossBar.setVisible(false);
            bossBar.removeAll();
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.setExp(0);
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event){
        if(timerTask != null && !timerTask.isCancelled()){
            event.getPlayer().setLevel(0);
            event.getPlayer().setExp(0);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        AtomicInteger seconds = new AtomicInteger();
        if (args.length > 0) {
            if (Objects.equals(args[0], "cancel")) {
                if (timerTask != null && !timerTask.isCancelled()) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        bossBar.setVisible(false);
                        bossBar.removeAll();
                        player.setLevel(0);
                        player.setExp(0);
                    }
                    sender.sendMessage(ChatColor.GREEN + "Timer was successfully cancelled.");
                    timerTask.cancel();
                } else {
                    sender.sendMessage(ChatColor.RED + "Hey! No timer is running at this present moment.");
                }
                return true;
            }
            try {
                seconds.set(Integer.parseInt(args[0]));
                if(seconds.get() < 1){
                    sender.sendMessage(ChatColor.RED + "Hey! You must provide a number above zero to start a timer!");
                    return true;
                }
            } catch (Exception exception) {
                sender.sendMessage(ChatColor.RED + "Hey! Please only include numbers in your arguments for this command :p");
                return false;
            }
            if (timerTask != null) {
                if (!timerTask.isCancelled()) {
                    Bukkit.getLogger().info(String.valueOf(1));
                    if (args.length > 1 && Objects.equals(args[1], "force")) {
                        bossBar.setVisible(false);
                        bossBar.removeAll();
                        timerTask.cancel();
                    } else {
                        sender.sendMessage(ChatColor.RED + "Hey! Someone already has a timer running, to override this, add \"force\" as a second argument to the command.");
                        return true;
                    }
                }
            }
            int initialSeconds = seconds.get();
            bossBar = Bukkit.createBossBar(ChatColor.BLUE + String.valueOf(seconds) + " seconds left...", BarColor.GREEN, BarStyle.SOLID);
            timerTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
                if (seconds.get() == 0) {
                    bossBar.setVisible(false);
                    bossBar.removeAll();
                    timerTask.cancel();
                    return;
                }
                for (Player player : Bukkit.getOnlinePlayers()) {
                    bossBar.addPlayer(player);
                    player.setLevel(seconds.get());
                }
                bossBar.setProgress((double) seconds.get() / initialSeconds);
                bossBar.setTitle(String.valueOf(seconds) + " seconds left...");
                seconds.set(seconds.get() - 1);
            }, 1L, 20L);
            sender.sendMessage(ChatColor.GREEN + "Running a timer for " + args[0] + " seconds!");
            bossBar.setVisible(true);
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.setLevel(0);
                player.setExp(0);
            }
            return true;
        }
        return false;
    }
}
