package pw.yumc.Yum.commands;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.TimedRegisteredListener;
import org.bukkit.scheduler.BukkitTask;

import cn.citycraft.PluginHelper.ext.kit.Reflect;
import cn.citycraft.PluginHelper.kit.PluginKit;
import cn.citycraft.PluginHelper.kit.StrKit;
import pw.yumc.Yum.Yum;
import pw.yumc.Yum.api.YumAPI;
import pw.yumc.Yum.inject.CommandInjector;
import pw.yumc.Yum.inject.ListenerInjector;
import pw.yumc.Yum.inject.TaskInjector;
import pw.yumc.Yum.managers.MonitorManager;
import pw.yumc.Yum.managers.MonitorManager.MonitorInfo;
import pw.yumc.YumCore.commands.CommandArgument;
import pw.yumc.YumCore.commands.CommandExecutor;
import pw.yumc.YumCore.commands.CommandManager;
import pw.yumc.YumCore.commands.annotation.Async;
import pw.yumc.YumCore.commands.annotation.Cmd;
import pw.yumc.YumCore.commands.annotation.Help;

/**
 *
 * @since 2016年7月6日 下午5:13:32
 * @author 喵♂呜
 */
public class MonitorCommand implements CommandExecutor {
    public static Throwable lastError = null;

    private final String prefix = "§6[§bYum §a能耗监控§6] ";

    private final String total = "§6总耗时: §a%.2f毫秒 ";
    private final String count = "§6执行次数: §b%s次 ";
    private final String avg = "§6平均耗时: §d%.5f毫秒!";
    private final String avg_warn = "§6平均耗时: §c%.5f毫秒!";

    private final String reinject = prefix + "§a能耗监控器重载完毕!";
    private final String injected = prefix + "§a插件 §b%s §a成功注入能耗监控器!";
    private final String uninjected = prefix + "§a插件 §b%s §a成功撤销能耗监控器!";
    private final String notEnable = prefix + "§c插件 §b%s §c未成功加载 无法执行注入!";

    private final String lagprefix = "   §6插件名称        §c主线程耗时  §a命令耗时  §b事件耗时  §d任务耗时";
    private final String laglist = "§6%-2s §b%-15s §c%-11.2f §a%-9.2f §b%-9.2f §d%-9.2f";

    private final String no_error = prefix + "§a自服务器启动以来尚未发现报错!";
    private final String last_error = prefix + "§c最后一次错误异常由 §b%s §c造成 详细如下:";

    private final String p_n_f = prefix + "§c插件 §b%s §c不存在!";

    private final double um = 1000000.00;

    public MonitorCommand(final Yum yum) {
        new CommandManager("monitor", this, PluginTabComplete.instence);
    }

    @Cmd(aliases = "c", minimumArguments = 1)
    @Help(value = "查看插件命令能耗", possibleArguments = "[插件名称]")
    @Async
    public void cmd(final CommandArgument e) {
        final String pname = e.getArgs()[0];
        final CommandSender sender = e.getSender();
        if (Bukkit.getPluginManager().getPlugin(pname) == null) {
            sender.sendMessage(String.format(p_n_f, pname));
            return;
        }
        final PluginManager pluginManager = Bukkit.getPluginManager();
        final SimpleCommandMap commandMap = Reflect.on(pluginManager).get("commandMap");
        sender.sendMessage(prefix + "§6插件 §b" + pname + " §6的命令能耗如下!");
        final Map<String, Command> temp = new HashMap<>();
        for (final Command command : commandMap.getCommands()) {
            if (command instanceof PluginCommand) {
                final PluginCommand pluginCommand = (PluginCommand) command;
                final Plugin plugin = pluginCommand.getPlugin();
                if (plugin.getName().equalsIgnoreCase(pname)) {
                    temp.put(command.getName(), command);
                }
            }
        }
        for (final Entry<String, Command> command : temp.entrySet()) {
            final org.bukkit.command.CommandExecutor executor = Reflect.on(command.getValue()).get("executor");
            if (executor instanceof CommandInjector) {
                final CommandInjector injected = (CommandInjector) executor;
                final StringBuffer str = new StringBuffer();
                str.append("§6- §e" + command.getValue().getName() + " ");
                str.append(String.format(total, injected.totalTime / um));
                str.append(String.format(count, injected.count));
                if (injected.count != 0) {
                    str.append(String.format(avg, injected.totalTime / um / injected.count));
                }
                e.getSender().sendMessage(str.toString());
            }
        }
    }

