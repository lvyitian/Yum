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

import pw.yumc.Yum.Yum;
import pw.yumc.Yum.api.YumAPI;
import pw.yumc.Yum.inject.CommandInjector;
import pw.yumc.Yum.inject.ListenerInjector;
import pw.yumc.Yum.inject.TaskInjector;
import pw.yumc.Yum.managers.MonitorManager;
import pw.yumc.Yum.managers.MonitorManager.MonitorInfo;
import pw.yumc.YumCore.commands.CommandSub;
import pw.yumc.YumCore.commands.annotation.Async;
import pw.yumc.YumCore.commands.annotation.Cmd;
import pw.yumc.YumCore.commands.annotation.Help;
import pw.yumc.YumCore.commands.annotation.Option;
import pw.yumc.YumCore.commands.interfaces.Executor;
import pw.yumc.YumCore.kit.PKit;
import pw.yumc.YumCore.kit.StrKit;
import pw.yumc.YumCore.reflect.Reflect;

/**
 *
 * @since 2016年7月6日 下午5:13:32
 * @author 喵♂呜
 */
public class MonitorCommand implements Executor {
    public static Throwable lastError = null;

    private String prefix = "§6[§bYum §a能耗监控§6] ";

    private String no_mi = prefix + "§6%s §a自服务器启动以来尚未执行任何操作";
    private String micprefix = "  §6命令名称             §a总耗时    §b执行次数  §d平均耗时";
    private String mieprefix = "  §6事件名称             §a总耗时    §b执行次数  §d平均耗时";
    private String mitprefix = "  §6任务名称             §a总耗时    §b执行次数  §d平均耗时";
    private String milist = "§6- §e%-20s §a%-9.2f §b%-9s §d%-9.5f";
    private String miwlist = "§6- §c%-20s §a%-9.2f §b%-9s §c%-9.5f";

    private String reinject = prefix + "§a能耗监控器重载完毕!";
    private String injected = prefix + "§a插件 §b%s §a成功注入能耗监控器!";
    private String uninjected = prefix + "§a插件 §b%s §a成功撤销能耗监控器!";
    private String notEnable = prefix + "§c插件 §b%s §c未成功加载 无法执行注入!";

    private String lag = prefix + "§a当前服务器插件能耗如下§6(单位: %)";
    private String lagprefix = "   §6插件名称             §c主线程                  §a命令  §b事件  §d任务";
    private String laglist = "§6%-2s §b%-18s §c%-25s §a%-5.2f §b%-5.2f §d%-5.2f";

    private String no_error = prefix + "§a自服务器启动以来尚未发现报错!";
    private String last_error = prefix + "§c最后一次错误异常由 §b%s §c造成 详细如下:";

    private String p_n_f = prefix + "§c插件 §b%s §c不存在!";

    private double um = 1000000.00;

    public MonitorCommand(Yum yum) {
        new CommandSub("monitor", this, PluginTabComplete.instence);
    }

