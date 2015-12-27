/**
 *
 */
package cn.citycraft.Yum.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import cn.citycraft.PluginHelper.commands.BaseCommand;
import cn.citycraft.PluginHelper.utils.StringUtil;
import cn.citycraft.Yum.Yum;
import cn.citycraft.Yum.api.YumAPI;

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
		super();
		this.main = main;
		setMinimumArguments(1);
		setDescription("查看插件详情");
		setPossibleArguments("<插件名称>");
	}

	@Override
	public void execute(final CommandSender sender, final Command command, final String label, final String[] args) throws CommandException {
		final String pluginname = args[0];
		final Plugin plugin = main.getServer().getPluginManager().getPlugin(pluginname);
		if (plugin != null) {
			final PluginDescriptionFile desc = plugin.getDescription();
			sender.sendMessage("§6插件名称: §3" + plugin.getName());
			sender.sendMessage("§6插件版本: §3" + desc.getVersion());
			sender.sendMessage("§6插件作者: §3" + StringUtils.join(desc.getAuthors(), " "));
			sender.sendMessage("§6插件描述: §3" + (desc.getDescription() == null ? "无" : desc.getDescription()));
			sender.sendMessage("§6插件依赖: §3" + (desc.getDepend().isEmpty() ? "无" : ""));
			StringUtil.sendStringArray(sender, desc.getDepend(), "§6 - §a");
			sender.sendMessage("§6插件软依赖: §3" + (desc.getSoftDepend().isEmpty() ? "无" : ""));
			StringUtil.sendStringArray(sender, desc.getSoftDepend(), "§6 - §a");
			final Map<String, Map<String, Object>> clist = desc.getCommands();
			if (clist != null) {
				sender.sendMessage("§6插件注册命令: §3" + (clist.isEmpty() ? "无" : ""));
				StringUtil.sendStringArray(sender, clist.keySet(), "§6 - §a");
			}
			final List<Permission> plist = desc.getPermissions();
			if (plist != null) {
				sender.sendMessage("§6插件注册权限: " + (plist.isEmpty() ? "无" : ""));
				for (final Permission perm : plist) {
					sender.sendMessage("§6 - §a" + perm.getName() + "§6 - §e" + (perm.getDescription().isEmpty() ? "无描述" : perm.getDescription()));
				}
			}
			sender.sendMessage("§6插件物理路径: §3" + YumAPI.getPlugman().getPluginFile(plugin).getAbsolutePath());
		} else {
			sender.sendMessage("§4错误: §c插件 " + pluginname + " 不存在或已卸载!");
		}
	}

	@Override
	public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
		if (!args[0].equalsIgnoreCase("install") && !args[0].equalsIgnoreCase("repo")) {
			return StringUtil.copyPartialMatches(args[1], YumAPI.getPlugman().getPluginNames(false), new ArrayList<String>());
		}
		return null;
	}
}
