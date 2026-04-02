package calendar.controller.commands;

import calendar.model.InterfaceCalendar;
import calendar.model.InterfaceCalendarSystem;
import calendar.model.InterfaceEvent;
import calendar.util.DateTimeParser;
import calendar.view.InterfaceCalendarView;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Command to print events.
 * Supports two modes:
 * 1. Print events on a specific date
 * 2. Print events in a date/time range
 */
public class PrintEventsCommand extends BaseCommand {

  private final PrintMode mode;
  private final LocalDate date;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;

  /**
   * Enum for print modes.
   */
  public enum PrintMode {
    SINGLE_DATE,
    DATE_RANGE
  }

  /**
   * Builder for PrintEventsCommand.
   */
  public static class Builder {
    private PrintMode mode;
    private LocalDate date;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    /**
     * Sets the command to print events on a single date.
     *
     * @param date the date to print events for
     * @return this builder
     */
    public Builder onDate(LocalDate date) {
      if (date == null) {
        throw new IllegalArgumentException("Date cannot be null");
      }
      this.mode = PrintMode.SINGLE_DATE;
      this.date = date;
      return this;
    }

    /**
     * Sets the command to print events in a date/time range.
     *
     * @param start the start date and time
     * @param end the end date and time
     * @return this builder
     */
    public Builder inRange(LocalDateTime start, LocalDateTime end) {
      if (start == null) {
        throw new IllegalArgumentException("Start date/time cannot be null");
      }
      if (end == null) {
        throw new IllegalArgumentException("End date/time cannot be null");
      }
      if (end.isBefore(start)) {
        throw new IllegalArgumentException("End cannot be before start");
      }
      this.mode = PrintMode.DATE_RANGE;
      this.startDateTime = start;
      this.endDateTime = end;
      return this;
    }

    /**
     * Builds the PrintEventsCommand.
     *
     * @return a new PrintEventsCommand instance
     */
    public PrintEventsCommand build() {
      if (mode == null) {
        throw new IllegalStateException(
            "Must call onDate() or inRange() before build()");
      }
      return new PrintEventsCommand(this);
    }
  }

  private PrintEventsCommand(Builder builder) {
    this.mode = builder.mode;
    this.date = builder.date;
    this.startDateTime = builder.startDateTime;
    this.endDateTime = builder.endDateTime;
  }

  /**
   * Constructor for printing events on a specific date.
   *
   * @param date the date to print events for
   */
  public PrintEventsCommand(LocalDate date) {
    this.mode = PrintMode.SINGLE_DATE;
    this.date = date;
    this.startDateTime = null;
    this.endDateTime = null;
  }

  /**
   * Constructor for printing events in a date/time range.
   *
   * @param startDateTime the start date and time
   * @param endDateTime the end date and time
   */
  public PrintEventsCommand(LocalDateTime startDateTime, LocalDateTime endDateTime) {
    this.mode = PrintMode.DATE_RANGE;
    this.date = null;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
  }

  @Override
  public void execute(InterfaceCalendarSystem model,
                      InterfaceCalendarView view, String currentCalendar) {
    validateCalendarInUse(currentCalendar);

    try {
      InterfaceCalendar calendar = model.getCalendar(currentCalendar);

      if (mode == PrintMode.SINGLE_DATE) {
        printEventsOnDate(calendar, view);
      } else {
        printEventsInRange(calendar, view);
      }

    } catch (Exception e) {
      view.displayError("Error printing events: " + e.getMessage());
    }
  }

  private void printEventsOnDate(InterfaceCalendar calendar, InterfaceCalendarView view) {
    List<InterfaceEvent> events = calendar.getEventsOnDate(date);

    if (events.isEmpty()) {
      view.displayMessage("No events scheduled on " + DateTimeParser.formatDate(date));
      return;
    }

    view.displayEventsForDate(events, DateTimeParser.formatDate(date));
  }

  private void printEventsInRange(InterfaceCalendar calendar, InterfaceCalendarView view) {
    List<InterfaceEvent> events = calendar.getEventsInRange(startDateTime, endDateTime);

    if (events.isEmpty()) {
      view.displayMessage("No events found between "
          + DateTimeParser.formatDateTime(startDateTime)
          + " and "
          + DateTimeParser.formatDateTime(endDateTime));
      return;
    }

    view.displayEventsInRange(events,
        DateTimeParser.formatDateTime(startDateTime),
        DateTimeParser.formatDateTime(endDateTime));
  }

  /**
   * Gets the print mode.
   *
   * @return the print mode
   */
  public PrintMode getMode() {
    return mode;
  }
}