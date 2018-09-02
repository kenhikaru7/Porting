package org.isf.opd.gui;

/*------------------------------------------
 * OpdBrowser - list all OPD. let the user select an opd to edit or delete
 * -----------------------------------------
 * modification history
 * 11/12/2005 - Vero, Rick  - first beta version 
 * 07/11/2006 - ross - renamed from Surgery 
 *                   - changed confirm delete message
 * 			         - version is now 1.0 
 *    12/2007 - isf bari - multilanguage version
 * 			         - version is now 1.2 
 * 21/06/2008 - ross - fixed getFilterButton method, need compare to translated string "female" to get correct filter
 *                   - displayed visitdate in the grid instead of opdDate (=system date)
 *                   - fixed "todate" bug (in case of 31/12: 31/12/2008 became 1/1/2008)
 * 			         - version is now 1.2.1 
 * 09/01/2009 - fabrizio - Column full name appears only in OPD extended. Better formatting of OPD date.
 *                         Age column justified to the right. Cosmetic changed to code style.
 * 13/02/2009 - alex - fixed variable visibility in filtering mechanism
 *------------------------------------------*/


import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextField;
import com.vaadin.ui.RadioButtonGroup;
import com.vaadin.ui.Grid;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.shared.ui.window.WindowMode;

import de.steinwedel.messagebox.MessageBox;

import org.isf.disease.manager.DiseaseBrowserManager;
import org.isf.disease.model.Disease;
import org.isf.distype.manager.DiseaseTypeBrowserManager;
import org.isf.distype.model.DiseaseType;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.gui.MainMenu;
import org.isf.opd.manager.OpdBrowserManager;
import org.isf.opd.model.Opd;
import org.isf.patient.model.Patient;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.Logging;

public class OpdBrowser extends Window implements OpdEditExtended.OpdListener {

	private static final long serialVersionUID = 2372745781159245861L;

	private static final String VERSION="1.2.1"; 
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");

	private Logging logger;
	private HorizontalLayout jButtonLayout = null;
	private VerticalLayout jContainLayout = null;
//	private int pfrmWidth;
	private int pfrmHeight;
	private Button newButton = null;
	private Button editButton = null;
	private Button closeButton = null;
	private Button deleteButton = null;
	private VerticalLayout jSelectionLayout = null;
	private Label label = null;
	private HorizontalLayout dateFromLayout = null;
	private HorizontalLayout dateToLayout = null;
	private TextField dayFrom = null;
	private TextField monthFrom = null;
	private TextField yearFrom = null;
	private TextField dayTo = null;
	private TextField monthTo = null;
	private TextField yearTo = null;
	private Panel jSelectionDiseasePanel = null;  //  @jve:decl-index=0:visual-constraint="232,358"
	private Label label2 = null;
	private Label label3 = null;
	private HorizontalLayout jAgeFromLayout = null;
	private Label label4 = null;
	private TextField jAgeFromTextField = null;
	private HorizontalLayout jAgeToLayout = null;
	private Label label5 = null;
	private TextField jAgeToTextField = null;
	private Panel jAgePanel = null;
	private ComboBox jDiseaseTypeBox;
	private ComboBox jDiseaseBox;
	private Panel sexPanel=null;
	private Panel newPatientPanel=null;
	private RadioButtonGroup sexGroup=null;
	private RadioButtonGroup groupNewPatient=null;
	private Integer ageTo = 0;
	private Integer ageFrom = 0;
	private DiseaseType allType= new DiseaseType(MessageBundle.getMessage("angal.opd.alltype"),MessageBundle.getMessage("angal.opd.alltype"));
	//private String[] pColums = { MessageBundle.getMessage("angal.common.datem"), MessageBundle.getMessage("angal.opd.fullname"), MessageBundle.getMessage("angal.opd.sexm"), MessageBundle.getMessage("angal.opd.agem"),MessageBundle.getMessage("angal.opd.diseasem"),MessageBundle.getMessage("angal.opd.diseasetypem"),MessageBundle.getMessage("angal.opd.patientstatus")};
	//MODIFIED : alex
	private String[] pColums = { MessageBundle.getMessage("angal.common.datem"), MessageBundle.getMessage("angal.opd.patientid"), MessageBundle.getMessage("angal.opd.fullname"), MessageBundle.getMessage("angal.opd.sexm"), MessageBundle.getMessage("angal.opd.agem"),MessageBundle.getMessage("angal.opd.diseasem"),MessageBundle.getMessage("angal.opd.diseasetypem"),MessageBundle.getMessage("angal.opd.patientstatus")};
	private ArrayList<Opd> opdList;
	private Grid<Opd> grid;
	private OpdBrowsingModel model;
	private int[] pColumwidth = { 70, 70, 150, 30, 30, 195, 195, 50 };
	private boolean[] columnResizable = { false, false, true, false, false, true, true, false };
	private boolean[] columnsVisible = { true, GeneralData.OPDEXTENDED, GeneralData.OPDEXTENDED, true, true, true, true, true };
	private int[] columnsAlignment = { SwingConstants.LEFT, SwingConstants.CENTER, SwingConstants.LEFT, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.LEFT };
	private boolean[] columnsBold = { false, true, false, false, false, false, false, false };
	private int selectedrow;
	private OpdBrowserManager manager = new OpdBrowserManager();
	private Button filterButton = null;
	private String rowCounterText = MessageBundle.getMessage("angal.opd.count") + ": ";
	private Label rowCounter = null;
	private JRadioButton radioNew;
	private JRadioButton radioRea;
	private JRadioButton radioAll;
	private JRadioButton radiom;
	private JRadioButton radiof;
	private JRadioButton radioa;

