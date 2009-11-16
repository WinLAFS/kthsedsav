package yacs.resources.data;

import dks.niche.ids.ComponentId;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Used by clients of the resource service to convey specifications of needed resources.
 * Used by the resource service to return available resources, if any. 
 * @author LTDATH
 */
public class ResourceRequest implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String componentType;
	private ArrayList<ComponentId> availableComponents = new ArrayList<ComponentId>();
	
	public ResourceRequest() {
		super();
	}
	
	public String getComponentType() {
		return componentType;
	}
	public void setComponentType(String componentType) {
		this.componentType = componentType;
	}
	public ArrayList<ComponentId> getAvailableComponents() {
		return availableComponents;
	}
	public void setAvailableComponents(ArrayList<ComponentId> availableComponents) {
		this.availableComponents = availableComponents;
	}
	
	
}
