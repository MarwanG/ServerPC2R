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
	boolean connected;
	
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
	}
	
	public JouerClient(int id,Socket s,Server serv){
		System.out.println("my id is = " + id);	
		this.id = id;
		this.s = s;
		this.serv = serv;
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
		connected = true;
		serv.addPlayer(this);
	}
	
	//fonction run of the thread.
	public void run(){
		try {
			while (true) {
					String command = inchan.readLine();
					if(command.equals("EXIT") && name==null){
						break;
					}
					if(command.equals("EXIT/"+name+"/")) { 
						serv.printToExcept("EXITED/"+name+"/\n", id);
						break;
					} 
					if(command.contains("CONNECT/")){
						init(command.split("/")[1]);
						serv.printToExcept("CONNECTED/"+name+"/\n",id);
					}
					else if(command.contains("GUESS/") && type==TypeJouer.guesser){
						String tmp = command.split("/")[1];
						if(serv.correctWord(tmp,this)){
							printToStream("WORD_FOUND/"+name+"/"+tmp+"\n");
							serv.printToExcept("WORD_FOUND/"+name+"/ \n", id);
						}else{
							serv.printToExcept("GUESSED/"+tmp+"/"+name+"/ \n", id);
						}
					}else if(command.contains("CHEAT/") && type==TypeJouer.guesser){
						serv.cheating();
					}else if(command.contains("SET_COLOR/") && type==TypeJouer.drawer){
						serv.printToGuessers(command);
					}else if(command.contains("SET_SIZE/") && type==TypeJouer.drawer){
						serv.printToGuessers(command);
					}else if(command.contains("SET_LINE/") && type==TypeJouer.drawer){
						serv.printToGuessers(command);
					}else if(command.contains("LINE/") && type==TypeJouer.drawer){
						serv.printToGuessers(command);
					}else{
						System.out.println("chat shit");
						System.out.println(command);
						serv.printToExcept(name +" : "+command + "\n",id);
					}
			}
			System.out.println("arrived here");
			s.close();
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
	
	
	
	public void addToScore(int n){
		score = score+n;
	}
	
	
	public void disconnect(){
		try {
			s.close();
		}catch (IOException e) {
			e.printStackTrace();
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
	
	public boolean getConnect(){
		return connected;
	}
	
	public int getScore(){
		return score;
	}
	
}