	public Grid getGrid(){
		if(grid == null){
			model = new OpdBrowsingModel();
			grid = new Grid();
			grid.setItems(opdList);
			grid.addColumn(Opd::getSVisitDate).setCaption("DATE");
			grid.addColumn(Opd::getpatientCode).setCaption("ID");
			grid.addColumn(Opd::getFullName).setCaption("FULL NAME");
			grid.addColumn(Opd::getSex).setCaption("SEX");
			grid.addColumn(Opd::getAge).setCaption("AGE");
			grid.addColumn(Opd::getDiseaseDesc).setCaption("DISEASE");
			grid.addColumn(Opd::getDiseaseTypeDesc).setCaption("DISEASE TYPE");
			grid.addColumn(Opd::getNewPatient).setCaption("STATUS");
			// grid.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			// TableColumnModel columnModel = grid.getColumnModel();
			// DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
			// cellRenderer.setHorizontalAlignment(Label.RIGHT);
			// for(int i = 0; i < pColums.length; i++){
			// 	columnModel.getColumn(i).setMinWidth(pColumwidth[i]);
			// 	columnModel.getColumn(i).setCellRenderer(new AlignmentCellRenderer());
			// 	if(!columnResizable[i])
			// 		columnModel.getColumn(i).setMaxWidth(pColumwidth[i]);
			// 	if(!columnsVisible[i]){
			// 		columnModel.getColumn(i).setMaxWidth(0);
			// 		columnModel.getColumn(i).setMinWidth(0);
			// 		columnModel.getColumn(i).setPreferredWidth(0);
			// 	}
			// }
		}
		return grid;
	}
	
	// class AlignmentCellRenderer extends DefaultTableCellRenderer {  
		
	// 	/**
	// 	 * 
	// 	 */
	// 	private static final long serialVersionUID = 1L;

	// 	@Override
	// 	public Component getTableCellRendererComponent(Grid table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
			
	// 		Component cell=super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
	// 		setHorizontalAlignment(columnsAlignment[column]);
	// 		if(columnsBold[column])
	// 			cell.setFont(new Font(null, Font.BOLD, 12));
	// 		return cell;
	// 	}
	// }
	
	/**
	 * This method initializes 
	 * 
	 */
	public OpdBrowser(){
		// myFrame=this;
		initialize();
	}
	
