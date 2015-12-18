package cn.citycraft.Yum.file.commands;

import cn.citycraft.PluginHelper.commands.HandlerSubCommand;
import cn.citycraft.Yum.Yum;

public class FileCommand {
	public FileCommand(final Yum yum) {
		final HandlerSubCommand cmdhandler = new HandlerSubCommand(yum, "file");
		cmdhandler.setAllCommandOnlyConsole(yum.config.getBoolean("onlyFileCommandConsole", true));
		cmdhandler.registerCommand(new CommandDownLoad(yum));
		cmdhandler.registerCommand(new CommandDelete(yum));
		cmdhandler.registerCommand(new CommandRm(yum));
		cmdhandler.registerCommand(new CommandLs(yum));
	}
}