    @Cmd(aliases = "e", minimumArguments = 1)
    @Help(value = "查看插件事件能耗", possibleArguments = "[插件名称]")
    @Async
    public void event(final CommandArgument e) throws InstantiationException, IllegalAccessException, NoSuchFieldException {
        final String pname = e.getArgs()[0];
        final CommandSender sender = e.getSender();
        final Plugin plugin = Bukkit.getPluginManager().getPlugin(pname);
        if (plugin == null) {
            sender.sendMessage(String.format(p_n_f, pname));
            return;
        }
        sender.sendMessage(prefix + "§6插件 §b" + pname + " §6的事件能耗如下!");
        final List<RegisteredListener> listeners = HandlerList.getRegisteredListeners(plugin);
        final Map<String, Long> eventTotalTime = new HashMap<>();
        final Map<String, Integer> eventCount = new HashMap<>();
        for (final RegisteredListener listener : listeners) {
            if (listener instanceof TimedRegisteredListener) {
                final TimedRegisteredListener trl = (TimedRegisteredListener) listener;
                eventTotalTime.put(trl.getEventClass().getSimpleName(), trl.getTotalTime());
                eventCount.put(trl.getEventClass().getSimpleName(), trl.getCount());
                continue;
            }
            EventExecutor executor = Reflect.on(listener).get("executor");
            if (listener.getClass().getName().contains("PWPRegisteredListener")) {
                final Field f = Reflect.getDeclaredField(RegisteredListener.class, "executor");
                f.setAccessible(true);
                executor = (EventExecutor) f.get(listener);
            }
            if (executor instanceof ListenerInjector) {
                final ListenerInjector injected = (ListenerInjector) executor;
                for (final String entry : injected.eventTotalTime.keySet()) {
                    if (eventTotalTime.containsKey(entry)) {
                        eventTotalTime.put(entry, eventTotalTime.get(entry) + injected.eventTotalTime.get(entry));
                        eventCount.put(entry, eventCount.get(entry) + injected.eventCount.get(entry));
                    } else {
                        eventTotalTime.put(entry, injected.eventTotalTime.get(entry));
                        eventCount.put(entry, injected.eventCount.get(entry));
                    }
                }
            }
        }
        for (final String event : eventTotalTime.keySet()) {
            final StringBuffer str = new StringBuffer();
            str.append(String.format("§6- §e%-25s ", event));
            str.append(String.format(total, eventTotalTime.get(event) / um));
            str.append(String.format(count, eventCount.get(event)));
            if (eventCount.get(event) != 0) {
                final double avgTime = eventTotalTime.get(event) / um / eventCount.get(event);
                str.append(String.format(avgTime < 10 ? avg : avg_warn, avgTime));
            }
            e.getSender().sendMessage(str.toString());
        }
    }

    @Cmd(aliases = "i", minimumArguments = 1)
    @Help(value = "注入能耗监控器", possibleArguments = "[插件名称]")
    public void inject(final CommandArgument e) {
        final String pname = e.getArgs()[0];
        final CommandSender sender = e.getSender();
        final Plugin plugin = Bukkit.getPluginManager().getPlugin(pname);
        if (plugin == null) {
            sender.sendMessage(String.format(p_n_f, pname));
            return;
        }
        if (plugin.isEnabled()) {
            YumAPI.inject(plugin);
            sender.sendMessage(String.format(injected, pname));
        } else {
            sender.sendMessage(String.format(notEnable, pname));
        }
    }

