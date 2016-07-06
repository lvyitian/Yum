package pw.yumc.Yum.inject;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import cn.citycraft.PluginHelper.ext.kit.Reflect;

public class TaskInjector implements Runnable {

    private final Runnable originalTask;

    public long totalTime;
    public long count;

    public TaskInjector(final Runnable originalTask) {
        this.originalTask = originalTask;
    }

    // sadly it works only with interval tasks
    // for single runs we would have to register a dynamic proxy
    public static void inject(final Plugin plugin) {
        final BukkitScheduler scheduler = Bukkit.getScheduler();
        final List<BukkitTask> pendingTasks = scheduler.getPendingTasks();
        for (final BukkitTask pendingTask : pendingTasks) {
            // we could ignore async tasks for now
            if (pendingTask.isSync() && pendingTask.getOwner().equals(plugin)) {
                final Runnable originalTask = Reflect.on(pendingTask).get("task");
                final TaskInjector taskInjector = new TaskInjector(originalTask);
                Reflect.on(pendingTask).set("task", taskInjector);
            }
        }
    }

    public static void uninject(final Plugin plugin) {
        final BukkitScheduler scheduler = Bukkit.getScheduler();
        final List<BukkitTask> pendingTasks = scheduler.getPendingTasks();
        for (final BukkitTask pendingTask : pendingTasks) {
            // we could ignore async tasks for now
            if (pendingTask.isSync() && pendingTask.getOwner().equals(plugin)) {
                final TaskInjector originalTask = Reflect.on(pendingTask).get("task");
                Reflect.on(pendingTask).set("task", originalTask.getOriginalTask());
            }
        }
    }

    public Runnable getOriginalTask() {
        return originalTask;
    }

    @Override
    public void run() {
        final long start = System.nanoTime();
        // todo add a more aggressive 10 ms cpu sample
        originalTask.run();
        final long end = System.nanoTime();

        totalTime += end - start;
        count++;
    }
}
