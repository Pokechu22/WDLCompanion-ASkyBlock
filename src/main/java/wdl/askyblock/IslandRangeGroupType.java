package wdl.askyblock;

import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import wdl.range.IRangeGroup;
import wdl.range.IRangeGroupType;

public class IslandRangeGroupType<T extends IslandRangeProducer> implements
		IRangeGroupType<T> {
	/** Constructor for the range producer class */
	public static interface Supplier<T> {
		public T make(IRangeGroup group, PermLevel level);
	}
	private final Plugin plugin;
	private final Supplier<T> supplier;

	public IslandRangeGroupType(Plugin plugin, Supplier<T> supplier) {
		this.plugin = plugin;
		this.supplier = supplier;
	}
	
	@Override
	public T createRangeProducer(IRangeGroup group,
			ConfigurationSection config) {
		PermLevel level = PermLevel.parse(config.getString("requiredPerm"));
		T producer = supplier.make(group, level);
		plugin.getServer().getPluginManager().registerEvents(producer, plugin);
		
		return producer;
	}

	@Override
	public boolean isValidConfig(ConfigurationSection config,
			List<String> warnings, List<String> errors) {
		if (!config.isString("requiredPerm")) {
			errors.add("'requiredPerm' must be one of 'OWNER', 'TEAM_MEMBER', "
					+ "or 'COOP'!");
			return false;
		}
		String requiredPerm = config.getString("requiredPerm");
		if (PermLevel.parse(requiredPerm) == null) {
			errors.add("'requiredPerm' must be one of 'OWNER', 'TEAM_MEMBER', "
					+ "or 'COOP'; currently set to '" + requiredPerm + "'!");
			return false;
		}
		return true;
	}
	
	@Override
	public void dispose() {
		
	}
}
