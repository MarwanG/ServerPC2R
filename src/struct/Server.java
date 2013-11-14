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
	ServerSocket serv;
	Socket client;
	int capacity;
	int port;
	int nbConnected;
	int ids = 0;
	boolean complete = false;
	boolean running = false;
	
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
	
	public void gameRun(){
		new Thread(){
			public void run(){
				for(int i = 0 ; i < players.size() ; i++){
					players.get(i).setType(TypeJouer.drawer);
					String msg = "NEW_ROUND/dessinateur/"+Tools.randomWord()+"/ \n";
					printToDrawer(msg);
					printToGuessers("NEW_ROUND/chercheur/ \n"); 
					break; //TODO THE REST OF THE GAME;
				}
			}
		}.start();
	}
	
}
