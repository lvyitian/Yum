package pw.yumc.Yum.inject;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import pw.yumc.Yum.commands.MonitorCommand;
import pw.yumc.Yum.managers.MonitorManager;
import pw.yumc.YumCore.kit.StrKit;
import pw.yumc.YumCore.reflect.Reflect;

public class TaskInjector implements Runnable {
    private static String prefix = "§6[§bYum §a任务监控§6] ";
    private static String warn = "§c注意! §6插件 §b%s §6处理 §d%s §6任务 §c耗时 §4%sms §c平均耗时 §4%sms!";
    private static String err = prefix + "§6插件 §b%s §6处理 §d%s §6任务时发生异常!";
    private static String inject_error = prefix + "§6插件 §b%s §c注入能耗监控失败!";
    private static String plugin_is_null = "插件不得为NULL!";
    private Runnable originalTask;
    private Plugin plugin;

    private String taskName;

    public long totalTime;
    public int count;

    public TaskInjector(Runnable originalTask, Plugin plugin) {
        this.originalTask = originalTask;
        this.plugin = plugin;
        Class<? extends Runnable> taskClass = getOriginalTask().getClass();
        taskName = StrKit.isBlank(taskClass.getSimpleName()) ? taskClass.getName() : taskClass.getSimpleName();
    }

    // 当前注入只能对TimerTask有效
    // 对于单次执行的任务 我们需要注册一个动态的代理
    public static void inject(Plugin plugin) {
        Validate.notNull(plugin, plugin_is_null);
        try {
            BukkitScheduler scheduler = Bukkit.getScheduler();
            List<BukkitTask> pendingTasks = scheduler.getPendingTasks();
            for (BukkitTask pendingTask : pendingTasks) {
                // 忽略异步任务
                if (pendingTask.isSync() && pendingTask.getOwner().equals(plugin)) {
                    Runnable originalTask = Reflect.on(pendingTask).get("task");
                    if (originalTask instanceof TaskInjector) { return; }
                    TaskInjector taskInjector = new TaskInjector(originalTask, plugin);
                    Reflect.on(pendingTask).set("task", taskInjector);
                }
            }
        } catch (Throwable e) {
            MonitorManager.log(String.format(inject_error, plugin.getName()));
        }
    }

    public static void uninject(Plugin plugin) {
        Validate.notNull(plugin, plugin_is_null);
        try {
            BukkitScheduler scheduler = Bukkit.getScheduler();
            List<BukkitTask> pendingTasks = scheduler.getPendingTasks();
            for (BukkitTask pendingTask : pendingTasks) {
                // 忽略异步任务
                if (pendingTask.isSync() && pendingTask.getOwner().equals(plugin)) {
                    Runnable originalTask = Reflect.on(pendingTask).get("task");
                    if (originalTask instanceof TaskInjector) {
                        Reflect.on(pendingTask).set("task", ((TaskInjector) originalTask).getOriginalTask());
                    }
                }
            }
        } catch (Throwable ignored) {
        }
    }

    public Runnable getOriginalTask() {
        return originalTask;
    }

    @Override
    public void run() {
        try {
            long start = System.nanoTime();
            originalTask.run();
            long end = System.nanoTime();
            long lag = end - start;
            totalTime += lag;
            count++;
            long lagms = lag / MonitorManager.um;
            long avglagms = totalTime / count / MonitorManager.um;
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
