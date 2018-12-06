package org.isf.lab.gui;

/*------------------------------------------
 * LabBrowser - list all exams
 * -----------------------------------------
 * modification history
 * 02/03/2006 - theo, Davide - first beta version
 * 08/11/2006 - ross - changed button Show into Results
 *                     fixed the exam deletion
 * 					   version is now 1.0 
 * 04/01/2009 - ross - do not use roll, use add(week,-1)!
 *                     roll does not change the year! 
 *------------------------------------------*/

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;

import java.util.Date;
import java.util.Locale;
import java.time.LocalDate;
import java.time.ZoneId;

import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import de.steinwedel.messagebox.MessageBox;

import org.isf.utils.jobjects.ModalWindow;
import org.isf.utils.Logging;
import org.isf.exa.manager.ExamBrowsingManager;
import org.isf.exa.model.Exam;
import org.isf.exatype.model.ExamType;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.lab.gui.LabEdit.LabEditListener;
import org.isf.lab.gui.LabEditExtended.LabEditExtendedListener;
import org.isf.lab.gui.LabNew.LabListener;
import org.isf.lab.manager.LabManager;
import org.isf.lab.manager.LabRowManager;
import org.isf.lab.model.Laboratory;
import org.isf.lab.model.LaboratoryForPrint;
import org.isf.lab.model.LaboratoryRow;
import org.isf.menu.gui.MainMenu;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.jobjects.VoDateTextField;


