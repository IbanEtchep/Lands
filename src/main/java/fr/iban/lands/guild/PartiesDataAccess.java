package fr.iban.lands.guild;

import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.alessiodp.parties.api.interfaces.Party;
import com.alessiodp.parties.api.interfaces.PartyPlayer;

import java.util.UUID;

public class PartiesDataAccess implements GuildDataAccess {

    private PartiesAPI partiesAPI;

    public PartiesDataAccess(PartiesAPI partiesAPI) {
        this.partiesAPI = partiesAPI;
    }

    @Override
    public boolean guildExists(UUID guildId) {
        return partiesAPI.getParty(guildId) != null;
    }

    @Override
    public String getGuildName(UUID guildId) {
        Party party = partiesAPI.getParty(guildId);
        return party == null ? "" : party.getName();
    }

    @Override
    public UUID getGuildId(UUID uuid) {
        Party party = partiesAPI.getPartyOfPlayer(uuid);
        if (party != null) {
            return party.getId();
        }
        return null;
    }

    @Override
    public boolean canManageGuildLand(UUID uuid, UUID guildId) {
        PartyPlayer partyPlayer = partiesAPI.getPartyPlayer(uuid);
        return partyPlayer != null && partyPlayer.getPartyId().equals(guildId) && partyPlayer.getRank() >= 10;
    }

    @Override
    public boolean areInSameGuild(UUID uuid1, UUID uuid2) {
        return partiesAPI.areInTheSameParty(uuid1, uuid2);
    }

    @Override
    public boolean hasGuild(UUID uuid) {
        return partiesAPI.isPlayerInParty(uuid);
    }

    @Override
    public boolean isGuildLeader(UUID uuid, UUID guildId) {
        Party party = partiesAPI.getParty(guildId);
        return party != null && party.getLeader() != null && party.getLeader().equals(uuid);
    }

    @Override
    public boolean isGuildMember(UUID uuid, UUID guildId) {
        Party party = partiesAPI.getParty(guildId);
        return party != null && party.getMembers().contains(uuid);
    }
}
