import java.net.Socket;

public class Players {
	public Socket socket;
	public String username;
	
	Players(Socket socket,String username){
		this.socket = socket;
		this.username = username;
	}
}
