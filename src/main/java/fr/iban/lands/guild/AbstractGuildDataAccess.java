package fr.iban.lands.guild;

import java.util.UUID;

public interface AbstractGuildDataAccess {

    boolean guildExists(UUID guildId);

    String getGuildName(UUID guildId);

    UUID getGuildId(UUID uuid);

    boolean canManageGuildLand(UUID uuid);

    boolean areInSameGuild(UUID uuid1, UUID uuid2);

    boolean hasGuild(UUID uuid);

    boolean isGuildLeader(UUID uuid, UUID guildId);

    boolean isGuildMember(UUID uuid, UUID guildId);

}
