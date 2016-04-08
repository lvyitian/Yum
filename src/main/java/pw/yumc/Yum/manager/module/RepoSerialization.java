/**
 *
 */
package pw.yumc.Yum.manager.module;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 源仓库序列化类
 *
 * @author 喵♂呜
 * @since 2015年8月31日下午7:41:53
 */
public class RepoSerialization {
    public class PackageInfo implements Serializable {
        public String name;
        public List<Plugin> plugins = new ArrayList<>();
        public String pom;
        public String url;
        public URLType type;
    }

    public class Plugin implements Serializable {
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

    public class Repositories implements Serializable {
        public String name;
        public List<Repository> repos;
    }

    public class Repository implements Serializable {
        public String id;
        public URLType type;
        public String url;
    }

    public class TagInfo implements Serializable {
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
