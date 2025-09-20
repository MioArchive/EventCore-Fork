package me.david.listener;

import me.david.EventCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class PlayerPickupItemListener implements Listener {

    @EventHandler
    public void onPlayerPickUpItem(PlayerPickupItemEvent event) {
        final Player player = event.getPlayer();

        if (EventCore.getInstance().getConfig().getBoolean("Settings.AllowItemDropBeforeStart")) return;

        if (player.hasPermission("event.bypass")) {
            event.setCancelled(false);
            return;
        }

        event.setCancelled(!(EventCore.getInstance().getGameManager().isRunning()));
    }

}
