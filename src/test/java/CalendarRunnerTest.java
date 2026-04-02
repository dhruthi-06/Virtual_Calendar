import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration tests for CalendarRunner.
 */
public class CalendarRunnerTest {

  private final ByteArrayOutputStream capturedOutput = new ByteArrayOutputStream();
  private final ByteArrayOutputStream capturedErrors = new ByteArrayOutputStream();
  private final PrintStream originalSystemOut = System.out;
  private final PrintStream originalSystemErr = System.err;
  private File testFile;

  /**
   * Sets up the test fixtures.
   */
  @Before
  public void setUp() {
    System.setOut(new PrintStream(capturedOutput));
    System.setErr(new PrintStream(capturedErrors));
  }

  /**
   * Tears down the test fixtures.
   */
  @After
  public void tearDown() {
    System.setOut(originalSystemOut);
    System.setErr(originalSystemErr);
    if (testFile != null && testFile.exists()) {
      testFile.delete();
    }
  }


  @Test
  public void testMissingMode() {
    CalendarRunner.main(new String[]{"somefile.txt"});
    String errorMessage = capturedErrors.toString();
    assertTrue(errorMessage.contains("--mode argument is required"));
    assertTrue(errorMessage.contains("Usage:"));
    assertTrue(errorMessage.contains("Modes:"));
  }

  @Test
  public void testInvalidMode() {
    CalendarRunner.main(new String[]{"--mode", "wrongmode"});
    String errorMessage = capturedErrors.toString();
    assertTrue(errorMessage.contains("mode must be either 'interactive' or 'headless'"));
    assertTrue(errorMessage.contains("Provided mode: wrongmode"));
    assertTrue(errorMessage.contains("Usage:"));
    assertTrue(errorMessage.contains("Examples:"));
  }

  @Test
  public void testHeadlessWithoutFile() {
    CalendarRunner.main(new String[]{"--mode", "headless"});
    String errorMessage = capturedErrors.toString();
    assertTrue(errorMessage.contains("headless mode requires a file path"));
    assertTrue(errorMessage.contains("Usage:"));
    assertTrue(errorMessage.contains("Note:"));
  }

  @Test
  public void testHeadlessWithNonExistentFile() {
    CalendarRunner.main(new String[]{"--mode", "headless", "nonexistent.txt"});
    String errorMessage = capturedErrors.toString();
    assertTrue(errorMessage.contains("Error reading file"));
    assertTrue(errorMessage.contains("File path: nonexistent.txt"));
  }

  @Test
  public void testHeadlessWithValidFile() throws Exception {
    testFile = File.createTempFile("test", ".txt");
    FileWriter writer = new FileWriter(testFile);
    writer.write("exit\n");
    writer.close();
    CalendarRunner.main(new String[]{"--mode", "headless", testFile.getAbsolutePath()});
    String errorMessage = capturedErrors.toString();
    assertFalse(errorMessage.contains("Error reading file"));
  }

  @Test
  public void testHeadlessExecutesCommands() throws Exception {
    testFile = File.createTempFile("test", ".txt");
    FileWriter writer = new FileWriter(testFile);
    writer.write("create calendar --name Test --timezone America/New_York\n");
    writer.write("use calendar --name Test\n");
    writer.write("create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n");
    writer.write("exit\n");
    writer.close();
    CalendarRunner.main(new String[]{"--mode", "headless", testFile.getAbsolutePath()});
    String programOutput = capturedOutput.toString();
    assertTrue(programOutput.contains("Calendar created"));
    assertTrue(programOutput.contains("Event created"));
  }

  @Test
  public void testModeCaseInsensitive() throws Exception {
    testFile = File.createTempFile("test", ".txt");
    FileWriter writer = new FileWriter(testFile);
    writer.write("exit\n");
    writer.close();
    CalendarRunner.main(new String[]{"--MODE", "HEADLESS", testFile.getAbsolutePath()});
    String errorMessage = capturedErrors.toString();
    assertFalse(errorMessage.contains("mode must be either"));
  }


