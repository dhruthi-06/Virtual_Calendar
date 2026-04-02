package calendar.view.gui;

import calendar.controller.CalendarGuiController;
import calendar.model.InterfaceEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

/**
 * The main GUI view for the calendar application.
 */
public class CalendarGuiView extends JFrame implements InterfaceCalendarGuiView {

  private CalendarGuiController controller;
  private CalendarGridRenderer gridRenderer;
  private final CalendarColorManager colorManager;
  private DialogLauncher dialogLauncher;

  private YearMonth currentMonth;
  private LocalDate selectedDate;
  private String currentCalendarName;

  private JLabel monthYearLabel;
  private JLabel currentCalendarLabel;
  private JLabel selectedDateLabel;
  private JPanel calendarGridPanel;
  private JPanel eventsPanel;

  /**
   * Constructs the CalendarGuiView, initializing the default date and UI components.
   */
  public CalendarGuiView() {
    this.currentMonth = YearMonth.now();
    this.selectedDate = LocalDate.now();
    this.colorManager = new CalendarColorManager();
    initializeUi();
  }

  /**
   * Sets the controller and initializes the collaborator objects.
   *
   * @param controller the calendar GUI controller
   */
  public void setFeatures(CalendarGuiController controller) {
    this.controller = controller;
    this.gridRenderer = new CalendarGridRenderer(calendarGridPanel, colorManager);
    this.dialogLauncher = new DialogLauncher(this, controller);
  }

  private void initializeUi() {
    setTitle("Calendar Application");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(1000, 700);
    setLocationRelativeTo(null);
    setLayout(new BorderLayout(10, 10));
    add(createTopPanel(), BorderLayout.NORTH);
    add(createCalendarPanel(), BorderLayout.CENTER);
    add(createEventsPanel(), BorderLayout.EAST);
  }

  private JPanel createTopPanel() {
    JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    JPanel leftPanel = createCalendarInfoSection();
    JPanel navigationPanel = createNavigationSection();

    topPanel.add(leftPanel, BorderLayout.WEST);
    topPanel.add(navigationPanel, BorderLayout.CENTER);
    return topPanel;
  }

  private JPanel createCalendarInfoSection() {
    JPanel leftPanel = new JPanel();
    leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

    JPanel calendarInfoPanel = createCalendarInfoPanel();
    JPanel calendarButtonsPanel = createCalendarButtonsPanel();

    leftPanel.add(calendarInfoPanel);
    leftPanel.add(calendarButtonsPanel);
    return leftPanel;
  }

