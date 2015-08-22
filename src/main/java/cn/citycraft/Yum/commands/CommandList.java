/**
 *
 */
package cn.citycraft.Yum.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import cn.citycraft.Yum.Yum;
import cn.citycraft.Yum.utils.PluginsManager;

/**
 * 插件查看命令类
 * 
 * @author 蒋天蓓 2015年8月12日下午2:04:05
 */
public class CommandList extends BaseCommand {
	Yum plugin;

	/**
	 * @param name
	 */
	public CommandList(Yum main) {
		super("list");
		this.plugin = main;
	}

	@Override
	public boolean isOnlyPlayerExecutable() {
		return false;
	};

	@Override
	public void execute(CommandSender sender, String label, String[] args) throws CommandException {
		sender.sendMessage("§6[Yum仓库]§3服务器已安装插件: ");
		for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
			sender.sendMessage("§6 - " + PluginsManager.getFormattedName(plugin, true));
		}
	}

	@Override
	public int getMinimumArguments() {
		return 0;
	}

	@Override
	public String getPossibleArguments() {
		return "";
	}
}
