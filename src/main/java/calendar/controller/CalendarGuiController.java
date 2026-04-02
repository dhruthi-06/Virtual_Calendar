package calendar.controller;

import calendar.model.EventCreationRequest;
import calendar.model.InterfaceCalendarSystem;
import calendar.model.InterfaceEvent;
import calendar.model.RecurringEventCreationRequest;
import calendar.view.gui.InterfaceCalendarGuiView;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * GUI Controller for the calendar application.
 */
public class CalendarGuiController {

  private final InterfaceCalendarSystem model;
  private final InterfaceCalendarGuiView view;
  private final CalendarOperationsHandler calendarOps;
  private final EventOperationsHandler eventOps;

  private String currentCalendarName;
  private static final String DEFAULT_CALENDAR = "My Calendar";

  /**
   * Local request wrapper for GUI that maps to model's EventCreationRequest.
   * Kept for backward compatibility with existing GUI code.
   */
  public static class EventCreationRequestWrapper {
    public final String subject;
    public final LocalDateTime start;
    public final LocalDateTime end;
    public final String description;
    public final String location;
    public final boolean isPublic;

    private EventCreationRequestWrapper(Builder builder) {
      this.subject = builder.subject;
      this.start = builder.start;
      this.end = builder.end;
      this.description = builder.description;
      this.location = builder.location;
      this.isPublic = builder.isPublic;
    }

    /**
     * Builder class for EventCreationRequestWrapper.
     */
    public static class Builder {
      private final String subject;
      private final LocalDateTime start;
      private final LocalDateTime end;
      private String description = "";
      private String location = "";
      private boolean isPublic = true;

      /**
       * Constructs a Builder with required fields.
       *
       * @param subject the subject of the event.
       * @param start the start date and time of the event.
       * @param end the end date and time of the event.
       */
      public Builder(String subject, LocalDateTime start, LocalDateTime end) {
        this.subject = subject;
        this.start = start;
        this.end = end;
      }

      /**
       * Sets the optional description.
       *
       * @param description the event description.
       * @return this Builder instance.
       */
      public Builder description(String description) {
        this.description = description;
        return this;
      }

      /**
       * Sets the optional location.
       *
       * @param location the event location.
       * @return this Builder instance.
       */
      public Builder location(String location) {
        this.location = location;
        return this;
      }

      /**
       * Sets the optional public visibility status.
       *
       * @param isPublic true if the event is public, false otherwise.
       * @return this Builder instance.
       */
      public Builder isPublic(boolean isPublic) {
        this.isPublic = isPublic;
        return this;
      }

      /**
       * Creates the immutable EventCreationRequestWrapper instance.
       *
       * @return the new EventCreationRequestWrapper.
       */
      public EventCreationRequestWrapper build() {
        return new EventCreationRequestWrapper(this);
      }
    }
  }

  /**
   * Local request wrapper for GUI that maps to model's RecurringEventCreationRequest.
   * Kept for backward compatibility with existing GUI code.
   */
  public static class RecurringEventRequestWrapper {
    public final String subject;
    public final LocalDateTime start;
    public final LocalDateTime end;
    public final String weekdays;
    public final Integer repeatCount;
    public final LocalDateTime repeatUntil;
    public final String description;
    public final String location;
    public final boolean isPublic;

    private RecurringEventRequestWrapper(Builder builder) {
      this.subject = builder.subject;
      this.start = builder.start;
      this.end = builder.end;
      this.weekdays = builder.weekdays;
      this.repeatCount = builder.repeatCount;
      this.repeatUntil = builder.repeatUntil;
      this.description = builder.description;
      this.location = builder.location;
      this.isPublic = builder.isPublic;
    }

    /**
     * Builder class for RecurringEventRequestWrapper.
     */
    public static class Builder {
      private final String subject;
      private final LocalDateTime start;
      private final LocalDateTime end;
      private final String weekdays;
      private Integer repeatCount = null;
      private LocalDateTime repeatUntil = null;
      private String description = "";
      private String location = "";
      private boolean isPublic = true;

      /**
       * Constructs a Builder with required fields.
       *
       * @param subject the subject of the recurring event.
       * @param start the start date and time of the first occurrence.
       * @param end the end date and time of the first occurrence.
       * @param weekdays the recurring days of the week (e.g., "MWF").
       */
      public Builder(String subject, LocalDateTime start, LocalDateTime end, String weekdays) {
        this.subject = subject;
        this.start = start;
        this.end = end;
        this.weekdays = weekdays;
      }

      /**
       * Sets the optional repeat count. Cannot be set with repeatUntil.
       *
       * @param repeatCount the number of times the event should repeat.
       * @return this Builder instance.
       */
      public Builder repeatCount(Integer repeatCount) {
        this.repeatCount = repeatCount;
        return this;
      }

      /**
       * Sets the optional repeat until date. Cannot be set with repeatCount.
       *
       * @param repeatUntil the date/time the recurrence should stop.
       * @return this Builder instance.
       */
      public Builder repeatUntil(LocalDateTime repeatUntil) {
        this.repeatUntil = repeatUntil;
        return this;
      }