	public OpdBrowser(Patient patient){
		super();
		// myFrame=this;
		initialize();
        setVisible(true);
        //if(bOpenEdit)
        Opd newOpd = new Opd(0,' ',-1,"0",0);
        OpdEditExtended editrecord = new OpdEditExtended(newOpd, patient, true);
        editrecord.addOpdListener(OpdBrowser.this);
	}
	
	
	/**
	 * This method initializes jButtonLayout
	 * 
	 * @return javax.swing.Panel
	 */
	private HorizontalLayout getjButtonLayout(){
		if(jButtonLayout == null){
			jButtonLayout = new HorizontalLayout();
			if(MainMenu.checkUserGrants("btnopdnew")) jButtonLayout.addComponent(getNewButton());
			if(MainMenu.checkUserGrants("btnopdedit")) jButtonLayout.addComponent(getEditButton());
			if(MainMenu.checkUserGrants("btnopddel")) jButtonLayout.addComponent(getdeleteButton());
			jButtonLayout.addComponent(getcloseButton());
		}
		return jButtonLayout;
	}
	
	
	
	
	/**
	 * This method initializes this
	 * 
	 */
	private void initialize(){
		logger = new Logging();
		UI.getCurrent().addWindow(this);
		this.setModal(true);
		this.setWindowMode(WindowMode.MAXIMIZED);
		this.setCaption(MessageBundle.getMessage("angal.opd.opdoutpatientdepartment")+"("+VERSION+")");
		this.setContent(getjContainLayout());
		rowCounter.setCaption(rowCounterText + opdList.size());
	}
	
	/**
	 * This method initializes containPanel	
	 * 	
	 * @return javax.swing.Panel	
	 */
	private VerticalLayout getjContainLayout(){
		if(jContainLayout == null){
			jContainLayout = new VerticalLayout();
			HorizontalLayout top = new HorizontalLayout();
			top.addComponent(getJSelectionLayout());
			top.addComponent(getGrid());
			jContainLayout.addComponent(top);
			jContainLayout.addComponent(getjButtonLayout());
		}
		return jContainLayout;
	}
	
