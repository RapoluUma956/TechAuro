/*
 * Decompiled with CFR 0.152.
 */
package org.example.agent.model;

public class PerformanceInfo {
    private Integer processCount;
    private Integer threadCount;

    public Integer getProcessCount() {
        return this.processCount;
    }

    public void setProcessCount(Integer processCount) {
        this.processCount = processCount;
    }

    public Integer getThreadCount() {
        return this.threadCount;
    }

    public void setThreadCount(Integer threadCount) {
        this.threadCount = threadCount;
    }

    public String toString() {
        return "PerformanceInfo{processCount=" + this.processCount + ", threadCount=" + this.threadCount + "}";
    }
}

