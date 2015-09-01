/**
 *
 */
package cn.citycraft.Yum;

import org.bukkit.plugin.java.JavaPlugin;

import cn.citycraft.Yum.commands.CommandHandler;
import cn.citycraft.Yum.config.FileConfig;
import cn.citycraft.Yum.manager.YumManager;

/**
 * MC插件仓库
 *
 * @author 蒋天蓓 2015年8月21日下午5:14:39
 */
public class Yum extends JavaPlugin {
	public YumManager yumgr;
	public FileConfig config;

	@Override
	public void onLoad() {
		config = new FileConfig(this, "config.yml");
	}

	@Override
	public void onEnable() {
		CommandHandler cmdhandler = new CommandHandler(this);
		this.getCommand("yum").setExecutor(cmdhandler);
		this.getCommand("yum").setTabCompleter(cmdhandler);
		yumgr = new YumManager(this);
		YumManager.repo.jsonToCache(config);
	}

	@Override
	public void onDisable() {
		YumManager.repo.cacheToJson(config);
		config.save();
	}

}
