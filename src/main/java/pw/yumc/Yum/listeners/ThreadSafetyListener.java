package pw.yumc.Yum.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import pw.yumc.Yum.Yum;

/**
 * Fork From LagMonitor: https://github.com/games647/LagMonitor.git
 * We can listen to events which are intended to run sync to the main thread.
 * If those events are fired on a async task the operation was likely not thread-safe.
 */
public class ThreadSafetyListener implements Listener {

    private final Thread mainThread = Thread.currentThread();

    public ThreadSafetyListener(final Yum yum) {
        Bukkit.getPluginManager().registerEvents(this, yum);
    }

    @EventHandler
    public void onChunkLoad(final ChunkLoadEvent chunkLoadEvent) {
        checkSafety(chunkLoadEvent);
    }

    @EventHandler
    public void onChunkUnload(final ChunkUnloadEvent chunkUnloadEvent) {
        checkSafety(chunkUnloadEvent);
    }

    @EventHandler
    public void onCreatureSpawn(final CreatureSpawnEvent creatureSpawnEvent) {
        checkSafety(creatureSpawnEvent);
    }

    @EventHandler
    public void onInventoryOpen(final InventoryOpenEvent inventoryOpenEvent) {
        checkSafety(inventoryOpenEvent);
    }

    @EventHandler
    public void onItemSpawn(final ItemSpawnEvent itemSpawnEvent) {
        checkSafety(itemSpawnEvent);
    }

    @EventHandler
    public void onPlayerMove(final PlayerMoveEvent moveEvent) {
        checkSafety(moveEvent);
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent quitEvent) {
        checkSafety(quitEvent);
    }

    @EventHandler
    public void onPlayerTeleport(final PlayerTeleportEvent teleportEvent) {
        checkSafety(teleportEvent);
    }

    @EventHandler
    public void onPluginDisable(final PluginDisableEvent pluginDisableEvent) {
        checkSafety(pluginDisableEvent);
    }

    @EventHandler
    public void onPluginEnable(final PluginEnableEvent pluginEnableEvent) {
        checkSafety(pluginEnableEvent);
    }

    @EventHandler
    public void onWorldLoad(final WorldLoadEvent worldLoadEvent) {
        checkSafety(worldLoadEvent);
    }

    @EventHandler
    public void onWorldSave(final WorldSaveEvent worldSaveEvent) {
        checkSafety(worldSaveEvent);
    }

    @EventHandler
    public void onWorldUnload(final WorldUnloadEvent worldUnloadEvent) {
        checkSafety(worldUnloadEvent);
    }

    private void checkSafety(final Event eventType) {
        if (Thread.currentThread() != mainThread && !eventType.isAsynchronous()) {
            final String eventName = eventType.getEventName();
            throw new IllegalAccessError("[Yum 线程安全]: 请勿异步调用一个同步事件 " + eventName);
        }
    }
}
