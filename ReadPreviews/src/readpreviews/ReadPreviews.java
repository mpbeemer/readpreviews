package readpreviews;

import java.io.File;
import java.util.Collections;
import javax.swing.SwingUtilities;

public class ReadPreviews
{
  public static String			targetMonth					= "";
  public static String			currentPreviewsPageNumber	= "";
  public static Configuration	configuration				= new Configuration();
  public static PreviewsCatalog	catalog						= new PreviewsCatalog();
  public static GUI				window;

  public static void main(String[] args)
  {
	  File xmlFile = new File("ReadPreviewsConfiguration.xml");
	  if (xmlFile.exists()) {
		  configuration = (Configuration)Utilities.getConfigurationFromXMLFile("ReadPreviewsConfiguration.xml");
	  } else {
		  Utilities.saveConfigurationToXMLFile("ReadPreviewsConfiguration.xml", configuration);
	  }
    xmlFile = new File("ReadPreviews.xml");
    if (xmlFile.exists()) {
      catalog = (PreviewsCatalog)Utilities.getCatalogFromXMLFile("ReadPreviews.xml");
    }
    Collections.sort(catalog.entries, new PreviewsEntry.AltEntryComparator());

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        ReadPreviews.window = new GUI();
        ReadPreviews.window.setLocationRelativeTo(null);
        ReadPreviews.window.setVisible(true);
      }
    });
  }
}