  private JPanel createCalendarInfoPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    currentCalendarLabel = new JLabel("My Calendar");
    currentCalendarLabel.setFont(new Font("Arial", Font.BOLD, 14));
    panel.add(new JLabel("Current Calendar: "));
    panel.add(currentCalendarLabel);
    return panel;
  }

  private JPanel createCalendarButtonsPanel() {
    final JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

    JButton newCalendarBtn = new JButton("New Calendar");
    JButton switchCalendarBtn = new JButton("Switch Calendar");
    JButton editCalendarBtn = new JButton("Edit Calendar");

    newCalendarBtn.addActionListener(e -> dialogLauncher.showCreateCalendarDialog());
    switchCalendarBtn.addActionListener(e -> dialogLauncher.showSwitchCalendarDialog());
    editCalendarBtn.addActionListener(e ->
        dialogLauncher.showEditCalendarDialog(currentCalendarName, this::showError));

    panel.add(newCalendarBtn);
    panel.add(switchCalendarBtn);
    panel.add(editCalendarBtn);
    return panel;
  }

  private JPanel createNavigationSection() {
    final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));

    monthYearLabel = new JLabel();
    monthYearLabel.setFont(new Font("Arial", Font.BOLD, 18));
    updateMonthYearLabel();

    JButton prevMonthBtn = createNavigationButton("<", -1);
    JButton nextMonthBtn = createNavigationButton(">", 1);
    JButton todayBtn = createTodayButton();

    panel.add(prevMonthBtn);
    panel.add(Box.createHorizontalStrut(20));
    panel.add(monthYearLabel);
    panel.add(Box.createHorizontalStrut(20));
    panel.add(nextMonthBtn);
    panel.add(Box.createHorizontalStrut(20));
    panel.add(todayBtn);

    return panel;
  }

  private JButton createNavigationButton(String text, int monthsToAdd) {
    JButton button = new JButton(text);
    button.addActionListener(e -> {
      currentMonth = currentMonth.plusMonths(monthsToAdd);
      refreshCalendar();
    });
    return button;
  }

  private JButton createTodayButton() {
    JButton button = new JButton("Today");
    button.addActionListener(e -> {
      currentMonth = YearMonth.now();
      selectedDate = LocalDate.now();
      refreshCalendar();
    });
    return button;
  }

  private JPanel createCalendarPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

    JPanel headerPanel = createCalendarHeaderPanel();
    calendarGridPanel = new JPanel(new GridLayout(0, 7));
    calendarGridPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

    panel.add(headerPanel, BorderLayout.NORTH);
    panel.add(calendarGridPanel, BorderLayout.CENTER);
    return panel;
  }

  private JPanel createCalendarHeaderPanel() {
    JPanel headerPanel = new JPanel(new GridLayout(1, 7));
    String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    for (String day : dayNames) {
      JLabel label = new JLabel(day, SwingConstants.CENTER);
      label.setFont(new Font("Arial", Font.BOLD, 12));
      label.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
      headerPanel.add(label);
    }
    return headerPanel;
  }

  private JPanel createEventsPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setPreferredSize(new Dimension(300, 0));
    panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));

    JPanel headerPanel = createEventsPanelHeader();
    eventsPanel = new JPanel();
    eventsPanel.setLayout(new BoxLayout(eventsPanel, BoxLayout.Y_AXIS));
    JScrollPane scrollPane = new JScrollPane(eventsPanel);

    JPanel buttonsPanel = createEventActionButtons();

    panel.add(headerPanel, BorderLayout.NORTH);
    panel.add(scrollPane, BorderLayout.CENTER);
    panel.add(buttonsPanel, BorderLayout.SOUTH);
    return panel;
  }

  private JPanel createEventsPanelHeader() {
    JPanel headerPanel = new JPanel();
    headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

    selectedDateLabel = new JLabel("No date selected", SwingConstants.CENTER);
    selectedDateLabel.setFont(new Font("Arial", Font.BOLD, 14));
    selectedDateLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));

    JLabel titleLabel = new JLabel("Events", SwingConstants.CENTER);
    titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
    titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

    headerPanel.add(titleLabel);
    headerPanel.add(selectedDateLabel);
    return headerPanel;
  }

  private JPanel createEventActionButtons() {
    final JPanel buttonsPanel = new JPanel(new GridLayout(4, 1, 5, 5));

    JButton createEventBtn = new JButton("Create Event");
    JButton editEventBtn = new JButton("Edit Event");
    JButton searchEventBtn = new JButton("Search Events");
    final JButton dashboardBtn = new JButton("Dashboard");

    createEventBtn.addActionListener(e ->
        dialogLauncher.showCreateEventDialog(selectedDate, this::showError));
    editEventBtn.addActionListener(e ->
        dialogLauncher.showEditEventDialog(selectedDate, this::showError));
    searchEventBtn.addActionListener(e -> dialogLauncher.showSearchEventsDialog());
    dashboardBtn.addActionListener(e -> dialogLauncher.showDashboardDialog());

    buttonsPanel.add(createEventBtn);
    buttonsPanel.add(editEventBtn);
    buttonsPanel.add(searchEventBtn);
    buttonsPanel.add(dashboardBtn);

    return buttonsPanel;
  }

  private void updateMonthYearLabel() {
    String monthName = currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    monthYearLabel.setText(monthName + " " + currentMonth.getYear());
  }

  @Override
  public void refreshCalendar() {
    updateMonthYearLabel();
    updateCalendarGrid();
    updateEventsDisplay();
  }

  /**
   * Updates the calendar grid by delegating to the grid renderer.
   * MODIFIED: Now uses MonthRenderContext parameter object.
   */
  private void updateCalendarGrid() {
    if (gridRenderer != null) {
      MonthRenderContext context = new MonthRenderContext.Builder(
          currentMonth,
          selectedDate,
          currentCalendarName)
          .clickListener(this::onDayClicked)
          .eventProvider(this::getEventsForDateProvider)
          .build();

      gridRenderer.renderMonth(context);
    }
  }

  /**
   * Callback for when a day is clicked.
   *
   * @param date the clicked date
   */
  private void onDayClicked(LocalDate date) {
    selectedDate = date;
    updateCalendarGrid();
    updateEventsDisplay();
  }

  /**
   * Provider method for getting events (used by grid renderer).
   *
   * @param date the date to get events for
   * @return list of events
   */
  private List<InterfaceEvent> getEventsForDateProvider(LocalDate date) {
    return controller != null ? controller.getEventsForDate(date) : List.of();
  }

  /**
   * Updates the events display panel.
   * Uses EventCardRenderer for rendering individual event cards.
   */
  private void updateEventsDisplay() {
    eventsPanel.removeAll();

    if (selectedDate == null || controller == null) {
      selectedDateLabel.setText("No date selected");
      eventsPanel.revalidate();
      eventsPanel.repaint();
      return;
    }

    updateSelectedDateLabel();
    displayEventsForSelectedDate();

    eventsPanel.revalidate();
    eventsPanel.repaint();
  }

  private void updateSelectedDateLabel() {
    String dayName = selectedDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    String dateStr = selectedDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
        + " " + selectedDate.getDayOfMonth() + ", " + selectedDate.getYear();
    selectedDateLabel.setText(dayName + " - " + dateStr);
  }

  /**
   * Displays events for the selected date using EventCardRenderer.
   */
  private void displayEventsForSelectedDate() {
    List<InterfaceEvent> events = controller.getEventsForDate(selectedDate);

    if (events.isEmpty()) {
      JLabel noEventsLabel = new JLabel("No events on this day");
      noEventsLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
      eventsPanel.add(noEventsLabel);
    } else {
      for (InterfaceEvent event : events) {
        eventsPanel.add(EventCardRenderer.createEventCard(event));
      }
    }
  }

  @Override
  public void showError(String message) {
    JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
  }

  @Override
  public void showMessage(String message) {
    JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
  }

  @Override
  public void updateEventsForDate(LocalDate date, List<InterfaceEvent> events) {
    if (date.equals(selectedDate)) {
      updateEventsDisplay();
    }
  }

  @Override
  public void updateCalendarList(List<String> calendarNames) {

  }

  @Override
  public void setCurrentCalendar(String calendarName) {
    this.currentCalendarName = calendarName;
    currentCalendarLabel.setText(calendarName);
    currentCalendarLabel.setForeground(colorManager.getCalendarColor(calendarName));
    refreshCalendar();
  }

  @Override
  public LocalDate getCurrentMonth() {
    return currentMonth.atDay(1);
  }

  @Override
  public LocalDate getSelectedDate() {
    return selectedDate;
  }
}