	/**
	 * This method initializes newButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private Button getNewButton(){
		if(newButton == null){
			newButton = new Button();
			newButton.setCaption(MessageBundle.getMessage("angal.common.new"));
			newButton.setClickShortcut(KeyEvent.VK_N);
			newButton.addClickListener(e -> {
				Opd newOpd = new Opd(0,' ',-1,"0",0);
				if(GeneralData.OPDEXTENDED){
					OpdEditExtended newrecord = new OpdEditExtended(newOpd, true);
					newrecord.addOpdListener(this);
					// newrecord.setVisible(true);
				} else {
					// OpdEdit newrecord = new OpdEdit(newOpd, true);
					// newrecord.addOpdListener(OpdBrowser.this);
					// newrecord.setVisible(true);
				}
			});
		}
		return newButton;
	}
	
	public void NewOpd(){
		newButton.click();
	}
	
	/**
	 * This method initializes editButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private Button getEditButton(){
		if(editButton == null){
			editButton = new Button();
			editButton.setCaption(MessageBundle.getMessage("angal.common.edit"));
			editButton.setClickShortcut(KeyEvent.VK_E);
			editButton.addClickListener(e->{
				if(grid.getSelectedItems().isEmpty()){
					MessageBox.createInfo().withCaption(MessageBundle.getMessage("angal.hospital")).withMessage(MessageBundle.getMessage("angal.common.pleaseselectarow"))
					.withOkButton().open();
					return;
				} else {
					Opd opd = (Opd) grid.getSelectedItems().toArray()[0];
					if(GeneralData.OPDEXTENDED){
						OpdEditExtended editrecord = new OpdEditExtended(opd, false);
						editrecord.addOpdListener(this);
						// editrecord.setVisible(true);
					} else {
						// OpdEdit editrecord = new OpdEdit(opd, false);
						// editrecord.addOpdListener(this);
						// editrecord.setVisible(true);
					}
				}
			});
		}
		return editButton;
	}
	
	/**
	 * This method initializes closeButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private Button getcloseButton(){
		if(closeButton == null){
			closeButton = new Button(MessageBundle.getMessage("angal.common.close"));
            closeButton.setClickShortcut(KeyEvent.VK_C);
			closeButton.addActionListener(e->{
				this.close();
			});
		}
		return closeButton;
	}
	
	/**
	 * This method initializes deleteButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private Button getdeleteButton(){
		if(deleteButton == null){
			deleteButton = new Button(MessageBundle.getMessage("angal.common.delete"));
			deleteButton.setClickShortcut(KeyEvent.VK_D);
			deleteButton.addClickListener(e->{
				if(grid.getSelectedItems().isEmpty()){
					MessageBox.createInfo().withCaption(MessageBundle.getMessage("angal.hospital")).withMessage(MessageBundle.getMessage("angal.common.pleaseselectarow"))
					.withOkButton().open();
					return;
				} else {
					Opd opd = (Opd) grid.getSelectedItems().toArray()[0];
					String dt="[not specified]";
					try {
						final DateFormat currentDateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.ITALIAN);
						dt = currentDateFormat.format(opd.getVisitDate().getTime());
					}
					catch (Exception ex){
					}
	
					MessageBox.createQuestion().withCaption(MessageBundle.getMessage("angal.hospital"))
					.withMessage(MessageBundle.getMessage("angal.opd.deletefollowingopd") +
							"\n"+MessageBundle.getMessage("angal.opd.registrationdate")+"="+dateFormat.format(opd.getDate()) + 
							"\n"+MessageBundle.getMessage("angal.opd.disease")+"= "+ ((opd.getDiseaseDesc()==null)? "["+MessageBundle.getMessage("angal.opd.notspecified")+"]": opd.getDiseaseDesc()) + 
							"\n"+MessageBundle.getMessage("angal.opd.age")+"="+ opd.getAge()+", "+"Sex="+" " +opd.getSex()+
							"\n"+MessageBundle.getMessage("angal.opd.visitdate")+"=" + dt +
							"\n ?")
					.withYesButton(()-> {
						if(manager.deleteOpd(opd)){
							for(int i = 0; i < opdList.size(); i++){
								if (opdList.get(i).getCode()==opd.getCode()){
									opdList.remove(i);
									break;
								}
							}
							grid.setItems(opdList);
						}
					})
					.withNoButton(()-> {
					}).open();
				}
			});
		}
		return deleteButton;
	}
	
	/**
	 * This method initializes jSelectionLayout	
	 * 	
	 * @return javax.swing.Panel	
	 */
	private VerticalLayout getJSelectionLayout(){
		if(jSelectionLayout == null){
			label3 = new Label();
			label3.setValue(MessageBundle.getMessage("angal.opd.selectsex"));
			label = new Label();
			label.setValue(MessageBundle.getMessage("angal.opd.selectadisease"));
			jSelectionLayout = new VerticalLayout();
			jSelectionLayout.addComponent(label);
			jSelectionLayout.addComponent(getDiseaseTypeBox());
			jSelectionLayout.addComponent(getDiseaseBox());
			jSelectionLayout.addComponent(getDateFromLayout());
			jSelectionLayout.addComponent(getDateToLayout());
			jSelectionLayout.addComponent(getJAgeFromLayout());
			jSelectionLayout.addComponent(getJAgeToLayout());
			jSelectionLayout.addComponent(label3);
			jSelectionLayout.addComponent(getSexButtonGroup());
			label4 = new Label();
			label4.setValue(MessageBundle.getMessage("angal.opd.patient"));
			jSelectionLayout.addComponent(label4);
			jSelectionLayout.addComponent(getNewPatientRadioButton());			
			jSelectionLayout.addComponent(getFilterButton());
			jSelectionLayout.addComponent(getRowCounter());
		}
		return jSelectionLayout;
	}
	
	
	private Label getRowCounter(){
		if(rowCounter == null){
			rowCounter = new Label();
			// rowCounter.setAlignmentX(Box.CENTER_ALIGNMENT);
		}
		return rowCounter;
	}

	private HorizontalLayout getDateFromLayout(){
		if(dateFromLayout == null){
			dateFromLayout = new HorizontalLayout();
			dateFromLayout.addComponent(new Label(MessageBundle.getMessage("angal.opd.datefrom")));
			dayFrom = new TextField();
			dayFrom.setMaxLength(2);
			dayFrom.addBlurListener(e -> {
				if(dayFrom.getValue().length() != 0){
					if(dayFrom.getValue().length() == 1){
						String typed = dayFrom.getValue();
						dayFrom.setValue("0" + typed);
					}
					if(!isValidDay(dayFrom.getValue()))
						dayFrom.setValue("1");
				}
			});
			monthFrom = new TextField();
			monthFrom.setMaxLength(2);
			monthFrom.addBlurListener(e -> {
				if(monthFrom.getValue().length() != 0){
					if(monthFrom.getValue().length() == 1){
						String typed = monthFrom.getValue();
						monthFrom.setValue("0" + typed);
					}
					if(!isValidMonth(monthFrom.getValue()))
						monthFrom.setValue("1");
				}
			});
			yearFrom = new TextField();
			monthFrom.setMaxLength(4);
			yearFrom.addFocusListener(e -> {
				if(yearFrom.getValue().length() == 4){
					if(!isValidYear(yearFrom.getValue()))
						yearFrom.setValue("2006");
				} else
					yearFrom.setValue("2006");
			});
			dateFromLayout.addComponent(dayFrom);
			dateFromLayout.addComponent(monthFrom);
			dateFromLayout.addComponent(yearFrom);
			GregorianCalendar now = new GregorianCalendar();
			if(!GeneralData.ENHANCEDSEARCH) now.add(GregorianCalendar.WEEK_OF_YEAR, -1);
			dayFrom.setValue(String.valueOf(now
					.get(GregorianCalendar.DAY_OF_MONTH)));
			monthFrom.setValue(String
					.valueOf(now.get(GregorianCalendar.MONTH) + 1));
			yearFrom.setValue(String.valueOf(now.get(GregorianCalendar.YEAR)));
		}
		return dateFromLayout;
	}
	
