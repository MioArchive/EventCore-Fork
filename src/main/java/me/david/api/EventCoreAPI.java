package me.david.api;

import lombok.Getter;
import me.david.EventCore;
import me.david.manager.GameManager;
import me.david.manager.KitManager;
import me.david.manager.MapManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public final class EventCoreAPI {

    // The documentation is absolutely not done yet, will follow SOON-ish

    @Nullable
    private static EventCoreAPI instance;
    private final @NotNull EventCore plugin;
    private final @NotNull GameManager gameManager;
    private final @NotNull KitManager kitManager;
    private final @NotNull MapManager mapManager;

    private EventCoreAPI() {
        this.plugin = EventCore.getInstance();
        this.gameManager = plugin.getGameManager();
        this.kitManager = plugin.getKitManager();
        this.mapManager = plugin.getMapManager();
    }

    @NotNull
    public static EventCoreAPI getInstance() {
        if (instance == null) {
            instance = new EventCoreAPI();
        }
        return instance;
    }

    public void startGame() {
        gameManager.start();
    }

    public void stopGame(@NotNull final String winner) {
        gameManager.stop(winner);
    }

    public boolean isGameRunning() {
        return gameManager.isRunning();
    }

    public boolean isTimerRunning() {
        return gameManager.isTimerRunning();
    }

    public long getInGameTimer() {
        return gameManager.getInGameTimer();
    }

    public void giveKit(@NotNull final Player player) {
        kitManager.give(player);
    }

    public void saveKit(@NotNull final String kitName, @NotNull final Player player) {
        kitManager.save(kitName, player);
    }

    public void enableKit(@NotNull final String kitName) {
        kitManager.enable(kitName);
    }

    public void deleteKit(@NotNull final String kitName) {
        kitManager.delete(kitName);
    }

    @Nullable
    public String getEnabledKit() {
        return kitManager.getEnabledKit();
    }

    @Nullable
    public Location getSpawnLocation() {
        return mapManager.getSpawnLocation();
    }

    public void saveSpawnLocation(@NotNull final Player player) {
        mapManager.saveSpawnLocation(player);
    }

    public void dropMap() {
        mapManager.drop();
    }

    public void resetMap() {
        mapManager.reset();
    }
}