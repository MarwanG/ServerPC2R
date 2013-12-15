package struct;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import tools.Config;
import tools.Profiles;
import tools.Tools;


/**
 * 
 * @author marwanghanem
 *
 */
public class PlayerClient extends Thread {

	BufferedReader inchan;
	//DataOutputStream outchan;
	PrintWriter outchan;
	Socket s;
	String name;
	Player p;
	int score;
	TypeJouer type;
	Server serv;
	int id;
	boolean guessed;
	boolean connected;
	Game game;
	
	public PlayerClient(int id,Socket s,Server serv){
		this.id = id;
		this.s = s;
		this.serv = serv;
		this.connected = false;
		try {
			inchan = new BufferedReader(new InputStreamReader(s.getInputStream()));
			//outchan = new DataOutputStream(s.getOutputStream());
			outchan = new PrintWriter(s.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	/**
	 * Method to init the class with the name normally used after 
	 * a command CONNECT/ has been recived.
	 * @param s
	 */
	private void init(String s){
		this.name = s;
		name.replace("/", "");
		this.type = TypeJouer.guesser;
		this.score = 0;
		guessed = false;
		serv.addPlayer(this);
		connected = true;
		this.printToStream("WELCOME/"+name+"/\n");
	}
	
	private void initSpec(String s){
		this.name = s;
		this.type = TypeJouer.spec;
		connected = true;
		this.printToStream("WELCOME/"+name+"/ \n");
		serv.AddObs(this);
	}
	/**
	 * Method run for the thread
	 */
	public void run(){
		try {
			while (true) {
					String command = inchan.readLine();
					if(command == null){
						if(connected)
							serv.removePlayer(id);
						else
							disconnect();
						break;
					}
					if(command.endsWith("/")){
						if(command.contains("EXIT/"+name)) { 
							if(connected && this.type != TypeJouer.spec)
								serv.removePlayer(id);
							else if(connected && this.type == TypeJouer.spec)
								serv.removeObs(id);
							else
								disconnect();
							break;
						}else if(command.contains("REGISTER/") && !connected && command.split("/").length > 2){
							if(serv.getNbConnected() > serv.getCapacity()){
								printToStream("ACCESSDENIED/ \n");
								break;
							}
							if(!register(command.split("/")[1],command.split("/")[2])){
								break;
							}
						}else if(command.contains("LOGIN/") && !connected  && command.split("/").length > 2){
							if(serv.getNbConnected() > serv.getCapacity()){
								printToStream("ACCESSDENIED/ \n");
								break;
							}
							if(!login(command.split("/")[1],command.split("/")[2])){
								break;
							}
						}else if(command.contains("CONNECT/") && !connected){
							if(Profiles.nameExists(command.split("/")[1]) || serv.NameConnected(command.split("/")[1])){
								printToStream("ACCESSDENIED/ \n");
								break;
							}else{
								if(serv.getNbConnected() > serv.getCapacity()){
									printToStream("ACCESSDENIED/ \n");
									break;
								}
								init(command.split("/")[1]);
								serv.printToExcept("CONNECTED/"+name+"/ \n", id);
							}
						}else if(command.contains("CHEAT/") && type==TypeJouer.guesser && command.split("/").length > 1){
								if(command.split("/")[1].equals(game.drawer.getNom()))
									game.cheating();
						}else if(command.contains("GUESS/") && type==TypeJouer.guesser && !guessed){
								String word = command.split("/")[1];
								guess(word);						
						}else if(command.contains("SET_COLOR/") && type==TypeJouer.drawer && command.split("/").length >3){
								Tools.changeColor(command);
						}else if(command.contains("SET_SIZE/") && type==TypeJouer.drawer && command.split("/").length > 1){
								Config.size = Integer.valueOf(command.split("/")[1]);
						}else if(command.contains("SET_LINE/") && type==TypeJouer.drawer && command.split("/").length > 4){
								sendDrawing(command);
						}else if(command.contains("TALK/")){
							serv.printToAll("LISTEN/"+name+"/"+command.split("/")[1]+"/ \n");
						}else if(command.contains("COURBE/") && type==TypeJouer.drawer && command.split("/").length > 8){
							sendDrawingCourbe(command);
						}else if(command.contains("PASS/") && type==TypeJouer.drawer){
							serv.notifyObj();
						}else if(command.contains("SPECTATOR/")  && !connected ){
							if(!serv.NameConnected(command.split("/")[1])){
								initSpec(command.split("/")[1]);
								serv.printToExcept("CONNECTED/"+name+"/ \n", id);
							}else{
								printToStream("ACCESSDENIED/ \n");
								break;
							}
						}else{
							System.out.println("command unknown will be ignored");
						}
					}
				}
				disconnect();
		}catch(IOException e){ 
			//e.printStackTrace(); 
			System.exit(1);
		} 
	}

	/**
	 * Method to print stream to the client.
	 * uses writeUTF instead of writeChars to be compatible with C programs
	 * only difference the first 2 bytes are the number of bytes to be transmitted.
	 * A 0.25 second delay is added to make sure no loss of data while client is 
	 * reciving. since server has no actual control on the network-window of client.
	 * @param s
	 */
	public void printToStream(String s){
	
		try {
			Thread.sleep(50);
			synchronized(outchan){
				//outchan.writeChars(s);
				//outchan.writeUTF(s);
				outchan.print(s);
				outchan.flush();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Method to disconnect the client properly.
	 */
	public void disconnect(){
		try {
			s.shutdownInput();
			s.shutdownOutput();
			
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Method to register a player with the name and password passed.
	 * @param name
	 * @param password
	 * @return returns true if the register process has been done correctly.
	 */
	private boolean register(String name,String password){
		if(!Profiles.nameExists(name)){
			init(name);
			p = new Player(name,password);
			Profiles.addPlayer(p);
			serv.printToAll("CONNECTED/"+name+"/ \n");
			return true;
		}else{
			printToStream("ACCESSDENIED/ \n");
			return false;
		}
	}
	
	/**
	 * Method responsible for the login a client
	 * with name and password.
	 * @param name
	 * @param password
	 * @return true if the user actually manages to login.
	 */
	private boolean login(String name,String password){
		Player tmp = Profiles.playerExists(name, password);
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
	
	
	/**
	 * Method to send command LINE to all clients.
	 * @param command
	 */

	private void sendDrawing(String command) {
		String[] points = command.split("/");
		String line = "LINE/";
		for(int i = 1 ; i < points.length ; i++){
			line+=points[i]+"/";
		}
		for(int i = 0 ; i < Config.rgb.length ; i++){
			line+=Config.rgb[i]+"/";
		}
		line+=Config.size + "/ \n";
		serv.printToAll(line);
	}
	
	/**
	 * Method to send command COURBE to all clients.
	 * @param command
	 */
	private void sendDrawingCourbe(String command){
		String[] points = command.split("/");
		String line = "COURBE/";
		for(int i = 1 ; i < points.length ; i++){
			line+=points[i]+"/";
		}	
		for(int i = 0 ; i < Config.rgb.length ; i++){
			line+=Config.rgb[i]+"/";
		}
		line+=Config.size + "/ \n";
		serv.printToAll(line);
	}
	
	/**
	 * Method to test if the guessed word is correct
	 * @param word
	 */
	private void guess(String word){
		if(game.correctWord(word,this)){
			printToStream("WORD_FOUND/"+name+"/"+word+"/\n");
			serv.printToExcept("WORD_FOUND/"+name+"/ \n", id);
			guessed = true;
		}else{
			serv.printToAll("GUESSED/"+word+"/"+name+"/ \n");
		}
	}
		
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

	public void setGuessed(boolean b) {
		guessed = b;	
	}	
	
	public void setGame(Game g){
		this.game = g;
	}
}


