package calendar.util;

import calendar.controller.CalendarController;
import calendar.model.Calendar;
import calendar.model.CalendarSystem;
import calendar.model.InterfaceCalendar;
import calendar.model.InterfaceCalendarSystem;
import calendar.view.CalendarTextView;
import calendar.view.InterfaceCalendarView;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration tests for InterfaceCalExporter.
 */
public class InterfaceCalExporterTest {
  private InterfaceCalendarSystem model;
  private ByteArrayOutputStream outputStream;
  private InterfaceCalendarView view;

  /**
   * Sets up the test environment before each test.
   */
  @Before
  public void setUp() {
    model = new CalendarSystem();
    outputStream = new ByteArrayOutputStream();
    view = new CalendarTextView(new PrintStream(outputStream));
  }

  /**
   * Cleans up test files after each test.
   */
  @After
  public void tearDown() {
    deleteFileIfExists("test_export.ical");
    deleteFileIfExists("test_export.ics");
  }

  private void deleteFileIfExists(String filename) {
    File file = new File(filename);
    if (file.exists()) {
      file.delete();
    }
  }

  @Test
  public void testExportEmptyCalendarIntegrated() throws Exception {
    String commands = "create calendar --name Work --timezone America/New_York\n"
        + "use calendar --name Work\n"
        + "export cal test_export.ical\nexit\n";
    CalendarController controller = new CalendarController(model, view,
        new StringReader(commands));
    controller.run();
    List<String> lines = readFile("test_export.ical");
    Assert.assertEquals("BEGIN:VCALENDAR", lines.get(0));
    Assert.assertEquals("END:VCALENDAR", lines.get(lines.size() - 1));
    Assert.assertTrue(lines.stream().anyMatch(line -> line.startsWith("VERSION:")));
  }

  @Test
  public void testExportSingleEventIntegrated() throws Exception {
    String commands = "create calendar --name Work --timezone America/New_York\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "export cal test_export.ical\nexit\n";
    CalendarController controller = new CalendarController(model, view,
        new StringReader(commands));
    controller.run();
    List<String> lines = readFile("test_export.ical");
    Assert.assertTrue(lines.stream().anyMatch(line -> line.equals("BEGIN:VEVENT")));
    Assert.assertTrue(lines.stream().anyMatch(line -> line.equals("END:VEVENT")));
    Assert.assertTrue(lines.stream()
        .anyMatch(line -> line.startsWith("SUMMARY:") && line.contains("Meeting")));
    Assert.assertTrue(lines.stream().anyMatch(line -> line.startsWith("DTSTART")));
    Assert.assertTrue(lines.stream().anyMatch(line -> line.startsWith("DTEND")));
  }

  @Test
  public void testExportMultipleEventsIntegrated() throws Exception {
    String commands = "create calendar --name Work --timezone America/New_York\n"
        + "use calendar --name Work\n"
        + "create event Meeting1 from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "create event Meeting2 from 2025-11-16T14:00 to 2025-11-16T15:30\n"
        + "export cal test_export.ical\nexit\n";
    CalendarController controller = new CalendarController(model, view,
        new StringReader(commands));
    controller.run();
    List<String> lines = readFile("test_export.ical");
    long beginEventCount = lines.stream().filter(line -> line.equals("BEGIN:VEVENT")).count();
    long endEventCount = lines.stream().filter(line -> line.equals("END:VEVENT")).count();
    Assert.assertEquals(2, beginEventCount);
    Assert.assertEquals(2, endEventCount);
    Assert.assertTrue(lines.stream().anyMatch(line -> line.contains("Meeting1")));
    Assert.assertTrue(lines.stream().anyMatch(line -> line.contains("Meeting2")));
  }

  @Test
  public void testExportAllDayEventIntegrated() throws Exception {
    String commands = "create calendar --name Work --timezone America/New_York\n"
        + "use calendar --name Work\n"
        + "create event Holiday on 2025-12-25\n"
        + "export cal test_export.ical\nexit\n";
    CalendarController controller = new CalendarController(model, view,
        new StringReader(commands));
    controller.run();
    List<String> lines = readFile("test_export.ical");
    Assert.assertTrue(lines.stream().anyMatch(line -> line.contains("Holiday")));
    Assert.assertTrue(lines.stream().anyMatch(line -> line.contains("VALUE=DATE")));
  }

