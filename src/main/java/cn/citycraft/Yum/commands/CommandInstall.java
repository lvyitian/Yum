/**
 *
 */
package cn.citycraft.Yum.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import cn.citycraft.PluginHelper.commands.BaseCommand;
import cn.citycraft.PluginHelper.utils.StringUtil;
import cn.citycraft.Yum.Yum;
import cn.citycraft.Yum.manager.YumManager;

/**
 * 插件安装命令类
 *
 * @author 蒋天蓓 2015年8月12日下午2:04:05
 */
public class CommandInstall extends BaseCommand {
	Yum main;

	/**
	 * @param name
	 */
	public CommandInstall(final Yum main) {
		super("install");
		this.main = main;
		setMinimumArguments(1);
		setDescription("安装插件");
		setPossibleArguments("<插件名称>");
	}

	@Override
	public void execute(final CommandSender sender, final Command command, final String label, final String[] args) throws CommandException {
		final String pluginname = args[0];
		final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginname);
		if (plugin == null) {
			Bukkit.getScheduler().runTaskAsynchronously(main, new Runnable() {
				@Override
				public void run() {
					if (args.length < 2) {
						YumManager.install(sender, pluginname);
					} else {
						YumManager.install(sender, pluginname, args[1]);
					}
				}
			});
		} else {
			sender.sendMessage("§c插件" + pluginname + "已安装在服务器 需要更新请使用yum update " + pluginname + "!");
		}
	}

	@Override
	public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
		if (args[0].equalsIgnoreCase("install")) {
			return StringUtil.copyPartialMatches(args[1], YumManager.repo.getAllPluginName(), new ArrayList<String>());
		}
		return null;
	}
}
