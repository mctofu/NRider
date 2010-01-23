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
package nrider.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by IntelliJ IDEA.
 * User: David
 * Date: Nov 5, 2009
 * Time: 10:02:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class EventPublisher<T> implements IEventPublisher<T>
{
	private List<T> _listeners = new ArrayList<T>();
	private static HashMap<String, Executor> _executorMap = new HashMap<String, Executor>();

	private Executor _executor;

	public EventPublisher( Executor executor )
	{
		_executor = executor;
	}

	public static<T> EventPublisher<T> directPublisher()
	{
		return new EventPublisher<T>(
			new Executor() {
				public void execute(Runnable r) {
					r.run();
				}
			}
		);
	}

	public static<T> EventPublisher<T> singleThreadPublisher()
	{
		return new EventPublisher<T>( Executors.newSingleThreadExecutor() );
	}

	public static<T> EventPublisher<T> singleThreadPublisher( String executorName )
	{
		Executor executor;
		synchronized( _executorMap )
		{
			if( !_executorMap.containsKey( executorName ) )
			{
				_executorMap.put( executorName, Executors.newSingleThreadExecutor() );
			}
			executor = _executorMap.get( executorName );
		}
		return new EventPublisher<T>( executor );
	}

	public void addListener( T listener )
	{
		synchronized( _listeners )
		{
			_listeners.add( listener );
		}
	}

	public void publishEvent( final IEvent<T> event )
	{
		synchronized( _listeners )
		{
			for( final T listener : _listeners )
			{
				_executor.execute(
					new Runnable()
					{
						public void run()
						{
							event.trigger( listener );
						}
					});
			}
		}
	}
}
