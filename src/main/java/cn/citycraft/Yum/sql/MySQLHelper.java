package cn.citycraft.Yum.sql;

// import java.sql.*;

/**
 * 数据库操作类
 * 
 * @author 蒋天蓓 2015年7月14日下午3:25:06
 */
public class MySQLHelper extends SQLHelper {

	private static String drivername = "com.mysql.jdbc.Driver";

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
	public MySQLHelper(String host, String port, String dbname, String username, String password) {
		super(password, password, drivername, getUrl(host, port, dbname));
	}

	public static String getUrl(String host, String port, String dbaName) {
		String Encode = "?&useUnicode=true&characterEncoding=utf-8";
		return "jdbc:mysql://" + host + ":" + port + "/" + dbaName + Encode;
	}
}
