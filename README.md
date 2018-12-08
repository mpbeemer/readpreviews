# readpreviews
PREVIEWS (comics) monthly catalog mark-up application
## Background
PREVIEWS is a monthly catalog of comic shop items.  The vendor provides a hard-copy 
catalog, a text file listing all of the items and a PDF order form file.  An order to 
the retailer containing the item numbers and the page number on the order form assists 
them in placing their orders to the vendor.
## Application
readpreviews parses the monthly text file and presents a window with the data in table 
format, providing dynamic sort and search features.  On exit it writes an XML file to 
record the order status of all items and a text file containing marked items.
##### Pre-requisites
The catalog text file does not contain the order form page numbers, so the user must provide a 
support file listing the last item number appearing on each order form page. This can be a dummy 
value of '9999 99' to assign 'order form page 99' to all items and the user can add the actual 
page numbers manually once the order is compiled.
## Operation
* The application will read the contents of ReadPreviews.xml if it is present.  If not it will 
present a table with no contents.
* The File/Open menu selection will present file selection 
dialogs for the catalog text file and the order form page number file.  The data from the files 
will be parsed and merged and presented in table format:
  * Unlabeled column (order status: 1,2,3 - quantity, W - 'watch', S - 'save', L - 'look up', ? - 'undecided', 'o' (obsolete), 'x' - 'not selected')
  * Month (catalog month designation)
  * Item (item number)
  * Description
  * Ships (estimated shipping month and day)
  * Price (retail price: 'PI' - 'Please Inquire')
  * Page (catalog page number)
  * From (order form page number)
  * Prefixes (catalog file prefixes: 'FI' - 'Featured Item', 'OA' - 'Offered Again', etc.)
* Each of the columns may be clicked to sort the data on that field.  When returning to an 
unfinished order it is useful to sort on the order status column so unmarked items are presented 
first.
* Items can be marked by selecting them and pressing the corresponding key, or by selecting a value 
from the drop-down before the 'Mark Items' button and pressing that button.  The drop-down provides 
a blank option to un-mark items which is not otherwise available.
* The status bar at the bottom of the window shows the number of items currently selected and their 
total price.
* The search field in the center of the menu bar provides a dynamic search supporting 
regular expressions.	
* Order form pages for one or more items can be set by selecting the items, entering the page number into the unlabeled field before the 'Update Page' button and pressing that button.
* The 'Report' button generates the text file containing marked items and also presents the results 
in a text window.
* The 'Help/About' menu selection presents a dummy 'help' window.
* On exit by 'File/Exit' or closing the window, the application writes the ReadPreviews.xml file to 
preserve the order status of all items and writes ReadPreviews.txt to list all marked items if there 
are any.
## Dependancies
The application uses the xstream library to read and write XML files.