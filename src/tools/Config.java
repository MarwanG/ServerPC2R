package tools;

import java.util.ArrayList;

import struct.Player;

import com.beust.jcommander.Parameter;
/**
 * Class responsible for all variables used by the server as well used by JCommander so they can 
 * be modified easily by the user using command line.
 * @author marwanghanem
 *
 */
public class Config {

	@Parameter(names="-n",description="le nombre de joueurs maximal")
	public static int nbJouer = 4;
	@Parameter(names="-t",description="la durée du timeout une fois le mot trouvé")
	public static int timeSec = 30;
	@Parameter(names="-port",description="le port a utilise")
	public static int port = 2013;
	@Parameter(names="-dico",description="le dictionnaire")
	public static String dico = "words";
	@Parameter(names="-tMax",description="la durée maximal pour trouver le mot")
	public static int tMax = 180;
	@Parameter(names="-cheat",description="la nombre de joueur qui faut declare cheat")
	public static int cheat = 3;
	
	public static ArrayList<String> list;

	public static ArrayList<Player> comptes;
	
	public static int [] rgb = {0,0,0};
	
	public static int size = 1;
	
	
	
}
