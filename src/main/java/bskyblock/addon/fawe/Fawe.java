/**
 * 
 */
package bskyblock.addon.fawe;

import org.bukkit.event.Listener;

import us.tastybento.bskyblock.api.addons.Addon;

/**
 * @author tastybento
 *
 */
public class Fawe extends Addon implements Listener {
    
    public void onEnable() {

        // Register listener
        getServer().getPluginManager().registerEvents(new PasteSchematic(this), getBSkyBlock());
        
    }

    public void onDisable() {
        // TODO Auto-generated method stub
        
    }    
    
}
