public class SSSocketException extends java.io.IOException
{
	public String supertype = new String();
	
	SSSocketException()
	{
		supertype = "Undefined exception encountered in SSSocket operation.";
	}
	
	SSSocketException(String exceptype)
	{
		supertype = exceptype;
	}
};