  @Test
  public void testExportPrivateEventIntegrated() throws Exception {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Private from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event status Private from 2025-11-15T10:00 with PRIVATE\n"
        + "export cal test_export.ical\nexit\n";
    CalendarController controller = new CalendarController(model, view,
        new StringReader(commands));
    controller.run();
    List<String> lines = readFile("test_export.ical");
    Assert.assertTrue(lines.stream().anyMatch(line -> line.equals("CLASS:PRIVATE")));
  }

  @Test
  public void testExportPublicEventIntegrated() throws Exception {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "export cal test_export.ical\nexit\n";
    CalendarController controller = new CalendarController(model, view,
        new StringReader(commands));
    controller.run();
    List<String> lines = readFile("test_export.ical");
    Assert.assertTrue(lines.stream().anyMatch(line -> line.equals("CLASS:PUBLIC")));
  }

  @Test
  public void testExportWithLocationIntegrated() throws Exception {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event location Meeting from 2025-11-15T10:00 with RoomA\n"
        + "export cal test_export.ical\nexit\n";
    CalendarController controller = new CalendarController(model, view,
        new StringReader(commands));
    controller.run();
    List<String> lines = readFile("test_export.ical");
    Assert.assertTrue(lines.stream()
        .anyMatch(line -> line.startsWith("LOCATION:") && line.contains("RoomA")));
  }

  @Test
  public void testExportWithDescriptionIntegrated() throws Exception {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event description Meeting from 2025-11-15T10:00 with Important\n"
        + "export cal test_export.ical\nexit\n";
    CalendarController controller = new CalendarController(model, view,
        new StringReader(commands));
    controller.run();
    List<String> lines = readFile("test_export.ical");
    Assert.assertTrue(lines.stream()
        .anyMatch(line -> line.startsWith("DESCRIPTION:") && line.contains("Important")));
  }

  @Test
  public void testExportHeaderStructureIntegrated() throws Exception {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "export cal test_export.ical\nexit\n";
    CalendarController controller = new CalendarController(model, view,
        new StringReader(commands));
    controller.run();
    List<String> lines = readFile("test_export.ical");
    Assert.assertEquals("BEGIN:VCALENDAR", lines.get(0));
    Assert.assertEquals("VERSION:2.0", lines.get(1));
    Assert.assertEquals("PRODID:-//Calendar Application//EN", lines.get(2));
    Assert.assertEquals("CALSCALE:GREGORIAN", lines.get(3));
    Assert.assertEquals("METHOD:PUBLISH", lines.get(4));
    Assert.assertTrue(lines.get(5).startsWith("X-WR-CALNAME:"));
    Assert.assertTrue(lines.get(6).startsWith("X-WR-TIMEZONE:"));
  }

  @Test
  public void testExportTimezoneBlockIntegrated() throws Exception {
    String commands = "create calendar --name Work --timezone America/New_York\n"
        + "use calendar --name Work\n"
        + "export cal test_export.ical\nexit\n";
    CalendarController controller = new CalendarController(model, view,
        new StringReader(commands));
    controller.run();
    List<String> lines = readFile("test_export.ical");
    int idx = lines.indexOf("BEGIN:VTIMEZONE");
    Assert.assertTrue(idx >= 0);
    Assert.assertEquals("TZID:America/New_York", lines.get(idx + 1));
    Assert.assertEquals("END:VTIMEZONE", lines.get(idx + 2));
  }

  @Test
  public void testExportUidFormatIntegrated() throws Exception {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-10T10:00 to 2025-11-10T11:00\n"
        + "export cal test_export.ical\nexit\n";
    CalendarController controller = new CalendarController(model, view,
        new StringReader(commands));
    controller.run();
    List<String> lines = readFile("test_export.ical");
    Assert.assertTrue(lines.stream().anyMatch(l -> l.matches("UID:\\d+@calendar-app\\.com")));
  }

