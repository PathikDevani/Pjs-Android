package com.v8.builtin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

import com.eclipsesource.v8.JavaCallback;
import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.v8.V8Utils;
import com.v8.V8Worker;

public class V8Mysql implements JavaCallback {
	
	public static Connection conn;
	public static Config config;
	public static ConnectionManager manager;
	public static V8 runtime;
	
	public V8Mysql() {
		runtime = V8Worker.runtime;
		config = new Config();
		manager = new ConnectionManager(config.DB, config.mysql_user, config.mysql_pass);
	}
	
	@Override
	public Object invoke(V8Object receiver, V8Array parameters) {

		String QueryStr = "";
		if (parameters.length() > 0) {
			Object arg1 = parameters.get(0).toString();
			QueryStr = arg1.toString();
			V8Utils.releasObject(arg1);
		}
		
		
		V8Object obj = new V8Object(runtime);
		
		MysqlQurey query = new MysqlQurey(QueryStr);
		obj.registerJavaMethod(query, "setString", "set", new Class<?>[]{Integer.class,String.class});
		obj.registerJavaMethod(query, "getQuery", "getQuery", new Class<?>[]{});
		obj.registerJavaMethod(query, "execute", "execute", new Class<?>[]{});
		obj.registerJavaMethod(query, "update", "update", new Class<?>[]{});
		obj.registerJavaMethod(query, "insert", "insert", new Class<?>[]{});
		
		return obj;
	}

	public class MysqlQurey {
		
		private PreparedStatement statement;
		private String qurey;
		public MysqlQurey(String qurey) {
			this.qurey = qurey;
			try {
				statement = getConnection().prepareStatement(qurey,PreparedStatement.RETURN_GENERATED_KEYS);
				statement.setEscapeProcessing(true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		public String test(){
			return qurey;
		}
		
		public void setString(Integer index,String str) throws SQLException  {
			statement.setString(index, str);
		}
		
		public Object getQuery() throws SQLException  {
			return statement.toString();
		}
		
		public Object execute() throws SQLException {
			V8Array data = new V8Array(runtime);
			ResultSet rs = statement.executeQuery();
			if(rs != null){
				try {

					int count = getRowCount(rs);
					ResultSetMetaData rsmd = rs.getMetaData();
					int callcount = rsmd.getColumnCount();
					
					ArrayList<String> cols = new ArrayList<>();
					for (int i = 1; i <= callcount; i++) {
						cols.add(rsmd.getColumnName(i));
					}
					
					for (int j = 1; j < count+1; j++) {
						rs.next();
						V8Object row = new V8Object(runtime);
						data.push(row);
						for (int i = 0; i < callcount ; i++) {
							row.add(cols.get(i),rs.getString(i+1));
						}
						row.release();						
					}
				} catch (Exception e) {
					System.out.println("execute");
					e.printStackTrace();
				} 
			}
			return data;
		}
		
		public Object update() throws SQLException {
			return statement.executeUpdate() + ""; 
		}
		
		public Object insert() throws SQLException { 
			int generatedkey = -1; 
			statement.executeUpdate();
			ResultSet rs = statement.getGeneratedKeys();
			if (rs.next()) {
				generatedkey = rs.getInt(1);
			} 
			return generatedkey; 
		}
		
		public Connection getConnection(){
			if(!manager.checkConnectionAlive(conn)){
				conn = manager.getConnection();
			}
			return conn;
		}
		
		public int getRowCount(ResultSet set) throws SQLException{
		    int rowCount;
		    int currentRow = set.getRow();
		    rowCount = set.last() ? set.getRow() : 0;    
		    if (currentRow == 0){
		    	set.beforeFirst();                    		    	
		    }else{
		    	set.absolute(currentRow);             		    	
		    }
		    return rowCount;
		}
	}
	
	
	public class Config{
		public String mysql_user = "root";
		public String mysql_pass = "";
		public String DB = "texttile";
		 
		public Config() {
			if(System.getProperty("os.name").contains("Windows")){
				mysql_user = "root";
				mysql_pass = "";
			} else {  
				mysql_user = "root";
				mysql_pass = "semderjsxs";
			} 
		}
	}
	
	public class ConnectionManager{
		public String mysql_user = "root";
		public String mysql_pass = "";
		public String DB;
		
		public ConnectionManager(String db,String user,String pass) {
			this.DB = db;
			this.mysql_user = user;
			this.mysql_pass = pass;
		}		
		
		public Connection getConnection(){
			try {
				return DriverManager.getConnection( "jdbc:mysql://localhost:3306/"+DB, mysql_user,mysql_pass);
			} catch (SQLException e) {
				System.out.println("getConnection");
				e.printStackTrace();
				return null;
			}
		}
		
		public boolean checkConnectionAlive(Connection conn){
			try {
				if(conn == null || conn.isClosed()){
					return false;
				}
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
	}
}
