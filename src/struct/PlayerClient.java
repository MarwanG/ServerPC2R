package struct;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.Socket;

import tools.Tools;

public class PlayerClient extends Thread {

		BufferedReader inchan;
	DataOutputStream outchan;
	Socket s;
	String name;
	Player p;
	int score;
	TypeJouer type;
	Server serv;
	int id;
	boolean guessed;
	boolean connected;
	
	public PlayerClient(int id,Socket s,Server serv){
		System.out.println("my id is = " + id);	
		this.id = id;
		this.s = s;
		this.serv = serv;
		this.connected = false;
		try {
			inchan = new BufferedReader(new InputStreamReader(s.getInputStream()));
			outchan = new DataOutputStream(s.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	//init function called when user actually connects. cmd: CONNECT/
	private void init(String s){
		this.name = s;
		name.replace("/", "");
		this.type = TypeJouer.guesser;
		this.score = 0;
		guessed = false;
		serv.addPlayer(this);
		connected = true;
	}
	
	//fonction run of the thread.
	public void run(){
		try {
			while (true) {
					String command = inchan.readLine();
					if(command == null){
						break;
					}
					if(command.endsWith("/")){
						if(command.contains("EXIT/"+name)) { 
							break;
						}else if(command.contains("REGISTER/") && !connected){
							if(!register(command.split("/")[1],command.split("/")[2])){
								break;
							}
						}else if(command.contains("LOGIN/") && !connected){
							if(!login(command.split("/")[1],command.split("/")[2])){
								break;
							}
						}else if(command.contains("CONNECT/") && !connected){
							init(command.split("/")[1]);
							serv.printToAll("CONNECTED/"+name+"/ \n");
						}else if(command.contains("CHEAT/") && type==TypeJouer.guesser){
							serv.cheating();
						}else if(command.contains("GUESS/") && type==TypeJouer.guesser && !guessed){
								String word = command.split("/")[1];
								guess(word);						
						}else if(command.contains("SET_COLOR/") && type==TypeJouer.drawer){
							serv.printToGuessers(command);
						}else if(command.contains("SET_SIZE/") && type==TypeJouer.drawer){
							serv.printToGuessers(command);
						}else if(command.contains("SET_LINE/") && type==TypeJouer.drawer){
							serv.printToGuessers(command);
						}else if(command.contains("LINE/") && type==TypeJouer.drawer){
							serv.printToGuessers(command);
						}else if(command.contains("TALK/")){
							serv.printToExcept("LISTEN/"+name+"/"+command.split("/")[1]+"/ \n", id);
						}
					}
				}
			s.close();
			if(connected)
				serv.removePlayer(id);
		}catch(IOException e){ 
			e.printStackTrace(); 
			System.exit(1);
		} 
	}
	
	//function for print to DataOutputStream
	public void printToStream(String s){
		try {
			synchronized(outchan){
				outchan.writeChars(s);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//function to disconnect properly
	public void disconnect(){
		try {
			inchan.close(); //NOT SURE IF IMPORTANT OR NOT
			s.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private boolean register(String name,String password){
		if(!Tools.nameExists(name)){
			init(name);
			p = new Player(name,password);
			Tools.addPlayer(p);
			serv.printToAll("CONNECTED/"+name+"/ \n");
			return true;
		}else{
			printToStream("ACCESSDENIED/ \n");
			return false;
		}
	}
	
	private boolean login(String name,String password){
		Player tmp = Tools.playerExists(name, password);
		if(tmp == null){
			printToStream("ACCESSDENIED/ \n");
			return false;
		}else{
			p = tmp;
			init(name);
			serv.printToAll("CONNECTED/"+name+"/ \n");
			return true;
		}
	}
	
	private void guess(String word){
		if(serv.correctWord(word,this)){
			printToStream("WORD_FOUND/"+name+"/"+word+"\n");
			serv.printToExcept("WORD_FOUND/"+name+"/ \n", id);
			guessed = true;
		}else{
			serv.printToExcept("GUESSED/"+word+"/"+name+"/ \n", id);
		}
	}
	
	//SETTERS AND GETTERS
	public void setType(TypeJouer type){
		this.type = type;
	}
	
	public TypeJouer getType(){
		return type;
	}
	
	
	public void setNom(String s){
		this.name=s;
	}
	
	public String getNom(){
		return this.name;
	}
		
	public int getPlayerId(){
		return id;
	}
	
	
	public int getScore(){
		return score;
	}
	
	public void addToScore(int n){
		score = score+n;
	}
	
	public String toString(){
		return "id = "+id + " name = " + this.name;
	}	
}

