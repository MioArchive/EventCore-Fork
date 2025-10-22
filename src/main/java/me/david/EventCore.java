package me.david;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.david.command.*;
import me.david.listener.*;
import me.david.manager.GameManager;
import me.david.manager.KitManager;
import me.david.manager.MapManager;
import me.david.manager.TeamManager;
import me.david.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

@Slf4j
@Getter
public class EventCore extends JavaPlugin {

    @Getter
    private static EventCore instance;
    private MapManager mapManager;
    private GameManager gameManager;
    private KitManager kitManager;
    private TeamManager teamManager;

    @Override
    @SuppressWarnings("deprecation")
    public void onEnable() {
        saveDefaultConfig();

        instance = this;
        mapManager = new MapManager();
        gameManager = new GameManager();
        kitManager = new KitManager();
        teamManager = new TeamManager();

        new AnnoucementCommand("announcement");
        new EventCommand("event");
        new KitCommand("kit");
        new ReviveCommand("revive");
        new SpawnCommand("spawn");

        Bukkit.getPluginManager().registerEvents(new BlockBreakListener(), instance);
        Bukkit.getPluginManager().registerEvents(new BlockExplodeListener(), instance);
        Bukkit.getPluginManager().registerEvents(new BlockPlaceListener(), instance);
        Bukkit.getPluginManager().registerEvents(new CreatureSpawnListener(), instance);
        Bukkit.getPluginManager().registerEvents(new EntityDamageByEntityListener(), instance);
        Bukkit.getPluginManager().registerEvents(new EntityDamageListener(), instance);
        Bukkit.getPluginManager().registerEvents(new EntityExplodeListener(), instance);
        Bukkit.getPluginManager().registerEvents(new PlayerDeathListener(), instance);
        Bukkit.getPluginManager().registerEvents(new PlayerDropItemListener(), instance);
        Bukkit.getPluginManager().registerEvents(new PlayerInteractListener(), instance);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), instance);
        Bukkit.getPluginManager().registerEvents(new PlayerPickupItemListener(), instance);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(), instance);
        Bukkit.getPluginManager().registerEvents(new PlayerRespawnListener(), instance);
        Bukkit.getPluginManager().registerEvents(new PlayerTeleportListener(), instance);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderHook().register();
        }

        Scheduler.timerAsync(new BorderUtil(), 20, 10);
        Scheduler.timerAsync(new AutoBroadcast(), 20, 20 * getConfig().getLong("AutoBroadcast.Interval", 60));
        Scheduler.wait(() -> {
            World world = mapManager.getSpawnLocation().getWorld();
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
            world.setDifficulty(Difficulty.PEACEFUL);
            world.getWorldBorder().setSize(BorderUtil.borderDefault);
            world.getWorldBorder().setDamageBuffer(BorderUtil.borderDamageBuffer);
            world.getWorldBorder().setDamageAmount(BorderUtil.borderDamageAmount);
        }, 2);

        if (getConfig().getBoolean("Messages.Actionbar.Enabled")) {
            Scheduler.timerAsync(() -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendActionBar(MessageUtil.get("Messages.Actionbar.Message"));
                }
            }, 0, 20);
        }
    }

    @Override
    public void onDisable() {
        if (gameManager.isRunning()) {
            gameManager.stop(null);
        }
    }

}
