package ru.job4j.grabber.quartz;

import org.quartz.SchedulerException;

public interface Grab {
    void init() throws SchedulerException;
}
