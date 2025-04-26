import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/*
    Participants:
    - Philipp Arbeitstein [12205666]
    - Philipp Kaiser [12203588]
 */

public class ReportWriter {
    private final String reportPath;

    public ReportWriter(String reportPath) {
        this.reportPath = reportPath;
        clearFile();
    }

    public void writeHeadingIntoReport(String headingText, int headingLevel, int currentDepth) {
        String markdownHeading = "#".repeat(headingLevel) + " \t" + "-".repeat(currentDepth) + "> \t" + headingText;
        appendToReport(markdownHeading);
    }

    protected void clearFile() {
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
