package counter.service;

import java.util.Random;

import counter.interfaces.CounterInterface;
import counter.interfaces.CounterResyncInterface;
import counter.interfaces.CounterStatusInterface;
import counter.interfaces.HelloAllInterface;
import counter.interfaces.HelloAnyInterface;
import counter.interfaces.SynchronizeInterface;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.jasmine.jade.service.nicheOS.OverlayAccess;
import org.objectweb.jasmine.jade.util.FractalUtil;
import org.objectweb.jasmine.jade.util.NoSuchComponentException;

import dks.niche.ids.ComponentId;
import dks.niche.interfaces.NicheActuatorInterface;
import dks.niche.interfaces.NicheComponentSupportInterface;

public class ServiceComponent implements SynchronizeInterface, CounterInterface, 
	HelloAnyInterface, HelloAllInterface, BindingController, CounterResyncInterface,
    LifeCycleController {

    private Component myself;
    private boolean status;
    private int counterNumber = 0;
    private int round = 0;
    private CounterStatusInterface counterStatus;
    private ComponentId myGlobalId;
    

    public ServiceComponent() {
        System.err.println("CounterService created");
    }
    

    // /////////////////////////////////////////////////////////////////////
    // //////////////////////// Server interfaces //////////////////////////
    // /////////////////////////////////////////////////////////////////////

    public void helloAny(String s) {
        System.out.println(s);
    }

    public void helloAll(String s) {
        System.out.println(s);
    }
    
	public void inreaseCounter(String a) {
		double r = Math.random();
		round++;
		if(r<0.90) {
			int newVal =  increaseCounter();
			System.out.println("[service|"+ round + "\t]> increaseCounter called. New value: " + newVal);
			counterStatus.informCounterValue(myGlobalId, newVal);
		}
		else {
			System.out.println("[service|"+ round + "\t]> increaseCounter OMIT. Value: " + getCounterNumber());
		}
		
	}
	
	public void synchronize(int value) {//TODO REMOVE
		System.out.println("[service]> synchronize called. Current value: " + getCounterNumber() +
				", New value: " + value);
		if (getCounterNumber() < value) {
			setCounterNumber(value);
		}
		
	}
	
	public void reSynchronize(int value) {
		System.out.println("[service]> Component: received sync msg. Current value: " + getCounterNumber() + ". New value: " + value);
		if (getCounterNumber() < value) {
			setCounterNumber(value);
		}
		
		
	}
	
	public int getCounterNumber() {
		return counterNumber;
	}

	public void setCounterNumber(int counterNumber) {
		this.counterNumber = counterNumber;
	}
	
	public int increaseCounter() {
			counterNumber += 1;
			return counterNumber;
	}

    // /////////////////////////////////////////////////////////////////////
    // //////////////////////// Fractal Stuff //////////////////////////////
    // /////////////////////////////////////////////////////////////////////

	public String[] listFc() {
        return new String[] { "component", "counterStatus" };
    }

    public Object lookupFc(final String itfName) throws NoSuchInterfaceException {
        if (itfName.equals("component")) {
            return myself;
        } else if (itfName.equals("counterStatus")) {
        	return counterStatus;
        }
        else {
            throw new NoSuchInterfaceException(itfName);
        }
    }

    public void bindFc(final String itfName, final Object itfValue) throws NoSuchInterfaceException {
        if (itfName.equals("component")) {
            myself = (Component) itfValue;
        } else if (itfName.equals("counterStatus")) {
        	counterStatus = (CounterStatusInterface) itfValue;
        } else {
            throw new NoSuchInterfaceException(itfName);
        }
    }

    public void unbindFc(final String itfName) throws NoSuchInterfaceException {
        if (itfName.equals("component")) {
            myself = null;
        } else if (itfName.equals("counterStatus")) {
        	counterStatus = null;
        } else {
            throw new NoSuchInterfaceException(itfName);
        }
    }

    public String getFcState() {
        return status ? "STARTED" : "STOPPED";
    }

    public void startFc() throws IllegalLifeCycleException {
    	init();
    	Component jadeNode = null;
		Component niche = null;
		OverlayAccess overlayAccess = null;

		Component comps[] = null;
		try {
			comps = Fractal.getSuperController(myself).getFcSuperComponents();
		}
		catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < comps.length; i++) {
			try {
				if (Fractal.getNameController(comps[i]).getFcName().equals("managed_resources")) {
					jadeNode = comps[i];
					break;
				}
			}
			catch (NoSuchInterfaceException e) {
				e.printStackTrace();
			}
		}

		try {
			niche = FractalUtil.getFirstFoundSubComponentByName(jadeNode,"nicheOS");
		}
		catch (NoSuchComponentException e1) {
			e1.printStackTrace();
		}

		try {
			overlayAccess = (OverlayAccess) niche.getFcInterface("overlayAccess");
		}
		catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}

		NicheActuatorInterface nicheOSSupport = overlayAccess.getOverlay().getComponentSupport(myself);
		
		myGlobalId = nicheOSSupport.getResourceManager().getComponentId(myself);
    	
    	status = true;
        System.err.println("Service component started.");
    }

    public void stopFc() throws IllegalLifeCycleException {
        status = false;
    }

	private void init(){
		Component jadeNode = null;
		Component niche = null;
		OverlayAccess overlayAccess = null;

		Component comps[] = null;
		try {
			comps = Fractal.getSuperController(myself).getFcSuperComponents();
		}
		catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < comps.length; i++) {
			try {
				if (Fractal.getNameController(comps[i]).getFcName().equals("managed_resources")) {
					jadeNode = comps[i];
					break;
				}
			}
			catch (NoSuchInterfaceException e) {
				e.printStackTrace();
			}
		}

		try {
			niche = FractalUtil.getFirstFoundSubComponentByName(jadeNode,"nicheOS");
		}
		catch (NoSuchComponentException e1) {
			e1.printStackTrace();
		}

		try {
			overlayAccess = (OverlayAccess) niche.getFcInterface("overlayAccess");
		}
		catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}

		NicheComponentSupportInterface nicheOSSupport = overlayAccess.getOverlay().getComponentSupport(myself);
		myGlobalId = nicheOSSupport.getResourceManager().getComponentId(myself);
		
	}


	

}
