package struct;

import java.util.ArrayList;

import com.apple.mrj.macos.carbon.Timer;

import tools.Config;
import tools.Tools;

public class Game extends Thread {

	
	private Thread Timer;
	private String word;
	private Server serv;
	private Object obj;
	private int nbCheat;
	private ArrayList<PlayerClient> players;
	ArrayList<PlayerClient> founds;
	PlayerClient drawer;
	

	public Game(Server server,ArrayList<PlayerClient> players, Object obj) {
		this.serv = server;
		this.players = players;		
		this.obj = obj;
	}


	public void run() {
		for(int i = 0 ; i < players.size() ; i++){
			System.out.println("partie nb : " + i);
			startNewPartie(i);	
			synchronized(obj){
					try {
						obj.wait();
					} catch (InterruptedException e) {
						System.out.println("first");
						e.printStackTrace();
					}
				}
			if(nbCheat >= 3){
				serv.printToAll("CHEAT/"+players.get(i).getNom()+"/ \n");
			}
			if(founds.size() > 0 && founds.size() < players.size()-1){
				serv.printToAll("WORD_FOUND_TIMEOUT/"+Config.timeSec+"/ \n");
				runTimer(Config.timeSec);
				synchronized(obj){
					try {
						obj.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
						System.out.println("2nd");
					}
				}
			}
			System.out.println("after the inturupt");
			endPartie(i);
			System.out.println("next round");
		}
		System.out.println("end of partie");
		serv.setRunning(false);
		serv.disconnectAll();
	}
	
	
		//test if guessed word is correct.
		public boolean correctWord(String w,PlayerClient jc){
			if(this.word.equals(w)){
		//		nbFound++;
				synchronized(founds){
					founds.add(jc);
				}
				synchronized(obj){
					obj.notify();
				}
				if(founds.size() == players.size()-1){
					System.out.println("SIZE == guessed");
					Timer.interrupt();
				}
				return true;
			}else{
				return false;
			}
		}
		
		//to declare cheating and end partie if there is more than 3.
		public void cheating(){
			nbCheat++;
			if(nbCheat >= 3){
				synchronized(obj){
					System.out.println("cheating");
					obj.notify();
				}
			}
		}
		
		
		//starts a party	
		private void startNewPartie(int i){
			System.out.println("!!!!!!!!!!!!!!!NEW PARTIE !!!!!!!!!!!!!!!!!");
			runTimer(Config.tMax );
			founds = new ArrayList<PlayerClient>();
			players.get(i).setType(TypeJouer.drawer);
			drawer = players.get(i);
			serv.setDrawer(drawer);
			word = Tools.randomWord();
			//nbFound = 0;
			nbCheat = 0;
			String msg = "NEW_ROUND/dessinateur/"+word+"/ \n";
			serv.printToDrawer(msg);
			serv.printToGuessers("NEW_ROUND/chercheur/ \n");		
		}
		
		//ends a party
		private void endPartie(int i){
			updateScores();
			if(founds.size() > 0)
				serv.printToAll("END_ROUND/"+founds.get(0).getNom()+"/"+word + "\n");
			else
				serv.printToAll("END_ROUND/"+word+"\n");
			printScore();
			players.get(i).setType(TypeJouer.guesser);
			for(int z = 0 ; z < players.size() ; z++){
				System.out.println("player nb turning him " + z);
				players.get(z).setType(TypeJouer.guesser);
				players.get(z).setGuessed(false);
			}
			Timer = null;

			System.out.println("!!!!!!!!!!!!!!!FIN PARTIE !!!!!!!!!!!!!!!!!");
		}
		
		//timer to count global time and end after Config.tMax mins
		private void runTimer(final int t){
			Timer = new Thread(){
				public void run(){
					try {
						Thread.sleep(t * 1000);
						synchronized(obj){
							obj.notify();
							System.out.println("TIMER");
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
			Timer.start();
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
	
		//prints the current score to everyone.
		private void printScore(){
			String msg = "SCORE_FOUND/";
			for(int i = 0 ; i < players.size() ; i++){
				msg+= players.get(i).getNom()+"/"+players.get(i).getScore()+"/";
			}
			msg+="\n";
			serv.printToAll(msg);
		}
}
		


