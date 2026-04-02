package calendar.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import calendar.model.InterfaceCalendar;
import calendar.model.InterfaceCalendarSystem;
import calendar.model.InterfaceEvent;
import calendar.view.gui.InterfaceCalendarGuiView;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

/** Test suite for CalendarGuiController that tests the interaction between
 * controller, model, and view using mock implementations.
 */
public class CalendarGuiControllerTest {

  private CalendarGuiController controller;
  private MockCalendarSystem mockModel;
  private MockGuiView mockView;

  /**
   * Sets up default values before each test.
   */
  @Before
  public void setUp() {
    mockModel = new MockCalendarSystem();
    mockView = new MockGuiView();
    mockModel.createCalendar("Default", "America/New_York");
    controller = new CalendarGuiController(mockModel, mockView);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorNullModel() {
    new CalendarGuiController(null, mockView);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorNullView() {
    new CalendarGuiController(mockModel, null);
  }

  @Test
  public void testConstructorSetsFeatures() {
    MockGuiView view = new MockGuiView();
    new CalendarGuiController(mockModel, view);
    assertTrue(view.getCalls().contains("setFeatures"));
  }

  @Test
  public void testStart() {
    mockModel.reset();
    mockView.reset();
    controller.start();

    assertTrue(mockModel.getCalls().contains("createCalendar:My Calendar:"
        + ZoneId.systemDefault().getId()));
    assertTrue(mockView.getCalls().contains("setCurrentCalendar"));
    assertTrue(mockView.getCalls().contains("updateCalendarList"));
    assertTrue(mockView.getCalls().contains("refreshCalendar"));
  }

  @Test
  public void testStartException() {
    MockCalendarSystem failingModel = new MockCalendarSystem();
    failingModel.setShouldFail(true);
    MockGuiView view = new MockGuiView();
    CalendarGuiController ctrl = new CalendarGuiController(failingModel, view);

    view.reset();
    ctrl.start();
    assertTrue(view.getCalls().contains("showError"));
  }

  @Test
  public void testCreateCalendar() {
    mockView.reset();
    controller.createCalendar("Work", "Europe/London");
    assertTrue(mockView.getCalls().contains("showMessage"));

    mockView.reset();
    controller.createCalendar("Invalid", "Bad/Timezone");
    assertTrue(mockView.getCalls().contains("showError"));
  }

  @Test
  public void testSwitchCalendar() {
    mockView.reset();
    controller.switchCalendar("Default");
    assertTrue(mockView.getCalls().contains("setCurrentCalendar"));
    assertTrue(mockView.getCalls().contains("refreshCalendar"));

    mockView.reset();
    controller.switchCalendar("NonExistent");
    assertTrue(mockView.getCalls().contains("showError"));
  }

  @Test
  public void testCreateEventWithValidation() {
    controller.start();
    mockView.reset();

    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    CalendarGuiController.EventCreationRequestWrapper.Builder builder =
        new CalendarGuiController.EventCreationRequestWrapper.Builder("Meeting", start, end)
            .description("Team sync")
            .location("Room 101")
            .isPublic(false);

    controller.createEvent(builder);
    assertTrue(mockView.getCalls().contains("showMessage"));
    assertTrue(mockView.getCalls().contains("refreshCalendar"));
  }

  @Test
  public void testCreateEventWithoutCurrentCalendar() {
    CalendarGuiController ctrl = new CalendarGuiController(mockModel, mockView);
    mockView.reset();

    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    CalendarGuiController.EventCreationRequestWrapper.Builder builder =
        new CalendarGuiController.EventCreationRequestWrapper.Builder("Meeting", start, end);

    ctrl.createEvent(builder);
    assertTrue(mockView.getCalls().contains("showError"));
  }

  @Test
  public void testCreateEventWithException() {
    controller.start();
    mockView.reset();

    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    CalendarGuiController.EventCreationRequestWrapper.Builder builder =
        new CalendarGuiController.EventCreationRequestWrapper.Builder("Meeting", start, end);

    mockModel.setFailOnCreateEvent(true);
    controller.createEvent(builder);
    assertTrue(mockView.getCalls().contains("showError"));
  }

  @Test
  public void testCreateRecurringWithCount() {
    controller.start();
    mockView.reset();

    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    CalendarGuiController.RecurringEventRequestWrapper.Builder builder =
        new CalendarGuiController.RecurringEventRequestWrapper.Builder("Weekly", start, end, "MWF")
            .repeatCount(10)
            .description("Test")
            .location("Room")
            .isPublic(false);
    controller.createRecurringEvent(builder);
    assertTrue(mockView.getCalls().contains("showMessage"));
  }

  @Test
  public void testCreateRecurringWithUntil() {
    controller.start();
    mockView.reset();

    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    LocalDateTime until = LocalDate.of(2025, 2, 15).atTime(23, 59);

    CalendarGuiController.RecurringEventRequestWrapper.Builder builder =
        new CalendarGuiController.RecurringEventRequestWrapper.Builder("Daily", start, end, "MTWRF")
            .repeatUntil(until);
    controller.createRecurringEvent(builder);
    assertTrue(mockView.getCalls().contains("showMessage"));
  }

  @Test
  public void testCreateRecurringWithoutCurrentCalendar() {
    CalendarGuiController ctrl = new CalendarGuiController(mockModel, mockView);
    mockView.reset();

    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    CalendarGuiController.RecurringEventRequestWrapper.Builder builder =
        new CalendarGuiController.RecurringEventRequestWrapper.Builder("Weekly", start, end, "M")
            .repeatCount(5);

    ctrl.createRecurringEvent(builder);
    assertTrue(mockView.getCalls().contains("showError"));
  }

  @Test
  public void testEditEvent() {
    controller.start();
    mockView.reset();

    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    controller.editEvent("Meeting", start, "location", "Room 202");
    assertTrue(mockView.getCalls().contains("showMessage"));
  }

  @Test
  public void testEditEventWithoutCurrentCalendar() {
    CalendarGuiController ctrl = new CalendarGuiController(mockModel, mockView);
    mockView.reset();

    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    ctrl.editEvent("Meeting", start, "location", "Room 202");
    assertTrue(mockView.getCalls().contains("showError"));
  }

  @Test
  public void testEditEventsFromDate() {
    controller.start();
    mockView.reset();

    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    controller.editEventsFromDate("Series", start, "location", "Room 303");
    assertTrue(mockView.getCalls().contains("showMessage"));
  }

  @Test
  public void testEditEventsFromDateWithoutCurrentCalendar() {
    CalendarGuiController ctrl = new CalendarGuiController(mockModel, mockView);
    mockView.reset();

    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    ctrl.editEventsFromDate("Series", start, "location", "Room 303");
    assertTrue(mockView.getCalls().contains("showError"));
  }

  @Test
  public void testEditEntireSeries() {
    controller.start();
    mockView.reset();

    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    controller.editEntireSeries("Series", start, "subject", "Updated");
    assertTrue(mockView.getCalls().contains("showMessage"));
  }

  @Test
  public void testEditEntireSeriesWithoutCurrentCalendar() {
    CalendarGuiController ctrl = new CalendarGuiController(mockModel, mockView);
    mockView.reset();

    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    ctrl.editEntireSeries("Series", start, "subject", "Updated");
    assertTrue(mockView.getCalls().contains("showError"));
  }

  @Test
  public void testGetEventsForDate() {
    controller.start();
    mockView.reset();

    LocalDate date = LocalDate.of(2025, 1, 15);
    List<InterfaceEvent> events = controller.getEventsForDate(date);
    assertNotNull(events);
    assertFalse(mockView.getCalls().contains("showError"));
  }

  @Test
  public void testGetEventsForDateWithoutCurrentCalendar() {
    CalendarGuiController ctrl = new CalendarGuiController(mockModel, mockView);
    mockView.reset();

    LocalDate date = LocalDate.of(2025, 1, 15);
    List<InterfaceEvent> events = ctrl.getEventsForDate(date);
    assertNotNull(events);
    assertTrue(events.isEmpty());
    assertTrue(mockView.getCalls().contains("showError"));
  }

  @Test
  public void testGetEventsForDateReturnsActualEvents() {
    controller.start();
    mockModel.addTestEvent();
    mockView.reset();

    LocalDate date = LocalDate.of(2025, 1, 15);
    List<InterfaceEvent> events = controller.getEventsForDate(date);
    assertNotNull(events);
    assertEquals(1, events.size());
  }

  @Test
  public void testGetAllEventsFromCalendar() {
    controller.start();
    List<InterfaceEvent> events = controller.getAllEventsFromCalendar();
    assertNotNull(events);
  }

  @Test
  public void testGetAllEventsFromCalendarWithoutCurrent() {
    CalendarGuiController ctrl = new CalendarGuiController(mockModel, mockView);
    mockView.reset();

    List<InterfaceEvent> events = ctrl.getAllEventsFromCalendar();
    assertNotNull(events);
    assertTrue(events.isEmpty());
    assertTrue(mockView.getCalls().contains("showError"));
  }

  @Test
  public void testGetAllEventsFromCalendarReturnsActual() {
    controller.start();
    mockModel.addTestEvent();
    mockView.reset();

    List<InterfaceEvent> events = controller.getAllEventsFromCalendar();
    assertNotNull(events);
    assertEquals(1, events.size());
  }

  @Test
  public void testEditCalendarName() {
    mockView.reset();
    controller.editCalendarName("Default", "Personal");
    assertTrue(mockModel.getCalls().contains("editCalendarName:Default:Personal"));
    assertTrue(mockView.getCalls().contains("updateCalendarList"));
  }

  @Test
  public void testEditCalendarTimezone() {
    mockView.reset();
    controller.editCalendarTimezone("Default", "Asia/Tokyo");
    assertTrue(mockModel.getCalls().contains("editCalendarTimezone:Default:Asia/Tokyo"));
    assertTrue(mockView.getCalls().contains("showMessage"));
  }

  @Test
  public void testGetAllCalendarNames() {
    List<String> names = controller.getAllCalendarNames();
    assertNotNull(names);
    assertTrue(names.contains("Default"));
  }

  @Test
  public void testGetCurrentCalendarName() {
    controller.start();
    assertEquals("My Calendar", controller.getCurrentCalendarName());
  }

  @Test
  public void testGetCurrentCalendarNameBeforeStart() {
    CalendarGuiController ctrl = new CalendarGuiController(mockModel, mockView);
    assertNull(ctrl.getCurrentCalendarName());
  }

  @Test
  public void testGetCurrentCalendarTimezone() {
    controller.start();
    String timezone = controller.getCurrentCalendarTimezone();
    assertNotNull(timezone);
  }

  @Test
  public void testGetCurrentCalendarTimezoneWithoutCurrent() {
    CalendarGuiController ctrl = new CalendarGuiController(mockModel, mockView);
    String timezone = ctrl.getCurrentCalendarTimezone();
    assertEquals(ZoneId.systemDefault().getId(), timezone);
  }

  @Test
  public void testGetCurrentCalendarTimezoneReturnsActual() {
    controller.start();
    mockView.reset();

    String timezone = controller.getCurrentCalendarTimezone();
    assertNotNull(timezone);
    assertEquals(ZoneId.systemDefault().getId(), timezone);
  }

  @Test
  public void testEventCreationWrapperBuilder() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    CalendarGuiController.EventCreationRequestWrapper wrapper =
        new CalendarGuiController.EventCreationRequestWrapper.Builder("Test", start, end)
            .description("Desc")
            .location("Loc")
            .isPublic(true)
            .build();

    assertNotNull(wrapper);
    assertEquals("Test", wrapper.subject);
    assertEquals("Desc", wrapper.description);
    assertEquals("Loc", wrapper.location);
    assertTrue(wrapper.isPublic);
  }

  @Test
  public void testRecurringEventWrapperBuilder() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    CalendarGuiController.RecurringEventRequestWrapper wrapper =
        new CalendarGuiController.RecurringEventRequestWrapper.Builder("Weekly", start, end, "MWF")
            .repeatCount(10)
            .description("Test")
            .location("Room")
            .isPublic(false)
            .build();

    assertNotNull(wrapper);
    assertEquals("Weekly", wrapper.subject);
    assertEquals(Integer.valueOf(10), wrapper.repeatCount);
  }

