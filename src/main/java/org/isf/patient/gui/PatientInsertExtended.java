package org.isf.patient.gui;

import java.lang.InterruptedException;
import java.lang.Thread;
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
// import java.awt.event.FocusEvent;
// import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EventListener;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.List;

// import javax.swing.BorderFactory;
// import javax.swing.BoxLayout;
// import javax.swing.ButtonGroup;
// import javax.swing.ImageIcon;
// import javax.swing.Button;
// import javax.swing.ComboBox;
// import javax.swing.JDialog;
// import javax.swing.JFrame;
// import javax.swing.Label;
// import javax.swing.JOptionPane;
// import javax.swing.Panel;
// import javax.swing.RadioButton;
// import javax.swing.ScrollPane;
// import javax.swing.TextArea;
// import javax.swing.TextField;
// import javax.swing.event.EventListenerList;

import org.isf.agetype.manager.AgeTypeBrowserManager;
import org.isf.agetype.model.AgeType;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.generaldata.SmsParameters;
import org.isf.patient.manager.PatientBrowserManager;
import org.isf.patient.model.Patient;
import org.isf.admission.gui.AdmittedPatientBrowser;
import org.isf.video.gui.PatientPhotoPanel;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import com.toedter.calendar.JDateChooser;

import org.isf.utils.jobjects.BusyState;
import org.isf.utils.jobjects.ModalWindow;
import org.isf.utils.Logging;

import java.io.File;
import com.vaadin.server.FileResource;
import com.vaadin.server.VaadinService;

import com.vaadin.ui.Button;
import com.vaadin.ui.DateField;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Grid;
import com.vaadin.ui.RadioButtonGroup;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.event.FieldEvents.FocusEvent;
import de.steinwedel.messagebox.MessageBox;
import com.vaadin.ui.Component.Listener;

/*------------------------------------------
 * PatientInsertExtended - model for the patient entry
 * -----------------------------------------
 * modification history
 * 11/08/2008 - alessandro - added mother and father names textfield
 * 11/08/2008 - alessandro - changed economicStatut -> hasInsurance
 * 19/08/2008 - mex        - changed educational level with blood type
 * 26/08/2008 - cla		   - added calendar for calculating age
 * 						   - modified age field from int to varchar
 * 28/08/2008 - cla		   - added tooltip for age field and cheching name and age for patient editing
 * 05/09/2008 - alex       - added patient code
 * 01/01/2009 - Fabrizio   - modified assignment to age field to set an int value
 *------------------------------------------*/


public class PatientInsertExtended extends ModalWindow{

	private static final long serialVersionUID = -827831581202765055L;

	// private EventListenerList patientListeners = new EventListenerList();
	private List<PatientListener> patientListeners;
	
	public interface PatientListener{
		public void patientUpdated(Patient aPatient);

		public void patientInserted(Patient aPatient);
	}

	public void addPatientListener(PatientListener l){
		if(patientListeners==null)
			patientListeners = new ArrayList<PatientListener>();
		patientListeners.add(l);
	}

	public void removePatientListener(PatientListener l){
		if(patientListeners==null){
			patientListeners = new ArrayList<PatientListener>();
			return;
		}
		patientListeners.remove(l);
	}

	private void firePatientInserted(Patient aPatient){
		if(patientListeners != null){
			for(PatientListener patientListener : patientListeners){
				patientListener.patientInserted(aPatient);
			}
		}
	}

	private void firePatientUpdated(Patient aPatient){
		if(patientListeners != null){
			for(PatientListener patientListener : patientListeners){
				patientListener.patientUpdated(aPatient);
			}
		}
	}

	// COMPONENTS: Main
	private VerticalLayout windowContent;
	private Panel jMainPanel = null;
	private boolean insert;
	private boolean justSave;
	final private Patient patient;
	private int lock;
	private PatientBrowserManager manager = new PatientBrowserManager();
	private String resPath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();

	// COMPONENTS: Data
	private Panel jDataPanel = null;

	// COMPONENTS: Anagraph
	private Panel jDataContainPanel = null;
	private Panel jAnagraphPanel = null;

	// First Name Components:
	private TextField jFirstNameTextField = null;

	// Second Name Components:
	private Panel jSecondName = null;
	private Panel jSecondNameLabelPanel = null;
	private Panel jSecondNameFieldPanel = null;
	private Label jSecondNameLabel = null;
	private TextField jSecondNameTextField = null;

	// AgeTypeSelection:
	private Panel jAgeType = null;
	private RadioButtonGroup<String> jAgeTypeButtonGroup = null;
	private HorizontalLayout jAgeTypeSelection = null;
	// private ButtonGroup ageTypeGroup = null;
	// private Panel jAgeType_BirthDatePanel = null;
	// private RadioButton jAgeType_Age = null;
	// private RadioButton jAgeType_BirthDate = null;
	// private RadioButton jAgeType_Description = null;

	// Age Components:
	private HorizontalLayout jAge = null;
	private TextField jAgeYears = null;
	private TextField jAgeMonths = null;
	private TextField jAgeDays = null;
	private int years;
	private int months;
	private int days;

	// BirthDate Components:
	private DateField birthDateField = null;
	private HorizontalLayout birthDateLayuot = null;
	private Panel birthDateLayuotLabelPanel = null;
	private Label birthDateLayuotLabel = null;
	private Panel birthDateLayuotGroupPanel = null;
	private Calendar cBirthDate = null;
	private Button birthDateReset = null;
	private Label birthDateLayuotAge = null;

	// AgeDescription Components:
	private int ageType;
	private int ageTypeMonths;
	private Panel ageDesc = null;
	private HorizontalLayout descAgeLayout = null;
	private HorizontalLayout ageMonthsLayout = null;
	private ComboBox ageDescComboBox = null;
	private ComboBox ageMonthsComboBox = null;
	private Label ageMonthsLabel = null;

	// Sex Components:
	private FormLayout jSexPanel = null;
	private RadioButtonGroup sexGroup = null;
	private HorizontalLayout jSexLabelPanel = null;
	private Label jSexLabel = null;
	// private RadioButton radiof = null;
	// private RadioButton radiom = null;

	// Address Components:
	private FormLayout jAddress = null;
	private HorizontalLayout jAddressLabelPanel = null;
	private HorizontalLayout jAddressFieldPanel = null;
	private Label jAddressLabel = null;
	private TextField jAddressTextField = null;

	// Address Components:
	private Panel jTaxCodePanel = null;
	private Panel jTaxCodeLabelPanel = null;
	private Panel jTaxCodeFieldPanel = null;
	private Label jTaxCodeLabel = null;
	private TextField jTaxCodeTextField = null;

	// City Components:
	private FormLayout jCity = null;
	private Panel jCityLabelPanel = null;
	private Panel jCityFieldPanel = null;
	private Label jCityLabel = null;
	private TextField jCityTextField = null;

	// NextKin Components:
	private Panel jNextKin = null;
	private Panel jNextKinLabelPanel = null;
	private Panel jNextKinFieldPanel = null;
	private Label jNextKinLabel = null;
	private TextField jNextKinTextField = null;

	// Telephone Components:
	private Panel jTelephone = null;
	private Panel jTelephoneLabelPanel = null;
	private Panel jTelephoneFieldPanel = null;
	private Label jTelephoneLabel = null;
	private TextField jTelephoneTextField = null;

	// COMPONENTS: Extension
	private VerticalLayout jExtensionContent = null;

