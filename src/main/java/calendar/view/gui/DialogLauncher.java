package calendar.view.gui;

import calendar.controller.CalendarGuiController;
import calendar.model.InterfaceEvent;
import java.time.LocalDate;
import java.util.List;
import javax.swing.JFrame;

/**
 * Handles launching of various dialog windows.
 */
public class DialogLauncher {
  private final JFrame parentView;
  private final CalendarGuiController controller;

  /**
   * Constructs a DialogLauncher.
   *
   * @param parentView the parent frame for dialogs
   * @param controller the application controller
   */
  public DialogLauncher(JFrame parentView, CalendarGuiController controller) {
    this.parentView = parentView;
    this.controller = controller;
  }

  /**
   * Shows the create calendar dialog.
   */
  public void showCreateCalendarDialog() {
    CreateCalendarDialog dialog = new CreateCalendarDialog(parentView, controller);
    dialog.setVisible(true);
  }

  /**
   * Shows the switch calendar dialog.
   */
  public void showSwitchCalendarDialog() {
    SwitchCalendarDialog dialog = new SwitchCalendarDialog(parentView, controller);
    dialog.setVisible(true);
  }

  /**
   * Shows the create event dialog.
   *
   * @param selectedDate the currently selected date
   * @param errorCallback callback to show errors
   */
  public void showCreateEventDialog(LocalDate selectedDate, ErrorCallback errorCallback) {
    if (selectedDate == null) {
      errorCallback.showError("Please select a date first");
      return;
    }
    CreateEventDialog dialog = new CreateEventDialog(parentView, controller, selectedDate);
    dialog.setVisible(true);
  }

  /**
   * Shows the edit event dialog.
   *
   * @param selectedDate the currently selected date
   * @param errorCallback callback to show errors
   */
  public void showEditEventDialog(LocalDate selectedDate, ErrorCallback errorCallback) {
    if (selectedDate == null) {
      errorCallback.showError("Please select a date first");
      return;
    }

    List<InterfaceEvent> events = controller.getEventsForDate(selectedDate);
    if (events.isEmpty()) {
      errorCallback.showError("No events on this date to edit");
      return;
    }

    EditEventDialog dialog = new EditEventDialog(parentView, controller, selectedDate, events);
    dialog.setVisible(true);
  }

  /**
   * Shows the edit calendar dialog.
   *
   * @param currentCalendarName the current calendar name
   * @param errorCallback callback to show errors
   */
  public void showEditCalendarDialog(String currentCalendarName, ErrorCallback errorCallback) {
    if (currentCalendarName == null) {
      errorCallback.showError("No calendar selected");
      return;
    }
    EditCalendarDialog dialog = new EditCalendarDialog(parentView, controller,
        currentCalendarName);
    dialog.setVisible(true);
  }

  /**
   * Shows the search events dialog.
   */
  public void showSearchEventsDialog() {
    SearchEventsDialog dialog = new SearchEventsDialog(parentView, controller);
    dialog.setVisible(true);
  }

  /**
   * Shows the calendar analytics dashboard dialog.
   */
  public void showDashboardDialog() {
    DashboardDialog dialog = new DashboardDialog(parentView, controller);
    dialog.setVisible(true);
  }

  /**
   * Callback interface for error messages.
   */
  public interface ErrorCallback {
    /**
     * Shows an error message to the user.
     *
     * @param message the error message to display
     */
    void showError(String message);
  }
}