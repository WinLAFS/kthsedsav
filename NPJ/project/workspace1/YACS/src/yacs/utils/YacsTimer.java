package yacs.utils;

public class YacsTimer {
	
	private long ttid;
	private long start;
	
	public YacsTimer(){}
	public YacsTimer( long ttid ){
		this.ttid = ttid;
		this.start = System.currentTimeMillis();
	}
	
	public void reset( long ttid ){ this.ttid = ttid; start=System.currentTimeMillis(); }
	public void reset(){ start=System.currentTimeMillis(); }
	public long elapsed(){ return System.currentTimeMillis()-start; }

	public long getTtid() {
		return ttid;
	}
	public void setTtid(long ttid) {
		this.ttid = ttid;
	}

	public long getStart() {
		return start;
	}
	public void setStart(long start) {
		this.start = start;
	}
}
