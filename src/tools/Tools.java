package tools;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * 
 * @author marwanghanem
 *
 */
public class Tools {

	
	/**
	 * Method that takes a name of a file and creates list of word for the server. 
	 * @param file
	 */
	public static void fillDic(String file){
			Config.list = new ArrayList<String>();  
			try{
				  FileInputStream fstream = new FileInputStream(file);
				  DataInputStream in = new DataInputStream(fstream);
				  BufferedReader br = new BufferedReader(new InputStreamReader(in));
				  String strLine;
				  while ((strLine = br.readLine()) != null)   {
					  Config.list.add(strLine);
				  }
				  in.close();
				  System.out.println("words have been charged");
			  }catch (Exception e){//Catch exception if any
				  System.err.println("Error: " + e.getMessage());
			  }
		  }

	/**
	 * Method that returns a random word for the list of words.
	 * @return a string that corresponds to the word for next round
	 */
	public static String randomWord(){
		double f = Math.random()*Config.list.size();
		int n = (int)f;
		return Config.list.get(n);
	}
	
	/**
	 * Method to change the default color for drawing.
	 * @param cmd
	 */
	public static void changeColor(String cmd){
		String [] split = cmd.split("/");
		for(int i = 0 ; i < 3 ;i++){
			Config.rgb[i] = Integer.valueOf(split[i+1]);
		}
		
	}
	

}





