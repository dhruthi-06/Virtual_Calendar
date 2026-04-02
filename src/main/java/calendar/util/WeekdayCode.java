package calendar.util;

import java.time.DayOfWeek;

/**
 * Enum representing weekday codes for recurring events.
 * Eliminates primitive obsession with string codes.
 * Each weekday is represented by a single-letter code:
 * - M = Monday
 * - T = Tuesday
 * - W = Wednesday
 * - R = Thursday (R to avoid confusion with T for Tuesday)
 * - F = Friday
 * - S = Saturday
 * - U = Sunday (U to avoid confusion with S for Saturday)
 * This enum provides type-safe handling of weekday patterns used in
 * recurring events (e.g., "MWF" for Monday-Wednesday-Friday).
 */
public enum WeekdayCode {
  MONDAY("M", DayOfWeek.MONDAY),
  TUESDAY("T", DayOfWeek.TUESDAY),
  WEDNESDAY("W", DayOfWeek.WEDNESDAY),
  THURSDAY("R", DayOfWeek.THURSDAY),
  FRIDAY("F", DayOfWeek.FRIDAY),
  SATURDAY("S", DayOfWeek.SATURDAY),
  SUNDAY("U", DayOfWeek.SUNDAY);

  private final String code;
  private final DayOfWeek dayOfWeek;

  /**
   * Constructs a WeekdayCode.
   *
   * @param code the single-letter code
   * @param dayOfWeek the corresponding Java DayOfWeek
   */
  WeekdayCode(String code, DayOfWeek dayOfWeek) {
    this.code = code;
    this.dayOfWeek = dayOfWeek;
  }

  /**
   * Gets the single-letter code for this weekday.
   *
   * @return the code (M, T, W, R, F, S, or U)
   */
  public String getCode() {
    return code;
  }

  /**
   * Gets the Java DayOfWeek corresponding to this weekday.
   *
   * @return the DayOfWeek
   */
  public DayOfWeek getDayOfWeek() {
    return dayOfWeek;
  }

  /**
   * Gets a WeekdayCode from a string code.
   * Case-insensitive.
   *
   * @param code the code string (M, T, W, R, F, S, or U)
   * @return the corresponding WeekdayCode
   * @throws IllegalArgumentException if code is invalid
   */
  public static WeekdayCode fromCode(String code) {
    if (code == null || code.trim().isEmpty()) {
      throw new IllegalArgumentException("Weekday code cannot be null or empty");
    }

    for (WeekdayCode wc : values()) {
      if (wc.code.equalsIgnoreCase(code.trim())) {
        return wc;
      }
    }
    throw new IllegalArgumentException("Invalid weekday code: " + code
        + ". Valid codes: M, T, W, R, F, S, U");
  }

  /**
   * Gets a WeekdayCode from a Java DayOfWeek.
   *
   * @param day the day of week
   * @return the corresponding WeekdayCode
   * @throws IllegalStateException if day is unknown (should never happen)
   */
  public static WeekdayCode fromDayOfWeek(DayOfWeek day) {
    if (day == null) {
      throw new IllegalArgumentException("DayOfWeek cannot be null");
    }

    for (WeekdayCode wc : values()) {
      if (wc.dayOfWeek == day) {
        return wc;
      }
    }
    throw new IllegalStateException("Unknown day of week: " + day);
  }

  /**
   * Gets the full name of this weekday.
   *
   * @return the weekday name (e.g., "Monday", "Tuesday")
   */
  public String getFullName() {
    switch (this) {
      case MONDAY:
        return "Monday";
      case TUESDAY:
        return "Tuesday";
      case WEDNESDAY:
        return "Wednesday";
      case THURSDAY:
        return "Thursday";
      case FRIDAY:
        return "Friday";
      case SATURDAY:
        return "Saturday";
      case SUNDAY:
        return "Sunday";
      default:
        return this.name();
    }
  }

  /**
   * Gets the abbreviated name of this weekday.
   *
   * @return the abbreviated name (e.g., "Mon", "Tue")
   */
  public String getAbbreviation() {
    return getFullName().substring(0, 3);
  }

  /**
   * Checks if this weekday is a weekend day.
   *
   * @return true if Saturday or Sunday
   */
  public boolean isWeekend() {
    return this == SATURDAY || this == SUNDAY;
  }

  /**
   * Checks if this weekday is a weekday (Monday-Friday).
   *
   * @return true if Monday through Friday
   */
  public boolean isWeekday() {
    return !isWeekend();
  }

  @Override
  public String toString() {
    return code;
  }
}