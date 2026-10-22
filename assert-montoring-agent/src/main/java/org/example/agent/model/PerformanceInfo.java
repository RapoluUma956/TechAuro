package org.example.agent.model;

public class PerformanceInfo {

    private Integer processCount;
    private Integer threadCount;

    public Integer getProcessCount() {
        return processCount;
    }

    public void setProcessCount(Integer processCount) {
        this.processCount = processCount;
    }

    public Integer getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(Integer threadCount) {
        this.threadCount = threadCount;
    }

    @Override
    public String toString() {
        return "PerformanceInfo{" +
                "processCount=" + processCount +
                ", threadCount=" + threadCount +
                '}';
    }
}