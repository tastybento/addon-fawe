/**
 * 
 */
package bskyblock.addon.fawe;

import java.io.File;
import java.io.IOException;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.world.World;

import us.tastybento.bskyblock.api.addons.Addon;
import us.tastybento.bskyblock.api.events.IslandBaseEvent;
import us.tastybento.bskyblock.api.events.island.IslandEvent;
import us.tastybento.bskyblock.api.events.island.IslandEvent.IslandCreateEvent;
import us.tastybento.bskyblock.api.events.island.IslandEvent.IslandResetEvent;
import us.tastybento.bskyblock.api.events.island.IslandEvent.Reason;

/**
 * @author tastybento
 *
 */
public class Fawe extends Addon implements Listener {
    
    private File schematic;


    public void onEnable() {
        schematic = new File(getDataFolder(), "test.schematic");
        if (!schematic.exists()) {
            getLogger().severe(schematic.getAbsolutePath() + " not found!");
        }

        // Register listeners
        PluginManager manager = getServer().getPluginManager();
        // Player join events
        manager.registerEvents(this, getBSkyBlock());
        
    }

    public void onDisable() {
        // TODO Auto-generated method stub
        
    }    
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onIslandCreat(final IslandBaseEvent event) {
        getLogger().info("DEBUG: island base create event called");
        if (event instanceof IslandCreateEvent) {
            getLogger().info("DEBUG: create ");
        }
    }
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onIslandCreate(final IslandCreateEvent event) {
        getLogger().info("DEBUG: island create event called");
        event.setCancelled(true);
        paste(event);
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onIslandReset(final IslandResetEvent event) {
        getLogger().info("DEBUG: island reset event called");
        event.setCancelled(true);
        paste(event);
    }

    private void paste(IslandBaseEvent event) {
        // Paste the file
        World world = new BukkitWorld(event.getLocation().getWorld());
        Vector position = new Vector(event.getLocation().getBlockX(), event.getLocation().getBlockY(), event.getLocation().getBlockZ());
        boolean allowUndo = false;
        boolean noAir = true;
        try {
            getLogger().info("DEBUG: Trying to paste");
            EditSession editSession = ClipboardFormat.SCHEMATIC.load(schematic).paste(world, position, allowUndo, !noAir, (Transform) null);
            editSession.addNotifyTask(new Runnable() {

                public void run() {
                    getLogger().info("DEBUG: Pasting completed!");
                    // Get the player. They may have logged out so check if they are online
                    Player player = getServer().getPlayer(event.getPlayerUUID());
                    if (player != null && player.isOnline()) {
                        // Teleport player to their island
                        getLogger().info("DEBUG: teleporting");
                        getBSkyBlock().getIslands().homeTeleport(player);
                    }
                    // Fire exit event
                    IslandBaseEvent ev = IslandEvent.builder()
                            .involvedPlayer(player.getUniqueId())
                            .reason(Reason.CREATED)
                            .island(event.getIsland())
                            .location(event.getLocation())
                            .build();
                    getServer().getPluginManager().callEvent(ev);

                }});
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        
    }
}
