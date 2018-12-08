package readpreviews;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.SwingUtilities;

public class ReadPreviews
{
  public static String targetMonth = "";
  public static String currentPreviewsPageNumber = "";
  public static PreviewsCatalog catalog = new PreviewsCatalog();
  public static ArrayList<PreviewsEntry> entries = new ArrayList<PreviewsEntry>();
  public static GUI window;

  @SuppressWarnings("unchecked")
public static void main(String[] args)
  {
    File xmlFile = new File("ReadPreviews.xml");
    if (xmlFile.exists()) {
      entries = (ArrayList<PreviewsEntry>)Utilities.getObjectsFromXMLFile("ReadPreviews.xml");
    }
    Collections.sort(entries, new PreviewsEntry.AltEntryComparator());

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        ReadPreviews.window = new GUI();
        ReadPreviews.window.setLocationRelativeTo(null);
        ReadPreviews.window.setVisible(true);
      }
    });
  }
}