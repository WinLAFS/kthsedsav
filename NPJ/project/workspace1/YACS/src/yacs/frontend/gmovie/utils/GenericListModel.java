package yacs.frontend.gmovie.utils;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import java.util.*;

public class GenericListModel implements ListModel {
	
	private ArrayList<ListDataListener> listeners = new ArrayList<ListDataListener>();
	private ArrayList<Object> values = new ArrayList<Object>();
	private Hashtable<String,Object> valuemap = new Hashtable<String,Object>();

	@Override
	public void addListDataListener(ListDataListener l) {
		listeners.add( l );
	}

	@Override
	public Object getElementAt(int index) {
		return values.get( index );
	}

	@Override
	public int getSize() {
		return values.size();
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		listeners.remove( l );
	}
	
	public boolean addValue( Object object ){
		if( object == null )
			return addValue( ""+System.currentTimeMillis(), "NULL" );
		else
			return addValue( object.toString(), object );
	}
	
	public boolean addValue( String key, Object object ){
		
		values.add( object );
		valuemap.put( key, object );
        
        ListDataEvent event = new ListDataEvent(this,ListDataEvent.INTERVAL_ADDED, values.size()-1, values.size()-1);
        
        Iterator<ListDataListener> itr = listeners.iterator();
        while( itr.hasNext() )
        {
            itr.next().intervalAdded(event);
        }
                
        return true;
	}
	public boolean removeValue( String key ){
		
		Object object = valuemap.get(key);
		if( object == null )
			return false;
		int index = values.indexOf(object);
        
		values.remove( object );
		valuemap.remove(key);
        
        ListDataEvent event = new ListDataEvent( this, ListDataEvent.INTERVAL_REMOVED, index, index );
        Iterator<ListDataListener> itr = listeners.iterator();
        while( itr.hasNext() )
        {
            itr.next().intervalRemoved(event);
        }
                
        return true;
		
	}

	public void clear()
	{
		int count = values.size();
		if( count == 0 ){
			valuemap.clear();
			return;
		}
		
		values.clear();
		valuemap.clear();
		
		ListDataEvent event = new ListDataEvent(this,ListDataEvent.INTERVAL_REMOVED, 0, count-1);
        
        Iterator<ListDataListener> itr = listeners.iterator();
        while( itr.hasNext() )
        {
            itr.next().intervalAdded(event);
        }
	}
	
	public Object getByKey( Object key ){
		return valuemap.get( key );
	}
}
