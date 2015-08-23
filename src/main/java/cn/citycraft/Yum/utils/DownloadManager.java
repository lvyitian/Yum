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
		CommandSender resultsender = sender;
		if (sender == null) {
			resultsender = Bukkit.getConsoleSender();
		}
		try {
			resultsender.sendMessage("§6开始下载: §3" + getFileName(url));
			resultsender.sendMessage("§6下载地址: §3" + url.getPath());
			int fileLength = url.openConnection().getContentLength();
			resultsender.sendMessage("§6文件长度: §3" + fileLength);
			in = new BufferedInputStream(url.openStream());
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
				resultsender.sendMessage("§d创建新目录: " + file.getParentFile().getAbsolutePath());
			}
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
			resultsender.sendMessage("§6创建新文件: §d" + file.getAbsolutePath());
			fout = new FileOutputStream(file);
			byte[] data = new byte[1024];
			long downloaded = 0L;
			int count;
			while ((count = in.read(data)) != -1) {
				downloaded += count;
				fout.write(data, 0, count);
				int percent = (int) (downloaded * 100L / fileLength);
				if (percent % 10 == 0) {
					resultsender.sendMessage(String.format("§6已下载: §a" + getPer(percent / 10) + " %s%%", percent));
				}
			}
			resultsender.sendMessage("§a文件: " + file.getName() + " 下载完成!");
			return true;
		} catch (Exception ex) {
			resultsender.sendMessage("§c文件" + file.getName() + "下载失败!");
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
		return run(sender, getUrl(pluginname), new File("plugins/update", filename));
	}

	public boolean yum(CommandSender sender, String pluginname) {
		return run(sender, getUrl(pluginname), new File("YumCenter", pluginname + ".jar"));
	}
}
