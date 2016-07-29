package pw.yumc.Yum.commands;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import pw.yumc.Yum.Yum;
import pw.yumc.Yum.managers.ConfigManager;
import pw.yumc.YumCore.commands.CommandArgument;
import pw.yumc.YumCore.commands.CommandExecutor;
import pw.yumc.YumCore.commands.CommandManager;
import pw.yumc.YumCore.commands.annotation.Async;
import pw.yumc.YumCore.commands.annotation.Cmd;
import pw.yumc.YumCore.commands.annotation.Help;

public class NetCommand implements CommandExecutor {
    public static HashMap<String, Integer> netlist = new HashMap<>();

    private final String prefix = "§6[§bYum §a网络管理§6] ";

    private final String showlist = prefix + "§a自服务器启动以来尝试联网的插件列表如下:";
    private final String listprefix = "  §6插件名称    §d联网次数";
    private final String list = "§6- §b%s    §d%s";
    private final String no_net = prefix + "§a尚未检测到尝试联网的插件!";
    private final String add = prefix + "§a已添加插件 §b%s §a到网络 %s §a列表!";

    private final String p_n_f = prefix + "§c插件 §b%s §c不存在!";

    public NetCommand(final Yum yum) {
        new CommandManager("net", this, PluginTabComplete.instence);
    }

    public static void addNetCount(final String pname) {
        if (netlist.containsKey(pname)) {
            netlist.put(pname, netlist.get(pname) + 1);
        } else {
            netlist.put(pname, 1);
        }
    }

    @Cmd(aliases = "l")
    @Help("列出联网的插件详情")
    @Async
    public void list(final CommandArgument e) {
        final CommandSender sender = e.getSender();
        if (netlist.isEmpty()) {
            sender.sendMessage(no_net);
            return;
        }
        sender.sendMessage(showlist);
        sender.sendMessage(listprefix);
        for (final Entry<String, Integer> entry : netlist.entrySet()) {
            sender.sendMessage(String.format(list, entry.getKey(), entry.getValue()));
        }
    }

    @Cmd(minimumArguments = 1)
    @Help(value = "禁止插件联网", possibleArguments = "[插件名称]")
    public void off(final CommandArgument e) {
        final String pname = e.getArgs()[0];
        final CommandSender sender = e.getSender();
        if (Bukkit.getPluginManager().getPlugin(pname) == null) {
            sender.sendMessage(String.format(p_n_f, pname));
            return;
        }
        ConfigManager.i().network.addToStringList(ConfigManager.BLACK, pname, false).save();
        sender.sendMessage(String.format(add, pname, "§c黑名单"));
    }

    @Cmd(minimumArguments = 1)
    @Help(value = "允许插件联网", possibleArguments = "[插件名称]")
    public void on(final CommandArgument e) {
        final String pname = e.getArgs()[0];
        final CommandSender sender = e.getSender();
        if (Bukkit.getPluginManager().getPlugin(pname) == null) {
            sender.sendMessage(String.format(p_n_f, pname));
            return;
        }
        ConfigManager.i().network.addToStringList(ConfigManager.IGNORE, pname, false).save();
        sender.sendMessage(String.format(add, pname, "§e白名单"));
    }
}