      /**
       * Sets the optional description.
       *
       * @param description the event description.
       * @return this Builder instance.
       */
      public Builder description(String description) {
        this.description = description;
        return this;
      }

      /**
       * Sets the optional location.
       *
       * @param location the event location.
       * @return this Builder instance.
       */
      public Builder location(String location) {
        this.location = location;
        return this;
      }

      /**
       * Sets the optional public visibility status.
       *
       * @param isPublic true if the event is public, false otherwise.
       * @return this Builder instance.
       */
      public Builder isPublic(boolean isPublic) {
        this.isPublic = isPublic;
        return this;
      }

      /**
       * Creates the immutable RecurringEventRequestWrapper instance.
       *
       * @return the new RecurringEventRequestWrapper.
       */
      public RecurringEventRequestWrapper build() {
        return new RecurringEventRequestWrapper(this);
      }
    }
  }

  /**
   * Constructs a CalendarGuiController.
   *
   * @param model the calendar system model.
   * @param view the GUI view.
   * @throws IllegalArgumentException if model or view is null.
   */
  public CalendarGuiController(InterfaceCalendarSystem model,
                               InterfaceCalendarGuiView view) {
    if (model == null) {
      throw new IllegalArgumentException("Model cannot be null");
    }
    if (view == null) {
      throw new IllegalArgumentException("View cannot be null");
    }

    this.model = model;
    this.view = view;
    this.calendarOps = new CalendarOperationsHandler(model, view);
    this.eventOps = new EventOperationsHandler(model, view);
    this.view.setFeatures(this);
  }

  /**
   * Starts the application by creating default calendar.
   */
  public void start() {
    try {
      String systemTimezone = ZoneId.systemDefault().getId();
      model.createCalendar(DEFAULT_CALENDAR, systemTimezone);
      currentCalendarName = DEFAULT_CALENDAR;

      view.setCurrentCalendar(currentCalendarName);
      view.updateCalendarList(model.getAllCalendarNames());
      view.refreshCalendar();
    } catch (Exception e) {
      view.showError("Failed to initialize application: " + e.getMessage());
    }
  }

  /**
   * Creates a new calendar. Delegates to CalendarOperationsHandler.
   *
   * @param name the name of the new calendar.
   * @param timezone the timezone for the new calendar.
   */
  public void createCalendar(String name, String timezone) {
    calendarOps.createCalendar(name, timezone);
  }

  /**
   * Switches to a different calendar. Delegates to CalendarOperationsHandler.
   *
   * @param calendarName the name of the calendar to switch to.
   */
  public void switchCalendar(String calendarName) {
    currentCalendarName = calendarOps.switchCalendar(calendarName, currentCalendarName);
  }

  /**
   * Creates an event. Delegates to EventOperationsHandler.
   *
   * @param builder the builder containing event creation request parameters.
   */
  public void createEvent(EventCreationRequestWrapper.Builder builder) {
    EventCreationRequestWrapper wrapper = builder.build();
    try {
      validateCurrentCalendar();

      EventCreationRequest modelRequest = new EventCreationRequest.Builder(
          wrapper.subject, wrapper.start, wrapper.end)
          .description(wrapper.description)
          .location(wrapper.location)
          .isPublic(wrapper.isPublic)
          .build();

      eventOps.createEvent(currentCalendarName, modelRequest);
    } catch (Exception e) {
      view.showError("Failed to create event: " + e.getMessage());
    }
  }

  /**
   * Creates a recurring event. Delegates to EventOperationsHandler.
   *
   * @param builder the builder containing recurring event creation request parameters.
   */
  public void createRecurringEvent(RecurringEventRequestWrapper.Builder builder) {
    RecurringEventRequestWrapper wrapper = builder.build();
    try {
      validateCurrentCalendar();

      RecurringEventCreationRequest.Builder modelBuilder =
          new RecurringEventCreationRequest.Builder(
              wrapper.subject, wrapper.start, wrapper.end, wrapper.weekdays);

      if (wrapper.repeatCount != null) {
        modelBuilder.repeatCount(wrapper.repeatCount);
      } else if (wrapper.repeatUntil != null) {
        modelBuilder.repeatUntil(wrapper.repeatUntil);
      }

      RecurringEventCreationRequest modelRequest = modelBuilder.build();
      eventOps.createRecurringEvent(currentCalendarName, modelRequest);
    } catch (Exception e) {
      view.showError("Failed to create recurring event: " + e.getMessage());
    }
  }

  /**
   * Edits a single event. Delegates to EventOperationsHandler.
   *
   * @param subject the subject of the event.
   * @param startDateTime the start time of the event (used as a key).
   * @param property the property to edit (e.g., "subject", "location").
   * @param newValue the new value for the property.
   */
  public void editEvent(String subject, LocalDateTime startDateTime,
                        String property, String newValue) {
    try {
      validateCurrentCalendar();
      eventOps.editEvent(currentCalendarName, subject, startDateTime, property, newValue);
    } catch (Exception e) {
      view.showError("Failed to edit event: " + e.getMessage());
    }
  }

