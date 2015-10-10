/*********************************************************************
	Filename: VfSer.java	
	Author: Anand Vadaje

This file consists of the VF server part of the code for the project,
wherein when the voter connects with the Vf server it check the veri-
fication acquired by the LA and if its same then it begins with the 
voting procedure for the connected voter.
*********************************************************************/

import java.io.*; 
import java.net.*;
import java.io.File;
import javax.crypto.*;
import java.security.*;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.security.MessageDigest;
import java.security.interfaces.RSAPrivateKey;	
import java.security.interfaces.RSAPublicKey;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.DateFormat;
import java.util.Calendar;
 

class VfSer {
	public static void main(String argv[]) throws Exception{
		
		//Check for command line arguments
		if(argv.length != 1)
		{
			System.out.println("ONE Command line argument has to be Entered!!");
			System.exit(0);
		}
		
		ServerSocket listen = new ServerSocket(Integer.parseInt(argv[0]));
		
		while(true){
			Socket conn = listen.accept(); 
			new MyServerThread(conn);
		}
	}
	
	//thread class 
	private static class MyServerThread implements Runnable{
		ObjectInputStream in1;
		ObjectOutputStream out;
		Socket conn1;
		
		public MyServerThread(Socket conn)
		{
			try{
				//creation of input and out streams for the sockets
				conn1=conn;
				in1 = new ObjectInputStream(conn.getInputStream()); 
				out = new ObjectOutputStream(conn.getOutputStream());		
				(new Thread(this)).start();
			}catch(Exception e){
					e.printStackTrace();
			}
		}
		
