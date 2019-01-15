package readpreviews;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.JTable;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class Utilities
{
	private static final String CRLF = "\r\n";

	public static String getStringFromFile(String fileName)
	{
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
		try
		{
			br = new BufferedReader(new FileReader(fileName));
			try
			{
				String s;
				while ((s = br.readLine()) != null)
				{
					sb.append(s);
					sb.append(CRLF);
				}
			} finally {
				br.close();
			}
		} catch (IOException ex) {
			System.err.println("Unable to read from file: " + fileName + ".");
			System.exit(0);
		}
		return sb.toString();
	}

	public static boolean saveStringToFile(String fileName, String saveString) {
		boolean saved = false;
		BufferedWriter bw = null;
		try
		{
			bw = new BufferedWriter(new FileWriter(fileName));
			try
			{
				bw.write(saveString);
				saved = true;
			} finally {
				bw.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return saved;
	}

	public static String convertCatalogToXML(Object catalogInfo) {
		String result = "";

		XStream xstream = new XStream(new DomDriver());
		xstream.processAnnotations(PreviewsCatalog.class);
		xstream.alias("PreviewsCatalog", List.class);
		result = xstream.toXML(catalogInfo);
		return result;
	}

	public static String postProcessXML(String xmlStream)
	{
		String result = xmlStream;

		Pattern pattern = Pattern.compile(".*<(.*)></\\1>.*");
		Matcher matcher = pattern.matcher(result);
		while (matcher.find()) {
			String target = "<" + matcher.group(1) + "></" + matcher.group(1) + ">";
			String replacement = "<" + matcher.group(1) + "/>";
			result = result.replace(target, replacement);
			matcher = pattern.matcher(result);
		}
		return "<?xml version=\"1.0\" encoding=\"US-ASCII\"?>\n" + result + 
				CRLF;
	}

	public static boolean saveCatalogToXMLFile(String fileName, Object objectArray)
	{
		return saveStringToFile(fileName, 
				postProcessXML(convertCatalogToXML(objectArray)));
	}

	public static Object convertCatalogFromXML(String XMLString) {
		XStream xstream = new XStream(new DomDriver());
		xstream.processAnnotations(PreviewsCatalog.class);
		xstream.alias("PreviewsCatalog", List.class);
		Object obj = xstream.fromXML(XMLString);
		return obj;
	}

	public static Object getCatalogFromXMLFile(String fileName) {
		return convertCatalogFromXML(getStringFromFile(fileName));
	}

	public static Object convertConfigurationFromXML(String XMLString) {
		Object object = null;
		XStream xstream = new XStream(new DomDriver());
		xstream.setMode(XStream.NO_REFERENCES);
		xstream.alias("configuration", Configuration.class);
		try {
			object = xstream.fromXML(XMLString);
		} catch (Exception e) {
			// pass null object back.
		}
		return object;
	}

	public static Object getConfigurationFromXMLFile(String fileName) {
		return convertConfigurationFromXML(getStringFromFile(fileName));
	}

	public static String convertConfigurationToXML(Object object) {
		XStream xstream = new XStream(new DomDriver());
		xstream.setMode(XStream.NO_REFERENCES);
		xstream.alias("configuration", Configuration.class);
		return xstream.toXML(object);
	}

	public static boolean saveConfigurationToXMLFile(String fileName,
			Object object) {
		return saveStringToFile(fileName,postProcessXML(convertConfigurationToXML(object)));
	}

	public static void readPreviewsEntries(String textFile, String partNumberFile) throws NumberFormatException
	{
		String result = getStringFromFile(textFile);
		String[] data = result.split(CRLF);

		result = getStringFromFile(partNumberFile);
		String[] pageData = result.split(CRLF);
		int entryIdx = 0;
		String lastItemSeen = pageData[entryIdx].substring(0, 4);
		String lastPageSeen = pageData[entryIdx].substring(5);

		ReadPreviews.catalog.entries = new ArrayList<PreviewsEntry>();

		Pattern pattern = Pattern.compile("^PREVIEWS ([A-Z]{3}).* V.*(\\d\\d) .*(\\d\\d)$");
		Matcher matcher = pattern.matcher(data[0]);
		boolean found = matcher.matches();
		if (found) {
			ReadPreviews.catalog.volume = matcher.group(2);
			ReadPreviews.catalog.month = matcher.group(3);
			int vol = Integer.parseInt(matcher.group(2)) - 10;
			ReadPreviews.targetMonth = matcher.group(1) + Integer.toString(vol);
		} else {
			pattern = Pattern.compile("^PREVIEWS ([A-Z]{3}).* V.*(\\d\\d) .*(\\d)$");
			matcher = pattern.matcher(data[0]);
			found = matcher.matches();
			if (found) {
				ReadPreviews.catalog.volume = matcher.group(2);
				ReadPreviews.catalog.month = matcher.group(3);
				int vol = Integer.parseInt(matcher.group(2)) - 10;
				ReadPreviews.targetMonth = matcher.group(1) + Integer.toString(vol);
			} else {
				JOptionPane.showMessageDialog(null, 
						"Unable to read current month from input file.", 
						"ReadPreviews halted", 0);
			}
		}

		for (String dataLine : data) {
			if (dataLine.length() > 5) {
				if (dataLine.substring(0, 5).equals("PAGE ")) {
					ReadPreviews.currentPreviewsPageNumber = dataLine
							.substring(5, dataLine.length());
				}
				if (dataLine.contains(ReadPreviews.targetMonth + " ")) {
					ReadPreviews.catalog.entries.add(new PreviewsEntry(dataLine));

					if (((PreviewsEntry)ReadPreviews.catalog.entries.get(ReadPreviews.catalog.entries.size() - 1)).itemNumber.compareTo(lastItemSeen) > 0) {
						entryIdx++;
						lastItemSeen = pageData[entryIdx].substring(0, 4);
						lastPageSeen = pageData[entryIdx].substring(5);
					}
					((PreviewsEntry)ReadPreviews.catalog.entries.get(ReadPreviews.catalog.entries.size() - 1)).orderFormPage = lastPageSeen;
				}

			}

		}

		Collections.sort(ReadPreviews.catalog.entries, 
				new PreviewsEntry.AltEntryComparator());
		int tableWidth = ReadPreviews.window.autoFitTableColumns();
		ReadPreviews.window.tableModel.fireTableDataChanged();
		ReadPreviews.window.filterText.setEnabled(true);
		ReadPreviews.window.setSize(tableWidth + 27, 600);
	}

	public static void markSelected(JTable table) {
		markSelected(table, ReadPreviews.window.statusCombo.getSelectedItem().toString());
	}

	public static void markSelected(JTable table, String mark)
	{
		int[] rowIndices = table.getSelectedRows();
		for (int idx : rowIndices) {
			int modelRow = table.convertRowIndexToModel(idx);
			PreviewsEntry entry = (PreviewsEntry)ReadPreviews.catalog.entries.get(modelRow);
			entry.status = mark;
		}
		ReadPreviews.window.tableModel.fireTableDataChanged();
	}

	public static void updateSelectedOrderPage(JTable table)
	{
		int[] rowIndices = table.getSelectedRows();
		for (int idx : rowIndices) {
			int modelRow = table.convertRowIndexToModel(idx);
			PreviewsEntry entry = (PreviewsEntry)ReadPreviews.catalog.entries.get(modelRow);
			entry.orderFormPage = ReadPreviews.window.orderPageText.getText();
		}
		ReadPreviews.window.tableModel.fireTableDataChanged();
	}

	public static String writeOrderFile() {
		String report = "";
		String currentStatus = "";
		double orderTotal = 0.0D;
		boolean totalReported = false;
		String paddedNumber = "";
		String header = "================================================================================" + CRLF + 
				"V" + ReadPreviews.catalog.volume + "#" + ReadPreviews.catalog.month + "  Previews Order Form" + CRLF + 
				CRLF + 
				ReadPreviews.configuration.customerName + CRLF + 
				ReadPreviews.configuration.customerStreet + CRLF + 
				ReadPreviews.configuration.customerCityStateZip + CRLF + 
				ReadPreviews.configuration.customerPhone + CRLF + 
				CRLF + 
				"QTY  TITLE                                                                    PRICE    ORDERED FROM   EXPECTED  RECD" + CRLF + 
				CRLF;

		report = header;

		Collections.sort(ReadPreviews.catalog.entries);
		for (PreviewsEntry entry : ReadPreviews.catalog.entries) {
			if ((!entry.status.isEmpty()) && (!entry.status.equals("x"))) {
				if (!currentStatus.equals(entry.status)) {
					if ((!currentStatus.isEmpty()) && 
							(!entry.status.matches("[123]"))) {
						if ((!totalReported) && (orderTotal > 0.0D)) {
							report = report + CRLF + 
									"================================================================================ Total: $";
							paddedNumber = String.format("%5.2f", new Object[] { Double.valueOf(orderTotal) }).trim();
							report = report + String.format("%6s", new Object[] { paddedNumber }) + CRLF;
							totalReported = true;
						}
						report = report + CRLF;
					}

					currentStatus = entry.status;
				}
				report = report + entry + CRLF;
				if (entry.status.matches("[123]")) {
					if (!entry.price.trim().equals("PI") ) {
						orderTotal += Integer.valueOf(entry.status).intValue() * Double.valueOf(entry.price.replace("$", "").trim()).doubleValue();
					}
				}
			}
		}
		if ((!totalReported) && (orderTotal > 0.0D)) {
			report = report + CRLF + 
					"================================================================================ Total: $";
			paddedNumber = String.format("%5.2f", new Object[] { Double.valueOf(orderTotal) }).trim();
			report = report + String.format("%6s", new Object[] { paddedNumber }) + CRLF;
			totalReported = true;
		}

		totalReported = false;
		report = report + CRLF + CRLF + 
				"--------------------------------------------------------------------------------------------------------------------" + CRLF + 
				CRLF + header;

		for (PreviewsEntry entry : ReadPreviews.catalog.entries) {
			if ((!entry.status.isEmpty()) && (!entry.status.equals("x"))) {
				if (!currentStatus.equals(entry.status)) {
					currentStatus = entry.status;
				}
				if (entry.status.matches("[123]")) {
					report = report + entry.toString().substring(0, 94) + CRLF;
				}
				if (entry.status.matches("[123]")) {
					if (!entry.price.trim().equals("PI") ) {
						orderTotal += Integer.valueOf(entry.status).intValue() * Double.valueOf(entry.price.replace("$", "").trim()).doubleValue();
					}
				}
			}
		}

		report = report + CRLF + 
				"================================================================================";

		if (!report.isEmpty()) {
			saveStringToFile("ReadPreviews.txt", report);
		}
		return report;
	}
}