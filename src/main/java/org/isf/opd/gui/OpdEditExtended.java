package org.isf.opd.gui;

/*------------------------------------------
 * OpdEdit - add/edit an OPD registration
 * -----------------------------------------
 * modification history
 * 11/12/2005 - Vero, Rick  - first beta version 
 * 07/11/2006 - ross - renamed from Surgery 
 *                   - added visit date, disease 2, diseas3
 *                   - disease is not mandatory if re-attendance
 * 			         - version is now 1.0 
 * 28/05/2008 - ross - added referral to / referral from check boxes
 * 12/06/2008 - ross - added patient data
 * 					 - fixed error on checking "male"/"female" option: should check after translation
 * 					 - version is not a resource into the boundle, is locale to the form
 *                   - form rearranged in x,y coordinates 
 * 			         - version is now 1.1 
 * 26/08/2008 - teo  - added patient chooser 
 * 01/09/2008 - alex - added constructor forcall from Admission
 * 					 - set Patient oriented OPD
 * 					 - history management forthe patients
 * 					 - version now is 1.2
 * 01/01/2009 - Fabrizio - modified age fields back to Integer type
 * 13/02/2009 - Alex - added possibility to edit patient through EditButton
 * 					   added Edit.png icon
 * 					   fixed a bug on the first element in the comboBox
 * 13/02/2009 - Alex - added trash button forresetting searchfield
 * 03/13/2009 - Alex - lastOpdVisit appears at the bottom
 * 					   added control on duplicated diseases
 * 					   added re-attendance checkbox fora clear view
 * 					   new/re-attendance managed freely
 * 07/13/2009 - Alex - note field forthe visit recall last visit note when start OPD from
	  				   Admission and added Note even in Last OPD Visit
	  				   Extended patient search to patient code
 *------------------------------------------*/

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EventListener;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.time.ZoneId;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.EventListenerList;

import com.vaadin.server.ThemeResource;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;

import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.DateField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.RadioButtonGroup;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.shared.ui.window.WindowMode;

import de.steinwedel.messagebox.MessageBox;

import org.isf.disease.manager.DiseaseBrowserManager;
import org.isf.disease.model.Disease;
import org.isf.distype.manager.DiseaseTypeBrowserManager;
import org.isf.distype.model.DiseaseType;
import org.isf.examination.gui.PatientExaminationEdit;
import org.isf.examination.model.GenderPatientExamination;
import org.isf.examination.model.PatientExamination;
import org.isf.examination.service.ExaminationOperations;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.gui.MainMenu;
import org.isf.opd.manager.OpdBrowserManager;
import org.isf.opd.model.Opd;
import org.isf.patient.gui.PatientInsert;
import org.isf.patient.gui.PatientInsertExtended;
import org.isf.patient.manager.PatientBrowserManager;
import org.isf.patient.model.Patient;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.time.RememberDates;
import org.isf.utils.time.TimeTools;
import org.isf.utils.Logging;
import org.isf.utils.jobjects.ModalWindow;

import com.vaadin.ui.Component.Listener;

public class OpdEditExtended extends ModalWindow implements PatientInsertExtended.PatientListener{

	private static final long serialVersionUID = 1L;

	// @Override
	public void patientInserted(Patient aPatient){
		opdPatient=aPatient;
		setPatient(aPatient);
		patientList.add(aPatient);
		jComboPatResult.setItems(patientList);
		jComboPatResult.setSelectedItem(aPatient);
		jPatientEditButton.setEnabled(true);
	}

	// @Override
	public void patientUpdated(Patient aPatient){
		setPatient(opdPatient);
	}

	private List<OpdListener> opdListeners;

	public interface OpdListener{
		public void opdUpdated(Opd opd);

		public void opdInserted(Opd opd);
	}

	public void addOpdListener(OpdListener l){
		if(opdListeners==null)
			opdListeners = new ArrayList<OpdListener>();
		opdListeners.add(l);
	}

	public void removeOpdListener(OpdListener listener){
		if(opdListeners==null){
			opdListeners = new ArrayList<OpdListener>();
			return;
		}
		opdListeners.remove(listener);
	}

	private void fireOpdInserted(Opd opd){
		if(opdListeners != null){
			for(OpdListener opdListener : opdListeners){
				opdListener.opdInserted(opd);
			}
		}
	}

	private void fireOpdUpdated(Opd opd){
		if(opdListeners != null){
			for(OpdListener opdListener : opdListeners){
				opdListener.opdUpdated(opd);
			}
		}
	}

	private static final String VERSION = "1.3";

	private static final String LastOPDLabel = "<html><i>"+MessageBundle.getMessage("angal.opd.lastopdvisitm")+"</i></html>:";
	private static final String LastNoteLabel = "<html><i>"+MessageBundle.getMessage("angal.opd.lastopdnote")+"</i></html>:";

	private VerticalLayout jMainLayout = null;
	private HorizontalLayout jNorthLayout;
	private VerticalLayout leftCentralLayout;
	private FormLayout dataLayout = null;
	private HorizontalLayout buttonLayout = null;
	private Label jLabelDate = null;
	private Label jLabelDiseaseType1 = null;
	private Label jLabelDisease1 = null;
	private Label jLabelDis2 = null;
	private Label jLabelDis3 = null;

