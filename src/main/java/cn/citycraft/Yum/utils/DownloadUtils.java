/**
 *
 */
package cn.citycraft.Yum.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

/**
 * @author 蒋天蓓 2015年8月21日下午6:08:09 TODO
 */
public class DownloadUtils {
	Plugin plugin;

	public DownloadUtils(Plugin main) {
		this.plugin = main;
	}

	public boolean download(CommandSender sender, String pluginname) {
		String url = "http://ci.citycraft.cn:8800/jenkins/job/%1$s/lastSuccessfulBuild/artifact/target/%1$s.jar";
		BufferedInputStream in = null;
		FileOutputStream fout = null;
		if (sender == null) {
			sender = Bukkit.getConsoleSender();
		}
		try {
			String filename = pluginname + ".jar";
			sender.sendMessage("§6开始下载: §3" + pluginname);
			URL fileUrl = new URL(String.format(url, pluginname));
			sender.sendMessage("§6下载地址: §3http://********/" + filename);
			int fileLength = fileUrl.openConnection().getContentLength();
			sender.sendMessage("§6文件长度: §3" + fileLength);
			in = new BufferedInputStream(fileUrl.openStream());
			File file = new File(new File("plugins"), filename);
			if (!file.exists()) {
				file.createNewFile();
				sender.sendMessage("§d创建新文件: " + filename);
			}
			fout = new FileOutputStream(file);
			byte[] data = new byte[1024];
			long downloaded = 0L;
			int count;
			long time = System.currentTimeMillis();
			while ((count = in.read(data)) != -1) {
				downloaded += count;
				fout.write(data, 0, count);
				double percent = downloaded / fileLength * 10000;
				if (System.currentTimeMillis() - time > 1000) {
					sender.sendMessage(String.format("§a已下载: §a" + getPer(percent) + " %.2f%%", percent));
					time = System.currentTimeMillis();
				}
			}
			sender.sendMessage("§6已下载: §a====================> 100%");
			sender.sendMessage("§a插件: " + pluginname + " 下载完成!");
			return true;
		} catch (Exception ex) {
			sender.sendMessage("§c插件" + pluginname + "下载失败!");
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

	private String getPer(double per) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 10; i++) {
			if (per > i) {
				sb.append("  ");
			} else {
				sb.append("==");
			}
		}
		sb.append(">");
		return sb.toString();
	}
}