public class LabBrowser extends ModalWindow implements LabListener, LabEditListener, LabEditExtendedListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void labInserted() {
		LabManager manager = new LabManager();
		pLabs = manager.getLaboratory();
		examGrid.setItems(pLabs);
	}
	
	public void labUpdated() {
		filterButton.click();
	}
	
	private static final String VERSION=MessageBundle.getMessage("angal.versione");
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	
	private VerticalLayout windowContent = null;
	private HorizontalLayout buttonLayout = null;
	private Button buttonEdit = null;
	private Button newButton = null;
	private Button deleteButton = null;
	private Button closeButton = null;
	private JButton printTableButton = null;
	private Button filterButton = null;
	private VerticalLayout selectionLayout = null;
	private Grid<Laboratory> examGrid = null;
	private ComboBox examsCombo = null;
	private int pfrmHeight;
	private ArrayList<Laboratory> pLabs;
	private String[] pColums = { MessageBundle.getMessage("angal.common.datem"), MessageBundle.getMessage("angal.lab.patient"), MessageBundle.getMessage("angal.lab.examm"), MessageBundle.getMessage("angal.lab.resultm") };
	private boolean[] columnsResizable = {false, true, true, false};
	private int[] pColumwidth = { 100, 200, 200, 200 };
	private int[] maxWidth = {150, 200, 200, 200};
	private boolean[] columnsVisible = { true, GeneralData.LABEXTENDED, true, true};
	private LabManager manager;
	private LabBrowsingModel model;
	private Laboratory laboratory;
	private int selectedrow;
	private String typeSelected = null;
	private DateField dateFrom = null;
	private DateField dateTo = null;
	private final JFrame myFrame;

	private Logging logger = new Logging();

	/**
	 * This is the default constructor
	 */
	public LabBrowser() {
		myFrame = new JFrame();//dlt ths
		manager = new LabManager();
		initialize();
		// setResizable(false);
		// setVisible(true);
	}

	/**
	 * This method initializes this Frame, sets the correct Dimensions
	 * 
	 * @return void
	 */
	private void initialize() {
		UI.getCurrent().addWindow(this);
		// Toolkit kit = Toolkit.getDefaultToolkit();
		// Dimension screensize = kit.getScreenSize();
		// final int pfrmBase = 20;
		// final int pfrmWidth = 14;
		// final int pfrmHeight = 12;
		// this.setBounds((screensize.width - screensize.width * pfrmWidth
		// 		/ pfrmBase) / 2, (screensize.height - screensize.height
		// 		* pfrmHeight / pfrmBase) / 2, screensize.width * pfrmWidth
		// 		/ pfrmBase, screensize.height * pfrmHeight / pfrmBase);
		setContent(getWindowContent());
		setCaption(MessageBundle.getMessage("angal.lab.laboratorybrowsing")+" ("+VERSION+")");
	}

	/**
	 * This method initializes windowContent, adds the main parts of the frame
	 * 
	 * @return windowContentl (JPanel)
	 */
	private VerticalLayout getWindowContent() {
		if (windowContent == null) {
			windowContent = new VerticalLayout();
			VerticalLayout topLayout = new VerticalLayout();
			windowContent.addComponent(topLayout);
			topLayout.addComponent(getSelectionLayout());
			topLayout.addComponent(getGrid());
			windowContent.addComponent(getButtonLayout());
			// validate();
		}
		return windowContent;
	}

	/**
	 * This method initializes buttonLayout, that contains the buttons of the
	 * frame (on the bottom)
	 * 
	 * @return buttonLayout (JPanel)
	 */
	private HorizontalLayout getButtonLayout() {
		if (buttonLayout == null) {
			buttonLayout = new HorizontalLayout();
			if (MainMenu.checkUserGrants("btnlaboratorynew")) buttonLayout.addComponent(getNewButton());
			if (MainMenu.checkUserGrants("btnlaboratoryedit")) buttonLayout.addComponent(getButtonEdit());
			if (MainMenu.checkUserGrants("btnlaboratorydel")) buttonLayout.addComponent(getdeleteButton());
			buttonLayout.addComponent(getCloseButton());
			// buttonLayout.addComponent((getPrintTableButton()));
		}
		return buttonLayout;
	}

	private JButton getPrintTableButton() {
		if (printTableButton == null) {
			printTableButton = new JButton(MessageBundle.getMessage("angal.lab.printtable"));
			printTableButton.setMnemonic(KeyEvent.VK_P);
			printTableButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent arg0) {
					LabRowManager rowManager = new LabRowManager();
					ArrayList<LaboratoryRow> rows = null;
					typeSelected = ((Exam) examsCombo.getSelectedItem().get())
							.toString();
					if (typeSelected.equalsIgnoreCase(MessageBundle.getMessage("angal.lab.all")))
						typeSelected = null;
					ArrayList<LaboratoryForPrint> labs = manager.getLaboratoryForPrint(typeSelected, GregorianCalendar.from(dateFrom.getValue().atStartOfDay(ZoneId.systemDefault())), GregorianCalendar.from(dateTo.getValue().atStartOfDay(ZoneId.systemDefault())));
					for (int i = 0; i < labs.size(); i++) {
						if (labs.get(i).getResult().equalsIgnoreCase(
								MessageBundle.getMessage("angal.lab.multipleresults"))) {
							rows = rowManager.getLabRow(labs.get(i).getCode());
							
							if (rows == null || rows.size() == 0) {
								labs.get(i).setResult(MessageBundle.getMessage("angal.lab.allnegative"));
							} else {
								labs.get(i).setResult(MessageBundle.getMessage("angal.lab.positive")+" : "+rows.get(0).getDescription());
								for (int j=1;j<rows.size();j++) {
									labs.get(i).setResult(
											labs.get(i).getResult() + ","
													+ rows.get(j).getDescription());
								}
							}
						}
					}
					if (!labs.isEmpty()) new LabPrintFrame(myFrame, labs);
				}

			});
		}
		return printTableButton;
	}

	private Button getButtonEdit() {
		if (buttonEdit == null) {
			buttonEdit = new Button(MessageBundle.getMessage("angal.common.edit"));
			buttonEdit.setClickShortcut(KeyEvent.VK_E);
			buttonEdit.addClickListener(e->{
				if(examGrid.getSelectedItems().isEmpty()){
					MessageBox.create().withCaption(MessageBundle.getMessage("angal.hospital"))
					.withMessage(MessageBundle.getMessage("angal.common.pleaseselectarow")).open();
					return;
				} 
				laboratory = (Laboratory)examGrid.getSelectedItems().toArray()[0];
				if (GeneralData.LABEXTENDED) {
					LabEditExtended editrecord = new LabEditExtended(laboratory, false);
					editrecord.addLabEditExtendedListener(this);
					editrecord.setVisible(true);
				} else {
					LabEdit editrecord = new LabEdit(myFrame, laboratory, false);
					editrecord.addLabEditListener(LabBrowser.this);
					editrecord.setVisible(true);
				}
			});
		}
		return buttonEdit;
	}

	/**
	 * This method initializes newButton, that loads LabEdit Mask
	 * 
	 * @return newButton (JButton)
	 */
	private Button getNewButton() {
		if (newButton == null) {
			newButton = new Button(MessageBundle.getMessage("angal.common.new"));
			newButton.setClickShortcut(KeyEvent.VK_N);
			newButton.addClickListener(e->{
				laboratory = new Laboratory(0, new Exam("", "",
						new ExamType("", ""), 0, "", 0),
						new GregorianCalendar(), "P", 0, "", 0, "");
				if (GeneralData.LABEXTENDED) {
					if (GeneralData.LABMULTIPLEINSERT) {
						LabNew editrecord = new LabNew();
						editrecord.addLabListener(this);
						// editrecord.setVisible(true);
					} else {
						LabEditExtended editrecord = new LabEditExtended(laboratory, true);
						editrecord.addLabEditExtendedListener(LabBrowser.this);
						editrecord.setVisible(true);
					}
				} else {
					LabEdit editrecord = new LabEdit(myFrame, laboratory, true);
					editrecord.addLabEditListener(this);
					// editrecord.setVisible(true);
				}
			});
		}
		return newButton;
	}

	/**
	 * This method initializes deleteButton, that delets the selected records
	 * 
	 * @return deleteButton (JButton)
	 */
	private Button getdeleteButton() {
		if (deleteButton == null) {
			deleteButton = new Button(MessageBundle.getMessage("angal.common.delete"));
			deleteButton.setClickShortcut(KeyEvent.VK_D);
			deleteButton.addClickListener(event->{
				if (examGrid.getSelectedItems().isEmpty()) {
					MessageBox.create().withCaption(MessageBundle.getMessage("angal.hospital"))
					.withMessage(MessageBundle.getMessage("angal.common.pleaseselectarow")).open();
					return;
				} else {
					Laboratory lab = (Laboratory) examGrid.getSelectedItems().toArray()[0];
					MessageBox.createQuestion().withCaption(MessageBundle.getMessage("angal.hospital"))
					.withMessage(MessageBundle.getMessage("angal.lab.deletefollowinglabexam")+"; " +
							"\n"+ MessageBundle.getMessage("angal.lab.registationdate")+"=" + getConvertedString(lab.getDate()) + 
							"\n "+ MessageBundle.getMessage("angal.lab.examdate")+"=" + getConvertedString(lab.getExamDate()) + 
							"\n "+ MessageBundle.getMessage("angal.lab.exam")+"=" + lab.getExam() + 
							"\n "+ MessageBundle.getMessage("angal.lab.patient")+" =" + lab.getPatName() + 
							"\n "+ MessageBundle.getMessage("angal.lab.result")+" =" + lab.getResult() + 
							"\n ?")
					.withYesButton(()->{
						if (manager.deleteLaboratory(lab)) {
							pLabs.remove((Laboratory) examGrid.getSelectedItems().toArray()[0]);
							model.fireTableDataChanged();
							examGrid.setItems(pLabs);
						}
					}).withNoButton().open();
				}
			});
		}
		return deleteButton;
	}

	/**
	 * This method initializes closeButton, that disposes the entire Frame
	 * 
	 * @return closeButton (JButton)
	 */
	private Button getCloseButton() {
		if (closeButton == null) {
			closeButton = new Button(MessageBundle.getMessage("angal.common.close"));
			closeButton.setClickShortcut(KeyEvent.VK_C);
			closeButton.addClickListener(e->{
				close();
			});
		}
		return closeButton;
	}

	/**
	 * This method initializes selectionLayout, that contains the filter objects
	 * 
	 * @return selectionLayout (JPanel)
	 */
	private VerticalLayout getSelectionLayout() {
		if (selectionLayout == null) {
			selectionLayout = new VerticalLayout();
			// selectionLayout.setPreferredSize(new Dimension(200, pfrmHeight));
			selectionLayout.addComponent(new Label(MessageBundle.getMessage("angal.lab.selectanexam")));
			selectionLayout.addComponent(getexamsCombo());
			selectionLayout.addComponent(new Label(MessageBundle.getMessage("angal.common.datem") +":"+ MessageBundle.getMessage("angal.lab.from")));
			selectionLayout.addComponent(getDateFrom());
			selectionLayout.addComponent(new Label(MessageBundle.getMessage("angal.common.datem") +":"+MessageBundle.getMessage("angal.lab.to") +"     "));
			selectionLayout.addComponent(getDateTo());
			selectionLayout.addComponent(getFilterButton());
		}
		return selectionLayout;
	}

	/**
	 * This method initializes examGrid, that contains the information about the
	 * Laboratory Tests
	 * 
	 * @return examGrid (Grid)
	 */
	private Grid getGrid() {
		if (examGrid == null) {
			examGrid = new Grid();
			// model = new LabBrowsingModel();
			LabManager manager = new LabManager();
			pLabs = manager.getLaboratory();
			examGrid.setItems(pLabs);
			examGrid.addColumn(Laboratory::getDateFormat).setCaption("Date");
			examGrid.addColumn(Laboratory::getPatName).setCaption("Patient");
			examGrid.addColumn(Laboratory::getExam).setCaption("Exam");
			examGrid.addColumn(Laboratory::getResult).setCaption("Result");
			// int columnLengh = pColumwidth.length;
			// if (!GeneralData.LABEXTENDED) {
			// 	columnLengh--;
			// }
			// for (int i=0;i<columnLengh; i++){
			// 	examGrid.getColumnModel().getColumn(i).setMinWidth(pColumwidth[i]);
			// 	if (!columnsResizable[i]) examGrid.getColumnModel().getColumn(i).setMaxWidth(maxWidth[i]);
			// }
		}
		return examGrid;
	}
	class LabBrowsingModel extends DefaultTableModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private LabManager manager = new LabManager();

		public LabBrowsingModel(String exam, GregorianCalendar dateFrom, GregorianCalendar dateTo) {
			pLabs = manager.getLaboratory(exam, dateFrom, dateTo);
		}

		public LabBrowsingModel() {
			LabManager manager = new LabManager();
			pLabs = manager.getLaboratory();
		}

		public int getRowCount() {
			if (pLabs == null)
				return 0;
			return pLabs.size();
		}

		public String getColumnName(int c) {
			return pColums[getNumber(c)];
		}

		public int getColumnCount() {
			int c = 0;
			for (int i = 0; i < columnsVisible.length; i++) {
				if (columnsVisible[i]) {
					c++;
				}
			}
			return c;
		}

		/** 
	     * This method converts a column number in the table
	     * to the right number of the datas.
	     */
	    protected int getNumber(int col) {
	    	// right number to return
	        int n = col;    
	        int i = 0;
	        do {
	            if (!columnsVisible[i]) {
	            	n++;
	            }
	            i++;
	        } while (i < n);
	        // If we are on an invisible column, 
	        // we have to go one step further
	        while (!columnsVisible[n]) {
	        	n++;
	        }
	        return n;
	    }
	    
		/**
		 * Note: We must get the objects in a reversed way because of the query
		 * 
		 * @see org.isf.lab.service.IoOperations
		 */
		public Object getValueAt(int r, int c) {
			if (c == -1) {
				return pLabs.get(pLabs.size() - r - 1);
			} else if (getNumber(c) == 0) {
				return //getConvertedString(pLabs.get(pLabs.size() - r - 1).getDate());
					   dateFormat.format(pLabs.get(pLabs.size() - r - 1).getExamDate().getTime());
			} else if (getNumber(c) == 1) {
				return pLabs.get(pLabs.size() - r - 1).getPatName(); //Alex: added
			} else if (getNumber(c) == 2) {
				return pLabs.get(pLabs.size() - r - 1).getExam();
			} else if (getNumber(c) == 3) {
				return pLabs.get(pLabs.size() - r - 1).getResult();
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			// return super.isCellEditable(arg0, arg1);
			return false;
		}
	}

	/**
	 * This method initializes examsCombo, that allows to choose which Exam the
	 * user want to display on the Table
	 * 
	 * @return examsCombo (JComboBox)
	 */
	private ComboBox getexamsCombo() {
		ExamBrowsingManager managerExams = new ExamBrowsingManager();
		if (examsCombo == null) {
			examsCombo = new ComboBox();
			// examsCombo.setPreferredSize(new Dimension(200, 30));
			ArrayList<Exam> type = managerExams.getExams();
			Exam allExam = new Exam("", MessageBundle.getMessage("angal.lab.all"), new ExamType("", ""), 0, "",0);
			type.add(0,allExam);
			examsCombo.setEmptySelectionAllowed(false);
			examsCombo.setItems(type);
			examsCombo.addValueChangeListener(e->{
				typeSelected = ((Exam) examsCombo.getSelectedItem().get()).toString();
				if (typeSelected.equalsIgnoreCase(MessageBundle.getMessage("angal.lab.all")))
					typeSelected = null;
			});
			examsCombo.setValue(allExam);
		}
		return examsCombo;
	}

	/**
	 * This method initializes dateFrom, which is the Panel that contains the
	 * date (From) input for the filtering
	 * 
	 * @return dateFrom (JPanel)
	 */
	private DateField getDateFrom() {
		if (dateFrom == null) {
			dateFrom = new DateField("",LocalDate.now().minusDays(7));
			dateFrom.setDateFormat("dd/MM/yyyy");
		}
		return dateFrom;
	}

	/**
	 * This method initializes dateTo, which is the Panel that contains the date
	 * (To) input for the filtering
	 * 
	 * @return dateTo (JPanel)
	 */
	private DateField getDateTo() {
		if (dateTo == null) {
			dateTo = new DateField("",LocalDate.now());
			dateTo.setDateFormat("dd/MM/yyyy");
		}
		return dateTo;
	}

	/**
	 * This method initializes filterButton, which is the button that perform
	 * the filtering and calls the methods to refresh the Table
	 * 
	 * @return filterButton (JButton)
	 */
	private Button getFilterButton(){
		if (filterButton == null) {
			filterButton = new Button(MessageBundle.getMessage("angal.lab.search"));
			filterButton.setClickShortcut(KeyEvent.VK_S);
			filterButton.addClickListener(e->{
				typeSelected = ((Exam) examsCombo.getSelectedItem().get()).toString();
				if (typeSelected.equalsIgnoreCase(MessageBundle.getMessage("angal.lab.all")))
					typeSelected = null;
				model = new LabBrowsingModel(typeSelected, GregorianCalendar.from(dateFrom.getValue().atStartOfDay(ZoneId.systemDefault())), GregorianCalendar.from(dateTo.getValue().atStartOfDay(ZoneId.systemDefault())));
				// model.fireTableDataChanged();
				examGrid.setItems(pLabs);
			});
		}
		return filterButton;
	}

	/**
	 * This class defines the model for the Table
	 * 
	 * @author theo
	 * 
	 */
	

	/**
	 * This method updates the Table because a laboratory test has been updated
	 * Sets the focus on the same record as before
	 * 
	 */
	/*public void laboratoryUpdated() {
		pLabs.set(pLabs.size() - selectedrow - 1, laboratory);
		((LabBrowsingModel) examGrid.getModel()).fireTableDataChanged();
		examGrid.updateUI();
		if ((examGrid.getRowCount() > 0) && selectedrow > -1)
			examGrid.setRowSelectionInterval(selectedrow, selectedrow);
	}*/

	/**
	 * This method updates the Table because a laboratory test has been inserted
	 * Sets the focus on the first record
	 * 
	 */
	/*public void laboratoryInserted() {
		pLabs.add(pLabs.size(), laboratory);
		((LabBrowsingModel) examGrid.getModel()).fireTableDataChanged();
		if (examGrid.getRowCount() > 0)
			examGrid.setRowSelectionInterval(0, 0);
	}
*/
	/**
	 * This method is needed to display the date in a more understandable format
	 * 
	 * @param time
	 * @return String
	 */
	private String getConvertedString(GregorianCalendar time) {
		String string = "";
		if (time!=null) {
			string=String
					.valueOf(time.get(GregorianCalendar.DAY_OF_MONTH));
			string += "/" + String.valueOf(time.get(GregorianCalendar.MONTH) + 1);
			string += "/" + String.valueOf(time.get(GregorianCalendar.YEAR));
			string += "  "
					+ String.valueOf(time.get(GregorianCalendar.HOUR_OF_DAY));
			string += ":" + String.valueOf(time.get(GregorianCalendar.MINUTE));
			string += ":" + String.valueOf(time.get(GregorianCalendar.SECOND));
		}
		return string;
	}
}
