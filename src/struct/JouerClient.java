package struct;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class JouerClient extends Thread {

	BufferedReader inchan;
	DataOutputStream outchan;
	Socket s;
	String name;
	int score;
	TypeJouer type;
	Server serv;
	int id;
	
	public JouerClient(String name , Socket s,TypeJouer type){
		this.name = name;
		this.s = s;
		this.type = type;
		try {
			inchan = new BufferedReader(new InputStreamReader(s.getInputStream()));
			outchan = new DataOutputStream(s.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		} 
		serv.addPlayer(this);
	}
	
	public JouerClient(int id,Socket s,Server serv){
		this.id = id;
		this.s = s;
		this.serv = serv;
		try {
			inchan = new BufferedReader(new InputStreamReader(s.getInputStream()));
			outchan = new DataOutputStream(s.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		} 
		serv.addPlayer(this);
	}
	
	private void init(String s){
		this.name = s;
		name.replace("/", "");
		this.type = TypeJouer.guesser;
		this.score = 0;
	}
	
	public void setNom(String s){
		this.name=s;
	}
	
	public int getInt(){
		return id;
	}
	
	public void run(){
		try {
			while (true) {
				String command = inchan.readLine();
				if(command.equals("EXIT/"+name+"/")) { 
					//TODO if is the drawer;
					outchan.writeChars("Déconnexion de \""+name+"\" \n");
					serv.printToAll("EXITED/"+name+"/\n", id);
					break;
					} 
				if(command.contains("CONNECT/")){
					init(command.split("/")[1]);
					outchan.writeChars("Nouvelle connexion de \""+ name +"\" \n");
					serv.printToAll("CONNECTED/"+name+"/\n",id);
				}else{
					serv.printToAll(name +" : "+command + "\n",id);
				}
			}
			s.close();
			serv.removePlayer(id);
		}catch(IOException e) { 
			e.printStackTrace(); 
			System.exit(1);
			} 
	}
	
	public void printToStream(String s){
		try {
			outchan.writeChars(s);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}


