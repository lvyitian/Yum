package pw.yumc.Yum.commands;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import cn.citycraft.PluginHelper.commands.HandlerCommand;
import cn.citycraft.PluginHelper.commands.HandlerCommands;
import cn.citycraft.PluginHelper.commands.InvokeCommandEvent;
import cn.citycraft.PluginHelper.commands.InvokeSubCommand;
import pw.yumc.Yum.Yum;
import pw.yumc.Yum.managers.ConfigManager;

public class NetCommand implements HandlerCommands {
    public static HashMap<String, Integer> netlist = new HashMap<>();

    private final String prefix = "§6[§bYum §a网络管理§6] ";

    private final String showlist = prefix + "§a自服务器启动以来尝试联网的插件列表如下:";
    private final String listprefix = "  §6插件名称    §d联网次数";
    private final String list = "§6- §b&s    §d%s";
    private final String no_net = prefix + "§a尚未检测到尝试联网的插件!";
    private final String add = prefix + "§a已添加插件 §b%s §a到网络 %s §a列表!";

    private final String p_n_f = prefix + "§c插件 §b%s §c不存在!";

    public NetCommand(final Yum yum) {
        final InvokeSubCommand cmdhandler = new InvokeSubCommand(yum, "net");
        cmdhandler.registerCommands(this);
        cmdhandler.registerCommands(PluginTabComplete.instence);
    }

    public static void addNetCount(final String pname) {
        if (netlist.containsKey(pname)) {
            netlist.put(pname, netlist.get(pname) + 1);
        } else {
            netlist.put(pname, 1);
        }
    }

    @HandlerCommand(name = "list", aliases = "l", permission = "", description = "列出联网的插件")
    public void list(final InvokeCommandEvent e) {
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

    @HandlerCommand(name = "off", minimumArguments = 1, description = "禁止插件联网", possibleArguments = "[插件名称]")
    public void off(final InvokeCommandEvent e) {
        final String pname = e.getArgs()[0];
        final CommandSender sender = e.getSender();
        if (Bukkit.getPluginManager().getPlugin(pname) == null) {
            sender.sendMessage(String.format(p_n_f, pname));
            return;
        }
        ConfigManager.i().network.addToStringList(ConfigManager.BLACK, pname, false).save();
        sender.sendMessage(String.format(add, pname, "§c黑名单"));
    }

    @HandlerCommand(name = "on", minimumArguments = 1, description = "允许插件联网", possibleArguments = "[插件名称]")
    public void on(final InvokeCommandEvent e) {
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
