package pw.yumc.Yum.commands;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import pw.yumc.Yum.Yum;
import pw.yumc.Yum.managers.ConfigManager;
import pw.yumc.YumCore.commands.CommandSub;
import pw.yumc.YumCore.commands.annotation.Async;
import pw.yumc.YumCore.commands.annotation.Cmd;
import pw.yumc.YumCore.commands.annotation.Help;
import pw.yumc.YumCore.commands.interfaces.Executor;

public class NetCommand implements Executor {
    public static HashMap<String, Integer> netlist = new HashMap<>();

    private String prefix = "§6[§bYum §a网络管理§6] ";

    private String showlist = prefix + "§a自服务器启动以来尝试联网的插件列表如下:";
    private String listprefix = "  §6插件名称             §d联网次数";
    private String list = "§6- §b%-20s §d%s";
    private String no_net = prefix + "§a尚未检测到尝试联网的插件!";
    private String add = prefix + "§a已添加插件 §b%s §a到网络 %s §a列表!";

    private String p_n_f = prefix + "§c插件 §b%s §c不存在!";

    public NetCommand(Yum yum) {
        new CommandSub("net", this, PluginTabComplete.instence);
    }

    public static void addNetCount(String pname) {
        if (netlist.containsKey(pname)) {
            netlist.put(pname, netlist.get(pname) + 1);
        } else {
            netlist.put(pname, 1);
        }
    }

    @Cmd(aliases = "l")
    @Help("列出联网的插件详情")
    @Async
    public void list(CommandSender sender) {
        if (netlist.isEmpty()) {
            sender.sendMessage(no_net);
            return;
        }
        sender.sendMessage(showlist);
        sender.sendMessage(listprefix);
        for (Entry<String, Integer> entry : netlist.entrySet()) {
            sender.sendMessage(String.format(list, entry.getKey(), entry.getValue()));
        }
    }

    @Cmd(minimumArguments = 1)
    @Help(value = "禁止插件联网", possibleArguments = "[插件名称]")
    public void off(CommandSender sender, String pname) {
        if (Bukkit.getPluginManager().getPlugin(pname) == null) {
            sender.sendMessage(String.format(p_n_f, pname));
            return;
        }
        ConfigManager.i().network.addToStringList(ConfigManager.BLACK, pname, false).save();
        sender.sendMessage(String.format(add, pname, "§c黑名单"));
    }

    @Cmd(minimumArguments = 1)
    @Help(value = "允许插件联网", possibleArguments = "[插件名称]")
    public void on(CommandSender sender, String pname) {
        if (Bukkit.getPluginManager().getPlugin(pname) == null) {
            sender.sendMessage(String.format(p_n_f, pname));
            return;
        }
        ConfigManager.i().network.addToStringList(ConfigManager.IGNORE, pname, false).save();
        sender.sendMessage(String.format(add, pname, "§e白名单"));
    }
}
