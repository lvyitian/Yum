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
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.Plugin;

import pw.yumc.Yum.Yum;
import pw.yumc.YumCore.kit.PKit;

/**
 * Fork From LagMonitor: https://github.com/games647/LagMonitor.git
 * We can listen to events which are intended to run sync to the main thread.
 * If those events are fired on a async task the operation was likely not thread-safe.
 */
public class ThreadSafetyListener implements Listener {

    public ThreadSafetyListener(Yum yum) {
        Bukkit.getPluginManager().registerEvents(this, yum);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent chunkLoadEvent) {
        checkSafety(chunkLoadEvent);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent chunkUnloadEvent) {
        checkSafety(chunkUnloadEvent);
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent creatureSpawnEvent) {
        checkSafety(creatureSpawnEvent);
    }

    public void onInventoryOpen(InventoryOpenEvent inventoryOpenEvent) {
        checkSafety(inventoryOpenEvent);
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent itemSpawnEvent) {
        checkSafety(itemSpawnEvent);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent moveEvent) {
        checkSafety(moveEvent);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent quitEvent) {
        checkSafety(quitEvent);
    }

    public void onPlayerTeleport(PlayerTeleportEvent teleportEvent) {
        checkSafety(teleportEvent);
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent worldLoadEvent) {
        checkSafety(worldLoadEvent);
    }

    public void onWorldSave(WorldSaveEvent worldSaveEvent) {
        checkSafety(worldSaveEvent);
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent worldUnloadEvent) {
        checkSafety(worldUnloadEvent);
    }

    private void checkSafety(Event eventType) {
        if (Yum.mainThread != null && Thread.currentThread() != Yum.mainThread && !eventType.isAsynchronous()) {
            String eventName = eventType.getEventName();
            Plugin plugin = PKit.getOperatePlugin();
            if (plugin != null) { throw new IllegalAccessError("[Yum 线程安全]: 请勿异步调用一个同步事件 " + eventName + " 操作插件: "
                    + plugin.getName()); }
        }
    }
}
