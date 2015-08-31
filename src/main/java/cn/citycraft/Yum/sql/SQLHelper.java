package cn.citycraft.Yum.sql;

/*
 * 数据库连接、选择、更新、删除演示
 */
// import java.sql.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author 蒋天蓓 2015年7月14日下午3:25:06 数据库操作类
 */
public abstract class SQLHelper {

	// ///////////////////////////////////////———–>>>数据成员 and 构造函数
	protected Connection dbconn = null;
	protected Statement dbstate = null;
	protected ResultSet dbresult = null;

	protected String username;
	protected String password;
	protected String url;

	protected String drivername;

	/**
	 * 初始化连接信息
	 * 
	 * @param username
	 *            - 用户名
	 * @param password
	 *            - 密码
	 * @param drivername
	 *            - 驱动名称
	 * @param url
	 *            - 数据库地址
	 */
	public SQLHelper(String username, String password, String drivername, String url) {
		try {
			Class.forName(drivername).newInstance();
		} catch (Exception e) {
		}
		this.username = username;
		this.password = password;
		this.drivername = drivername;
		this.url = url;
	}

	/**
	 * 创建数据表
	 *
	 * @param tableName
	 *            - 表名
	 * @param fields
	 *            - 字段参数
	 * @param Conditions
	 *            -附加值
	 * @return 运行结果
	 */
	public boolean createTables(String tableName, HashMap<String, String> fields, String Conditions) {
		if (!dbConnection())
			return false;
		String kv = "";
		for (Entry<String, String> kvs : fields.entrySet()) {
			kv += "`" + kvs.getKey() + "` " + kvs.getValue() + " NOT NULL , ";
		}
		kv = kv.substring(0, kv.length() - 2);// 根据String的索引提取子串
		String sql = "CREATE TABLE `" + tableName + "` ( " + kv + (Conditions == "" ? "" : " , " + Conditions)
				+ " ) ENGINE = InnoDB DEFAULT CHARSET=UTF8";
		try {
			PreparedStatement state = dbconn.prepareStatement(sql);
			state.executeUpdate();
			state.close();
			return true;
		} catch (final Exception e) {
			print("数据库操作出错: " + e.getMessage());
			print("SQL查询语句: " + sql);
		}
		return false;
	}

	/**
	 * 断开数据库
	 *
	 * @return bool值，成功返回true，失败返回false
	 */
	public boolean dbClose() {
		try {
			dbconn.close();
			return true;
		} catch (Exception e) {
			print("数据库操作出错: " + e.getMessage());
			return false;
		}
	}// end dbClose()

	/**
	 * 连接到数据库
	 *
	 * @return 是否成功
	 */
	public boolean dbConnection() {
		try {
			dbconn = DriverManager.getConnection(url, username, password);
			dbstate = dbconn.createStatement();
			return true;
		} catch (Exception e) {
			print("数据库操作出错: " + e.getMessage());// 得到出错信息
			print("登录URL: " + url); // 发生错误时，将连接数据库信息打印出来
			print("登录账户: " + username);
			print("登录密码: " + password);
			return false;
		}
	}

	/**
	 * 对数据库表中的记录进行删除操作
	 *
	 * @param tableName
	 * @param condition
	 * @return bool值，表示删除成功或者失败。
	 */
	public boolean dbDelete(String tableName, HashMap<String, String> selConditions) {// ——–>>>删除操作
		if (!dbConnection())
			return false;
		String selCondition = "";
		if (selConditions != null && !selConditions.isEmpty()) {
			for (Entry<String, String> kvs : selConditions.entrySet()) {
				selCondition += kvs.getKey() + "='" + kvs.getValue() + "', ";
			}
			selCondition = " WHERE " + selCondition.substring(0, selCondition.length() - 2);// 根据String的索引提取子串
		}
		String sql = "DELETE FROM `" + tableName + "` " + selCondition;
		try {
			dbstate.executeUpdate(sql);
			return true;
		} catch (Exception e) {
			print("数据库操作出错: " + e.getMessage());
			print("SQL查询语句: " + sql);
			return false;
		}
	}// end dbDelete(…)

