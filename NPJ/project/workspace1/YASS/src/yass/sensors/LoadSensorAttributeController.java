package yass.sensors;

import org.objectweb.fractal.api.control.AttributeController;

public interface LoadSensorAttributeController extends AttributeController {
	public void setDelta(int delta);
	public int getDelta();
	
	public void setTotalStorage(int total);
	public int getTotalStorage();
	
	public void setCurrentLoad(int load);
	public int getCurrentLoad();
	
	
}
