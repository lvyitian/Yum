package pw.yumc.Yum.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
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

import cn.citycraft.PluginHelper.commands.HandlerCommand;
import cn.citycraft.PluginHelper.commands.HandlerCommands;
import cn.citycraft.PluginHelper.commands.InvokeCommandEvent;
import cn.citycraft.PluginHelper.commands.InvokeSubCommand;
import cn.citycraft.PluginHelper.ext.kit.Reflect;
import cn.citycraft.PluginHelper.kit.StrKit;
import pw.yumc.Yum.Yum;
import pw.yumc.Yum.api.YumAPI;
import pw.yumc.Yum.inject.CommandInjector;
import pw.yumc.Yum.inject.ListenerInjector;
import pw.yumc.Yum.inject.TaskInjector;

/**
 *
 * @since 2016年7月6日 下午5:13:32
 * @author 喵♂呜
 */
public class MonitorCommand implements HandlerCommands {
    private final String prefix = "§6[§bYum §a能耗监控§6] ";
    private final String total = "§6总耗时: §a%.2f毫秒 ";
    private final String count = "§6执行次数: §b%s次 ";
    private final String avg = "§6平均耗时: §d%.5f毫秒!";
    private final String avg_warn = "§6平均耗时: §c%.5f毫秒!";
    private final String p_n_f = prefix + "§c插件 §b%s §c不存在!";

    private final String injected = "§a插件 §b%s §a成功注入能耗监控器!";
    private final String uninjected = "§a插件 §b%s §a成功撤销能耗监控器!";
    private final String notEnable = "§c插件 §b%s §c未成功加载 无法执行注入!";

    private final double um = 1000000.0;

    public MonitorCommand(final Yum yum) {
        final InvokeSubCommand cmdhandler = new InvokeSubCommand(yum, "monitor");
        cmdhandler.registerCommands(this);
        cmdhandler.registerCommands(PluginTabComplete.instence);
    }

    @HandlerCommand(name = "cmd", description = "查看插件命令能耗", minimumArguments = 1, possibleArguments = "[插件名称]")
    public void cmd(final InvokeCommandEvent e) {
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
            final CommandExecutor executor = Reflect.on(command.getValue()).get("executor");
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

    @HandlerCommand(name = "event", description = "查看插件事件能耗", minimumArguments = 1, possibleArguments = "[插件名称]")
    public void event(final InvokeCommandEvent e) throws InstantiationException, IllegalAccessException {
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
            final EventExecutor executor = Reflect.on(listener).get("executor");
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
            str.append("§6- §e" + event + " ");
            str.append(String.format(total, eventTotalTime.get(event) / um));
            str.append(String.format(count, eventCount.get(event)));
            if (eventCount.get(event) != 0) {
                final double avgTime = eventTotalTime.get(event) / um / eventCount.get(event);
                str.append(String.format(avgTime < 10 ? avg : avg_warn, avgTime));
            }
            e.getSender().sendMessage(str.toString());
        }
    }

    @HandlerCommand(name = "inject", aliases = "i", description = "注入能耗监控器", minimumArguments = 1, possibleArguments = "[插件名称]")
    public void inject(final InvokeCommandEvent e) {
        final String pname = e.getArgs()[0];
        final CommandSender sender = e.getSender();
        final Plugin plugin = Bukkit.getPluginManager().getPlugin(pname);
        if (plugin == null) {
            sender.sendMessage(String.format(p_n_f, pname));
            return;
        }
        if (plugin.isEnabled()) {
            YumAPI.inject(plugin);
            sender.sendMessage(String.format(prefix + injected, pname));
        } else {
            sender.sendMessage(String.format(prefix + notEnable, pname));
        }
    }

    @HandlerCommand(name = "task", description = "查看插件任务能耗", minimumArguments = 1, possibleArguments = "[插件名称]")
    public void task(final InvokeCommandEvent e) {
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
                    final Class<? extends Runnable> taskName = executor.getOriginalTask().getClass();
                    str.append("§6- §e" + (StrKit.isBlank(taskName.getSimpleName()) ? taskName.getName() : taskName.getSimpleName()) + " ");
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

    @HandlerCommand(name = "uninject", aliases = "ui", description = "撤销能耗监控器", minimumArguments = 1, possibleArguments = "[插件名称]")
    public void uninject(final InvokeCommandEvent e) {
        final String pname = e.getArgs()[0];
        final CommandSender sender = e.getSender();
        final Plugin plugin = Bukkit.getPluginManager().getPlugin(pname);
        if (plugin == null) {
            sender.sendMessage(String.format(p_n_f, pname));
            return;
        }
        if (plugin.isEnabled()) {
            YumAPI.uninject(plugin);
            sender.sendMessage(String.format(prefix + uninjected, pname));
        }
    }
}
