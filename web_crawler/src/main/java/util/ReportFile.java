package util;

import exceptions.ReportFileException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
/*
    Participants:
    - Philipp Arbeitstein [12205666]
    - Philipp Kaiser [12203588]
 */
public class ReportFile {

    public static void appendToReport(String path, String content) throws ReportFileException{
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path, true))) {
            writer.write(content);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
            throw new ReportFileException("Failed to append to report file: " + path, e);
        }
    }

    public static void clearFile(String path) throws ReportFileException {
        try (BufferedWriter ignored = new BufferedWriter(new FileWriter(path, false))) {
        } catch (IOException e) {
            e.printStackTrace();
            throw new ReportFileException("Failed to clear the report file: " + path, e);
        }
    }
}
