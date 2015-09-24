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
public class Repositories {
	public class PackageInfo {
		public String name;
		public List<Plugin> plugins = new ArrayList<>();
		public String url;
	}

	public class Plugin {
		public String artifactId;
		public String description;
		public String groupId;
		public List<TagInfo> tags;
		public String version;
	}

	public class Repository {
		public String id;
		public String type;
		public String url;
	}

	public class TagInfo {
		public String tag;
		public String version;
	}
}
