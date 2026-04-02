package calendar.controller.commands;

import calendar.model.InterfaceCalendarSystem;
import calendar.view.InterfaceCalendarView;

/**
 * Command to switch to using a different calendar.
 */
public class UseCalendarCommand extends BaseCommand {
  private final String calendarName;

  /**
   * Constructs a UseCalendarCommand.
   *
   * @param calendarName the name of the calendar to use
   */
  public UseCalendarCommand(String calendarName) {
    this.calendarName = calendarName;
  }

  /**
   * Gets the calendar name.
   *
   * @return the calendar name
   */
  public String getCalendarName() {
    return calendarName;
  }

  @Override
  public void execute(InterfaceCalendarSystem model, InterfaceCalendarView view,
                      String currentCalendar) {
    try {
      if (!model.calendarExists(calendarName)) {
        throw new IllegalArgumentException("Calendar does not exist: " + calendarName);
      }
      view.displayMessage("Now using calendar: " + calendarName);
    } catch (Exception e) {
      view.displayError("Failed to use calendar: " + e.getMessage());
    }
  }

  @Override
  public String getNewCalendarContext() {
    return calendarName;
  }

}