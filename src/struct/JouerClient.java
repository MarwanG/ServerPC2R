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
	
	private void init(String s){
		this.name = s;
		name.replace("/", "");
		this.type = TypeJouer.guesser;
		this.score = 0;
		serv.addPlayer(this);
	}
	
	public void setNom(String s){
		this.name=s;
	}
	
	public int getPlayerId(){
		return id;
	}
	
	public void run(){
		try {
			while (true) {
				String command = inchan.readLine();
				if(command.equals("EXIT/"+name+"/")) { 
					//TODO if is the drawer;
					outchan.writeChars("Déconnexion de \""+name+"\" \n");
					serv.printToExcept("EXITED/"+name+"/\n", id);
					break;
					} 
				if(command.contains("CONNECT/")){
					init(command.split("/")[1]);
					outchan.writeChars("Nouvelle connexion de \""+ name +"\" \n");
					serv.printToExcept("CONNECTED/"+name+"/\n",id);
				}
				if(command.contains("GUESS/")){
					String tmp = command.split("/")[1];
					if(serv.correctWord(tmp)){
						outchan.writeChars("WORD_FOUND/"+name+"/"+tmp+"\n");
						serv.printToExcept("WORD_FOUND/"+name+"/ \n", id);
						serv.add1();
						serv.addMeToFound(this);
						synchronized(serv.obj){
							serv.obj.notify();
						}
					}else{
						serv.printToExcept("GUESSED/"+tmp+"/"+name+"/ \n", id);
					}
				}else{
					serv.printToExcept(name +" : "+command + "\n",id);
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
			synchronized(outchan){
				outchan.writeChars(s);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void setType(TypeJouer type){
		this.type = type;
	}
	
	public TypeJouer getType(){
		return type;
	}
	
	public String getNom(){return this.name;}
	
	public void addToScore(int n){score = score+n;}
	
	public int getScore(){return score;}
}


