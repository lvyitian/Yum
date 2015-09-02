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
public class CommandUpdate extends BaseCommand {
	Yum main;

	/**
	 * @param name
	 */
	public CommandUpdate(Yum main) {
		super("update");
		this.main = main;
	}

	@Override
	public void execute(final CommandSender sender, String label, final String[] args) throws CommandException {
		final String pluginname = args[0];
		final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginname);
		sender.sendMessage("§a开始更新插件: " + pluginname);
		if (plugin != null)
			Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
				if (args.length < 2)
					YumManager.update(sender, plugin);
				else
					YumManager.update(sender, plugin, args[1]);
			});
		else
			sender.sendMessage("§c插件未安装或已卸载 需要安装请使用yum install " + pluginname + "!");
	};

	@Override
	public int getMinimumArguments() {
		return 1;
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
