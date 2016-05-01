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

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: Aug 1, 2010
 * Time: 12:51:04 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class BaseLogger
{

	private LinkedList<PerformanceData> _data = new LinkedList<PerformanceData>();

	public void logData( PerformanceData data )
	{
		_data.add( data );
	}

	private float getAverage( List<Float> values )
	{
		float sum = 0;

		for( Float value : values )
		{
			sum += value;
		}

		return  sum / values.size();
	}

	public Log ComputeLog()
	{
		List<LogEntry> logEntries = new ArrayList<LogEntry>();
		long trackStart = _data.getFirst().getTimeStamp();
		HashMap<PerformanceData.Type, List<Float>> _trackValues = new HashMap<PerformanceData.Type, List<Float>>();
		HashMap<PerformanceData.Type, Float> lastValue = new HashMap<PerformanceData.Type, Float>();
		for( PerformanceData pd : _data )
		{
			if( pd.getTimeStamp() - trackStart > 1000  || pd == _data.getLast() )
			{
				if( !_trackValues.isEmpty() )
				{
					LogEntry entry = new LogEntry();
					entry.setTimeStamp( trackStart );
					HashMap<PerformanceData.Type, Float> values = new HashMap();
					for( Map.Entry<PerformanceData.Type, List<Float>> dataEntry : _trackValues.entrySet() )
					{
						float value = getAverage(dataEntry.getValue() );
						values.put( dataEntry.getKey(), value );
						lastValue.put( dataEntry.getKey(), value );
					}
					for( Map.Entry<PerformanceData.Type, Float> lastEntry : lastValue.entrySet() )
					{
						if( !values.containsKey( lastEntry.getKey() ) )
						{
							values.put( lastEntry.getKey(), lastEntry.getValue() );
						}
					}
					entry.setValues( values );
					logEntries.add( entry );
				}
				_trackValues.clear();
				trackStart = pd.getTimeStamp();
			}
			if( !_trackValues.containsKey( pd.getType() ) )
			{
				_trackValues.put( pd.getType(), new ArrayList<Float>() );
			}
			_trackValues.get( pd.getType() ).add( pd.getValue() );
		}

		Log log = new Log();
		log.setEntries( logEntries );
		return log;
	}

	public abstract void close();

	class Log
	{
		private List<LogEntry> _entries;

		public boolean isEmpty()
		{
			return _entries.isEmpty();
		}

		public long getStartTime()
		{
			return _entries.get( 0 ).getTimeStamp();
		}

		public long getEndTime()
		{
			return _entries.get( _entries.size() - 1 ).getTimeStamp();
		}

		public long getDuration()
		{
			return getEndTime() - getStartTime();
		}

		public List<LogEntry> getEntries()
		{
			return _entries;
		}

		public void setEntries( List<LogEntry> entries )
		{
			_entries = entries;
		}
	}

	class LogEntry
	{
		private long _timeStamp;
		private HashMap<PerformanceData.Type,Float> _values = new HashMap();

		public long getTimeStamp()
		{
			return _timeStamp;
		}

		public HashMap<PerformanceData.Type, Float> getValues()
		{
			return _values;
		}

		public void setTimeStamp( long timeStamp )
		{
			_timeStamp = timeStamp;
		}

		public void setValues( HashMap<PerformanceData.Type, Float> values )
		{
			_values = values;
		}
	}
}
