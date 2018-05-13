package wdl.askyblock;

import wdl.askyblock.IslandRangeGroupType.Supplier;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;
import org.mcstats.Metrics.Graph;
import org.mcstats.Metrics.Plotter;

import wdl.RangeGroupTypeRegistrationEvent;
import wdl.range.IRangeGroup;
import wdl.range.IRangeProducer;

/**
 * Plugin that provides simple support for ASkyBlock chunk overrides.
 * 
 * @see https://github.com/tastybento/askyblock
 */
public class ASkyBlockSupportPlugin extends JavaPlugin implements Listener {
	/**
	 * Attempts to find a class with the given name, returning null otherwise.
	 */
	private Class<?> tryGetClass(String name) {
		try {
			return Class.forName(name);
		} catch (Exception ex) {
			return null;
		}
	}
	/**
	 * Gets the version of the plugin associated with the given class.
	 * If the class is null, returns N/A.
	 */
	private String getPluginVersion(Class<?> cls) {
		if (cls != null) {
			return getProvidingPlugin(cls).getDescription().getFullName();
		} else {
			return "N/A";
		}
	}

	private boolean hasAskyblock, hasAcidIsland;

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		Class<?> askyblockClass = tryGetClass("com.wasteofplastic.askyblock.ASkyBlockAPI");
		String askyblockVersion = getPluginVersion(askyblockClass);
		if (hasAskyblock = (askyblockClass != null)) {
			getLogger().info("Found ASkyBlock: " + askyblockVersion);
		} else {
			getLogger().info("Did not find ASkyBlock.");
		}
		Class<?> acidIslandClass = tryGetClass("com.wasteofplastic.acidisland.ASkyBlockAPI");
		String acidIslandVersion = getPluginVersion(acidIslandClass);
		if (hasAcidIsland = (acidIslandClass != null)) {
			getLogger().info("Found AcidIsland: " + acidIslandVersion);
		} else {
			getLogger().info("Did not find AcidIsland.");
		}
		if (!hasAskyblock && !hasAcidIsland) {
			getLogger().warning("Found neither AcidIsland nor ASkyBlock.  Please install at least one of those, or else this plugin does nothing useful.");
		}
		try {
			class StringPlotter extends Plotter {
				public StringPlotter(String str) {
					super(str);
				}
				
				@Override
				public int getValue() {
					return 1;
				}
			}
			
			Metrics metrics = new Metrics(this);
			
			Graph asbVersionGraph = metrics.createGraph("askyblockVersion");
			asbVersionGraph.addPlotter(new StringPlotter(askyblockVersion));
			
			Graph aiVersionGraph = metrics.createGraph("acidislandVersion");
			aiVersionGraph.addPlotter(new StringPlotter(acidIslandVersion));
			
			Graph wdlcVersionGraph = metrics.createGraph("wdlcompanionVersion");
			String wdlcVersion = getProvidingPlugin(IRangeProducer.class)
					.getDescription().getFullName();
			wdlcVersionGraph.addPlotter(new StringPlotter(wdlcVersion));
			
			metrics.start();
		} catch (Exception e) {
			getLogger().warning("Failed to start PluginMetrics :(");
		}
	}
	
	@EventHandler
	public void registerRangeGroupTypes(RangeGroupTypeRegistrationEvent e) {
		if (hasAskyblock) {
			e.addRegistration("ASkyBlock island",
				new IslandRangeGroupType<ASkyBlockRangeProducer>(this,
					new Supplier<ASkyBlockRangeProducer>() {
						@Override
						public ASkyBlockRangeProducer make(IRangeGroup group, PermLevel level) {
							return new ASkyBlockRangeProducer(group, level);
						}
					}
			));
		}
		if (hasAcidIsland) {
			e.addRegistration("AcidIsland island", new IslandRangeGroupType<AcidIslandRangeProducer>(this,
					new Supplier<AcidIslandRangeProducer>() {
						@Override
						public AcidIslandRangeProducer make(IRangeGroup group, PermLevel level) {
							return new AcidIslandRangeProducer(group, level);
						}
					}
			));
		}
	}
}
