package com.evgeniysharafan.takephoto.util.lib;

import java.util.ArrayList;
import java.util.List;

public final class TimeLogger {

    private String label;
    private List<Long> intervals = new ArrayList<>();
    private long start;

    public TimeLogger() {
        this("");
    }

    public TimeLogger(String label) {
        this.label = label;
    }

    public void start() {
        start = System.nanoTime();
    }

    public void finish() {
        long timeNs = System.nanoTime() - start;
        L.w(label + " time = " + (float) timeNs / 1000000 + " ms");
    }

    public void finishInterval(boolean needOneShotResult) {
        intervals.add(System.nanoTime() - start);

        if (needOneShotResult) {
            long timeNs = System.nanoTime() - start;
            L.w(label + " shot time = " + (float) timeNs / 1000000 + " ms");
        }
    }

    public void resultIntervals() {
        long timeNs = 0;
        for (Long interval : intervals) {
            timeNs += interval;
        }
        timeNs = timeNs / intervals.size();

        L.e(label + " time = " + (float) timeNs / 1000000 + " ms");
    }

    public void resetIntervals() {
        intervals.clear();
    }

}
