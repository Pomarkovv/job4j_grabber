package ru.job4j.grabber.utils;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public class HabrCareerDateTimeParser implements DateTimeParser {

    @Override
    public LocalDateTime parse(String parse) {
        return LocalDateTime.from(ZonedDateTime.parse(parse));
    }

}