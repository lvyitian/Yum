package pw.yumc.Yum.inject;

import java.util.List;

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

public class CommandInjector implements TabExecutor {

    private final CommandExecutor originalExecutor;
    private final TabCompleter originalCompleter;

    public long totalTime;
    public long count;

    public CommandInjector(final CommandExecutor originalCommandExecutor, final TabCompleter originalTabCompleter) {
        this.originalExecutor = originalCommandExecutor;
        this.originalCompleter = originalTabCompleter;
    }

    public static void inject(final Plugin toInjectPlugin) {
        final PluginManager pluginManager = Bukkit.getPluginManager();
        final SimpleCommandMap commandMap = Reflect.on(pluginManager).get("commandMap");
        for (final Command command : commandMap.getCommands()) {
            if (command instanceof PluginCommand) {
                final PluginCommand pluginCommand = (PluginCommand) command;
                final Plugin plugin = pluginCommand.getPlugin();
                if (plugin.equals(toInjectPlugin)) {
                    final CommandExecutor executor = Reflect.on(command).get("executor");
                    if (executor instanceof CommandInjector) {
                        return;
                    }
                    final TabCompleter completer = Reflect.on(command).get("completer");
                    final CommandInjector commandInjector = new CommandInjector(executor, completer);
                    Reflect.on(command).set("executor", commandInjector);
                    Reflect.on(command).set("completer", commandInjector);
                }
            }
        }
        PluginKit.scp("§a注入命令性能监控到 " + toInjectPlugin.getName());
    }

    public static void uninject(final Plugin toUninject) {
        final PluginManager pluginManager = Bukkit.getPluginManager();
        final SimpleCommandMap commandMap = Reflect.on(pluginManager).get("commandMap");
        for (final Command command : commandMap.getCommands()) {
            if (command instanceof PluginCommand) {
                final PluginCommand pluginCommand = (PluginCommand) command;
                final Plugin plugin = pluginCommand.getPlugin();
                if (plugin.equals(toUninject)) {
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
    }

    public TabCompleter getOriginalCompleter() {
        return originalCompleter;
    }

    public CommandExecutor getOriginalExecutor() {
        return originalExecutor;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final long start = System.nanoTime();
        // todo add a more aggressive 10 ms cpu sample
        final boolean result = originalExecutor.onCommand(sender, command, label, args);
        final long end = System.nanoTime();

        totalTime += end - start;
        count++;
        return result;
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        final long start = System.nanoTime();
        // todo add a more aggressive 10 ms cpu sample
        final List<String> result = originalCompleter.onTabComplete(sender, command, alias, args);
        final long end = System.nanoTime();

        totalTime += end - start;
        count++;
        return result;
    }
}
