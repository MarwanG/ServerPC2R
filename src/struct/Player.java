package struct;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import tools.Encryption;

public class Player implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5389982303843344762L;
	private String name;
	private String password;
	
	
	public Player(String name, String password) {
		super();
		this.name = name;
		this.password = password;
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

	@Override
	public String toString() {
		return "Player [name=" + name + ", password=" + password + "]";
	}
	
	private void writeObject(ObjectOutputStream stream) throws IOException {
		
		name = Encryption.encode("thisisencrypt/"+name+"/makeitharder");
		password = Encryption.encode("thisisencrypt/"+password+"/makeitharder");
		stream.defaultWriteObject();
	}
	
	private void readObject(ObjectInputStream stream) throws ClassNotFoundException, IOException{
		stream.defaultReadObject();
		name = Encryption.decode(name);
		password = Encryption.decode(password);
		name = name.split("/")[1];
		password= password.split("/")[1];
	}
}
