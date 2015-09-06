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
 * @author 蒋天蓓 2015年8月12日下午2:04:05
 */
public class CommandUpgrade extends BaseCommand {
	Yum main;

	/**
	 * @param name
	 */
	public CommandUpgrade(Yum main) {
		super("upgrade");
		this.main = main;
	}

	@Override
	public void execute(final CommandSender sender, String label, final String[] args) throws CommandException {
		Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
			if (args.length == 0)
				YumManager.plugman.upgrade(sender);
			else {
				String pluginname = args[0];
				Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginname);
				sender.sendMessage("§a开始升级插件: " + pluginname);
				if (plugin != null)
					YumManager.plugman.upgrade(sender, plugin);
				else
					sender.sendMessage("§c插件未安装或已卸载 需要安装请使用yum install " + pluginname + "!");
			}
		});
	};

	@Override
	public int getMinimumArguments() {
		return 0;
	}

	@Override
	public String getPossibleArguments() {
		return "<插件名称> <插件版本>";
	}

	@Override
	public boolean isOnlyPlayerExecutable() {
		return false;
	}
}
