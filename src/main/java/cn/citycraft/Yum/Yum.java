/**
 *
 */
package cn.citycraft.Yum;

import org.bukkit.plugin.java.JavaPlugin;

import cn.citycraft.PluginHelper.commands.HandlerSubCommand;
import cn.citycraft.PluginHelper.config.FileConfig;
import cn.citycraft.PluginHelper.utils.VersionChecker;
import cn.citycraft.Yum.commands.CommandDelete;
import cn.citycraft.Yum.commands.CommandInfo;
import cn.citycraft.Yum.commands.CommandInstall;
import cn.citycraft.Yum.commands.CommandList;
import cn.citycraft.Yum.commands.CommandLoad;
import cn.citycraft.Yum.commands.CommandReload;
import cn.citycraft.Yum.commands.CommandRepo;
import cn.citycraft.Yum.commands.CommandUnload;
import cn.citycraft.Yum.commands.CommandUpdate;
import cn.citycraft.Yum.commands.CommandUpgrade;
import cn.citycraft.Yum.manager.YumManager;

/**
 * MC插件仓库
 *
 * @author 蒋天蓓 2015年8月21日下午5:14:39
 */
public class Yum extends JavaPlugin {
	public FileConfig config;
	public YumManager yumgr;

	public void initCommands() {
		final HandlerSubCommand cmdhandler = new HandlerSubCommand(this);
		cmdhandler.registerCommand(new CommandList(this));
		cmdhandler.registerCommand(new CommandInstall(this));
		cmdhandler.registerCommand(new CommandUpdate(this));
		cmdhandler.registerCommand(new CommandDelete(this));
		cmdhandler.registerCommand(new CommandInfo(this));
		cmdhandler.registerCommand(new CommandRepo(this));
		cmdhandler.registerCommand(new CommandReload(this));
		cmdhandler.registerCommand(new CommandLoad(this));
		cmdhandler.registerCommand(new CommandUnload(this));
		cmdhandler.registerCommand(new CommandUpgrade(this));
		this.getCommand("yum").setExecutor(cmdhandler);
		this.getCommand("yum").setTabCompleter(cmdhandler);
	}

	@Override
	public void onDisable() {
		YumManager.repo.cacheToJson(config);
		config.save();
	}

	@Override
	public void onEnable() {
		this.initCommands();
		yumgr = new YumManager(this);
		YumManager.repo.jsonToCache(config);
		YumManager.updaterepo();
		new VersionChecker(this);
	}

	@Override
	public void onLoad() {
		config = new FileConfig(this);
	}

}
