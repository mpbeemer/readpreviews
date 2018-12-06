package readpreviews;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.regex.PatternSyntaxException;
import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

// import readpreviews.GUI.PreviewsTableModel;

public class GUI extends JFrame
  implements WindowListener
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
JMenuBar menuBar;
  JMenu fileMenu;
  JMenuItem openItem;
  JFileChooser openFileChooser;
  JMenuItem exitItem;
  JComboBox statusCombo;
  JButton markButton;
  JTextField filterText;
  JTextField orderPageText;
  JButton updatePageButton;
  JButton reportButton;
  JMenu helpMenu;
  JMenuItem aboutItem;
  JPanel tableArea;
  PreviewsTableModel tableModel;
  TableRowSorter<PreviewsTableModel> sorter;
  JTable tableContents;
  JScrollPane scrollPane;
  JComboBox statusComboBox;
  JPanel statusBar;
  JLabel statusBarLabel;
  KeyListener tableKeyListener = new KeyListener()
  {
    public void keyTyped(KeyEvent e) {
      char keyChar = e.getKeyChar();

      switch (keyChar) {
      case 'l':
      case 's':
      case 'w':
        keyChar = Character.toUpperCase(keyChar);
      }

      switch (keyChar) {
      case '1':
      case '2':
      case '3':
      case '?':
      case 'L':
      case 'S':
      case 'W':
      case 'o':
      case 'x':
        Utilities.markSelected(GUI.this.tableContents, Character.toString(keyChar));
      }
    }

    public void keyPressed(KeyEvent e)
    {
    }

    public void keyReleased(KeyEvent e)
    {
    }
  };

  ActionListener menuItemListener = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      Object source = e.getSource();

      if (source == GUI.this.openItem) {
        GUI.this.openFileChooser.setCurrentDirectory(new File("."));
        GUI.this.openFileChooser.setDialogTitle("Select PREVIEWS order form text file");
        GUI.this.openFileChooser.setFileSelectionMode(0);
        int returnVal = GUI.this.openFileChooser
          .showOpenDialog(ReadPreviews.window);

        if (returnVal == 0) {
          String orderFormTextFile = GUI.this.openFileChooser.getSelectedFile()
            .getAbsolutePath();
          GUI.this.openFileChooser.setDialogTitle("Select PREVIEWS order form page number file");
          returnVal = GUI.this.openFileChooser
            .showOpenDialog(ReadPreviews.window);
          if (returnVal == 0) {
            String orderFormPageNumberFile = GUI.this.openFileChooser
              .getSelectedFile().getAbsolutePath();
            Utilities.readPreviewsEntries(orderFormTextFile, orderFormPageNumberFile);
          }
        }
      } else if (source == GUI.this.exitItem) {
        GUI.this.writeOutputFiles();
        System.exit(0);
      } else if (source == GUI.this.markButton) {
        Utilities.markSelected(GUI.this.tableContents);
      } else if (source == GUI.this.updatePageButton) {
        Utilities.updateSelectedOrderPage(GUI.this.tableContents);
      } else if (source == GUI.this.reportButton) {
        String buffer = Utilities.writeOrderFile();
        JEditorPane editorPane = new JEditorPane();
        editorPane.setFont(new Font("Monospaced", Font.PLAIN, 12));
        editorPane.setText(buffer);
        editorPane.setEditable(false);
        JScrollPane editorScrollPane = new JScrollPane(editorPane);
        editorScrollPane
          .setVerticalScrollBarPolicy(22);
        editorScrollPane.setPreferredSize(new Dimension(900, 400));
        editorScrollPane.setMinimumSize(new Dimension(10, 10));
        JFrame frame = new JFrame("Order Report");
        frame.setDefaultCloseOperation(2);

        frame.add(editorScrollPane);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
      } else if (source == GUI.this.aboutItem) {
        JOptionPane.showMessageDialog(null, "Something goes here...", 
          "ReadPreviews help:", 3);
      }
    }
  };

  KeyListener filterTextKeyListener = new KeyListener() {
    public void keyTyped(KeyEvent e) {
      if (e.getKeyChar() == '\033')
        ((JTextField)e.getSource()).setText("");
    }

    public void keyPressed(KeyEvent e)
    {
    }

    public void keyReleased(KeyEvent e)
    {
    }
  };

  CaretListener filterTextListener = new CaretListener() {
    public void caretUpdate(CaretEvent e) {
      GUI.this.newFilter();
    }
  };

  ListSelectionListener updateStatus = new ListSelectionListener() {
    public void valueChanged(ListSelectionEvent e) {
      float total = 0.0F;
      int nFiles = 0;

      if (e.getValueIsAdjusting()) {
        return;
      }
      ListSelectionModel rowSM = (ListSelectionModel)e.getSource();

      for (int idx = 0; idx < ReadPreviews.entries.size(); idx++)
        if (rowSM.isSelectedIndex(idx)) {
          nFiles++;
          int modelRow = GUI.this.tableContents.convertRowIndexToModel(idx);
          String price = ((PreviewsEntry)ReadPreviews.entries.get(modelRow)).price.replace("$", "").trim();
          if (!price.equals("PI")) {
            if (((PreviewsEntry)ReadPreviews.entries.get(modelRow)).status.equals("2"))
              total += Float.parseFloat(price) * 2.0F;
            else if (((PreviewsEntry)ReadPreviews.entries.get(modelRow)).status.equals("3"))
              total += Float.parseFloat(price) * 3.0F;
            else {
              total += Float.parseFloat(price);
            }
          }
          GUI.this.setStatusText(nFiles + " items, $" + String.format("%,5.2f total.", new Object[] { Float.valueOf(total) }).trim());
        }
    }
  };

  public GUI()
  {
    initGUI();
  }

  private void initGUI()
  {
    try
    {
      setDefaultCloseOperation(2);

      configureLookAndFeel();

      this.menuBar = new JMenuBar();
      this.fileMenu = new JMenu("File");
      this.fileMenu.setMnemonic(70);
      this.openItem = new JMenuItem("Open", 79);
      this.openFileChooser = new JFileChooser();
      FileNameExtensionFilter filter = new FileNameExtensionFilter(
        "text files", new String[] { "txt" });
      this.openFileChooser.setFileFilter(filter);

      this.exitItem = new JMenuItem("Exit", 88);
      this.statusCombo = new JComboBox(Constants.statusStrings);
      this.markButton = new JButton("Mark items");
      this.filterText = new JTextField("");
      this.orderPageText = new JTextField(3);
      this.updatePageButton = new JButton("Update Page");
      this.reportButton = new JButton("Report");
      this.helpMenu = new JMenu("Help");
      this.aboutItem = new JMenuItem("About");
      this.tableArea = new JPanel(new GridLayout(1, 0));
      this.statusComboBox = new JComboBox(Constants.statusStrings);
      this.statusBar = new JPanel();
      this.statusBarLabel = new JLabel();

      setTitle("ReadPreviews");

      this.menuBar.add(this.fileMenu);
      this.fileMenu.add(this.openItem);
      this.openItem.addActionListener(this.menuItemListener);
      this.fileMenu.add(this.exitItem);
      this.exitItem.addActionListener(this.menuItemListener);
      this.menuBar.add(Box.createRigidArea(new Dimension(10, 0)));
      this.menuBar.add(this.statusCombo);
      this.statusCombo.setSelectedIndex(0);
      this.menuBar.add(Box.createRigidArea(new Dimension(10, 0)));
      this.menuBar.add(this.markButton);
      this.markButton.setMargin(new Insets(1, 3, 1, 3));
      this.markButton.addActionListener(this.menuItemListener);
      this.menuBar.add(Box.createRigidArea(new Dimension(10, 0)));
      this.menuBar.add(this.filterText);

      this.filterText.setDocument(new PlainDocument()
      {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public void insertString(int offset, String string, AttributeSet attributeSet) throws BadLocationException {
          super.insertString(offset, string.toUpperCase(), 
            attributeSet);
        }
      });
      this.filterText.addKeyListener(this.filterTextKeyListener);

      this.filterText.addCaretListener(this.filterTextListener);
      if ((ReadPreviews.entries == null) || 
        (ReadPreviews.entries.size() <= 0)) {
        this.filterText.setEnabled(false);
      }
      this.menuBar.add(Box.createRigidArea(new Dimension(10, 0)));
      this.orderPageText.setMaximumSize(new Dimension(1, 40));
      this.orderPageText.setMinimumSize(new Dimension(1, 40));
      this.menuBar.add(this.orderPageText);
      this.menuBar.add(Box.createRigidArea(new Dimension(10, 0)));
      this.menuBar.add(this.updatePageButton);
      this.updatePageButton.addActionListener(this.menuItemListener);
      this.menuBar.add(Box.createRigidArea(new Dimension(10, 0)));
      this.menuBar.add(this.reportButton);
      this.reportButton.addActionListener(this.menuItemListener);
      this.menuBar.add(Box.createRigidArea(new Dimension(150, 0)));
      this.menuBar.add(Box.createHorizontalGlue());
      this.menuBar.add(this.helpMenu);
      this.helpMenu.add(this.aboutItem);
      this.aboutItem.addActionListener(this.menuItemListener);

      this.tableModel = new PreviewsTableModel();
      this.sorter = new TableRowSorter<PreviewsTableModel>(this.tableModel);
      this.tableContents = new JTable(this.tableModel);
      this.tableContents.setRowSorter(this.sorter);
      TableColumn statusColumn = this.tableContents.getColumn("     ");
      statusColumn.setCellEditor(new DefaultCellEditor(this.statusComboBox));
      TableColumnModel columnModel = this.tableContents.getColumnModel();
      columnModel.getColumn(5).setCellRenderer(
        NumberRenderer.getCurrencyRenderer());
      ListSelectionModel rowSM = this.tableContents.getSelectionModel();
      rowSM.addListSelectionListener(this.updateStatus);
      this.tableContents.addKeyListener(this.tableKeyListener);
      this.scrollPane = new JScrollPane(this.tableContents);
      this.tableArea.add(this.scrollPane);

      this.statusBarLabel.setBorder(new EmptyBorder(0, 5, 0, 10));
      this.statusBarLabel.setText("0 items, $0.00 total.");
      this.statusBarLabel.setPreferredSize(new Dimension(800, 15));
      this.statusBar.setBorder(new EmptyBorder(3, 3, 1, 5));
      this.statusBar.setLayout(new GridLayout());
      this.statusBar.add(this.statusBarLabel);

      getContentPane().add(this.menuBar, "North");
      getContentPane().add(this.tableArea, "Center");
      getContentPane().add(this.statusBar, "South");

      addWindowListener(this);

      int tableWidth = autoFitTableColumns();
      int windowWidth = Math.max(tableWidth + 30, 850);

      pack();
      setSize(windowWidth, 600);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public int autoFitTableColumns() {
    int tableWidth = 0;

    this.tableContents.setAutoResizeMode(0);

    for (int i = 0; i < this.tableContents.getColumnCount(); i++) {
      DefaultTableColumnModel colModel = (DefaultTableColumnModel)this.tableContents
        .getColumnModel();
      TableColumn col = colModel.getColumn(i);
      int width = 0;

      TableCellRenderer renderer = col.getHeaderRenderer();
      if (renderer == null) {
        renderer = this.tableContents.getTableHeader().getDefaultRenderer();
      }

      Component comp = renderer.getTableCellRendererComponent(
        this.tableContents, col.getHeaderValue(), false, false, 0, 0);

      width = comp.getPreferredSize().width;
      for (int r = 0; r < this.tableContents.getRowCount(); r++) {
        renderer = this.tableContents.getCellRenderer(r, i);
        comp = renderer.getTableCellRendererComponent(this.tableContents, 
          this.tableContents.getValueAt(r, i), false, false, r, i);
        width = Math.max(width, comp.getPreferredSize().width);
      }
      if (i == 0) {
        col.setPreferredWidth(width + 20);
        tableWidth += width + 20;
      } else {
        col.setPreferredWidth(width + 2);
        tableWidth += width + 2;
      }
    }
    return tableWidth;
  }

  private void configureLookAndFeel()
  {
    try
    {
      try
      {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }
      catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
      catch (InstantiationException e) {
        e.printStackTrace();
      }
      catch (IllegalAccessException e) {
        e.printStackTrace();
      }

    }
    catch (UnsupportedLookAndFeelException e)
    {
      System.err.println("Unsupported Look and Feel Exception");
    }
  }

  @SuppressWarnings("unchecked")
private void newFilter()
  {
    @SuppressWarnings("rawtypes")
	RowFilter rf = null;
    try
    {
      rf = RowFilter.regexFilter("(?i)" + this.filterText.getText(), new int[0]);
    } catch (PatternSyntaxException e) {
      return;
    }
    this.sorter.setRowFilter(rf);
  }

  public void windowOpened(WindowEvent e)
  {
    this.filterText.requestFocusInWindow();
  }

  public void windowClosing(WindowEvent e)
  {
    writeOutputFiles();
  }

  public void writeOutputFiles() {
    if ((ReadPreviews.entries != null) && (ReadPreviews.entries.size() > 0)) {
      Utilities.saveObjectsToXMLFile("ReadPreviews.xml", 
        ReadPreviews.entries);
      Utilities.writeOrderFile();
    }
  }

  public void windowClosed(WindowEvent e)
  {
  }

  public void windowIconified(WindowEvent e)
  {
  }

  public void windowDeiconified(WindowEvent e)
  {
  }

  public void windowActivated(WindowEvent e)
  {
  }

  public void windowDeactivated(WindowEvent e)
  {
  }

  public void setStatusText(final String message)
  {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        GUI.this.statusBarLabel.setText(message);
      }
    });
  }

  class PreviewsTableModel extends AbstractTableModel
  {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	PreviewsTableModel()
    {
    }

    public int getColumnCount()
    {
      return Constants.columnNames.length;
    }

    public int getRowCount() {
      return ReadPreviews.entries.size();
    }

    public String getColumnName(int col) {
      return Constants.columnNames[col];
    }

    public Object getValueAt(int row, int col) {
      Object value = null;
      PreviewsEntry entry = (PreviewsEntry)ReadPreviews.entries.get(row);
      switch (col) {
      case 0:
        value = entry.status;
        break;
      case 1:
        value = entry.month;
        break;
      case 2:
        value = entry.itemNumber;
        break;
      case 3:
        value = entry.itemDescription;
        break;
      case 4:
        value = entry.shipDate;
        break;
      case 5:
        value = entry.price;
        break;
      case 6:
        value = entry.pageNumber;
        break;
      case 7:
        value = entry.orderFormPage;
        break;
      case 8:
        value = entry.prefixes;
      }

      return value;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public Class getColumnClass(int col)
    {
      return getValueAt(0, col).getClass();
    }

    public boolean isCellEditable(int row, int col) {
      switch (col) {
      case 0:
        return true;
      }
      return false;
    }

    public void setValueAt(Object value, int row, int col)
    {
      PreviewsEntry entry = (PreviewsEntry)ReadPreviews.entries.get(row);
      switch (col) {
      case 0:
        entry.status = ((String)value);
        break;
      case 1:
        entry.month = ((String)value);
        break;
      case 2:
        entry.itemNumber = ((String)value);
        break;
      case 3:
        entry.itemDescription = ((String)value);
        break;
      case 4:
        entry.shipDate = ((String)value);
        break;
      case 5:
        entry.price = ((String)value);
        break;
      case 6:
        entry.pageNumber = ((String)value);
        break;
      case 7:
        entry.orderFormPage = ((String)value);
        break;
      case 8:
        entry.prefixes = ((String)value);
      }

      fireTableCellUpdated(row, col);
    }
  }
}