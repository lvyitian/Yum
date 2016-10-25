package pw.yumc.Yum.inject;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import pw.yumc.Yum.commands.MonitorCommand;
import pw.yumc.Yum.managers.MonitorManager;
import pw.yumc.YumCore.kit.StrKit;
import pw.yumc.YumCore.reflect.Reflect;

public class CommandInjector implements TabExecutor {
    private static String prefix = "§6[§bYum §a命令监控§6] ";
    private static String warn = "§c注意! §6玩家 §a%s §6执行 §b%s §6插件 §d%s %s §6命令 §c耗时 §4%sms §c平均耗时 §4%sms!";
    private static String err = prefix + "§6玩家 §a%s §6执行 §b%s §6插件 §d%s %s §6命令时发生异常!";
    private static String inject_error = prefix + "§6插件 §b%s §c注入能耗监控失败!";
    private static String plugin_is_null = "插件不得为NULL!";
    private CommandExecutor originalExecutor;
    private TabCompleter originalCompleter;

    private Plugin plugin;

    public long totalTime;
    public int count;

    public CommandInjector(CommandExecutor originalCommandExecutor, TabCompleter originalTabCompleter, Plugin plugin) {
        this.originalExecutor = originalCommandExecutor;
        this.originalCompleter = originalTabCompleter;
        this.plugin = plugin;
    }

    public static void inject(Plugin plugin) {
        Validate.notNull(plugin, plugin_is_null);
        try {
            PluginManager pluginManager = Bukkit.getPluginManager();
            SimpleCommandMap commandMap = Reflect.on(pluginManager).get("commandMap");
            for (Command command : commandMap.getCommands()) {
                if (command instanceof PluginCommand) {
                    PluginCommand pluginCommand = (PluginCommand) command;
                    Plugin cp = pluginCommand.getPlugin();
                    if (cp.equals(plugin)) {
                        CommandExecutor executor = Reflect.on(command).get("executor");
                        if (executor instanceof CommandInjector) { return; }
                        TabCompleter completer = Reflect.on(command).get("completer");
                        CommandInjector commandInjector = new CommandInjector(executor, completer, plugin);
                        Reflect.on(command).set("executor", commandInjector);
                        Reflect.on(command).set("completer", commandInjector);
                    }
                }
            }
        } catch (Throwable e) {
            MonitorManager.log(String.format(inject_error, plugin.getName()));
        }
    }

    public static void uninject(Plugin plugin) {
        Validate.notNull(plugin, plugin_is_null);
        try {
            PluginManager pluginManager = Bukkit.getPluginManager();
            SimpleCommandMap commandMap = Reflect.on(pluginManager).get("commandMap");
            for (Command command : commandMap.getCommands()) {
                if (command instanceof PluginCommand) {
                    PluginCommand pluginCommand = (PluginCommand) command;
                    Plugin cp = pluginCommand.getPlugin();
                    if (cp.equals(plugin)) {
                        CommandExecutor executor = Reflect.on(command).get("executor");
                        if (executor instanceof CommandInjector) {
                            CommandInjector injected = (CommandInjector) executor;
                            Reflect.on(command).set("executor", injected.getOriginalExecutor());
                        }
                        TabCompleter completer = Reflect.on(command).get("completer");
                        if (completer instanceof CommandInjector) {
                            CommandInjector injected = (CommandInjector) completer;
                            Reflect.on(command).set("completer", injected.getOriginalCompleter());
                        }
                    }
                }
            }
        } catch (Throwable e) {
        }
    }

    public TabCompleter getOriginalCompleter() {
        return originalCompleter;
    }

    public CommandExecutor getOriginalExecutor() {
        return originalExecutor;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            long start = System.nanoTime();
            boolean result = originalExecutor.onCommand(sender, command, label, args);
            long end = System.nanoTime();
            long lag = end - start;
            totalTime += lag;
            count++;
            long lagms = lag / MonitorManager.um;
            long avglagms = totalTime / count / MonitorManager.um;
            if (Bukkit.isPrimaryThread() && lagms > MonitorManager.lagTime && avglagms > MonitorManager.lagTime) {
                MonitorManager.lagTip(String.format(warn,
                        sender.getName(),
                        plugin.getName(),
                        label,
                        StrKit.join(args, " "),
                        lagms,
                        avglagms));
            }
            MonitorManager.addCmd(plugin.getName(), lag);
            return result;
        } catch (Throwable e) {
            while (e.getCause() != null) {
                e = e.getCause();
            }
            MonitorCommand.lastError = e;
            MonitorManager.sendError(sender, plugin, e);
            MonitorManager.printThrowable(
                    String.format(err, sender.getName(), plugin.getName(), label, StrKit.join(args, " ")), e);
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (originalCompleter == null) { return Collections.emptyList(); }
        long start = System.nanoTime();
        List<String> result = originalCompleter.onTabComplete(sender, command, alias, args);
        long end = System.nanoTime();
        totalTime += end - start;
        count++;
        return result;
    }
}
