package counter.interfaces;

import counter.actuators.CounterStatusActuator;
import counter.service.ServiceComponent;

/**
 * The interface that {@link ServiceComponent} implements in order
 * to receive calls by {@link CounterStatusActuator} component for 
 * resynchronizing its counter value. 
 *
 */
public interface CounterResyncInterface {
	/**
	 * The method that provides the synchonization.
	 * 
	 * @param value the value in which the components should try to synchronize
	 * @param syncId the id of the call that produced this value. See {@link CounterInterface}.
	 */
	public void reSynchronize(int value, int syncId);
}
