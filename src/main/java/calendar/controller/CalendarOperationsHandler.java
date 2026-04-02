package calendar.controller;

import calendar.model.InterfaceCalendar;
import calendar.model.InterfaceCalendarSystem;
import calendar.view.gui.InterfaceCalendarGuiView;
import java.time.ZoneId;
import java.util.List;

/**
 * Handles calendar-related operations for GUI controller.
 */
public class CalendarOperationsHandler {

  private final InterfaceCalendarSystem model;
  private final InterfaceCalendarGuiView view;

  /**
   * Constructor.
   *
   * @param model the calendar system model
   * @param view the GUI view
   */
  public CalendarOperationsHandler(InterfaceCalendarSystem model, InterfaceCalendarGuiView view) {
    this.model = model;
    this.view = view;
  }

  /**
   * Creates a new calendar.
   *
   * @param name calendar name
   * @param timezone timezone string
   */
  public void createCalendar(String name, String timezone) {
    try {
      model.createCalendar(name, timezone);
      view.updateCalendarList(model.getAllCalendarNames());
      view.showMessage("Calendar '" + name + "' created successfully");
    } catch (Exception e) {
      view.showError("Failed to create calendar: " + e.getMessage());
    }
  }

  /**
   * Switches to a different calendar.
   *
   * @param calendarName the calendar to switch to
   * @param currentCalendarName the current calendar name (for updating)
   * @return the new calendar name, or current if switch failed
   */
  public String switchCalendar(String calendarName, String currentCalendarName) {
    try {
      if (!model.calendarExists(calendarName)) {
        view.showError("Calendar '" + calendarName + "' does not exist");
        return currentCalendarName;
      }

      view.setCurrentCalendar(calendarName);
      view.refreshCalendar();
      return calendarName;
    } catch (Exception e) {
      view.showError("Failed to switch calendar: " + e.getMessage());
      return currentCalendarName;
    }
  }

  /**
   * Edits a calendar's name.
   *
   * @param oldName current name
   * @param newName new name
   * @param currentCalendarName the current calendar in use
   * @return updated current calendar name
   */
  public String editCalendarName(String oldName, String newName, String currentCalendarName) {
    try {
      model.editCalendarName(oldName, newName);

      String updatedCurrentName = currentCalendarName;
      if (oldName.equals(currentCalendarName)) {
        updatedCurrentName = newName;
        view.setCurrentCalendar(newName);
      }

      view.updateCalendarList(model.getAllCalendarNames());
      view.showMessage("Calendar renamed successfully");
      return updatedCurrentName;
    } catch (Exception e) {
      view.showError("Failed to rename calendar: " + e.getMessage());
      return currentCalendarName;
    }
  }

  /**
   * Edits a calendar's timezone.
   *
   * @param calendarName calendar to edit
   * @param newTimezone new timezone
   * @param currentCalendarName current calendar in use
   */
  public void editCalendarTimezone(String calendarName, String newTimezone,
                                   String currentCalendarName) {
    try {
      model.editCalendarTimezone(calendarName, newTimezone);

      if (calendarName.equals(currentCalendarName)) {
        view.refreshCalendar();
      }

      view.showMessage("Timezone updated successfully");
    } catch (Exception e) {
      view.showError("Failed to update timezone: " + e.getMessage());
    }
  }

  /**
   * Gets all calendar names.
   *
   * @return list of calendar names
   */
  public List<String> getAllCalendarNames() {
    return model.getAllCalendarNames();
  }

  /**
   * Gets the timezone of a calendar.
   *
   * @param calendarName the calendar name
   * @return timezone ID string
   */
  public String getCalendarTimezone(String calendarName) {
    try {
      InterfaceCalendar calendar = model.getCalendar(calendarName);
      return calendar.getTimezone().getId();
    } catch (Exception e) {
      return ZoneId.systemDefault().getId();
    }
  }

  /**
   * Checks if a calendar exists.
   *
   * @param calendarName the calendar name
   * @return true if exists
   */
  public boolean calendarExists(String calendarName) {
    return model.calendarExists(calendarName);
  }
}