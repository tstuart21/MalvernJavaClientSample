/**********************************************************************************\
Purpose: Java MSS Client Demonstration 
Author: John Del Percio
Purpose: This collection of definitions/functions are used throughout.  These were pulled from a
         default query at initialization of the main screen.  They all utilize the SSSocket class
         defined seperately (excerpts below.) 
Language: Java
Platform: Java JRE >= 4.2
Depends on: Java JDK >= 4.2
Notes:  This is an extremly simple, functional demonstration of communicating with
        Malvern Site Server via Java Sockets.  It is a simple console application that takes
        a hardcoded host and port, and sends a single hardcoded request to the MSS server, with sample 
        MSS error response handling included, and displays the request and response to the console 
        output. It does not demonstrate threading, timeout handling, or other more involved features 
        a full client should implement.  This serves only as a demonstration/reusable sample code
        for the communications protocol used with MSS. 
\***********************************************************************************/

import java.io.IOException;
import java.net.*;
import java.io.*;


class msssocket
{
    public static void main(String[] args)
    {
        //Creates the client object. Basic single query transaction test below.  Application should run query, and exit. 
        try
        {
            SSSocket client = new SSSocket();
            
            //Connects to the server/port combination
            System.out.println("Connecting...");
            client.connect("localhost", 1048);
            
            //Sends a transaction to the MSS.  The entire parameter can just be your ping string here.
             //Basic ping request:    //String request = "0,\"098\"99,\"\""; 
             //Basic version request: //String request = "0,\"009\"1,\"126\"99,\"\"";            
            //Basic rate request:
            String request = "0,\"002\"1,\"12345\"12,\"Contact\"11,\"Company\"13,\"40 Lloyd Ave\"14,\"Suite 103\"15,\"Malvern\"16,\"PA\"17,\"19355\"50,\"\"18,\"6101112233\"21,\"10\"19,\"UPS\"22,\"GND\"24,\"20211108\"6001,\"11082021112233\"99,\"\"";
            
            System.out.println("\nSending request: " + request);
            client.send(request);
            
            //Final application should include timeout handling. This example does not handle timeout conditions.
            String response = client.recv();
            
            //Handle an error response (replies that feature a field 2 (error code) and field 3 (error message), or log successful response.
            if((!SSSocket.getMsgField("2", response).equals("")) && (!SSSocket.getMsgField("3", response).equals("")))
                System.out.println("\nSS ERROR RESPONSE FOUND: " + SSSocket.getMsgField("3", response));
            else
                System.out.println("\nResponse received: " + response);
            
            //Socket shutdown is handled by the SSSocket destructor, but it's a simple "Socket.close();"
            client.disconnect();
        }
        catch(Exception e)
        {
            System.out.println("An Exception occurred: " + e.getMessage());
        }
    }
}

class SSSocket
{
    public  boolean is_connected;
	private Socket client = null;
	private PrintWriter clientout = null;
	private BufferedReader clientin = null;
    
    public void connect(String host, int port) throws SSSocketException
    {
        try
		{
			client = new Socket(host, port);
			clientout = new PrintWriter(client.getOutputStream(), true);
			clientin = new BufferedReader(new InputStreamReader(client.getInputStream()));
		}
		catch(UnknownHostException e)
		{
			throw new SSSocketException("Can not connect to host!: " + e.getMessage());
		}
		catch(IOException e)
		{
			throw new SSSocketException("Connection IO error: " + e.getMessage());
		}
		
		is_connected = true;
    }
    
    public void disconnect()
	{
		try
		{
			clientout.close();
			clientin.close();
			client.close();
		}
		catch(IOException e)
		{
			System.out.println("IOException in disconnect(): " + e.getMessage());
		}
		finally
		{
			is_connected = false;
		}
	}
	
	//Sends the transaction to the MSS.  Streams can be formatted or unformatted type for transmission.
	public void send(String data)
	{	
		clientout.print(data);
		clientout.flush();
	}
	
	//Receives the response from MSS.  This is a little more complicated than send.  Note the use of .read (single character reads) 
	// instead of the simpler .ReadLine.  Label buffers, specifically USPS/Endicia can return some binary data with the label which poses
	// a problem for formatted streams.  If you won't be printing your own labels (having them returned in fields 188, etc.) then this should not 
	// be an issue and you won't have to worry about formatted streams.  Additionally, you may ignore the more complex end-of-line handling here.  
	// Much of this revolved around some complexity in the application this was excerpted from.  The "99,"" lookup is all you need.
	public String recv()
	{
		StringBuffer instring = new StringBuffer();
		int readchr = 0;
		
		try
		{
			do
			{
				try
				{
					while(!clientin.ready())
					{
						Thread.sleep(4);
					}
				}
				catch(InterruptedException e)
				{
					System.out.println("Interrupted exception caught: " + e.getMessage());
				}
				
				readchr = clientin.read();
				
				if(readchr != -1)
					instring.append((char)readchr);
				
			} while((instring.length() <= 5 || (instring.indexOf("99,\"\"") == -1)) && readchr != -1);
					
		}
		catch(IOException e)
		{
			System.out.println("IOException found in recv(): " + e.getMessage());
		}
		
		return(instring.toString());
	}	
	
	
	 public static String getMsgField(String field, String message)
	 {
	     int startpos;
	     int endpos;
	     
	     startpos = message.indexOf("\"" + field + ",\"");
	     
	     if(startpos == -1)
	     {
	         startpos = message.indexOf(field + ",\"");
	         if(startpos != 0)
	             return("");
	         else
	             startpos += field.length() + 2;
	     }
	     else
	         startpos += field.length() + 3;
	         
         endpos = message.indexOf("\"", startpos);
         
         return(message.substring(startpos, startpos + (endpos - startpos)));
	 }
	
}