	// BloodType Components:
	private Panel jBloodTypePanel = null;
	private ComboBox jBloodTypeComboBox = null;

	// // Father Components:
	// private Panel jFatherPanelOptions;
	private Panel jFatherPanel = null; // added
	private VerticalLayout jFatherLayout = null; // added
	private TextField jFatherNameTextField = null; // added
	private RadioButtonGroup fatherGroup = null;

	// Mother Components:
	private Panel jMotherOptions;
	private Panel jMotherPanel = null; // added
	private VerticalLayout jMotherNameLayout = null; // added
	private TextField jMotherNameTextField = null; // added
	private RadioButtonGroup motherGroup = null;

	// ParentTogether Components:
	private Panel jParentPanel = null;
	private RadioButtonGroup parentGroup = null;

	// private ButtonGroup eduLevelGroup=null; //removed

	// HasInsurance Components:
	private Panel jInsurancePanel = null;
	private RadioButtonGroup insuranceGroup = null;

	// COMPONENTS: Note
	private VerticalLayout jRightLayout = null;
	private Panel jNotePanel = null;
	private TextArea jNoteTextArea = null;

	// COMPONENTS: Buttons
	private HorizontalLayout ButtonLayout = null;
	private Button jOkButton = null;
	private Button jCancelButton = null;

	private Label labelRequiredFields;
	private Logging logger;
	
	private PatientPhotoPanel photoPanel;
	

	/**
	 * This method initializes
	 * @param owner 
	 * 
	 */
	private AdmittedPatientBrowser browser;
	public PatientInsertExtended(Patient old, boolean inserting){
		logger = new Logging();
		showAsModal();
		this.windowContent = new VerticalLayout();
        this.setContent(this.windowContent);
        UI.getCurrent().addWindow(this);
		patient = old;
		insert = inserting;

		if (!insert){
			lock = patient.getLock();
		}

		initialize(this);
	}

