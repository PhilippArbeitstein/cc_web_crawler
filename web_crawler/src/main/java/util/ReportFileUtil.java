package util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
/*
    Participants:
    - Philipp Arbeitstein [12205666]
    - Philipp Kaiser [12203588]
 */
public class ReportFileUtil {

    public static void appendToReport(String path, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path, true))) {
            writer.write(content);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void clearFile(String path) {
        try (BufferedWriter ignored = new BufferedWriter(new FileWriter(path, false))) {
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