  @Test
  public void testHeadlessWithMultipleCommands() throws Exception {
    testFile = File.createTempFile("test", ".txt");
    FileWriter writer = new FileWriter(testFile);
    writer.write("create calendar --name Cal1 --timezone America/New_York\n");
    writer.write("create calendar --name Cal2 --timezone America/Los_Angeles\n");
    writer.write("use calendar --name Cal1\n");
    writer.write("create event Event1 from 2025-11-15T10:00 to 2025-11-15T11:00\n");
    writer.write("print events on 2025-11-15\n");
    writer.write("exit\n");
    writer.close();
    CalendarRunner.main(new String[]{"--mode", "headless", testFile.getAbsolutePath()});
    String programOutput = capturedOutput.toString();
    assertTrue(programOutput.contains("Event1"));
  }

  @Test
  public void testHeadlessWithInvalidCommand() throws Exception {
    testFile = File.createTempFile("test", ".txt");
    FileWriter writer = new FileWriter(testFile);
    writer.write("invalid command here\n");
    writer.write("exit\n");
    writer.close();
    CalendarRunner.main(new String[]{"--mode", "headless", testFile.getAbsolutePath()});
    String programOutput = capturedOutput.toString();
    assertTrue(programOutput.contains("ERROR") || programOutput.contains("Unknown command"));
  }

  @Test
  public void testHeadlessFileWithoutExit() throws Exception {
    testFile = File.createTempFile("test", ".txt");
    FileWriter writer = new FileWriter(testFile);
    writer.write("create calendar --name Test --timezone America/New_York\n");
    writer.close();
    CalendarRunner.main(new String[]{"--mode", "headless", testFile.getAbsolutePath()});
    String programOutput = capturedOutput.toString();
    assertTrue(programOutput.contains("File ended without 'exit' command"));
  }

  @Test
  public void testUsageFormattingComplete() {
    CalendarRunner.main(new String[]{"--mode", "invalid"});
    String errorMessage = capturedErrors.toString();
    assertTrue(errorMessage.contains("\n"));
    assertTrue(errorMessage.contains("Examples:"));
    assertTrue(errorMessage.contains("Note:"));
    assertTrue(errorMessage.contains("  "));
  }


  @Test
  public void testRunInteractive() {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outputStream));

    ByteArrayInputStream inputStream = new ByteArrayInputStream("exit\n".getBytes());
    System.setIn(inputStream);

    try {
      CalendarRunner.main(new String[]{"--mode", "interactive"});

      String output = outputStream.toString();
      Assert.assertTrue(output.contains("Welcome"));
    } finally {
      System.setOut(originalOut);
      System.setIn(System.in);
    }
  }

  @Test
  public void testRunHeadless() {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outputStream));

    try {
      File tempFile = File.createTempFile("test_commands", ".txt");
      tempFile.deleteOnExit();

      try (PrintWriter writer = new PrintWriter(tempFile)) {
        writer.println("create calendar --name Work --timezone UTC");
        writer.println("exit");
      }

      CalendarRunner.main(new String[]{"--mode", "headless", tempFile.getAbsolutePath()});

      String output = outputStream.toString();
      Assert.assertTrue(output.length() > 0);
    } catch (Exception e) {
      Assert.fail("Exception occurred: " + e.getMessage());
    } finally {
      System.setOut(originalOut);
    }
  }

  @Test
  public void testHeadlessIoError() {
    ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
    PrintStream originalErr = System.err;
    System.setErr(new PrintStream(errorStream));

    try {
      CalendarRunner.main(new String[]{"--mode", "headless", "/invalid/path/file.txt"});

      String error = errorStream.toString();
      Assert.assertTrue(error.contains("Error reading file"));
    } finally {
      System.setErr(originalErr);
    }
  }
}