	public class DocumentoLimitato extends DefaultStyledDocument {
		
		private static final long serialVersionUID = -5098766139884585921L;
		
		private final int NUMERO_MASSIMO_CARATTERI;
		
		public DocumentoLimitato(int numeroMassimoCaratteri){
			NUMERO_MASSIMO_CARATTERI = numeroMassimoCaratteri;
		}
		
		public void insertString(int off, String text, AttributeSet att)
		throws BadLocationException {
			int numeroCaratteriNelDocumento = getLength();
			int lunghezzaNuovoTesto = text.length();
			if(numeroCaratteriNelDocumento + lunghezzaNuovoTesto > NUMERO_MASSIMO_CARATTERI){
				int numeroCaratteriInseribili = NUMERO_MASSIMO_CARATTERI
				- numeroCaratteriNelDocumento;
				if(numeroCaratteriInseribili > 0){
					String parteNuovoTesto = text.substring(0,
							numeroCaratteriInseribili);
					super.insertString(off, parteNuovoTesto, att);
				}
			} else {
				super.insertString(off, text, att);
			}
		}
	}
	
	
	private HorizontalLayout getDateToLayout(){
		if(dateToLayout == null){
			dateToLayout = new HorizontalLayout();
			dateToLayout.addComponent(new Label(MessageBundle.getMessage("angal.opd.dateto")));
			dayTo = new TextField();
			dayTo.setMaxLength(2);
			dayTo.addBlurListener(e -> {
				if(dayTo.getValue().length() != 0){
					if(dayTo.getValue().length() == 1){
						String typed = dayTo.getValue();
						dayTo.setValue("0" + typed);
					}
					if(!isValidDay(dayTo.getValue()))
						dayTo.setValue("1");
				}
			});
			monthTo = new TextField();
			monthTo.setMaxLength(2);
			monthTo.addBlurListener(e -> {
				if(monthTo.getValue().length() != 0){
					if(monthTo.getValue().length() == 1){
						String typed = monthTo.getValue();
						monthTo.setValue("0" + typed);
					}
					if(!isValidMonth(monthTo.getValue()))
						monthTo.setValue("1");
				}
			});
			yearTo = new TextField();
			yearTo.setMaxLength(4);
			yearTo.addBlurListener(e -> {
				if(yearTo.getValue().length() == 4){
					if(!isValidYear(yearTo.getValue()))
						yearTo.setValue("2006");
				} else
					yearTo.setValue("2006");
			});
			dateToLayout.addComponent(dayTo);
			dateToLayout.addComponent(monthTo);
			dateToLayout.addComponent(yearTo);
			GregorianCalendar now = new GregorianCalendar();
			dayTo.setValue(String.valueOf(now
					.get(GregorianCalendar.DAY_OF_MONTH)));
			monthTo.setValue(String
					.valueOf(now.get(GregorianCalendar.MONTH) + 1));
			yearTo.setValue(String.valueOf(now.get(GregorianCalendar.YEAR)));
			
		}
		return dateToLayout;
	}
	/**
	 * 
	 * @param day 
	 * 48 == '0'
	 * 57 == '9'
	 * @return
	 */
	private boolean isValidDay(String day){		
		byte[] typed = day.getBytes();
		if(typed[0] < 48 || typed[0] > 57 || typed[1] < 48 || typed[1] > 57){
			return false;
		}
		int num = Integer.valueOf(day);
		if(num < 1 || num > 31)
			return false;
		return true;
	}
	
