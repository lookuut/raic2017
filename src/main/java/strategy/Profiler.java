package strategy;

public class Profiler {

    private long startTime;
    private long endTime;

    public Profiler() {
        startTime = 0;
        endTime = 0;
    }


    public void startProfile () {
        startTime = System.currentTimeMillis();
    }

    public void endProfile() {
        endTime = System.currentTimeMillis();
    }

    public long getMilliseconds() {
        return endTime - startTime;
    }
}
