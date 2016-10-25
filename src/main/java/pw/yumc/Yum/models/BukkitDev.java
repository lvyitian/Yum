package pw.yumc.Yum.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import pw.yumc.YumCore.kit.StrKit;

public class BukkitDev implements Serializable {
    public static String HOST = "https://api.curseforge.com";
    public static String MODULE = "/servermods";
    public static String SEARCH = HOST + MODULE + "/projects?search=%s";
    public static String PLUGIN = HOST + MODULE + "/files?projectIds=%s";

    public static class Files {
        public int projectId;
        public String name;
        public String fileUrl;
        public String fileName;
        public String downloadUrl;
        public String gameVersion;
        public String md5;
        public String releaseType;

        public Files(JSONObject obj) {
            projectId = Integer.parseInt(obj.get("projectId").toString());
            name = obj.get("name").toString();
            fileUrl = obj.get("fileUrl").toString();
            fileName = obj.get("fileName").toString();
            downloadUrl = obj.get("downloadUrl").toString();
            gameVersion = obj.get("gameVersion").toString();
            md5 = obj.get("md5").toString();
            releaseType = obj.get("releaseType").toString();
        }

        public static List<Files> parseList(String json) {
            if (StrKit.isBlank(json) || json.equals("[]")) { return Collections.emptyList(); }
            List<Files> temp = new ArrayList<>();
            JSONArray ja = (JSONArray) JSONValue.parse(json);
            for (int i = 0; i < ja.size(); i++) {
                temp.add(new Files((JSONObject) ja.get(i)));
            }
            Collections.reverse(temp);
            return temp;
        }
    }

    public static class Projects {
        public int id;
        public String name;
        public String slug;
        public String stage;

        public Projects(JSONObject obj) {
            id = Integer.parseInt(obj.get("id").toString());
            name = obj.get("name").toString();
            slug = obj.get("slug").toString();
            stage = obj.get("stage").toString();
        }

        public static List<Projects> parseList(String json) {
            if (StrKit.isBlank(json) || json.equals("[]")) { return Collections.emptyList(); }
            List<Projects> temp = new ArrayList<>();
            JSONArray ja = (JSONArray) JSONValue.parse(json);
            for (int i = 0; i < ja.size(); i++) {
                temp.add(new Projects((JSONObject) ja.get(i)));
            }
            return temp;
        }
    }
}