	private boolean isValidMonth(String month){
		byte[] typed = month.getBytes();
		if(typed[0] < 48 || typed[0] > 57 || typed[1] < 48 || typed[1] > 57){
			return false;
		}
		int num = Integer.valueOf(month);
		if(num < 1 || num > 12)
			return false;
		return true;
	}
	
	private boolean isValidYear(String year){
		byte[] typed = year.getBytes();
		if(typed[0] < 48 || typed[0] > 57 || typed[1] < 48 || typed[1] > 57
				|| typed[2] < 48 || typed[2] > 57 || typed[3] < 48
				|| typed[3] > 57){
			return false;
		}
		return true;
	}
	
	private GregorianCalendar getDateFrom(){
		return new GregorianCalendar(Integer.valueOf(yearFrom.getValue()),
									 Integer.valueOf(monthFrom.getValue()) - 1, 
									 Integer.valueOf(dayFrom.getValue()));
	}
	
	private GregorianCalendar getDateTo(){
		return new GregorianCalendar(Integer.valueOf(yearTo.getValue()), 
									 Integer.valueOf(monthTo.getValue()) - 1, 
									 Integer.valueOf(dayTo.getValue()));
	}

	
	
	
	/**
	 * This method initializes jComboBox	
	 * 	
	 * @return javax.swing.ComboBox	
	 */
	public ComboBox getDiseaseTypeBox(){
		if(jDiseaseTypeBox == null){
			jDiseaseTypeBox = new ComboBox();
			
			DiseaseTypeBrowserManager manager = new DiseaseTypeBrowserManager();
			ArrayList<DiseaseType> types = new ArrayList<DiseaseType>();
			types.add(allType);
			types.addAll(manager.getDiseaseType());
			jDiseaseTypeBox.setItems(types);
			jDiseaseTypeBox.setValue(allType);
			jDiseaseTypeBox.setEmptySelectionAllowed(false);
			jDiseaseTypeBox.addValueChangeListener(e-> {
				jDiseaseBox.setItems();
				getDiseaseBox();
			});					
		}
		
		return jDiseaseTypeBox;
	}
	
	/**
	 * This method initializes jComboBox1	
	 * 	
	 * @return javax.swing.ComboBox	
	 */
	public ComboBox getDiseaseBox(){
		if(jDiseaseBox == null){
			jDiseaseBox = new ComboBox();
			jDiseaseBox.setEmptySelectionAllowed(false);
		};
		DiseaseBrowserManager manager = new DiseaseBrowserManager();
		ArrayList<Disease> diseases = new ArrayList<Disease>();
		Disease allDisease = new Disease(MessageBundle.getMessage("angal.opd.alldisease"), MessageBundle.getMessage("angal.opd.alldisease"), allType, 0);
		diseases.add(allDisease);
		if(((DiseaseType)jDiseaseTypeBox.getSelectedItem().get()).getDescription().equals(MessageBundle.getMessage("angal.opd.alltype"))){
			diseases.addAll(manager.getDiseaseOpd());
		}else{
			diseases.addAll(manager.getDiseaseOpd(((DiseaseType)jDiseaseTypeBox.getSelectedItem().get()).getCode()));
		};
		jDiseaseBox.setItems(diseases);
		jDiseaseBox.setValue(allDisease);
		return jDiseaseBox;
	}
	
	/**
	 * This method initializes sexPanel	
	 * 	
	 * @return javax.swing.Panel	
	 */
	public RadioButtonGroup getSexButtonGroup(){
			sexGroup=new RadioButtonGroup();
			sexGroup.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
			sexGroup.setItems(MessageBundle.getMessage("angal.opd.male"),MessageBundle.getMessage("angal.opd.female"),MessageBundle.getMessage("angal.opd.all"));
			sexGroup.setValue(MessageBundle.getMessage("angal.opd.all"));
			return sexGroup;
	}
	
	public RadioButtonGroup getNewPatientRadioButton(){
			groupNewPatient=new RadioButtonGroup();
			groupNewPatient.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
			groupNewPatient.setItems(MessageBundle.getMessage("angal.common.new"),MessageBundle.getMessage("angal.opd.reattendance"),MessageBundle.getMessage("angal.opd.all"));
			groupNewPatient.setValue(MessageBundle.getMessage("angal.opd.all"));
			return groupNewPatient;
	}
	
