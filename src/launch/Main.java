package launch;

import struct.Server;
import tools.Config;
import tools.Profiles;
import tools.Tools;

import com.beust.jcommander.JCommander;

public class Main {

	public static void main(String args[]){
		
		//Responsible for the parameters.
		new JCommander(new Config(),args);
		//chargement de bili;
		Profiles.initComptes();
		Tools.fillDic(Config.dico);
		Server s = new Server();
		s.start();
	}
}
