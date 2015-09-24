/**
 *
 */
package cn.citycraft.Yum;

import java.io.IOException;

import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import cn.citycraft.PluginHelper.config.FileConfig;
import cn.citycraft.PluginHelper.utils.VersionChecker;
import cn.citycraft.Yum.commands.HandlerCommand;
import cn.citycraft.Yum.manager.YumManager;

/**
 * MC插件仓库
 *
 * @author 蒋天蓓 2015年8月21日下午5:14:39
 */
public class Yum extends JavaPlugin {
	public FileConfig config;
	public YumManager yumgr;

	@Override
	public void onDisable() {
		YumManager.repo.cacheToJson(config);
		config.save();
	}

	@Override
	public void onEnable() {
		final HandlerCommand cmdhandler = new HandlerCommand(this);
		this.getCommand("yum").setExecutor(cmdhandler);
		this.getCommand("yum").setTabCompleter(cmdhandler);
		yumgr = new YumManager(this);
		YumManager.repo.jsonToCache(config);
		new VersionChecker(this);
		try {
			final Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (final IOException e) {
		}
	}

	@Override
	public void onLoad() {
		config = new FileConfig(this);
	}

}
