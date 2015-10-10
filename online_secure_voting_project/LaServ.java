/*********************************************************************
	Filename: LaServ.java	
	Author: Anand Vadaje

This file consists of the LAServer part of the project for the voting
system, wherein it assigns the verification number to each voter who 
has registered their name and ssn with the LA.
*********************************************************************/

import java.io.*; 
import java.net.*;
import java.io.File;
import javax.crypto.*;
import java.security.*;
import java.io.*;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.security.MessageDigest;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.security.interfaces.RSAPrivateKey;	
import java.security.interfaces.RSAPublicKey;
 

class LaServ {
	public static void main(String argv[]) throws Exception{

		//Check for command line arguments
		if(argv.length != 3)
		{
			System.out.println("THREE Command line arguments have to be Entered!!");
			System.exit(0);
		}
		
		//Socket Creation
		ServerSocket listen = new ServerSocket(Integer.parseInt(argv[0]));
		Socket conn;
		ObjectInputStream in1;
		ObjectOutputStream out;
		
		//working iteratively
		while(true)
		{
			conn = listen.accept(); 
			
			//creation of input and out streams for the sockets
			in1 = new ObjectInputStream(conn.getInputStream()); 
			out = new ObjectOutputStream(conn.getOutputStream());

			out.writeObject("I am LA");
		
			//declaration and initializations of the strings needed		
			byte[] in = (byte[])in1.readObject();
			byte[] to_decrypt = in; 
			String stringtosend = " ";
			String toencrypt = null;
			String voterpublic = null;
			String voterprivate = null;	
		
			//Decrypting the input from voter
			ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream("PrLA.key"));
			final PrivateKey privateKey = (PrivateKey) inputStream.readObject();		
			String plainText = decrypt(to_decrypt, privateKey);
		
			String[] parts = plainText.split("="); 
			String inname = parts[0];
			String inssn = parts[1];
	
			System.out.println("\nThe name of the voter is: \n"+"\t"+inname);
			
			voterpublic="Pu"+inname+".key";
			voterprivate="Pr"+inname+".key";
			
			//Getting the contents of the files STATUS and VERIFY
			//-----------------------------------------------------------
			String datafilename = "status.txt";
		
			Scanner infrmfile = new Scanner(new File(datafilename));
			List<String> lines = new ArrayList<String>();
			
			while (infrmfile.hasNextLine()) {
				lines.add(infrmfile.nextLine());
			}

			String[] arr = lines.toArray(new String[0]);
		
			String name= " ", ssn = " ",status = " ";
			String validation_num = "0";
		
			String verifyfilename = "verify.txt";
		
			Scanner infrmfile1 = new Scanner(new File(verifyfilename));
			List<String> lines1 = new ArrayList<String>();
			
			while (infrmfile1.hasNextLine()) {
				lines1.add(infrmfile1.nextLine());
			}

			String[] arr1 = lines1.toArray(new String[0]);
		
			String vssn[] = new String[4];
			String vnum[] = new String[4]; 
			Integer i = 0;		
			byte[] ciphertosend = in;

			for(String value1:lines1){
				String[] parts1 = value1.split("="); 
				vssn[i] = parts1[0];
				vnum[i] =parts1[1];
 
				i++;		
			}

			infrmfile1.close();
			
			//---------------------------------------------------------------
	
			i=0;	
			for(String value:lines)
			{
				String[] parts2 = value.split("="); 
				name = parts2[0];
				ssn = parts2[1];
				status=parts2[2];
				
				//if the voter is valid
				if( inname.equals(name) && ssn.equals(inssn) && status.equals("citizen"))
				{
					if(vnum[i].equals("0")) //if the verification number is not present
					{
						int validate = (new Random().nextInt(90000000)+10000000);
				
						validation_num = Integer.toString(validate);
						vnum[i]= validation_num; 

						// Encrypt the voter information using the public key of Voter
						ObjectInputStream inputStream1 = new ObjectInputStream(new FileInputStream(voterpublic));
						final PublicKey publicKey = (PublicKey) inputStream1.readObject();
						final byte[] cipherText = encrypt(validation_num, publicKey);
						ciphertosend = cipherText;
					
						// Encrypt the verification number using the public key of VF
						ObjectInputStream inputStream10 = new ObjectInputStream(new FileInputStream("PuVF.key"));
						final PublicKey publicKey10 = (PublicKey) inputStream10.readObject();
						final byte[] cipherText10 = encrypt(validation_num, publicKey10);
						
						stringtosend = cipherText.toString();
					
						//Socket to connect to VF
						Socket sock = new Socket(argv[1], Integer.parseInt(argv[2]));    	
						ObjectOutputStream outtovf = new ObjectOutputStream(sock.getOutputStream());
						ObjectInputStream infromvf = new ObjectInputStream(sock.getInputStream());

						String aa = (String)infromvf.readObject();
						
						outtovf.writeObject("I am LAServer");
					
						outtovf.writeObject(cipherText10);
					
						//assigning digital signature
						//-----------------------------------------------------------------------------------------
						ObjectInputStream stream = new ObjectInputStream (new FileInputStream("PrLA.key"));
						PrivateKey LA_privateKey = (PrivateKey)stream.readObject();
						stream.close(); 
			
						Signature sig = Signature.getInstance("SHA1withRSA");
						sig.initSign(LA_privateKey);
						sig.update(cipherText10); 
						outtovf.writeObject(sig.sign());
						//-----------------------------------------------------------------------------------------
					
						sock.close();
					}
					else //if the verification number is present
					{
						ObjectInputStream inputStream1 = new ObjectInputStream(new FileInputStream(voterpublic));
						final PublicKey publicKey = (PublicKey) inputStream1.readObject();
						final byte[] cipherText = encrypt(vnum[i].toString(), publicKey);
						ciphertosend = cipherText;
				
						stringtosend = cipherText.toString();
					}
	    
				}			
				i=i+1;		
			}
		

			if(stringtosend.equals(" ")) //if the voter was not valid
			{
				out.writeObject("NO");
			}		
			else	//if the voter is valid necessary changes are done
			{
				FileWriter fw = new FileWriter("verify.txt");			
				fw.write("");
				fw.close();
				File dir = new File(".");
				String loc = dir.getCanonicalPath() + File.separator + "verify.txt";
 
				FileWriter fstream = new FileWriter(loc, true);
				BufferedWriter out1 = new BufferedWriter(fstream);
 	
				for(i=0;i<4;i++)
				{
					out1.write(vssn[i]+"="+vnum[i]);
					out1.newLine();
				}

				//close buffer writer
				out1.close();
		
				out.writeObject(ciphertosend);
			} 

			conn.close();
		
		}
		
	}
	
	//The RSA Decrypting function
	public static String decrypt(byte[] text, PrivateKey key) {
    		byte[] dectyptedText = null;
				try {
					// get an RSA cipher object and print the provider
					final Cipher cipher = Cipher.getInstance("RSA");

					// decrypt the text using the private key
					cipher.init(Cipher.DECRYPT_MODE, key);
					dectyptedText = cipher.doFinal(text);

				} catch (Exception ex) {
						ex.printStackTrace();
				    }
		 
			return new String(dectyptedText);
  	}

	//The RSA Encrypting function
	public static byte[] encrypt(String text, PublicKey key) {
		byte[] cipherText = null;
    		try {
      			// get an RSA cipher object and print the provider
      			final Cipher cipher = Cipher.getInstance("RSA");
      			// encrypt the plain text using the public key
      			cipher.init(Cipher.ENCRYPT_MODE, key);
      			cipherText = cipher.doFinal(text.getBytes());
    		} catch (Exception e) {
      			e.printStackTrace();
    		   }
    
		return (cipherText);
  	}	
}
