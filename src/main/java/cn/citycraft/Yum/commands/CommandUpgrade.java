/**
 *
 */
package cn.citycraft.Yum.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import cn.citycraft.PluginHelper.commands.BaseCommand;
import cn.citycraft.Yum.Yum;
import cn.citycraft.Yum.manager.YumManager;

/**
 * @author 蒋天蓓 2015年8月12日下午2:04:05
 */
public class CommandUpgrade extends BaseCommand {
	Yum main;

	/**
	 * @param name
	 */
	public CommandUpgrade(final Yum main) {
		super("upgrade");
		this.main = main;
		setDescription("升级插件");
		setPossibleArguments("[插件名称]");
	}

	@Override
	public void execute(final CommandSender sender, final Command command, final String label, final String[] args) throws CommandException {
		Bukkit.getScheduler().runTaskAsynchronously(main, new Runnable() {
			@Override
			public void run() {
				if (args.length == 0) {
					YumManager.plugman.upgrade(sender);
				} else {
					final String pluginname = args[0];
					final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginname);
					sender.sendMessage("§a开始升级插件: " + pluginname);
					if (plugin != null) {
						YumManager.plugman.upgrade(sender, plugin);
					} else {
						sender.sendMessage("§c插件未安装或已卸载 需要安装请使用yum install " + pluginname + "!");
					}
				}
			}
		});
	};
}
