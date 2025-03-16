import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class ReportWriter {
    private final String reportPath;

    public ReportWriter(String reportPath) {
        this.reportPath = reportPath;
        clearFile();
    }

    public void writeHeadingIntoReport(String headingText, int level, int currentDepth) {
        String markdownHeading = "#".repeat(level) + " \t" + "-".repeat(currentDepth) + "> \t" + headingText;
        appendToReport(markdownHeading);
    }

    private void clearFile() {
        try (BufferedWriter ignored = new BufferedWriter(new FileWriter(reportPath, false))) {
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("An error occurred while clearing the file.");
        }
    }

    public void appendToReport(String textToAppend) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportPath, true))) {
            writer.write(textToAppend);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("An error occurred while writing to the file.");
        }
    }

    public void printLinkAndDepthInformation(String link, int currentDepth, boolean isBrokenLink) {
        if (isBrokenLink) {
            appendToReport("<br>" + "-".repeat(currentDepth) + "> broken link <a>" + link + " </a>");
        } else {
            appendToReport("<br>" + "-".repeat(currentDepth) + "> link to <a>" + link + " </a>");
        }
        appendToReport("<br>depth:" + currentDepth);
    }
}