	/**
	 * 判断数据库某个值是否存在!
	 *
	 * @param tableName
	 *            数据库表名
	 * @param fieles
	 *            字段名
	 * @param selCondition
	 *            选择条件
	 * @return 首个符合条件的结果
	 */
	public boolean dbExist(String tableName, HashMap<String, String> selConditions) {
		if (!dbConnection())
			return false;
		String selCondition = "";
		if (selConditions != null && !selConditions.isEmpty()) {
			for (Entry<String, String> kvs : selConditions.entrySet()) {
				selCondition += kvs.getKey() + "='" + kvs.getValue() + "', ";
			}
			selCondition = " WHERE " + selCondition.substring(0, selCondition.length() - 2);// 根据String的索引提取子串
		}
		String sql = "SELECT * FROM " + tableName + selCondition;
		try {
			dbresult = dbstate.executeQuery(sql);
			return dbresult.next();
		} catch (Exception e) {
			print("数据库操作出错: " + e.getMessage());
			print("SQL查询语句: " + sql);
		}
		return false;
	}

	/**
	 * 对数据库表进行插入操作
	 *
	 * @param tabName
	 *            - 表名
	 * @param values
	 *            - 带键值的HashMap
	 * @return bool值，成功返回true，失败返回false
	 */
	public boolean dbInsert(String tabName, HashMap<String, String> values) {
		if (!dbConnection())
			return false;
		String sql = "";
		String insertFields = "";
		String insertValues = "";
		for (Entry<String, String> kvs : values.entrySet()) {
			insertFields += "`" + kvs.getKey() + "`, ";
			insertValues += "'" + kvs.getValue() + "', ";
		}
		insertFields = insertFields.substring(0, insertFields.length() - 2);
		insertValues = insertValues.substring(0, insertValues.length() - 2);
		sql += "INSERT INTO `" + tabName + "` (" + insertFields + ") VALUES" + "(" + insertValues + ")";
		try {
			dbstate.executeUpdate(sql);
			return true;
		} catch (Exception e) {
			print("数据库操作出错: " + e.getMessage());
			print("SQL查询语句: " + sql);
			return false;
		}

	}// end dbInsert(…)

	/**
	 * 对数据库表进行选择操作！
	 *
	 * @param tableName
	 *            数据库表名
	 * @param fieles
	 *            字段名
	 * @param selCondition
	 *            选择条件
	 * @return 一个含有map的List（列表）
	 */
	@SuppressWarnings({
			"rawtypes",
			"unchecked"
	})
	public List dbSelect(String tableName, List<String> fields, String selCondition) {
		if (!dbConnection())
			return null;
		List mapInList = new ArrayList();
		String selFields = "";
		for (int i = 0; i < fields.size(); ++i) {
			selFields += fields.get(i) + ", ";
		}
		String selFieldsTem = selFields.substring(0, selFields.length() - 2);// 根据String的索引提取子串
		String sql = "SELECT " + selFieldsTem + " FROM `" + tableName + "`" + selCondition == "" ? "" : " WHERE " + selCondition;
		try {
			dbstate = dbconn.createStatement();
			try {
				dbresult = dbstate.executeQuery(sql);
			} catch (Exception e) {
				print("数据库操作出错: " + e.getMessage());
				print("SQL查询语句: " + sql);
			}
			while (dbresult.next()) {
				Map selResult = new HashMap();
				for (String col : fields) {
					selResult.put(col, dbresult.getString(col));
				}
				mapInList.add(selResult);
			}
		} catch (Exception e) {
			print("数据库操作出错: " + e.getMessage());
			print("SQL查询语句: " + sql);
		}
		return mapInList;
	}// end String dbSelect(…)

