package wdl.askyblock;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;
import org.mcstats.Metrics.Graph;
import org.mcstats.Metrics.Plotter;

import com.wasteofplastic.askyblock.ASkyBlockAPI;

import wdl.RangeGroupTypeRegistrationEvent;
import wdl.range.IRangeProducer;

/**
 * Plugin that provides simple support for ASkyBlock chunk overrides.
 * 
 * @see https://github.com/tastybento/askyblock
 */
public class ASkyBlockSupportPlugin extends JavaPlugin implements Listener {
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		
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
			String askyblockVersion = getProvidingPlugin(ASkyBlockAPI.class)
					.getDescription().getFullName();
			asbVersionGraph.addPlotter(new StringPlotter(askyblockVersion));
			
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
		e.addRegistration("ASkyBlock island", new ASkyBlockRangeGroupType(this));
	}
}
