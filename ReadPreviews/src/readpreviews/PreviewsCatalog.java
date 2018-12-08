package readpreviews;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("CATALOG")
public class PreviewsCatalog
{

  @XStreamAsAttribute
  String volume = "";

  @XStreamAsAttribute
  String month = "";

  PreviewsCatalog(String volume, String month)
  {
    this.volume = volume;
    this.month = month;
  }

  PreviewsCatalog() {
    this.volume = "";
    this.month = "";
  }

  public String toString() {
    String result =  this.volume + " " + this.month;
    return result;
  }

}