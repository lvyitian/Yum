/**
 *
 */
package cn.citycraft.Yum;

import org.bukkit.plugin.java.JavaPlugin;

import cn.citycraft.PluginHelper.config.FileConfig;
import cn.citycraft.PluginHelper.utils.VersionChecker;
import cn.citycraft.Yum.api.YumAPI;
import cn.citycraft.Yum.commands.YumCommand;

/**
 * MC插件仓库
 *
 * @author 蒋天蓓 2015年8月21日下午5:14:39
 */
public class Yum extends JavaPlugin {
	public FileConfig config;

	@Override
	public void onDisable() {
		YumAPI.getRepo().cacheToJson(config);
		config.save();
	}

	@Override
	public void onEnable() {
		new YumCommand(this);
		new YumAPI(this);
		YumAPI.getRepo().jsonToCache(config);
		YumAPI.updaterepo();
		new VersionChecker(this);
	}

	@Override
	public void onLoad() {
		config = new FileConfig(this);
	}

}
