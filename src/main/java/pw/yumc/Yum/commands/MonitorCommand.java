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

    private final String no_mi = prefix + "§6%s §a自服务器启动以来尚未执行任何操作";
    private final String micprefix = "  §6命令名称             §a总耗时    §b执行次数  §d平均耗时";
    private final String mieprefix = "  §6事件名称             §a总耗时    §b执行次数  §d平均耗时";
    private final String mitprefix = "  §6任务名称             §a总耗时    §b执行次数  §d平均耗时";
    private final String milist = "§6- §e%-20s §a%-9.2f §b%-9s §d%-9.5f";
    private final String miwlist = "§6- §c%-20s §a%-9.2f §b%-9s §c%-9.5f";

    private final String reinject = prefix + "§a能耗监控器重载完毕!";
    private final String injected = prefix + "§a插件 §b%s §a成功注入能耗监控器!";
    private final String uninjected = prefix + "§a插件 §b%s §a成功撤销能耗监控器!";
    private final String notEnable = prefix + "§c插件 §b%s §c未成功加载 无法执行注入!";

    private final String lag = prefix + "§a当前服务器插件能耗如下§6(单位: %)";
    private final String lagprefix = "   §6插件名称             §c主线程                  §a命令  §b事件  §d任务";
    private final String laglist = "§6%-2s §b%-20s §c%-25s §a%-5.2f §b%-5.2f §d%-5.2f";

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
        if (temp.isEmpty()) {
            sender.sendMessage(String.format(no_mi, pname));
            return;
        }
        sender.sendMessage(micprefix);
        for (final Entry<String, Command> command : temp.entrySet()) {
            final org.bukkit.command.CommandExecutor executor = Reflect.on(command.getValue()).get("executor");
            if (executor instanceof CommandInjector) {
                final CommandInjector injected = (CommandInjector) executor;
                if (injected.count != 0) {
                    final double avgTime = injected.totalTime / um / injected.count;
                    sender.sendMessage(String.format(avgTime < 10 ? milist : miwlist, command.getValue().getName(), injected.totalTime / um, injected.count, avgTime));
                } else {
                    sender.sendMessage(String.format(milist, command.getValue().getName(), injected.totalTime / um, injected.count, 0D));
                }
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
        if (eventTotalTime.isEmpty()) {
            sender.sendMessage(String.format(no_mi, pname));
            return;
        }
        sender.sendMessage(mieprefix);
        for (final String event : MonitorManager.sortMapByValue(eventTotalTime).keySet()) {
            final double avgTime = eventTotalTime.get(event) / um / eventCount.get(event);
            sender.sendMessage(String.format(avgTime < 10 ? milist : miwlist, event, eventTotalTime.get(event) / um, eventCount.get(event), avgTime));
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
        int max = 8;
        try {
            max = Integer.parseInt(e.getArgs()[0]);
        } catch (final Exception ignore) {
        }
        sender.sendMessage(lag);
        sender.sendMessage(lagprefix);
        for (final Entry<String, Long> entry : mm.entrySet()) {
            if (++i > max) {
                break;
            }
            final MonitorInfo mi = MonitorManager.getMonitorInfo(entry.getKey());
            sender.sendMessage(String.format(laglist, i, entry.getKey(), getPer(mi.monitor), mi.cmd, mi.event, mi.task));
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
        if (pendingTasks.isEmpty()) {
            sender.sendMessage(String.format(no_mi, pname));
            return;
        }
        sender.sendMessage(prefix + "§6插件 §b" + pname + " §6的任务能耗如下!");
        sender.sendMessage(mitprefix);
        for (final BukkitTask pendingTask : pendingTasks) {
            if (pendingTask.getOwner().getName().equalsIgnoreCase(pname)) {
                final Runnable task = Reflect.on(pendingTask).get("task");
                if (task instanceof TaskInjector) {
                    final TaskInjector executor = (TaskInjector) task;
                    if (executor.count != 0) {
                        final double avgTime = executor.totalTime / um / executor.count;
                        sender.sendMessage(String.format(avgTime < 10 ? milist : miwlist, getClassName(executor.getOriginalTask().getClass()), executor.totalTime / um, executor.count, avgTime));
                    } else {
                        sender.sendMessage(String.format(milist, getClassName(executor.getOriginalTask().getClass()), executor.totalTime / um, executor.count, 0D));
                    }
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

    private String getPer(final double per) {
        final double p = per / 5;
        final StringBuilder sb = new StringBuilder();
        if (p < 4) {
            sb.append("§a");
        } else if (p < 7) {
            sb.append("§d");
        } else if (p < 10) {
            sb.append("§c");
        } else {
            sb.append("§4");
        }
        for (int i = 0; i < 11; i++) {
            if (p > i) {
                sb.append("|");
            }
        }
        if (per > 0) {
            sb.append(String.format("% 3.2f", per));
        }
        return sb.toString();
    }
}
