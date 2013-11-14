package struct;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import tools.Config;

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
		if(nbConnected == capacity)
			complete = true;
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
	
	public void printToAll(String s,int id){
		for(int i = 0 ; i < this.nbConnected ; i++){
			if(players.get(i).getPlayerId() != id){
				players.get(i).printToStream(s);
			}
		}
	}
	
	public void run(){
		{
			try {
				serv = new ServerSocket(port);
				while(true){
					if(!this.complete){
						client = serv.accept();
						System.out.println("New connection \n");
						if(nbConnected >= capacity){
							DataOutputStream outchan = new DataOutputStream(client.getOutputStream());
							outchan.writeChars("Maximum capacity please try again later\n");
							client.close();
						}else{
							JouerClient jc = new JouerClient(ids,client,this);
							ids++;
							jc.start();
						}
					}else{
						if(!running){
							System.out.println("game gona run");
							gameRun();
						}							
						client = serv.accept();
						System.out.println("In the else someone tried to connect");
						DataOutputStream outchan = new DataOutputStream(client.getOutputStream());
						outchan.writeChars("Maximum capacity please try again later\n");
						client.close();
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
				for(int i = 0 ; i < 5 ; i++){
					System.out.println("kaka");
				}
			}
		}.run();;
	}
	
}
