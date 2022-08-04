package fr.iban.lands.guild;

import com.alessiodp.parties.api.events.bukkit.party.BukkitPartiesPartyPostDeleteEvent;
import fr.iban.guilds.Guild;
import fr.iban.guilds.GuildPlayer;
import fr.iban.guilds.GuildsManager;
import fr.iban.guilds.GuildsPlugin;
import fr.iban.guilds.enums.Rank;
import fr.iban.guilds.event.GuildDisbandEvent;
import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class GuildsDataAccess implements AbstractGuildDataAccess, Listener {

    private GuildsManager guildsManager;
    private LandManager landManager;

    public GuildsDataAccess(GuildsPlugin guildsPlugin, LandsPlugin landsPlugin) {
        this.guildsManager = guildsPlugin.getGuildsManager();
        this.landManager = landsPlugin.getLandManager();
    }

    @Override
    public boolean guildExists(UUID guildId) {
        return guildsManager.getGuildById(guildId) != null;
    }

    @Override
    public String getGuildName(UUID guildId) {
        Guild guild = guildsManager.getGuildById(guildId);
        return guild == null ? "" : guild.getName();
    }

    @Override
    public UUID getGuildId(UUID uuid) {
        GuildPlayer guildPlayer = guildsManager.getGuildPlayer(uuid);
        return guildPlayer == null ? null : guildPlayer.getGuildId();
    }

    @Override
    public boolean canManageGuildLand(UUID uuid) {
        GuildPlayer guildPlayer = guildsManager.getGuildPlayer(uuid);
        return guildPlayer != null && guildPlayer.isGranted(Rank.ADMIN);
    }

    @Override
    public boolean areInSameGuild(UUID uuid1, UUID uuid2) {
        GuildPlayer guildPlayer = guildsManager.getGuildPlayer(uuid1);
        GuildPlayer guildPlayer2 = guildsManager.getGuildPlayer(uuid2);
        return guildPlayer != null && guildPlayer2 != null && guildPlayer.getGuildId().equals(guildPlayer2.getGuildId());
    }

    @Override
    public boolean hasGuild(UUID uuid) {
        return guildsManager.getGuildPlayer(uuid) != null;
    }

    @Override
    public boolean isGuildLeader(UUID uuid, UUID guildId) {
        GuildPlayer guildPlayer = guildsManager.getGuildPlayer(uuid);
        return guildPlayer != null && guildPlayer.getGuildId().equals(guildId) && guildPlayer.isGranted(Rank.OWNER);
    }

    @Override
    public boolean isGuildMember(UUID uuid, UUID guildId) {
        GuildPlayer guildPlayer = guildsManager.getGuildPlayer(uuid);
        return guildPlayer != null && guildPlayer.getGuildId().equals(guildId);
    }

    @EventHandler
    public void onDisband(GuildDisbandEvent e) {
        landManager.getGuildLandsAsync(e.getGuild().getId()).thenAccept(lands -> {
            lands.forEach(land -> landManager.deleteLand(land));
        });
    }
}
