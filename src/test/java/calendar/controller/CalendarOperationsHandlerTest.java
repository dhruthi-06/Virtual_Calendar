package calendar.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import calendar.model.EventCreationRequest;
import calendar.model.InterfaceCalendar;
import calendar.model.InterfaceCalendarSystem;
import calendar.model.InterfaceEvent;
import calendar.model.RecurringEventCreationRequest;
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

/**
 * Test suite for CalendarOperationsHandler.
 * Tests calendar management operations with mock implementations.
 */
public class CalendarOperationsHandlerTest {

  private CalendarOperationsHandler handler;
  private MockCalendarSystem mockModel;
  private MockGuiView mockView;

  /**
   * Sets up test fixtures before each test.
   */
  @Before
  public void setUp() {
    mockModel = new MockCalendarSystem();
    mockView = new MockGuiView();
    handler = new CalendarOperationsHandler(mockModel, mockView);
    mockModel.createCalendar("Default", "UTC");
  }

  @Test
  public void testCreateCalendar() {
    handler.createCalendar("Work", "America/New_York");
    assertTrue(mockModel.getCalls().contains("createCalendar:Work:America/New_York"));
    assertTrue(mockView.getCalls().contains("updateCalendarList"));
    assertTrue(mockView.getCalls().contains("showMessage"));

    handler.createCalendar("Work", "UTC");
    assertTrue(mockView.getCalls().contains("showError"));
  }

  @Test
  public void testSwitchCalendar() {
    String result = handler.switchCalendar("Default", "OldCal");
    assertEquals("Default", result);
    assertTrue(mockView.getCalls().contains("setCurrentCalendar"));
    assertTrue(mockView.getCalls().contains("refreshCalendar"));

    mockView.reset();
    result = handler.switchCalendar("NonExistent", "Default");
    assertEquals("Default", result);
    assertTrue(mockView.getCalls().contains("showError"));

    mockModel.shouldThrowOnExists = true;
    mockView.reset();
    result = handler.switchCalendar("Default", "OldCal");
    assertEquals("OldCal", result);
    assertTrue(mockView.getCalls().contains("showError"));
  }

  @Test
  public void testEditCalendarName() {
    String result = handler.editCalendarName("Default", "NewDefault", "Default");
    assertEquals("NewDefault", result);
    assertTrue(mockModel.getCalls().contains("editCalendarName:Default:NewDefault"));
    assertTrue(mockView.getCalls().contains("setCurrentCalendar"));
    assertTrue(mockView.getCalls().contains("updateCalendarList"));
    assertTrue(mockView.getCalls().contains("showMessage"));

    mockModel.createCalendar("Other", "UTC");
    mockView.reset();
    result = handler.editCalendarName("Other", "NewOther", "Default");
    assertEquals("Default", result);
    assertTrue(mockView.getCalls().contains("updateCalendarList"));
    assertFalse(mockView.getCalls().contains("setCurrentCalendar"));

    mockView.reset();
    result = handler.editCalendarName("NonExistent", "New", "Default");
    assertEquals("Default", result);
    assertTrue(mockView.getCalls().contains("showError"));
  }

  @Test
  public void testEditCalendarTimezone() {
    handler.editCalendarTimezone("Default", "America/New_York", "Default");
    assertTrue(mockModel.getCalls().contains("editCalendarTimezone:Default:America/New_York"));
    assertTrue(mockView.getCalls().contains("refreshCalendar"));
    assertTrue(mockView.getCalls().contains("showMessage"));

    mockModel.createCalendar("Other", "UTC");
    mockView.reset();
    handler.editCalendarTimezone("Other", "America/Chicago", "Default");
    assertTrue(mockModel.getCalls().contains("editCalendarTimezone:Other:America/Chicago"));
    assertFalse(mockView.getCalls().contains("refreshCalendar"));

    mockView.reset();
    handler.editCalendarTimezone("NonExistent", "UTC", "Default");
    assertTrue(mockView.getCalls().contains("showError"));
  }

  @Test
  public void testGetAllCalendarNames() {
    List<String> names = handler.getAllCalendarNames();
    assertNotNull(names);
    assertTrue(names.contains("Default"));
  }

  @Test
  public void testGetCalendarTimezone() {
    String timezone = handler.getCalendarTimezone("Default");
    assertEquals("UTC", timezone);

    timezone = handler.getCalendarTimezone("NonExistent");
    assertEquals(ZoneId.systemDefault().getId(), timezone);
  }

  @Test
  public void testCalendarExists() {
    assertTrue(handler.calendarExists("Default"));
    assertFalse(handler.calendarExists("NonExistent"));
  }

  private static class MockCalendarSystem implements InterfaceCalendarSystem {
    private List<String> methodCalls = new ArrayList<>();
    private Map<String, MockCalendar> calendars = new HashMap<>();
    public boolean shouldThrowOnExists = false;

    public List<String> getCalls() {
      return methodCalls;
    }

    public void reset() {
      methodCalls.clear();
    }

    @Override
    public void createCalendar(String name, String timezone) {
      methodCalls.add("createCalendar:" + name + ":" + timezone);
      if (calendars.containsKey(name)) {
        throw new IllegalArgumentException("Calendar already exists");
      }
      calendars.put(name, new MockCalendar(name, timezone));
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
      if (shouldThrowOnExists) {
        throw new RuntimeException("Test exception");
      }
      return calendars.containsKey(name);
    }

    @Override
    public void editCalendarName(String oldName, String newName) {
      methodCalls.add("editCalendarName:" + oldName + ":" + newName);
      if (!calendars.containsKey(oldName)) {
        throw new IllegalArgumentException("Calendar not found");
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
    public void createEvent(String calendarName, EventCreationRequest request) {
    }

    @Override
    public void createRecurringEvent(String calendarName,
                                     RecurringEventCreationRequest request) {
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

    MockCalendar(String name, String timezone) {
      this.name = name;
      this.timezone = timezone;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public void setName(String name) {
      this.name = name;
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
      return new ArrayList<>();
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
      return new ArrayList<>();
    }

    @Override
    public int getEventCount() {
      return 0;
    }

    @Override
    public boolean hasConflict(InterfaceEvent event) {
      return false;
    }
  }

  private static class MockGuiView implements InterfaceCalendarGuiView {
    private List<String> methodCalls = new ArrayList<>();
    public String lastMessage;
    public String lastError;

    public List<String> getCalls() {
      return methodCalls;
    }

    public void reset() {
      methodCalls.clear();
      lastMessage = null;
      lastError = null;
    }

    @Override
    public void setFeatures(calendar.controller.CalendarGuiController controller) {
    }

    @Override
    public void setVisible(boolean visible) {
    }

    @Override
    public void showError(String message) {
      methodCalls.add("showError");
      this.lastError = message;
    }

    @Override
    public void showMessage(String message) {
      methodCalls.add("showMessage");
      this.lastMessage = message;
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