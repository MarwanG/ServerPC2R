package struct;

import java.util.ArrayList;

import tools.Config;
import tools.Tools;


/**
 * 
 * @author marwanghanem
 *
 */
public class Game extends Thread {

	
	private Thread Timer;
	private String word;
	private Server serv;
	private Object obj;
	private int nbCheat;
	private ArrayList<PlayerClient> players;
	ArrayList<PlayerClient> founds;
	PlayerClient drawer;
	private boolean run;
	
	
	
	/**
	 * 
	 * @param server Server of type struct.server
	 * @param players List of player Client
	 * @param obj Object for sync between different classes.
	 */

	public Game(Server server,ArrayList<PlayerClient> players, Object obj) {
		this.serv = server;
		this.players = players;		
		this.obj = obj;
	}


	/**
	 * Function responsible to run the game.
	 * A wait of 2 seconds is added to make sure that all clients are 
	 * ready to play.
	 */
	
	public void run() {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			for(int i = 0 ; i < players.size() ; i++){
				startNewPartie(i);	
				synchronized(obj){
						try {
							obj.wait();
						} catch (InterruptedException e) {
							//e.printStackTrace();
						}
					}
				if(nbCheat >= 3){
					serv.printToAll("CHEAT/"+players.get(i).getNom()+"/ \n");
					founds.clear();
					serv.removeCheater(players.get(i).getPlayerId());
				}
				if(founds.size() > 0 && founds.size() < players.size()-1){
					serv.printToAll("WORD_FOUND_TIMEOUT/"+Config.timeSec+"/ \n");
					runTimer(Config.timeSec);
					synchronized(obj){
						try {
							obj.wait();
						} catch (InterruptedException e) {
						}
					}
				}
				endPartie(i);
			}
		serv.setRunning(false);
		synchronized(obj){
			try {
				obj.wait();
			} catch (InterruptedException e) {
			}
		}
		serv.disconnectAll();
		serv.setListOfCommands(new ArrayList<String>());
	}
	
	/**
	 * Function to test if the word is correct or not and alert thread using obj.
	 * @param w	Word that to be test.
	 * @param jc PlayerClient that has guessed the word.
	 * @return boolean to alert the called if the word is correct or not.
	 */
	public boolean correctWord(String w,PlayerClient jc){
		if(this.word.equals(w)){
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
		
	
	/**
	 * Function to declare that the drawer has cheated.
	 */
	public void cheating(){
		nbCheat++;
		if(nbCheat >= Config.cheat){
			synchronized(obj){
				obj.notify();
			}
		}
	}
		
		
	/**
	 * Function to start a new turn.
	 * @param i position of the player that will be the drawer.
	 */
	private void startNewPartie(int i){
		runTimer(Config.tMax );
		founds = new ArrayList<PlayerClient>();
		players.get(i).setType(TypeJouer.drawer);
		drawer = players.get(i);
		serv.setDrawer(drawer);
		word = Tools.randomWord();
		nbCheat = 0;
		String msg = "NEW_ROUND/dessinateur/"+word+"/ \n";
		serv.printToDrawer(msg);
		serv.printToGuessers("NEW_ROUND/chercheur/ \n");		
	}
		
	/**
	 * Function to end the turn and print for players.
	 * and rest the variables needed for the next turn. 
	 * in case where no one founds it a extra / is added to always have same number of parameters
	 * @param i
	 */
	private void endPartie(int i){
		updateScores();
		if(founds.size() > 0)
			serv.printToAll("END_ROUND/"+founds.get(0).getNom()+"/"+word + "/\n");
		else
			serv.printToAll("END_ROUND//"+word+"/\n");
		printScore();
		players.get(i).setType(TypeJouer.guesser);
		for(int z = 0 ; z < players.size() ; z++){
			players.get(z).setType(TypeJouer.guesser);
			players.get(z).setGuessed(false);
		}
		Timer = null;

	}
		
	/**
	 * Timer used in different parts of the program and uses 
	 * obj as a buzzer to alert
	 * @param t variable of type integer counts as the number of secs 
	 * 			the timer will use.
	 */
	private void runTimer(final int t){
		Timer = new Thread(){
			public void run(){
				try {
					Thread.sleep(t * 1000);
					synchronized(obj){
						obj.notify();
					}
				} catch (InterruptedException e) {
					//e.printStackTrace();
				}
			}
		};
		Timer.start();
	}
		
	/**
	 * Function that updates score for all the players including the drawer.
	 */
	private void updateScores(){
		for(int i = 0 ; i < founds.size() ; i++){
			int sc = Math.max(10-i, 5);
			founds.get(i).addToScore(sc);
		}
		if(founds.size()>0)
			drawer.addToScore(10+(founds.size()-1));
	}
	
	/**
	 * creates the string with scores and sends to server to be printed 
	 * to all players.
	 */
	private void printScore(){
		String msg = "SCORE_ROUND/";
		for(int i = 0 ; i < players.size() ; i++){
			msg+= players.get(i).getNom()+"/"+players.get(i).getScore()+"/";
		}
		msg+="\n";
		serv.printToAll(msg);
	}


	public boolean isRun() {
		return run;
	}


	public void setRun(boolean run) {
		this.run = run;
	}
	
	
}

		


