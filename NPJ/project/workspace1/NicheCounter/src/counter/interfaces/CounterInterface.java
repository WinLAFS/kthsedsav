package counter.interfaces;

import counter.frontend.FrontendComponent;
import counter.service.ServiceComponent;

/**
 * The interface the is implementing by the {@link ServiceComponent}
 * in order to receives calls by the {@link FrontendComponent} for
 * increasing the counter.
 *
 */
public interface CounterInterface {
	/**
	 * The increased counter method. Each time called the counter increase
	 * its value by 1.
	 * 
	 * @param a the unique id of the call
	 */
	public void inreaseCounter(int syncId);
}
