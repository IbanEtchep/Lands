package fr.iban.lands.guild;

import java.util.UUID;

public interface AbstractGuildDataAccess {

    void load();

    void unload();

    boolean isEnabled();

    boolean guildExists(UUID guildId);

    String getGuildName(UUID guildId);

    UUID getGuildId(UUID uuid);

    boolean canManageGuildLand(UUID uuid);

    boolean areInSameGuild(UUID uuid1, UUID uuid2);

    boolean hasGuild(UUID uuid);

    boolean isGuildLeader(UUID uuid, UUID guildId);

    UUID getGuildLeader(UUID guildId);

    boolean isGuildMember(UUID uuid, UUID guildId);

    boolean withdraw(UUID guildId, double amount, String reason);

    boolean withdraw(UUID guildId, double amount);

    boolean deposit(UUID guildId, double amount);

}
