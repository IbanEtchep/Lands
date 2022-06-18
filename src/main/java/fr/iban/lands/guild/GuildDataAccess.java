package fr.iban.lands.guild;

import java.util.UUID;

public interface GuildDataAccess {

    boolean guildExists(UUID guildId);

    String getGuildName(UUID guildId);

    UUID getGuildId(UUID uuid);

    boolean canManageGuildLand(UUID uuid, UUID guildId);

    boolean areInSameGuild(UUID uuid1, UUID uuid2);

    boolean hasGuild(UUID uuid);

    boolean isGuildLeader(UUID uuid, UUID guildId);

    boolean isGuildMember(UUID uuid, UUID guildId);

}