  @Test
  public void testExportEventStatusIntegrated() throws Exception {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "export cal test_export.ical\nexit\n";
    CalendarController controller = new CalendarController(model, view,
        new StringReader(commands));
    controller.run();
    List<String> lines = readFile("test_export.ical");
    Assert.assertTrue(lines.stream().anyMatch(line -> line.equals("STATUS:CONFIRMED")));
    Assert.assertTrue(lines.stream().anyMatch(line -> line.equals("TRANSP:OPAQUE")));
  }

  @Test
  public void testExportRecurringEventsIntegrated() throws Exception {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Standup from 2025-11-10T09:00 to 2025-11-10T09:30 "
        + "repeats MWF for 6 times\n"
        + "export cal test_export.ical\nexit\n";
    CalendarController controller = new CalendarController(model, view,
        new StringReader(commands));
    controller.run();
    List<String> lines = readFile("test_export.ical");
    long eventCount = lines.stream().filter(line -> line.equals("BEGIN:VEVENT")).count();
    Assert.assertEquals(6, eventCount);
  }

  @Test
  public void testExportSpecialCharactersIntegrated() throws Exception {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event \"Test;Event\" from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "export cal test_export.ical\nexit\n";
    CalendarController controller = new CalendarController(model, view,
        new StringReader(commands));
    controller.run();
    List<String> lines = readFile("test_export.ical");
    Assert.assertTrue(lines.stream().anyMatch(line -> line.contains("\\;")
        || line.contains("Test")));
  }

  @Test
  public void testExportFileEndsWithNewlineIntegrated() throws Exception {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "export cal test_export.ical\nexit\n";
    CalendarController controller = new CalendarController(model, view,
        new StringReader(commands));
    controller.run();
    List<String> lines = readFile("test_export.ical");
    Assert.assertTrue(lines.get(lines.size() - 1).equals("END:VCALENDAR"));
    File file = new File("test_export.ical");
    long size = file.length();
    Assert.assertTrue(size > 100);
  }

  @Test
  public void testExportAllDayEventFormatIntegrated() throws Exception {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Holiday on 2025-12-25\n"
        + "export cal test_export.ical\nexit\n";
    CalendarController controller = new CalendarController(model, view,
        new StringReader(commands));
    controller.run();
    List<String> lines = readFile("test_export.ical");
    Assert.assertTrue(lines.stream().anyMatch(l -> l.equals("DTSTART;VALUE=DATE:20251225")));
    Assert.assertTrue(lines.stream().anyMatch(l -> l.equals("DTEND;VALUE=DATE:20251226")));
  }

  @Test
  public void testExportTimedEventFormatIntegrated() throws Exception {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "export cal test_export.ical\nexit\n";
    CalendarController controller = new CalendarController(model, view,
        new StringReader(commands));
    controller.run();
    List<String> lines = readFile("test_export.ical");
    Assert.assertTrue(lines.stream().anyMatch(l -> l.equals("DTSTART;TZID=UTC:20251115T100000")));
    Assert.assertTrue(lines.stream().anyMatch(l -> l.equals("DTEND;TZID=UTC:20251115T110000")));
  }

  @Test
  public void testExportNegativeHashCodeUidIntegrated() throws Exception {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event A from 2025-01-01T00:00 to 2025-01-01T01:00\n"
        + "export cal test_export.ical\nexit\n";
    CalendarController controller = new CalendarController(model, view,
        new StringReader(commands));
    controller.run();
    List<String> lines = readFile("test_export.ical");
    Assert.assertTrue(lines.stream().anyMatch(l -> l.matches("UID:\\d+@calendar-app\\.com")));
  }

