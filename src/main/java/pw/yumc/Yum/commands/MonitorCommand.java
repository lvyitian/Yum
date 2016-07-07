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
import org.bukkit.scheduler.BukkitTask;

import cn.citycraft.PluginHelper.commands.HandlerCommand;
import cn.citycraft.PluginHelper.commands.HandlerCommands;
import cn.citycraft.PluginHelper.commands.InvokeCommandEvent;
import cn.citycraft.PluginHelper.commands.InvokeSubCommand;
import cn.citycraft.PluginHelper.ext.kit.Reflect;
import pw.yumc.Yum.Yum;
import pw.yumc.Yum.inject.CommandInjector;
import pw.yumc.Yum.inject.ListenerInjector;
import pw.yumc.Yum.inject.TaskInjector;

/**
 *
 * @since 2016年7月6日 下午5:13:32
 * @author 喵♂呜
 */
public class MonitorCommand implements HandlerCommands {
    Yum main;

    public MonitorCommand(final Yum yum) {
        main = yum;
        final InvokeSubCommand cmdhandler = new InvokeSubCommand(yum, "monitor");
        cmdhandler.setAllCommandOnlyConsole(yum.getConfig().getBoolean("onlyFileCommandConsole", true));
        cmdhandler.registerCommands(this);
        cmdhandler.registerCommands(PluginTabComplete.instence);
    }

    @HandlerCommand(name = "a")
    public void a(final InvokeCommandEvent e) {

    }

    @HandlerCommand(name = "cmd", minimumArguments = 1, possibleArguments = "插件名称")
    public void cmd(final InvokeCommandEvent e) {
        final String pname = e.getArgs()[0];
        final CommandSender sender = e.getSender();
        if (Bukkit.getPluginManager().getPlugin(pname) == null) {
            sender.sendMessage("§c插件不存在!");
            return;
        }
        final PluginManager pluginManager = Bukkit.getPluginManager();
        final SimpleCommandMap commandMap = Reflect.on(pluginManager).get("commandMap");
        sender.sendMessage("§6插件 §b" + pname + " §6的命令能耗如下!");
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
                str.append(String.format("§6总耗时: §a%.2f秒 ", injected.totalTime / 1000000.0));
                str.append("§6执行次数: §b" + injected.count + "次 ");
                if (injected.count != 0) {
                    str.append(String.format("§6平均耗时: §d%.2f秒!", injected.totalTime / 1000000.0 / injected.count));
                }
                e.getSender().sendMessage(str.toString());
            }
        }
    }

    @HandlerCommand(name = "event", minimumArguments = 1, possibleArguments = "插件名称")
    public void event(final InvokeCommandEvent e) {
        final String pname = e.getArgs()[0];
        final CommandSender sender = e.getSender();
        final Plugin plugin = Bukkit.getPluginManager().getPlugin(pname);
        if (plugin == null) {
            sender.sendMessage("§c插件不存在!");
            return;
        }
        sender.sendMessage("§6插件 §b" + pname + " §6的事件能耗如下!");
        final List<RegisteredListener> listeners = HandlerList.getRegisteredListeners(plugin);
        for (final RegisteredListener listener : listeners) {
            final EventExecutor executor = Reflect.on(listener).get("executor");
            if (executor instanceof ListenerInjector) {
                final ListenerInjector injected = (ListenerInjector) executor;
                final StringBuffer str = new StringBuffer();
                str.append("§6- §e" + injected.getOriginalExecutor().getClass().getSimpleName() + " ");
                str.append(String.format("§6总耗时: §a%.2f秒 ", injected.totalTime / 1000000.0));
                str.append("§6执行次数: §b" + injected.count + "次 ");
                if (injected.count != 0) {
                    str.append(String.format("§6平均耗时: §d%.2f秒!", injected.totalTime / 1000000.0 / injected.count));
                }
                e.getSender().sendMessage(str.toString());
            }
        }
    }

    @HandlerCommand(name = "task", minimumArguments = 1, possibleArguments = "插件名称")
    public void task(final InvokeCommandEvent e) {
        final String pname = e.getArgs()[0];
        final CommandSender sender = e.getSender();
        final Plugin plugin = Bukkit.getPluginManager().getPlugin(pname);
        if (plugin == null) {
            sender.sendMessage("§c插件不存在!");
            return;
        }
        final List<BukkitTask> pendingTasks = Bukkit.getScheduler().getPendingTasks();
        sender.sendMessage("§6插件 §b" + pname + " §6的任务能耗如下!");
        for (final BukkitTask pendingTask : pendingTasks) {
            final Runnable task = Reflect.on(pendingTask).get("task");
            if (task instanceof TaskInjector) {
                final TaskInjector executor = (TaskInjector) task;
                final StringBuffer str = new StringBuffer();
                str.append("§6- §e" + executor.getOriginalTask().getClass().getSimpleName() + " ");
                str.append(String.format("§6总耗时: §a%.2f秒 ", executor.totalTime / 1000000.0));
                str.append("§6执行次数: §b" + executor.count + "次 ");
                if (executor.count != 0) {
                    str.append(String.format("§6平均耗时: §d%.2f秒!", executor.totalTime / 1000000.0 / executor.count));
                }
                e.getSender().sendMessage(str.toString());
            }
        }
    }
}
