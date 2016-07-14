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
    public int count;

    public TaskInjector(final Runnable originalTask) {
        this.originalTask = originalTask;
    }

    // 当前注入只能对TimerTask有效
    // 对于单次执行的任务 我们需要注册一个动态的代理
    public static void inject(final Plugin plugin) {
        final BukkitScheduler scheduler = Bukkit.getScheduler();
        final List<BukkitTask> pendingTasks = scheduler.getPendingTasks();
        for (final BukkitTask pendingTask : pendingTasks) {
            // 忽略异步任务
            if (pendingTask.isSync() && pendingTask.getOwner().equals(plugin)) {
                final Runnable originalTask = Reflect.on(pendingTask).get("task");
                if (originalTask instanceof TaskInjector) {
                    return;
                }
                final TaskInjector taskInjector = new TaskInjector(originalTask);
                Reflect.on(pendingTask).set("task", taskInjector);
            }
        }
    }

    public static void uninject(final Plugin plugin) {
        final BukkitScheduler scheduler = Bukkit.getScheduler();
        final List<BukkitTask> pendingTasks = scheduler.getPendingTasks();
        for (final BukkitTask pendingTask : pendingTasks) {
            // 忽略异步任务
            if (pendingTask.isSync() && pendingTask.getOwner().equals(plugin)) {
                final Runnable originalTask = Reflect.on(pendingTask).get("task");
                if (originalTask instanceof TaskInjector) {
                    Reflect.on(pendingTask).set("task", ((TaskInjector) originalTask).getOriginalTask());
                }
            }
        }
    }

    public Runnable getOriginalTask() {
        return originalTask;
    }

    @Override
    public void run() {
        final long start = System.nanoTime();
        // TODO 当操作大于10ms的时候添加一个Lag提示
        originalTask.run();
        final long end = System.nanoTime();
        totalTime += end - start;
        count++;
    }
}
