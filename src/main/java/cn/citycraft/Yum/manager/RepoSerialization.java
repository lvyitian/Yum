/**
 *
 */
package cn.citycraft.Yum.manager;

import java.util.ArrayList;
import java.util.List;

/**
 * 源仓库序列化类
 *
 * @author 蒋天蓓
 *         2015年8月31日下午7:41:53
 */
public class RepoSerialization {
	public class PackageInfo {
		public String name;
		public List<Plugin> plugins = new ArrayList<>();
		public String pom;
		public String url;
		public URLType type;
	}

	public class Plugin {
		public String artifactId;
		public String branch;
		public String description;
		public String groupId;
		public String name;
		public String url;
		public String pom;
		public List<TagInfo> tags;
		public String version;
		public URLType type;
	}

	public class Repositories {
		public String name;
		public List<Repository> repos;
	}

	public class Repository {
		public String id;
		public URLType type;
		public String url;
	}

	public class TagInfo {
		public String tag;
		public String version;
		public URLType type;
		public String url;
	}

	public enum URLType {
		Maven,
		DirectUrl;
	}
}
