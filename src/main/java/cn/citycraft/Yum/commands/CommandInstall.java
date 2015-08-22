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
 * 插件安装命令类
 * 
 * @author 蒋天蓓 2015年8月12日下午2:04:05
 */
public class CommandInstall extends BaseCommand {
	Yum yum;

	/**
	 * @param name
	 */
	public CommandInstall(Yum main) {
		super("install");
		this.yum = main;
	}

	@Override
	public boolean isOnlyPlayerExecutable() {
		return false;
	};

	@Override
	public void execute(final CommandSender sender, String label, String[] args) throws CommandException {
		final String pluginname = args[0];
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginname);
		if (plugin == null) {
			Bukkit.getScheduler().runTaskAsynchronously(yum, new Runnable() {
				@Override
				public void run() {
					if (yum.download.run(sender, pluginname)) {
						sender.sendMessage(PluginsManager.load(pluginname));
					}
				}
			});
		} else {
			sender.sendMessage("§c插件已安装在服务器 需要更新请使用yum update " + pluginname + "!");
		}
	}

	@Override
	public int getMinimumArguments() {
		return 1;
	}

	@Override
	public String getPossibleArguments() {
		return "<插件名称>";
	}
}
