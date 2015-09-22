/**
 *
 */
package cn.citycraft.Yum.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

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
	public CommandInstall(Yum main) {
		super("install", "安装插件");
		this.main = main;
		setMinimumArguments(1);
		setPossibleArguments("<插件名称>");
	}

	@Override
	public void execute(final CommandSender sender, String label, final String[] args) throws CommandException {
		final String pluginname = args[0];
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginname);
		if (plugin == null)
			Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
				if (args.length < 2)
					YumManager.install(sender, pluginname);
				else
					YumManager.install(sender, pluginname, args[1]);
			});
		else
			sender.sendMessage("§c插件已安装在服务器 需要更新请使用yum update " + pluginname + "!");

	};
}
