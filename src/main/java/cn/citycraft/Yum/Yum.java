/**
 *
 */
package cn.citycraft.Yum;

import org.bukkit.plugin.java.JavaPlugin;

import cn.citycraft.Yum.commands.CommandHandler;
import cn.citycraft.Yum.utils.DownloadManager;

/**
 * MC插件仓库
 *
 * @author 蒋天蓓 2015年8月21日下午5:14:39
 */
public class Yum extends JavaPlugin {
	public DownloadManager download;

	@Override
	public void onEnable() {
		download = new DownloadManager(this);
		this.getCommand("yum").setExecutor(new CommandHandler(this));
		this.getCommand("yum").setTabCompleter(new CommandHandler(this));
	}
}
