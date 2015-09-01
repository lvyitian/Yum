/**
 *
 */
package cn.citycraft.Yum;

import org.bukkit.plugin.java.JavaPlugin;

import cn.citycraft.Yum.commands.CommandHandler;
import cn.citycraft.Yum.config.FileConfig;
import cn.citycraft.Yum.manager.DownloadManager;
import cn.citycraft.Yum.manager.PluginsManager;
import cn.citycraft.Yum.repository.RepositoryManager;

/**
 * MC插件仓库
 *
 * @author 蒋天蓓 2015年8月21日下午5:14:39
 */
public class Yum extends JavaPlugin {
	public DownloadManager download;
	public PluginsManager plugman;
	public RepositoryManager repo;
	public FileConfig config;

	@Override
	public void onEnable() {
		CommandHandler cmdhandler = new CommandHandler(this);
		this.getCommand("yum").setExecutor(cmdhandler);
		this.getCommand("yum").setTabCompleter(cmdhandler);
		plugman = new PluginsManager(this);
		download = new DownloadManager(this);
		repo = new RepositoryManager(this);
		config = new FileConfig(this, "config.yml");
		repo.jsonToCache(config.getString("cache"));
	}

	@Override
	public void onDisable() {
		if (config != null)
			config.set("cache", repo.cacheToJson());
		config.save();
	}

}
