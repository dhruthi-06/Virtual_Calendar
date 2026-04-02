package calendar.model;

import java.time.LocalDateTime;

/**
 * Parameter object for recurring event creation requests.
 * Eliminates long parameter lists (6 parameters reduced to 1).
 * Extracted to fix Long Parameter List code smell.
 */
public class RecurringEventCreationRequest {
  private final String subject;
  private final LocalDateTime start;
  private final LocalDateTime end;
  private final String weekdays;
  private final Integer repeatCount;
  private final LocalDateTime repeatUntil;

  /**
   * Private constructor - use Builder.
   */
  private RecurringEventCreationRequest(Builder builder) {
    this.subject = builder.subject;
    this.start = builder.start;
    this.end = builder.end;
    this.weekdays = builder.weekdays;
    this.repeatCount = builder.repeatCount;
    this.repeatUntil = builder.repeatUntil;
  }

  public String getSubject() {
    return subject;
  }

  public LocalDateTime getStart() {
    return start;
  }

  public LocalDateTime getEnd() {
    return end;
  }

  public String getWeekdays() {
    return weekdays;
  }

  /**
   * Gets the number of times the event should repeat.
   *
   * @return the repeat count, or null if repeatUntil is set.
   */
  public Integer getRepeatCount() {
    return repeatCount;
  }

  /**
   * Gets the date until which the event should repeat.
   *
   * @return the repeat until date, or null if repeatCount is set.
   */
  public LocalDateTime getRepeatUntil() {
    return repeatUntil;
  }

  /**
   * Checks if the request specifies a repeat count (number of occurrences).
   *
   * @return true if repeatCount is set, false otherwise.
   */
  public boolean hasRepeatCount() {
    return repeatCount != null;
  }

  /**
   * Checks if the request specifies a repeat until date.
   *
   * @return true if repeatUntil is set, false otherwise.
   */
  public boolean hasRepeatUntil() {
    return repeatUntil != null;
  }

  /**
   * Builder for RecurringEventCreationRequest.
   */
  public static class Builder {
    private final String subject;
    private final LocalDateTime start;
    private final LocalDateTime end;
    private final String weekdays;
    private Integer repeatCount = null;
    private LocalDateTime repeatUntil = null;

    /**
     * Constructor with required fields.
     *
     * @param subject event subject
     * @param start start date/time
     * @param end end date/time
     * @param weekdays weekday pattern
     */
    public Builder(String subject, LocalDateTime start, LocalDateTime end, String weekdays) {
      this.subject = subject;
      this.start = start;
      this.end = end;
      this.weekdays = weekdays;
    }

    /**
     * Sets the total number of times the event should occur.
     *
     * @param repeatCount the number of times to repeat.
     * @return this Builder instance.
     */
    public Builder repeatCount(int repeatCount) {
      this.repeatCount = repeatCount;
      return this;
    }

    /**
     * Sets the date/time until which the event should repeat.
     *
     * @param repeatUntil the date/time the recurrence should stop.
     * @return this Builder instance.
     */
    public Builder repeatUntil(LocalDateTime repeatUntil) {
      this.repeatUntil = repeatUntil;
      return this;
    }

    /**
     * Creates the immutable RecurringEventCreationRequest object.
     *
     * @return the new RecurringEventCreationRequest.
     */
    public RecurringEventCreationRequest build() {
      return new RecurringEventCreationRequest(this);
    }
  }
}