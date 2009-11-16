package yacs.resources.data;

import java.io.Serializable;
import java.util.*;

import yacs.interfaces.YACSNames;

import yacs.resources.data.*;

/**
 * Class for storing and transmitting system resource state. Used by ResourceService components
 * @author LTDATH
 */
public class ResourceState implements Serializable {

	private static final long serialVersionUID = 1L;

	// main container
	private Hashtable<String,ResourceHolder> resources = new Hashtable<String,ResourceHolder>();
	
	private ArrayList<ResourceHolder> workers = new ArrayList<ResourceHolder>();
	private ArrayList<ResourceHolder> masters = new ArrayList<ResourceHolder>();
	private ArrayList<ResourceHolder> resourceServices = new ArrayList<ResourceHolder>();
	
	private boolean insert( ResourceInfo info ){
		String id = info.getComponentId().getId().toString();
		
		if( resources.containsKey(id) ){
			return this.update(info);
		}
		ResourceHolder newResource = new ResourceHolder( id, info );
		
		resources.put( id, newResource );
		if( info.getComponentType().equals(YACSNames.WORKER_COMPONENT) )
			workers.add( newResource );
		else if( info.getComponentType().equals(YACSNames.MASTER_COMPONENT) )
			masters.add( newResource );
		else
			resourceServices.add( newResource );
		
		return true;
	}
	public boolean update( ResourceInfo info ){
		String id = info.getComponentId().getId().toString();
		
		if( !resources.containsKey(id) ){
			return this.insert(info);
		}
		
		ResourceHolder current = resources.get(id);
		
		// do I have more recent information?
		if( current.getResource().isNewerThan(info) )
			return false;
		
		long lastHashCode = current.getResource().hashCode();
		long lastUpdate = current.getResource().getUpdateTime();
		
		current.setResource( info );

		return true;
	}
	public boolean remove( ResourceInfo info ){
		String id = info.getComponentId().getId().toString();
		
		if( resources.containsKey(id) )
		{
			ResourceInfo held = resources.get(id).getResource();
			
			// do I, for some unforeseen reason, have more recent information about this resource?
			if( held.isNewerThan(info) ){
				return false;
			}
			
			ResourceHolder rem = resources.remove(id);
			masters.remove( rem );
			workers.remove( rem );
			resourceServices.remove( rem );
						
			return true;
		}
		else {
			return false;
		}
	}
	
	/*private void removeFromList( ResourceHolder rem, ArrayList<ResourceHolder> list ){
		int i=-1;
		for( int a=0; a<list.size(); a++ ){
			ResourceHolder held = list.get(a);
			if( rem.getId().equals(held.getId()) ){
				list.remove(a);
				return;
			}
		}
	}*/
	
	public boolean merge( ResourceState other ){
		
		ArrayList<ResourceInfo> stateChanges = new ArrayList<ResourceInfo>();
		
		compareLists( other.getResourceServices(), stateChanges );
		compareLists( other.getMasters(), stateChanges );
		compareLists( other.getWorkers(), stateChanges );
		
		for( ResourceInfo change : stateChanges ){
			this.update(change);
		}
		return stateChanges.size() > 0 ? true : false;
	}
	private void compareLists( ArrayList<ResourceHolder> other, ArrayList<ResourceInfo> stateChanges ){
		for( ResourceHolder holder : other ){
			String id = holder.getResource().getComponentId().toString();
			
			if( !resources.containsKey(id) )
				stateChanges.add( holder.getResource() );
			else {
				ResourceHolder current = resources.get(id);
				if( !current.getResource().isNewerThan(holder.getResource()) )
					stateChanges.add(holder.getResource());
			}
		}
	}
	
	// getters
	
	public Hashtable<String, ResourceHolder> getResources() {
		return resources;
	}
	public ArrayList<ResourceHolder> getWorkers() {
		return workers;
	}
	public ArrayList<ResourceHolder> getMasters() {
		return masters;
	}
	public ArrayList<ResourceHolder> getResourceServices() {
		return resourceServices;
	}
	
	// helpers
	// debuggers
	public String state2String(){
		StringBuffer str = new StringBuffer();
		
		str.append("RSs:"+System.getProperty("line.separator"));
		str.append( list2String(this.resourceServices) );
		
		str.append("Masters:"+System.getProperty("line.separator"));
		str.append( list2String(this.masters) );
		
		str.append("Workers:"+System.getProperty("line.separator"));
		str.append( list2String(this.workers) );	
		
		return str.toString();
	}
	private String list2String( ArrayList<ResourceHolder> list ){
		StringBuffer ret = new StringBuffer();
		
		for( ResourceHolder holder : list ){
			ResourceInfo res = holder.getResource(); 
			ret.append( res.getComponentId().getId() + "\t" + res.getComponentType() + "\t" + res.getCreationTime() + "\t" + res.getUpdateTime() );
			ret.append( System.getProperty("line.separator") );
		}
		
		return ret.toString();
	}
	
}
