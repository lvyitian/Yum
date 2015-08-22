/**
 *
 */
package cn.citycraft.Yum.commands;

import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import cn.citycraft.Yum.Yum;
import cn.citycraft.Yum.utils.PluginsManager;
import cn.citycraft.Yum.utils.StringUtil;

/**
 * 插件删除命令类
 * 
 * @author 蒋天蓓 2015年8月12日下午2:04:05
 */
public class CommandInfo extends BaseCommand {
	Yum yum;

	/**
	 * @param name
	 */
	public CommandInfo(Yum main) {
		super("info");
		this.yum = main;
	}

	@Override
	public boolean isOnlyPlayerExecutable() {
		return false;
	};

	@Override
	public void execute(CommandSender sender, String label, String[] args) throws CommandException {
		if (args.length == 0) {
			return;
		}
		String pluginname = args[0];
		Plugin plugin = yum.getServer().getPluginManager().getPlugin(pluginname);
		if (plugin != null) {
			PluginDescriptionFile desc = plugin.getDescription();
			sender.sendMessage("§6插件名称: §3" + plugin.getName());
			sender.sendMessage("§6插件版本: §3" + desc.getVersion());
			sender.sendMessage("§6插件作者: §3" + desc.getAuthors().toArray(new String[0]).toString());
			sender.sendMessage("§6插件描述: §3" + desc.getDescription() == null ? "无" : desc.getDescription());
			sender.sendMessage("§6插件依赖: §3" + (desc.getDepend().size() == 0 ? "无" : ""));
			StringUtil.sendStringArray(sender, desc.getDepend());
			sender.sendMessage("§6插件软依赖: §3" + (desc.getSoftDepend().size() == 0 ? "无" : ""));
			StringUtil.sendStringArray(sender, desc.getSoftDepend());
			sender.sendMessage("§6插件载入前: §3" + (desc.getLoadBefore().size() == 0 ? "无" : ""));
			StringUtil.sendStringArray(sender, desc.getLoadBefore());
			sender.sendMessage("§6插件物理路径: §3" + PluginsManager.getPluginFile(plugin).getAbsolutePath());
		} else {
			sender.sendMessage("§c插件 " + pluginname + " 不存在或已卸载!");
		}
	}

	@Override
	public int getMinimumArguments() {
		return 0;
	}

	@Override
	public String getPossibleArguments() {
		return "<插件名称>";
	}
}
