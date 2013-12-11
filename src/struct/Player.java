package struct;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


import tools.Encryption;

/**
 * 
 * @author marwanghanem
 *
 */
public class Player implements Serializable{

	
	private static final long serialVersionUID = -5389982303843344762L;
	private String name;
	private String password;
	
	
	/**
	 * Class used mainly to create accounts for players who would like to keep thier accounts.
	 * @param name
	 * @param password
	 */
	public Player(String name, String password) {
		super();
		this.name = name;
		this.password = password;
	}
	
	/**
	 * A modified writeObject method so the serialization would be encrypted
	 * as password is considered as personal data. 
	 * @param stream
	 * @throws IOException
	 */
	private void writeObject(ObjectOutputStream stream) throws IOException {
		
		name = Encryption.encode(name);
		password = Encryption.encode(password);
		stream.defaultWriteObject();
	}
	
	/**
	 * A modified readObject to be able to decode the stream as it is encrypted.
	 * @param stream
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private void readObject(ObjectInputStream stream) throws ClassNotFoundException, IOException{
		stream.defaultReadObject();
		name = Encryption.decode(name);
		password = Encryption.decode(password);
	}
	
	@Override
	public String toString() {
		return "Player [name=" + name + ", password=" + password + "]";
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

	
}
