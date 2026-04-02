package calendar.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import calendar.controller.CalendarController;
import calendar.view.CalendarTextView;
import calendar.view.InterfaceCalendarView;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for ConflictDetector.
 * Tests conflict detection logic between events.
 */
public class ConflictDetectorTest {
  private InterfaceCalendarSystem model;
  private ByteArrayOutputStream outputStream;
  private InterfaceCalendarView view;
  private ConflictDetector detector;

  /**
   * Sets up test fixtures before each test.
   */
  @Before
  public void setUp() {
    model = new CalendarSystem();
    outputStream = new ByteArrayOutputStream();
    view = new CalendarTextView(new PrintStream(outputStream));
    detector = new ConflictDetector();
  }

  @Test
  public void testHasConflictNullInputs() {
    List<InterfaceEvent> events = new ArrayList<>();
    assertFalse(detector.hasConflict(null, events));

    InterfaceEvent event = new Event("Test",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));
    assertFalse(detector.hasConflict(event, null));
  }

  @Test
  public void testHasConflict() {
    InterfaceEvent event1 = new Event("Event1",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));
    InterfaceEvent event2 = new Event("Event2",
        LocalDateTime.of(2025, 11, 15, 10, 30),
        LocalDateTime.of(2025, 11, 15, 11, 30));

    List<InterfaceEvent> events = new ArrayList<>();
    events.add(event1);

    assertTrue(detector.hasConflict(event2, events));
  }

  @Test
  public void testNoConflict() {
    InterfaceEvent event1 = new Event("Event1",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));
    InterfaceEvent event2 = new Event("Event2",
        LocalDateTime.of(2025, 11, 15, 11, 0),
        LocalDateTime.of(2025, 11, 15, 12, 0));

    List<InterfaceEvent> events = new ArrayList<>();
    events.add(event1);

    assertFalse(detector.hasConflict(event2, events));
  }

  @Test
  public void testIsDuplicateNullInputs() {
    List<InterfaceEvent> events = new ArrayList<>();
    assertFalse(detector.isDuplicate(null, events));

    InterfaceEvent event = new Event("Test",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));
    assertFalse(detector.isDuplicate(event, null));
  }

  @Test
  public void testIsDuplicate() {
    InterfaceEvent event1 = new Event("Meeting",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));
    InterfaceEvent event2 = new Event("Meeting",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));

    List<InterfaceEvent> events = new ArrayList<>();
    events.add(event1);

    assertTrue(detector.isDuplicate(event2, events));
  }

  @Test
  public void testNotDuplicate() {
    InterfaceEvent event1 = new Event("Meeting1",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));

    List<InterfaceEvent> events = new ArrayList<>();
    events.add(event1);

    InterfaceEvent event2 = new Event("Meeting2",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));
    assertFalse(detector.isDuplicate(event2, events));

    InterfaceEvent event3 = new Event("Meeting1",
        LocalDateTime.of(2025, 11, 15, 10, 30),
        LocalDateTime.of(2025, 11, 15, 11, 0));
    assertFalse(detector.isDuplicate(event3, events));

    InterfaceEvent event4 = new Event("Meeting1",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 30));
    assertFalse(detector.isDuplicate(event4, events));
  }

  @Test
  public void testFindConflictingEventsNullInputs() {
    List<InterfaceEvent> events = new ArrayList<>();
    List<InterfaceEvent> conflicts = detector.findConflictingEvents(null, events);
    assertTrue(conflicts.isEmpty());

    InterfaceEvent event = new Event("Test",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));
    conflicts = detector.findConflictingEvents(event, null);
    assertTrue(conflicts.isEmpty());
  }

  @Test
  public void testFindConflictingEvents() {
    InterfaceEvent event1 = new Event("Event1",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));
    InterfaceEvent event2 = new Event("Event2",
        LocalDateTime.of(2025, 11, 15, 10, 30),
        LocalDateTime.of(2025, 11, 15, 11, 30));
    InterfaceEvent newEvent = new Event("New",
        LocalDateTime.of(2025, 11, 15, 10, 15),
        LocalDateTime.of(2025, 11, 15, 10, 45));

    List<InterfaceEvent> events = new ArrayList<>();
    events.add(event1);
    events.add(event2);

    List<InterfaceEvent> conflicts = detector.findConflictingEvents(newEvent, events);
    assertEquals(2, conflicts.size());
  }

  @Test
  public void testFindConflictingEventsNone() {
    InterfaceEvent event1 = new Event("Event1",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));
    InterfaceEvent newEvent = new Event("New",
        LocalDateTime.of(2025, 11, 15, 11, 0),
        LocalDateTime.of(2025, 11, 15, 12, 0));

    List<InterfaceEvent> events = new ArrayList<>();
    events.add(event1);

    List<InterfaceEvent> conflicts = detector.findConflictingEvents(newEvent, events);
    assertTrue(conflicts.isEmpty());
  }

  @Test
  public void testCanScheduleNullInputs() {
    List<InterfaceEvent> events = new ArrayList<>();
    assertTrue(detector.canSchedule(null, events));

    InterfaceEvent event = new Event("Test",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));
    assertTrue(detector.canSchedule(event, null));
  }

  @Test
  public void testCanSchedule() {
    InterfaceEvent event1 = new Event("Event1",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));
    InterfaceEvent newEvent = new Event("New",
        LocalDateTime.of(2025, 11, 15, 11, 0),
        LocalDateTime.of(2025, 11, 15, 12, 0));

    List<InterfaceEvent> events = new ArrayList<>();
    events.add(event1);

    assertTrue(detector.canSchedule(newEvent, events));
  }

  @Test
  public void testCannotScheduleConflict() {
    InterfaceEvent event1 = new Event("Event1",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));
    InterfaceEvent newEvent = new Event("New",
        LocalDateTime.of(2025, 11, 15, 10, 30),
        LocalDateTime.of(2025, 11, 15, 11, 30));

    List<InterfaceEvent> events = new ArrayList<>();
    events.add(event1);

    assertFalse(detector.canSchedule(newEvent, events));
  }

  @Test
  public void testCannotScheduleDuplicate() {
    InterfaceEvent event1 = new Event("Meeting",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));
    InterfaceEvent newEvent = new Event("Meeting",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));

    List<InterfaceEvent> events = new ArrayList<>();
    events.add(event1);

    assertFalse(detector.canSchedule(newEvent, events));
  }

  @Test
  public void testFindAvailableSlotsInvalidInputs() {
    List<InterfaceEvent> events = new ArrayList<>();

    List<LocalDateTime> slots = detector.findAvailableSlots(null, 60, events);
    assertTrue(slots.isEmpty());

    LocalDate date = LocalDate.of(2025, 11, 15);
    slots = detector.findAvailableSlots(date, 0, events);
    assertTrue(slots.isEmpty());

    slots = detector.findAvailableSlots(date, -60, events);
    assertTrue(slots.isEmpty());
  }

  @Test
  public void testFindAvailableSlots() {
    LocalDate date = LocalDate.of(2025, 11, 15);
    List<InterfaceEvent> events = new ArrayList<>();

    List<LocalDateTime> slots = detector.findAvailableSlots(date, 60, events);
    assertFalse(slots.isEmpty());
    assertTrue(slots.size() > 10);
  }

  @Test
  public void testFindAvailableSlotsWithEvents() {
    LocalDate date = LocalDate.of(2025, 11, 15);
    InterfaceEvent event = new Event("Meeting",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));

    List<InterfaceEvent> events = new ArrayList<>();
    events.add(event);

    List<LocalDateTime> slots = detector.findAvailableSlots(date, 60, events);
    assertFalse(slots.isEmpty());

    for (LocalDateTime slot : slots) {
      LocalDateTime slotEnd = slot.plusMinutes(60);
      assertFalse(slot.isBefore(event.getEnd()) && slotEnd.isAfter(event.getStart()));
    }
  }

  @Test
  public void testCountConflictsNullInputs() {
    List<InterfaceEvent> events = new ArrayList<>();
    assertEquals(0, detector.countConflicts(null, events));

    InterfaceEvent event = new Event("Test",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));
    assertEquals(0, detector.countConflicts(event, null));
  }

  @Test
  public void testCountConflicts() {
    InterfaceEvent event1 = new Event("Event1",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));
    InterfaceEvent newEvent = new Event("New",
        LocalDateTime.of(2025, 11, 15, 11, 0),
        LocalDateTime.of(2025, 11, 15, 12, 0));

    List<InterfaceEvent> events = new ArrayList<>();
    events.add(event1);

    assertEquals(0, detector.countConflicts(newEvent, events));
  }

  @Test
  public void testCountMultipleConflicts() {
    InterfaceEvent event1 = new Event("Event1",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));
    InterfaceEvent event2 = new Event("Event2",
        LocalDateTime.of(2025, 11, 15, 10, 30),
        LocalDateTime.of(2025, 11, 15, 11, 30));
    InterfaceEvent newEvent = new Event("New",
        LocalDateTime.of(2025, 11, 15, 10, 15),
        LocalDateTime.of(2025, 11, 15, 10, 45));

    List<InterfaceEvent> events = new ArrayList<>();
    events.add(event1);
    events.add(event2);

    assertEquals(2, detector.countConflicts(newEvent, events));
  }

  @Test
  public void testIsTimeRangeFreeNullInputs() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 15, 11, 0);
    List<InterfaceEvent> events = new ArrayList<>();

    assertTrue(detector.isTimeRangeFree(null, end, events));
    assertTrue(detector.isTimeRangeFree(start, null, events));
    assertTrue(detector.isTimeRangeFree(start, end, null));
  }

  @Test
  public void testIsTimeRangeFree() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 15, 11, 0);
    List<InterfaceEvent> events = new ArrayList<>();

    assertTrue(detector.isTimeRangeFree(start, end, events));

    InterfaceEvent event = new Event("Meeting",
        LocalDateTime.of(2025, 11, 15, 11, 0),
        LocalDateTime.of(2025, 11, 15, 12, 0));
    events.add(event);

    assertTrue(detector.isTimeRangeFree(start, end, events));
  }

  @Test
  public void testIsTimeRangeNotFree() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 15, 11, 0);

    InterfaceEvent event = new Event("Meeting",
        LocalDateTime.of(2025, 11, 15, 10, 30),
        LocalDateTime.of(2025, 11, 15, 11, 30));

    List<InterfaceEvent> events = new ArrayList<>();
    events.add(event);

    assertFalse(detector.isTimeRangeFree(start, end, events));
  }

  @Test
  public void testConflictIntegration() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting1 from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "create event Meeting2 from 2025-11-15T10:30 to 2025-11-15T11:30\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertEquals(2, model.getCalendar("Work").getEventCount());
  }

  @Test
  public void testNoConflictIntegration() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting1 from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "create event Meeting2 from 2025-11-15T11:00 to 2025-11-15T12:00\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertEquals(2, model.getCalendar("Work").getEventCount());
  }

  @Test
  public void testDuplicateIntegration() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertEquals(1, model.getCalendar("Work").getEventCount());
    String output = outputStream.toString();
    assertTrue(output.contains("ERROR"));
  }
}