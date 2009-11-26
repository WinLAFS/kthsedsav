package counter.interfaces;

import counter.sensors.CounterStatusSensor;
import counter.service.ServiceComponent;
import dks.niche.ids.ComponentId;

/**
 * The interface used by {@link ServiceComponent} and implemented by
 * {@link CounterStatusSensor} in order the 1st to inform the
 * second about the change on the 1st's state (counter value).  
 *
 */
public interface CounterStatusInterface {
	/**
	 * The method that provides the inform functionality.
	 * 
	 * @param cid the {@link ComponentId} of the component informing about the state change
	 * @param value the new counter value of the component
	 * @param syncId the sync id value that the "order" of the counter increase had
	 */
	public void informCounterValue(ComponentId cid,int value, int syncId);
}
