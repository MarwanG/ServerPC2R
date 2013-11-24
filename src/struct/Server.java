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
	
	Thread game;
	
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
		{
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
	}
	
	//thread to start a game.
	public void gameRun(){	
		game = new Thread(){
			public void run(){
				for(int i = 0 ; i < players.size() ; i++){
					System.out.println("Game nb : " + i);
					startNewPartie(i);	
					synchronized(obj){
							try {
								obj.wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						if(nbCheat >= 3){
							printToAll("CHEAT/"+players.get(i).getNom()+"/ \n");
						}
						if(founds.size() > 0){
							printToAll("WORD_FOUND_TIMEOUT/"+Config.timeSec+"/ \n");
							try {
								Thread.sleep(Config.timeSec * 1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						endPartie(i);
						System.out.println("next round");
				}
				System.out.println("end of partie");
				running = false;
				disconnectAll();
			}
		};
		game.start();
	}
	
	
	//UTILS FOR RUNNING A GAME


	//starts a party	
	private void startNewPartie(int i){
		runTimer();
		founds = new ArrayList<PlayerClient>();
		partie = true;
		players.get(i).setType(TypeJouer.drawer);
		drawer = players.get(i);
		word = Tools.randomWord();
		nbFound = 0;
		nbCheat = 0;
		String msg = "NEW_ROUND/dessinateur/"+word+"/ \n";
		printToDrawer(msg);
		printToGuessers("NEW_ROUND/chercheur/ \n");		
	}
	
	//ends a party
	private void endPartie(int i){
		updateScores();
		if(founds.size() > 0)
			printToAll("END_ROUND/"+founds.get(0).getNom()+"/"+word + "\n");
		else
			printToAll("END_ROUND/"+word+"\n");
		printScore();
		players.get(i).setType(TypeJouer.guesser);
		for(int z = 0 ; z < players.size() ; z++){
			players.get(z).setGuessed(false);
		}
	}
	
	//timer to count global time and end after Config.tMax mins
	private void runTimer(){
		new Thread(){
			public void run(){
				try {
					Thread.sleep(Config.tMax * 1000);
					synchronized(obj){
						obj.notify();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	//disconnects everyone
	private void disconnectAll(){
		for(int i = 0 ; i < players.size() ; i++)
			players.get(i).disconnect();
	}

	//updates scores
	private void updateScores(){
		for(int i = 0 ; i < founds.size() ; i++){
			int sc = Math.max(10-i, 5);
			founds.get(i).addToScore(sc);
		}
		if(founds.size()>0)
			drawer.addToScore(10+(founds.size()-1));
	}
	
	//to declare cheating and end partie if there is more than 3.
	public void cheating(){
		nbCheat++;
		if(nbCheat >= 3){
			synchronized(obj){
				obj.notify();
			}
		}
	}
	
	//test if guessed word is correct.
	public boolean correctWord(String w,PlayerClient jc){
		if(this.word.equals(w)){
			nbFound++;
			synchronized(founds){
				founds.add(jc);
			}
			synchronized(obj){
				obj.notify();
			}
			if(founds.size() == players.size()-1){
				game.interrupt();
			}
			return true;
		}else{
			return false;
		}
	}
	
	
	//PRINT FUNCTIONS.
	
	//prints the current score to everyone.
	private void printScore(){
		String msg = "SCORE_FOUND/";
		for(int i = 0 ; i < players.size() ; i++){
			msg+= players.get(i).getNom()+"/"+players.get(i).getScore()+"/";
		}
		msg+="\n";
		this.printToAll(msg);
	}
	
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
			gameRun();
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
}
