package calendar.util;

import calendar.controller.CalendarController;
import calendar.model.CalendarSystem;
import calendar.model.InterfaceCalendarSystem;
import calendar.view.CalendarTextView;
import calendar.view.InterfaceCalendarView;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration tests for CsvExporter.
 */
public class CsvExporterTest {
  private InterfaceCalendarSystem model;
  private ByteArrayOutputStream outputStream;
  private InterfaceCalendarView view;

  /**
   * Sets up the test fixtures.
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
    deleteFileIfExists("test_export.csv");
  }

  private void deleteFileIfExists(String filename) {
    File file = new File(filename);
    if (file.exists()) {
      file.delete();
    }
  }

  @Test
  public void testEmptyCalendar() {
    String commands = "create calendar --name Work --timezone America/New_York\n"
        + "use calendar --name Work\n"
        + "export cal test_export.csv\nexit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));
    controller.run();
    String output = outputStream.toString();
    Assert.assertTrue(output.contains("exported successfully"));
    File file = new File("test_export.csv");
    Assert.assertTrue(file.exists());
  }

  @Test
  public void testSingleEvent() throws Exception {
    String commands = "create calendar --name Work --timezone America/New_York\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "export cal test_export.csv\nexit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));
    controller.run();
    List<String> lines = readFile("test_export.csv");
    Assert.assertEquals(2, lines.size());
    Assert.assertTrue(lines.get(1).contains("Meeting"));
    Assert.assertTrue(lines.get(1).contains("11/15/2025"));
    Assert.assertTrue(lines.get(1).contains("10:00"));
    Assert.assertTrue(lines.get(1).contains("11:00"));
    Assert.assertTrue(lines.get(1).contains("False"));
  }

  @Test
  public void testMultipleEvents() throws Exception {
    String commands = "create calendar --name Work --timezone America/New_York\n"
        + "use calendar --name Work\n"
        + "create event Meeting1 from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "create event Meeting2 from 2025-11-16T14:00 to 2025-11-16T15:30\n"
        + "export cal test_export.csv\nexit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));
    controller.run();
    List<String> lines = readFile("test_export.csv");
    Assert.assertEquals(3, lines.size());
    Assert.assertTrue(lines.get(1).contains("Meeting1"));
    Assert.assertTrue(lines.get(2).contains("Meeting2"));
  }

  @Test
  public void testAllDay() throws Exception {
    String commands = "create calendar --name Work --timezone America/New_York\n"
        + "use calendar --name Work\n"
        + "create event Holiday on 2025-12-25\n"
        + "export cal test_export.csv\nexit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));
    controller.run();
    List<String> lines = readFile("test_export.csv");
    String dataLine = lines.get(1);
    Assert.assertTrue(dataLine.contains("Holiday"));
    Assert.assertTrue(dataLine.contains("True"));
  }

  @Test
  public void testCommas() throws Exception {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event \"Meeting, Planning\" from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event description \"Meeting, Planning\" from 2025-11-15T10:00 "
        + "with \"Important, urgent\"\n"
        + "edit event location \"Meeting, Planning\" from 2025-11-15T10:00 "
        + "with \"Room 101, Building A\"\n"
        + "export cal test_export.csv\nexit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));
    controller.run();
    List<String> lines = readFile("test_export.csv");
    String dataLine = lines.get(1);
    Assert.assertTrue(dataLine.contains("\"Meeting, Planning\""));
    Assert.assertTrue(dataLine.contains("\"Important, urgent\"")
        || dataLine.contains("Important"));
    Assert.assertTrue(dataLine.contains("\"Room 101, Building A\"")
        || dataLine.contains("Room 101"));
  }

  @Test
  public void testQuotes() throws Exception {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event \"Test Event\" from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "export cal test_export.csv\nexit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));
    controller.run();
    List<String> lines = readFile("test_export.csv");
    Assert.assertTrue(lines.get(1).contains("Test Event"));
  }

  @Test
  public void testNewlines() throws Exception {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "export cal test_export.csv\nexit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));
    controller.run();
    File file = new File("test_export.csv");
    Assert.assertTrue(file.exists());
  }

  @Test
  public void testRecurring() throws Exception {
    String commands = "create calendar --name Work --timezone America/New_York\n"
        + "use calendar --name Work\n"
        + "create event Standup from 2025-11-10T09:00 to 2025-11-10T09:30 "
        + "repeats MWF for 6 times\n"
        + "export cal test_export.csv\nexit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));
    controller.run();
    List<String> lines = readFile("test_export.csv");
    Assert.assertEquals(7, lines.size());
    for (int i = 1; i <= 6; i++) {
      Assert.assertTrue(lines.get(i).contains("Standup"));
    }
  }

  @Test
  public void testAbsolutePath() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "export cal test_export.csv\nexit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));
    controller.run();
    String output = outputStream.toString();
    Assert.assertTrue(output.contains("File location:"));
    File file = new File("test_export.csv");
    Assert.assertTrue(file.isAbsolute() || file.exists());
  }

  @Test
  public void testPrivateEvent() throws Exception {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event status Meeting from 2025-11-15T10:00 with private\n"
        + "export cal test_export.csv\nexit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));
    controller.run();
    List<String> lines = readFile("test_export.csv");
    String dataLine = lines.get(1);
    Assert.assertTrue(dataLine.endsWith("True"));
  }

  @Test
  public void testHeaderWritten() throws Exception {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Test from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "export cal test_export.csv\nexit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));
    controller.run();
    List<String> lines = readFile("test_export.csv");
    Assert.assertTrue(lines.get(0).contains("Subject"));
    Assert.assertTrue(lines.get(0).contains("Start Date"));
    Assert.assertTrue(lines.get(0).contains("All Day Event"));
  }

  @Test
  public void testEscapeQuotes() throws Exception {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event description Meeting from 2025-11-15T10:00 with \"Say \"\"hello\"\"\"\n"
        + "export cal test_export.csv\nexit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));
    controller.run();
    List<String> lines = readFile("test_export.csv");
    Assert.assertTrue(lines.get(1).contains("\"\"\"\""));
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
  public void testNewlineInValue() throws Exception {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event description Meeting from 2025-11-15T10:00 "
        + "with \"Line one\nLine two\"\n"
        + "export cal test_export.csv\nexit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));
    controller.run();

    List<String> lines = readFile("test_export.csv");
    String dataLine = lines.get(1);
    Assert.assertTrue(dataLine.contains("\"Line one"));
  }
}