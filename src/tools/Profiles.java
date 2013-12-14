package tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import struct.Player;
/**
 * Class responsible for creating profiles 
 * @author marwanghanem
 *
 */
public class Profiles {

	
	/**
	 * Function that initializations the accounts that have been saved before.
	 */
	@SuppressWarnings("unchecked")
	public static void initComptes(){
		ArrayList<Player> list = null; 
		
		try{
			if((new File("tmp/listCompte.ser").exists())){
				FileInputStream fileIn = new FileInputStream("tmp/listCompte.ser");
				ObjectInputStream in = new ObjectInputStream(fileIn);		
				list = (ArrayList<Player>) in.readObject();
				in.close();
				fileIn.close(); 
			}
		}
		catch(IOException i) {
			i.printStackTrace();
			return; 
		}catch(ClassNotFoundException c) {
			c.printStackTrace();
			return;
		}
		if(list == null){
			Config.comptes = new ArrayList<Player>();
		}else{
			Config.comptes = list;
		}
	}
	
	/**
	 * Function that keeps saves the accounts in file listCompte.ser to make sure there is a backup.
	 */
	public static void saveCompters(){
		try
		{
			File f = new File("tmp/listCompte.ser");
			if(!(f.exists())){
				if(!f.getParentFile().exists())
					f.getParentFile().mkdirs();
				f.createNewFile();
			}
			FileOutputStream fileOut = new FileOutputStream(f);
			ObjectOutputStream out = new ObjectOutputStream(fileOut); 
			if(Config.comptes != null){
				out.writeObject(Config.comptes);
			}
			out.close();
			fileOut.close();
		}catch(IOException i) {
			i.printStackTrace(); 
		}
	}
	
	
	/**
	 * Function that tests if the string s has already be used as a nickname by a different user.
	 * @param s
	 * @return returns a boolean that corresponds to true if a player with name s already exisits.
	 */
	public static boolean nameExists(String s){
		boolean res = false;
		for(Player p : Config.comptes){
			if(p.getName().equals(s)){
				res = true;
				break;
			}
		}
		return res;
	}
	
	/**
	 * Function tests if the name and password passed as parameter correspond to an actual player in
	 * the database.
	 * @param name
	 * @param password
	 * @return returns true if a player with the same name and password exists
	 */
	public static Player playerExists(String name , String password){
		for(Player p : Config.comptes){
			if(p.getName().equals(name) && p.getPassword().equals(password)){
				return p;
			}
		}
		return null;
	}
	
	
	/**
	 * Adds player p to the dataBase.
	 * @param p
	 */
	public static void addPlayer(Player p){
		Config.comptes.add(p);
		saveCompters();
		initComptes();
	}

}
