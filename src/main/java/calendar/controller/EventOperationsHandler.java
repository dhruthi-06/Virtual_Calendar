package calendar.controller;

import calendar.model.EventCreationRequest;
import calendar.model.InterfaceCalendar;
import calendar.model.InterfaceCalendarSystem;
import calendar.model.InterfaceEvent;
import calendar.model.RecurringEventCreationRequest;
import calendar.view.gui.InterfaceCalendarGuiView;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Handles event-related operations for GUI controller.
 */
public class EventOperationsHandler {

  private final InterfaceCalendarSystem model;
  private final InterfaceCalendarGuiView view;

  /**
   * Constructor.
   *
   * @param model the calendar system model
   * @param view the GUI view
   */
  public EventOperationsHandler(InterfaceCalendarSystem model, InterfaceCalendarGuiView view) {
    this.model = model;
    this.view = view;
  }

  /**
   * Creates a single event.
   *
   * @param calendarName the calendar to add to
   * @param request event creation request
   */
  public void createEvent(String calendarName, EventCreationRequest request) {
    try {
      model.createEvent(calendarName, request);
      view.refreshCalendar();
      view.showMessage("Event '" + request.getSubject() + "' created successfully");
    } catch (Exception e) {
      view.showError("Failed to create event: " + e.getMessage());
    }
  }

  /**
   * Creates a recurring event.
   *
   * @param calendarName the calendar to add to
   * @param request recurring event creation request
   */
  public void createRecurringEvent(String calendarName, RecurringEventCreationRequest request) {
    try {
      model.createRecurringEvent(calendarName, request);
      view.refreshCalendar();
      view.showMessage("Recurring event '" + request.getSubject() + "' created successfully");
    } catch (Exception e) {
      view.showError("Failed to create recurring event: " + e.getMessage());
    }
  }

  /**
   * Edits a single event.
   *
   * @param calendarName the calendar containing the event
   * @param subject event subject
   * @param startDateTime event start time
   * @param property property to edit
   * @param newValue new value
   */
  public void editEvent(String calendarName, String subject, LocalDateTime startDateTime,
                        String property, String newValue) {
    try {
      InterfaceCalendar calendar = model.getCalendar(calendarName);
      calendar.editEvent(subject, startDateTime, property, newValue);
      view.refreshCalendar();
      view.showMessage("Event updated successfully");
    } catch (Exception e) {
      view.showError("Failed to edit event: " + e.getMessage());
    }
  }

  /**
   * Edits events from a specific date forward in a series.
   *
   * @param calendarName the calendar containing the event
   * @param subject event subject
   * @param startDateTime event start time
   * @param property property to edit
   * @param newValue new value
   */
  public void editEventsFromDate(String calendarName, String subject,
                                 LocalDateTime startDateTime, String property, String newValue) {
    try {
      InterfaceCalendar calendar = model.getCalendar(calendarName);
      calendar.editEventsFromDate(subject, startDateTime, property, newValue);
      view.refreshCalendar();
      view.showMessage("Events updated successfully");
    } catch (Exception e) {
      view.showError("Failed to edit events: " + e.getMessage());
    }
  }

  /**
   * Edits an entire event series.
   *
   * @param calendarName the calendar containing the event
   * @param subject event subject
   * @param startDateTime event start time
   * @param property property to edit
   * @param newValue new value
   */
  public void editEntireSeries(String calendarName, String subject,
                               LocalDateTime startDateTime, String property, String newValue) {
    try {
      InterfaceCalendar calendar = model.getCalendar(calendarName);
      calendar.editEntireSeries(subject, startDateTime, property, newValue);
      view.refreshCalendar();
      view.showMessage("Event series updated successfully");
    } catch (Exception e) {
      view.showError("Failed to edit event series: " + e.getMessage());
    }
  }

  /**
   * Gets all events on a specific date.
   *
   * @param calendarName the calendar to query
   * @param date the date
   * @return list of events
   */
  public List<InterfaceEvent> getEventsForDate(String calendarName, LocalDate date) {
    try {
      InterfaceCalendar calendar = model.getCalendar(calendarName);
      return calendar.getEventsOnDate(date);
    } catch (Exception e) {
      view.showError("Failed to get events: " + e.getMessage());
      return List.of();
    }
  }

  /**
   * Gets all events from a calendar.
   *
   * @param calendarName the calendar to query
   * @return list of all events
   */
  public List<InterfaceEvent> getAllEvents(String calendarName) {
    try {
      InterfaceCalendar calendar = model.getCalendar(calendarName);
      return calendar.getAllEvents();
    } catch (Exception e) {
      view.showError("Failed to get events: " + e.getMessage());
      return List.of();
    }
  }

  /**
   * Gets events in a date range.
   *
   * @param calendarName the calendar to query
   * @param startDate the start date (inclusive)
   * @param endDate the end date (inclusive)
   * @return list of events in the range
   */
  public List<InterfaceEvent> getEventsInRange(String calendarName,
                                               LocalDate startDate, LocalDate endDate) {
    try {
      InterfaceCalendar calendar = model.getCalendar(calendarName);
      LocalDateTime rangeStart = startDate.atStartOfDay();
      LocalDateTime rangeEnd = endDate.atTime(23, 59, 59);
      return calendar.getEventsInRange(rangeStart, rangeEnd);
    } catch (Exception e) {
      view.showError("Failed to get events: " + e.getMessage());
      return List.of();
    }
  }
}