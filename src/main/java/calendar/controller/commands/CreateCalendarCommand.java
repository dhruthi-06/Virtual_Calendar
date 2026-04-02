package calendar.controller.commands;

import calendar.model.InterfaceCalendarSystem;
import calendar.view.InterfaceCalendarView;

/**
 * Command to create a new calendar with a specified name and timezone.
 */
public class CreateCalendarCommand extends BaseCommand {
  private final String calendarName;
  private final String timezone;

  /**
   * Constructs a CreateCalendarCommand.
   *
   * @param calendarName the name of the calendar to create
   * @param timezone the timezone for the calendar
   */
  public CreateCalendarCommand(String calendarName, String timezone) {
    this.calendarName = calendarName;
    this.timezone = timezone;
  }

  @Override
  public void execute(InterfaceCalendarSystem model,
                      InterfaceCalendarView view, String currentCalendar) {
    try {
      model.createCalendar(calendarName, timezone);
      view.displayMessage("Calendar created: " + calendarName + " (Timezone: " + timezone + ")");
    } catch (Exception e) {
      view.displayError("Failed to create calendar: " + e.getMessage());
    }
  }

}