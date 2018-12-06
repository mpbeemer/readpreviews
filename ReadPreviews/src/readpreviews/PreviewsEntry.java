package readpreviews;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@XStreamAlias("ENTRY")
public class PreviewsEntry
  implements Comparable<PreviewsEntry>
{

  @XStreamAsAttribute
  String month = "";

  @XStreamAsAttribute
  @XStreamAlias("item")
  String itemNumber = "";

  @XStreamAsAttribute
  String status = "";

  @XStreamAsAttribute
  String prefixes = "";

  @XStreamAsAttribute
  @XStreamAlias("ships")
  String shipDate = "";

  @XStreamAsAttribute
  String price = "";

  @XStreamAsAttribute
  @XStreamAlias("page")
  String pageNumber = "";

  @XStreamAsAttribute
  @XStreamAlias("from")
  String orderFormPage = "";

  @XStreamAlias("description")
  String itemDescription = "";

  PreviewsEntry(String data) {
    Pattern patternWithDate = Pattern.compile("^(.*)\t" + 
      ReadPreviews.targetMonth + 
      " (\\d{4})\t(.*)\t(.*)\t.*: ([\\S]+)\t = \\$.*$");

    boolean found = false;
    this.month = ReadPreviews.targetMonth;
    this.pageNumber = ReadPreviews.currentPreviewsPageNumber;
    this.orderFormPage = "";
    Matcher matcher = patternWithDate.matcher(data);
    found = matcher.matches();
    if (found) {
      this.status = "";
      this.prefixes = matcher.group(1);
      this.itemNumber = matcher.group(2);
      this.itemDescription = matcher.group(3);
      this.shipDate = matcher.group(4);
      this.price = matcher.group(5);
    }
  }

  PreviewsEntry(String prefixes, String month, String itemNumber, String itemDescription, String shipDate, String price, String pageNumber, String orderFormPage)
  {
    this.status = "";
    this.prefixes = prefixes;
    this.month = month;
    this.itemNumber = itemNumber;
    this.itemDescription = itemDescription;
    this.shipDate = shipDate;
    this.price = price;
    this.pageNumber = pageNumber;
    this.orderFormPage = orderFormPage;
  }

  PreviewsEntry() {
    this.status = "";
    this.prefixes = "";
    this.month = "";
    this.itemNumber = "";
    this.itemDescription = "";
    this.shipDate = "";
    this.price = "";
    this.pageNumber = "";
    this.orderFormPage = "";
  }

  public String toString() {
    String result = this.status.isEmpty() ? " " : this.status;
    result = result + "    " + this.month + " " + this.itemNumber + "  ";
    if (this.itemDescription.length() > 55)
      result = result + this.itemDescription.substring(0, 56) + "... ";
    else {
      result = result + String.format("%-59s", new Object[] { this.itemDescription }) + " ";
    }
    if (this.price.contains("$"))
      result = result + String.format("%7s", new Object[] { "$" + 
        String.format("%5s", new Object[] { this.price.replace("$", "") }) });
    else if (this.price.equals("PI"))
      result = result + " PI    ";
    else {
      result = result + "       ";
    }
    result = result + 
      " - Page " + 
      String.format("%2s", new Object[] { this.orderFormPage }) + 
      "        " + 
      new StringBuilder(String.valueOf(this.shipDate)).append("     ").toString().substring(0, 5) + 
      "     00/00 ";
    
    if (this.pageNumber.substring(0,1).equals("M"))
    	result = result + this.pageNumber.substring(0,1) + this.pageNumber.substring(2,5);
    else
    	result = result + new StringBuilder("p" + String.format("%03d", new Object[] { Integer.valueOf(Integer.parseInt(this.pageNumber.trim())) }));

    return result;
  }

  public int compareTo(PreviewsEntry other)
  {
    if (this.status.equals(other.status)) {
      return this.itemNumber.compareTo(other.itemNumber);
    }
    if (((this.status.equals("1")) || (this.status.equals("2")) || (this.status.equals("3"))) && (
      (other.status.equals("1")) || (other.status.equals("2")) || (other.status.equals("3")))) {
      return this.itemNumber.compareTo(other.itemNumber);
    }

    return this.status.compareTo(other.status);
  }

  public static class AltEntryComparator
    implements Comparator<PreviewsEntry>
  {
    public int compare(PreviewsEntry oneEntry, PreviewsEntry anotherEntry)
    {
      return oneEntry.itemNumber.compareTo(anotherEntry.itemNumber);
    }
  }
}