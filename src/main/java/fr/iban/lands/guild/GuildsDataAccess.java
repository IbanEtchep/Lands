package fr.iban.lands.guild;

import fr.iban.guilds.api.GuildManager;
import fr.iban.guilds.api.service.GuildBankService;
import fr.iban.guilds.enums.GuildPermission;
import fr.iban.guilds.event.GuildDisbandEvent;
import fr.iban.guilds.model.Guild;
import fr.iban.guilds.model.GuildPlayer;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.api.LandRepository;
import fr.iban.lands.api.LandService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.event.server.ServiceUnregisterEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;

import java.util.UUID;

public class GuildsDataAccess implements AbstractGuildDataAccess, Listener {

    private final LandsPlugin landsPlugin;
    private GuildManager guildsManager;
    private GuildBankService guildBankService;

    public GuildsDataAccess(LandsPlugin landsPlugin) {
        this.landsPlugin = landsPlugin;
    }

    @Override
    public void load() {
        ServicesManager servicesManager = landsPlugin.getServer().getServicesManager();
        RegisteredServiceProvider<GuildManager> guildManagerRsp = servicesManager.getRegistration(GuildManager.class);
        RegisteredServiceProvider<GuildBankService> guildBankServiceRsp = servicesManager.getRegistration(GuildBankService.class);

        if (guildManagerRsp != null && guildBankServiceRsp != null) {
            guildsManager = guildManagerRsp.getProvider();
            guildBankService = guildBankServiceRsp.getProvider();

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
        return guildPlayer == null ? null : guildPlayer.getGuild().getId();
    }

    @Override
    public boolean canManageGuildLand(UUID uuid) {
        GuildPlayer guildPlayer = guildsManager.getGuildPlayer(uuid);
        return guildPlayer != null && guildPlayer.isGranted(GuildPermission.MANAGE_LANDS);
    }

    @Override
    public boolean areInSameGuild(UUID uuid1, UUID uuid2) {
        GuildPlayer guildPlayer = guildsManager.getGuildPlayer(uuid1);
        GuildPlayer guildPlayer2 = guildsManager.getGuildPlayer(uuid2);
        return guildPlayer != null
                && guildPlayer2 != null
                && guildPlayer.getGuild().equals(guildPlayer2.getGuild());
    }

    @Override
    public boolean hasGuild(UUID uuid) {
        return guildsManager.getGuildPlayer(uuid) != null;
    }

    @Override
    public boolean isGuildLeader(UUID uuid, UUID guildId) {
        GuildPlayer guildPlayer = guildsManager.getGuildPlayer(uuid);
        return guildPlayer != null
                && guildPlayer.getGuild().getId().equals(guildId)
                && guildPlayer.isOwner();
    }

    @Override
    public UUID getGuildLeader(UUID guildId) {
        Guild guild = guildsManager.getGuildById(guildId);
        return guild != null ? guild.getOwner().getUuid() : null;
    }

    @Override
    public boolean isGuildMember(UUID uuid, UUID guildId) {
        GuildPlayer guildPlayer = guildsManager.getGuildPlayer(uuid);
        return guildPlayer != null && guildPlayer.getGuild().getId().equals(guildId);
    }

    @Override
    public boolean withdraw(UUID guildId, double amount, String reason) {
        Guild guild = guildsManager.getGuildById(guildId);
        if (guild == null) {
            return false;
        }
        return guildBankService.withdraw(guild, amount, reason);
    }

    @Override
    public boolean withdraw(UUID guildId, double amount) {
        Guild guild = guildsManager.getGuildById(guildId);
        if (guild == null) {
            return false;
        }
        return guildBankService.withdraw(guild, amount);
    }

    @Override
    public boolean deposit(UUID guildId, double amount) {
        Guild guild = guildsManager.getGuildById(guildId);
        if (guild == null) {
            return false;
        }
        return guildBankService.deposit(guild, amount);
    }

    @EventHandler
    public void onDisband(GuildDisbandEvent e) {
        LandService landService = landsPlugin.getLandService();
        LandRepository landRepository = landsPlugin.getLandRepository();
        UUID guildId = e.getGuild().getId();

        landService.transferClaims(guildId, e.getGuild().getOwner().getUuid());

        landRepository.getGuildLands(guildId).forEach(landRepository::deleteLand);
    }

    @EventHandler
    public void onServiceRegister(ServiceRegisterEvent event) {
        if (event.getProvider().getService().getName().equals("fr.iban.guilds.api.GuildManager")) {
            load();
        }
    }

    @EventHandler
    public void onServiceUnregister(ServiceUnregisterEvent event) {
        if (event.getProvider().getService().getName().equals("fr.iban.guilds.api.GuildManager")) {
            this.guildsManager = null;
            this.guildBankService = null;
            landsPlugin.getLogger().info("Intégration avec le plugin Guilds désactivée.");
            load();
        }
    }
}