  private static class MockCalendarSystem implements InterfaceCalendarSystem {
    private List<String> methodCalls = new ArrayList<>();
    private Map<String, MockCalendar> calendars = new HashMap<>();
    private boolean shouldFail = false;
    private boolean failOnCreateEvent = false;

    public List<String> getCalls() {
      return methodCalls;
    }

    public void reset() {
      methodCalls.clear();
      calendars.clear();
      shouldFail = false;
      failOnCreateEvent = false;
    }

    public void setShouldFail(boolean fail) {
      this.shouldFail = fail;
    }

    public void setFailOnCreateEvent(boolean fail) {
      this.failOnCreateEvent = fail;
    }

    public void addTestEvent() {
      if (!calendars.isEmpty()) {
        calendars.values().iterator().next().addTestEvent();
      }
    }

    @Override
    public void createCalendar(String name, String timezone) {
      methodCalls.add("createCalendar:" + name + ":" + timezone);
      if (shouldFail) {
        throw new RuntimeException("Forced failure");
      }
      if (name == null || name.trim().isEmpty()) {
        throw new IllegalArgumentException("Invalid name");
      }
      try {
        ZoneId.of(timezone);
        if (calendars.containsKey(name)) {
          throw new IllegalArgumentException("Already exists");
        }
        calendars.put(name, new MockCalendar(name, timezone));
      } catch (Exception e) {
        throw new IllegalArgumentException("Invalid timezone: " + timezone);
      }
    }

