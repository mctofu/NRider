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
package nrider.net;

import nrider.core.IWorkoutListener;
import nrider.event.EventPublisher;
import nrider.io.IPerformanceDataListener;
import nrider.io.IPerformanceDataSource;

/**
 *
 */
public abstract class NetSource implements IPerformanceDataSource, IPerformanceDataListener, IWorkoutListener
{
	private EventPublisher<IPerformanceDataListener> _dataPublisher = EventPublisher.singleThreadPublisher( NetSource.class.getName() );
	private EventPublisher<IWorkoutListener> _workoutPublisher = EventPublisher.singleThreadPublisher( NetSource.class.getName() );

	public void addPerformanceDataListener( IPerformanceDataListener listener )
	{
		_dataPublisher.addListener( listener );
	}

	public void addWorkoutListener( IWorkoutListener listener )
	{
		_workoutPublisher.addListener( listener );
	}

	protected EventPublisher<IPerformanceDataListener> getDataPublisher()
	{
		return _dataPublisher;
	}


}
