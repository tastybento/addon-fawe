package bskyblock.addon.fawe;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.world.World;

import us.tastybento.bskyblock.api.events.IslandBaseEvent;
import us.tastybento.bskyblock.api.events.island.IslandEvent;
import us.tastybento.bskyblock.api.events.island.IslandEvent.IslandCreateEvent;
import us.tastybento.bskyblock.api.events.island.IslandEvent.IslandResetEvent;
import us.tastybento.bskyblock.api.events.island.IslandEvent.Reason;

public class PasteSchematic implements Listener {

    private static final boolean DEBUG = false;
    private Fawe addon;
    private boolean disabled = false;
    private Map<String, File> schematic;

    public PasteSchematic(Fawe fawe) {
        this.addon = fawe;
        schematic = new HashMap<>();
        saveDefaults();
        loadSchematics();
    }

    private void loadSchematics() {
        schematic.put("island", new File(addon.getDataFolder() + File.separator + "schematics", "island.schematic"));
        // Add others
    }

    private void saveDefaults() {
        File schematicsFolder = new File (addon.getDataFolder(), "schematics");
        if (!schematicsFolder.exists()) {
            schematicsFolder.mkdirs();
            // Save the default schems
            addon.saveResource("schematics/island.schematic", false);
            addon.saveResource("schematics/double.schematic", false);
            addon.saveResource("schematics/harder.schematic", false);
            addon.saveResource("schematics/nether.schematic", false);
        }
        
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onIslandCreate(final IslandCreateEvent event) {
        if (!disabled) {
            event.setCancelled(true);
            paste(event);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onIslandReset(final IslandResetEvent event) {
        if (!disabled) {
            event.setCancelled(true);
            paste(event);
        }
    }

    private void paste(IslandBaseEvent event) {
        // Paste the file
        World world = new BukkitWorld(event.getLocation().getWorld());
        Vector position = new Vector(event.getLocation().getBlockX(), event.getLocation().getBlockY(), event.getLocation().getBlockZ());
        boolean allowUndo = false;
        boolean noAir = true;
        try {
            if (DEBUG)
                addon.getLogger().info("DEBUG: Trying to paste");
            EditSession editSession = ClipboardFormat.SCHEMATIC.load(schematic.get("island")).paste(world, position, allowUndo, !noAir, (Transform) null);
            editSession.addNotifyTask(new Runnable() {

                public void run() {
                    if (DEBUG)
                        addon.getLogger().info("DEBUG: Pasting completed!");
                    // Get the player. They may have logged out so check if they are online
                    Player player = addon.getServer().getPlayer(event.getPlayerUUID());
                    if (player != null && player.isOnline()) {
                        // Teleport player to their island
                        if (DEBUG)
                            addon.getLogger().info("DEBUG: teleporting");
                        addon.getBSkyBlock().getIslands().homeTeleport(player);
                    }
                    // Fire exit event
                    IslandBaseEvent ev = IslandEvent.builder()
                            .involvedPlayer(player.getUniqueId())
                            .reason(Reason.CREATED)
                            .island(event.getIsland())
                            .location(event.getLocation())
                            .build();
                    addon.getServer().getPluginManager().callEvent(ev);

                }});
        } catch (IOException e) {
            e.printStackTrace();
            event.setCancelled(false);
        }


    }
}