    @Override
    public InterfaceCalendar getCalendar(String name) {
      methodCalls.add("getCalendar:" + name);
      MockCalendar cal = calendars.get(name);
      if (cal == null) {
        throw new IllegalArgumentException("Calendar not found: " + name);
      }
      return cal;
    }

    @Override
    public boolean calendarExists(String name) {
      return calendars.containsKey(name);
    }

    @Override
    public void editCalendarName(String oldName, String newName) {
      methodCalls.add("editCalendarName:" + oldName + ":" + newName);
      if (!calendars.containsKey(oldName)) {
        throw new IllegalArgumentException("Calendar not found");
      }
      if (calendars.containsKey(newName)) {
        throw new IllegalArgumentException("Already exists");
      }
      MockCalendar cal = calendars.remove(oldName);
      calendars.put(newName, cal);
    }

    @Override
    public void editCalendarTimezone(String calendarName, String newTimezone) {
      methodCalls.add("editCalendarTimezone:" + calendarName + ":" + newTimezone);
      if (!calendars.containsKey(calendarName)) {
        throw new IllegalArgumentException("Calendar not found");
      }
      try {
        ZoneId.of(newTimezone);
      } catch (Exception e) {
        throw new IllegalArgumentException("Invalid timezone");
      }
    }

    @Override
    public List<String> getAllCalendarNames() {
      return new ArrayList<>(calendars.keySet());
    }

