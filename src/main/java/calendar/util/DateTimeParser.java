package calendar.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility class for parsing and formatting date and time strings.
 */
public class DateTimeParser {

  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter TIME_FORMATTER =
      DateTimeFormatter.ofPattern("HH:mm");
  private static final DateTimeFormatter DATETIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");


  private DateTimeParser() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  /**
   * Parses a date string in format YYYY-MM-DD.
   *
   * @param dateString the date string to parse (e.g., "2025-11-15")
   * @return LocalDate object
   * @throws IllegalArgumentException if format is invalid
   */
  public static LocalDate parseDate(String dateString) {
    if (dateString == null || dateString.trim().isEmpty()) {
      throw new IllegalArgumentException("Date string cannot be null or empty");
    }

    try {
      return LocalDate.parse(dateString.trim(), DATE_FORMATTER);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException(
          "Invalid date format: '" + dateString + "'. Expected: YYYY-MM-DD (e.g., 2025-11-15)");
    }
  }

  /**
   * Parses a date-time string in format YYYY-MM-DDTHH:mm.
   *
   * @param dateTimeString the date-time string to parse (e.g., "2025-11-15T10:00")
   * @return LocalDateTime object
   * @throws IllegalArgumentException if format is invalid
   */
  public static LocalDateTime parseDateTime(String dateTimeString) {
    if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
      throw new IllegalArgumentException("DateTime string cannot be null or empty");
    }

    try {
      return LocalDateTime.parse(dateTimeString.trim(), DATETIME_FORMATTER);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException(
          "Invalid date-time format: '" + dateTimeString
              + "'. Expected: YYYY-MM-DDTHH:mm (e.g., 2025-11-15T10:00)");
    }
  }

  /**
   * Parses a time string in format HH:mm.
   * Note: Currently unused but kept for API completeness and future use.
   *
   * @param timeString the time string to parse (e.g., "10:00")
   * @return LocalTime object
   * @throws IllegalArgumentException if format is invalid
   */
  public static LocalTime parseTime(String timeString) {
    if (timeString == null || timeString.trim().isEmpty()) {
      throw new IllegalArgumentException("Time string cannot be null or empty");
    }

    try {
      return LocalTime.parse(timeString.trim(), TIME_FORMATTER);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException(
          "Invalid time format: '" + timeString + "'. Expected: HH:mm (e.g., 10:00)");
    }
  }

  /**
   * Combines a date and time string into LocalDateTime.
   *
   * @param dateString date in format YYYY-MM-DD
   * @param timeString time in format HH:mm
   * @return LocalDateTime object
   * @throws IllegalArgumentException if format is invalid
   */
  public static LocalDateTime combineDateTime(String dateString, String timeString) {
    LocalDate date = parseDate(dateString);
    LocalTime time = parseTime(timeString);
    return LocalDateTime.of(date, time);
  }

  /**
   * Formats a LocalDateTime to string in standard format.
   *
   * @param dateTime the LocalDateTime to format
   * @return formatted string (YYYY-MM-DDTHH:mm)
   */
  public static String formatDateTime(LocalDateTime dateTime) {
    if (dateTime == null) {
      throw new IllegalArgumentException("DateTime cannot be null");
    }
    return dateTime.format(DATETIME_FORMATTER);
  }

  /**
   * Formats a LocalDate to string in standard format.
   *
   * @param date the LocalDate to format
   * @return formatted string (YYYY-MM-DD)
   */
  public static String formatDate(LocalDate date) {
    if (date == null) {
      throw new IllegalArgumentException("Date cannot be null");
    }
    return date.format(DATE_FORMATTER);
  }

  /**
   * Formats a LocalTime to string in standard format.
   * Note: Currently unused but kept for API completeness and future use.
   *
   * @param time the LocalTime to format
   * @return formatted string (HH:mm)
   */
  public static String formatTime(LocalTime time) {
    if (time == null) {
      throw new IllegalArgumentException("Time cannot be null");
    }
    return time.format(TIME_FORMATTER);
  }

  /**
   * Validates if a string is a valid date format.
   * Useful for pre-validation before parsing.
   *
   * @param dateString the date string to validate
   * @return true if valid, false otherwise
   */
  public static boolean isValidDate(String dateString) {
    try {
      parseDate(dateString);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  /**
   * Validates if a string is a valid date-time format.
   * Useful for pre-validation before parsing.
   *
   * @param dateTimeString the date-time string to validate
   * @return true if valid, false otherwise
   */
  public static boolean isValidDateTime(String dateTimeString) {
    try {
      parseDateTime(dateTimeString);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  /**
   * Validates if a string is a valid time format.
   * Useful for pre-validation before parsing.
   *
   * @param timeString the time string to validate
   * @return true if valid, false otherwise
   */
  public static boolean isValidTime(String timeString) {
    try {
      parseTime(timeString);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}