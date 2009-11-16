package yacs;

import dks.niche.fractal.interfaces.InitInterface;
import dks.niche.interfaces.NicheActuatorInterface;

import yacs.utils.YacsTimer;

/**
 * For stuff common to YACS management elements but not to functional components.
 * @author LTDATH
 */
// TODO: put more common stuff here, like isActiveReplica logic
public abstract class YacsManagementElement extends YacsComponent implements InitInterface {
	
	// only one ME is/should be active at any given time
	protected boolean activeReplica = true;
	
	protected String abnormalReinit = null;
	protected int isReinited = 0;
	
	protected boolean initArrayCalled=false;
	protected boolean reinitCalled=false;
	protected boolean initActuatorCalled=false;
	protected boolean initIdCalled=false;
	
	private YacsTimer initTimer = null; // should init in Constructor maybe?
	private long tuid;
	
	protected synchronized boolean areInitCallsComplete(){
		return initActuatorCalled && initIdCalled &&
					(initArrayCalled || reinitCalled);
	}
	public void init(Object[] parameters){
		createInitTimer();
		setInitArrayCalled( true );
		doInit( parameters );
		if( areInitCallsComplete() )
			doInitCallsPostprocessing_();
	}
	public void reinit(Object[] parameters){
		createInitTimer();
		setReinitCalled( true );
		doReinit( parameters );
		if( areInitCallsComplete() )
			doInitCallsPostprocessing_();
	}
	public void init(NicheActuatorInterface actuator){
		createInitTimer();
		setInitActuatorCalled( true );
		doInit( actuator );
		if( areInitCallsComplete() )
			doInitCallsPostprocessing_();
	}
	public void initId(Object id){
		createInitTimer();
		setInitIdCalled( true );
		doInitId( id );
		if( areInitCallsComplete() )
			doInitCallsPostprocessing_();		
	}
	
	protected abstract void doInit(Object[] parameters);
	protected abstract void doReinit(Object[] parameters);
	protected abstract void doInit(NicheActuatorInterface actuator);
	protected abstract void doInitId(Object id);
	protected void doInitCallsPostprocessing(){
		log( "doInitCallsPostprocessing" );
	}
	
	private synchronized void createInitTimer(){
		if( initTimer == null ) 
			initTimer = new YacsTimer( tuid++ );
	}
	private void doInitCallsPostprocessing_(){
		log( "doInitCallsPostprocessing_" );
		doInitCallsPostprocessing();
		timefx("YME",""+initTimer.getTtid(),initTimer.getTtid(),"MEIT",null,initTimer.elapsed(),null); // Management-Element Init Time
	}
	
	
	// for reinit
	/**
	 * Order of init events is not guaranteed so the logger might not be initialized in REinit.
	 * I don't want to miss logging such an important event so I note it happening and log at the earliest opportunity 
	 */
	protected void logReinit(){
		if( isReinited == 1 ){
			isReinited = 2;
			if( abnormalReinit != null )
				log("Component is reinited abnormally: " + abnormalReinit);
			else
				log("Component is reinited.");
		}
	}
	
	// GETTERS and SETTERS
	public void setReinited( boolean isReinited ){
		this.isReinited = isReinited ? 1 : 0;
	}
	public boolean isReinited(){ return isReinited > 0; }
	
	public boolean isAbnormalReinit() {
		return abnormalReinit == null;
	}
	public void setAbnormalReinit(String abnormalityDesc ) {
		this.abnormalReinit = abnormalityDesc;
	}

	// for active replica logic
	public boolean isActiveReplica() {
		return activeReplica;
	}
	public void setActiveReplica(boolean isActiveReplica) {
		this.activeReplica = isActiveReplica;
	}
	
	// for init calls
	public boolean isInitArrayCalled() {
		return initArrayCalled;
	}
	public void setInitArrayCalled(boolean initArrayCalled) {
		this.initArrayCalled = initArrayCalled;
	}
	public boolean isReinitCalled() {
		return reinitCalled;
	}
	public void setReinitCalled(boolean reinitCalled) {
		this.reinitCalled = reinitCalled;
	}
	public boolean isInitActuatorCalled() {
		return initActuatorCalled;
	}
	public void setInitActuatorCalled(boolean initActuatorCalled) {
		this.initActuatorCalled = initActuatorCalled;
	}
	public boolean isInitIdCalled() {
		return initIdCalled;
	}
	public void setInitIdCalled(boolean initIdCalled) {
		this.initIdCalled = initIdCalled;
	}
}
