package calendar.controller.commands;

import calendar.model.InterfaceCalendar;
import calendar.model.InterfaceCalendarSystem;
import calendar.view.InterfaceCalendarView;
import java.time.LocalDateTime;

/**
 * Command to show the busy status at a specific date and time.
 */
public class ShowStatusCommand extends BaseCommand {

  private final LocalDateTime dateTime;

  /**
   * Constructs a ShowStatusCommand.
   *
   * @param dateTime the date and time to check status for
   */
  public ShowStatusCommand(LocalDateTime dateTime) {
    if (dateTime == null) {
      throw new IllegalArgumentException("Date/time cannot be null");
    }
    this.dateTime = dateTime;
  }

  @Override
  public void execute(InterfaceCalendarSystem model, InterfaceCalendarView view,
                      String currentCalendar) {
    validateCalendarInUse(currentCalendar);

    try {
      InterfaceCalendar calendar = model.getCalendar(currentCalendar);
      boolean isBusy = calendar.isBusyAt(dateTime);
      view.displayStatus(isBusy, dateTime.toString());
    } catch (Exception e) {
      view.displayError("Error checking status: " + e.getMessage());
    }
  }

  /**
   * Gets the date and time.
   *
   * @return the date and time
   */
  public LocalDateTime getDateTime() {
    return dateTime;
  }
}