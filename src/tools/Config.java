package tools;

import java.util.ArrayList;

import com.beust.jcommander.Parameter;

public class Config {

	@Parameter(names="-n",description="le nombre de joueurs maximal")
	public static int nbJouer = 3;
	@Parameter(names="-t",description="la durée du timeout une fois le mot trouvé")
	public static int timeSec = 30;
	@Parameter(names="-port",description="le port a utilise")
	public static int port = 2013;
	@Parameter(names="-dico",description="le dictionnaire")
	public static String dico = "words";
	@Parameter(names="-tMax",description="la durée maximal pour trouver le mot")
	public static int tMax = 180; 
	
	public static ArrayList<String> list;

	
}
