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

import cn.citycraft.PluginHelper.ext.kit.Reflect;
import cn.citycraft.PluginHelper.kit.PluginKit;
import cn.citycraft.PluginHelper.kit.StrKit;
import pw.yumc.Yum.commands.MonitorCommand;
import pw.yumc.Yum.managers.MonitorManager;

public class CommandInjector implements TabExecutor {
    private final static String prefix = "§6[§bYum §a命令监控§6] ";
    private final static String inject_error = prefix + "§6插件 §b%s §c注入能耗监控失败!";
    private final static String plugin_is_null = "插件不得为NULL!";
    private final CommandExecutor originalExecutor;
    private final TabCompleter originalCompleter;

    private final Plugin plugin;

    public long totalTime;
    public int count;

    public CommandInjector(final CommandExecutor originalCommandExecutor, final TabCompleter originalTabCompleter, final Plugin plugin) {
        this.originalExecutor = originalCommandExecutor;
        this.originalCompleter = originalTabCompleter;
        this.plugin = plugin;
    }

    public static void inject(final Plugin plugin) {
        Validate.notNull(plugin, plugin_is_null);
        try {
            final PluginManager pluginManager = Bukkit.getPluginManager();
            final SimpleCommandMap commandMap = Reflect.on(pluginManager).get("commandMap");
            for (final Command command : commandMap.getCommands()) {
                if (command instanceof PluginCommand) {
                    final PluginCommand pluginCommand = (PluginCommand) command;
                    final Plugin cp = pluginCommand.getPlugin();
                    if (cp.equals(plugin)) {
                        final CommandExecutor executor = Reflect.on(command).get("executor");
                        if (executor instanceof CommandInjector) {
                            return;
                        }
                        final TabCompleter completer = Reflect.on(command).get("completer");
                        final CommandInjector commandInjector = new CommandInjector(executor, completer, plugin);
                        Reflect.on(command).set("executor", commandInjector);
                        Reflect.on(command).set("completer", commandInjector);
                    }
                }
            }
        } catch (final Throwable e) {
            PluginKit.sc(String.format(inject_error, plugin.getName()));
        }
    }

    public static void uninject(final Plugin plugin) {
        Validate.notNull(plugin, plugin_is_null);
        try {
            final PluginManager pluginManager = Bukkit.getPluginManager();
            final SimpleCommandMap commandMap = Reflect.on(pluginManager).get("commandMap");
            for (final Command command : commandMap.getCommands()) {
                if (command instanceof PluginCommand) {
                    final PluginCommand pluginCommand = (PluginCommand) command;
                    final Plugin cp = pluginCommand.getPlugin();
                    if (cp.equals(plugin)) {
                        final CommandExecutor executor = Reflect.on(command).get("executor");
                        if (executor instanceof CommandInjector) {
                            final CommandInjector injected = (CommandInjector) executor;
                            Reflect.on(command).set("executor", injected.getOriginalExecutor());
                        }
                        final TabCompleter completer = Reflect.on(command).get("completer");
                        if (completer instanceof CommandInjector) {
                            final CommandInjector injected = (CommandInjector) completer;
                            Reflect.on(command).set("completer", injected.getOriginalCompleter());
                        }
                    }
                }
            }
        } catch (final Throwable e) {
        }
    }

    public TabCompleter getOriginalCompleter() {
        return originalCompleter;
    }

    public CommandExecutor getOriginalExecutor() {
        return originalExecutor;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        try {
            final long start = System.nanoTime();
            // TODO 当操作大于10ms的时候添加一个Lag提示
            final boolean result = originalExecutor.onCommand(sender, command, label, args);
            final long end = System.nanoTime();
            final long lag = end - start;
            if (Bukkit.isPrimaryThread() && lag / 1000000 > 10) {
                PluginKit.sc("§6[§bYum §a能耗监控§6] §c注意! §6玩家 §a" + sender.getName() + " §6执行 §b" + plugin.getName() + " §6插件 §d" + label + " " + StrKit.join(args, " ") + " §6命令 §c耗时 §4" + lag / 1000000 + "ms!");
            }
            totalTime += lag;
            count++;
            MonitorManager.addCmd(plugin.getName(), lag);
            return result;
        } catch (Throwable e) {
            while (e.getCause() != null) {
                e = e.getCause();
            }
            sender.sendMessage(prefix + "§c命令执行异常 请反馈下列信息给腐竹!");
            sender.sendMessage("§6插件名称: §b" + plugin.getName());
            sender.sendMessage("§6异常名称: §c" + e.getClass().getName());
            sender.sendMessage("§6异常说明: §3" + e.getMessage());
            MonitorCommand.lastError = e;
            PluginKit.sc(prefix + "§6玩家 §a" + sender.getName() + " §6执行 §b" + plugin.getName() + " §6插件 §d" + label + " " + StrKit.join(args, " ") + " §6命令时发生异常!");
            PluginKit.sc("§6异常名称: §c" + e.getClass().getName());
            PluginKit.sc("§6异常说明: §3" + e.getMessage());
            PluginKit.sc("§6简易错误信息如下:");
            final int l = e.getStackTrace().length > 5 ? 5 : e.getStackTrace().length;
            for (int i = 0; i < l; i++) {
                final StackTraceElement ste = e.getStackTrace()[i];
                PluginKit.sc("    §e位于 §c" + ste.getClassName() + "." + ste.getMethodName() + "(§4" + ste.getFileName() + ":" + ste.getLineNumber() + "§c)");
            }
            if (MonitorCommand.debug) {
                PluginKit.sc("§c开发人员调试信息如下:");
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (originalCompleter == null) {
            return Collections.emptyList();
        }
        final long start = System.nanoTime();
        // TODO add a more aggressive 10 ms cpu sample
        final List<String> result = originalCompleter.onTabComplete(sender, command, alias, args);
        final long end = System.nanoTime();

        totalTime += end - start;
        count++;
        return result;
    }
}
