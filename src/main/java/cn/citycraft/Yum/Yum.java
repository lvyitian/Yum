/**
 *
 */
package cn.citycraft.Yum;

import org.bukkit.plugin.java.JavaPlugin;

import cn.citycraft.PluginHelper.config.FileConfig;
import cn.citycraft.PluginHelper.utils.VersionChecker;
import cn.citycraft.Yum.api.YumAPI;
import cn.citycraft.Yum.commands.YumCommand;
import cn.citycraft.Yum.file.commands.FileCommand;

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
	}

	@Override
	public void onEnable() {
		new YumAPI(this);
		new YumCommand(this);
		new FileCommand(this);
		YumAPI.getRepo().jsonToCache(config);
		YumAPI.updaterepo();
		new VersionChecker(this);
	}

	@Override
	public void onLoad() {
		config = new FileConfig(this);
	}

}
