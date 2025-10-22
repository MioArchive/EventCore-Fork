package me.david.manager;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.Getter;
import me.david.EventCore;
import me.david.api.events.team.TeamCreateEvent;
import me.david.api.events.team.TeamEliminateEvent;
import me.david.api.events.team.TeamJoinEvent;
import me.david.api.events.team.TeamLeaveEvent;
import me.david.util.TeamUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class TeamManager {

    private final Map<String, Set<UUID>> teams = new ConcurrentHashMap<>();
    private final Map<UUID, String> playerTeams = new ConcurrentHashMap<>();
    private final AtomicInteger teamCounter = new AtomicInteger(1);
    private boolean enabled = false;
    private int teamSize = 2;

    public TeamManager() {
        load();
    }

    private void load() {
        enabled = EventCore.getInstance().getConfig().getBoolean("Teams.Enabled", false);
        teamSize = EventCore.getInstance().getConfig().getInt("Teams.TeamSize", 2);
    }

    public void enable(final int size) {
        this.enabled = true;
        this.teamSize = Math.max(1, size);
        EventCore.getInstance().getConfig().set("Teams.Enabled", true);
        EventCore.getInstance().getConfig().set("Teams.TeamSize", teamSize);
        EventCore.getInstance().saveConfig();
    }

    public void disable() {
        this.enabled = false;
        clear();
        EventCore.getInstance().getConfig().set("Teams.Enabled", false);
        EventCore.getInstance().saveConfig();
    }

    @NotNull
    @CanIgnoreReturnValue
    public String createTeam(@Nullable final String teamName) {
        final String name = teamName != null ? teamName : "Team-" + teamCounter.getAndIncrement();

        final TeamCreateEvent event = new TeamCreateEvent(name);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return null;
        }

        teams.putIfAbsent(name, ConcurrentHashMap.newKeySet());
        return name;
    }

    @CanIgnoreReturnValue
    public boolean addPlayer(@NotNull final Player player, @NotNull final String teamName) {
        if (!teams.containsKey(teamName)) return false;

        final Set<UUID> team = teams.get(teamName);
        if (team.size() >= teamSize) return false;

        final TeamJoinEvent event = new TeamJoinEvent(player, teamName);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return false;

        removePlayer(player);
        team.add(player.getUniqueId());
        playerTeams.put(player.getUniqueId(), teamName);
        return true;
    }

    public void removePlayer(@NotNull final Player player) {
        final String oldTeam = playerTeams.remove(player.getUniqueId());
        if (oldTeam != null) {
            final TeamLeaveEvent event = new TeamLeaveEvent(player, oldTeam);
            Bukkit.getPluginManager().callEvent(event);

            Set<UUID> team = teams.get(oldTeam);
            if (team != null) {
                team.remove(player.getUniqueId());
                if (team.isEmpty()) {
                    teams.remove(oldTeam);
                }
            }
        }
    }

    public void eliminateTeam(@NotNull final String teamName) {
        if (!teams.containsKey(teamName)) return;

        final TeamEliminateEvent event = new TeamEliminateEvent(teamName, new HashSet<>(teams.get(teamName)));
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return;

        Set<UUID> team = teams.remove(teamName);
        if (team != null) {
            team.forEach(playerTeams::remove);
        }
    }

    public void autoAssignTeams() {
        clear();
        final List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        Collections.shuffle(players);

        for (int i = 0; i < players.size(); i++) {
            int teamIndex = (i / teamSize) + 1;
            String teamName = "Team-" + teamIndex;

            if (!teams.containsKey(teamName)) {
                createTeam(teamName);
            }

            addPlayer(players.get(i), teamName);
        }
    }

    public void clear() {
        teams.clear();
        playerTeams.clear();
        teamCounter.set(1);
    }

    @Nullable
    public String getPlayerTeam(@NotNull final UUID uuid) {
        return playerTeams.get(uuid);
    }

    @Nullable
    public Set<UUID> getTeamMembers(@NotNull final String teamName) {
        return teams.get(teamName);
    }

    public int getAliveTeamsCount() {
        return (int) teams.values().stream()
                .filter(team -> team.stream().anyMatch(TeamUtils::isPlayerAlive))
                .count();
    }

    public boolean isTeamAlive(@NotNull final String teamName) {
        Set<UUID> team = teams.get(teamName);
        return team != null && team.stream().anyMatch(TeamUtils::isPlayerAlive);
    }
}
