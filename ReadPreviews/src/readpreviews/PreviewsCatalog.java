package readpreviews;

import java.util.ArrayList;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("CATALOG")
public class PreviewsCatalog
{

	@XStreamAsAttribute
	String volume = "";

	@XStreamAsAttribute
	String month = "";

	@XStreamAsAttribute
	@XStreamAlias("ENTRIES")
	ArrayList<PreviewsEntry> entries = new ArrayList<PreviewsEntry>();

	PreviewsCatalog(String volume, String month)
	{
		this.volume = volume;
		this.month = month;
		entries = new ArrayList<PreviewsEntry>();
	}

	PreviewsCatalog() {
		this.volume = "";
		this.month = "";
		entries = new ArrayList<PreviewsEntry>();
	}

	public String toString() {
		String result =  this.volume + " " + this.month + ": " + Integer.toString(entries.size()) + " entries";
		return result;
	}

}