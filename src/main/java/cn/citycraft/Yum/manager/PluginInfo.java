package cn.citycraft.Yum.manager;

import java.util.List;

import cn.citycraft.Yum.manager.Repositories.Plugin;
import cn.citycraft.Yum.manager.Repositories.TagInfo;

public class PluginInfo {
	public Plugin plugin;
	public String url;
	public String repo;
	public List<TagInfo> tags;

	public String getFileName() {
		return String.format("%1$s-%2$s.jar", plugin.artifactId, plugin.version);
	}

	public String getMavenUrl() {
		return getMavenUrl(null);
	}

	public String getMavenUrl(String version) {
		String ver = version;
		if (ver == null && tags != null)
			for (TagInfo tagInfo : tags)
				if (tagInfo.tag.equalsIgnoreCase("1.7.10")) {
					ver = tagInfo.version;
					break;
				}
		return String.format(url + (url.endsWith("/") ? "" : "/") + "%1$s/%2$s/%3$s/%2$s-%3$s.jar", plugin.groupId.replace(".", "/"), plugin.artifactId, ver == null ? plugin.version : ver);
	}
}