		//processing of the code by each thread
		public void run(){
			try{
				
				out.writeObject("I am VF");
				
				//checking which client has connected
				String whoisit = (String)in1.readObject();
				
				
				if(whoisit.equals("I am Client")) //if the Voter-Cli has connected
				{
					
					
					byte[] in = (byte[])in1.readObject();
					byte[] to_decrypt = in; 
					String stringtosend = " ";
					String toencrypt = null;
		
					//Decrypting the input from voter
					ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream("PrVF.key"));
					final PrivateKey privateKey = (PrivateKey) inputStream.readObject();		
					String plainText = decrypt(to_decrypt, privateKey);
		
					String decrypted_number = plainText;
					System.out.println("\nThe Verification Number sent by the client is: \n"+plainText);
		
					//Getting the contents of the file Voternumber
					//-----------------------------------------------------------------------
					String datafilename = "Voternumber";
		
					Scanner infrmfile = new Scanner(new File(datafilename));
					List<String> lines = new ArrayList<String>();
			
					while (infrmfile.hasNextLine()) {
						lines.add(infrmfile.nextLine());
					}

					String[] arr = lines.toArray(new String[0]);
					
					Integer i = 0;
					Integer count = arr.length;
					String voted = "-1";
		
					for(String value:lines){
						String[] parts1 = value.split("="); 
						variable.num[i] = parts1[0];
						variable.val[i] =parts1[1];
						i++;		
					}
		
					infrmfile.close();
					//------------------------------------------------------------------------
		
					//Getting the contents of the file Result
					//------------------------------------------------------------------------
					String resultfilename = "Result";
		
					Scanner infrmfile2 = new Scanner(new File(resultfilename));
					List<String> lines2 = new ArrayList<String>();
			
					while (infrmfile2.hasNextLine()) {
						lines2.add(infrmfile2.nextLine());
					}

					String[] arr2 = lines2.toArray(new String[0]);
				
					String candidate[] = new String[4];
					i = 0;		

					for(String value2:lines2){
						String[] parts2 = value2.split("="); 
						candidate[i] = parts2[0];
						variable.numvotes[i] =parts2[1];

						i++;		
					}
	
					infrmfile2.close();
					//-------------------------------------------------------------------------
				
					//Getting the contents of File History
					//-------------------------------------------------------------------------
					String historyfilename = "History";
		
					Scanner infrmfile3 = new Scanner(new File(historyfilename));
					List<String> lines3 = new ArrayList<String>();
				
					while (infrmfile3.hasNextLine()) {
						lines3.add(infrmfile3.nextLine());
					}

					String[] arr3 = lines3.toArray(new String[0]);
				
					i = 0;		

					for(String value3:lines3){
						String[] parts3 = value3.split("="); 
						variable.validatehistory[i] = parts3[0];
						variable.history[i] =parts3[1];

						i++;		
					}
		
					Integer histcount = i;

					infrmfile3.close();
					//----------------------------------------------------------------

					Integer index = -1;
					Integer found = 0;
		
					//Checking to see whether the verification number is present in voternumber
					for( i=0; i<count; i++)
					{
						if(variable.num[i].equals(decrypted_number))
						{	
							found = 1;
							voted = variable.val[i];
							index = i;
						}
					}
					
					
					if(found == 0) //If the verification number is not found
					{
						out.writeObject("Invalid");
						conn1.close();
					}
					else //If the verification number is found
					{
						out.writeObject("Found");
					}
		
					if(found == 1)
					{
						while(true){
							String voting = (String)in1.readObject();
							
							
							
							
							if(voting.equals("1"))
							{
								if((variable.val[index].equals("0")))
								{
								
								out.writeObject("Not Voted");
								
								byte[] in2 = (byte[])in1.readObject();
			
								plainText = decrypt(in2, privateKey);
			
								Integer value = Integer.parseInt(plainText);
								Integer pre = Integer.parseInt(variable.numvotes[value-1]);
				
								if(variable.val[index].equals("0"))
									pre = pre + 1;
								else
								{
									System.out.println("This voter has already voted!!");
								}
								variable.val[index] = "1";
			
								//changes in the results
								variable.numvotes[value-1] = Integer.toString(pre);
		
								//changes in the history 
								Date currentDate=new Date();
								SimpleDateFormat niceDateFormat=new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS");
								String currentDateString=niceDateFormat.format(currentDate);
					
								for(i=0; i<histcount; i++)
								{
									if(variable.validatehistory[i].equals(decrypted_number))
										variable.history[i] = currentDateString;				
								}
				
								//Writing the new results to the file Result
								//-------------------------------------------------------------------
								FileWriter fw = new FileWriter("Result");			
								fw.write("");
								fw.close();

								File dir = new File(".");
								String loc = dir.getCanonicalPath() + File.separator + "Result";
	
								FileWriter fstream = new FileWriter(loc, true);
								BufferedWriter out1 = new BufferedWriter(fstream);
			
								for(i=0;i<2;i++)
								{
									out1.write(candidate[i]+"="+variable.numvotes[i]);
									out1.newLine();
								}
									
								//close buffer writer
								out1.close();
								//--------------------------------------------------------------------
			
								//Writing the chenges to the History File
								//--------------------------------------------------------------------
								FileWriter fw1 = new FileWriter("History");			
								fw1.write("");
								fw1.close();

								File dir1 = new File(".");
								String loc1 = dir1.getCanonicalPath() + File.separator + "History";

								FileWriter fstream1 = new FileWriter(loc1, true);
								BufferedWriter out2 = new BufferedWriter(fstream1);
		
								for(i=0;i < histcount;i++)
								{
									out2.write(variable.validatehistory[i]+"="+variable.history[i]);
									out2.newLine();
								}
								
								//close buffer writer
								out2.close();
								//--------------------------------------------------------------------
			
								//Writing the new results to the file Result
								//-------------------------------------------------------------------
								FileWriter fw2 = new FileWriter("Voternumber");			
								fw2.write("");
								fw2.close();

								File dir2 = new File(".");
								String loc2 = dir2.getCanonicalPath() + File.separator + "Voternumber";

								FileWriter fstream2 = new FileWriter(loc2, true);
								BufferedWriter out3 = new BufferedWriter(fstream2);
		
								for(i=0;i<count;i++)
								{
									out3.write(variable.num[i]+"="+variable.val[i]);
									out3.newLine();
								}
									
								//close buffer writer
								out3.close();
								//--------------------------------------------------------------------
								}
								else
								{
									out.writeObject("Voted");
									
								}
							}
							else if(voting.equals("2"))
							{
								String hist = null;
		
								for(i=0; i<count; i++)
								{
									if(variable.validatehistory[i].equals(decrypted_number))
									hist = variable.history[i];
								}
			
								out.writeObject(hist);
							}
							else if(voting.equals("3"))
							{
								String ressend = null;
			
								for(i=0; i<2 ;i++)
								{
									ressend = candidate[i]+"="+variable.numvotes[i];
									out.writeObject(ressend);
								}
			
								out.writeObject("done");
							}
							else if(voting.equals("4"))
							{
								System.out.println("Disconnecting......");
								conn1.close();
								break;
							}
						}
					}
				}
				else //if the connection is from LA Server
				{
					byte[] in = (byte[])in1.readObject();
					byte[] to_decrypt = in;
					
					//Decrypting the input from voter
					ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream("PrVF.key"));
					final PrivateKey privateKey = (PrivateKey) inputStream.readObject();		
					String plainText = decrypt(to_decrypt, privateKey);
					
					String plainText1 = plainText;
					//String decrypted_number = plainText;
					
					ObjectInputStream stream2 = new ObjectInputStream (new FileInputStream("PuLA.key"));
					PublicKey LA_pubkey = (PublicKey)stream2.readObject();
					stream2.close(); 
	
					//Verifying Digital Signature
					Signature sig = Signature.getInstance("SHA1withRSA");
	                sig.initVerify(LA_pubkey);
	                sig.update(to_decrypt);                
	                System.out.println("\nClient signature is being verified...");
					System.out.println("----------------------------------------");
	                if(!sig.verify((byte[])in1.readObject()))
	                {
	                    System.out.println("\n\tClient signature failed\n");
						conn1.close();
	                } 
	                else
	                {
	                    System.out.println("\tLA Server signature has been verified succesfully.");
	                }
					
					File f = new File("Voternumber");
					File f1 = new File("History");
					if(f.exists())
					{
						System.out.println("Files Are Present");
						
						String filename= "Voternumber";
						FileWriter fw = new FileWriter(filename,true); //the true will append the new data
						fw.write(plainText1+"=0\n");//appends the string to the file
						fw.close();
						
						String filename1= "History";
						FileWriter fw1 = new FileWriter(filename1,true); //the true will append the new data
						fw1.write(plainText1+"=0\n");//appends the string to the file
						fw1.close();
					}
					else
					{
						f.createNewFile();
						f1.createNewFile();
						System.out.println("Voternumber and History file created....");
						
						String filename= "Voternumber";
						FileWriter fw = new FileWriter(filename,true); //the true will append the new data
						fw.write(plainText1+"=0\n");//appends the string to the file
						fw.close();

						String filename1= "History";
						FileWriter fw1 = new FileWriter(filename1,true); //the true will append the new data
						fw1.write(plainText1+"=0\n");//appends the string to the file
						fw1.close();
					}	
				}
				
			}catch(Exception e){
				e.printStackTrace();
			 }
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

}

class variable{
		
		static String numvotes[] = new String[4];
		
		static String validatehistory[] = new String[4];
		static String history[] = new String[4]; 
		
		static String num[] = new String[4];
		static String val[] = new String[4];		
}

