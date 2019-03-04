
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.Date;

import org.json.JSONObject;

public class Database {
	
	static DateFormat mediumFormat = 
            DateFormat.getDateTimeInstance( 
                DateFormat.MEDIUM, DateFormat.MEDIUM); 
	
	final static String driver = "com.mysql.jdbc.Driver";
	final static String url = "jdbc:mysql://192.168.56.1:3306/chat";
	final static String username = "root3";
	final static String password = "1234";
	
	private static Statement stat = null;
	private static Connection con = null;
	private static PreparedStatement pst = null;
	private static ResultSet rs = null;
	
	public void Connect(){
		try {
			
			Class.forName(driver); 
			System.out.println(mediumFormat.format(new Date())+" Database:Connect...");
			con = DriverManager.getConnection(url,username,password);
			System.out.println(mediumFormat.format(new Date())+" Database:Connection successful !");
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	public boolean InsertAccount(JSONObject json_read){
		Connect();
		System.out.println(mediumFormat.format(new Date())+" Database:Insert account...");
		String username = json_read.get("j_username").toString();
		String password = json_read.get("j_password").toString();
		String email = json_read.get("j_email").toString();
		boolean pass = false;
		try{
			stat = con.createStatement();
			rs = stat.executeQuery("select count(*) from account where username='"+username+"'");
			rs.next();
			if(rs.getInt(1)==0){
				pst = con.prepareStatement("insert into account(id,username,password,email) values(?,?,?,?)");
				pst.setInt(1,0);
				pst.setString(2,username);
				pst.setString(3,password);
				pst.setString(4,email);
				pst.executeUpdate();
				pass = true;
				
			} else
				pass = false;
		}catch (Exception e) {
			System.out.println(e);
			// TODO: handle exception
		}finally{
			Disconnect();
		}
		return pass;
	}
	public String SelectAccount(JSONObject json_read){
		Connect();
		System.out.println(mediumFormat.format(new Date())+" Database:Select account...");
		String username = json_read.get("j_username").toString();
		String password = json_read.get("j_password").toString();
		try{
			stat = con.createStatement();
			rs = stat.executeQuery("select * from account where username = '"+username+"'");
			while(rs.next()){
				String tmp = rs.getString("password");
				if(tmp.equals(password))
					return rs.getString("username");
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return "error";
	}
	public void Disconnect(){
		pst = null;
		stat = null;
		rs = null;
		con = null;	
	}
}
