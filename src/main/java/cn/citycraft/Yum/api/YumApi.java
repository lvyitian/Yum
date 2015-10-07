/**
 *
 */
package cn.citycraft.Yum.api;

import java.net.URL;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import cn.citycraft.Yum.manager.YumManager;

/**
 * Yum仓库插件API
 *
 * @author 蒋天蓓
 * @since 2015年8月22日下午4:43:41
 */
public class YumApi {
	public static boolean update(final CommandSender sender, final Plugin plugin, final URL url, final String version) {
		return YumManager.update(sender, plugin, url, version);
	}
}
