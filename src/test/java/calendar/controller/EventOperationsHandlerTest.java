package calendar.controller;

import static org.junit.Assert.assertEquals;
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
 * Test suite for EventOperationsHandler.
 * Tests event management operations with mock implementations.
 */
public class EventOperationsHandlerTest {

  private EventOperationsHandler handler;
  private MockCalendarSystem mockModel;
  private MockGuiView mockView;

  /**
   * Sets up test fixtures before each test.
   */
  @Before
  public void setUp() {
    mockModel = new MockCalendarSystem();
    mockView = new MockGuiView();
    handler = new EventOperationsHandler(mockModel, mockView);
    mockModel.createCalendar("TestCal", "UTC");
  }

  @Test
  public void testCreateEvent() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 15, 11, 0);
    EventCreationRequest request = new EventCreationRequest.Builder("Meeting", start, end).build();

    handler.createEvent("TestCal", request);
    assertTrue(mockModel.getCalls().contains("createEvent:Meeting"));
    assertTrue(mockView.getCalls().contains("refreshCalendar"));
    assertTrue(mockView.getCalls().contains("showMessage"));

    mockView.reset();
    handler.createEvent("NonExistent", request);
    assertTrue(mockView.getCalls().contains("showError"));
  }

  @Test
  public void testCreateRecurringEvent() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 10, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 10, 9, 30);
    RecurringEventCreationRequest request = new RecurringEventCreationRequest.Builder(
        "Standup", start, end, "MWF").repeatCount(5).build();

    handler.createRecurringEvent("TestCal", request);
    assertTrue(mockModel.getCalls().contains("createRecurringEvent:Standup"));
    assertTrue(mockView.getCalls().contains("refreshCalendar"));
    assertTrue(mockView.getCalls().contains("showMessage"));

    mockView.reset();
    handler.createRecurringEvent("NonExistent", request);
    assertTrue(mockView.getCalls().contains("showError"));
  }

  @Test
  public void testEditEvent() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 15, 10, 0);

    handler.editEvent("TestCal", "Meeting", start, "location", "Room 101");
    assertTrue(mockView.getCalls().contains("refreshCalendar"));
    assertTrue(mockView.getCalls().contains("showMessage"));

    mockView.reset();
    handler.editEvent("NonExistent", "Meeting", start, "location", "Room 101");
    assertTrue(mockView.getCalls().contains("showError"));
  }

  @Test
  public void testEditEventActuallyEdits() {
    MockCalendar calendar = mockModel.getMockCalendar("TestCal");
    calendar.setTrackCalls(true);
    LocalDateTime start = LocalDateTime.of(2025, 11, 15, 10, 0);

    handler.editEvent("TestCal", "Meeting", start, "location", "Room 101");

    assertTrue(calendar.getCalls().contains("editEvent"));
  }

  @Test
  public void testEditEventsFromDate() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 15, 10, 0);

    handler.editEventsFromDate("TestCal", "Series", start, "location", "Room 202");
    assertTrue(mockView.getCalls().contains("refreshCalendar"));
    assertTrue(mockView.getCalls().contains("showMessage"));

    mockView.reset();
    handler.editEventsFromDate("NonExistent", "Series", start, "location", "Room 202");
    assertTrue(mockView.getCalls().contains("showError"));
  }

  @Test
  public void testEditEventsFromDateActuallyEdits() {
    MockCalendar calendar = mockModel.getMockCalendar("TestCal");
    calendar.setTrackCalls(true);
    LocalDateTime start = LocalDateTime.of(2025, 11, 15, 10, 0);

    handler.editEventsFromDate("TestCal", "Series", start, "location", "Room 202");

    assertTrue(calendar.getCalls().contains("editEventsFromDate"));
  }

  @Test
  public void testEditEntireSeries() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 15, 10, 0);

    handler.editEntireSeries("TestCal", "Series", start, "subject", "Updated");
    assertTrue(mockView.getCalls().contains("refreshCalendar"));
    assertTrue(mockView.getCalls().contains("showMessage"));

    mockView.reset();
    handler.editEntireSeries("NonExistent", "Series", start, "subject", "Updated");
    assertTrue(mockView.getCalls().contains("showError"));
  }

  @Test
  public void testEditEntireSeriesActuallyEdits() {
    MockCalendar calendar = mockModel.getMockCalendar("TestCal");
    calendar.setTrackCalls(true);
    LocalDateTime start = LocalDateTime.of(2025, 11, 15, 10, 0);

    handler.editEntireSeries("TestCal", "Series", start, "subject", "Updated");

    assertTrue(calendar.getCalls().contains("editEntireSeries"));
  }

  @Test
  public void testGetEventsForDate() {
    LocalDate date = LocalDate.of(2025, 11, 15);

    List<InterfaceEvent> events = handler.getEventsForDate("TestCal", date);
    assertNotNull(events);

    mockView.reset();
    events = handler.getEventsForDate("NonExistent", date);
    assertNotNull(events);
    assertEquals(0, events.size());
    assertTrue(mockView.getCalls().contains("showError"));
  }

  @Test
  public void testGetEventsForDateReturnsActual() {
    MockCalendar calendar = mockModel.getMockCalendar("TestCal");
    calendar.addTestEvent();
    LocalDate date = LocalDate.of(2025, 11, 15);

    List<InterfaceEvent> events = handler.getEventsForDate("TestCal", date);
    assertNotNull(events);
    assertEquals(1, events.size());
  }

  @Test
  public void testGetAllEvents() {
    List<InterfaceEvent> events = handler.getAllEvents("TestCal");
    assertNotNull(events);

    mockView.reset();
    events = handler.getAllEvents("NonExistent");
    assertNotNull(events);
    assertEquals(0, events.size());
    assertTrue(mockView.getCalls().contains("showError"));
  }

  @Test
  public void testGetAllEventsReturnsActual() {
    MockCalendar calendar = mockModel.getMockCalendar("TestCal");
    calendar.addTestEvent();

    List<InterfaceEvent> events = handler.getAllEvents("TestCal");
    assertNotNull(events);
    assertEquals(1, events.size());
  }

  private static class MockCalendarSystem implements InterfaceCalendarSystem {
    private List<String> methodCalls = new ArrayList<>();
    private Map<String, MockCalendar> calendars = new HashMap<>();

    public List<String> getCalls() {
      return methodCalls;
    }

    public MockCalendar getMockCalendar(String name) {
      return calendars.get(name);
    }

    @Override
    public void createCalendar(String name, String timezone) {
      methodCalls.add("createCalendar:" + name);
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
      return calendars.containsKey(name);
    }

    @Override
    public void editCalendarName(String oldName, String newName) {
      if (!calendars.containsKey(oldName)) {
        throw new IllegalArgumentException("Calendar not found");
      }
      MockCalendar cal = calendars.remove(oldName);
      calendars.put(newName, cal);
    }

    @Override
    public void editCalendarTimezone(String calendarName, String newTimezone) {
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
      methodCalls.add("createEvent:" + subject);
    }

    @Override
    public void createEvent(String calendarName, EventCreationRequest request) {
      methodCalls.add("createEvent:" + request.getSubject());
      if (!calendars.containsKey(calendarName)) {
        throw new IllegalArgumentException("Calendar not found");
      }
    }

    @Override
    public void createRecurringEvent(String calendarName,
                                     RecurringEventCreationRequest request) {
      methodCalls.add("createRecurringEvent:" + request.getSubject());
      if (!calendars.containsKey(calendarName)) {
        throw new IllegalArgumentException("Calendar not found");
      }
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
    private List<String> methodCalls = new ArrayList<>();
    private boolean trackCalls = false;

    MockCalendar(String name, String timezone) {
      this.name = name;
      this.timezone = timezone;
    }

    public void setTrackCalls(boolean track) {
      this.trackCalls = track;
    }

    public List<String> getCalls() {
      return methodCalls;
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
      if (trackCalls) {
        methodCalls.add("editEvent");
      }
    }

    @Override
    public void editEventsFromDate(String subject, LocalDateTime start,
                                   String property, String newValue) {
      if (trackCalls) {
        methodCalls.add("editEventsFromDate");
      }
    }

    @Override
    public void editEntireSeries(String subject, LocalDateTime start,
                                 String property, String newValue) {
      if (trackCalls) {
        methodCalls.add("editEntireSeries");
      }
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
    }

    @Override
    public void setCurrentCalendar(String calendarName) {
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