  /**
   * Edits events from a date forward. Delegates to EventOperationsHandler.
   *
   * @param subject the subject of the event series.
   * @param startDateTime the start time of the event occurrence to start editing from.
   * @param property the property to edit.
   * @param newValue the new value for the property.
   */
  public void editEventsFromDate(String subject, LocalDateTime startDateTime,
                                 String property, String newValue) {
    try {
      validateCurrentCalendar();
      eventOps.editEventsFromDate(currentCalendarName, subject, startDateTime, property, newValue);
    } catch (Exception e) {
      view.showError("Failed to edit events: " + e.getMessage());
    }
  }

  /**
   * Edits entire event series. Delegates to EventOperationsHandler.
   *
   * @param subject the subject of the event series.
   * @param startDateTime the start time of any event in the series.
   * @param property the property to edit.
   * @param newValue the new value for the property.
   */
  public void editEntireSeries(String subject, LocalDateTime startDateTime,
                               String property, String newValue) {
    try {
      validateCurrentCalendar();
      eventOps.editEntireSeries(currentCalendarName, subject, startDateTime, property, newValue);
    } catch (Exception e) {
      view.showError("Failed to edit event series: " + e.getMessage());
    }
  }

  /**
   * Gets events for a specific date. Delegates to EventOperationsHandler.
   *
   * @param date the date to fetch events for.
   * @return a list of events on that date, or an empty list on failure.
   */
  public List<InterfaceEvent> getEventsForDate(LocalDate date) {
    try {
      validateCurrentCalendar();
      return eventOps.getEventsForDate(currentCalendarName, date);
    } catch (Exception e) {
      view.showError("Failed to get events: " + e.getMessage());
      return List.of();
    }
  }

  /**
   * Gets all events from current calendar. Delegates to EventOperationsHandler.
   *
   * @return a list of all events in the current calendar, or an empty list on failure.
   */
  public List<InterfaceEvent> getAllEventsFromCalendar() {
    try {
      validateCurrentCalendar();
      return eventOps.getAllEvents(currentCalendarName);
    } catch (Exception e) {
      view.showError("Failed to get events: " + e.getMessage());
      return List.of();
    }
  }

  /**
   * Gets events in a date range for analytics.
   *
   * @param startDate the start date (inclusive)
   * @param endDate the end date (inclusive)
   * @return a list of events in the range, or an empty list on failure
   */
  public List<InterfaceEvent> getEventsInRange(LocalDate startDate, LocalDate endDate) {
    try {
      validateCurrentCalendar();
      return eventOps.getEventsInRange(currentCalendarName, startDate, endDate);
    } catch (Exception e) {
      view.showError("Failed to get events: " + e.getMessage());
      return List.of();
    }
  }

  /**
   * Gets calendar analytics for a date range.
   *
   * @param startDate the start date (inclusive)
   * @param endDate the end date (inclusive)
   * @return analytics result, or null on failure
   */
  public calendar.util.CalendarAnalytics.AnalyticsResult getAnalytics(LocalDate startDate,
                                                                      LocalDate endDate) {
    try {
      validateCurrentCalendar();
      List<InterfaceEvent> events = getEventsInRange(startDate, endDate);
      return calendar.util.CalendarAnalytics.calculateAnalytics(events, startDate, endDate);
    } catch (Exception e) {
      view.showError("Failed to calculate analytics: " + e.getMessage());
      return null;
    }
  }

  /**
   * Edits calendar name. Delegates to CalendarOperationsHandler.
   *
   * @param oldName the current name of the calendar.
   * @param newName the new name for the calendar.
   */
  public void editCalendarName(String oldName, String newName) {
    currentCalendarName = calendarOps.editCalendarName(oldName, newName, currentCalendarName);
  }

  /**
   * Edits calendar timezone. Delegates to CalendarOperationsHandler.
   *
   * @param calendarName the name of the calendar to edit.
   * @param newTimezone the new timezone ID.
   */
  public void editCalendarTimezone(String calendarName, String newTimezone) {
    calendarOps.editCalendarTimezone(calendarName, newTimezone, currentCalendarName);
  }

  /**
   * Gets current calendar name.
   *
   * @return the name of the currently selected calendar.
   */
  public String getCurrentCalendarName() {
    return currentCalendarName;
  }

  /**
   * Gets all calendar names. Delegates to CalendarOperationsHandler.
   *
   * @return a list of all calendar names in the system.
   */
  public List<String> getAllCalendarNames() {
    return calendarOps.getAllCalendarNames();
  }

  /**
   * Gets current calendar timezone. Delegates to CalendarOperationsHandler.
   *
   * @return the timezone ID of the current calendar.
   */
  public String getCurrentCalendarTimezone() {
    try {
      validateCurrentCalendar();
      return calendarOps.getCalendarTimezone(currentCalendarName);
    } catch (Exception e) {
      return ZoneId.systemDefault().getId();
    }
  }

  /**
   * Validates that a calendar is currently selected.
   *
   * @throws IllegalStateException if no calendar is currently selected.
   */
  private void validateCurrentCalendar() {
    if (currentCalendarName == null) {
      throw new IllegalStateException("No calendar selected");
    }
  }
}