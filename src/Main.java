import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Savepoint;
import java.util.Date;

import javax.swing.plaf.synth.SynthDesktopIconUI;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.BufferedReader; 
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;    
import java.io.UnsupportedEncodingException;           
import java.util.ArrayList;
import java.text.DateFormat;
import org.json.*;
import org.omg.CORBA.PRIVATE_MEMBER;
import org.w3c.dom.css.ElementCSSInlineStyle;
public class Main {
	
	private static ServerSocket serverSocket;
	private static int port = 5050;
	static DateFormat mediumFormat = 
            DateFormat.getDateTimeInstance( 
                DateFormat.MEDIUM, DateFormat.MEDIUM); 
	private static Socket mainSocket;
	private static ArrayList<Players> players= new ArrayList<Players>();
	public static void main(String[] args) {
		try {
			serverSocket = new ServerSocket(port);
			while(!serverSocket.isClosed()){
				
				System.out.println(mediumFormat.format(new Date())+" Server is start");
				waitNewRequest();
			}
		} catch (Exception e) {}
	}
	private static void waitNewRequest(){
		try{
			mainSocket = serverSocket.accept();
			
			new Thread(new Runnable() {
			
				@Override
				public void run() {
					try {
						Socket branchSocket = mainSocket;
						BufferedReader br = new BufferedReader(new InputStreamReader(branchSocket.getInputStream(),"UTF8"));
						String RequestType;
						while((RequestType = br.readLine())==null) {
							//System.out.println("Get message"+RequestType);
						}
						//ObjectInputStream ois = new ObjectInputStream(branchSocket.getInputStream());
						
						
						if(RequestType.equals("SignUp")){
							System.out.println(mediumFormat.format(new Date())+" Received a sign up requestion !");
							signUpHandle(branchSocket);
						}else if(RequestType.equals("LogIn")){
							System.out.println(mediumFormat.format(new Date())+" Received a log in requestion !");
							logInHandle(branchSocket);
						}else {
							System.out.println(RequestType);
						}
					} catch (Exception e) {}	
				}
			}).start();
		}catch (Exception e) {}
	}
	
	private static void signUpHandle(Socket branchSocket){
		try {
			System.out.println(mediumFormat.format(new Date())+" Sign up handling... ");
			ObjectInputStream ois = new ObjectInputStream(branchSocket.getInputStream());
			String accountMsg; 
			while((accountMsg = ois.readObject().toString())==null) {				
			}
			JSONObject json_read = new JSONObject(accountMsg);
			
			//BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(branchSocket.getOutputStream(),"UTF8"));
			if(new Database().InsertAccount(json_read)){
				//saveImage(json_read.get("j_username").toString(),ois);
				try {
		            System.out.println(mediumFormat.format(new Date())+" Sign up success !");
		            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(branchSocket.getOutputStream(),"UTF8"));
					bw.write("success");
					bw.newLine();
					bw.flush();
				} catch (Exception e) {}
				
			} else{
				System.out.println(mediumFormat.format(new Date())+" Sign up error !");
				 BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(branchSocket.getOutputStream(),"UTF8"));
				bw.write("error");
				bw.newLine();
				bw.flush();
			}
			
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	public static void saveImage(String username,ObjectInputStream ois){
		try {
			byte[] buffer = (byte[])ois.readObject();
            FileOutputStream fos = new FileOutputStream("d:\\chat\\"+username+".png");
            fos.write(buffer);
            fos.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static void logInHandle(Socket branchSocket) {
		// TODO Auto-generated method stub
		try{
			//System.out.println(mediumFormat.format(new Date())+" Log in handling... ");
			System.out.println(mediumFormat.format(new Date())+" Log in handling... ");
			BufferedReader br = new BufferedReader(new InputStreamReader(branchSocket.getInputStream(), "UTF8"));
			
			JSONObject json_read = new JSONObject(br.readLine());
			String tmp = new Database().SelectAccount(json_read);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(branchSocket.getOutputStream(),"UTF8"));
			if(tmp.equals("error")){
				System.out.println(mediumFormat.format(new Date())+" Log in error !");
				bw.write("error");
				bw.newLine();
				bw.flush();
				return ;
			} else {
				System.out.println(mediumFormat.format(new Date())+" Log in success !");
				bw.write("success");
				bw.newLine();
				bw.flush();
			}
			bw = null;
			br = null;
			Chat(branchSocket, tmp);
		}catch (Exception e) {
			// TODO: handle exception
		}
		
	}
	
	private static void Chat(Socket branchSocket,String username ){
		
		try {
			Thread.sleep(100);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(branchSocket.getOutputStream(),"UTF8"));
			
			bw.write("Hello! Welcome to chatroon !");
			bw.newLine();
			bw.flush();
			//logInNotify(username);
			players.add(new Players(branchSocket, username));
			BufferedReader br = new BufferedReader(new InputStreamReader(branchSocket.getInputStream(), "UTF8"));
			while(branchSocket.isConnected()){
				String tmp;
				if((tmp = br.readLine())!=null){
					System.out.println(mediumFormat.format(new Date())+" "+username+": "+tmp);
					JSONObject json_write = new JSONObject();
					json_write.put("j_type","1");
					json_write.put("j_username", username);
					json_write.put("j_message",tmp);
					for(int i=0;i<players.size();i++){
						if(players.get(i).username.equals(username))
							continue;
						BufferedWriter bw2 = new BufferedWriter(new OutputStreamWriter(players.get(i).socket.getOutputStream(),"UTF8"));
						bw2.write(json_write.toString());
						bw2.newLine();
						bw2.flush();
						
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 	
	}
	private static void logInNotify(String username){
		System.out.println(mediumFormat.format(new Date())+" Send online notify !");
		JSONObject json_write = new JSONObject();
		json_write.put("j_type", "2");
		json_write.put("j_username", username);
		for(int i=0;i<players.size();i++){
			try {
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(players.get(i).socket.getOutputStream(),"UTF8"));
				bw.write(json_write.toString());
				bw.newLine();
				bw.flush();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
