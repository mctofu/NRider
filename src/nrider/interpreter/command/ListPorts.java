package nrider.interpreter.command;

import gnu.io.CommPortIdentifier;
import nrider.interpreter.BaseCommand;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;

/**
 * Created by IntelliJ IDEA.
 * User: David
 * Date: Nov 1, 2009
 * Time: 11:31:06 AM
 * To change this template use File | Settings | File Templates.
 */
public class ListPorts extends BaseCommand
{
	public String getDescription()
	{
		return "List COM ports";
	}

	public String run( String[] args )
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter( sw );
		Enumeration comms = CommPortIdentifier.getPortIdentifiers();
		while( comms.hasMoreElements() )
		{
			CommPortIdentifier id = (CommPortIdentifier) comms.nextElement();
			pw.println( id.getName() );
		}
		return sw.toString();
	}
}
