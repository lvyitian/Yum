/**
 *
 */
package cn.citycraft.Yum.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

/**
 * @author 蒋天蓓 2015年8月21日下午6:08:09 TODO
 */
public class DownloadManager {
	String yumurl = "http://ci.citycraft.cn:8800/jenkins/job/%1$s/lastSuccessfulBuild/artifact/target/%1$s.jar";
	Plugin plugin;

	public DownloadManager(Plugin main) {
		this.plugin = main;
	}

	public String getFileName(URL url) {
		int end = url.getFile().lastIndexOf('/');
		return url.getFile().substring(end + 1);
	}

	public String getFileName(String url) {
		int end = url.lastIndexOf('/');
		return url.substring(end + 1);
	}

	private String getPer(int per) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 11; i++) {
			if (per > i) {
				sb.append("==");
			} else if (per == i) {
				sb.append("> ");
			} else {
				sb.append("  ");
			}
		}
		return sb.toString();
	}

	public URL getUrl(String pluginname) {
		try {
			return new URL(String.format(yumurl, pluginname));
		} catch (MalformedURLException e) {
			return null;
		}
	}

	public boolean install(CommandSender sender, String pluginname) {
		return run(sender, getUrl(pluginname), new File("plugins", pluginname + ".jar"));
	}

	public boolean run(CommandSender sender, URL url, File file) {
		BufferedInputStream in = null;
		FileOutputStream fout = null;
		if (sender == null) {
			sender = Bukkit.getConsoleSender();
		}
		try {
			sender.sendMessage("§6开始下载: §3" + getFileName(url));
			sender.sendMessage("§6下载地址: §3" + url.toString());
			int fileLength = url.openConnection().getContentLength();
			sender.sendMessage("§6文件长度: §3" + fileLength);
			in = new BufferedInputStream(url.openStream());
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
				sender.sendMessage("§6创建新目录: §d" + file.getParentFile().getAbsolutePath());
			}
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
			sender.sendMessage("§6创建新文件: §d" + file.getAbsolutePath());
			fout = new FileOutputStream(file);
			byte[] data = new byte[1024];
			long downloaded = 0L;
			int count;
			while ((count = in.read(data)) != -1) {
				downloaded += count;
				fout.write(data, 0, count);
				int percent = (int) (downloaded * 100L / fileLength);
				if (percent % 10 == 0) {
					sender.sendMessage(String.format("§6已下载: §a" + getPer(percent / 10) + " %s%%", percent));
				}
			}
			sender.sendMessage("§6文件: §a" + file.getName() + " 下载完成!");
			return true;
		} catch (Exception ex) {
			sender.sendMessage("§6文件: §c" + file.getName() + "下载失败!");
			ex.printStackTrace();
			return false;
		} finally {
			try {
				if (in != null) {
					in.close();
					fout.close();
				}
			} catch (Exception ex) {
			}
		}
	}

	public boolean run(URL url, File file) {
		return run(null, url, file);
	}

	public boolean update(CommandSender sender, Plugin plugin) {
		String pluginname = plugin.getName();
		String filename = PluginsManager.getPluginFile(plugin).getName();
		URL url = getUrl(pluginname);
		if (url == null) {
			sender.sendMessage("§4错误: §cURL地址解析失败!");
			return false;
		}
		return run(sender, url, new File("plugins/update", filename));
	}

	public boolean yumdl(CommandSender sender, String address, String pluginname) {
		try {
			URL url = new URL(address);
			File yumplugin = new File("plugins/YumCenter", pluginname + ".jar");
			if (yumplugin.exists()) {
				sender.sendMessage("§6更新: §e仓库已存在插件 " + pluginname + " 开始更新...");
				yumplugin.delete();
			}
			return run(sender, url, yumplugin);
		} catch (MalformedURLException e) {
			return false;
		}
	}

	public boolean yumdl(CommandSender sender, String address) {
		String pluginname = getFileName(address);
		return yumdl(sender, pluginname, address);
	}

	public boolean yum(CommandSender sender, String pluginname) {
		File yumplugin = new File("plugins/YumCenter", pluginname + ".jar");
		if (yumplugin.exists()) {
			sender.sendMessage("§6更新: §e仓库已存在插件 " + pluginname + " 开始更新...");
			yumplugin.delete();
		}
		return run(sender, getUrl(pluginname), yumplugin);
	}
}
