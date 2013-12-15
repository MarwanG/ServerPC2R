package struct;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import tools.Config;
/**
 * 
 * @author marwanghanem
 * Class responsible for the server.
 */
public class Server extends Thread {

	
	
	ArrayList<String> listOfCommands;
	ArrayList<PlayerClient> players;
	ArrayList<Socket> sockets;
	ArrayList<DataOutputStream> streams;
	ArrayList<PlayerClient> observs;
	ArrayList<PlayerClient> founds;
	ArrayList<String> names;
	PlayerClient drawer;
	ServerSocket serv;
	Socket client;
	int capacity;
	int port;
	int nbConnected;
	int ids = 0;
	int nbFound;
	boolean complete = false;
	boolean running = false;
	boolean partie = false;
	public Object obj = new Object();
	String word;
	int nbCheat = 0;
	Game game;
	
	
	/**
	 * Constructor used in the case where the variables are in default.
	 */
	public Server(){
		this(Config.nbJouer,Config.port);	
	}
	
	
	/**
	 * Constructor that use modified variables.
	 * @param c Capacity of the players in a game.
	 * @param port the port to be used;
	 */
	public Server(int c , int port){
		this.capacity = c;
		this.port = port;
		nbConnected = 0;
		players = new ArrayList<PlayerClient>();
		sockets = new ArrayList<Socket>();
		streams = new ArrayList<DataOutputStream>();
		observs = new ArrayList<PlayerClient>();
		names = new ArrayList<String>();
	}
	
	
	
	/**
	 * Method responsible to run the thread.
	 */
	public void run(){
			try {
				this.listOfCommands = new ArrayList<String>();
				while(true){
					serv = new ServerSocket(port);
					while(true){
							client = serv.accept();
							System.out.println("New connection");
							/*if(nbConnected >= capacity || running){
								DataOutputStream outchan = new DataOutputStream(client.getOutputStream());
								outchan.writeChars("Maximum capacity please try again later\n");
								client.close();
								if(complete)
									break;
							}else{*/
								PlayerClient jc = new PlayerClient(ids,client,this);
								ids++;
								jc.start();
							}
					}
				//}
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
	
	

	/**
	 * Method responsible to disconnect all the players.
	 */
	public void disconnectAll(){
		System.out.println("i got called ");
		for(int i = 0 ; i < players.size() ; i++)
			players.get(i).disconnect();
	}


	/**
	 * Method prints the string s to all players except the one with id = id;
	 * @param s
	 * @param id
	 */
	public void printToExcept(String s,int id){
		this.listOfCommands.add(s);
		printToObs(s);
		for(int i = 0 ; i < this.nbConnected ; i++){
			if(players.get(i).getPlayerId() != id){
				players.get(i).printToStream(s);
			}
		}
	}
	
	/**
	 * Method prints the string s to the player in position pos in the list of players.
	 * @param s
	 * @param pos
	 */
	public void printToSpecfic(String s,int pos){
		this.listOfCommands.add(s);
		printToObs(s);
		players.get(pos).printToStream(s);
	}
	
	
	/**
	 * Method prints the string s all to the guessers 
	 * @param s
	 */
	public void printToGuessers(String s){ 
		//this.listOfCommands.add(s);
		//printToObs(s);
		for(int i = 0 ; i < this.nbConnected ; i++){
			if(players.get(i).getType() == TypeJouer.guesser){
				players.get(i).printToStream(s);
			}
		}
	}
	
	/**
	 * Method prints the string s to the drawer.
	 * @param s
	 */
	public void printToDrawer(String s){ 
		this.listOfCommands.add(s);
		printToObs(s);
		drawer.printToStream(s);
	}
	
	/**
	 * Method that prints the string s to all players
	 * @param s
	 */
	public void printToAll(String s){
		this.listOfCommands.add(s);
		printToObs(s);
		for(int i = 0 ; i < this.nbConnected ; i++){
				players.get(i).printToStream(s);
			}
		}
	
	
	public void printToObs(String s){
		for(int i = 0 ; i < this.observs.size() ; i++)
			observs.get(i).printToStream(s);
	}
	
	public void AddObs(PlayerClient p){
		this.observs.add(p);
		for(int i = 0 ; i < this.listOfCommands.size() ; i++){
			p.printToStream(listOfCommands.get(i));
		}
	}
	
	
	/**
	 * Adds a client(player) to the list and starts the game if the capacity has been reached.
	 * @param player
	 */
	public void addPlayer(PlayerClient player){
		this.players.add(player);
		nbConnected++;
		System.out.println("nbConnected = " + nbConnected);
		if(nbConnected == capacity){
			complete = true;
			game = new Game(this, players, obj);
			for(int i = 0 ; i < players.size() ; i++)
				players.get(i).setGame(game);
			game.setRun(true);
			game.start();
			System.out.println("A new game will run");
		}
			
	}
		
	/**
	 * Method to remove the player with the ID: id and treats different cases of disconnections.
	 * Including brutal disconnection or loss of connection.
	 * @param id
	 */
	public void removePlayer(int id){
		for(int i = 0 ; i < this.nbConnected ; i++){
			if(players.get(i).getPlayerId() == id){
				printToExcept("EXITED/"+players.get(i).getNom()+"/\n", id);
				players.remove(i);
				break;
			}
		}
		if(nbConnected > 0)
			nbConnected--;
		
		
		if(drawer != null){
			if(drawer.getPlayerId() == id){
				synchronized(obj){
					obj.notify();
				}
			}
		}	
		if(nbConnected == 1){
			synchronized(obj){
				obj.notify();
			}
		}
	}
	
	/**
	 * Special method to remove the player that cheated without notifying 
	 * since the turn would be ending in all cases.
	 * @param id the player's id is passed to keep the method general in case
	 * 		  future implementations include other cheaters beside drawer.
	 */
	public void removeCheater(int id){
		for(int i = 0 ; i < this.nbConnected ; i++){
			if(players.get(i).getPlayerId() == id){
				printToExcept("EXITED/"+players.get(i).getNom()+"/\n", id);
				players.remove(i);
				break;
			}
		}
		if(nbConnected > 0)
			nbConnected--;
		
	}
	
	/**
	 * Method that returns if there is a player already using the same nickname
	 * @param name
	 * @return returns true if there is a player in the party with the same nickname
	 */
	public boolean NameConnected(String name){
		for(int i = 0 ; i < players.size() ; i++){
			if(name.equals(players.get(i).getNom()))
				return true;
		}
		return false;
	}
	
	/**
	 * Method that simply does a notify on the obj to be used as a signal.
	 */

	public void notifyObj(){
		synchronized(obj){
			obj.notify();
		}
	}
	
	public void setPartie(boolean b){
		partie = b;
	}
	
	public boolean getPartie(){
		return partie;
	}
	
	public int getNbFound(){
		return nbFound;
	}

	public PlayerClient getDrawer() {
		return drawer;
	}

	public void setDrawer(PlayerClient drawer) {
		this.drawer = drawer;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}	

	
	public void addName(String name){
		names.add(name);
	}


	public int getCapacity() {
		return capacity;
	}


	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}


	public int getNbConnected() {
		return nbConnected;
	}


	public void setNbConnected(int nbConnected) {
		this.nbConnected = nbConnected;
	}


	public ArrayList<String> getListOfCommands() {
		return listOfCommands;
	}


	public void setListOfCommands(ArrayList<String> listOfCommands) {
		this.listOfCommands = listOfCommands;
	}
	
	
	
	
}