package cn.citycraft.Yum.sql;

// import java.sql.*;

/**
 * 数据库操作类
 * 
 * @author 蒋天蓓 2015年7月14日下午3:25:06
 */
public class SQLiteHelper extends SQLHelper {

	private static String drivername = "org.sqlite.JDBC";

	/**
	 * 初始化连接信息
	 * 
	 * @param host
	 *            - 域名
	 * @param port
	 *            - 端口
	 * @param dbname
	 *            - 数据库
	 * @param username
	 *            - 用户名
	 * @param password
	 *            - 密码
	 */
	public SQLiteHelper(String filepath, String username, String password) {
		super(password, password, drivername, getUrl(filepath));
	}

	public static String getUrl(String filepath) {
		return "jdbc:sqlite:" + filepath;
	}
	//
	// Class.forName("org.sqlite.JDBC");
	// Connection conn =
	// DriverManager.getConnection("jdbc:sqlite:filename");//filename为你的SQLite数据名称
}