    @Override
    public void deleteCalendar(String name) {
    }

    @Override
    public void createEvent(String calendarName, String subject,
                            LocalDateTime start, LocalDateTime end) {
    }

    @Override
    public void createEvent(String calendarName,
                            calendar.model.EventCreationRequest request) {
      if (failOnCreateEvent) {
        throw new RuntimeException("Forced event creation failure");
      }
    }

    @Override
    public void createRecurringEvent(String calendarName,
                                     calendar.model.RecurringEventCreationRequest request) {
    }

    @Override
    public void copyEvent(String sourceCalendar,
                          calendar.model.EventCopyRequest request) {
    }

    @Override
    public int copyEventsInRange(String sourceCalendar, String targetCalendar,
                                 calendar.model.DateRangeCopyRequest request) {
      return 0;
    }
  }

  private static class MockCalendar implements InterfaceCalendar {
    private String name;
    private String timezone;
    private List<InterfaceEvent> events = new ArrayList<>();

    MockCalendar(String name, String timezone) {
      this.name = name;
      this.timezone = timezone;
    }

    public void addTestEvent() {
      events.add(new MockEvent());
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public void setName(String name) {
    }

    @Override
    public ZoneId getTimezone() {
      return ZoneId.of(timezone);
    }

    @Override
    public void setTimezone(ZoneId timezone) {
    }

    @Override
    public void addEvent(InterfaceEvent event) {
    }

    @Override
    public void addRecurringEvent(calendar.model.InterfaceRecurringEvent event) {
    }

    @Override
    public boolean removeEvent(String subject, LocalDateTime start) {
      return false;
    }

    @Override
    public InterfaceEvent findEvent(String subject, LocalDateTime start) {
      return null;
    }

    @Override
    public void editEvent(String subject, LocalDateTime start,
                          String property, String newValue) {
    }

    @Override
    public void editEventsFromDate(String subject, LocalDateTime start,
                                   String property, String newValue) {
    }

    @Override
    public void editEntireSeries(String subject, LocalDateTime start,
                                 String property, String newValue) {
    }

    @Override
    public List<InterfaceEvent> getEventsOnDate(LocalDate date) {
      return new ArrayList<>(events);
    }

    @Override
    public List<InterfaceEvent> getEventsInRange(LocalDateTime start, LocalDateTime end) {
      return new ArrayList<>();
    }

    @Override
    public boolean isBusyAt(LocalDateTime dateTime) {
      return false;
    }

    @Override
    public List<InterfaceEvent> getAllEvents() {
      return new ArrayList<>(events);
    }

    @Override
    public int getEventCount() {
      return events.size();
    }

    @Override
    public boolean hasConflict(InterfaceEvent event) {
      return false;
    }
  }

