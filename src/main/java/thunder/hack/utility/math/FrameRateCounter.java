package thunder.hack.utility.math;

import java.util.ArrayDeque;

public class FrameRateCounter {
    public static final FrameRateCounter INSTANCE = new FrameRateCounter();
    final ArrayDeque<Long> records = new ArrayDeque<>(512);
    int fps = 5;

    public void recordFrame() {
        long c = System.currentTimeMillis();
        long cutoff = c - 1000;
        records.addLast(c);
        while (!records.isEmpty() && records.peekFirst() < cutoff)
            records.removeFirst();
        fps = Math.max(records.size(), 4);
    }

    public int getFps() {
        return fps;
    }
}
