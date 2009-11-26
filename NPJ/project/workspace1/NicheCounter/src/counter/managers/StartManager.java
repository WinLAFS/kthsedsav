package counter.managers;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.jasmine.jade.service.componentdeployment.DeploymentParams;
import org.objectweb.jasmine.jade.service.componentdeployment.NicheIdRegistry;
import org.objectweb.jasmine.jade.service.nicheOS.OverlayAccess;
import org.objectweb.jasmine.jade.util.Serialization;

import counter.aggregators.ServiceSupervisor;
import counter.events.ComponentOutOfSyncEvent;
import counter.events.CounterChangedEvent;
import counter.events.InformOutOfSyncEvent;
import counter.events.ServiceAvailabilityChangeEvent;
import counter.executors.CounterStateChangedExecutor;
import counter.service.ServiceComponent;
import counter.watchers.CounterChangedWatcher;
import dks.niche.events.ComponentFailEvent;
import dks.niche.events.MemberAddedEvent;
import dks.niche.fractal.FractalInterfaceNames;
import dks.niche.ids.ComponentId;
import dks.niche.ids.GroupId;
import dks.niche.ids.NicheId;
import dks.niche.interfaces.JadeBindInterface;
import dks.niche.interfaces.NicheActuatorInterface;
import dks.niche.interfaces.NicheManagementInterface;
import dks.niche.wrappers.ManagementDeployParameters;
import dks.niche.wrappers.ScriptInfo;

/**
 * This class is responsible for initializing the system by deploying all the management
 * elements, creating the {@link ServiceComponent}s group, binding the element with
 * the interfaces that they will use to make calls and subscribing management elements
 * to events.
 * 
 * @author Vasileios Trigoanakis, Andrei Shumanski
 */
public class StartManager implements BindingController, LifeCycleController {

    private final static String APPLICATION_PREFIX = "NicheCounter_0/";
    private final static String FRONTEND_COMPONENT = "frontend";
    private final static String SERVICE_COMPONENT = "service";
    private OverlayAccess nicheService;
    private NicheIdRegistry nicheIdRegistry;
    private Component mySelf;

    private boolean status;

    /**
     * Default constructor
     */
    public StartManager() {
    }
    
    /**
     * Constuctor
     * 
     * @param nicheInstance
     * @param si
     */
    public StartManager(NicheManagementInterface nicheInstance, ScriptInfo si) {
    }

