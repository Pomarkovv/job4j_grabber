package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {
    public static void main(String[] args) throws ClassNotFoundException {
        Properties properties = getProp();
        Class.forName(properties.getProperty("driver-class-name"));
        try (Connection cn = DriverManager.getConnection(
                properties.getProperty("url"),
                properties.getProperty("username"),
                properties.getProperty("password")
        )) {
            try {
                Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
                scheduler.start();
                JobDataMap data = new JobDataMap();
                data.put("cn", cn);
                JobDetail job = newJob(Rabbit.class)
                        .usingJobData(data)
                        .build();
                SimpleScheduleBuilder times = simpleSchedule()
                        .withIntervalInSeconds(Integer.parseInt(properties.getProperty("rabbit.interval")))
                        .repeatForever();
                Trigger trigger = newTrigger()
                        .startNow()
                        .withSchedule(times)
                        .build();
                scheduler.scheduleJob(job, trigger);
                Thread.sleep(10000);
                scheduler.shutdown();
            } catch (SchedulerException | InterruptedException se) {
                se.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();

        }
    }

    public static class Rabbit implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.out.println("Rabbit runs here ...");
            Connection cn = (Connection) context.getJobDetail().getJobDataMap().get("cn");
            try (PreparedStatement statement = cn.prepareStatement(
                    "INSERT INTO rabbit(created_date) VALUES (?)")) {
                statement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                statement.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Properties getProp() {
        Properties properties = new Properties();
        try (InputStream in = AlertRabbit.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }
}