    @Cmd(aliases = "l")
    @Help("查看插件总耗时")
    @Async
    public void lag(final CommandArgument e) {
        final CommandSender sender = e.getSender();
        final Map<String, Long> mm = MonitorManager.getMonitor();
        int i = 0;
        final int max = e.getArgs().length > 0 ? Integer.parseInt(e.getArgs()[0]) : 8;
        sender.sendMessage(lagprefix);
        for (final Entry<String, Long> entry : mm.entrySet()) {
            if (++i > max) {
                break;
            }
            final MonitorInfo mi = MonitorManager.getMonitorInfo(entry.getKey());
            sender.sendMessage(String.format(laglist, i, entry.getKey(), mi.monitor, mi.cmd, mi.event, mi.task));
        }
    }

    @Cmd(aliases = "la")
    @Help("查看最后一次报错")
    @Async
    public void lasterror(final CommandArgument e) {
        final CommandSender sender = e.getSender();
        if (lastError == null) {
            sender.sendMessage(no_error);
            return;
        }
        final Plugin plugin = PluginKit.getOperatePlugin(lastError.getStackTrace());
        if (plugin != null) {
            sender.sendMessage(String.format(last_error, plugin.getName()));
            lastError.printStackTrace();
        }
    }

    @Cmd
    public void lk(final CommandArgument e) {
        MonitorManager.sendObject(e.getSender());
    }

    @Cmd(aliases = "ri")
    @Help("重载能耗监控器")
    public void reinject(final CommandArgument e) {
        final CommandSender sender = e.getSender();
        YumAPI.updateInject();
        sender.sendMessage(reinject);
    }

    @Cmd(aliases = "t", minimumArguments = 1)
    @Help(value = "查看插件任务能耗", possibleArguments = "[插件名称]")
    @Async
    public void task(final CommandArgument e) {
        final String pname = e.getArgs()[0];
        final CommandSender sender = e.getSender();
        final Plugin plugin = Bukkit.getPluginManager().getPlugin(pname);
        if (plugin == null) {
            sender.sendMessage(String.format(p_n_f, pname));
            return;
        }
        final List<BukkitTask> pendingTasks = Bukkit.getScheduler().getPendingTasks();
        sender.sendMessage(prefix + "§6插件 §b" + pname + " §6的任务能耗如下!");
        for (final BukkitTask pendingTask : pendingTasks) {
            if (pendingTask.getOwner().getName().equalsIgnoreCase(pname)) {
                final Runnable task = Reflect.on(pendingTask).get("task");
                if (task instanceof TaskInjector) {
                    final TaskInjector executor = (TaskInjector) task;
                    final StringBuffer str = new StringBuffer();
                    str.append("§6- §e" + getClassName(executor.getOriginalTask().getClass()) + " ");
                    str.append(String.format(total, executor.totalTime / um));
                    str.append(String.format(count, executor.count));
                    if (executor.count != 0) {
                        str.append(String.format(avg, executor.totalTime / um / executor.count));
                    }
                    e.getSender().sendMessage(str.toString());
                }
            }
        }
    }

    @Cmd(aliases = "ui", minimumArguments = 1)
    @Help(value = "撤销能耗监控器", possibleArguments = "[插件名称]")
    public void uninject(final CommandArgument e) {
        final String pname = e.getArgs()[0];
        final CommandSender sender = e.getSender();
        final Plugin plugin = Bukkit.getPluginManager().getPlugin(pname);
        if (plugin == null) {
            sender.sendMessage(String.format(p_n_f, pname));
            return;
        }
        if (plugin.isEnabled()) {
            YumAPI.uninject(plugin);
            sender.sendMessage(String.format(uninjected, pname));
        }
    }

    private String getClassName(final Class<?> clazz) {
        return StrKit.isBlank(clazz.getSimpleName()) ? clazz.getName().substring(clazz.getName().lastIndexOf(".") + 1) : clazz.getSimpleName();
    }
}
