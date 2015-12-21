/**
 *
 */
package cn.citycraft.Yum.file.commands;

import java.io.File;

import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;

import cn.citycraft.PluginHelper.commands.BaseCommand;
import cn.citycraft.Yum.Yum;

/**
 * 插件删除命令类
 *
 * @author 蒋天蓓 2015年8月12日下午2:04:05
 */
public class CommandRename extends BaseCommand {
	public CommandRename(final Yum main) {
		super("rn");
		setMinimumArguments(2);
		setDescription("重命名文件(服务器JAR为根目录)");
		setPossibleArguments("<文件相对目录> <文件名称>");
	}

	@Override
	public void execute(final CommandSender sender, final Command command, final String label, final String[] args) throws CommandException {
		final String fpath = args[0];
		final File file = new File(fpath);
		if (!file.exists()) {
			sendMessage(sender, "§c文件 " + file.getAbsolutePath() + " 不存在!");
		} else {
			try {
				final File newFile = new File(file.getParentFile(), args[1]);
				file.renameTo(newFile);
				sendMessage(sender, "§a文件 §e" + file.getAbsolutePath() + " §a重命名为 §d" + newFile.getAbsolutePath());
			} catch (final Exception e) {
				sendMessage(sender, "§c文件 §e" + file.getAbsolutePath() + " §c重命名失败: " + e.getMessage());
			}
		}
	}
}
