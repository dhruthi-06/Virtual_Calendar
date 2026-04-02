package calendar.controller.commands;

import calendar.controller.CalendarController;
import calendar.model.CalendarSystem;
import calendar.model.InterfaceCalendar;
import calendar.model.InterfaceCalendarSystem;
import calendar.view.CalendarTextView;
import calendar.view.InterfaceCalendarView;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.StringReader;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for ExportCalendarCommand.
 */
public class ExportCalendarCommandTest {
  private InterfaceCalendarSystem model;
  private InterfaceCalendarView view;
  private ByteArrayOutputStream outputStream;

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
    deleteFileIfExists("test_export.csv");
    deleteFileIfExists("test_export.ical");
    deleteFileIfExists("test_export.ics");
    deleteFileIfExists("empty.csv");
    deleteFileIfExists("recurring.csv");
    deleteFileIfExists("full.csv");
  }

  private void deleteFileIfExists(String filename) {
    File file = new File(filename);
    if (file.exists()) {
      file.delete();
    }
  }

  @Test
  public void testExportCsvIntegrated() {
    String commands = "create calendar --name Work --timezone America/New_York\n"
        + "use calendar --name Work\n"
        + "create event Meeting1 from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "create event Meeting2 from 2025-11-16T14:00 to 2025-11-16T15:30\n"
        + "export cal test_export.csv\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar calendar = model.getCalendar("Work");
    Assert.assertEquals(2, calendar.getEventCount());

    File exportedFile = new File("test_export.csv");
    Assert.assertTrue(exportedFile.exists());
    Assert.assertTrue(exportedFile.length() > 0);

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Calendar exported successfully"));
    Assert.assertTrue(output.contains("File location"));
    Assert.assertTrue(output.contains("File size"));
    Assert.assertTrue(output.contains("bytes"));

    Assert.assertEquals(2, calendar.getEventCount());
  }

  @Test
  public void testExportIcalIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "export cal test_export.ical\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    File exportedFile = new File("test_export.ical");
    Assert.assertTrue(exportedFile.exists());
    Assert.assertTrue(exportedFile.length() > 0);

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Calendar exported successfully"));
  }

  @Test
  public void testExportIcsIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "export cal test_export.ics\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    File exportedFile = new File("test_export.ics");
    Assert.assertTrue(exportedFile.exists());

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Calendar exported successfully"));
  }

  @Test
  public void testExportEmptyCalendarIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "export cal empty.csv\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar calendar = model.getCalendar("Work");
    Assert.assertEquals(0, calendar.getEventCount());

    File exportedFile = new File("empty.csv");
    Assert.assertTrue(exportedFile.exists());

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Calendar exported successfully"));
  }

  @Test
  public void testExportRecurringEventsIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Standup from 2025-11-10T09:00 to 2025-11-10T09:30 "
        + "repeats MWF for 6 times\n"
        + "export cal recurring.csv\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar calendar = model.getCalendar("Work");
    Assert.assertEquals(6, calendar.getEventCount());

    File exportedFile = new File("recurring.csv");
    Assert.assertTrue(exportedFile.exists());

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Calendar exported successfully"));
  }

  @Test
  public void testExportWithAllPropertiesIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T15:00 to 2025-11-15T16:30\n"
        + "edit event description Meeting from 2025-11-15T15:00 with \"Q4 strategy\"\n"
        + "edit event location Meeting from 2025-11-15T15:00 with \"Room A\"\n"
        + "edit event status Meeting from 2025-11-15T15:00 with private\n"
        + "export cal full.csv\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    File exportedFile = new File("full.csv");
    Assert.assertTrue(exportedFile.exists());
  }

  @Test
  public void testExportNoCalendarIntegrated() {
    String commands = "export cal test_export.csv\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("ERROR"));
    Assert.assertTrue(output.contains("No calendar in use"));
  }

  @Test
  public void testExportInvalidFormatIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "export cal test_export.txt\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Invalid export request"));
    Assert.assertTrue(output.contains("Unsupported file format"));
  }

  @Test
  public void testGetFileNameIntegrated() {
    ExportCalendarCommand cmd = new ExportCalendarCommand("output.csv");
    Assert.assertEquals("output.csv", cmd.getFileName());
  }

  @Test
  public void testGetFormatIntegrated() {
    ExportCalendarCommand cmd1 = new ExportCalendarCommand("output.csv");
    Assert.assertEquals("CSV", cmd1.getFormat());

    ExportCalendarCommand cmd2 = new ExportCalendarCommand("output.ical");
    Assert.assertEquals("iCal", cmd2.getFormat());

    ExportCalendarCommand cmd3 = new ExportCalendarCommand("output.ics");
    Assert.assertEquals("iCal", cmd3.getFormat());

    ExportCalendarCommand cmd4 = new ExportCalendarCommand("output.txt");
    Assert.assertEquals("Unknown", cmd4.getFormat());
  }

  @Test
  public void testInterfaceMethodsIntegrated() {
    ExportCalendarCommand cmd = new ExportCalendarCommand("output.csv");
    Assert.assertNull(cmd.getNewCalendarContext());
    Assert.assertFalse(cmd.isExitCommand());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorNullFileName() {
    new ExportCalendarCommand(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorEmptyFileName() {
    new ExportCalendarCommand("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWhitespaceFileName() {
    new ExportCalendarCommand("   ");
  }
}