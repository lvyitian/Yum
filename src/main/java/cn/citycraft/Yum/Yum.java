/**
 * 
 */
package cn.citycraft.Yum;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import cn.citycraft.Yum.utils.DownloadUtils;

/**
 * MC插件仓库
 * 
 * @author 蒋天蓓
 *         2015年8月21日下午5:14:39
 */
public class Yum extends JavaPlugin {
	String url = "http://ci.citycraft.cn:8800/jenkins/job/%1$s/lastSuccessfulBuild/artifact/target/%1$s.jar";

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		switch (args.length) {
		case 0:
			break;
		case 2:
			switch (args[0]) {
			case "install":
				if (DownloadUtils.download(String.format(url, args[1]), getDataFolder().getParent(), args[1])) {
					sender.sendMessage("OK");
				} else {
					sender.sendMessage("Error");
				}
				break;
			case "remove":
				break;
			}
			break;

		}
		return true;
	}

}
