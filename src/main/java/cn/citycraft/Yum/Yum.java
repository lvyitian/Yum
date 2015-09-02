/**
 *
 */
package cn.citycraft.Yum;

import org.bukkit.plugin.java.JavaPlugin;

import cn.citycraft.Yum.commands.CommandHandler;
import cn.citycraft.Yum.manager.YumManager;
import cn.citycraft.config.FileConfig;
import cn.citycraft.utils.VersionChecker;

/**
 * MC插件仓库
 *
 * @author 蒋天蓓 2015年8月21日下午5:14:39
 */
public class Yum extends JavaPlugin {
	public YumManager yumgr;
	public FileConfig config;

	@Override
	public void onDisable() {
		YumManager.repo.cacheToJson(config);
		config.save();
	}

	@Override
	public void onEnable() {
		CommandHandler cmdhandler = new CommandHandler(this);
		this.getCommand("yum").setExecutor(cmdhandler);
		this.getCommand("yum").setTabCompleter(cmdhandler);
		yumgr = new YumManager(this);
		YumManager.repo.jsonToCache(config);
		new VersionChecker(this);
	}

	@Override
	public void onLoad() {
		config = new FileConfig(this, "config.yml");
	}

}
