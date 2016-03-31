package pw.yumc.Yum.commands;

import cn.citycraft.PluginHelper.commands.HandlerCommand;
import cn.citycraft.PluginHelper.commands.HandlerCommands;
import cn.citycraft.PluginHelper.commands.InvokeCommandEvent;
import cn.citycraft.PluginHelper.commands.InvokeSubCommand;
import pw.yumc.Yum.Yum;

public class NetCommand implements HandlerCommands {

    public NetCommand(final Yum yum) {
        final InvokeSubCommand cmdhandler = new InvokeSubCommand(yum, "net");
        cmdhandler.setAllCommandOnlyConsole(yum.getConfig().getBoolean("onlyNetCommandConsole", false));
        cmdhandler.registerCommands(this);
    }

    @HandlerCommand(name = "list", aliases = "l", permission = "", description = "列出联网的插件")
    public void list(final InvokeCommandEvent e) {

    }

    @HandlerCommand(name = "off", description = "禁止插件联网", possibleArguments = "[插件名称]")
    public void off(final InvokeCommandEvent e) {

    }

    @HandlerCommand(name = "on", description = "允许插件联网", possibleArguments = "[插件名称]")
    public void on(final InvokeCommandEvent e) {

    }
}
