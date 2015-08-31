package cn.citycraft.Yum.repository;

/**
 * TODO
 * 
 * @author 蒋天蓓
 *         2015年8月31日下午7:43:19
 */
public class PluginInfo {
	public Plugin plugin;
	public String url;

	public String getMavenUrl() {
		return String.format(url + "/%1$s/%2$s/%3$s/%2$s-%3$s.jar", plugin.groupId.replace(".", "/"), plugin.artifactId, plugin.version);
	}

	public String getFileName() {
		return String.format("%1$s-%2$s.jar", plugin.artifactId, plugin.version);
	}
}
