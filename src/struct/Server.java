package struct;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import tools.Config;
import tools.Tools;

public class Server extends Thread {

	ArrayList<PlayerClient> players;
	ArrayList<Socket> sockets;
	ArrayList<DataOutputStream> streams;
	ArrayList<PlayerClient> founds;
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
	
	public Server(){
		this(Config.nbJouer,Config.port);	
	}
	
	public Server(int c , int port){
		this.capacity = c;
		this.port = port;
		nbConnected = 0;
		players = new ArrayList<PlayerClient>();
		sockets = new ArrayList<Socket>();
		streams = new ArrayList<DataOutputStream>();
	}
	

	//run function for thread
	public void run(){
			try {
				while(true){
					serv = new ServerSocket(port);
					while(true){
							client = serv.accept();
							System.out.println("New connection");
							if(nbConnected >= capacity || running){
								DataOutputStream outchan = new DataOutputStream(client.getOutputStream());
								outchan.writeChars("Maximum capacity please try again later\n");
								client.close();
								if(complete)
									break;
							}else{
								PlayerClient jc = new PlayerClient(ids,client,this);
								ids++;
								jc.start();
							}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
	
	

	//disconnects everyone
	public void disconnectAll(){
		System.out.println("i got called ");
		for(int i = 0 ; i < players.size() ; i++)
			players.get(i).disconnect();
	}

	
	
	
	
	
	//PRINT FUNCTIONS.
	
	
	
	//prints to add except user with the id;
	public void printToExcept(String s,int id){
		for(int i = 0 ; i < this.nbConnected ; i++){
			if(players.get(i).getPlayerId() != id){
				players.get(i).printToStream(s);
			}
		}
	}
	
	//print to certain player.
	public void printToSpecfic(String s,int pos){
		players.get(pos).printToStream(s);
	}
	
	
	//print to guessers.
	public void printToGuessers(String s){ 
		for(int i = 0 ; i < this.nbConnected ; i++){
			if(players.get(i).getType() == TypeJouer.guesser){
				players.get(i).printToStream(s);
			}
		}
	}
	
	//print to drawer
	public void printToDrawer(String s){ 
		drawer.printToStream(s);
	}
	
	//prints to all
	public void printToAll(String s){
		for(int i = 0 ; i < this.nbConnected ; i++){
				players.get(i).printToStream(s);
			}
		}
	
	//ADDERS AND REMOVERS OF PLAYERS
	
	//ADD PLAYER.
	public void addPlayer(PlayerClient player){
		this.players.add(player);
		nbConnected++;
		System.out.println("nbConnected = " + nbConnected);
		if(nbConnected == capacity){
			complete = true;
			game = new Game(this, players, obj);
			for(int i = 0 ; i < players.size() ; i++)
				players.get(i).setGame(game);
			game.start();
			System.out.println("A new game will run");
		}
			
	}
		
	//REMOVE PLAYER
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

	public boolean NameConnected(String name){
		for(int i = 0 ; i < players.size() ; i++){
			if(name.equals(players.get(i).getNom()))
				return true;
		}
		return false;
	}
	
}
