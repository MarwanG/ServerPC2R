package struct;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import tools.Config;
import tools.Tools;

public class Server extends Thread {

	ArrayList<JouerClient> players;
	ArrayList<Socket> sockets;
	ArrayList<DataOutputStream> streams;
	ArrayList<JouerClient> founds;
	JouerClient drawer;
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
	
	public Server(){
		this(Config.nbJouer,Config.port);	
	}
	
	public Server(int c , int port){
		this.capacity = c;
		this.port = port;
		nbConnected = 0;
		players = new ArrayList<JouerClient>();
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
								JouerClient jc = new JouerClient(ids,client,this);
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
		new Thread(){
			public void run(){
				for(int i = 0 ; i < players.size() ; i++){
					startNewPartie(i);	
					synchronized(obj){
							try {
								obj.wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
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
				disconnectAll();
			}
		}.start();
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
	
	
	
	public boolean correctWord(String w,JouerClient jc){
		if(this.word.equals(w)){
			nbFound++;
			synchronized(founds){
				founds.add(jc);
			}
			synchronized(obj){
				obj.notify();
			}
			return true;
		}else{
			return false;
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
	
	
	private void startNewPartie(int i){
		runTimer();
		founds = new ArrayList<JouerClient>();
		partie = true;
		players.get(i).setType(TypeJouer.drawer);
		drawer = players.get(i);
		word = Tools.randomWord();
		System.out.println(word);
		nbFound = 0;
		String msg = "NEW_ROUND/dessinateur/"+word+"/ \n";
		printToDrawer(msg);
		printToGuessers("NEW_ROUND/chercheur/ \n");
		
	}
	
	private void endPartie(int i){
		updateScores();
		printScore();
		players.get(i).setType(TypeJouer.guesser);
	}
	
	private void runTimer(){
		new Thread(){
			public void run(){
				try {
					System.out.println("timer just started");
					Thread.sleep(Config.tMax * 1000);
					System.out.println("i am done");
					synchronized(obj){
						obj.notify();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	private void disconnectAll(){
		for(int i = 0 ; i < players.size() ; i++)
			players.get(i).disconnect();
	}

	private void updateScores(){
		for(int i = 0 ; i < founds.size() ; i++){
			int sc = Math.max(10-i, 5);
			founds.get(i).addToScore(sc);
		}
		if(founds.size()>0)
			drawer.addToScore(10+(founds.size()-1));
	}
	
	private void printScore(){
		String msg = "SCORE_FOUND/";
		for(int i = 0 ; i < players.size() ; i++){
			msg+= players.get(i).getNom()+"/"+players.get(i).getScore()+"/";
		}
		msg+="\n";
		this.printToAll(msg);
	}
	
	//ADD PLAYER.
	public void addPlayer(JouerClient player){
		this.players.add(player);
		nbConnected++;
		System.out.println("connected equal " + nbConnected);
		if(nbConnected == capacity){
			complete = true;
			gameRun();
			System.out.println("game gona run");
		}
			
	}
		
	//REMOVE PLAYER
	public void removePlayer(int id){
		for(int i = 0 ; i < this.nbConnected ; i++){
			if(players.get(i).getId() == id){
				players.remove(i);
				break;
			}
		}
		nbConnected--;
	}
	
	
}
