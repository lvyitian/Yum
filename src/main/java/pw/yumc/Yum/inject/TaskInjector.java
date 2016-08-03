package pw.yumc.Yum.inject;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import cn.citycraft.PluginHelper.ext.kit.Reflect;
import cn.citycraft.PluginHelper.kit.StrKit;
import pw.yumc.Yum.commands.MonitorCommand;
import pw.yumc.Yum.managers.MonitorManager;

public class TaskInjector implements Runnable {
    private final static String prefix = "§6[§bYum §a任务监控§6] ";
    private final static String warn = "§c注意! §6插件 §b%s §6处理 §d%s §6任务 §c耗时 §4%sms §c平均耗时 §4%sms!";
    private final static String err = prefix + "§6插件 §b%s §6处理 §d%s §6任务时发生异常!";
    private final static String inject_error = prefix + "§6插件 §b%s §c注入能耗监控失败!";
    private final static String plugin_is_null = "插件不得为NULL!";
    private final Runnable originalTask;
    private final Plugin plugin;

    private final String taskName;

    public long totalTime;
    public int count;

    public TaskInjector(final Runnable originalTask, final Plugin plugin) {
        this.originalTask = originalTask;
        this.plugin = plugin;
        final Class<? extends Runnable> taskClass = getOriginalTask().getClass();
        taskName = StrKit.isBlank(taskClass.getSimpleName()) ? taskClass.getName() : taskClass.getSimpleName();
    }

    // 当前注入只能对TimerTask有效
    // 对于单次执行的任务 我们需要注册一个动态的代理
    public static void inject(final Plugin plugin) {
        Validate.notNull(plugin, plugin_is_null);
        try {
            final BukkitScheduler scheduler = Bukkit.getScheduler();
            final List<BukkitTask> pendingTasks = scheduler.getPendingTasks();
            for (final BukkitTask pendingTask : pendingTasks) {
                // 忽略异步任务
                if (pendingTask.isSync() && pendingTask.getOwner().equals(plugin)) {
                    final Runnable originalTask = Reflect.on(pendingTask).get("task");
                    if (originalTask instanceof TaskInjector) {
                        return;
                    }
                    final TaskInjector taskInjector = new TaskInjector(originalTask, plugin);
                    Reflect.on(pendingTask).set("task", taskInjector);
                }
            }
        } catch (final Throwable e) {
            MonitorManager.log(String.format(inject_error, plugin.getName()));
        }
    }

    public static void uninject(final Plugin plugin) {
        Validate.notNull(plugin, plugin_is_null);
        try {
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
        } catch (final Throwable e) {
        }
    }

    public Runnable getOriginalTask() {
        return originalTask;
    }

    @Override
    public void run() {
        try {
            final long start = System.nanoTime();
            originalTask.run();
            final long end = System.nanoTime();
            final long lag = end - start;
            totalTime += lag;
            count++;
            final long lagms = lag / MonitorManager.um;
            final long avglagms = totalTime / count / MonitorManager.um;
            if (Bukkit.isPrimaryThread() && lagms > MonitorManager.lagTime && avglagms > MonitorManager.lagTime) {
                MonitorManager.lagTip(String.format(warn, plugin.getName(), taskName, lagms, avglagms));
            }
            MonitorManager.addTask(plugin.getName(), lag);
        } catch (Throwable e) {
            while (e.getCause() != null) {
                e = e.getCause();
            }
            MonitorCommand.lastError = e;
            MonitorManager.printThrowable(String.format(err, plugin.getName(), taskName), e);
        }
    }
}
