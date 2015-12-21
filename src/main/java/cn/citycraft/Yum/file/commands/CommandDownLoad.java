/**
 *
 */
package cn.citycraft.Yum.file.commands;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;

import cn.citycraft.PluginHelper.commands.BaseCommand;
import cn.citycraft.Yum.Yum;
import cn.citycraft.Yum.api.YumAPI;
import cn.citycraft.Yum.manager.DownloadManager;

/**
 * 插件删除命令类
 *
 * @author 蒋天蓓 2015年8月12日下午2:04:05
 */
public class CommandDownLoad extends BaseCommand {
	Yum main;
	DownloadManager dl;

	public CommandDownLoad(final Yum main) {
		super("d");
		this.main = main;
		dl = YumAPI.getDownload();
		setMinimumArguments(1);
		setDescription("下载文件(默认保存到服务器更新文件夹)");
		setPossibleArguments("<下载地址> [保存文件路径]");
	}

	@Override
	public void execute(final CommandSender sender, final Command command, final String label, final String[] args) throws CommandException {
		String urlstr = args[0];
		if (!urlstr.startsWith("http")) {
			urlstr = "http://" + urlstr;
		}
		File file = null;
		if (args.length == 2) {
			file = new File(args[1]);
		} else {
			file = new File(Bukkit.getUpdateFolderFile(), dl.getFileName(urlstr));
		}
		dl.run(sender, urlstr, file);
	}
}