	private ComboBox diseaseTypeBox = null;
	private ComboBox diseaseBox1 = null;
	private ComboBox diseaseBox2 = null;
	private ComboBox diseaseBox3 = null;
	private JLabel jLabelAge = null;
	private JLabel jLabelSex = null;
	private GregorianCalendar dateIn = null;
	private DateFormat currentDateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.ITALIAN);
	private DateField opdDateFieldCal = null;
	private Button okButton = null;
	private Button cancelButton = null;
	private Button examinationButton = null;
	private CheckBox rePatientCheckBox = null;
	private CheckBox newPatientCheckBox = null;
	private CheckBox referralToCheckBox = null;
	private CheckBox referralFromCheckBox = null;
	private JPanel jPanelSex = null;
	private ButtonGroup group = null;

	private JLabel jLabelfirstName = null;
	private JLabel jLabelsecondName = null;
	private JLabel jLabeladdress = null;
	private JLabel jLabelcity = null;
	private JLabel jLabelnextKin = null;

	private Panel jPatientPanel = null;

	private TextField jFieldFirstName = null;
	private TextField jFieldSecondName = null;
	private TextField jFieldAddress = null;
	private TextField jFieldCity = null;
	private TextField jFieldNextKin = null;
	private TextField jFieldAge = null;

	private Opd opd;
	private boolean insert;
	private DiseaseType allType= new DiseaseType(MessageBundle.getMessage("angal.opd.alltype"),MessageBundle.getMessage("angal.opd.alltype"));

	// ADDED : teo
	private TextField jTextPatientSrc;
	private ComboBox jComboPatResult;
	private Label jSearchLabel = null;
	private RadioButtonGroup sexGroup;
	// ADDED : alex
	private Button jPatientEditButton = null;
	private Button jSearchButton = null;
	private Label jLabelLastOpdVisit = null;
	private Label jFieldLastOpdVisit = null;
	private Label jLabelLastOpdNote = null;
	private Label jFieldLastOpdNote = null;

	private Patient opdPatient = null;
	private Panel jNotePanel = null;
	private JScrollPane jNoteScrollPane = null;
	private TextArea jNoteTextArea = null;
	private JPanel jPatientNotePanel = null;
	private JScrollPane jPatientScrollNote = null;
	private TextArea jPatientNote = null;
	private HorizontalLayout jopdNumberLayout = null;
	private TextField jOpdNumField = null;
	private Label jOpdNumLabel = null;

	/*
	 * Managers and Arrays
	 */
	private DiseaseTypeBrowserManager typeManager = new DiseaseTypeBrowserManager();
	private DiseaseBrowserManager manager = new DiseaseBrowserManager();
	private ArrayList<DiseaseType> types = typeManager.getDiseaseType();
	private ArrayList<Disease> diseasesOPD = manager.getDiseaseOpd();
	private ArrayList<Disease> diseasesAll = manager.getDiseaseAll();
	private OpdBrowserManager opdManager = new OpdBrowserManager();
	private ArrayList<Opd> opdArray = new ArrayList<Opd>();
	private PatientBrowserManager patBrowser = new PatientBrowserManager();
	private ArrayList<Patient> pat = new ArrayList<Patient>();
	private ArrayList patientList;

	private Disease lastOPDDisease1;
	private JLabel JlabelOpd;
	private Logging logger;

	/**
	 * This method initializes
	 * @wbp.parser.constructor
	 * 
	 */
	public OpdEditExtended(Opd old, boolean inserting){
		opd = old;
		insert = inserting;
		if(!insert){
			if(opd.getpatientCode() != 0){
				PatientBrowserManager patBrowser = new PatientBrowserManager();
				opdPatient = patBrowser.getPatientAll(opd.getpatientCode());
			}else{ // old OPD has no PAT_ID => Create Patient from OPD
				opdPatient = new Patient(opd);
				opdPatient.setCode(0);
			}
		}
		initialize();
	}

	public OpdEditExtended(Opd opd, Patient patient, boolean inserting){
		this.opd = opd;
		opdPatient = patient;
		insert = inserting;
		initialize();
	}

	private void setPatient(Patient p){
		jFieldAge.setValue(TimeTools.getFormattedAge(p.getBirthDate()));
		jFieldFirstName.setValue(p.getFirstName());
		jFieldAddress.setValue(p.getAddress());
		jFieldCity.setValue(p.getCity());
		jFieldSecondName.setValue(p.getSecondName());
		jFieldNextKin.setValue(p.getNextKin());
		jPatientNote.setValue(p.getNote());
		jPatientPanel.setCaption(MessageBundle.getMessage("angal.opd.patient") + " (code: " + p.getCode() + ")");
		if(p.getSex() == 'M'){
			// Alex: SET SELECTED INSTEAD OF DOCLICK(), no listeners
			sexGroup.setValue(MessageBundle.getMessage("angal.opd.male"));
		}else if(p.getSex() == 'F'){
			// Alex: SET SELECTED INSTEAD OF DOCLICK(), no listeners
			sexGroup.setValue(MessageBundle.getMessage("angal.opd.female"));
		}
		if(insert)
			getLastOpd(p.getCode());
	}

	private void resetPatient(){
		jFieldAge.setValue("");
		jFieldFirstName.setValue("");
		jFieldAddress.setValue("");
		jFieldCity.setValue("");
		jFieldSecondName.setValue("");
		jFieldNextKin.setValue("");
		jPatientNote.setValue("");
		jPatientPanel.setCaption(MessageBundle.getMessage("angal.opd.patient"));
		sexGroup.setValue(MessageBundle.getMessage("angal.opd.male"));
		opdPatient = null;
	}

	// Alex: Resetting history from the last OPD visit forthe patient
	private boolean getLastOpd(int code){
		Opd lastOpd = opdManager.getLastOpd(code);

		if(lastOpd == null){
			newPatientCheckBox.setValue(true);
			rePatientCheckBox.setValue(false);
			jLabelLastOpdVisit.setValue("");
			jFieldLastOpdVisit.setValue("");
			jLabelLastOpdNote.setValue("");
			jFieldLastOpdNote.setValue("");
			jNoteTextArea.setValue("");
			return false;
		}
		lastOPDDisease1 = null;
		Disease lastOPDDisease2 = null;
		Disease lastOPDDisease3 = null;

		for(Disease disease : diseasesOPD){

			if(lastOpd.getDisease() != null && disease.getCode().compareTo(lastOpd.getDisease()) == 0){
				lastOPDDisease1 = disease;
			}
			if(lastOpd.getDisease2() != null && disease.getCode().compareTo(lastOpd.getDisease2()) == 0){
				lastOPDDisease2 = disease;
			}
			if(lastOpd.getDisease3() != null && disease.getCode().compareTo(lastOpd.getDisease3()) == 0){
				lastOPDDisease3 = disease;
			}
		}

		StringBuilder lastOPDDisease = new StringBuilder();
		lastOPDDisease.append(MessageBundle.getMessage("angal.opd.on")).append(" ").append(currentDateFormat.format(lastOpd.getVisitDate().getTime())).append(" - ");
		if(lastOPDDisease1 != null){
			// setAttendance();
			lastOPDDisease.append(lastOPDDisease1.getDescription());
		}
		if(lastOPDDisease2 != null) lastOPDDisease.append(", ").append(lastOPDDisease2.getDescription());
		if(lastOPDDisease3 != null) lastOPDDisease.append(", ").append(lastOPDDisease3.getDescription());
		jLabelLastOpdVisit.setContentMode(ContentMode.HTML);
		jLabelLastOpdVisit.setValue(LastOPDLabel);
		jFieldLastOpdVisit.setValue(lastOPDDisease.toString());
		jLabelLastOpdNote.setContentMode(ContentMode.HTML);
		jLabelLastOpdNote.setValue(LastNoteLabel);
		String note = lastOpd.getNote();
		jFieldLastOpdNote.setValue(note.equals("") ? MessageBundle.getMessage("angal.opd.nonote") : note);
		jNoteTextArea.setValue(lastOpd.getNote());

		return true;
	}

	private void setAttendance(){
		if(!insert) return;
		Object selectedObject = diseaseBox1.getSelectedItem().get();
		if(selectedObject instanceof Disease){
			Disease disease = (Disease) selectedObject;
			if(lastOPDDisease1 != null && disease.getCode().equals(lastOPDDisease1.getCode())){
				rePatientCheckBox.setValue(true);
				newPatientCheckBox.setValue(false);
			}else{
				rePatientCheckBox.setValue(false);
				newPatientCheckBox.setValue(true);
			}
		}
	}

	/**
	 * @return the jNorthLayout
	 */
	private HorizontalLayout getNorthLayout(){
		if(jNorthLayout == null){
			String referralTo = "";
			String referralFrom = "";
			jNorthLayout = new HorizontalLayout();
			rePatientCheckBox = new CheckBox(MessageBundle.getMessage("angal.opd.reattendance"));
			newPatientCheckBox = new CheckBox(MessageBundle.getMessage("angal.opd.newattendance"));
			newPatientCheckBox.addValueChangeListener(e -> {
				if(newPatientCheckBox.getValue()){
					newPatientCheckBox.setValue(true);
					rePatientCheckBox.setValue(false);
				}else{
					newPatientCheckBox.setValue(false);
					rePatientCheckBox.setValue(true);
				}
			});
			rePatientCheckBox.addValueChangeListener(e -> {
				if(rePatientCheckBox.getValue()){
					rePatientCheckBox.setValue(true);
					newPatientCheckBox.setValue(false);
				}else{
					newPatientCheckBox.setValue(true);
					rePatientCheckBox.setValue(false);
				}
			});
			jNorthLayout.addComponent(rePatientCheckBox);
			jNorthLayout.addComponent(newPatientCheckBox);
			if(!insert){
				if(opd.getNewPatient().equals("N"))
					newPatientCheckBox.setValue(true);
				else
					rePatientCheckBox.setValue(true);
			}
			referralFromCheckBox = new CheckBox(MessageBundle.getMessage("angal.opd.referral.from"));
			jNorthLayout.addComponent(referralFromCheckBox);
			if(!insert){
				referralFrom = opd.getReferralFrom();
				if(referralFrom == null) referralFrom="";
				if(referralFrom.equals("R"))referralFromCheckBox.setValue(true);
			}
			referralToCheckBox = new CheckBox(MessageBundle.getMessage("angal.opd.referral.to"));
			jNorthLayout.addComponent(referralToCheckBox);
			if(!insert){
				referralTo = opd.getReferralTo();
				if(referralTo == null) referralTo="";
				if(referralTo.equals("R"))referralToCheckBox.setValue(true);
			}
		}
		return jNorthLayout;
	}

	/**
	 * @return the leftCentralLayout
	 */
	private VerticalLayout getLeftCentralLayout(){
		if(leftCentralLayout == null){
			leftCentralLayout = new VerticalLayout();
			leftCentralLayout.addStyleName("noverticalpadding");
			leftCentralLayout.setMargin(false);
			leftCentralLayout.addComponent(getDataLayout());
			leftCentralLayout.addComponent(getLastOpdVisit());
			leftCentralLayout.addComponent(getLastOpdNote());
			leftCentralLayout.addComponent(getPatientPanel());
		}
		return leftCentralLayout;
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize(){
		showAsModal();
		this.setWindowMode(WindowMode.MAXIMIZED);
		logger = new Logging();
		UI.getCurrent().addWindow(this);
		this.setContent(getMainLayout());
		if(insert){
			this.setCaption(MessageBundle.getMessage("angal.opd.newopdregistration") + "(" + VERSION + ")");
		}else{
			this.setCaption(MessageBundle.getMessage("angal.opd.editopdregistration") + "(" + VERSION + ")");
		}
		// this.setVisible(true);
		// if(insert){
		// 	jTextPatientSrc.requestFocusInWindow();
		// }else{
		// 	jNoteTextArea.requestFocusInWindow();
		// }
		// this.addWindowListener(new WindowAdapter(){

		// 	public void windowClosing(WindowEvent e){
		// 		// to free memory
		// 		pat.clear();
		// 		opdArray.clear();
		// 		diseasesAll.clear();
		// 		diseasesOPD.clear();
		// 		types.clear();
		// 		jComboPatResult.removeAllItems();
		// 		diseaseTypeBox.removeAllItems();
		// 		diseaseBox1.removeAllItems();
		// 		diseaseBox2.removeAllItems();
		// 		diseaseBox3.removeAllItems();
		// 		// dispose();
		// 	}
		// });
	}

	/**
	 * This method initializes jPanel1
	 * 
	 * @return javax.swing.JPanel
	 */
	private VerticalLayout getMainLayout(){
		if(jMainLayout == null){
			jMainLayout = new VerticalLayout();
			jMainLayout.addComponent(getNorthLayout());
			HorizontalLayout centralLayout = new HorizontalLayout();
			centralLayout.setWidth("100%");
			centralLayout.addComponent(getLeftCentralLayout());
			centralLayout.addComponent(getJNotePanel());
			centralLayout.setExpandRatio(getLeftCentralLayout(),3);
			centralLayout.setExpandRatio(getJNotePanel(),1);
			jMainLayout.addComponent(centralLayout);
			jMainLayout.addComponent(getButtonLayout());//qqq
		}
		return jMainLayout;
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private HorizontalLayout getLastOpdVisit(){
		HorizontalLayout hl = new HorizontalLayout();
		hl.setMargin(false);
		jLabelLastOpdVisit = new Label(" ");
		// jLabelLastOpdVisit.setHorizontalAlignment(SwingConstants.RIGHT);
		// jLabelLastOpdVisit.setForeground(Color.RED);
		hl.addComponent(jLabelLastOpdVisit);

		jFieldLastOpdVisit = new Label(" ");
		// jFieldLastOpdVisit.setFocusable(false);
		hl.addComponent(jFieldLastOpdVisit);
		return hl;
	}

	private HorizontalLayout getLastOpdNote(){
		HorizontalLayout hl = new HorizontalLayout();
		hl.setMargin(false);
		jLabelLastOpdNote = new Label(" ");
		// jLabelLastOpdNote.setHorizontalAlignment(SwingConstants.RIGHT);
		// jLabelLastOpdNote.setForeground(Color.RED);
		hl.addComponent(jLabelLastOpdNote);

		jFieldLastOpdNote = new Label(" ");
		// jFieldLastOpdNote.setPreferredSize(new Dimension(500, 30));
		// jFieldLastOpdNote.setFocusable(false);
		hl.addComponent(jFieldLastOpdNote);
		return hl;
	}

	private FormLayout getDataLayout(){
		if(dataLayout == null){
			dataLayout = new FormLayout();
			dataLayout.addStyleName("noverticalpadding");
			HorizontalLayout dateNOPD = new HorizontalLayout();
			dateNOPD.setMargin(false);
			dateNOPD.setCaption(MessageBundle.getMessage("angal.opd.attendancedate"));
			dateNOPD.addComponent(getopdDateFieldCal());
			dateNOPD.addComponent(getOpdNumberLayout());
			dataLayout.addComponent(dateNOPD);

			HorizontalLayout searchPatientLayout = new HorizontalLayout();
			searchPatientLayout.setCaption(MessageBundle.getMessage("angal.opd.search"));
			searchPatientLayout.addComponent(getJTextPatientSrc());
			searchPatientLayout.addComponent(getJSearchButton());
			searchPatientLayout.addComponent(getSearchBox());
			searchPatientLayout.addComponent(getJPatientEditButton());
			dataLayout.addComponent(searchPatientLayout);

			dataLayout.addComponent(getDiseaseTypeBox());
			getDiseaseTypeBox().setCaption(MessageBundle.getMessage("angal.opd.diseasetype"));
			getDiseaseTypeBox().setWidth("100%");

			dataLayout.addComponent(getDiseaseBox());
			getDiseaseBox().setCaption(MessageBundle.getMessage("angal.opd.diagnosis"));
			getDiseaseBox().setWidth("100%");

			dataLayout.addComponent(getDiseaseBox2());
			getDiseaseBox2().setCaption(MessageBundle.getMessage("angal.opd.diagnosisnfulllist"));
			getDiseaseBox2().setWidth("100%");

			dataLayout.addComponent(getDiseaseBox3());
			getDiseaseBox3().setCaption(MessageBundle.getMessage("angal.opd.diagnosisnfulllist3"));
			getDiseaseBox3().setWidth("100%");
		}
		return dataLayout;
	}

	/**
	 * 
	 */
	private DateField getopdDateFieldCal(){
		if(opdDateFieldCal == null){
			String d = "";

			java.util.Date myDate = null;
			if(insert){
				if(RememberDates.getLastOpdVisitDateGregorian() == null){
					dateIn = new GregorianCalendar();
				}else{
					dateIn = RememberDates.getLastOpdVisitDateGregorian();
				}
			}else{
				dateIn = opd.getVisitDate();
			}
			if(dateIn == null){
				d = "";
				logger.info("datein null");
			}else{
				myDate = dateIn.getTime();
				d = currentDateFormat.format(myDate);
			}
			try{
				opdDateFieldCal = new DateField(null,currentDateFormat.parse(d).toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
				opdDateFieldCal.setLocale(new Locale(GeneralData.LANGUAGE));
				opdDateFieldCal.setDateFormat("dd/MM/yy");
			}catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return opdDateFieldCal;
	}

	private HorizontalLayout getOpdNumberLayout(){
		if(jopdNumberLayout == null){

			jopdNumberLayout = new HorizontalLayout();

			jOpdNumLabel = new Label();
			jOpdNumLabel.setValue(MessageBundle.getMessage("angal.opd.opdnumber"));

			jOpdNumField = new TextField();
			jOpdNumField.setMaxLength(10);

			jOpdNumField.setValue(getOpdNum());

			// jOpdNumField.setColumns(11);

			jopdNumberLayout.addComponent(jOpdNumLabel);
			jopdNumberLayout.addComponent(jOpdNumField);
		}
		return jopdNumberLayout;
	}

	private String getOpdNum(){
		int OpdNum;
		if(!insert)
			return "" + opd.getYear();
		GregorianCalendar date = new GregorianCalendar();
		date.setTime(Date.from(opdDateFieldCal.getValue().atStartOfDay(opdDateFieldCal.getZoneId().systemDefault()).toInstant()));
		opd.setYear(opdManager.getProgYear(date.get(Calendar.YEAR)) + 1);
		OpdNum = opd.getYear();
		return "" + OpdNum;
	}

	private Panel getJNotePanel(){
		if(jNotePanel == null){
			jNotePanel = new Panel();
			jNotePanel.setWidth("100%");
			jNotePanel.setCaption(MessageBundle.getMessage("angal.opd.noteandsymptom"));
			jNotePanel.setContent(getJTextArea());
		}
		return jNotePanel;
	}

	private TextArea getJTextArea(){
		if(jNoteTextArea == null){
			jNoteTextArea = new TextArea();
			jNoteTextArea.setWidth("100%");
			jNoteTextArea.setRows(15);
			if(!insert){
				jNoteTextArea.setValue(opd.getNote());
			}
			jNoteTextArea.setWordWrap(true);
		}
		return jNoteTextArea;
	}

	/**
	 * This method initializes jComboBox
	 * 
	 * @return javax.swing.JComboBox
	 */
	private ComboBox getDiseaseTypeBox(){
		if(diseaseTypeBox == null){
			diseaseTypeBox = new ComboBox();
			diseaseTypeBox.setEmptySelectionAllowed(false);
			DiseaseType elem2 = null;
			// diseaseTypeBox.setMaximumSize(new Dimension(400, 50));
			ArrayList<DiseaseType> diseaseList = new ArrayList();
			diseaseList.add(allType);
			for(DiseaseType elem : types){
				if(!insert && opd.getDiseaseType() != null){
					if(opd.getDiseaseType().equals(elem.getCode())){
						elem2 = elem;
					}
				}
				diseaseList.add(elem);
			}
			diseaseTypeBox.setItems(diseaseList);
			if(elem2 != null){
				diseaseTypeBox.setValue(elem2);
			}else{
				diseaseTypeBox.setValue(allType);
			}
			diseaseTypeBox.addValueChangeListener(e -> {
				diseaseBox1.setItems();
				getDiseaseBox();
			});
		}
		return diseaseTypeBox;
	}

	/**
	 * This method initializes jComboBox1
	 * 
	 * @return javax.swing.JComboBox
	 */
	private ComboBox getDiseaseBox(){
		if(diseaseBox1 == null){
			diseaseBox1 = new ComboBox();
			// diseaseBox1.setMaximumSize(new Dimension(400, 50));
			diseaseBox1.setEmptySelectionAllowed(false);
			diseaseBox1.addValueChangeListener(e -> {
				setAttendance();
			});
		};
		Disease elem2 = null;
		ArrayList<Disease> diseaseList = new ArrayList();
		// diseaseList.add("");
		for(Disease elem : diseasesOPD){
			if(((DiseaseType) diseaseTypeBox.getSelectedItem().get()).equals(allType))
				diseaseList.add(elem);
			else if(elem.getType().equals((DiseaseType) diseaseTypeBox.getSelectedItem().get()))
				diseaseList.add(elem);
			if(!insert && opd.getDisease() != null){
				if(opd.getDisease().equals(elem.getCode())){
					elem2 = elem;
				}
			}
		}
		diseaseBox1.setItems(diseaseList);
		if(!insert){
			if(elem2 != null){
				diseaseBox1.setValue(elem2);
			}else{ // try in the canceled diseases
				if(opd.getDisease() != null){
					for(Disease elem : diseasesAll){
						if(opd.getDisease().compareTo(elem.getCode()) == 0){
							MessageBox.createInfo().withCaption("Message").withMessage(MessageBundle.getMessage("angal.opd.disease1mayhavebeencanceled"))
							.withOkButton().open();
							diseaseList.add(elem);
							diseaseBox1.setItems(diseaseList);
							diseaseBox1.setValue(elem);
						}
					}
				}
			}
		}
		return diseaseBox1;
	}

	public ComboBox getDiseaseBox2(){
		if(diseaseBox2 == null){
			diseaseBox2 = new ComboBox();
			diseaseBox2.setEmptySelectionAllowed(false);
			// diseaseBox2.setMaximumSize(new Dimension(400, 50));
		};
		Disease elem2 = null;
		ArrayList<Disease> diseaseList2 = new ArrayList();
		// diseaseList2.add("");

		for(Disease elem : diseasesOPD){
			diseaseList2.add(elem);
			if(!insert && opd.getDisease2() != null){
				if(opd.getDisease2().equals(elem.getCode())){
					elem2 = elem;
				}
			}
		}
		diseaseBox2.setItems(diseaseList2);
		if(elem2 != null){
			diseaseBox2.setValue(elem2);
		}else{ // try in the canceled diseases
			if(opd.getDisease2() != null){
				for(Disease elem : diseasesAll){
					if(opd.getDisease2().compareTo(elem.getCode()) == 0){
						MessageBox.createInfo().withCaption("Message").withMessage(MessageBundle.getMessage("angal.opd.disease2mayhavebeencanceled"))
						.withOkButton().open();
						diseaseList2.add(elem);
						diseaseBox2.setItems(diseaseList2);
						diseaseBox2.setValue(elem);
					}
				}
			}
		}
		return diseaseBox2;
	}

	private ComboBox getDiseaseBox3(){
		if(diseaseBox3 == null){
			diseaseBox3 = new ComboBox();
			diseaseBox3.setEmptySelectionAllowed(false);
			// diseaseBox3.setMaximumSize(new Dimension(400, 50));
		};
		Disease elem2 = null;
		ArrayList<Disease> diseaseList3 = new ArrayList();
		// diseaseList3.add("");

		for(Disease elem : diseasesOPD){
			diseaseList3.add(elem);
			if(!insert && opd.getDisease3() != null){
				if(opd.getDisease3().equals(elem.getCode())){
					elem2 = elem;
				}
			}
		}
		diseaseBox3.setItems(diseaseList3);
		if(elem2 != null){
			diseaseBox3.setValue(elem2);
		}else{ // try in the canceled diseases
			if(opd.getDisease3() != null){
				for(Disease elem : diseasesAll){
					if(opd.getDisease3().compareTo(elem.getCode()) == 0){
						MessageBox.createInfo().withCaption("Message").withMessage(MessageBundle.getMessage("angal.opd.disease3mayhavebeencanceled"))
						.withOkButton().open();
						diseaseList3.add(elem);
						diseaseBox3.setItems(diseaseList3);
						diseaseBox3.setValue(elem);
					}
				}
			}
		}
		return diseaseBox3;
	}

	/**
	 * 
	 */
	private TextField getJTextPatientSrc(){
		if(jTextPatientSrc == null){
			jTextPatientSrc = new TextField();
			jTextPatientSrc.setMaxLength(16);
			jTextPatientSrc.addShortcutListener(new ShortcutListener("Shortcut Name", ShortcutAction.KeyCode.ENTER, null){
				@Override
				public void handleAction(Object sender, Object target){
					jSearchButton.click();
				}
			});

		}
		return jTextPatientSrc;
	}

	private Button getJSearchButton(){
		if(jSearchButton == null){
			jSearchButton = new Button();
			jSearchButton.setIcon(new ThemeResource("icons/zoom_r_button.png"));
			jSearchButton.addClickListener(e -> {
				jComboPatResult.setItems();
				pat = patBrowser.getPatientWithHeightAndWeight(jTextPatientSrc.getValue());
				getSearchBox(jTextPatientSrc.getValue());
			});
		}
		return jSearchButton;
	}

	private void getSearchBox(String s){
		String key = s;
		String[] s1;
		patientList = new ArrayList();
		jComboPatResult.setEmptySelectionAllowed(false);

		if(key == null || key.compareTo("") == 0){
			patientList.add(MessageBundle.getMessage("angal.opd.selectapatient"));
			patientList.add(MessageBundle.getMessage("angal.opd.newpatient"));
			jLabelLastOpdVisit.setValue(" ");
			jFieldLastOpdVisit.setValue(" ");
			jLabelLastOpdNote.setValue(" ");
			jFieldLastOpdNote.setValue(" ");
			if(jNoteTextArea != null) jNoteTextArea.setValue("");
			if(jPatientPanel != null) resetPatient();
		}

		for(Patient elem : pat){
			if(key != null){
				s1 = key.split(" ");
				String name = elem.getSearchString();
				int a = 0;
				for(int i = 0; i < s1.length; i++){
					if(name.contains(s1[i].toLowerCase()) == true){
						a++;
					}
				}
				if(a == s1.length)	patientList.add(elem);
			} else
				patientList.add(elem);
		}
		jComboPatResult.setItems(patientList);
		// ADDED: Workaround forno items
		if(patientList.size() == 0){//qqq
			opdPatient = null;
			if(jPatientPanel != null) resetPatient();
			jPatientEditButton.setEnabled(true);
			jComboPatResult.setItems("Patient not Found");
			jComboPatResult.setValue("Patient not Found");
		}else
			jComboPatResult.setValue(patientList.get(0));
		// ADDED: Workaround forone item only
		if(patientList.size() == 1){
			opdPatient = (Patient) jComboPatResult.getSelectedItem().get();
			setPatient(opdPatient);
			jPatientEditButton.setEnabled(true);
		}
		// ADDED: Workaround forfirst item
		if(patientList.size() > 0){
			if(patientList.get(0) instanceof Patient){
				opdPatient = (Patient) patientList.get(0);
				setPatient(opdPatient);
				jPatientEditButton.setEnabled(true);
			}
		}
		jTextPatientSrc.focus();
	}

	private ComboBox getSearchBox(){
		if(jComboPatResult == null){
			jComboPatResult = new ComboBox();
			patientList = new ArrayList();
			jComboPatResult.setEmptySelectionAllowed(false);
			// jComboPatResult.setMaximumSize(new Dimension(400,50));
			if(opdPatient != null){
				jComboPatResult.setItems(opdPatient);
				jComboPatResult.setEnabled(false);
				jTextPatientSrc.setEnabled(false);
				jSearchButton.setEnabled(false);
				return jComboPatResult;
			}else{
				patientList.add(MessageBundle.getMessage("angal.opd.selectapatient"));
				patientList.add(MessageBundle.getMessage("angal.opd.newpatient"));
				jComboPatResult.setItems(patientList);
				jComboPatResult.setValue(MessageBundle.getMessage("angal.opd.selectapatient"));
			}

			jComboPatResult.addValueChangeListener(e -> {
				if(jComboPatResult.getSelectedItem().isPresent()){
					if(jComboPatResult.getSelectedItem().get().toString().compareTo(MessageBundle.getMessage("angal.opd.newpatient")) == 0){
						if(GeneralData.PATIENTEXTENDED){
							PatientInsertExtended newrecord = new PatientInsertExtended(new Patient(), true);
							newrecord.addPatientListener(this);
							// newrecord.setVisible(true);
						}else{
				// 			// PatientInsert newrecord = new PatientInsert(OpdEditExtended.this, new Patient(), true);
				// 			// // newrecord.addPatientListener(OpdEditExtended.this);
				// 			// newrecord.setVisible(true);
						}

					}else if(jComboPatResult.getSelectedItem().get().toString().compareTo(MessageBundle.getMessage("angal.opd.selectapatient")) == 0){
						jPatientEditButton.setEnabled(false);
					}else{
						opdPatient = (Patient) jComboPatResult.getSelectedItem().get();
						setPatient(opdPatient);
						jPatientEditButton.setEnabled(true);
					}
				}
			});
		}
		return jComboPatResult;
	}

	// ADDED: Alex
	private Button getJPatientEditButton(){
		if(jPatientEditButton == null){
			jPatientEditButton = new Button();
			jPatientEditButton.setIcon(new ThemeResource("icons/edit_button.png"));
			// jPatientEditButton.setPreferredSize(new Dimension(20, 20));
			jPatientEditButton.addClickListener(e -> {
				if(opdPatient != null){
					if(GeneralData.PATIENTEXTENDED){
						PatientInsertExtended editrecord = new PatientInsertExtended(opdPatient, false);
						editrecord.addPatientListener(this);
						// editrecord.setVisible(true);
					}else{
						// PatientInsert editrecord = new PatientInsert(OpdEditExtended.this, opdPatient, false);
						// editrecord.addPatientListener(OpdEditExtended.this);
						// editrecord.setVisible(true);
					}
				}
			});
			if(!insert) jPatientEditButton.setEnabled(false);
		}
		return jPatientEditButton;
	}


	// alex: metodo ridefinito, i settaggi avvegono tramite SetPatient()
	private Panel getPatientPanel(){
		if(jPatientPanel == null){
			jPatientPanel = new Panel();
			jPatientPanel.setWidth("100%");
			jPatientPanel.addStyleName("noverticalpadding");
			jPatientPanel.setCaption(MessageBundle.getMessage("angal.opd.patient"));

			HorizontalLayout patientLayout = new HorizontalLayout();
			patientLayout.setWidth("100%");
			patientLayout.setMargin(false);
			jPatientPanel.setContent(patientLayout);

			FormLayout patientForm = new FormLayout();
			patientForm.setMargin(false);
			patientLayout.addComponent(patientForm);

			jFieldFirstName = new TextField();
			jFieldFirstName.setMaxLength(50);
			jFieldFirstName.setCaption(MessageBundle.getMessage("angal.opd.first.name") + "\t");
			jFieldFirstName.setEnabled(false);
			patientForm.addComponent(jFieldFirstName);

			jFieldSecondName = new TextField();
			jFieldSecondName.setCaption(MessageBundle.getMessage("angal.opd.second.name") + "\t");
			jFieldSecondName.setMaxLength(50);
			jFieldSecondName.setEnabled(false);
			// jFieldSecondName.setFocusable(false);
			patientForm.addComponent(jFieldSecondName);

			jFieldAddress = new TextField();
			jFieldAddress.setCaption(MessageBundle.getMessage("angal.opd.address"));
			jFieldAddress.setMaxLength(50);
			jFieldAddress.setEnabled(false);
			// jFieldAddress.setFocusable(false);
			patientForm.addComponent(jFieldAddress);

			jFieldCity = new TextField();
			jFieldCity.setCaption(MessageBundle.getMessage("angal.opd.city"));
			jFieldCity.setMaxLength(50);
			jFieldCity.setEnabled(false);
			// jFieldCity.setFocusable(false);
			patientForm.addComponent(jFieldCity);
			
			jFieldNextKin = new TextField();
			jFieldNextKin.setMaxLength(50);
			jFieldNextKin.setCaption(MessageBundle.getMessage("angal.opd.nextkin"));
			jFieldNextKin.setEnabled(false);
			// jFieldNextKin.setFocusable(false);
			patientForm.addComponent(jFieldNextKin);

			jFieldAge = new TextField();
			jFieldAge.setMaxLength(50);
			jFieldAge.setCaption(MessageBundle.getMessage("angal.opd.age"));
			jFieldAge.setEnabled(false);
			// jFieldAge.setFocusable(false);
			patientForm.addComponent(jFieldAge);

			sexGroup = new RadioButtonGroup();
			sexGroup.setCaption(MessageBundle.getMessage("angal.opd.sex"));
			sexGroup.setItems(MessageBundle.getMessage("angal.opd.male"),MessageBundle.getMessage("angal.opd.female"));
			sexGroup.setValue(MessageBundle.getMessage("angal.opd.male"));
			sexGroup.setEnabled(false);
			patientForm.addComponent(sexGroup);
			
			patientLayout.addComponent(getJPatientNoteArea());//getJPatientNote()

			if(opdPatient != null) setPatient(opdPatient);
		}
		return jPatientPanel;
	}

	private TextArea getJPatientNoteArea(){
		if(jPatientNote == null){
			jPatientNote = new TextArea();
			jPatientNote.setWidth("100%");
			jPatientNote.setRows(15);
			jPatientNote.setWordWrap(true);
			if(!insert){
				jPatientNote.setValue(opdPatient.getNote());
			}
			jPatientNote.setEnabled(false);
		}
		return jPatientNote;
	}

	/**
	 * This method initializes buttonLayout
	 * 
	 * @return javax.swing.JPanel
	 */
	private HorizontalLayout getButtonLayout(){
		if(buttonLayout == null){
			buttonLayout = new HorizontalLayout();
			buttonLayout.addComponent(getOkButton());//qqq
			if(insert && MainMenu.checkUserGrants("btnopdnewexamination") || 
					!insert && MainMenu.checkUserGrants("btnopdeditexamination"))
				buttonLayout.addComponent(getExaminationButton());
			buttonLayout.addComponent(getCancelButton());
		}
		return buttonLayout;
	}

	private Button getExaminationButton(){
		if(examinationButton == null){
			examinationButton = new Button(MessageBundle.getMessage("angal.opd.examination"));
			////examinationButton.setClickShortcut(KeyEvent.VK_E);

			examinationButton.addClickListener(e->{
				if(opdPatient == null){
					MessageBox.createInfo().withCaption("Message").withMessage(MessageBundle.getMessage("angal.opd.pleaseselectapatient")).withOkButton().open();
					return;
				}

				PatientExamination patex;
				ExaminationOperations examOperations = new ExaminationOperations();

				PatientExamination lastPatex = examOperations.getLastByPatID(opdPatient.getCode());
				if(lastPatex != null){
					patex = examOperations.getFromLastPatientExamination(lastPatex);
				}else{
					patex = examOperations.getDefaultPatientExamination(opdPatient);
				}

				GenderPatientExamination gpatex = new GenderPatientExamination(patex, opdPatient.getSex() == 'M');

				PatientExaminationEdit dialog = new PatientExaminationEdit(gpatex);
				// dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				// dialog.pack();
				// dialog.setLocationRelativeTo(null);
				// dialog.setVisible(true);
			});
		}
		return examinationButton;
	}

	/**
	 * This method initializes okButton
	 * 
	 * @return javax.swing.JButton
	 */

	//alex: modified method to take data from Patient Object instead from jTextFields
	private Button getOkButton(){
		if(okButton == null){
			okButton = new Button(MessageBundle.getMessage("angal.common.ok"));
			////okButton.setClickShortcut(KeyEvent.VK_O);

			okButton.addClickListener(e->{
				if(!jOpdNumField.getValue().equals("") || !jOpdNumField.getValue().contains(" ")){
					OpdBrowserManager opm = new OpdBrowserManager();
					GregorianCalendar gregDate = new GregorianCalendar();
					if(opdDateFieldCal.getValue()==null){
						MessageBox.createInfo().withCaption("Message").withMessage(MessageBundle.getMessage("angal.opd.pleaseinsertattendancedate"))
						.withOkButton().open();
						return;
					}
					gregDate.setTime(Date.from(opdDateFieldCal.getValue().atStartOfDay(opdDateFieldCal.getZoneId().systemDefault()).toInstant()));
					boolean opdNumExist = false;
					int opdNum;
					try {
						opdNum = Integer.parseInt(jOpdNumField.getValue());
					} catch (NumberFormatException e1){
						MessageBox.createInfo().withCaption("Message").withMessage(MessageBundle.getMessage("angal.opd.opdnumbermustbeanumber"))
						.withOkButton().open();
						return;
					}
					int opdEdit = 0;
					if(insert){
						opdNumExist = opm.isExistOpdNum(opdNum, gregDate.get(Calendar.YEAR));
					}else{
						opdEdit = opd.getYear();
					}

					if(opdNum != opdEdit){
						opdNumExist = opm.isExistOpdNum(opdNum, gregDate.get(Calendar.YEAR));
					}else{
						opdNumExist = false;
					}

					if(!opdNumExist){
						opd.setYear(opdNum);

						boolean result = false;
						String newPatient = "";
						String referralTo = null;
						String referralFrom = null;
						String disease = null;
						String disease2 = null;
						String disease3 = null;
						String diseaseType = null;
						String diseaseDesc = "";
						String diseaseTypeDesc = "";

						if(!diseaseBox1.getSelectedItem().isPresent()){
							MessageBox.createInfo().withCaption("Message").withMessage(MessageBundle.getMessage("angal.opd.pleaseselectadisease"))
							.withOkButton().open();
							return;
						}
						if(opdPatient == null){
							MessageBox.createInfo().withCaption("Message").withMessage(MessageBundle.getMessage("angal.opd.pleaseselectapatient"))
							.withOkButton().open();
							return;
						}

						if(newPatientCheckBox.getValue()){
							newPatient = "N";
						}else{
							newPatient = "R";
						}
						if(referralToCheckBox.getValue()){
							referralTo = "R";
						}else{
							referralTo = "";
						}
						if(referralFromCheckBox.getValue()){
							referralFrom = "R";
						}else{
							referralFrom = "";
						}
						// disease
						if(diseaseBox1.getSelectedItem().isPresent()){
							disease = ((Disease) diseaseBox1.getSelectedItem().get()).getCode();
							diseaseDesc = ((Disease) diseaseBox1.getSelectedItem().get()).getDescription();
							diseaseTypeDesc = ((Disease) diseaseBox1.getSelectedItem().get()).getType().getDescription();
							diseaseType = (((Disease) diseaseBox1.getSelectedItem().get()).getType().getCode());
						}
						// disease2
						if(diseaseBox2.getSelectedItem().isPresent()){
							disease2 = ((Disease) diseaseBox2.getSelectedItem().get()).getCode();
						}
						// disease3
						if(diseaseBox3.getSelectedItem().isPresent()){
							disease3 = ((Disease) diseaseBox3.getSelectedItem().get()).getCode();
						}
						// Check double diseases
						if(disease2 != null && disease == disease2){
							MessageBox.createInfo().withCaption("Message").withMessage(MessageBundle.getMessage("angal.opd.duplicatediseasesnotallowed"))
							.withOkButton().open();
							disease2 = null;
							return;
						}
						if(disease3 != null && disease == disease3){
							MessageBox.createInfo().withCaption("Message").withMessage(MessageBundle.getMessage("angal.opd.duplicatediseasesnotallowed"))
							.withOkButton().open();
							disease3 = null;
							return;
						}
						if(disease2 != null && disease3 != null && disease2 == disease3){
							MessageBox.createInfo().withCaption("Message").withMessage(MessageBundle.getMessage("angal.opd.duplicatediseasesnotallowed"))
							.withOkButton().open();
							disease3 = null;
							return;
						}
						String d = currentDateFormat.format(Date.from(opdDateFieldCal.getValue().atStartOfDay(opdDateFieldCal.getZoneId().systemDefault()).toInstant()));
							opd.setNote(jNoteTextArea.getValue());
							opd.setpatientCode(opdPatient.getCode());
							opd.setFullName(opdPatient.getName());
							opd.setNewPatient(newPatient);
							opd.setReferralFrom(referralFrom);
							opd.setReferralTo(referralTo);
							opd.setSex(opdPatient.getSex());

							opd.setfirstName(opdPatient.getFirstName());
							opd.setsecondName(opdPatient.getSecondName());
							opd.setaddress(opdPatient.getAddress());
							opd.setcity(opdPatient.getCity());
							opd.setnextKin(opdPatient.getNextKin());

							opd.setDisease(disease);
							opd.setDiseaseType(diseaseType);
							opd.setDiseaseDesc(diseaseDesc);
							opd.setDiseaseTypeDesc(diseaseTypeDesc);
							opd.setDisease2(disease2);
							opd.setDisease3(disease3);

							String user = MainMenu.getUser();
							opd.setUserID(user);
							opd.setVisitDate(gregDate);
							if(insert){
								GregorianCalendar date = new GregorianCalendar();
								opd.setYear(opdNum);
								opd.setAge(opdPatient.getAge());
								// remember forlater use
								RememberDates.setLastOpdVisitDate(gregDate);
								result = opdManager.newOpd(opd);
								if(result){
									fireOpdInserted(opd);
								}
								if(!result)
									MessageBox.createInfo().withCaption("Message").withMessage(MessageBundle.getMessage("angal.opd.thedatacouldnotbesaved"))
									.withOkButton().open();
								else
									this.close();
							}else{ // Update
								opd.setYear(opdNum);
								result = opdManager.updateOpd(opd);
								if(result){
									fireOpdUpdated(opd);
								};
								if(!result)
									MessageBox.createInfo().withCaption("Message").withMessage(MessageBundle.getMessage("angal.opd.thedatacouldnotbesaved"))
									.withOkButton().open();
								else
									this.close();
							}
					}else{
						MessageBox.createInfo().withCaption("Message").withMessage(MessageBundle.getMessage("angal.opd.opdnumberalreadyexist"))
						.withOkButton().open();
						return;
					};
				}else
					logger.info("contain space");
			});
		}
		return okButton;
	}

	/**
	 * This method initializes cancelButton
	 * 
	 * @return javax.swing.JButton
	 */
	private Button getCancelButton(){
		if(cancelButton == null){
			cancelButton = new Button(MessageBundle.getMessage("angal.common.cancel"));
			////cancelButton.setClickShortcut(KeyEvent.VK_C);
			cancelButton.addClickListener(e->{
				// to free Memory
				pat.clear();
				opdArray.clear();
				diseasesAll.clear();
				diseasesOPD.clear();
				types.clear();
				jComboPatResult.setItems();
				diseaseTypeBox.setItems();
				diseaseBox1.setItems();
				diseaseBox2.setItems();
				diseaseBox3.setItems();
				this.close();
			});
		}
		return cancelButton;
	}

	/*
	 * set a specific border+title to a panel
	 */
	private JPanel setMyBorder(JPanel c, String title){
		javax.swing.border.Border b2 = BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(title), BorderFactory
						.createEmptyBorder(0, 0, 0, 0));
		c.setBorder(b2);
		return c;
	}

	/*
	 * set a specific border+title+matte to a panel
	 */
	private JPanel setMyMatteBorder(JPanel c, String title){
		c.setBorder(new TitledBorder(new MatteBorder(1, 20, 1, 1, (Color) new Color(153, 180, 209)), title, TitledBorder.LEADING, TitledBorder.TOP, null, null));
		return c;
	}
}