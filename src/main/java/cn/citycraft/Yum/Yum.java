/**
 * 
 */
package cn.citycraft.Yum;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import cn.citycraft.Yum.utils.DownloadUtils;
import cn.citycraft.Yum.utils.PluginUtil;

/**
 * MC插件仓库
 * 
 * @author 蒋天蓓
 *         2015年8月21日下午5:14:39
 */
public class Yum extends JavaPlugin {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		switch (args.length) {
		case 0:
			break;
		case 1:
			switch (args[0]) {
			case "list":
				sender.sendMessage("§3服务器已安装插件: ");
				for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
					sender.sendMessage("§6 - " + PluginUtil.getFormattedName(plugin, true));
				}
				break;
			}
			break;
		case 2:
			Plugin plugin = this.getServer().getPluginManager().getPlugin(args[1]);
			switch (args[0]) {
			case "install":
				if (DownloadUtils.download(sender, args[1]))
					sender.sendMessage(PluginUtil.load(args[1]));
				break;
			case "remove":
				if (plugin != null) {
					sender.sendMessage(PluginUtil.unload(plugin));
				} else {
					sender.sendMessage("插件不存在或已卸载!");
				}
				break;
			case "update":
				if (plugin != null) {
					if (DownloadUtils.download(sender, args[1]))
						sender.sendMessage(PluginUtil.load(args[1]));
				} else {
					sender.sendMessage("插件不存在或已卸载!");
				}
				break;
			}
		}
		return true;
	}
}
