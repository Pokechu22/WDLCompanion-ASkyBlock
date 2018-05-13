package wdl.askyblock;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import wdl.range.IRangeGroup;
import wdl.range.IRangeProducer;
import wdl.range.ProtectionRange;

/**
 * {@link IRangeProducer} that grants players permission to download on
 * their ASkyBlock islands.  Generalized to support either ASkyBlock or AcidIsland.
 */
public abstract class IslandRangeProducer implements IRangeProducer, Listener {
	private final IRangeGroup group;
	private final PermLevel requiredPerm;
	
	protected IslandRangeProducer(IRangeGroup group, PermLevel requiredPerm) {
		this.group = group;
		this.requiredPerm = requiredPerm;
	}

	/** Calls ASkyBlockAPI.getIslandWorld */
	protected abstract World getIslandWorld();
	/** Gets Settings.island_protectionRange */
	protected abstract int getIslandProtectionRange();
	/** Calls ASkyBlockAPI.getOwner */
	protected abstract UUID getOwner(Location location);
	/** Calls ASkyBlockAPI.getIslandLocation */
	protected abstract Location getIslandLocation(UUID playerID);
	/** Calls ASkyBlockAPI.hasIsland */
	protected abstract boolean hasIsland(UUID playerID);
	/** Calls ASkyBlockAPI.getCoopIslands */
	protected abstract Set<Location> getCoopIslands(Player player);

	@Override
	public List<ProtectionRange> getInitialRanges(Player player) {
		ArrayList<ProtectionRange> ranges = new ArrayList<>();
		
		if (player.getWorld().equals(getIslandWorld())) {
			List<Location> locations = getIslandsFor(player.getUniqueId());
			int protectionRange = getIslandProtectionRange();
			
			for (Location location : locations) {
				ranges.add(getProtectionRangeForIsland(location, protectionRange));
			}
		}
		
		return ranges;
	}

	@Override
	public IRangeGroup getRangeGroup() {
		return group;
	}
	
	/**
	 * Occurs when a player has left an island team.
	 */
	protected void onLeaveIsland(UUID playerID, UUID teamLeaderID) {
		Player player = Bukkit.getPlayer(playerID);
		if (!group.isWDLPlayer(player)) {
			return;
		}
		if (requiredPerm != PermLevel.OWNER) {
			group.removeRangesByTags(player, getIslandTag(teamLeaderID));
		}
	}
	
	/**
	 * Occurs when a player has joined an island team.
	 */
	protected void onJoinIsland(UUID playerID, UUID teamLeaderID, Location islandLoc, int protectionRange) {
		Player player = Bukkit.getPlayer(playerID);
		if (!group.isWDLPlayer(player)) {
			return;
		}
		if (requiredPerm != PermLevel.OWNER) {
			String tag = getIslandTag(teamLeaderID);
			ProtectionRange range = getProtectionRangeForIsland(
					islandLoc, protectionRange);

			group.setTagRanges(player, tag, range);
		}
	}
	
	/**
	 * Occurs when a player has created a new island.
	 */
	protected void onNewIsland(Player player, Location islandLoc, int protectionRange) {
		if (!group.isWDLPlayer(player)) {
			return;
		}
		
		ProtectionRange range = getProtectionRangeForIsland(
				islandLoc, protectionRange);
		
		group.setTagRanges(player, player.getName() + "'s island", range);
	}
	
	/**
	 * Occurs when a player has joined another player's coop team.
	 */
	public void onCoopJoin(UUID playerID, UUID teamLeaderID, Location islandLoc, int protectionRange) {
		Player player = Bukkit.getPlayer(playerID);
		if (!group.isWDLPlayer(player)) {
			return;
		}

		if (requiredPerm == PermLevel.COOP) {
			String tag = getIslandTag(teamLeaderID);
			ProtectionRange range = getProtectionRangeForIsland(
					islandLoc, protectionRange);
			
			group.setTagRanges(player, tag, range);
		}
	}
	
	/**
	 * Occurs when a player has left another player's coop team.
	 */
	protected void onCoopLeave(UUID playerID, UUID islandOwner) {
		Player player = Bukkit.getPlayer(playerID);
		if (!group.isWDLPlayer(player)) {
			return;
		}

		if (requiredPerm == PermLevel.COOP) {
			group.removeRangesByTags(player, getIslandTag(islandOwner));
		}
	}
	
	/**
	 * Gets the locations for all islands useable by the given player.
	 * Owned is defined as accessible under {@link #requiredPerm}.
	 * 
	 * @param playerID The unique ID of the player.
	 * @return A list of all islands the player can use. 
	 */
	private List<Location> getIslandsFor(UUID playerID) {
		List<Location> returned = new ArrayList<>();
		
		Location islandLoc = getIslandLocation(playerID);
		if (islandLoc != null) {
			if (requiredPerm == PermLevel.OWNER) {
				if (hasIsland(playerID)) {
					// Island must be owned by that player.
					returned.add(islandLoc);
				}
			} else {
				returned.add(islandLoc);
			}
		}
		
		if (requiredPerm == PermLevel.COOP) {
			returned.addAll(getCoopIslands(Bukkit.getPlayer(playerID)));
		}
		
		return returned;
	}
	
	
	
	/**
	 * Gets a {@link ProtectionRange} for the island at the given position.
	 * 
	 * @param center The center location of the island.
	 * @param protectionRange The distance an island is protected for.
	 */
	private ProtectionRange getProtectionRangeForIsland(Location center,
			int protectionRange) {
		int x1 = center.getBlockX() - protectionRange / 2;
		int z1 = center.getBlockZ() - protectionRange / 2;
		int x2 = center.getBlockX() + protectionRange / 2;
		int z2 = center.getBlockZ() + protectionRange / 2;
		String tag = getIslandTag(getOwner(center));
		return new ProtectionRange(tag, x1, z1, x2, z2);
	}
	
	/**
	 * Gets the name of the player with the given unique ID.
	 */
	private String getPlayerName(UUID uniqueID) {
		return Bukkit.getOfflinePlayer(uniqueID).getName();
	}
	
	/**
	 * Gets a tag that can be used for a player's island.
	 */
	private String getIslandTag(UUID ownerID) {
		return getPlayerName(ownerID) + "'s island";
	}
	
	@Override
	public void dispose() {
		HandlerList.unregisterAll(this);
	}
}
