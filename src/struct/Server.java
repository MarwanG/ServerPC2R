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
	
	public void removePlayer(int id){
		for(int i = 0 ; i < this.nbConnected ; i++){
			if(players.get(i).getId() == id){
				players.remove(i);
				break;
			}
		}
		nbConnected--;
	}
	
	public void printToExcept(String s,int id){
		for(int i = 0 ; i < this.nbConnected ; i++){
			if(players.get(i).getPlayerId() != id){
				players.get(i).printToStream(s);
			}
		}
	}
	
	public void printToSpecfic(String s,int pos){
		players.get(pos).printToStream(s);
	}
	
	public void printToGuessers(String s){ 
		for(int i = 0 ; i < this.nbConnected ; i++){
			if(players.get(i).getType() == TypeJouer.guesser){
				//players.get(i).printToStream("GUESSER");
				players.get(i).printToStream(s);
			}
		}
	}
	

	public void printToDrawer(String s){ 
		for(int i = 0 ; i < this.nbConnected ; i++){
			if(players.get(i).getType() == TypeJouer.drawer){
				players.get(i).printToStream(s);
				//players.get(i).printToStream("DRAWER");
			}
		}
	}
	
	public void printToAll(String s){
		for(int i = 0 ; i < this.nbConnected ; i++){
				players.get(i).printToStream(s);
			}
		}
	
	public void run(){
		{
			try {
				while(true){
					serv = new ServerSocket(port);
					while(true){
							client = serv.accept();
							System.out.println("New connection \n");
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
	
	public boolean correctWord(String w){
		return this.word.equals(w);
	}
	
	public void setPartie(boolean b){
		partie = b;
	}
	
	public boolean getPartie(){
		return partie;
	}
	
	public void add1(){
		nbFound++;
	}
	
	public int getNbFound(){
		return nbFound;
	}
	
	public void addMeToFound(JouerClient jp){
		synchronized(founds){
			founds.add(jp);
		}
	}
	
	public void gameRun(){
		new Thread(){
			public void run(){
				for(int i = 0 ; i < players.size() ; i++){
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
						synchronized(obj){
							try {
								obj.wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						updateScores();
						printScore();
						players.get(i).setType(TypeJouer.guesser);
						System.out.println("next round");
				}
				System.out.println("end of partie");
			}
		}.start();
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
	
}