	/**
	 * 对数据库表进行选择操作！
	 *
	 * @param tableName
	 *            数据库表名
	 * @param fieles
	 *            字段名
	 * @param selCondition
	 *            选择条件
	 * @return 首个符合条件的结果
	 */
	public String dbSelectFirst(String tableName, String fields, HashMap<String, String> selConditions) {
		if (!dbConnection())
			return null;
		String selFieldsTem = fields;
		String selCondition = "";
		if (selConditions != null && !selConditions.isEmpty()) {
			for (Entry<String, String> kvs : selConditions.entrySet()) {
				selCondition += kvs.getKey() + "='" + kvs.getValue() + "', ";
			}
			selCondition = " WHERE " + selCondition.substring(0, selCondition.length() - 2);// 根据String的索引提取子串
		}
		String sql = "SELECT " + selFieldsTem + " FROM " + tableName + selCondition + " limit 1";
		try {
			dbresult = dbstate.executeQuery(sql);
			if (dbresult.next())
				return dbresult.getString(fields);
		} catch (Exception e) {
			print("数据库操作出错: " + e.getMessage());
			print("SQL查询语句: " + sql);
		}
		return null;
	}

	/**
	 * 对数据库表中记录进行更新操作
	 *
	 * @param tabName
	 * @param reCount
	 * @return bool值，成功返回true，失败返回false
	 */
	@SuppressWarnings({
		"rawtypes"
	})
	public boolean dbUpdate(String tabName, HashMap reCount, String upCondition) {
		if (!dbConnection())
			return false;
		String Values = "";
		Iterator keyValues = reCount.entrySet().iterator();
		for (int i = 0; i < reCount.size(); ++i) {
			Map.Entry entry = (Map.Entry) keyValues.next();
			Object key = entry.getKey();
			Object value = entry.getValue();
			Values += key + "='" + value + "'" + ", ";
		}
		String updateValues = Values.substring(0, Values.length() - 2);
		String sql = "UPDATE `" + tabName + "` SET " + updateValues + " " + upCondition;
		try {
			dbstate.executeUpdate(sql);
			return true;
		} catch (Exception e) {
			print("数据库操作出错: " + e.getMessage());
			print("SQL查询语句: " + sql);
			return false;
		}
	}// end dbUpdate(…)

	/**
	 * 判断数据表是否存在
	 *
	 * @param table
	 *            - 表名
	 * @return 是否存在
	 */
	public boolean isTableExists(final String table) {
		try {
			if (!dbConnection())
				return false;
			final DatabaseMetaData dbm = dbconn.getMetaData();
			final ResultSet tables = dbm.getTables(null, null, table, null);
			return tables.next();
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void print(String str)// 简化输出
	{
		System.out.println(str);
	}

	/**
	 * 运行SQL语句
	 *
	 * @param sql
	 *            - SQL语句
	 * @return 运行结果
	 */
	public boolean runSql(String sql) {
		if (!dbConnection())
			return false;
		try {
			PreparedStatement state = dbconn.prepareStatement(sql);
			state.executeUpdate();
			state.close();
			return true;
		} catch (final Exception e) {
			print("数据库操作出错: " + e.getMessage());
			print("SQL语句: " + sql);
		}
		return false;
	}

	/**
	 * 运行SQL文件
	 *
	 * @param sql
	 *            - SQL文件
	 * @return 运行结果
	 */
	public boolean runSqlfile(File file) {
		BufferedReader br = null;
		Statement state = null;
		String sql = null;
		if (!file.exists())
			return false;

		if (!dbConnection())
			return false;

		try {
			print("执行SQL文件: " + file.getName() + " ...");
			br = new BufferedReader(new FileReader(file));
			state = dbconn.createStatement();
			while ((sql = br.readLine()) != null) {
				if (sql != "") {
					try {
						state.executeUpdate(sql);
					} catch (Exception e) {
						print("数据库操作出错: " + e.getMessage());
						print("SQL语句: " + sql);
					}
				}
			}
			return true;
		} catch (Exception e) {
			print("执行SQL文件 " + file.getName() + "出错: " + e.getMessage());
			return false;
		} finally {
			try {
				state.close();
				br.close();
			} catch (Exception e) {
			}
		}
	}

}
