package calendar.model;

import java.time.LocalDate;

/**
 * Parameter object for date range copy operations.
 * Used for copying events between calendars in date ranges.
 */
public class DateRangeCopyRequest {
  private final LocalDate sourceStartDate;
  private final LocalDate sourceEndDate;
  private final LocalDate targetStartDate;

  /**
   * Constructor for copying events on a single date.
   *
   * @param sourceDate the source date
   * @param targetDate the target date
   */
  public DateRangeCopyRequest(LocalDate sourceDate, LocalDate targetDate) {
    this.sourceStartDate = sourceDate;
    this.sourceEndDate = sourceDate;
    this.targetStartDate = targetDate;
  }

  /**
   * Constructor for copying events in a date range.
   *
   * @param sourceStartDate start of source range
   * @param sourceEndDate end of source range
   * @param targetStartDate start date in target calendar
   */
  public DateRangeCopyRequest(LocalDate sourceStartDate, LocalDate sourceEndDate,
                              LocalDate targetStartDate) {
    this.sourceStartDate = sourceStartDate;
    this.sourceEndDate = sourceEndDate;
    this.targetStartDate = targetStartDate;
  }

  public LocalDate getSourceStartDate() {
    return sourceStartDate;
  }

  public LocalDate getSourceEndDate() {
    return sourceEndDate;
  }

  public LocalDate getTargetStartDate() {
    return targetStartDate;
  }

  public long getDaysDiff() {
    return targetStartDate.toEpochDay() - sourceStartDate.toEpochDay();
  }

  public boolean isSingleDate() {
    return sourceStartDate.equals(sourceEndDate);
  }
}