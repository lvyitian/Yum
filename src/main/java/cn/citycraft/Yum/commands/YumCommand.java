package cn.citycraft.Yum.commands;

import cn.citycraft.PluginHelper.commands.HandlerSubCommand;
import cn.citycraft.Yum.Yum;

public class YumCommand {
	public YumCommand(final Yum yum) {
		final HandlerSubCommand cmdhandler = new HandlerSubCommand(yum, "yum");
		cmdhandler.setAllCommandOnlyConsole(yum.config.getBoolean("onlyCommandConsole", false));
		cmdhandler.registerCommand(new CommandList(yum));
		cmdhandler.registerCommand(new CommandInstall(yum));
		cmdhandler.registerCommand(new CommandUpdate(yum));
		cmdhandler.registerCommand(new CommandUpdateAll(yum));
		cmdhandler.registerCommand(new CommandDelete(yum));
		cmdhandler.registerCommand(new CommandInfo(yum));
		cmdhandler.registerCommand(new CommandRepo(yum));
		cmdhandler.registerCommand(new CommandReload(yum));
		cmdhandler.registerCommand(new CommandLoad(yum));
		cmdhandler.registerCommand(new CommandUnload(yum));
		cmdhandler.registerCommand(new CommandUpgrade(yum));
	}
}