	public PatientInsertExtended(Patient old, boolean inserting, AdmittedPatientBrowser browser){//change argument browser to dinamically type one
		this(old, inserting);
		this.browser = browser;
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize(Window window){
		getWindowContent();
		if (insert)
			window.setCaption(MessageBundle.getMessage("angal.patient.title"));
		else
			window.setCaption(MessageBundle.getMessage("angal.patient.titleedit"));
		// this.setSize(new Dimension(604, 445));
		// pack();
		// setResizable(false);
		// setLocationRelativeTo(null);
	}

	/**
	 * This method initializes jContainPanel
	 * 
	 * @return javax.swing.Panel
	 */
	private void getWindowContent(){
		getDataLayout();
		getButtonLayout();
	}

	/**
	 * This method initializes jMainPanel
	 * 
	 * @return javax.swing.Panel
	 */
	private void getDataLayout(){
		HorizontalLayout topWindow = new HorizontalLayout();
		topWindow.setMargin(false);
		windowContent.addComponent(topWindow);
		getJDataContainPanel(topWindow);//qqq
		getjRightLayout(topWindow);
	}

	/**
	 * This method initializes ButtonLayout
	 * 
	 * @return javax.swing.Panel
	 */
	private void getButtonLayout(){
		if (ButtonLayout == null){
			ButtonLayout = new HorizontalLayout();
			ButtonLayout.addComponent(getOkButton());
			ButtonLayout.addComponent(getJCancelButton());
		}
		windowContent.addComponent(ButtonLayout);
	}

	/**
	 * This method initializes jOkButton
	 * 
	 * @return javax.swing.Button
	 */
	private void isPatientPresentYes(){
		boolean result = false;
		String firstName = jFirstNameTextField.getValue().trim();
		String secondName = jSecondNameTextField.getValue().trim();
		patient.setFirstName(firstName);
		patient.setSecondName(secondName);
		if (sexGroup.getValue()==MessageBundle.getMessage("angal.patient.female")){
			patient.setSex('F');
		} else if (sexGroup.getValue()==MessageBundle.getMessage("angal.patient.male")){
			patient.setSex('M');
		} else {
			MessageBox.createInfo().withCaption("Message").withMessage("Please select a sex").withOkButton().open();
			return;
		}
		patient.setTaxCode(jTaxCodeTextField.getValue().trim());
		patient.setAddress(jAddressTextField.getValue().trim());
		patient.setCity(jCityTextField.getValue().trim());
		patient.setNextKin(jNextKinTextField.getValue().trim());
		patient.setTelephone(jTelephoneTextField.getValue().replaceAll(" ", ""));
		patient.setMother_name(jMotherNameTextField.getValue().trim());//insert
		if (motherGroup.getValue()==MessageBundle.getMessage("angal.patient.alive")){
			patient.setMother('A');
		} else {
			if (motherGroup.getValue()==MessageBundle.getMessage("angal.patient.dead")){
				patient.setMother('D');
			} else
				patient.setMother('U');
		}
		patient.setFather_name(jFatherNameTextField.getValue().trim());
		if (fatherGroup.getValue()==MessageBundle.getMessage("angal.patient.alive")){
			patient.setFather('A');
		} else {
			if (fatherGroup.getValue()==MessageBundle.getMessage("angal.patient.dead")){
				patient.setFather('D');
			} else
				patient.setFather('U');
		}
		patient.setBloodType(jBloodTypeComboBox.getValue().toString());
		if (insuranceGroup.getValue()==MessageBundle.getMessage("angal.patient.yes")){
			patient.setHasInsurance('Y');
		} else {
			if (insuranceGroup.getValue()==MessageBundle.getMessage("angal.patient.no")){
				patient.setHasInsurance('N');
			} else
				patient.setHasInsurance('U');
		}

		if (parentGroup.getValue()==MessageBundle.getMessage("angal.patient.yes")){
			patient.setParentTogether('Y');
		} else {
			if (parentGroup.getValue()==MessageBundle.getMessage("angal.patient.no")){
				patient.setParentTogether('N');
			} else
				patient.setParentTogether('U');
		}
		patient.setNote(jNoteTextArea.getValue().trim());
		result = manager.newPatient(patient);

		if (!result)
			MessageBox.createInfo().withCaption("Message").withMessage(MessageBundle.getMessage("angal.patient.thedatacouldnotbesaved")).withOkButton().open();
		else {
			if (justSave){
				insert = false;
				justSave = false;
				// PatientInsertExtended.this.requestFocus();
			} else {
				//qqqw
				this.close();
				firePatientInserted(patient);
			}
		}
	}

	boolean ok;
	private Button getOkButton(){
		if (jOkButton == null){
			jOkButton = new Button();
			jOkButton.setCaption(MessageBundle.getMessage("angal.common.ok"));
			////jOkButton.setClickShortcut(KeyEvent.VK_A + ('O' - 'A'));
			jOkButton.addClickListener(e -> {
				String firstName = jFirstNameTextField.getValue().trim();
				String secondName = jSecondNameTextField.getValue().trim();
				if (firstName.equals("")){
					MessageBox.createInfo().withCaption("Message").withMessage(MessageBundle.getMessage("angal.patient.insertfirstname")).withOkButton().open();
					return;
				}
				if (secondName.equals("")){
					MessageBox.createInfo().withCaption("Message").withMessage(MessageBundle.getMessage("angal.patient.insertsecondname")).withOkButton().open();
					return;
				}
				checkAge();
			});

		}
		return jOkButton;
	}

	/**
	 * This method checks Age insertion
	 * 
	 * @return javax.swing.Button
	 */
	private void ageFalse(){
		MessageBox.createInfo().withCaption("Message").withMessage(MessageBundle.getMessage("angal.patient.insertage")).withOkButton().open();
	}
	private void ageChecked(){
		String firstName = jFirstNameTextField.getValue().trim();
		String secondName = jSecondNameTextField.getValue().trim();
		patient.setAge(years);
		patient.setBirthDate(bbdate.toDate());
		patient.setAgetype("");
		if (insert){
			String name = firstName + " " + secondName;
			if(manager.isPatientPresent(name)){
				MessageBox.createQuestion().withCaption(MessageBundle.getMessage("angal.patient.select"))
				.withMessage(MessageBundle.getMessage("angal.patient.thepatientisalreadypresent") + ".\n" + MessageBundle.getMessage("angal.patient.doyouwanttocontinue") + "?")
				.withYesButton(()-> {
					isPatientPresentYes();
				})
				.withNoButton(()-> {
					return;
				}).open();
			}else{
				isPatientPresentYes();
			}
		} else { //update
			patient.setFirstName(firstName);
			patient.setSecondName(secondName);
			if (sexGroup.getValue()==MessageBundle.getMessage("angal.patient.female")){
				patient.setSex('F');
			} else if (sexGroup.getValue()==MessageBundle.getMessage("angal.patient.male")){
				patient.setSex('M');
			} else {
				MessageBox.createInfo().withCaption("Message").withMessage("Please select a sex").withOkButton().open();
				return;
			}
			patient.setTaxCode(jTaxCodeTextField.getValue().trim());
			patient.setAddress(jAddressTextField.getValue().trim());
			patient.setCity(jCityTextField.getValue().trim());
			patient.setNextKin(jNextKinTextField.getValue().trim());
			patient.setTelephone(jTelephoneTextField.getValue().replaceAll(" ", ""));
			patient.setMother_name(jMotherNameTextField.getValue().trim());
			if (motherGroup.getValue()==MessageBundle.getMessage("angal.patient.alive")){
				patient.setMother('A');
			} else {
				if (motherGroup.getValue()==MessageBundle.getMessage("angal.patient.dead")){
					patient.setMother('D');
				} else
					patient.setMother('U');
			}
			patient.setFather_name(jFatherNameTextField.getValue().trim());
			if (fatherGroup.getValue()==MessageBundle.getMessage("angal.patient.alive")){
				patient.setFather('A');
			} else {
				if (fatherGroup.getValue()==MessageBundle.getMessage("angal.patient.dead")){
					patient.setFather('D');
				} else
					patient.setFather('U');
			}
			patient.setBloodType(jBloodTypeComboBox.getValue().toString());
			if (insuranceGroup.getValue()==MessageBundle.getMessage("angal.patient.yes")){
				patient.setHasInsurance('Y');
			} else {
				if (insuranceGroup.getValue()==MessageBundle.getMessage("angal.patient.no")){
					patient.setHasInsurance('N');
				} else
					patient.setHasInsurance('U');
			}

			if (parentGroup.getValue()==MessageBundle.getMessage("angal.patient.yes")){
				patient.setParentTogether('Y');
			} else {
				if (parentGroup.getValue()==MessageBundle.getMessage("angal.patient.no")){
					patient.setParentTogether('N');
				} else
					patient.setParentTogether('U');
			}
			boolean result = false;
			patient.setNote(jNoteTextArea.getValue().trim());
			result = manager.updatePatient(patient);
			if (!result)
				MessageBox.createInfo().withCaption("Message").withMessage(MessageBundle.getMessage("angal.patient.thedatacouldnotbesaved")).withOkButton().open();
			else {
				this.close();
				firePatientUpdated(patient);
			}
		}
	}
	DateTime bbdate = new DateTime();
	private void checkAge(){
		if (jAgeTypeButtonGroup.getValue()==MessageBundle.getMessage("angal.patient.modeage")){
			try {
				years = Integer.parseInt(jAgeYears.getValue());
				months = Integer.parseInt(jAgeMonths.getValue());
				days = Integer.parseInt(jAgeDays.getValue());
				if (years == 0 && months == 0 && days == 0) throw new NumberFormatException();
				bbdate = bbdate.minusYears(years).minusMonths(months).minusDays(days);
			} catch (NumberFormatException ex1){
				MessageBox.createInfo().withCaption("Message").withMessage(MessageBundle.getMessage("angal.patient.insertvalidage")).withOkButton().open();
				ageFalse();
			}
			if (years < 0 || years > 200)
				ageFalse();
			else if (years > 100){
				MessageBox.createQuestion().withCaption(MessageBundle.getMessage("angal.patient.veryoldpatient"))
				.withMessage(MessageBundle.getMessage("angal.patient.confirmage"))
				.withYesButton(()-> {
					ageChecked();
				})
				.withNoButton(()-> {
					ageFalse();
				}).open();
			}else
				ageChecked();
		} else if (jAgeTypeButtonGroup.getValue()==MessageBundle.getMessage("angal.patient.modedescription")){
			AgeTypeBrowserManager at = new AgeTypeBrowserManager();
			int index = 0;
			ArrayList<AgeType> ageList = at.getAgeType();
			int i=0;
			if(ageDescComboBox.getSelectedItem().isPresent()){
				String selectedAge = (String) ageDescComboBox.getSelectedItem().get();
				for (AgeType ag : ageList){
					i+=1;
					if(selectedAge.equals(MessageBundle.getMessage(ag.getDescription()))){
						index=i;
						break;
					}
				}
			}

			AgeType ageType = null;
			
			if (index > 0){
				ageType = at.getTypeByCode(index);
			} else
				ageFalse();

			years = ageType.getFrom();
			if (index == 1){
				String[] monthsArray = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" };
				i=0;
				String selectedMonth = (String) ageDescComboBox.getSelectedItem().get();
				for(String month : monthsArray){
					i+=1;
					if(selectedMonth.equals(month)){
						months=i;
						break;
					}
				}
				patient.setAgetype(ageType.getCode() + "/" + months);
				bbdate = bbdate.minusYears(years).minusMonths(months);
			} else {
				bbdate = bbdate.minusYears(years);
			}
			ageChecked();
		} else{
			if (cBirthDate == null) ageFalse();
			else {
				bbdate = new DateTime(cBirthDate);
				calcAge(bbdate);
				ageChecked();
			}
		}
	}

	/**
	 * This method initializes jCancelButton
	 * 
	 * @return javax.swing.Button
	 */
	private Button getJCancelButton(){
		if (jCancelButton == null){
			jCancelButton = new Button();
			jCancelButton.setCaption("Cancel");
			////jCancelButton.setClickShortcut(KeyEvent.VK_A + ('C' - 'A'));
			jCancelButton.addClickListener(e->{
				close();
			});
		}
		return jCancelButton;
	}

	/**
	 * This method initializes birthDateLayuot
	 * 
	 * @return javax.swing.Panel
	 */
	private HorizontalLayout getBirthDate(){
		if (birthDateLayuot == null){
			birthDateLayuot = new HorizontalLayout();
			birthDateLayuot.addComponent(getBirthDateLabel());
			birthDateLayuot.addComponent(getBirthDateField());
			birthDateLayuot.addComponent(getBirthDateReset());
		}
		return birthDateLayuot;
	}

	private Label getBirthDateAge(){
		if (birthDateLayuotAge == null){
			birthDateLayuotAge = new Label(" ");
		}
		return birthDateLayuotAge;
	}
	
	private String formatYearsMonthsDays(int years, int months, int days){
		return years+"y "+months+"m "+days+"d";
	}

	/**
	 * This method initializes birthDateLayuotLabel
	 * 
	 * @return javax.swing.Label
	 */
	private Label getBirthDateLabel(){
		if (birthDateLayuotLabel == null){
			birthDateLayuotLabel = new Label();
			birthDateLayuotLabel.setValue(MessageBundle.getMessage("angal.patient.birthdate"));
		}
		return birthDateLayuotLabel;
	}

	/**
	 * This method initializes birthDateLayuotGroupPanel
	 * 
	 * @return javax.swing.Panel
	 */

	private LocalDate dateToLocalDate(Date date){
		LocalDate ldate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		return ldate;
	}

	private Date localDateToDate(LocalDate ldate){
		Date date = Date.from(ldate.atStartOfDay(ZoneId.systemDefault()).toInstant());
		return date;
	}

	private Button getBirthDateReset(){
		if (birthDateReset == null){
			birthDateReset = new Button();
			birthDateReset.setIcon(new FileResource(new File(resPath +"/WEB-INF/icons/trash_button.png")));
			// birthDateReset.setPreferredSize(new Dimension(20, 20));
			birthDateReset.addClickListener(e->{
				birthDateField.setValue(null);
				cBirthDate = null;
			});
		}
		return birthDateReset;
	}

	private DateField getBirthDateField(){
		if (!insert){//qqqedit
			Date sBirthDate = patient.getBirthDate();
			if (sBirthDate != null){
				cBirthDate = Calendar.getInstance();
				cBirthDate.setTimeInMillis(sBirthDate.getTime());
			}
		}
		birthDateField = new DateField();
		birthDateField.setLocale(new Locale(GeneralData.LANGUAGE));
		birthDateField.setDateFormat("dd/MM/yyyy");
		birthDateField.addValueChangeListener(e->{
			cBirthDate = Calendar.getInstance();
			if(e.getValue()!=null)
				cBirthDate.setTime(localDateToDate(e.getValue()));
			DateTime bdate = new DateTime(cBirthDate);
			if (bdate.isAfter(new DateTime())) birthDateField.setValue(LocalDate.now());
			else calcAge(bdate);
		});
		if (cBirthDate != null){
			birthDateField.setValue(dateToLocalDate(cBirthDate.getTime()));
		}

		return birthDateField;
	}

	private void calcAge(DateTime bdate){
		Period p = new Period(bdate, new DateTime(), PeriodType.yearMonthDay());
		years = p.getYears();
		months = p.getMonths();
		days = p.getDays();
		getBirthDateAge();
		birthDateLayuotAge.setCaption(formatYearsMonthsDays(years, months, days));
	}
	
	/**
	 * This method initializes birthDateLayuotLabelPanel
	 * 
	 * @return javax.swing.Panel
	 */

	/**
	 * This method initializes jFirstNameTextField
	 * 
	 * @return javax.swing.TextField
	 */
	private TextField getJFirstNameTextField(){
		if (jFirstNameTextField == null){
			jFirstNameTextField = new TextField();
			jFirstNameTextField.setWidth("15em");
			jFirstNameTextField.setCaption(MessageBundle.getMessage("angal.patient.firstname"));
			if (!insert)
				jFirstNameTextField.setValue(patient.getFirstName());
		}
		return jFirstNameTextField;
	}

	/**
	 * This method initializes jSecondNameTextField
	 * 
	 * @return javax.swing.TextField
	 */
	private TextField getJSecondNameTextField(){
		if (jSecondNameTextField == null){
			jSecondNameTextField = new TextField();
			jSecondNameTextField.setWidth("15em");
			jSecondNameTextField.setCaption(MessageBundle.getMessage("angal.patient.secondname"));
			if (!insert)
				jSecondNameTextField.setValue(patient.getSecondName());

		}
		return jSecondNameTextField;
	}

	/**
	 * This method initializes jSexPanel
	 * 
	 * @return javax.swing.Panel
	 */
	private RadioButtonGroup getSexPanel(){
		// if (jSexPanel == null){
			// jSexPanel = new FormLayout();
			sexGroup = new RadioButtonGroup<>();
			sexGroup.setCaption(MessageBundle.getMessage("angal.patient.sexstar"));
			sexGroup.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
			sexGroup.setItems(MessageBundle.getMessage("angal.patient.male"),MessageBundle.getMessage("angal.patient.female"));
		// 	////radiom.setClickShortcut(KeyEvent.VK_A + ('M' - 'A'));//unimplemented
		// 	////radiof.setClickShortcut(KeyEvent.VK_A + ('F' - 'A'));
			// jSexPanel.addComponent(getJSexLabelPanel());
			if (!insert){
				if (patient.getSex() == 'F')
					sexGroup.setValue(MessageBundle.getMessage("angal.patient.female"));
				else
					sexGroup.setValue(MessageBundle.getMessage("angal.patient.male"));
			}
			// jSexPanel.addComponent(sexGroup);
		// 	sexGroup.addComponent(radiom);
		// 	sexGroup.addComponent(radiof);
		// 	jSexPanel.addComponent(radiof);

		// }
		return sexGroup;
	}

	/**
	 * This method initializes jAdressPanel
	 * 
	 * @return javax.swing.Panel
	 */
	private Label getJAddressLabelPanel(){
		// if (jAddressLabelPanel == null){
			jAddressLabel = new Label(MessageBundle.getMessage("angal.patient.address"));
			// jAddressLabel.setCaption();
			// jAddressLabelPanel = new Panel();
			// jAddressLabelPanel.addComponent(jAddressLabel, BorderLayout.EAST);
		// }
		return jAddressLabel;
	}

	/**
	 * This method initializes jAdressTextField
	 * 
	 * @return javax.swing.TextField
	 */
	private TextField getJAddressTextField(){
		if (jAddressTextField == null){
			jAddressTextField = new TextField();
			jAddressTextField.setWidth("15em");
			jAddressTextField.setCaption(MessageBundle.getMessage("angal.patient.address"));
			if (!insert)
				jAddressTextField.setValue(patient.getAddress());
		}
		return jAddressTextField;
	}

	/**
	 * This method initializes jTaxCodeTextField
	 * 
	 * @return javax.swing.TextField
	 */
	private TextField getJTaxCodeTextField(){
		if (jTaxCodeTextField == null){
			jTaxCodeTextField = new TextField();
			jTaxCodeTextField.setWidth("15em");
			jTaxCodeTextField.setCaption(MessageBundle.getMessage("angal.patient.taxcode"));
			if (!insert)
				jTaxCodeTextField.setValue(patient.getTaxCode());
		}
		return jTaxCodeTextField;
	}

	/**
	 * This method initializes jCityLabelPanel
	 * 
	 * @return javax.swing.Panel
	 */
	private Label getJCityLabelPanel(){
		// if (jCityLabelPanel == null){
			jCityLabel = new Label(MessageBundle.getMessage("angal.patient.city"));
			// jCityLabelPanel = new Panel();
			// jCityLabelPanel.addComponent(jCityLabel, BorderLayout.EAST);
		// }
		return jCityLabel;
	}

	/**
	 * This method initializes jCityTextField
	 * 
	 * @return javax.swing.TextField
	 */
	private TextField getJCityTextField(){
		if (jCityTextField == null){
			jCityTextField = new TextField();
			jCityTextField.setWidth("15em");
			jCityTextField.setCaption(MessageBundle.getMessage("angal.patient.city"));
			if (!insert)
				jCityTextField.setValue(patient.getCity());
		}
		return jCityTextField;
	}

	/**
	 * This method initializes jTelPanel
	 * 
	 * @return javax.swing.Panel
	 */
	private Panel getJTelPanel(){
		if (jTelephoneLabelPanel == null){
			jTelephoneLabel = new Label();
			jTelephoneLabel.setCaption(MessageBundle.getMessage("angal.patient.telephone"));
			jTelephoneLabelPanel = new Panel();
			// jTelephoneLabelPanel.addComponent(jTelephoneLabel, BorderLayout.EAST);
		}
		return jTelephoneLabelPanel;
	}

	/**
	 * This method initializes jTelephoneTextField
	 * 
	 * @return javax.swing.TextField
	 */
	private TextField getJTelephoneTextField(){
		SmsParameters.getSmsParameters();
		if (jTelephoneTextField == null){
			jTelephoneTextField = new TextField();
			jTelephoneTextField.setWidth("15em");
			jTelephoneTextField.setValue(SmsParameters.ICC);
			jTelephoneTextField.setCaption(MessageBundle.getMessage("angal.patient.telephone"));
			if (!insert)
				jTelephoneTextField.setValue(patient.getTelephone());
		}
		return jTelephoneTextField;
	}

	/**
	 * This method initializes jNextKinLabelPanel
	 * 
	 * @return javax.swing.Panel
	 */
	private Panel getJNextKinLabelPanel(){
		if (jNextKinLabelPanel == null){
			jNextKinLabel = new Label();
			jNextKinLabel.setCaption(MessageBundle.getMessage("angal.patient.nextkin"));
			jNextKinLabelPanel = new Panel();
			// jNextKinLabelPanel.addComponent(jNextKinLabel, BorderLayout.EAST);
		}
		return jNextKinLabelPanel;
	}

	/**
	 * This method initializes jNextKinTextField
	 * 
	 * @return javax.swing.TextField
	 */
	private TextField getJNextKinTextField(){
		if (jNextKinTextField == null){
			jNextKinTextField = new TextField();
			jNextKinTextField.setCaption(MessageBundle.getMessage("angal.patient.nextkin"));
			jNextKinTextField.setWidth("15em");
			if (!insert)
				jNextKinTextField.setValue(patient.getNextKin());
		}
		return jNextKinTextField;
	}

	/**
	 * This method initializes jBloodTypePanel
	 * 
	 * @return javax.swing.Panel
	 */
	private Panel getJBloodTypePanel(){
		if (jBloodTypePanel == null){
			jBloodTypePanel = new Panel();
			jBloodTypePanel.setCaption(MessageBundle.getMessage("angal.patient.bloodtype"));
			String[] bloodTypes = { MessageBundle.getMessage("angal.patient.bloodtype.unknown"), "0+", "A+", "B+", "AB+", "0-", "A-", "B-", "AB-" };
			jBloodTypeComboBox = new ComboBox();
			jBloodTypeComboBox.setEmptySelectionAllowed(false);
			jBloodTypeComboBox.setItems(bloodTypes);
			jBloodTypeComboBox.setSelectedItem(MessageBundle.getMessage("angal.patient.bloodtype.unknown"));
			jBloodTypePanel.setContent(jBloodTypeComboBox);

			if (!insert){
				jBloodTypeComboBox.setSelectedItem(patient.getBloodType());
			} 
		}
		return jBloodTypePanel;
	}

	/**
	 * This method initializes jAnagraphPanel
	 * 
	 * @return javax.swing.Panel
	 */
	private Panel getJAnagraphPanel(){
		if (jAnagraphPanel == null){
			jAnagraphPanel = new Panel();
			FormLayout form1 = new FormLayout();
			FormLayout form2 = new FormLayout();
			form1.setMargin(false);
			form2.setMargin(false);
			form1.addComponent(getJFirstNameTextField());
			form1.addComponent(getJSecondNameTextField());
			form1.addComponent(getJTaxCodeTextField());
			form2.addComponent(getSexPanel());
			form2.addComponent(getJAddressPanel());
			form2.addComponent(getJCity());
			form2.addComponent(getJNextKin());
			form2.addComponent(getJTelephone());
			form2.addComponent(getLabelRequiredFields());

			VerticalLayout vLayout = new VerticalLayout();
			vLayout.setMargin(false);
			vLayout.addComponent(form1);
			vLayout.addComponent(getJAgeType());
			vLayout.addComponent(form2);
			jAnagraphPanel.setContent(vLayout);
		}
		return jAnagraphPanel;
	}
	
	private Label getLabelRequiredFields(){
		if (labelRequiredFields == null){
			labelRequiredFields = new Label(MessageBundle.getMessage("angal.patient.indicatesrequiredfields"));
			// labelRequiredFields.setAlignmentX(CENTER_ALIGNMENT);
		}
		return labelRequiredFields;
	}

	/**
	 * This method initializes jSexLabelPanel
	 * 
	 * @return javax.swing.Panel
	 */
	private Label getJSexLabelPanel(){
		if (jSexLabel == null){
			jSexLabel = new Label();
			jSexLabel.setCaption(MessageBundle.getMessage("angal.patient.sexstar"));
		}
		return jSexLabel;
	}

	/**
	 * This method initializes jAgeType
	 * 
	 * @return javax.swing.Panel
	 */
	private Panel getJAgeType(){///fix
		if (jAgeType == null){
			jAgeType = new Panel();
			jAgeType.setCaption(MessageBundle.getMessage("angal.patient.agestar"));
			VerticalLayout layout = new VerticalLayout();
			jAgeType.setContent(layout);
			layout.addComponent(getJAgeTypeButtonGroup(layout));//, BorderLayout.NORTH);
			layout.addComponent(getJAgeTypeSelection());//, BorderLayout.CENTER);
		// 	layout.setPreferredSize(new Dimension(100,100));
		}
		return jAgeType;
	}

	/**
	 * This method initializes jAgeTypeButtonGroup
	 * 
	 * @return javax.swing.Panel
	 */
	private RadioButtonGroup getJAgeTypeButtonGroup(VerticalLayout layout){//qqq
		if (jAgeTypeButtonGroup == null){
			jAgeTypeButtonGroup = new RadioButtonGroup<>();
			jAgeTypeButtonGroup.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
			jAgeTypeButtonGroup.setItems(MessageBundle.getMessage("angal.patient.modeage"), MessageBundle.getMessage("angal.patient.modebdate"), MessageBundle.getMessage("angal.patient.modedescription"));
			jAgeTypeButtonGroup.addValueChangeListener(e -> {
				if(e.getOldValue()==MessageBundle.getMessage("angal.patient.modeage")){
					layout.removeComponent(getJAge());
				}
				else if(e.getOldValue()==MessageBundle.getMessage("angal.patient.modedescription")){
					layout.removeComponent(getDescAgeLayout());
				}
				else{
					layout.removeComponent(getBirthDate());
				}
				if(e.getValue()==MessageBundle.getMessage("angal.patient.modeage")){
					layout.addComponent(getJAge());
				}
				else if(e.getValue()==MessageBundle.getMessage("angal.patient.modedescription")){
					layout.addComponent(getDescAgeLayout());
				}
				else{
					layout.addComponent(getBirthDate());
				}
			});
		
			if (!insert){//qqqedit
				if (patient.getBirthDate() != null){
					jAgeTypeButtonGroup.setValue(MessageBundle.getMessage("angal.patient.modebdate"));
					calcAge(new DateTime(patient.getBirthDate()));
				} else if (patient.getAgetype() != null && patient.getAgetype().compareTo("") != 0){
					parseAgeType();
					jAgeTypeButtonGroup.setValue(MessageBundle.getMessage("angal.patient.modedescription"));
				} else {
					jAgeTypeButtonGroup.setValue(MessageBundle.getMessage("angal.patient.modeage"));
					years = patient.getAge();
				}
			} else {
				jAgeTypeButtonGroup.setValue(MessageBundle.getMessage("angal.patient.modeage"));
			}
		}
		return jAgeTypeButtonGroup;
	}

	/**
	 * This method initializes ageType & ageTypeMonths
	 * 
	 * @return javax.swing.Panel
	 */
	private void parseAgeType(){

		if (patient.getAgetype().compareTo("") != 0){
			StringTokenizer token = new StringTokenizer(patient.getAgetype(), "/");
			String token1 = token.nextToken();
			String t1 = token1.substring(1, 2);
			ageType = Integer.parseInt(t1);

			if (token.hasMoreTokens()){

				String token2 = token.nextToken();
				int t2 = Integer.parseInt(token2);
				ageTypeMonths = t2;
			} else
				ageTypeMonths = 0;
		} else {
			ageType = -1;
		}
	}

	/**
	 * This method initializes jAgeTypeSelection
	 * 
	 * @return javax.swing.Panel
	 */
	private HorizontalLayout getJAgeTypeSelection(){
		if (jAgeTypeButtonGroup.getValue()==MessageBundle.getMessage("angal.patient.modeage"))
			jAgeTypeSelection = getJAge();
		else if (jAgeTypeButtonGroup.getValue()==MessageBundle.getMessage("angal.patient.modedescription"))
			jAgeTypeSelection = getDescAgeLayout();
		else
			jAgeTypeSelection = getBirthDate();
		return jAgeTypeSelection;
	}

	/**
	 * This method initializes ageDesc
	 * 
	 * @return javax.swing.Panel
	 */

	/**
	 * This method initializes ageMonthsLayout
	 * 
	 * @return javax.swing.Panel
	 */
	private HorizontalLayout getAgeMonthsLayout(){
		if (ageMonthsLayout == null){
			ageMonthsLayout = new HorizontalLayout();
			ageMonthsLabel = new Label("months");

			String[] months = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" };
			ageMonthsComboBox = new ComboBox();
			ageMonthsComboBox.setEmptySelectionAllowed(false);
			ageMonthsComboBox.setItems(months);
		}

		ageMonthsLayout.addComponent(ageMonthsComboBox);
		ageMonthsLayout.addComponent(ageMonthsLabel);

		if (!insert && ageType == 1){
			ageMonthsComboBox.setValue(""+ageTypeMonths);
		}
		return ageMonthsLayout;
	}

	/**
	 * This method initializes descAgeLayout
	 * 
	 * @return javax.swing.Panel
	 */
	private HorizontalLayout getDescAgeLayout(){
		if (descAgeLayout == null){
			descAgeLayout = new HorizontalLayout();

			ageDescComboBox = new ComboBox();

			AgeTypeBrowserManager at = new AgeTypeBrowserManager();
			ArrayList<AgeType> ageList = at.getAgeType();
			ArrayList ageDescList = new ArrayList();
			for (AgeType ag : ageList){
				ageDescList.add(MessageBundle.getMessage(ag.getDescription()));
			}
			ageDescComboBox.setItems(ageDescList.toArray());

			descAgeLayout.addComponent(ageDescComboBox);
			descAgeLayout.addComponent(getAgeMonthsLayout());
			ageMonthsComboBox.setEnabled(false);

			ageDescComboBox.addValueChangeListener(e->{
				if (e.getValue().equals(MessageBundle.getMessage("angal.agetype.newborn"))){
					ageMonthsComboBox.setEnabled(true);
				} else {
					ageMonthsComboBox.setEnabled(false);
				}
			});

			if (!insert){
				parseAgeType();
				ageDescComboBox.setValue(MessageBundle.getMessage(ageList.get(ageType + 1).getDescription()));
				if (ageType == 0){
					ageMonthsComboBox.setEnabled(true);
					ageMonthsComboBox.setValue(""+ageTypeMonths);
				}
			}

		}
		return descAgeLayout;
	}

	/**
	 * This method initializes jAge
	 * 
	 * @return javax.swing.Panel
	 */
	private HorizontalLayout getJAge(){
		if (jAge == null){//fix
			jAge = new HorizontalLayout();
			jAge.addComponent(new Label("Years"));
			jAge.addComponent(getJAgeFieldYears());
			jAge.addComponent(new Label("Months"));
			jAge.addComponent(getJAgeFieldMonths());
			jAge.addComponent(new Label("Days"));
			jAge.addComponent(getJAgeFieldDays());
		}
		return jAge;
	}

	/**
	 * This method initializes jAddressFieldPanel
	 * 
	 * @return javax.swing.Panel
	 */
	// private Panel getJAddressFieldPanel(){
	// 	if (jAddressFieldPanel == null){
	// 		jAddressFieldPanel = new Panel();
	// 		jAddressFieldPanel.addComponent(getJAdressTextField(), null);
	// 	}
	// 	return jAddressFieldPanel;
	// }

	private TextField getJAgeFieldYears(){
		if (jAgeYears == null){
			jAgeYears = new TextField();
			jAgeYears.setValue(""+0);
			jAgeYears.setWidth("4em");
			jAgeYears.addFocusListener(new FocusListener(){
				@Override
				public void focus(FocusEvent e){//blocking the textfield value value
					TextField thisField = (TextField) e.getSource();
					thisField.setSelection(0,thisField.getValue().length());
				}
			});
			if (!insert) jAgeYears.setValue(""+years);
		}
		return jAgeYears;
	}
	
	private TextField getJAgeFieldMonths(){
		if (jAgeMonths == null){
			jAgeMonths = new TextField();
			jAgeMonths.setValue(""+0);
			jAgeMonths.setWidth("3em");
			jAgeMonths.addFocusListener(new FocusListener(){
				@Override
				public void focus(FocusEvent e){//blocking the textfield value value
					TextField thisField = (TextField) e.getSource();
					thisField.setSelection(0,thisField.getValue().length());
				}
			});
			if (!insert) jAgeMonths.setValue(""+months); 
		}
		return jAgeMonths;
	}
	
	private TextField getJAgeFieldDays(){
		if (jAgeDays == null){
			jAgeDays = new TextField();
			jAgeDays.setValue(""+0);
			jAgeDays.setWidth("3em");
			jAgeDays.addFocusListener(new FocusListener(){
				@Override
				public void focus(FocusEvent e){//blocking the textfield value value
					TextField thisField = (TextField) e.getSource();
					thisField.setSelection(0,thisField.getValue().length());
				}
			});
			if (!insert) jAgeDays.setValue(""+days); 
		}
		return jAgeDays;
	}


	/**
	 * This method initializes jCityFieldPanel
	 * 
	 * @return javax.swing.Panel
	 */
	// private Panel getJCityFieldPanel(){
	// 	if (jCityFieldPanel == null){
	// 		jCityFieldPanel = new Panel();
	// 		jCityFieldPanel.addComponent(getJCityTextField(), null);
	// 	}
	// 	return jCityFieldPanel;
	// }

	/**
	 * This method initializes jNextKinFieldPanel
	 * 
	 * @return javax.swing.Panel
	 */
	private TextField getJNextKinFieldPanel(){
		// if (jNextKinFieldPanel == null){
		// 	jNextKinFieldPanel = new Panel();
		// 	jNextKinFieldPanel.addComponent(getJNextKinTextField(), null);
		// }
		return getJNextKinTextField();
	}

	/**
	 * This method initializes jTelephoneFieldPanel
	 * 
	 * @return javax.swing.Panel
	 */
	private TextField getJTelephoneFieldPanel(){
		// if (jTelephoneFieldPanel == null){
		// 	jTelephoneFieldPanel = new Panel();
		// 	jTelephoneFieldPanel.addComponent(getJTelephoneTextField(), null);
		// }
		return getJTelephoneTextField();
	}

	/**
	 * This method initializes jAdressPanel
	 * 
	 * @return javax.swing.Panel
	 */
	private TextField getJAddressPanel(){
		// if (jAddress == null){
			// jAddress = new FormLayout();
			// jAddress.setLayout(new BorderLayout());
			// jAddress.addComponent(getJAddressLabelPanel());
			// jAddress.addComponent(getJAddressTextField());

		// }
		return getJAddressTextField();
	}

	/**
	 * This method initializes jCity
	 * 
	 * @return javax.swing.Panel
	 */
	private TextField getJCity(){
		// if (jCity == null){
		// 	jCity = new FormLayout();
		// 	// jCity.setLayout(new BorderLayout());
		// 	jCity.addComponent(getJCityLabelPanel());
		// 	jCity.addComponent(getJCityTextField());
		// }
		return getJCityTextField();
	}

	/**
	 * This method initializes jNextKin
	 * 
	 * @return javax.swing.Panel
	 */
	private TextField getJNextKin(){
		// if (jNextKin == null){
			// jNextKin = new Panel();
			// jNextKin.setLayout(new BorderLayout());
			// jNextKin.addComponent(getJNextKinLabelPanel(), java.awt.BorderLayout.WEST);
			// jNextKin.addComponent();//, java.awt.BorderLayout.EAST);
		// }
		return getJNextKinFieldPanel();
	}

	/**
	 * This method initializes jTelephone
	 * 
	 * @return javax.swing.Panel
	 */
	private TextField getJTelephone(){
		// if (jTelephone == null){
		// 	jTelephone = new Panel();
		// 	jTelephone.setLayout(new BorderLayout());
		// 	jTelephone.addComponent(getJTelPanel(), java.awt.BorderLayout.WEST);
		// 	jTelephone.addComponent(getJTelephoneFieldPanel(), java.awt.BorderLayout.EAST);
		// }
		return getJTelephoneFieldPanel();
	}

	/**
	 * This method initializes jDataContainPanel
	 * 
	 * @return javax.swing.Panel
	 */
	private void getJDataContainPanel(HorizontalLayout layout){
		jDataContainPanel = new Panel();
		if (!insert){
			jDataContainPanel.setCaption(patient.getName() + " (" + MessageBundle.getMessage("angal.common.code") + ": " + patient.getCode() + ")");
		} else {
			int nextcode = manager.getNextPatientCode();
			patient.setCode(nextcode);
			jDataContainPanel.setCaption(MessageBundle.getMessage("angal.patient.insertdataofnewpatient"));
		}
		layout.addComponent(jDataContainPanel);
		HorizontalLayout dataWindow = new HorizontalLayout();
		dataWindow.setMargin(false);
		jDataContainPanel.setContent(dataWindow);
		dataWindow.addComponent(getJAnagraphPanel());
		dataWindow.addComponent(getJExtensionContent());
	}

	/**
	 * This method initializes jFatherPanel
	 * 
	 * @return javax.swing.Panel
	 */
	private Panel getJFatherPanel(){
		if (jFatherPanel == null){
			jFatherPanel = new Panel();
			jFatherPanel.setCaption(MessageBundle.getMessage("angal.patient.fathername"));
			jFatherLayout = new VerticalLayout();
			jFatherPanel.setContent(jFatherLayout);
			jFatherLayout.addComponent(getJFatherNameTextField());
			fatherGroup = new RadioButtonGroup();
			fatherGroup.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
			fatherGroup.setItems(MessageBundle.getMessage("angal.patient.dead"),MessageBundle.getMessage("angal.patient.alive"),MessageBundle.getMessage("angal.patient.unknown"));
			fatherGroup.setValue(MessageBundle.getMessage("angal.patient.unknown"));
			jFatherLayout.addComponent(fatherGroup);
			if (!insert){
				switch (patient.getFather()){
				case 'D':
					fatherGroup.setValue(MessageBundle.getMessage("angal.patient.dead"));
					break;
				case 'A':
					fatherGroup.setValue(MessageBundle.getMessage("angal.patient.alive"));
					break;
				default:
					break;
				}
			}

		}
		return jFatherPanel;
	}

	/**
	 * This method initializes jMotherPanel
	 * 
	 * @return javax.swing.Panel
	 */
	private Panel getJMotherPanel(){
		if (jMotherPanel == null){
			jMotherPanel = new Panel();
			jMotherPanel.setCaption(MessageBundle.getMessage("angal.patient.mothername"));
			jMotherNameLayout = new VerticalLayout();
			jMotherPanel.setContent(jMotherNameLayout);
			jMotherNameLayout.addComponent(getJMotherNameTextField());
			motherGroup = new RadioButtonGroup();
			motherGroup.setItems(MessageBundle.getMessage("angal.patient.dead"),MessageBundle.getMessage("angal.patient.alive"),MessageBundle.getMessage("angal.patient.unknown"));
			motherGroup.setValue(MessageBundle.getMessage("angal.patient.unknown"));
			motherGroup.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
			jMotherNameLayout.addComponent(motherGroup);
			if (!insert){
				switch (patient.getMother()){
				case 'D':
					motherGroup.setValue(MessageBundle.getMessage("angal.patient.dead"));
					break;
				case 'A':
					motherGroup.setValue(MessageBundle.getMessage("angal.patient.alive"));
					break;
				default:
					break;
				}
			}
		}
		return jMotherPanel;
	}

	/**
	 * This method initializes jInsurancePanel
	 * 
	 * @return javax.swing.Panel
	 */
	private Panel getJInsurancePanel(){
		if (jInsurancePanel == null){
			jInsurancePanel = new Panel(MessageBundle.getMessage("angal.patient.hasinsurance"));
			insuranceGroup = new RadioButtonGroup();
			jInsurancePanel.setContent(insuranceGroup);
			insuranceGroup.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
			insuranceGroup.setItems(MessageBundle.getMessage("angal.patient.yes"),MessageBundle.getMessage("angal.patient.no"),MessageBundle.getMessage("angal.patient.unknown"));
			insuranceGroup.setValue(MessageBundle.getMessage("angal.patient.unknown"));
			if (!insert){
				switch (patient.getHasInsurance()){
				case 'Y':
					insuranceGroup.setValue(MessageBundle.getMessage("angal.patient.yes"));
					break;
				case 'N':
					insuranceGroup.setValue(MessageBundle.getMessage("angal.patient.no"));
					break;
				default:
					break;
				}
			}
		}
		return jInsurancePanel;
	}

	/**
	 * This method initializes jInsuranceYesRadioButton
	 * 
	 * @return javax.swing.RadioButton
	 */
	// private RadioButton getJInsurance_Yes(){
	// 	if (jInsurance_Yes == null){
	// 		jInsurance_Yes = new RadioButton();
	// 		jInsurance_////Yes.setClickShortcut(KeyEvent.VK_A + ('R' - 'A'));
	// 		jInsurance_Yes.setCaption(MessageBundle.getMessage("angal.patient.hasinsuranceyes"));
	// 	}
	// 	return jInsurance_Yes;
	// }

	/**
	 * This method initializes jInsuranceNoRadioButton
	 * 
	 * @return javax.swing.RadioButton
	 */
	// private RadioButton getJInsurance_No(){
	// 	if (jInsurance_No == null){
	// 		jInsurance_No = new RadioButton();
	// 		jInsurance_////No.setClickShortcut(KeyEvent.VK_A + ('P' - 'A'));
	// 		jInsurance_No.setCaption(MessageBundle.getMessage("angal.patient.hasinsuranceno"));
	// 	}
	// 	return jInsurance_No;
	// }

	/**
	 * This method initializes jInsuranceUnknownRadioButton
	 * 
	 * @return javax.swing.RadioButton
	 */
	// private RadioButton getJInsurance_Unknown(){
	// 	if (jInsurance_Unknown == null){
	// 		jInsurance_Unknown = new RadioButton();
	// 		jInsurance_Unknown.setCaption(MessageBundle.getMessage("angal.patient.unknown"));
	// 		jInsurance_////Unknown.setClickShortcut(KeyEvent.VK_A + ('U' - 'A'));
	// 		jInsurance_Unknown.setSelected(true);
	// 	}
	// 	return jInsurance_Unknown;
	// }

	/**
	 * This method initializes jParentPanel
	 * 
	 * @return javax.swing.Panel
	 */
	private Panel getJParentPanel(){
		if (jParentPanel == null){
			jParentPanel = new Panel();
			jParentPanel.setCaption(MessageBundle.getMessage("angal.patient.parenttogether"));
			parentGroup = new RadioButtonGroup();
			parentGroup.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
			jParentPanel.setContent(parentGroup);
			parentGroup.setItems(MessageBundle.getMessage("angal.patient.yes"),MessageBundle.getMessage("angal.patient.no"),MessageBundle.getMessage("angal.patient.unknown"));
			parentGroup.setValue(MessageBundle.getMessage("angal.patient.unknown"));
			if (!insert){
				switch (patient.getParentTogether()){
				case 'Y':
					parentGroup.setValue(MessageBundle.getMessage("angal.patient.yes"));
					break;
				case 'N':
					parentGroup.setValue(MessageBundle.getMessage("angal.patient.no"));
					break;
				default:
					break;
				}
			}
		}
		return jParentPanel;
	}

	/**
	 * This method initializes jExtensionContent
	 * 
	 * @return javax.swing.Panel
	 */
	private VerticalLayout getJExtensionContent(){
		if (jExtensionContent == null){
			jExtensionContent = new VerticalLayout();
			jExtensionContent.setMargin(false);
			jExtensionContent.addComponent(getJBloodTypePanel());
			jExtensionContent.addComponent(getJFatherPanel());
			jExtensionContent.addComponent(getJMotherPanel());
			jExtensionContent.addComponent(getJParentPanel());
			jExtensionContent.addComponent(getJInsurancePanel());
		}
		return jExtensionContent;
	}

	/**
	 * set a specific border+title to a panel
	 */
	// private Panel setMyBorder(Panel c, String title){
	// 	javax.swing.border.Border b1 = BorderFactory.createLineBorder(Color.lightGray);
		
	// 	 * javax.swing.border.Border b2 = BorderFactory.createCompoundBorder(
	// 	 * BorderFactory.createTitledBorder(title),null);
		 
	// 	javax.swing.border.Border b2 = BorderFactory.createTitledBorder(b1, title, javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP);

	// 	c.setBorder(b2);
	// 	return c;
	// }

	// private Panel setMyBorderCenter(Panel c, String title){
	// 	javax.swing.border.Border b1 = BorderFactory.createLineBorder(Color.lightGray);
		
	// 	 * javax.swing.border.Border b2 = BorderFactory.createCompoundBorder(
	// 	 * BorderFactory.createTitledBorder(title),null);
		 
	// 	javax.swing.border.Border b2 = BorderFactory.createTitledBorder(b1, title, javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP);

	// 	c.setBorder(b2);
	// 	return c;
	// }

	/**
	 * This method initializes jFatherAlivePanel
	 * 
	 * @return javax.swing.Panel
	 */
	// private Panel getJFatherAlivePanel(){
	// 	if (jFatherAlivePanel == null){
	// 		jFatherAlivePanel = new Panel();
	// 		jFatherAlivePanel.addComponent(getJFather_Alive(), null);
	// 	}
	// 	return jFatherAlivePanel;
	// }

	/**
	 * This method initializes jNoteTextArea
	 * 
	 * @return javax.swing.Panel
	 */
	private TextArea getCaptionArea(){
		if (jNoteTextArea == null){
			jNoteTextArea = new TextArea();
			jNoteTextArea.setHeight("100%");
			// jNoteTextArea.setTabSize(4); //lebar field 4 tab
			// jNoteTextArea.setAutoscrolls(true);
			jNoteTextArea.setWordWrap(true);
			if (!insert){
				jNoteTextArea.setValue(patient.getNote());
			}
		}
		return jNoteTextArea;
	}

	/**
	 * This method initializes jNotePanel
	 * 
	 * @return javax.swing.Panel
	 */
	private void getjRightLayout(HorizontalLayout layout){
		if (jRightLayout == null){
			jRightLayout = new VerticalLayout();
			jRightLayout.setMargin(false);
			jRightLayout.setHeight("500px");
			// try {
			// 	// photoPanel = new PatientPhotoPanel(this, patient.getCode(), patient.getPhoto());
				
			// } catch (IOException e){
			// }
			// if (photoPanel != null) jRightLayout.addComponent(photoPanel);
			jRightLayout.addComponent(getjNotePanel());

		}
		layout.addComponent(jRightLayout);
	}

	private Panel getjNotePanel(){
		if (jNotePanel == null){
			jNotePanel = new Panel(MessageBundle.getMessage("angal.patient.note"));
			jNotePanel.setHeight("100%");
			jNotePanel.setContent(getCaptionArea());
		}
		return jNotePanel;
	}

	/**
	 * This method initializes jFatherNameTextField
	 * 
	 * @return javax.swing.TextField
	 */
	private TextField getJFatherNameTextField(){
		if (jFatherNameTextField == null){
			jFatherNameTextField = new TextField();
			jFatherNameTextField.setWidth("15em");
			if (!insert)
				jFatherNameTextField.setValue(patient.getFather_name());
		}
		return jFatherNameTextField;
	}

	/**
	 * This method initializes jMotherNameTextField
	 * 
	 * @return javax.swing.TextField
	 */
	private TextField getJMotherNameTextField(){
		if (jMotherNameTextField == null){
			jMotherNameTextField = new TextField();
			jMotherNameTextField.setWidth("15em");
			if (!insert)
				jMotherNameTextField.setValue(patient.getMother_name());
		}
		return jMotherNameTextField;
	}
	
	// public void setPatientPhoto(Image photo){
	// 	patient.setPhoto(photo);
	// }
}