    @Cmd(aliases = "c", minimumArguments = 1)
    @Help(value = "查看插件命令能耗", possibleArguments = "[插件名称]")
    @Async
    public void cmd(CommandSender sender, String pname) {
        if (Bukkit.getPluginManager().getPlugin(pname) == null) {
            sender.sendMessage(String.format(p_n_f, pname));
            return;
        }
        PluginManager pluginManager = Bukkit.getPluginManager();
        SimpleCommandMap commandMap = Reflect.on(pluginManager).get("commandMap");
        sender.sendMessage(prefix + "§6插件 §b" + pname + " §6的命令能耗如下!");
        Map<String, Command> temp = new HashMap<>();
        for (Command command : commandMap.getCommands()) {
            if (command instanceof PluginCommand) {
                PluginCommand pluginCommand = (PluginCommand) command;
                Plugin plugin = pluginCommand.getPlugin();
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
        for (Entry<String, Command> command : temp.entrySet()) {
            org.bukkit.command.CommandExecutor executor = Reflect.on(command.getValue()).get("executor");
            if (executor instanceof CommandInjector) {
                CommandInjector injected = (CommandInjector) executor;
                if (injected.count != 0) {
                    double avgTime = injected.totalTime / um / injected.count;
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
    public void event(CommandSender sender, String pname) throws InstantiationException, IllegalAccessException, NoSuchFieldException {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pname);
        if (plugin == null) {
            sender.sendMessage(String.format(p_n_f, pname));
            return;
        }
        sender.sendMessage(prefix + "§6插件 §b" + pname + " §6的事件能耗如下!");
        List<RegisteredListener> listeners = HandlerList.getRegisteredListeners(plugin);
        Map<String, Long> eventTotalTime = new HashMap<>();
        Map<String, Integer> eventCount = new HashMap<>();
        for (RegisteredListener listener : listeners) {
            if (listener instanceof TimedRegisteredListener) {
                TimedRegisteredListener trl = (TimedRegisteredListener) listener;
                eventTotalTime.put(trl.getEventClass().getSimpleName(), trl.getTotalTime());
                eventCount.put(trl.getEventClass().getSimpleName(), trl.getCount());
                continue;
            }
            EventExecutor executor = Reflect.on(listener).get("executor");
            if (listener.getClass().getName().contains("PWPRegisteredListener")) {
                Field f = Reflect.getDeclaredField(RegisteredListener.class, "executor");
                f.setAccessible(true);
                executor = (EventExecutor) f.get(listener);
            }
            if (executor instanceof ListenerInjector) {
                ListenerInjector injected = (ListenerInjector) executor;
                for (String entry : injected.eventTotalTime.keySet()) {
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
        for (String event : MonitorManager.sortMapByValue(eventTotalTime).keySet()) {
            double avgTime = eventTotalTime.get(event) / um / eventCount.get(event);
            sender.sendMessage(String.format(avgTime < 10 ? milist : miwlist, event, eventTotalTime.get(event) / um, eventCount.get(event), avgTime));
        }
    }

    @Cmd(aliases = "i", minimumArguments = 1)
    @Help(value = "注入能耗监控器", possibleArguments = "[插件名称]")
    public void inject(CommandSender sender, String pname) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pname);
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
    public void lag(CommandSender sender, @Option("def:8") int size) {
        Map<String, Long> mm = MonitorManager.getMonitor();
        sender.sendMessage(lag);
        sender.sendMessage(lagprefix);
        int index = 0;
        for (Entry<String, Long> entry : mm.entrySet()) {
            if (++index > size) {
                break;
            }
            MonitorInfo mi = MonitorManager.getMonitorInfo(entry.getKey());
            if (mi.monitor != 0) {
                sender.sendMessage(String.format(laglist, index, StrKit.substring(entry.getKey(), 0, 18), getPer(sender, mi.monitor), mi.cmd, mi.event, mi.task));
            }
        }
    }

    @Cmd(aliases = "la")
    @Help("查看最后一次报错")
    @Async
    public void lasterror(CommandSender sender) {
        if (lastError == null) {
            sender.sendMessage(no_error);
            return;
        }
        Plugin plugin = PKit.getOperatePlugin(lastError.getStackTrace());
        sender.sendMessage(String.format(last_error, plugin != null ? plugin.getName() : "未知"));
        lastError.printStackTrace();
    }

    @Cmd(aliases = "ri")
    @Help("重载能耗监控器")
    public void reinject(CommandSender sender) {
        YumAPI.updateInject();
        sender.sendMessage(reinject);
    }

    @Cmd
    @Help("重置能耗监控器")
    @Async
    public void reset(CommandSender sender) {
        MonitorManager.init();
    }

    @Cmd(aliases = "t", minimumArguments = 1)
    @Help(value = "查看插件任务能耗", possibleArguments = "[插件名称]")
    @Async
    public void task(CommandSender sender, String pname) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pname);
        if (plugin == null) {
            sender.sendMessage(String.format(p_n_f, pname));
            return;
        }
        List<BukkitTask> pendingTasks = Bukkit.getScheduler().getPendingTasks();
        if (pendingTasks.isEmpty()) {
            sender.sendMessage(String.format(no_mi, pname));
            return;
        }
        sender.sendMessage(prefix + "§6插件 §b" + pname + " §6的任务能耗如下!");
        sender.sendMessage(mitprefix);
        for (BukkitTask pendingTask : pendingTasks) {
            if (pendingTask.getOwner().getName().equalsIgnoreCase(pname)) {
                Runnable task = Reflect.on(pendingTask).get("task");
                if (task instanceof TaskInjector) {
                    TaskInjector executor = (TaskInjector) task;
                    if (executor.count != 0) {
                        double avgTime = executor.totalTime / um / executor.count;
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
    public void uninject(CommandSender sender, String pname) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pname);
        if (plugin == null) {
            sender.sendMessage(String.format(p_n_f, pname));
            return;
        }
        if (plugin.isEnabled()) {
            YumAPI.uninject(plugin);
            sender.sendMessage(String.format(uninjected, pname));
        }
    }

    private String getClassName(Class<?> clazz) {
        return StrKit.isBlank(clazz.getSimpleName()) ? clazz.getName().substring(clazz.getName().lastIndexOf(".") + 1) : clazz.getSimpleName();
    }

    private String getPer(CommandSender sender, double per) {
        String ps = "≡";
        double p = per / 5;
        StringBuilder sb = new StringBuilder();
        if (p < 3) {
            sb.append("§a");
        } else if (p < 6) {
            sb.append("§d");
        } else if (p < 9) {
            sb.append("§c");
        } else {
            sb.append("§4");
        }
        for (int i = 0; i < 11; i++) {
            if (p > i) {
                sb.append(ps);
            }
        }
        if (per > 0) {
            sb.append(String.format("% 3.2f", per));
        }
        return sb.toString();
    }
}
