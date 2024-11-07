package ru.job4j.grabber.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class HabrCareerDateTimeParserTest {
    @Test
    void whenParseIsoDateTimeWithOffsetThenReturnLocalDateTime() {
        HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
        String dateString = "2024-11-01T16:00:12+03:00";
        LocalDateTime expectedDateTime = LocalDateTime
                .of(2024, 11, 1, 16, 0, 12);
        LocalDateTime result = parser.parse(dateString);
        assertThat(result).isEqualTo(expectedDateTime);
    }

    @Test
    void whenParseDifferentOffsetThenReturnCorrectLocalDateTime() {
        HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
        String dateString = "2024-12-25T10:30:45-05:00";
        LocalDateTime expectedDateTime = LocalDateTime
                .of(2024, 12, 25, 10, 30, 45);
        LocalDateTime result = parser.parse(dateString);
        assertThat(result).isEqualTo(expectedDateTime);
    }

    @Test
    void whenParseUtcDateTimeThenReturnLocalDateTime() {
        HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
        String dateString = "2024-11-01T16:00:12Z";
        LocalDateTime expectedDateTime = LocalDateTime
                .of(2024, 11, 1, 16, 0, 12);
        LocalDateTime result = parser.parse(dateString);
        assertThat(result).isEqualTo(expectedDateTime);
    }
}