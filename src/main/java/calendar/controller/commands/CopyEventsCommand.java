package calendar.controller.commands;

import calendar.model.DateRangeCopyRequest;
import calendar.model.InterfaceCalendarSystem;
import calendar.view.InterfaceCalendarView;
import java.time.LocalDate;

/**
 * Command to copy events from one calendar to another.
 * Uses DateRangeCopyRequest parameter object.
 */
public class CopyEventsCommand extends BaseCommand {

  private final DateRangeCopyRequest copyRequest;
  private final String targetCalendar;

  /**
   * Builder class for CopyEventsCommand.
   */
  public static class Builder {
    private final LocalDate sourceDate;
    private final String targetCalendar;
    private final LocalDate targetDate;
    private LocalDate sourceEndDate = null;

    /**
     * Constructs a Builder with required parameters.
     *
     * @param sourceDate the source date to copy from
     * @param targetCalendar the target calendar to copy to
     * @param targetDate the target date to copy to
     */
    public Builder(LocalDate sourceDate, String targetCalendar, LocalDate targetDate) {
      this.sourceDate = sourceDate;
      this.targetCalendar = targetCalendar;
      this.targetDate = targetDate;
    }

    /**
     * Sets the source end date for copying a date range.
     *
     * @param sourceEndDate the end date of the source range
     * @return this builder
     */
    public Builder sourceEndDate(LocalDate sourceEndDate) {
      this.sourceEndDate = sourceEndDate;
      return this;
    }

    /**
     * Builds the CopyEventsCommand.
     *
     * @return a new CopyEventsCommand instance
     */
    public CopyEventsCommand build() {
      return new CopyEventsCommand(this);
    }
  }

  private CopyEventsCommand(Builder builder) {
    if (builder.sourceEndDate == null) {
      // Single date copy
      this.copyRequest = new DateRangeCopyRequest(builder.sourceDate, builder.targetDate);
    } else {
      // Date range copy
      this.copyRequest = new DateRangeCopyRequest(
          builder.sourceDate, builder.sourceEndDate, builder.targetDate);
    }
    this.targetCalendar = builder.targetCalendar;
  }

  /**
   * Constructs a CopyEventsCommand with all parameters.
   * Convenience constructor for backward compatibility.
   *
   * @param sourceDate the source date to copy from
   * @param sourceEndDate the end date of the source range (null for single date)
   * @param targetCalendar the target calendar to copy to
   * @param targetDate the target date to copy to
   */
  public CopyEventsCommand(LocalDate sourceDate, LocalDate sourceEndDate,
                           String targetCalendar, LocalDate targetDate) {
    if (sourceEndDate == null) {
      this.copyRequest = new DateRangeCopyRequest(sourceDate, targetDate);
    } else {
      this.copyRequest = new DateRangeCopyRequest(sourceDate, sourceEndDate, targetDate);
    }
    this.targetCalendar = targetCalendar;
  }

  @Override
  public void execute(InterfaceCalendarSystem model,
                      InterfaceCalendarView view, String currentCalendar) {
    validateCalendarInUse(currentCalendar);

    try {
      int count = model.copyEventsInRange(currentCalendar, targetCalendar, copyRequest);
      view.displayMessage(count + " event(s) copied to " + targetCalendar);
    } catch (Exception e) {
      view.displayError("Failed to copy events: " + e.getMessage());
    }
  }
}