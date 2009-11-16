package yacs.frontend.gmovie;

import java.awt.*;

import java.util.*;

public class TaskCanvas extends Canvas {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Vector<TaskDisplay> tasks;
	
	public TaskCanvas( Vector<TaskDisplay> tasks ){
		this.tasks = tasks;
	}

	public void paint( Graphics g ){
		if( tasks.size() == 0 ){
			g.drawRect(0,0, this.getWidth()-1, this.getHeight()-1);
			return;
		}
		
		int tw = (this.getWidth()-1) / tasks.size();
		
		if( tw > 20 )
			tw = 20;
		
		int tx = 0;
		for( TaskDisplay task : tasks ){
			task.setBounds( tx, 0, 
							tw-2, this.getHeight()-1); // 2 pixlar á milli
			task.paint( g );
			tx += tw;
		}
	}
}
