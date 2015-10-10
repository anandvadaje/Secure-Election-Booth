/*********************************************************************
	Filename: VoterCli.java	
	Author: Anand Vadaje

This file consists of the client part of the code for the project,
wherein the voter invokes it in connection with the LA server to
acquire the verification number which is further used for voting with
the Voting system.
*********************************************************************/

import java.io.*; 
import java.net.*;
import javax.crypto.*;
import java.security.*;
import java.util.*; 
import java.util.Scanner;
import java.security.interfaces.RSAPrivateKey;	
import java.security.interfaces.RSAPublicKey;
import java.nio.charset.Charset;

class VoterCli {
	public static void main(String argv[]) throws Exception{
		
		//Check for command line arguments
		if(argv.length != 2)
		{
			System.out.println("TWO Command line arguments have to be Entered!!");
			System.exit(0);
		}
		
		Socket sock = new Socket(argv[0], Integer.parseInt(argv[1]));    	
		ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
		ObjectInputStream in = new ObjectInputStream(sock.getInputStream());   
		
		String who = (String)in.readObject();
		
		if(who.equals("I am LA"))
		{
			//Scanner for system input
			InputStreamReader reader = new InputStreamReader(System.in);
			BufferedReader input = new BufferedReader(reader);
			
			String cmd = null;
			String fromserver = null;
			String name = "";
			String ssn = "";
			String to_encrypt = null;
			String to_send = null;
			String pubkey_voter = null;
		
			//Getting the details of the voter	
			System.out.println("Enter your NAME....");
			name = input.readLine();
			while(name.equals("")) //checking for blank input
			{
				System.out.println("Name cannot be blank...(Enter Your name)");
				name = input.readLine();
			}
			System.out.println("Enter your SSN....");
			ssn = input.readLine();
			while(ssn.equals("")) //checking for blank input
			{
				System.out.println("SSN cannot be blank...(Enter Your SSN)");
				ssn = input.readLine();
			}
		
			//getting the voter specific public and private keys
			String voterpublic="Pu"+name+".key";
			String voterprivate="Pr"+name+".key";

			//input for encryption
			to_encrypt = name+"="+ssn;            
		
			try{
				// Encrypt the voter information using the public key of LA
				ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream("PuLA.key"));
				final PublicKey publicKey = (PublicKey) inputStream.readObject();
				final byte[] cipherText = encrypt(to_encrypt, publicKey); 

				//sending encrypted text to LA
				out.writeObject(cipherText);
	
				System.out.println("\nVerifying...");
				System.out.println("---------------------------\n");
	
				Charset UTF8_CHARSET = Charset.forName("UTF-8");
	
				byte[] in1 = (byte[])in.readObject();
				byte[] to_dec = in1;
	
				fromserver=in1.toString();
				
				String vnum = null;

				if(fromserver.equals("NO"))
					System.out.println("You are not eligible to vote");
				else
				{
					//Decrypting the input from voter
					ObjectInputStream inputStream1 = new ObjectInputStream(new FileInputStream(voterprivate));
					final PrivateKey privateKey = (PrivateKey) inputStream1.readObject();		
					String plainText = decrypt(to_dec, privateKey);
		
					vnum = plainText;
		
					System.out.println("\nThe Validation number is: \n\t"+plainText);
				}
				
				sock.close();
		
			}catch(ClassCastException e){
				System.out.println("You are not eligible to vote");
				sock.close();
				System.exit(0);
		    }
		}
		else if(who.equals("I am VF"))
		{
			//Socket Creation
			Socket socktovf = sock;   	
			ObjectOutputStream outtovf = out;
			ObjectInputStream infromvf = in;
		
			//Scanner for System Input
			InputStreamReader reader = new InputStreamReader(System.in);
			BufferedReader input = new BufferedReader(reader);
			
			//Sending the identification to VF
			outtovf.writeObject("I am Client");
	
			//Getting the validation number from voter
			System.out.println("Enter the validation number..");
			String uservnum = input.readLine();
	
			// Encrypting the validation number using the public key of VF
			ObjectInputStream inputStream2 = new ObjectInputStream(new FileInputStream("PuVF.key"));
			final PublicKey publicKey2 = (PublicKey) inputStream2.readObject();
			final byte[] cipherText2 = encrypt(uservnum, publicKey2); 

			//sending encrypted text to VF Server
			outtovf.writeObject(cipherText2);

			String in2 = (String)infromvf.readObject();
			String option = null;
			String fromserver=in2;
	
			
			if (fromserver.equals("Invalid")) //If Wrong Verification number is entered 
				System.out.println("Invalid verification number...");
			else // If the verification number entered is correct
			{
				while(true)
				{
					System.out.println("\n Please enter a number(1-4)\n\t1.Vote\n\t2.My Vote History\n\t3.View the latest results\n\t4.Quit");
					System.out.println("Your Choice");
					option=input.readLine();
		
					if(option.equals("1")) //If the voter wants to vote
					{
						outtovf.writeObject("1");
					
						String in10 = (String)infromvf.readObject();
					
						//Checking if the voter has already voted
						if(in10.equals("Voted"))
						{
							System.out.println("You have already voted!!");				
						}
						else
						{
							System.out.println("Please Enter a Number(1-2)\n\t1.Bob\n\t2.John");
							String voteto = input.readLine();
			
							// Encrypt the validation number using the public key of VF
							final byte[] cipherText3 = encrypt(voteto, publicKey2);
				
							//sending encrypted text to VF
							outtovf.writeObject(cipherText3);
						}
					}
					else if(option.equals("2"))	//If the voter wants to check the history
					{
						outtovf.writeObject("2");
						in2 = (String)infromvf.readObject();
			
						System.out.println("\t"+in2);
					}
					else if(option.equals("3")) //If the voter wants to see the latest results
					{
						outtovf.writeObject("3");
						System.out.println("The Result are as follows....");
				
						while(!((in2 = (String)infromvf.readObject()).equals("done")))
						{
							System.out.println("\t" + in2);
						}
					}
					else if(option.equals("4")) //If the voter wants to disconnect
					{
						outtovf.writeObject("4");
						System.out.println("Disconnecting.....");
						socktovf.close();
						System.exit(0);
					}	
					else //Wrong input
					{
						System.out.println("Wrong Option Please select between 1 to 4.....");
					}
				}	
			}	
		}
		else
		{
			System.out.println("Connection problem !!");
		}
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
  	
  	//The RSA Encrypting function
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
} 
  
