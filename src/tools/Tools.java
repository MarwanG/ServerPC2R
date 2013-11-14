package tools;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Tools {

	public static void fillDic(String file){
			Config.list = new ArrayList<String>();  
			try{
				  FileInputStream fstream = new FileInputStream(file);
				  // Get the object of DataInputStream
				  DataInputStream in = new DataInputStream(fstream);
				  BufferedReader br = new BufferedReader(new InputStreamReader(in));
				  String strLine;
				  //Read File Line By Line
				  while ((strLine = br.readLine()) != null)   {
					  Config.list.add(strLine);
				  }
				  //Close the input stream
				  in.close();
				  System.out.println("words have been charged");
			  }catch (Exception e){//Catch exception if any
				  System.err.println("Error: " + e.getMessage());
			  }
		  }

	public static String randomWord(){
		double f = Math.random()*Config.list.size();
		int n = (int)f;
		System.out.println("random n = " + n + " f = " + f);
		return Config.list.get(n);
	}


}



