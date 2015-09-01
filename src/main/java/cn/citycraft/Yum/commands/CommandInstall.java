/**
 *
 */
package cn.citycraft.Yum.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import cn.citycraft.Yum.Yum;

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
		super("install");
		this.main = main;
	}

	@Override
	public void execute(final CommandSender sender, String label, final String[] args) throws CommandException {
		final String pluginname = args[0];
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginname);
		if (plugin == null) {
			Bukkit.getScheduler().runTaskAsynchronously(main, new Runnable() {
				@Override
				public void run() {

				}
			});
		} else {
			sender.sendMessage("§c插件已安装在服务器 需要更新请使用yum update " + pluginname + "!");
		}

	};

	// public static boolean installFromYum(CommandSender sender, String
	// filename) {
	// if (sender == null) {
	// sender = Bukkit.getConsoleSender();
	// }
	// File file = new File("plugins/YumCenter", filename + ".jar");
	// if (!file.exists()) {
	// sender.sendMessage("§4错误: §c仓库不存在 " + filename + " 插件!");
	// return false;
	// }
	// File pluginfile = new File("plugins", filename + ".jar");
	// FileUtil.copyFile(file, pluginfile);
	// if (PluginsManager.load(sender, filename + ".jar")) {
	// sender.sendMessage("§6安装: §a从Yum仓库安装插件 " + filename + " 成功!");
	// }
	// return false;
	// }

	@Override
	public int getMinimumArguments() {
		return 1;
	}

	@Override
	public String getPossibleArguments() {
		return "<插件名称>";
	}

	@Override
	public boolean isOnlyPlayerExecutable() {
		return false;
	}
}
