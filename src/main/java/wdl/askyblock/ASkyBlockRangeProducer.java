package wdl.askyblock;

import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import com.wasteofplastic.askyblock.ASkyBlockAPI;
import com.wasteofplastic.askyblock.Settings;
import com.wasteofplastic.askyblock.events.CoopJoinEvent;
import com.wasteofplastic.askyblock.events.CoopLeaveEvent;
import com.wasteofplastic.askyblock.events.IslandJoinEvent;
import com.wasteofplastic.askyblock.events.IslandLeaveEvent;
import com.wasteofplastic.askyblock.events.IslandNewEvent;

import wdl.range.IRangeGroup;

/**
 * Range producer that specifically targets ASkyBlock.
 * The main difference is the imports.
 */
public class ASkyBlockRangeProducer extends IslandRangeProducer {
	private final ASkyBlockAPI api;

	public ASkyBlockRangeProducer(IRangeGroup group, PermLevel requiredPerm) {
		super(group, requiredPerm);
		api = ASkyBlockAPI.getInstance();
	}

	@Override
	protected World getIslandWorld() {
		return api.getIslandWorld();
	}

	@Override
	protected int getIslandProtectionRange() {
		return Settings.islandProtectionRange;
	}

	@Override
	protected UUID getOwner(Location location) {
		return api.getOwner(location);
	}

	@Override
	protected Location getIslandLocation(UUID playerID) {
		return api.getIslandLocation(playerID);
	}

	@Override
	protected boolean hasIsland(UUID playerID) {
		return api.hasIsland(playerID);
	}

	@Override
	protected Set<Location> getCoopIslands(Player player) {
		return api.getCoopIslands(player);
	}

	/**
	 * Occurs when ASkyBlock fires an {@link IslandLeaveEvent}: a player has
	 * left an island team.
	 */
	@EventHandler
	public void onLeaveIsland(IslandLeaveEvent e) {
		super.onLeaveIsland(e.getPlayer(), e.getTeamLeader());
	}
	
	/**
	 * Occurs when ASkyBlock fires an {@link IslandJoinEvent}: a player has
	 * joined an island team.
	 */
	@EventHandler
	public void onJoinIsland(IslandJoinEvent e) {
		super.onJoinIsland(e.getPlayer(), e.getTeamLeader(), e.getIslandLocation(), e.getProtectionSize());
	}
	
	/**
	 * Occurs when ASkyBlock fires an {@link IslandNewEvent}: a player has
	 * created a new island.
	 */
	@EventHandler
	public void onNewIsland(IslandNewEvent e) {
		super.onNewIsland(e.getPlayer(), e.getIslandLocation(), e.getProtectionSize());
	}
	
	/**
	 * Occurs when ASkyBlock fires a {@link CoopJoinEvent}: A player has
	 * joined another player's coop team.
	 */
	@EventHandler
	public void onCoopJoin(CoopJoinEvent e) {
		super.onCoopJoin(e.getPlayer(), e.getTeamLeader(), e.getIslandLocation(), e.getProtectionSize());
	}
	
	/**
	 * Occurs when ASkyBlock fires a {@link CoopLeaveEvent}: A player has
	 * left another player's coop team.
	 */
	@EventHandler
	public void onCoopLeave(CoopLeaveEvent e) {
		super.onCoopLeave(e.getPlayer(), e.getIslandOwner());
	}
}
