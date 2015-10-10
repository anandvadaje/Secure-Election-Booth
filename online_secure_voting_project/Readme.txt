Name: Anand Vadaje
Email: avadaje1@binghamton.edu
-----------------------------------------------------------------------------------------------------
* The Project is done is JAVA.
-----------------------------------------------------------------------------------------------------
* It is been tested in LINUX and BINGSUNS as well.
-----------------------------------------------------------------------------------------------------
* Execution Steps are as follows:
    - First compile the programs using the make command.
    - After comipiling the programs can be executed using the following commands
	*For VfServer
		java VfSer <Vf server's port number>

	*For LASer 
		java LaSer <LA server's port number> <VF server's domain> <VF server's port>

	*For Voter-Cli
		java VoterCli <LA/VF server's domain> <LA/VF server's port>
------------------------------------------------------------------------------------------------------
* Core Code for Encryption and Decryption:

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
----------------------------------------------------------------------------------------------------
*Core code for implementing the concurrent server

  - The outline of the concurrent Vf server is as follows,
	
	class VfSer{
	    public static void main(String argv[]) throws Exception{

		//Declaration of the server socket is done here

		while(true)
		{
			//accept connection
			//create new thread for each new connection
		} 
	    }

	    public static class MyServerThread implements Runnable{

		//constructor	    	
		public MyServerThread()
	    	{
			//declaring the object used by each thread
	 	}

		public void run(){
			//the processing of the Vfser for each thread is put here
		}
	}
-------------------------------------------------------------------------------------------------------
* Some points to be noted
	- I have generated a mechanism wherein when the Voter Client itself identifies to which server it is
	  getting connected to.
	- So not special command line argument is required for the server identification for the voter client.
	- In the VF server I have printed some extra messaged wherein it states that whether the voternumber
	  and history file is created, the verification of digital signature and whether the file voternumber
	  and history file are present or not.
---------------------------------------------------------------------------------------------------------