package fr.iban.lands.objects;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class SChunk {

	private String server;
	private String world;
	private int x;
	private int z;

	public SChunk(String server, String world, int x, int z) {
		this.server = server;
		this.world = world;
		this.x = x;
		this.z = z;
	}
	
	public SChunk(Chunk chunk) {
		this(CoreBukkitPlugin.getInstance().getServerName(), chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getWorld() {
		return world;
	}

	public void setWorld(String world) {
		this.world = world;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getZ() {
		return z;
	}

	public void setZ(int z) {
		this.z = z;
	}

	public boolean equalsChunk(Chunk chunk) {
		return chunk.getWorld().getName().equals(world) && chunk.getX() == x && chunk.getZ() == z && CoreBukkitPlugin.getInstance().getServerName().equals(server);
	}

	public Chunk getChunk() {
		return Bukkit.getWorld(world).getChunkAt(x, z);
	}

	public @NotNull CompletableFuture<Chunk> getChunkAsync() {
		return Bukkit.getWorld(world).getChunkAtAsync(x, z);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(server);
		sb.append(":");
		sb.append(world);
		sb.append(":");
		sb.append(x);
		sb.append(":");
		sb.append(z);
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((server == null) ? 0 : server.hashCode());
		result = prime * result + ((world == null) ? 0 : world.hashCode());
		result = prime * result + x;
		result = prime * result + z;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SChunk other = (SChunk) obj;
		if (server == null) {
			if (other.server != null)
				return false;
		} else if (!server.equals(other.server))
			return false;
		if (world == null) {
			if (other.world != null)
				return false;
		} else if (!world.equals(other.world))
			return false;
		if (x != other.x)
			return false;
		if (z != other.z)
			return false;
		return true;
	}
	
}
