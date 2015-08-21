/**
 *
 */
package cn.citycraft.Yum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;

import cn.citycraft.Yum.utils.DownloadUtils;
import cn.citycraft.Yum.utils.PluginUtil;

/**
 * MC插件仓库
 *
 * @author 蒋天蓓 2015年8月21日下午5:14:39
 */
public class Yum extends JavaPlugin {
	DownloadUtils download;

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
				if (plugin != null) {
					if (download.download(sender, args[1])) {
						sender.sendMessage(PluginUtil.load(args[1]));
					}
				} else {
					sender.sendMessage("§c插件已安装在服务器!");
				}
				break;
			case "remove":
				if (plugin != null) {
					sender.sendMessage(PluginUtil.unload(plugin));
				} else {
					sender.sendMessage("§c插件不存在或已卸载!");
				}
				break;
			case "update":
				if (plugin != null) {
					sender.sendMessage(PluginUtil.unload(plugin));
					if (download.download(sender, args[1])) {
						sender.sendMessage(PluginUtil.load(args[1]));
					}
				} else {
					sender.sendMessage("§c插件不存在或已卸载!");
				}
				break;
			}
		}
		return true;
	};

	@Override
	public void onEnable() {
		download = new DownloadUtils(this);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		final String[] COMMANDS = { "install", "remove", "list", "update" };
		if (sender.isOp() || sender.hasPermission("yum.admin") || sender.hasPermission("yum." + args[0])) {
			List<String> completions = new ArrayList<>();
			if (args.length == 1) {
				String partialCommand = args[0];
				List<String> commands = new ArrayList<>(Arrays.asList(COMMANDS));
				StringUtil.copyPartialMatches(partialCommand, commands, completions);
			}
			if (args.length == 2) {
				String partialPlugin = args[1];
				List<String> plugins = PluginUtil.getPluginNames(false);
				StringUtil.copyPartialMatches(partialPlugin, plugins, completions);
			}
			Collections.sort(completions);
			return completions;
		}
		return null;
	}
}
