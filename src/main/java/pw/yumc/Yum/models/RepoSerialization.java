/**
 *
 */
package pw.yumc.Yum.models;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * 源仓库序列化类
 *
 * @author 喵♂呜
 * @since 2015年8月31日下午7:41:53
 */
public class RepoSerialization {
    @SuppressWarnings("unchecked")
    public static <E> List<E> parse(String json, Class<?> clazz) {
        if (json == null || "null".equals(json) || json.isEmpty()) { return null; }
        List<E> temp = new ArrayList<>();
        JSONArray ja = (JSONArray) JSONValue.parse(json);
        for (int i = 0; i < ja.size(); i++) {
            try {
                temp.add((E) clazz.getConstructor(JSONObject.class).newInstance((JSONObject) ja.get(i)));
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                e.printStackTrace();
            }
        }
        return temp;
    }

    public static class PackageInfo implements Serializable {
        public String name;
        public List<Plugin> plugins = new ArrayList<>();
        public String pom;
        public String url;
        public URLType type;

        public PackageInfo(JSONObject obj) {
            name = String.valueOf(obj.get("name"));
            plugins = Plugin.parseList(String.valueOf(obj.get("plugins")));
            pom = String.valueOf(obj.get("pom"));
            url = String.valueOf(obj.get("url"));
            Object tt = obj.get("type");
            type = tt == null ? null : URLType.valueOf(tt.toString());
        }
    }

    public static class Plugin implements Serializable {
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

        public Plugin(JSONObject obj) {
            artifactId = String.valueOf(obj.get("artifactId"));
            branch = String.valueOf(obj.get("branch"));
            description = String.valueOf(obj.get("description"));
            groupId = String.valueOf(obj.get("groupId"));
            name = String.valueOf(obj.get("name"));
            url = String.valueOf(obj.get("url"));
            pom = String.valueOf(obj.get("pom"));
            tags = TagInfo.parseList(String.valueOf(obj.get("tags")));
            version = String.valueOf(obj.get("version"));
            Object tt = obj.get("type");
            type = tt == null ? null : URLType.valueOf(tt.toString());
        }

        public static List<Plugin> parseList(String json) {
            return parse(json, Plugin.class);
        }
    }

    public static class Repositories implements Serializable {
        public String name;
        public List<Repository> repos;

        public Repositories(JSONObject obj) {
            name = String.valueOf(obj.get("name"));
            repos = Repository.parseList(String.valueOf(obj.get("repos")));
        }
    }

    public static class Repository implements Serializable {
        public String id;
        public URLType type;
        public String url;

        public Repository(JSONObject obj) {
            id = String.valueOf(obj.get("id"));
            Object tt = obj.get("type");
            type = tt == null ? null : URLType.valueOf(tt.toString());
            url = String.valueOf(obj.get("url"));
        }

        public static List<Repository> parseList(String json) {
            return parse(json, Repository.class);
        }
    }

    public static class TagInfo implements Serializable {
        public String tag;
        public String version;
        public URLType type;
        public String url;

        public TagInfo(JSONObject obj) {
            tag = String.valueOf(obj.get("tag"));
            version = String.valueOf(obj.get("version"));
            Object tt = obj.get("type");
            type = tt == null ? null : URLType.valueOf(tt.toString());
            url = String.valueOf(obj.get("url"));
        }

        public static List<TagInfo> parseList(String json) {
            return parse(json, TagInfo.class);
        }
    }

    public enum URLType {
        Maven,
        maven,
        DirectUrl;
    }
}
