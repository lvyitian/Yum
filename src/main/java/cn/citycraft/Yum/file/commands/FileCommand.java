package cn.citycraft.Yum.file.commands;

import cn.citycraft.PluginHelper.commands.HandlerSubCommand;
import cn.citycraft.Yum.Yum;

/**
 * File命令基类
 * 
 * @since 2016年1月9日 上午10:02:39
 * @author 喵♂呜
 */
public class FileCommand {
    public FileCommand(final Yum yum) {
        final HandlerSubCommand cmdhandler = new HandlerSubCommand(yum, "file");
        cmdhandler.setAllCommandOnlyConsole(yum.config.getBoolean("onlyFileCommandConsole", true));
        cmdhandler.registerCommand(new CommandDownLoad(yum));
        cmdhandler.registerCommand(new CommandDelete(yum));
        cmdhandler.registerCommand(new CommandRename(yum));
        cmdhandler.registerCommand(new CommandRm(yum));
        cmdhandler.registerCommand(new CommandLs(yum));
    }
}
