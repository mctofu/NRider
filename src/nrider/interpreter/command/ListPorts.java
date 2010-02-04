package nrider.interpreter.command;

import gnu.io.CommPortIdentifier;
import nrider.interpreter.BaseCommand;
import nrider.io.SerialDevice;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
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
		ArrayList<CommPortIdentifier> commPortIds = SerialDevice.getPortIdentifiers();
		for( CommPortIdentifier id : commPortIds )
		{
			pw.println( id.getName() );
		}
		return sw.toString();
	}
}
