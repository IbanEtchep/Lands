package fr.iban.lands.api;

import fr.iban.lands.enums.LandType;
import fr.iban.lands.model.SChunk;
import fr.iban.lands.model.land.Land;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.UUID;

public interface LandService {

    void renameLand(Land land, Player player, String name);

    void claim(SChunk chunk, Land land);

    void claim(Player player, List<SChunk> chunks, Land land);

    void claim(Player player, SChunk chunk, Land land);

    void unclaim(Player player, SChunk chunk);

    void unclaim(Chunk chunk);

    void unclaim(SChunk chunk);

    void transferClaims(UUID from, UUID to);

    void giveClaims(Player player, UUID target, int amount);

    Land createLand(Player creator, String name, LandType type, UUID landOwner);

    void createSubland(Player player, Land superLand, String name);

    void ban(Player sender, Land land, UUID uuid);

    void unban(Player sender, Land land, UUID uuid);
}