	/**
	 * This method initializes jAgePanel	
	 * 	
	 * @return javax.swing.Panel	
	 */
	private HorizontalLayout getJAgeFromLayout(){
		if(jAgeFromLayout == null){
			label4 = new Label();
			label4.setValue(MessageBundle.getMessage("angal.opd.agefrom"));
			jAgeFromLayout = new HorizontalLayout();
			jAgeFromLayout.addComponent(label4);
			jAgeFromLayout.addComponent(getJAgeFromTextField());
		}
		return jAgeFromLayout;
	}
	
	/**
	 * This method initializes jAgeFromTextField	
	 * 	
	 * @return javax.swing.TextField	
	 */
	private TextField getJAgeFromTextField(){
		if(jAgeFromTextField == null){
			jAgeFromTextField = new TextField();
			jAgeFromTextField.setMaxLength(3);
			jAgeFromTextField.setValue("0");
			ageFrom=0;
			jAgeFromTextField.addBlurListener(e -> {
				try {
					ageFrom = Integer.parseInt(jAgeFromTextField.getValue());
					if((ageFrom<0)||(ageFrom>200)){
						jAgeFromTextField.setValue("");
						MessageBox.createInfo().withCaption("Message").withMessage(MessageBundle.getMessage("angal.opd.insertvalidage"))
						.withOkButton().open();
					}
				} catch (NumberFormatException ex){
					jAgeFromTextField.setValue("0");
				}
			});
		}
		return jAgeFromTextField;
	}
	
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.Panel	
	 */
	private HorizontalLayout getJAgeToLayout(){
		if(jAgeToLayout == null){
			label5 = new Label();
			label5.setValue(MessageBundle.getMessage("angal.opd.ageto"));
			jAgeToLayout = new HorizontalLayout();
			jAgeToLayout.addComponent(label5);
			jAgeToLayout.addComponent(getJAgeToTextField());
		}
		return jAgeToLayout;
	}
	
	/**
	 * This method initializes TextField	
	 * 	
	 * @return javax.swing.TextField	
	 */
	private TextField getJAgeToTextField(){
		if(jAgeToTextField == null){
			jAgeToTextField = new TextField();
			jAgeToTextField.setMaxLength(3);
			jAgeToTextField.setValue("0");
			ageTo=0;
			jAgeToTextField.addBlurListener(e -> {
				try {				
					ageTo = Integer.parseInt(jAgeToTextField.getValue());
					if((ageTo<0)||(ageTo>200)){
						jAgeToTextField.setValue("");
						MessageBox.createInfo().withCaption("Message").withMessage(MessageBundle.getMessage("angal.opd.insertvalidage"))
						.withOkButton().open();
					}
					
				} catch (NumberFormatException ex){
					jAgeToTextField.setValue("0");
				}
			});
		}
		return jAgeToTextField;
	}

	/**
	 * This method initializes jAgePanel	
	 * 	
	 * @return javax.swing.Panel	
	 */
	private Panel getJAgePanel(){
		if(jAgePanel == null){
			jAgePanel = new Panel();
			// jAgePanel.add(getJAgeFromLayout(), null);
			// jAgePanel.add(getJAgeToLayout(), null);
		}
		return jAgePanel;
	}
	
	class OpdBrowsingModel {
		
		private static final long serialVersionUID = -9129145534999353730L;
		
		public OpdBrowsingModel(String diseaseTypeCode,String diseaseCode, GregorianCalendar dateFrom,GregorianCalendar dateTo,int ageFrom, int ageTo,char sex,String newPatient){
			opdList = manager.getOpd(diseaseTypeCode,diseaseCode,dateFrom,dateTo,ageFrom,ageTo,sex,newPatient);
		}
		
		public OpdBrowsingModel(){
			opdList = manager.getOpd(!GeneralData.ENHANCEDSEARCH);
		}
		
		public int getRowCount(){
			if(opdList == null)
				return 0;
			return opdList.size();
		}
		
		public String getColumnName(int c){
			return pColums[c];
		}
		
		public int getColumnCount(){
			int c = 0;
			for(int i = 0; i < columnsVisible.length; i++){
				if(columnsVisible[i]){
					c++;
				}
			}
			return c;
		}
		