  private static class MockEvent implements InterfaceEvent {
    @Override
    public String getSubject() {
      return "Test Event";
    }

    @Override
    public void setSubject(String subject) {
    }

    @Override
    public LocalDateTime getStart() {
      return LocalDateTime.of(2025, 1, 15, 10, 0);
    }

    @Override
    public void setStart(LocalDateTime start) {
    }

    @Override
    public LocalDateTime getEnd() {
      return LocalDateTime.of(2025, 1, 15, 11, 0);
    }

    @Override
    public void setEnd(LocalDateTime end) {
    }

    @Override
    public String getDescription() {
      return "";
    }

    @Override
    public void setDescription(String description) {
    }

    @Override
    public String getLocation() {
      return "";
    }

    @Override
    public void setLocation(String location) {
    }

    @Override
    public boolean isPublic() {
      return true;
    }

    @Override
    public void setPublic(boolean isPublic) {
    }

    @Override
    public String getSeriesId() {
      return null;
    }

    @Override
    public void setSeriesId(String seriesId) {
    }

    @Override
    public boolean isPartOfSeries() {
      return false;
    }

    @Override
    public void updateSubject(String newSubject) {
    }

    @Override
    public void updateStart(LocalDateTime newStart) {
    }

    @Override
    public void updateEnd(LocalDateTime newEnd) {
    }

    @Override
    public void updateStatus(String status) {
    }

    @Override
    public boolean overlapsWith(InterfaceEvent other) {
      return false;
    }

    @Override
    public boolean spansMultipleDays() {
      return false;
    }

    @Override
    public long getDurationMinutes() {
      return 60L;
    }

    @Override
    public boolean isAllDay() {
      return false;
    }

    @Override
    public InterfaceEvent copy() {
      return new MockEvent();
    }

    @Override
    public void shiftTime(long minutes) {
    }

    @Override
    public void setDuration(long minutes) {
    }

    @Override
    public boolean occursOnDate(LocalDateTime date) {
      return false;
    }

    @Override
    public boolean isActiveAt(LocalDateTime dateTime) {
      return false;
    }

    @Override
    public boolean matches(InterfaceEvent other) {
      return false;
    }
  }

  private static class MockGuiView implements InterfaceCalendarGuiView {
    private List<String> methodCalls = new ArrayList<>();

    public List<String> getCalls() {
      return methodCalls;
    }

    public void reset() {
      methodCalls.clear();
    }

    @Override
    public void setFeatures(calendar.controller.CalendarGuiController controller) {
      methodCalls.add("setFeatures");
    }

    @Override
    public void setVisible(boolean visible) {
    }

    @Override
    public void showError(String message) {
      methodCalls.add("showError");
    }

    @Override
    public void showMessage(String message) {
      methodCalls.add("showMessage");
    }

    @Override
    public void refreshCalendar() {
      methodCalls.add("refreshCalendar");
    }

    @Override
    public void updateEventsForDate(LocalDate date, List<InterfaceEvent> events) {
    }

    @Override
    public void updateCalendarList(List<String> calendarNames) {
      methodCalls.add("updateCalendarList");
    }

    @Override
    public void setCurrentCalendar(String calendarName) {
      methodCalls.add("setCurrentCalendar");
    }

    @Override
    public LocalDate getCurrentMonth() {
      return LocalDate.now();
    }

    @Override
    public LocalDate getSelectedDate() {
      return null;
    }
  }
}