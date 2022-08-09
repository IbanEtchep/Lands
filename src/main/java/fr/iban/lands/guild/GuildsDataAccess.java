package fr.iban.lands.guild;

import fr.iban.guilds.Guild;
import fr.iban.guilds.GuildPlayer;
import fr.iban.guilds.GuildsManager;
import fr.iban.guilds.enums.Rank;
import fr.iban.guilds.event.GuildDisbandEvent;
import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.event.server.ServiceUnregisterEvent;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.UUID;

public class GuildsDataAccess implements AbstractGuildDataAccess, Listener {

    private GuildsManager guildsManager;
    private final LandsPlugin landsPlugin;

    public GuildsDataAccess(LandsPlugin landsPlugin) {
        this.landsPlugin = landsPlugin;
    }

    @Override
    public void load() {
        RegisteredServiceProvider<GuildsManager> rsp = landsPlugin.getServer().getServicesManager().getRegistration(GuildsManager.class);
        if (rsp != null) {
            guildsManager = rsp.getProvider();
            landsPlugin.getServer().getPluginManager().registerEvents(this, landsPlugin);
            landsPlugin.getLogger().info("Intégration avec le plugin Guilds effectué.");
        }
    }

    @Override
    public void unload() {

    }

    public boolean isEnabled() {
        return guildsManager != null;
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

    @Override
    public boolean withdraw(UUID guildId, double amount, String reason) {
        Guild guild = guildsManager.getGuildById(guildId);
        if(guild == null) {
            return false;
        }
        return guildsManager.guildWithdraw(guild, amount, reason);
    }

    @Override
    public boolean withdraw(UUID guildId, double amount) {
        Guild guild = guildsManager.getGuildById(guildId);
        if(guild == null) {
            return false;
        }
        return guildsManager.guildWithdraw(guild, amount);
    }

    @EventHandler
    public void onDisband(GuildDisbandEvent e) {
        LandManager landManager = landsPlugin.getLandManager();
        landManager.getGuildLandsAsync(e.getGuild().getId()).thenAccept(lands -> {
            lands.forEach(landManager::deleteLand);
        });
    }

    @EventHandler
    public void onServiceRegister(ServiceRegisterEvent event) {
        if (event.getProvider().getService().getName().equals("fr.iban.guilds.GuildsManager")) {
            load();
        }
    }

    @EventHandler
    public void onServiceUnregister(ServiceUnregisterEvent event) {
        if (event.getProvider().getService().getName().equals("fr.iban.guilds.GuildsManager")) {
            this.guildsManager = null;
            landsPlugin.getLogger().info("Intégration avec le plugin Guilds désactivée.");
            load();
        }
    }
}
