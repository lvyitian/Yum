/**
 *
 */
package cn.citycraft.Yum.manager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

/**
 * 下载管理类
 * 
 * @author 蒋天蓓
 *         2015年8月21日下午6:08:09
 */
public class DownloadManager {
	Plugin plugin;

	public DownloadManager(Plugin main) {
		this.plugin = main;
	}

	/**
	 * 从地址获得文件名称
	 * 
	 * @param url
	 *            - 地址
	 * @return 文件名称
	 */
	public String getFileName(String url) {
		int end = url.lastIndexOf('/');
		return url.substring(end + 1);
	}

	/**
	 * 从地址获得文件名称
	 * 
	 * @param url
	 *            - 地址
	 * @return 文件名称
	 */
	public String getFileName(URL url) {
		return getFileName(url.getFile());
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

	/**
	 * 从网络下载文件
	 * 
	 * @param urlstring
	 *            - 下载地址
	 * @return 是否成功
	 */
	public boolean run(String urlstring) {
		return run(null, urlstring);
	}

	/**
	 * 从网络下载文件
	 * 
	 * @param sender
	 *            - 命令发送者
	 * @param urlstring
	 *            - 下载地址
	 * @return 是否成功
	 */
	public boolean run(CommandSender sender, String urlstring) {
		return run(sender, urlstring, new File("plugins", getFileName(urlstring)));
	}

	/**
	 * 从网络下载文件
	 * 
	 * @param sender
	 *            - 命令发送者
	 * @param urlstring
	 *            - 下载地址
	 * @param file
	 *            - 保存文件
	 * @return 是否成功
	 */
	public boolean run(CommandSender sender, String urlstring, File file) {
		URL url;
		try {
			url = new URL(urlstring);
			return run(sender, url, file);
		} catch (MalformedURLException e) {
			sender.sendMessage("§4错误: §c无法识别的URL地址...");
			return false;
		}
	}

	/**
	 * 从网络下载文件
	 * 
	 * @param sender
	 *            - 命令发送者
	 * @param url
	 *            - 下载地址
	 * @param file
	 *            - 保存文件
	 * @return 是否成功
	 */
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

	/**
	 * 从网络下载文件
	 * 
	 * @param urlstring
	 *            - 下载地址
	 * @param file
	 *            - 保存文件
	 * @return 是否成功
	 */
	public boolean run(String urlstring, File file) {
		return run(null, urlstring, file);
	}

	/**
	 * 从网络下载文件
	 * 
	 * @param url
	 *            - 下载地址
	 * @param file
	 *            - 保存文件
	 * @return 是否成功
	 */
	public boolean run(URL url, File file) {
		return run(null, url, file);
	}

}
