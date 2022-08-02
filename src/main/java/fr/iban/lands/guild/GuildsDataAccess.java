package fr.iban.lands.guild;

import java.util.UUID;

public class GuildsDataAccess implements AbstractGuildDataAccess{
    @Override
    public boolean guildExists(UUID guildId) {
        return false;
    }

    @Override
    public String getGuildName(UUID guildId) {
        return null;
    }

    @Override
    public UUID getGuildId(UUID uuid) {
        return null;
    }

    @Override
    public boolean canManageGuildLand(UUID uuid, UUID guildId) {
        return false;
    }

    @Override
    public boolean areInSameGuild(UUID uuid1, UUID uuid2) {
        return false;
    }

    @Override
    public boolean hasGuild(UUID uuid) {
        return false;
    }

    @Override
    public boolean isGuildLeader(UUID uuid, UUID guildId) {
        return false;
    }

    @Override
    public boolean isGuildMember(UUID uuid, UUID guildId) {
        return false;
    }
}
