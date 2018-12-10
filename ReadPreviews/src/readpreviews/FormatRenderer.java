package readpreviews;

import java.text.DateFormat;
import java.text.Format;
import javax.swing.table.DefaultTableCellRenderer;

public class FormatRenderer extends DefaultTableCellRenderer
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Format formatter;

	public FormatRenderer(Format formatter)
	{
		this.formatter = formatter;
	}

	public void setValue(Object value)
	{
		try
		{
			if (value != null)
				value = this.formatter.format(value);
		}
		catch (IllegalArgumentException localIllegalArgumentException) {
		}
		super.setValue(value);
	}

	public static FormatRenderer getDateTimeRenderer()
	{
		return new FormatRenderer(DateFormat.getDateTimeInstance());
	}

	public static FormatRenderer getTimeRenderer()
	{
		return new FormatRenderer(DateFormat.getTimeInstance());
	}
}