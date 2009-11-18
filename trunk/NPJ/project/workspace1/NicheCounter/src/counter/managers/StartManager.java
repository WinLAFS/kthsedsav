package counter.managers;

import counter.aggregators.ServiceSupervisor;
import counter.events.ServiceAvailabilityChangeEvent;

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

public class StartManager implements BindingController, LifeCycleController {

    private final static String APPLICATION_PREFIX = "NicheCounter_0/";
    private final static String FRONTEND_COMPONENT = "frontend";
    private final static String SERVICE_COMPONENT = "service";
    private OverlayAccess nicheService;
    private NicheIdRegistry nicheIdRegistry;
    private Component mySelf;

    private boolean status;

    // empty constructor always needed!
    public StartManager() {
    }

    public StartManager(NicheManagementInterface nicheInstance, ScriptInfo si) {
    }

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
        serviceGroupTemplate.addServerBinding("helloAny", JadeBindInterface.ONE_TO_ANY);
        serviceGroupTemplate.addServerBinding("helloAll", JadeBindInterface.ONE_TO_MANY);
        serviceGroupTemplate.addServerBinding("counter", JadeBindInterface.ONE_TO_MANY);
        GroupId serviceGroup =
            myActuatorInterface.createGroup(serviceGroupTemplate, serviceComponents);

        // Create a one-to-any binding from the front-end to the service group.
        // This binding uses the helloAny interface.
        String clientInterfaceName = "helloAny";
        String serverInterfaceName = "helloAny";
        myActuatorInterface.bind(frontendComponent, clientInterfaceName, serviceGroup,
                                 serverInterfaceName, JadeBindInterface.ONE_TO_ANY);

        // Create a one-to-all binding from the front-end to the service group.
        // This binding uses the helloAll interface.
        clientInterfaceName = "helloAll";
        serverInterfaceName = "helloAll";
        myActuatorInterface.bind(frontendComponent, clientInterfaceName, serviceGroup,
                                 serverInterfaceName, JadeBindInterface.ONE_TO_MANY);
        
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
        
        //bind service supervisor with service components
        clientInterfaceName = "counterStatus";
        serverInterfaceName = "counterStatus";
        //TODO
        myActuatorInterface.bind(serviceGroup, clientInterfaceName, serviceSupervisor, serverInterfaceName, JadeBindInterface.ONE_TO_ONE);

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
    }

    // ///////////////////////////////////////////////////////////////
    // ////////////////////// Fractal Stuff //////////////////////////
    // ///////////////////////////////////////////////////////////////

    public String[] listFc() {
        return new String[] { FractalInterfaceNames.COMPONENT,
                FractalInterfaceNames.OVERLAY_ACCESS, FractalInterfaceNames.ID_REGISTRY };
    }

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

    public String getFcState() {
        return status ? "STARTED" : "STOPPED";
    }

    public void startFc() throws IllegalLifeCycleException {
        status = true;
        startScript();
    }

    public void stopFc() throws IllegalLifeCycleException {
        status = false;
    }
}
