/**
 *
 */
package cn.citycraft.Yum.commands;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import cn.citycraft.PluginHelper.utils.StringUtil;
import cn.citycraft.Yum.Yum;
import cn.citycraft.Yum.manager.YumManager;

/**
 * 插件删除命令类
 *
 * @author 蒋天蓓 2015年8月12日下午2:04:05
 */
public class CommandInfo extends BaseCommand {
	Yum main;

	/**
	 * @param name
	 */
	public CommandInfo(final Yum main) {
		super("info", "查看插件详情");
		this.main = main;
		setPossibleArguments("<插件名称>");
	}

	@Override
	public void execute(final CommandSender sender, final String label, final String[] args) throws CommandException {
		if (args.length == 0) {
			return;
		}
		final String pluginname = args[0];
		final Plugin plugin = main.getServer().getPluginManager().getPlugin(pluginname);
		if (plugin != null) {
			final PluginDescriptionFile desc = plugin.getDescription();
			sender.sendMessage("§6插件名称: §3" + plugin.getName());
			sender.sendMessage("§6插件版本: §3" + desc.getVersion());
			sender.sendMessage("§6插件作者: §3" + StringUtils.join(desc.getAuthors(), " "));
			sender.sendMessage("§6插件描述: §3" + (desc.getDescription() == null ? "无" : desc.getDescription()));
			sender.sendMessage("§6插件依赖: §3" + (desc.getDepend().size() == 0 ? "无" : ""));
			StringUtil.sendStringArray(sender, desc.getDepend());
			sender.sendMessage("§6插件软依赖: §3" + (desc.getSoftDepend().size() == 0 ? "无" : ""));
			StringUtil.sendStringArray(sender, desc.getSoftDepend());
			sender.sendMessage("§6插件物理路径: §3" + YumManager.plugman.getPluginFile(plugin).getAbsolutePath());
		} else {
			sender.sendMessage("§4错误: §c插件 " + pluginname + " 不存在或已卸载!");
		}
	};
}
