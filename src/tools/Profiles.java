package tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import struct.Player;

public class Profiles {

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
			System.out.println("ArrayList<Player> class not found"); 
			c.printStackTrace();
			return;
		}
		if(list == null){
			Config.comptes = new ArrayList<Player>();
		}else{
			Config.comptes = list;
		}
		System.out.println("Accounts have been charged");
	}
	
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
			System.out.println("Accounts have been backed up");
		}catch(IOException i) {
			i.printStackTrace(); 
		}
	}
	
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
	
	public static Player playerExists(String name , String password){
		for(Player p : Config.comptes){
			if(p.getName().equals(name) && p.getPassword().equals(password)){
				return p;
			}
		}
		return null;
	}
	
	public static void addPlayer(Player p){
		Config.comptes.add(p);
		saveCompters();
		initComptes();
	}

}
