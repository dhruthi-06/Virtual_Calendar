package calendar.controller.commands;

import calendar.model.InterfaceCalendarSystem;
import calendar.view.InterfaceCalendarView;

/**
 * Command to exit the application.
 */
public class ExitCommand extends BaseCommand {

  @Override
  public void execute(InterfaceCalendarSystem model,
                      InterfaceCalendarView view, String currentCalendar) {
    view.displayMessage("Thank you for using Calendar Application. Goodbye!");
  }

  @Override
  public boolean isExitCommand() {
    return true;
  }

}