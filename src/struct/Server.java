package struct;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
			if(players.get(i).getId() != id)
				players.get(i).printToStream(s);
		}
	}
	
	public void run(){
		{
			try {
				serv = new ServerSocket(port);
				while(true){
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
				}
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
	}
	
}
