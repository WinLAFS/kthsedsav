package yacs.job.state;

import java.io.Serializable;

/**
 * A serializable data class that the client programmer of Task.process can use to store state
 * transported through the TaskProcessorInterface.checkpoint function. 
 * @author LTDATH
 */
public class UserCheckpoint implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private int id;
	private Serializable[] values = null;

	/**
	 * Construct an user checkpoint identified by the id. 
	 * Id not used by service, only for programmer convenience.
	 * @param id The id of the checkpoint.
	 */
	public UserCheckpoint(int id){
		this.id=id;
	}

	/**
	 * Id of this user invoked checkpoint.
	 * @return Id of this user invoked checkpoint.
	 */
	public int getId() {
		return id;
	}

	/**
	 * An array of serializable values relevant to this checkpoint.
	 * Interpreted by the functional task programmer.
	 * @return An array of serializable values relevant to this checkpoint.
	 */
	public Serializable[] getValues() {
		return values;
	}
	/**
	 * An array of values relevant to this checkpoint.
	 * Values set by the functional task programmer.
	 * @param values An array of serializable values which have meaning for this task.
	 */
	public void setValues(Serializable[] values) {
		this.values = values;
	}
}
