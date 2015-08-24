/**
 *
 */
package cn.citycraft.Yum;

import org.bukkit.plugin.java.JavaPlugin;

import cn.citycraft.Yum.commands.CommandHandler;
import cn.citycraft.Yum.config.FileConfig;
import cn.citycraft.Yum.utils.DownloadManager;

/**
 * MC插件仓库
 *
 * @author 蒋天蓓 2015年8月21日下午5:14:39
 */
public class Yum extends JavaPlugin {
	public DownloadManager download;
	public FileConfig config;

	@Override
	public void onEnable() {
		download = new DownloadManager(this);
		config = new FileConfig(this, "config.yml");
		CommandHandler cmdhandler = new CommandHandler(this);
		this.getCommand("yum").setExecutor(cmdhandler);
		this.getCommand("yum").setTabCompleter(cmdhandler);
	}
}
