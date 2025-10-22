package me.david.util;

import lombok.experimental.UtilityClass;
import me.david.EventCore;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@UtilityClass
public class TeamUtils {

    public boolean isPlayerAlive(@NotNull final UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        return player != null && player.isOnline() && player.getGameMode() == GameMode.SURVIVAL;
    }

    public boolean areTeammates(@NotNull final Player player1, @NotNull final Player player2) {
        if (!EventCore.getInstance().getTeamManager().isEnabled()) return false;

        String team1 = EventCore.getInstance().getTeamManager().getPlayerTeam(player1.getUniqueId());
        String team2 = EventCore.getInstance().getTeamManager().getPlayerTeam(player2.getUniqueId());

        return team1 != null && team1.equals(team2);
    }

    @Nullable
    public Set<Player> getTeammates(@NotNull final Player player) {
        String teamName = EventCore.getInstance().getTeamManager().getPlayerTeam(player.getUniqueId());
        if (teamName == null) return null;

        Set<UUID> members = EventCore.getInstance().getTeamManager().getTeamMembers(teamName);
        if (members == null) return null;

        return members.stream()
                .map(Bukkit::getPlayer)
                .filter(p -> p != null && !p.equals(player))
                .collect(Collectors.toSet());
    }

    public int getAliveTeammates(@NotNull final Player player) {
        Set<Player> teammates = getTeammates(player);
        if (teammates == null) return 0;

        return (int) teammates.stream()
                .filter(p -> p.getGameMode() == GameMode.SURVIVAL)
                .count();
    }

    @Nullable
    public String getWinningTeam() {
        return EventCore.getInstance().getTeamManager().getTeams().entrySet().stream()
                .filter(entry -> entry.getValue().stream().anyMatch(TeamUtils::isPlayerAlive))
                .map(entry -> entry.getKey())
                .findFirst()
                .orElse(null);
    }
}
