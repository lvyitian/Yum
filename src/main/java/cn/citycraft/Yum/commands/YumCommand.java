package cn.citycraft.Yum.commands;

import cn.citycraft.PluginHelper.commands.HandlerSubCommand;
import cn.citycraft.Yum.Yum;

/**
 * Yum命令基类
 * 
 * @since 2016年1月9日 上午10:02:24
 * @author 喵♂呜
 */
public class YumCommand {
    public YumCommand(final Yum yum) {
        final HandlerSubCommand cmdhandler = new HandlerSubCommand(yum, "yum");
        cmdhandler.setAllCommandOnlyConsole(yum.config.getBoolean("onlyCommandConsole", false));
        cmdhandler.registerCommand(new CommandList(yum));
        cmdhandler.registerCommand(new CommandInstall(yum));
        cmdhandler.registerCommand(new CommandUpdate(yum));
        cmdhandler.registerCommand(new CommandUpdateAll(yum));
        cmdhandler.registerCommand(new CommandDelete(yum));
        cmdhandler.registerCommand(new CommandFullDelete(yum));
        cmdhandler.registerCommand(new CommandInfo(yum));
        cmdhandler.registerCommand(new CommandRepo(yum));
        cmdhandler.registerCommand(new CommandReload(yum));
        cmdhandler.registerCommand(new CommandLoad(yum));
        cmdhandler.registerCommand(new CommandUnload(yum));
        cmdhandler.registerCommand(new CommandUpgrade(yum));
    }
}