		public Object getValueAt(int r, int c){
			Opd opd = opdList.get(opdList.size() - r - 1);
			if(c == -1){
				return opd;
			} else if(c == 0){
				String sVisitDate;
				if(opd.getVisitDate() == null){
					sVisitDate = "";
				} else {
					sVisitDate = dateFormat.format(opd.getVisitDate().getTime());
				}
				return sVisitDate;
			} else if(c == 1){
				return opd.getpatientCode(); //MODIFIED: alex
			} else if(c == 2){
				return opd.getFullName(); //MODIFIED: alex
			} else if(c == 3){
				return opd.getSex();
			} else if(c == 4){
				return opd.getAge();
			} else if(c == 5){
				return opd.getDiseaseDesc();
			} else if(c == 6){
				return opd.getDiseaseTypeDesc();
			} else if(c == 7){
				String patientStatus;
				if(opd.getNewPatient().equals("N")){
					patientStatus = MessageBundle.getMessage("angal.common.new");
				} else {
					patientStatus = MessageBundle.getMessage("angal.opd.reattendance");
				}
				return patientStatus;
			}
			
			return null;
		}//"DATE", "PROG YEAR", "SEX", "AGE","DISEASE","DISEASE TYPE"};
		
		
		// @Override
		// public boolean isCellEditable(int arg0, int arg1){
		// 	// return super.isCellEditable(arg0, arg1);
		// 	return false;
		// }
		
		/** 
	     * This method converts a column number in the table
	     * to the right number of the datas.
	     */
	    protected int getNumber(int col){
	    	// right number to return
	        int n = col;    
	        int i = 0;
	        do {
	            if(!columnsVisible[i]){
	            	n++;
	            }
	            i++;
	        } while (i < n);
	        // ifwe are on an invisible column, 
	        // we have to go one step further
	        while (!columnsVisible[n]){
	        	n++;
	        }
	        return n;
	    }
	}
	
	public void opdInserted(Opd opd){
		opdList.add(opd);
		grid.setItems(opdList);
		if(opdList.size() > 0)
			grid.select(opd);
		rowCounter.setCaption(rowCounterText + opdList.size());
	}
	
	public void opdUpdated(Opd opd){
		for(int i = 0; i < opdList.size(); i++){
			if (opdList.get(i).getCode()==opd.getCode()){
				// opdList.remove(i);
				opdList.set(i, opd);
				break;
			}
		}
		grid.setItems(opdList);
		grid.select(opd);
		rowCounter.setCaption(rowCounterText + opdList.size());
	}
	
	
	private Button getFilterButton(){
		if(filterButton == null){
			filterButton = new Button();
			filterButton.setCaption(MessageBundle.getMessage("angal.opd.search"));
            filterButton.setClickShortcut(KeyEvent.VK_S);
			filterButton.addClickListener(e -> {
					String disease=((Disease)jDiseaseBox.getSelectedItem().get()).getCode();
					String diseasetype=((DiseaseType)jDiseaseTypeBox.getSelectedItem().get()).getCode();
					char sex;
					if(sexGroup.getValue()==MessageBundle.getMessage("angal.opd.all")) sex='A';
					else if(sexGroup.getValue()==MessageBundle.getMessage("angal.opd.male")) sex='M';
					else sex='F';
					String newPatient;
					if(groupNewPatient.getValue()==MessageBundle.getMessage("angal.opd.all")) newPatient="A";
					else if(groupNewPatient.getValue()==MessageBundle.getMessage("angal.common.new")) newPatient="N";
					else newPatient="R";
					
					GregorianCalendar dateFrom = getDateFrom();
					GregorianCalendar dateTo = getDateTo();
					
					if(dateFrom.after(dateTo)){
						MessageBox.createInfo().withCaption("Message").withMessage(MessageBundle.getMessage("angal.opd.datefrommustbebefordateto"))
						.withOkButton().open();
						return;
					}
					
					if(ageFrom>ageTo){
						MessageBox.createInfo().withCaption("Message").withMessage(MessageBundle.getMessage("angal.opd.agefrommustbelowerthanageto"))
						.withOkButton().open();
						jAgeFromTextField.setValue(ageTo.toString());
						ageFrom=ageTo;
						return;
					}
					
					model = new OpdBrowsingModel(diseasetype,disease,getDateFrom(), getDateTo(),ageFrom,ageTo,sex,newPatient);
					grid.setItems(opdList);
					// model.fireTableDataChanged();
					rowCounter.setCaption(rowCounterText + opdList.size());
			});
		}
		return filterButton;
	}

} 
