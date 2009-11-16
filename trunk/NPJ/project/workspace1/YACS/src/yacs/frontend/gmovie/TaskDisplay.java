package yacs.frontend.gmovie;

import java.awt.*;

import yacs.interfaces.YACSNames;

public class TaskDisplay {
	
	private int x, y, width, height;
	
	private int state;
	private int result;
	private String worker;
	private int restores = 0; 
	
	public TaskDisplay() {
		super();
		this.state = YACSNames.TASK_NOT_INITIALIZED;
		this.result = YACSNames.RESULT_OK;
	}
	
	public void setBounds(int x, int y, int width, int height){
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;		
	}

	public void paint( Graphics g ){
		//g.drawRect( x, y, width, height);
		Color prev = g.getColor();
		
		Color c = Color.WHITE;
		switch( state ){
			case YACSNames.TASK_NOT_INITIALIZED: 	
				c = (worker==null ? Color.WHITE : Color.YELLOW); 
				break;
			case YACSNames.TASK_IS_PROCESSING: 		c=Color.ORANGE; break;
			case YACSNames.TASK_COMPLETED: 			c=Color.GREEN; break;
			case YACSNames.TASK_FAILED: 			c=Color.RED; break;
		}
		
		g.setColor(c);
		g.fillRect(x, y, width, height);
		
		if( state == YACSNames.TASK_COMPLETED && result != YACSNames.RESULT_OK){
			g.setColor(Color.RED);
			
			// draw a miniature rect within the big one
			// move 25% to the right, move 25% down, draw 50% to right, 50% down
			/*g.fillRect( x+(x/4),
						y+(y/4), 
						width/2, height/2 );*/
			g.drawLine(x, y, x+width, y+height);
			g.drawLine(x+width, y, x, y+height);
			
		}
		
		if( restores > 0 ){
			g.setColor( Color.BLACK );
			//g.drawLine( x, y+restores, x+restores, y );
			g.drawLine(x, y, x+width, y+height);
			g.drawLine(x+width, y, x, y+height);
			
			g.setColor( c );
			g.fillRect( x+(width/4), y+(height/4), width/2, height/2 );
			
			g.setColor( Color.BLACK );
			g.drawString( String.valueOf(restores), x+(width/3), y+((int)(height*0.75))-2 );
		}
		
		g.setColor(prev);
	}
	
	// getters and setters
	public void setState( int state ){
		this.state = state;
	}
	public void setResult( int result ){
		this.result = result;
	}
	public void setWorker( String worker ){
		if( this.worker != null && !this.worker.equals(worker) )
			restores++;
		this.worker = worker;
	}
	
	public int getRestores(){
		return restores;
	}
}