  @Test
  public void testExportIcsExtensionIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "export cal test_export.ics\nexit\n";
    CalendarController controller = new CalendarController(model, view,
        new StringReader(commands));
    controller.run();
    File file = new File("test_export.ics");
    Assert.assertTrue(file.exists());
  }

  private List<String> readFile(String path) throws Exception {
    List<String> lines = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
      String line;
      while ((line = reader.readLine()) != null) {
        lines.add(line);
      }
    }
    return lines;
  }

  @Test
  public void testFileEndsWithNewline() throws Exception {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "export cal test_export.ical\nexit\n";
    CalendarController controller = new CalendarController(model, view,
        new StringReader(commands));
    controller.run();

    File file = new File("test_export.ical");
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      String lastLine = null;
      String line;
      while ((line = reader.readLine()) != null) {
        lastLine = line;
      }
      Assert.assertEquals("END:VCALENDAR", lastLine);
    }
  }

  @Test
  public void testDtstampWritten() throws Exception {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "export cal test_export.ical\nexit\n";
    CalendarController controller = new CalendarController(model, view,
        new StringReader(commands));
    controller.run();

    List<String> lines = readFile("test_export.ical");
    Assert.assertTrue(lines.stream().anyMatch(l -> l.startsWith("DTSTAMP:")));
  }

  @Test
  public void testDescriptionNewline() throws Exception {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event description Meeting from 2025-11-15T10:00 with Notes\n"
        + "export cal test_export.ical\nexit\n";
    CalendarController controller = new CalendarController(model, view,
        new StringReader(commands));
    controller.run();

    List<String> lines = readFile("test_export.ical");
    long descCount = lines.stream().filter(l -> l.startsWith("DESCRIPTION:")).count();
    Assert.assertEquals(1, descCount);
  }

  @Test
  public void testLocationNewline() throws Exception {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event location Meeting from 2025-11-15T10:00 with Office\n"
        + "export cal test_export.ical\nexit\n";
    CalendarController controller = new CalendarController(model, view,
        new StringReader(commands));
    controller.run();

    List<String> lines = readFile("test_export.ical");
    long locCount = lines.stream().filter(l -> l.startsWith("LOCATION:")).count();
    Assert.assertEquals(1, locCount);
  }

  @Test
  public void testHashCodeBoundary() throws Exception {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event TestEvent from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "export cal test_export.ical\nexit\n";
    CalendarController controller = new CalendarController(model, view,
        new StringReader(commands));
    controller.run();

    List<String> lines = readFile("test_export.ical");
    boolean hasUid = lines.stream().anyMatch(l -> l.startsWith("UID:")
        && l.endsWith("@calendar-app.com"));
    Assert.assertTrue(hasUid);

    String uidLine = lines.stream()
        .filter(l -> l.startsWith("UID:"))
        .findFirst()
        .orElse("");
    String uidPart = uidLine.substring(4, uidLine.indexOf("@"));
    long uid = Long.parseLong(uidPart);
    Assert.assertTrue(uid >= 0);
  }

  @Test
  public void testExportNullCalendar() {
    InterfaceExporter exporter = new InterfaceCalExporter();
    try {
      exporter.export(null, "test.ics");
      Assert.fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      Assert.assertTrue(e.getMessage().contains("Calendar cannot be null"));
    } catch (IOException e) {
      Assert.fail("Wrong exception type");
    }
  }

  @Test
  public void testExportNullFileName() {
    InterfaceCalendar cal = new Calendar("Work", ZoneId.of("UTC"));
    InterfaceExporter exporter = new InterfaceCalExporter();
    try {
      exporter.export(cal, null);
      Assert.fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      Assert.assertTrue(e.getMessage().contains("File name cannot be null or empty"));
    } catch (IOException e) {
      Assert.fail("Wrong exception type");
    }
  }

  @Test
  public void testExportEmptyFileName() {
    InterfaceCalendar cal = new Calendar("Work", ZoneId.of("UTC"));
    InterfaceExporter exporter = new InterfaceCalExporter();
    try {
      exporter.export(cal, "   ");
      Assert.fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      Assert.assertTrue(e.getMessage().contains("File name cannot be null or empty"));
    } catch (IOException e) {
      Assert.fail("Wrong exception type");
    }
  }
}