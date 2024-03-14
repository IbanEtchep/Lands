package fr.iban.lands.model.land;

import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.Action;
import fr.iban.lands.enums.LandType;
import fr.iban.lands.guild.AbstractGuildDataAccess;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class GuildLand extends Land {

    private final UUID guildId;

    public GuildLand(UUID id, UUID guildId, String name) {
        super(id, name);
        this.guildId = guildId;
    }

    @Override
    public @Nullable UUID getOwner() {
        return guildId;
    }

    @Override
    public LandType getType() {
        return LandType.GUILD;
    }

    @Override
    public boolean isBypassing(Player player, Action action) {
        UUID uuid = player.getUniqueId();
        AbstractGuildDataAccess guildDataAccess = LandsPlugin.getInstance().getGuildDataAccess();
        if (guildDataAccess.isGuildLeader(uuid, guildId)
                || (guildDataAccess.isGuildMember(uuid, guildId)
                && getGuildTrust().hasPermission(action))) {
            return true;
        }

        return super.isBypassing(player, action);
    }

    public UUID getGuildId() {
        return guildId;
    }

    public UUID getGuildOwner() {
        AbstractGuildDataAccess guildDataAccess = LandsPlugin.getInstance().getGuildDataAccess();
        return guildDataAccess.getGuildLeader(guildId);
    }

    public String getGuildName() {
        AbstractGuildDataAccess guildDataAccess = LandsPlugin.getInstance().getGuildDataAccess();
        return guildDataAccess.getGuildName(guildId);
    }

    public boolean isGuildMember(UUID uuid) {
        AbstractGuildDataAccess guildDataAccess = LandsPlugin.getInstance().getGuildDataAccess();
        return guildDataAccess.isGuildMember(uuid, guildId);
    }
}
