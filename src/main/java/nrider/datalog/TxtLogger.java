/*
 * Copyright (c) 2009 David McIntosh (david.mcintosh@yahoo.com)
 *  
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package nrider.datalog;

import nrider.io.PerformanceData;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: Aug 1, 2010
 * Time: 12:50:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class TxtLogger extends BaseLogger
{
	private String _id;

	public TxtLogger( String id )
	{
		_id = id;
	}

	@Override
	public void close()
	{
		Log log = ComputeLog();

		if( log.isEmpty() )
		{
			return;
		}

		try
		{
			FileWriter outFile = new FileWriter( _id + "_log.txt" );
			PrintWriter out = new PrintWriter( outFile );

			out.println( "[USER DATA]" );
			out.println( _id );
			out.println( "[END USER DATA]" );
			out.println( "" );

			out.println( "[COURSE HEADER]" );
			out.println( "MINUTES WATTS" );
			out.println( "[END COURSE HEADER]" );
			out.println( "" );

			out.println( "[COURSE DATA]" );
			out.println( " 1        0.00      100.00" );
			out.println( "[END COURSE DATA]" );
			out.println( "" );

			out.println( "number of records = " + log.getEntries().size() );
			out.println( "" );

			out.println( "ms speed watts rpm hr miles" );
			out.println( "" );
			out.println( " 0   0.00     0   0   0   0.0000" );

			long startMs = log.getStartTime();

			NumberFormat intFormat = NumberFormat.getIntegerInstance();
			NumberFormat decFormat = new DecimalFormat( "0.00" );

			for( LogEntry entry : log.getEntries() )
			{
				out.print( entry.getTimeStamp() - startMs );
				out.print( "   " );
				HashMap<PerformanceData.Type,Float> values = entry.getValues();
				out.print( decFormat.format( getValueOrZero( values, PerformanceData.Type.SPEED ) ) );
				out.print( "   " );
				out.print( intFormat.format( getValueOrZero( values, PerformanceData.Type.POWER ) ) );
				out.print( "   " );
				out.print( intFormat.format( getValueOrZero( values, PerformanceData.Type.CADENCE ) ) );
				out.print( "   " );
				out.print( intFormat.format( getValueOrZero( values, PerformanceData.Type.EXT_HEART_RATE ) ) );
				out.print( "   " );
				out.println( "0.0000" );
			}

			out.close();
			
		}
		catch( IOException e )
		{
			throw new Error( "could not log", e );
		}
	}

	private float getValueOrZero(HashMap<PerformanceData.Type,Float> values, PerformanceData.Type type )
	{
		if( values.containsKey( type ) )
		{
			return values.get( type );
		}
		return 0;
	}
}
