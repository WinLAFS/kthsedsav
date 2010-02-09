package se.kth.ict.id2203.pfd.components;

import se.kth.ict.id2203.pfd.events.CheckTimeoutEvent;
import se.kth.ict.id2203.pfd.events.HeartbeatTimeoutEvent;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timer;

public class PerfectFailureDetector extends ComponentDefinition {
	
	long Delta = 1000;
	long Gamma = 4000;
	
//	private Positive<FailureDetectorPort> fpPositive = positive(FailureDetectorPort.class);
	Positive<Timer> timerHeartbeat = positive(Timer.class);
	  {
	    // scheduling a timeout
		System.out.println(">>");
	    ScheduleTimeout st = new ScheduleTimeout(Gamma);
	    st.setTimeoutEvent(new HeartbeatTimeoutEvent(st));
	    trigger(st, timerHeartbeat);
	  }
//	  Positive<Timer> timerCheck = positive(Timer.class);
//	  {
//	    // scheduling a timeout
//	    ScheduleTimeout st = new ScheduleTimeout(Gamma + Delta);
//	    st.setTimeoutEvent(new CheckTimeoutEvent(st));
//	    trigger(st, timerCheck);
//	  }
	  
	public PerfectFailureDetector() {
		System.out.println("yes");
		subscribe(hbHandler, timerHeartbeat);
//		subscribe(ctHandler, timerCheck);
	}
	
	Handler<HeartbeatTimeoutEvent> hbHandler = new Handler<HeartbeatTimeoutEvent>() {

		public void handle(HeartbeatTimeoutEvent arg0) {
			System.out.println(">>Heartbeat");
		}
	};
	
	Handler<CheckTimeoutEvent> ctHandler = new Handler<CheckTimeoutEvent>() {
		
		public void handle(CheckTimeoutEvent arg0) {
			System.out.println(">>CheckTimeout");
		}
	};
	
}
