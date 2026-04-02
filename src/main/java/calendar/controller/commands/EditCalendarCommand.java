package calendar.controller.commands;

import calendar.model.InterfaceCalendarSystem;
import calendar.view.InterfaceCalendarView;

/**
 * Command to edit calendar properties such as name or timezone.
 */
public class EditCalendarCommand extends BaseCommand {
  private final String calendarName;
  private final String property;
  private final String newValue;

  /**
   * Constructs an EditCalendarCommand.
   *
   * @param calendarName the name of the calendar to edit
   * @param property the property to change
   * @param newValue the new value for the property
   */
  public EditCalendarCommand(String calendarName, String property, String newValue) {
    this.calendarName = calendarName;
    this.property = property;
    this.newValue = newValue;
  }

  @Override
  public void execute(InterfaceCalendarSystem model,
                      InterfaceCalendarView view, String currentCalendar) {
    try {
      if (property.equalsIgnoreCase("name")) {
        model.editCalendarName(calendarName, newValue);
        view.displayMessage("Calendar name changed from '" + calendarName + "' to '"
            + newValue + "'");
      } else if (property.equalsIgnoreCase("timezone")) {
        model.editCalendarTimezone(calendarName, newValue);
        view.displayMessage("Calendar timezone changed to: " + newValue);
      } else {
        throw new IllegalArgumentException("Invalid property: " + property
            + ". Use 'name' or 'timezone'.");
      }
    } catch (Exception e) {
      view.displayError("Failed to edit calendar: " + e.getMessage());
    }
  }


}