    /**
     * The method that initializes everything.
     */
    private void startScript() {
        System.err.println("Starting Counter StartManager.");

        // Get a reference to the Niche API.
        NicheActuatorInterface myActuatorInterface = nicheService.getOverlay().getJadeSupport();

        // Find the front-end component.
        ComponentId frontendComponent =
            (ComponentId) nicheIdRegistry.lookup(APPLICATION_PREFIX + FRONTEND_COMPONENT);

        // Find all service components.
        ArrayList<ComponentId> serviceComponents = new ArrayList();
        int serviceComponentIndex = 1;
        ComponentId serviceComponent =
            (ComponentId) nicheIdRegistry.lookup(APPLICATION_PREFIX + SERVICE_COMPONENT
                + serviceComponentIndex);
        while (serviceComponent != null) {
            serviceComponents.add(serviceComponent);
            serviceComponentIndex++;
            serviceComponent =
                (ComponentId) nicheIdRegistry.lookup(APPLICATION_PREFIX + SERVICE_COMPONENT
                    + serviceComponentIndex);
        }

        // Create a component group containing all service components.
        GroupId serviceGroupTemplate = myActuatorInterface.getGroupTemplate();
        serviceGroupTemplate.addServerBinding("counter", JadeBindInterface.ONE_TO_MANY);
        GroupId serviceGroup =
            myActuatorInterface.createGroup(serviceGroupTemplate, serviceComponents);

        String clientInterfaceName = "";
        String serverInterfaceName = "";
        
        //Counter binding
        clientInterfaceName = "counter";
        serverInterfaceName = "counter";
        myActuatorInterface.bind(frontendComponent, clientInterfaceName, serviceGroup,
                                 serverInterfaceName, JadeBindInterface.ONE_TO_MANY);

        // Configure and deploy the ServiceSupervisor aggregator.
        ManagementDeployParameters params = new ManagementDeployParameters();
        params.describeAggregator(ServiceSupervisor.class.getName(), "SA", null,
                                  new Serializable[] { serviceGroup.getId() });
        params.setReliable(true);
        NicheId serviceSupervisor =
            myActuatorInterface.deployManagementElement(params, serviceGroup);
        myActuatorInterface.subscribe(serviceGroup, serviceSupervisor,
                                      ComponentFailEvent.class.getName());
        myActuatorInterface.subscribe(serviceGroup, serviceSupervisor,
                                      MemberAddedEvent.class.getName());
        
        // Grab the service component's properties from a service component
        // which is already deployed. The ConfigurationManager needs these when
        // it deploys a new service component.
        ComponentId grabParametersFromThis =
            (ComponentId) nicheIdRegistry.lookup(APPLICATION_PREFIX + SERVICE_COMPONENT + "1");
        DeploymentParams serviceComponentProperties = null;
        try {
            serviceComponentProperties =
                (DeploymentParams) Serialization.deserialize(grabParametersFromThis.getSerializedDeployParameters());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // Configure and deploy the ConfigurationManager manager.
        String minimumNodeCapacity = "200";
        params = new ManagementDeployParameters();
        params.describeManager(ConfigurationManager.class.getName(), "CM", null,
                               new Serializable[] { serviceGroup, serviceComponentProperties,
                                       minimumNodeCapacity });
        params.setReliable(true);
        NicheId configurationManager =
            myActuatorInterface.deployManagementElement(params, serviceGroup);
        myActuatorInterface.subscribe(serviceSupervisor, configurationManager,
                                      ServiceAvailabilityChangeEvent.class.getName());
        
        //deploy and initialize the watcher
        ManagementDeployParameters params2 = new ManagementDeployParameters();
        params2.describeWatcher(CounterChangedWatcher.class.getName(), 
        		"WW", 
        		null, 
        		new Serializable[] {}, 
        		serviceGroup.getId());
        NicheId watcherId = myActuatorInterface.deployManagementElement(params2, serviceGroup);
        
        myActuatorInterface.subscribe(watcherId, serviceSupervisor,
                CounterChangedEvent.class.getName());
        
        myActuatorInterface.subscribe(serviceSupervisor, configurationManager,
                ComponentOutOfSyncEvent.class.getName());
        
        //deploy and initialize executor
        ManagementDeployParameters params3 = new ManagementDeployParameters();
        params3.describeExecutor(CounterStateChangedExecutor.class.getName(), 
        		"CEX", 
        		null, 
        		new Serializable[] {}, 
        		serviceGroup.getId());
        NicheId executorId = myActuatorInterface.deployManagementElement(params3, serviceGroup);
        
        myActuatorInterface.subscribe(configurationManager, executorId,
                InformOutOfSyncEvent.class.getName());
    }

    // ///////////////////////////////////////////////////////////////
    // ////////////////////// Fractal Stuff //////////////////////////
    // ///////////////////////////////////////////////////////////////

    /**
     * @see org.objectweb.fractal.api.control.BindingController#listFc()
     */
    public String[] listFc() {
        return new String[] { FractalInterfaceNames.COMPONENT,
                FractalInterfaceNames.OVERLAY_ACCESS, FractalInterfaceNames.ID_REGISTRY };
    }

    /**
     * @see org.objectweb.fractal.api.control.BindingController#lookupFc(java.lang.String)
     */
    public Object lookupFc(final String itfName) throws NoSuchInterfaceException {
        if (itfName.equals(FractalInterfaceNames.OVERLAY_ACCESS)) {
            return nicheService;
        } else if (itfName.equals(FractalInterfaceNames.ID_REGISTRY)) {
            return nicheIdRegistry;
        } else if (itfName.equals(FractalInterfaceNames.COMPONENT)) {
            return mySelf;
        } else {
            throw new NoSuchInterfaceException(itfName);
        }
    }

    /**
     * @see org.objectweb.fractal.api.control.BindingController#bindFc(java.lang.String, java.lang.Object)
     */
    public void bindFc(final String itfName, final Object itfValue) throws NoSuchInterfaceException {
        if (itfName.equals(FractalInterfaceNames.OVERLAY_ACCESS)) {
            nicheService = (OverlayAccess) itfValue;
        } else if (itfName.equals(FractalInterfaceNames.ID_REGISTRY)) {
            nicheIdRegistry = (NicheIdRegistry) itfValue;
        } else if (itfName.equals(FractalInterfaceNames.COMPONENT)) {
            mySelf = (Component) itfValue;
        } else {
            throw new NoSuchInterfaceException(itfName);
        }
    }

    /**
     * @see org.objectweb.fractal.api.control.BindingController#unbindFc(java.lang.String)
     */
    public void unbindFc(final String itfName) throws NoSuchInterfaceException {
        if (itfName.equals(FractalInterfaceNames.OVERLAY_ACCESS)) {
            nicheService = null;
        } else if (itfName.equals(FractalInterfaceNames.ID_REGISTRY)) {
            nicheIdRegistry = null;
        } else if (itfName.equals(FractalInterfaceNames.COMPONENT)) {
            mySelf = null;
        } else {
            throw new NoSuchInterfaceException(itfName);
        }
    }

    /**
     * @see org.objectweb.fractal.api.control.LifeCycleController#getFcState()
     */
    public String getFcState() {
        return status ? "STARTED" : "STOPPED";
    }

    /**
     * @see org.objectweb.fractal.api.control.LifeCycleController#startFc()
     */
    public void startFc() throws IllegalLifeCycleException {
        status = true;
        startScript();
    }

    /**
     * @see org.objectweb.fractal.api.control.LifeCycleController#stopFc()
     */
    public void stopFc() throws IllegalLifeCycleException {
        status = false;
    }
}
