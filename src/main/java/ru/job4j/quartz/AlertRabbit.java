package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {
    public static void main(String[] args) {
        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDataMap data = new JobDataMap();
            data.put("dbProperties", loadProperties());
            JobDetail job = newJob(Rabbit.class)
                    .usingJobData(data)
                    .build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(getInterval())
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10000);
            scheduler.shutdown();
        } catch (SchedulerException | InterruptedException se) {
            throw new RuntimeException(se);
        }
    }

    public static class Rabbit implements Job {
        @Override
        public void execute(JobExecutionContext context) {
            System.out.println("Rabbit runs here ...");
            Properties properties = (Properties) context.getJobDetail().getJobDataMap().get("dbProperties");
            try (Connection connection = DriverManager.getConnection(
                    properties.getProperty("url"),
                    properties.getProperty("username"),
                    properties.getProperty("password"));
                 PreparedStatement ps = connection.prepareStatement(
                         "INSERT INTO rabbit (created_date) VALUES (?)")) {
                ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error connecting to database", e);
            }
        }
    }

    private static int getInterval() {
        Properties properties = loadProperties();
        return Integer.parseInt(properties.getProperty("rabbit.interval"));
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream is = AlertRabbit.class.getClassLoader()
                .getResourceAsStream("rabbit.properties")) {
            properties.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Error loading properties file", e);
        }
        return properties;
    }
}
