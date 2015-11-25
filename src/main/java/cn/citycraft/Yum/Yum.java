/**
 *
 */
package cn.citycraft.Yum;

import org.bukkit.plugin.java.JavaPlugin;

import cn.citycraft.PluginHelper.commands.HandlerSubCommand;
import cn.citycraft.PluginHelper.config.FileConfig;
import cn.citycraft.PluginHelper.utils.VersionChecker;
import cn.citycraft.Yum.api.YumAPI;
import cn.citycraft.Yum.commands.CommandDelete;
import cn.citycraft.Yum.commands.CommandInfo;
import cn.citycraft.Yum.commands.CommandInstall;
import cn.citycraft.Yum.commands.CommandList;
import cn.citycraft.Yum.commands.CommandLoad;
import cn.citycraft.Yum.commands.CommandReload;
import cn.citycraft.Yum.commands.CommandRepo;
import cn.citycraft.Yum.commands.CommandUnload;
import cn.citycraft.Yum.commands.CommandUpdate;
import cn.citycraft.Yum.commands.CommandUpdateAll;
import cn.citycraft.Yum.commands.CommandUpgrade;

/**
 * MC插件仓库
 *
 * @author 蒋天蓓 2015年8月21日下午5:14:39
 */
public class Yum extends JavaPlugin {
	public FileConfig config;
	public YumAPI yumgr;

	public void initCommands() {
		final HandlerSubCommand cmdhandler = new HandlerSubCommand(this, "yum");
		cmdhandler.registerCommand(new CommandList(this));
		cmdhandler.registerCommand(new CommandInstall(this));
		cmdhandler.registerCommand(new CommandUpdate(this));
		cmdhandler.registerCommand(new CommandUpdateAll(this));
		cmdhandler.registerCommand(new CommandDelete(this));
		cmdhandler.registerCommand(new CommandInfo(this));
		cmdhandler.registerCommand(new CommandRepo(this));
		cmdhandler.registerCommand(new CommandReload(this));
		cmdhandler.registerCommand(new CommandLoad(this));
		cmdhandler.registerCommand(new CommandUnload(this));
		cmdhandler.registerCommand(new CommandUpgrade(this));
	}

	@Override
	public void onDisable() {
		YumAPI.repo.cacheToJson(config);
		config.save();
	}

	@Override
	public void onEnable() {
		this.initCommands();
		yumgr = new YumAPI(this);
		YumAPI.repo.jsonToCache(config);
		YumAPI.updaterepo();
		new VersionChecker(this);
	}

	@Override
	public void onLoad() {
		config = new FileConfig(this);
	}

}
