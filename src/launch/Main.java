package launch;

import struct.Server;
import tools.Config;
import tools.Profiles;
import tools.Tools;

import com.beust.jcommander.JCommander;

/**
 * Main class to run the server.
 * @author marwanghanem
 *
 */
public class Main {
	public static void main(String args[]){
		new JCommander(new Config(),args);
		Profiles.initComptes();
		Tools.fillDic(Config.dico);
		Server s = new Server();
		s.start();
	}
}
