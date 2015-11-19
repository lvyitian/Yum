package cn.citycraft.Yum.manager;

import java.util.List;

import org.bukkit.command.CommandSender;

import cn.citycraft.PluginHelper.utils.IOUtil;
import cn.citycraft.Yum.manager.RepoSerialization.Plugin;
import cn.citycraft.Yum.manager.RepoSerialization.TagInfo;

public class PluginInfo {
	public String branch;
	public String name;
	public Plugin plugin;
	public String pom;
	public String repo;
	public List<TagInfo> tags;
	public String url;

	public String getFileName() {
		return getFileName(plugin.version);
	}

	public String getFileName(final String version) {
		return String.format("%1$s-%2$s.jar", plugin.artifactId, version);
	}

	public String getMavenUrl(final CommandSender sender, final String version) {
		String ver = version;
		if (ver == null) {
			if (tags != null) {
				for (final TagInfo tagInfo : tags) {
					if (tagInfo.tag.equalsIgnoreCase("1.7.10")) {
						sender.sendMessage("§6版本: §b从Tag标签中获取最新版本...");
						ver = tagInfo.version;
						break;
					}
				}
			} else if (pom != null && !pom.isEmpty()) {
				pom = pom.replace("[name]", plugin.name).replace("[branch]", branch);
				sender.sendMessage("§6版本: §b尝试从在线POM文件获取最新版本...");
				ver = IOUtil.getXMLTag(pom, "version", plugin.version);
				if (ver != null) {
					sender.sendMessage("§6版本: §a成功获取到最新版本 " + ver + " ...");
				}
			}
		}
		if (ver == null) {
			ver = plugin.version;
			sender.sendMessage("§6版本: §a使用缓存的版本 " + ver + " ...");
		}
		return String.format(url + (url.endsWith("/") ? "" : "/") + "%1$s/%2$s/%3$s/%2$s-%3$s.jar",
				plugin.groupId.replace(".", "/"),
				plugin.artifactId,
				(ver == null || ver.isEmpty()) ? plugin.version : ver);
	}
}