package utils;

public class Timer {
	private long start_time;
	private long stop_time;
	private boolean is_running;
	
	public Timer() {
		this.restart();
	}
	
	public void start() {
		this.start_time = currentTime();
		this.is_running = true;
	}
	
	public void stop() {
		this.stop_time = currentTime();
		this.is_running = false;
	}
	
	public void restart() {
		this.start_time = 0;
		this.stop_time = 0;
		this.is_running = false;
	}
	
	public long getElapsedTime() {
		if(isRunning()) {
			return calculateElapsedTime(this.start_time, currentTime());
		} else if(start_time != 0) {
			return calculateElapsedTime(this.start_time, this.stop_time);
		}
		return 0;
	}
	
	private long currentTime() {
		return System.currentTimeMillis();
	}
	
	private boolean isRunning() {
		return this.is_running;
	}
	
	private long calculateElapsedTime(long start_time, long end_time) {
		return end_time - start_time;
	}
}
