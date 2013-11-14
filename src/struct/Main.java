package struct;

import tools.Config;

import com.beust.jcommander.JCommander;

public class Main {

	public static void main(String args[]){
		
		
		//Responsible for the parameters.
		new JCommander(new Config(),args);
		Server s = new Server();
		s.start();
	}
}
