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

// import agetype.manager.AgeTypeBrowserManager;
// import agetype.model.AgeType;
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
import org.isf.utils.Logging;

import com.vaadin.ui.Window;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.Button;
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


public class PatientInsertExtended extends Window{

	private static final long serialVersionUID = -827831581202765055L;

	// private EventListenerList patientListeners = new EventListenerList();
	private List<PatientListener> patientListeners;// = new ArrayList();
	
	public interface PatientListener{
		public void patientUpdated(Patient aPatient);

		public void patientInserted(Patient aPatient);
	}

	public synchronized void addPatientListener(PatientListener l) {
		if(patientListeners==null)
			patientListeners = new ArrayList<PatientListener>();
		patientListeners.add(l);
	}

	public void removePatientListener(PatientListener l) {
		if(patientListeners==null){
			patientListeners = new ArrayList<PatientListener>();
			return;
		}
		patientListeners.remove(l);
	}

	// private void firePatientInserted(Patient aPatient) {
	// 	// browser.patientInserted(aPatient);
	// 	patientListeners[0].patientInserted(aPatient);
	// }

	private synchronized void firePatientInserted(Patient aPatient) {
		if(patientListeners != null){
			for(PatientListener patientListener : patientListeners){
				patientListener.patientInserted(aPatient);
			}
		}
	}

	private void firePatientUpdated(Patient aPatient) {
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
	private Panel jBirthDate = null;
	private Panel jBirthDateLabelPanel = null;
	private Label jBirthDateLabel = null;
	private Panel jBirthDateGroupPanel = null;
	private Calendar cBirthDate = null;
	private Button jBirthDateReset = null;
	private Label jBirthDateAge = null;

	// AgeDescription Components:
	private int ageType;
	private int ageTypeMonths;
	private Panel jAgeDesc = null;
	private Panel jAgeDescPanel = null;
	private Panel jAgeMonthsPanel = null;
	// private ComboBox jAgeDescComboBox = null;
	// private ComboBox jAgeMonthsComboBox = null;
	private Label jAgeMonthsLabel = null;

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
		this.setModal(true);
		this.windowContent = new VerticalLayout();
        this.setContent(this.windowContent);
        UI.getCurrent().addWindow(this);
		patient = old;
		insert = inserting;

		if (!insert) {
			lock = patient.getLock();
		}

		initialize(this);
	}

	public PatientInsertExtended(Patient old, boolean inserting, AdmittedPatientBrowser browser) {//change argument browser to dinamically type one
		this(old, inserting);
		this.browser = browser;
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize(Window window) {
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
	private void getWindowContent() {
		getDataLayout();
		getButtonLayout();
	}

	/**
	 * This method initializes jMainPanel
	 * 
	 * @return javax.swing.Panel
	 */
	private void getDataLayout() {
		HorizontalLayout topWindow = new HorizontalLayout();
		windowContent.addComponent(topWindow);
		getJDataContainPanel(topWindow);//qqq
		getjRightLayout(topWindow);
	}

	/**
	 * This method initializes ButtonLayout
	 * 
	 * @return javax.swing.Panel
	 */
	private void getButtonLayout() {
		if (ButtonLayout == null) {
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
		if (sexGroup.getValue()==MessageBundle.getMessage("angal.patient.female")) {
			patient.setSex('F');
		} else if (sexGroup.getValue()==MessageBundle.getMessage("angal.patient.male")) {
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
		if (motherGroup.getValue()==MessageBundle.getMessage("angal.patient.alive")) {
			patient.setMother('A');
		} else {
			if (motherGroup.getValue()==MessageBundle.getMessage("angal.patient.dead")) {
				patient.setMother('D');
			} else
				patient.setMother('U');
		}
		patient.setFather_name(jFatherNameTextField.getValue().trim());
		if (fatherGroup.getValue()==MessageBundle.getMessage("angal.patient.alive")) {
			patient.setFather('A');
		} else {
			if (fatherGroup.getValue()==MessageBundle.getMessage("angal.patient.dead")) {
				patient.setFather('D');
			} else
				patient.setFather('U');
		}
		patient.setBloodType(jBloodTypeComboBox.getValue().toString());
		if (insuranceGroup.getValue()==MessageBundle.getMessage("angal.patient.yes")) {
			patient.setHasInsurance('Y');
		} else {
			if (insuranceGroup.getValue()==MessageBundle.getMessage("angal.patient.no")) {
				patient.setHasInsurance('N');
			} else
				patient.setHasInsurance('U');
		}

		if (parentGroup.getValue()==MessageBundle.getMessage("angal.patient.yes")) {
			patient.setParentTogether('Y');
		} else {
			if (parentGroup.getValue()==MessageBundle.getMessage("angal.patient.no")) {
				patient.setParentTogether('N');
			} else
				patient.setParentTogether('U');
		}

		patient.setNote("");	// 	patient.setNote(jNoteTextArea.getValue().trim());
		result = manager.newPatient(patient);

		if (!result)
			MessageBox.createInfo().withCaption("Message").withMessage(MessageBundle.getMessage("angal.patient.thedatacouldnotbesaved")).withOkButton().open();
		else {
			if (justSave) {
				logger.info("masuk justsave");
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
		if (jOkButton == null) {
			jOkButton = new Button();
			jOkButton.setCaption(MessageBundle.getMessage("angal.common.ok"));
			jOkButton.setClickShortcut(KeyEvent.VK_A + ('O' - 'A'));
			jOkButton.addClickListener(e -> {
				String firstName = jFirstNameTextField.getValue().trim();
				String secondName = jSecondNameTextField.getValue().trim();
				if (firstName.equals("")) {
					MessageBox.createInfo().withCaption("Message").withMessage(MessageBundle.getMessage("angal.patient.insertfirstname")).withOkButton().open();
					return;
				}
				if (secondName.equals("")) {
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
		if (insert) {
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
			if (sexGroup.getValue()==MessageBundle.getMessage("angal.patient.female")) {
				patient.setSex('F');
			} else if (sexGroup.getValue()==MessageBundle.getMessage("angal.patient.male")) {
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
			if (motherGroup.getValue()==MessageBundle.getMessage("angal.patient.alive")) {
				patient.setMother('A');
			} else {
				if (motherGroup.getValue()==MessageBundle.getMessage("angal.patient.dead")) {
					patient.setMother('D');
				} else
					patient.setMother('U');
			}
			patient.setFather_name(jFatherNameTextField.getValue().trim());
			if (fatherGroup.getValue()==MessageBundle.getMessage("angal.patient.alive")) {
				patient.setFather('A');
			} else {
				if (fatherGroup.getValue()==MessageBundle.getMessage("angal.patient.dead")) {
					patient.setFather('D');
				} else
					patient.setFather('U');
			}
			patient.setBloodType(jBloodTypeComboBox.getValue().toString());
			if (insuranceGroup.getValue()==MessageBundle.getMessage("angal.patient.yes")) {
				patient.setHasInsurance('Y');
			} else {
				if (insuranceGroup.getValue()==MessageBundle.getMessage("angal.patient.no")) {
					patient.setHasInsurance('N');
				} else
					patient.setHasInsurance('U');
			}

			if (parentGroup.getValue()==MessageBundle.getMessage("angal.patient.yes")) {
				patient.setParentTogether('Y');
			} else {
				if (parentGroup.getValue()==MessageBundle.getMessage("angal.patient.no")) {
					patient.setParentTogether('N');
				} else
					patient.setParentTogether('U');
			}
			boolean result = false;
			patient.setNote("");	// 	patient.setNote(jNoteTextArea.getValue().trim());
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
	private void checkAge() {
		if (jAgeTypeButtonGroup.getValue()==MessageBundle.getMessage("angal.patient.modeage")) {
			try {
				years = Integer.parseInt(jAgeYears.getValue());
				months = Integer.parseInt(jAgeMonths.getValue());
				days = Integer.parseInt(jAgeDays.getValue());
				if (years == 0 && months == 0 && days == 0) throw new NumberFormatException();
				bbdate = bbdate.minusYears(years).minusMonths(months).minusDays(days);
			} catch (NumberFormatException ex1) {
				MessageBox.createInfo().withCaption("Message").withMessage(MessageBundle.getMessage("angal.patient.insertvalidage")).withOkButton().open();
				ageFalse();
			}
			if (years < 0 || years > 200)
				ageFalse();
			else if (years > 100) {
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
		}// else if (jAgeType_BirthDate.isSelected()) {
		// 	if (cBirthDate == null) return false;
		// 	else {
		// 		bdate = new DateTime(cBirthDate);
		// 		calcAge(bdate);
		// 	}
		// } else if (jAgeType_Description.isSelected()) {
		// 	AgeTypeBrowserManager at = new AgeTypeBrowserManager();
		// 	int index = jAgeDescComboBox.getSelectedIndex();
		// 	AgeType ageType = null;
			
		// 	if (index > 0) {
		// 		ageType = at.getTypeByCode(index);
		// 	} else
		// 		return false;

		// 	years = ageType.getFrom();
		// 	if (index == 1) {
		// 		months = jAgeMonthsComboBox.getSelectedIndex();
		// 		patient.setAgetype(ageType.getCode() + "/" + months);
		// 		bbdate = bbdate.minusYears(years).minusMonths(months);
		// 	} else {
		// 		bbdate = bbdate.minusYears(years);
		// 	}
		// }
	}

	/**
	 * This method initializes jCancelButton
	 * 
	 * @return javax.swing.Button
	 */
	private Button getJCancelButton() {
		if (jCancelButton == null) {
			jCancelButton = new Button();
			jCancelButton.setCaption("Cancel");
			jCancelButton.setClickShortcut(KeyEvent.VK_A + ('C' - 'A'));
			// jCancelButton.addClickListener(new java.awt.event.ActionListener() {
			// 	public void actionPerformed(java.awt.event.ActionEvent e) {
			// 		// dispose();
			// 	}
			// });
		}
		return jCancelButton;
	}

	/**
	 * This method initializes jBirthDate
	 * 
	 * @return javax.swing.Panel
	 */
	private Panel getJBirthDate() {
		if (jBirthDate == null) {
			jBirthDate = new Panel();
			GridBagLayout gbl_jBirthDate = new GridBagLayout();
			gbl_jBirthDate.columnWidths = new int[]{0, 0};
			gbl_jBirthDate.rowHeights = new int[]{0, 0};
			gbl_jBirthDate.columnWeights = new double[]{0.0, 1.0};
			gbl_jBirthDate.rowWeights = new double[]{0.0, 0.0};
			// jBirthDate.setLayout(gbl_jBirthDate);
			GridBagConstraints gbc_jBirthDateLabelPanel = new GridBagConstraints();
			gbc_jBirthDateLabelPanel.anchor = GridBagConstraints.WEST;
			gbc_jBirthDateLabelPanel.gridx = 0;
			gbc_jBirthDateLabelPanel.gridy = 0;
			// jBirthDate.addComponent(getJBirthDateLabelPanel(), gbc_jBirthDateLabelPanel);
			GridBagConstraints gbc_jBirthDateGroupPanel = new GridBagConstraints();
			gbc_jBirthDateGroupPanel.fill = GridBagConstraints.HORIZONTAL;
			gbc_jBirthDateGroupPanel.anchor = GridBagConstraints.WEST;
			gbc_jBirthDateGroupPanel.gridx = 1;
			gbc_jBirthDateGroupPanel.gridy = 0;
			// jBirthDate.addComponent(getJBirthDateGroupPanel(), gbc_jBirthDateGroupPanel);
			GridBagConstraints gbc_jBirthDateAge = new GridBagConstraints();
			gbc_jBirthDateAge.anchor = GridBagConstraints.WEST;
			gbc_jBirthDateAge.gridx = 1;
			gbc_jBirthDateAge.gridy = 1;
			// jBirthDate.addComponent(getJBirthDateAge(), gbc_jBirthDateAge);
		}
		return jBirthDate;
	}

	private Label getJBirthDateAge() {
		if (jBirthDateAge == null) {
			jBirthDateAge = new Label(" ");
		}
		return jBirthDateAge;
	}
	
	private String formatYearsMonthsDays(int years, int months, int days) {
		return years+"y "+months+"m "+days+"d";
	}

	/**
	 * This method initializes jBirthDateLabel
	 * 
	 * @return javax.swing.Label
	 */
	private Label getJBirthDateLabel() {
		if (jBirthDateLabel == null) {
			jBirthDateLabel = new Label();
			jBirthDateLabel.setCaption(MessageBundle.getMessage("angal.patient.birthdate"));
		}
		return jBirthDateLabel;
	}

	/**
	 * This method initializes jBirthDateGroupPanel
	 * 
	 * @return javax.swing.Panel
	 */

	private Panel getJBirthDateGroupPanel() {
		class BirthDateChooser extends JDateChooser {

			private static final long serialVersionUID = -78813689560070139L;

			public BirthDateChooser(Calendar cBirthDate) {
				super();
				super.setLocale(new Locale(GeneralData.LANGUAGE));
				super.setDateFormatString("dd/MM/yyyy");
				super.setPreferredSize(new Dimension(150, 20));
				// super.dateEditor.setEnabled(false);

				if (cBirthDate != null) {
					super.setCalendar(cBirthDate);
				}
			}

			public void propertyChange(PropertyChangeEvent e) {
				super.propertyChange(e);

				if (super.dateSelected) {
					cBirthDate = super.jcalendar.getCalendar();
					DateTime bdate = new DateTime(cBirthDate);
					if (bdate.isAfter(new DateTime())) super.setCalendar(new DateTime().toGregorianCalendar());
					else calcAge(bdate);
				}

				if (super.dateEditor.getDate() != null) {
					cBirthDate = super.getCalendar();
					DateTime bdate = new DateTime(cBirthDate);
					if (bdate.isAfter(new DateTime())) super.setCalendar(new DateTime().toGregorianCalendar());
					else calcAge(bdate);
				}
			}
		}
		
		if (jBirthDateGroupPanel == null) {
			jBirthDateGroupPanel = new Panel();
			// jBirthDateGroupPanel.setLayout(new BorderLayout());

			if (!insert) {//qqqedit
				Date sBirthDate = patient.getBirthDate();

				if (sBirthDate != null) {
					cBirthDate = Calendar.getInstance();
					cBirthDate.setTimeInMillis(sBirthDate.getTime());
				}
			}

			final BirthDateChooser jBirthDateChooser = new BirthDateChooser(cBirthDate);
			// jBirthDateGroupPanel.addComponent(jBirthDateChooser, BorderLayout.CENTER);

			// if (jBirthDateReset == null) {
			// 	jBirthDateReset = new Button();
			// 	jBirthDateReset.setIcon(new ImageIcon("rsc/icons/trash_button.png"));
			// 	jBirthDateReset.setPreferredSize(new Dimension(20, 20));
			// 	jBirthDateReset.addClickListener(new ActionListener() {
			// 		public void actionPerformed(ActionEvent e) {
			// 			jBirthDateChooser.getDateEditor().setDate(null);
			// 			/*
			// 			 * jAgeField.setCaption(""); jAgeField.setEditable(true);
			// 			 */
			// 			cBirthDate = null;
			// 		}
			// 	});

			// 	jBirthDateGroupPanel.addComponent(jBirthDateReset, BorderLayout.EAST);
			// }
		}
		return jBirthDateGroupPanel;
	}

	private void calcAge(DateTime bdate) {
		Period p = new Period(bdate, new DateTime(), PeriodType.yearMonthDay());
		years = p.getYears();
		months = p.getMonths();
		days = p.getDays();
		getJBirthDateAge();
		jBirthDateAge.setCaption(formatYearsMonthsDays(years, months, days));
	}
	
	/**
	 * This method initializes jBirthDateLabelPanel
	 * 
	 * @return javax.swing.Panel
	 */
	private Panel getJBirthDateLabelPanel() {
		if (jBirthDateLabelPanel == null) {
			jBirthDateLabelPanel = new Panel();
			// jBirthDateLabelPanel.addComponent(getJBirthDateLabel(), BorderLayout.EAST);
		}
		return jBirthDateLabelPanel;
	}

	/**
	 * This method initializes jFirstNameTextField
	 * 
	 * @return javax.swing.TextField
	 */
	private TextField getJFirstNameTextField() {
		if (jFirstNameTextField == null) {
			jFirstNameTextField = new TextField();
			jFirstNameTextField.setMaxLength(15);
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
	private TextField getJSecondNameTextField() {
		if (jSecondNameTextField == null) {
			jSecondNameTextField = new TextField();
			jSecondNameTextField.setMaxLength(15);
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
	private RadioButtonGroup getSexPanel() {
		// if (jSexPanel == null) {
			// jSexPanel = new FormLayout();
			sexGroup = new RadioButtonGroup<>();
			sexGroup.setCaption(MessageBundle.getMessage("angal.patient.sexstar"));
			sexGroup.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
			sexGroup.setItems(MessageBundle.getMessage("angal.patient.male"),MessageBundle.getMessage("angal.patient.female"));
		// 	radiom.setClickShortcut(KeyEvent.VK_A + ('M' - 'A'));//unimplemented
		// 	radiof.setClickShortcut(KeyEvent.VK_A + ('F' - 'A'));
			// jSexPanel.addComponent(getJSexLabelPanel());
			if (!insert) {
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
	private Label getJAddressLabelPanel() {
		// if (jAddressLabelPanel == null) {
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
	private TextField getJAddressTextField() {
		if (jAddressTextField == null) {
			jAddressTextField = new TextField();
			jAddressTextField.setMaxLength(15);
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
	private TextField getJTaxCodeTextField() {
		if (jTaxCodeTextField == null) {
			jTaxCodeTextField = new TextField();
			jTaxCodeTextField.setMaxLength(15);
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
	private Label getJCityLabelPanel() {
		// if (jCityLabelPanel == null) {
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
	private TextField getJCityTextField() {
		if (jCityTextField == null) {
			jCityTextField = new TextField();
			jCityTextField.setMaxLength(15);
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
	private Panel getJTelPanel() {
		if (jTelephoneLabelPanel == null) {
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
	private TextField getJTelephoneTextField() {
		SmsParameters.getSmsParameters();
		if (jTelephoneTextField == null) {
			jTelephoneTextField = new TextField();
			jTelephoneTextField.setMaxLength(15);
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
	private Panel getJNextKinLabelPanel() {
		if (jNextKinLabelPanel == null) {
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
	private TextField getJNextKinTextField() {
		if (jNextKinTextField == null) {
			jNextKinTextField = new TextField();
			jNextKinTextField.setCaption(MessageBundle.getMessage("angal.patient.nextkin"));
			jNextKinTextField.setMaxLength(15);
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
	private Panel getJBloodTypePanel() {
		if (jBloodTypePanel == null) {
			jBloodTypePanel = new Panel();
			jBloodTypePanel.setCaption(MessageBundle.getMessage("angal.patient.bloodtype"));
			String[] bloodTypes = { MessageBundle.getMessage("angal.patient.bloodtype.unknown"), "0+", "A+", "B+", "AB+", "0-", "A-", "B-", "AB-" };
			jBloodTypeComboBox = new ComboBox();
			jBloodTypeComboBox.setEmptySelectionAllowed(false);
			jBloodTypeComboBox.setItems(bloodTypes);
			jBloodTypeComboBox.setSelectedItem(MessageBundle.getMessage("angal.patient.bloodtype.unknown"));
			jBloodTypePanel.setContent(jBloodTypeComboBox);

			if (!insert) {
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
	private Panel getJAnagraphPanel() {
		if (jAnagraphPanel == null) {
			jAnagraphPanel = new Panel();
			FormLayout form = new FormLayout();
			jAnagraphPanel.setContent(form);
			form.addComponent(getJFirstNameTextField());
			form.addComponent(getJSecondNameTextField());
			form.addComponent(getJTaxCodeTextField());
			form.addComponent(getJAgeType());
			form.addComponent(getSexPanel());
			form.addComponent(getJAddressPanel());
			form.addComponent(getJCity());
			form.addComponent(getJNextKin());
			form.addComponent(getJTelephone());
			form.addComponent(getLabelRequiredFields());
		}
		return jAnagraphPanel;
	}
	
	private Label getLabelRequiredFields() {
		if (labelRequiredFields == null) {
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
	private Label getJSexLabelPanel() {
		if (jSexLabel == null) {
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
	private Panel getJAgeType() {///fix
		if (jAgeType == null) {
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
	private RadioButtonGroup getJAgeTypeButtonGroup(VerticalLayout layout) {//qqq
		if (jAgeTypeButtonGroup == null) {
			jAgeTypeButtonGroup = new RadioButtonGroup<>();
			jAgeTypeButtonGroup.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
			jAgeTypeButtonGroup.setItems(MessageBundle.getMessage("angal.patient.modeage"), MessageBundle.getMessage("angal.patient.modedescription"), MessageBundle.getMessage("angal.patient.modebdate"));
			jAgeTypeButtonGroup.addValueChangeListener(e -> {
				if(e.getValue()==MessageBundle.getMessage("angal.patient.modeage")){
					// layout.removeComponent();
				}
				else if(e.getValue()==MessageBundle.getMessage("angal.patient.modedescription")){
					logger.info("sb");
				}
				else{
					logger.info("sc");
				}
			});
			jAgeTypeButtonGroup.setValue(MessageBundle.getMessage("angal.patient.modeage"));//click action unimplemented yet
		// 	getJAge();
		// else if (jAgeTypeButtonGroup.getValue()==MessageBundle.getMessage("angal.patient.modedescription"))
		// 	jAgeTypeSelection = null;//getJBirthDate();
		// else
		// 	jAgeTypeSelection = null;//getJAgeDescription();
		// return jAgeTypeSelection;
		// 	ActionListener sliceActionListener = new ActionListener() {
		// 		public void actionPerformed(ActionEvent actionEvent) {
		// 			jAgeType.remove(jAgeTypeSelection);
		// 			jAgeType.addComponent(getJAgeTypeSelection());
		// 			jAgeType.validate();
		// 			jAgeType.repaint();

		// 		}
		// 	};

			if (!insert) {//qqqedit
			// 	if (patient.getBirthDate() != null) {
			// 		jAgeType_BirthDate.setSelected(true);
					calcAge(new DateTime(patient.getBirthDate()));
			// 	} else if (patient.getAgetype() != null && patient.getAgetype().compareTo("") != 0) {
			// 		parseAgeType();
			// 		jAgeType_Description.setSelected(true);
			// 	} else {
			// 		jAgeType_Age.setSelected(true);
					years = patient.getAge();
					
			// 	}
			} //else {
			// 	jAgeType_Age.setSelected(true);
			// }

		// 	jAgeType_Age.addClickListener(sliceActionListener);
		// 	jAgeType_Description.addClickListener(sliceActionListener);
		// 	jAgeType_BirthDate.addClickListener(sliceActionListener);

		}
		return jAgeTypeButtonGroup;
	}

	/**
	 * This method initializes ageType & ageTypeMonths
	 * 
	 * @return javax.swing.Panel
	 */
	private void parseAgeType() {

		// if (patient.getAgetype().compareTo("") != 0) {
		// 	StringTokenizer token = new StringTokenizer(patient.getAgetype(), "/");
		// 	String token1 = token.nextToken();
		// 	String t1 = token1.substring(1, 2);
		// 	ageType = Integer.parseInt(t1);

		// 	if (token.hasMoreTokens()) {

		// 		String token2 = token.nextToken();
		// 		int t2 = Integer.parseInt(token2);
		// 		ageTypeMonths = t2;
		// 	} else
		// 		ageTypeMonths = 0;
		// } else {
		// 	ageType = -1;
		// }
	}

	/**
	 * This method initializes jAgeTypeSelection
	 * 
	 * @return javax.swing.Panel
	 */
	private HorizontalLayout getJAgeTypeSelection() {
		if (jAgeTypeButtonGroup.getValue()==MessageBundle.getMessage("angal.patient.modeage"))
			jAgeTypeSelection = getJAge();
		else if (jAgeTypeButtonGroup.getValue()==MessageBundle.getMessage("angal.patient.modedescription"))
			jAgeTypeSelection = null;//getJBirthDate();
		else
			jAgeTypeSelection = null;//getJAgeDescription();
		return jAgeTypeSelection;
	}

	/**
	 * This method initializes jAgeType_BirthDatePanel
	 * 
	 * @return javax.swing.Panel
	 */
	// private Panel getJAgeType_BirthDatePanel() {
	// 	// if (jAgeType_BirthDatePanel == null) {
	// 	// 	jAgeType_BirthDatePanel = new Panel();
	// 	// 	jAgeType_BirthDatePanel.addComponent(getJAgeType_BirthDate(), null);
	// 	// }
	// 	// return jAgeType_BirthDatePanel;
	// }

	/**
	 * This method initializes jAgeDesc
	 * 
	 * @return javax.swing.Panel
	 */
	// private Panel getJAgeDescription() {
	// 	if (jAgeDesc == null) {
	// 		jAgeDesc = new Panel();
	// 		jAgeDesc.addComponent(getJAgeDescPanel());// , java.awt.BorderLayout.WEST);

	// 	}
	// 	return jAgeDesc;
	// }

	/**
	 * This method initializes jAgeMonthsPanel
	 * 
	 * @return javax.swing.Panel
	 */
	private Panel getJAgeMonthsPanel() {
		if (jAgeMonthsPanel == null) {
			jAgeMonthsPanel = new Panel();
			jAgeMonthsLabel = new Label("months");

			String[] months = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" };
			// jAgeMonthsComboBox = new ComboBox(months);
		}

		// jAgeMonthsPanel.addComponent(jAgeMonthsComboBox);
		// jAgeMonthsPanel.addComponent(jAgeMonthsLabel);

		// if (!insert && ageType == 1) {

		// 	jAgeMonthsComboBox.setSelectedIndex(ageTypeMonths);

		// }
		return jAgeMonthsPanel;
	}

	/**
	 * This method initializes jAgeDescPanel
	 * 
	 * @return javax.swing.Panel
	 */
	private Panel getJAgeDescPanel() {
		// if (jAgeDescPanel == null) {
		// 	jAgeDescPanel = new Panel();

		// 	jAgeDescComboBox = new ComboBox();

		// 	AgeTypeBrowserManager at = new AgeTypeBrowserManager();
		// 	ArrayList<AgeType> ageList = at.getAgeType();
		// 	jAgeDescComboBox.addItem("");
		// 	for (AgeType ag : ageList) {
		// 		jAgeDescComboBox.addItem(MessageBundle.getMessage(ag.getDescription()));
		// 	}

		// 	jAgeDescPanel.addComponent(jAgeDescComboBox);
		// 	jAgeDescPanel.addComponent(getJAgeMonthsPanel());
		// 	jAgeMonthsComboBox.setEnabled(false);

		// 	jAgeDescComboBox.addClickListener(new ActionListener() {
		// 		public void actionPerformed(ActionEvent e) {
		// 			if (jAgeDescComboBox.getSelectedItem().toString().compareTo(MessageBundle.getMessage("angal.agetype.newborn")) == 0) {
		// 				jAgeMonthsComboBox.setEnabled(true);

		// 			} else {
		// 				jAgeMonthsComboBox.setEnabled(false);

		// 			}
		// 		}
		// 	});

		// 	if (!insert) {

		// 		parseAgeType();
		// 		jAgeDescComboBox.setSelectedIndex(ageType + 1);

		// 		if (ageType == 0) {
		// 			jAgeMonthsComboBox.setEnabled(true);
		// 			jAgeMonthsComboBox.setSelectedIndex(ageTypeMonths);
		// 		}
		// 	}

		// }
		return jAgeDescPanel;
	}

	/**
	 * This method initializes jAge
	 * 
	 * @return javax.swing.Panel
	 */
	private HorizontalLayout getJAge() {
		if (jAge == null) {//fix
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
	// private Panel getJAddressFieldPanel() {
	// 	if (jAddressFieldPanel == null) {
	// 		jAddressFieldPanel = new Panel();
	// 		jAddressFieldPanel.addComponent(getJAdressTextField(), null);
	// 	}
	// 	return jAddressFieldPanel;
	// }

	private TextField getJAgeFieldYears() {
		if (jAgeYears == null) {
			jAgeYears = new TextField();
			jAgeYears.setValue(""+0);
			jAgeYears.setMaxLength(3);
			jAgeYears.setWidthUndefined();
			jAgeYears.addFocusListener(new FocusListener() {
				@Override
				public void focus(FocusEvent e) {//blocking the textfield value value
					TextField thisField = (TextField) e.getSource();
					thisField.setSelection(0,thisField.getValue().length());
				}
			});
			if (!insert) jAgeYears.setValue(""+years);
		}
		return jAgeYears;
	}
	
	private TextField getJAgeFieldMonths() {
		if (jAgeMonths == null) {
			jAgeMonths = new TextField();
			jAgeMonths.setValue(""+0);
			jAgeMonths.setMaxLength(3);
			jAgeMonths.setWidthUndefined();
			jAgeMonths.addFocusListener(new FocusListener() {
				@Override
				public void focus(FocusEvent e) {//blocking the textfield value value
					TextField thisField = (TextField) e.getSource();
					thisField.setSelection(0,thisField.getValue().length());
				}
			});
			if (!insert) jAgeMonths.setValue(""+months); 
		}
		return jAgeMonths;
	}
	
	private TextField getJAgeFieldDays() {
		if (jAgeDays == null) {
			jAgeDays = new TextField();
			jAgeDays.setValue(""+0);
			jAgeDays.setMaxLength(3);
			jAgeDays.setWidthUndefined();
			jAgeDays.addFocusListener(new FocusListener() {
				@Override
				public void focus(FocusEvent e) {//blocking the textfield value value
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
	// private Panel getJCityFieldPanel() {
	// 	if (jCityFieldPanel == null) {
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
	private TextField getJNextKinFieldPanel() {
		// if (jNextKinFieldPanel == null) {
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
	private TextField getJTelephoneFieldPanel() {
		// if (jTelephoneFieldPanel == null) {
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
	private TextField getJAddressPanel() {
		// if (jAddress == null) {
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
	private TextField getJCity() {
		// if (jCity == null) {
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
	private TextField getJNextKin() {
		// if (jNextKin == null) {
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
	private TextField getJTelephone() {
		// if (jTelephone == null) {
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
	private void getJDataContainPanel(HorizontalLayout layout) {
		jDataContainPanel = new Panel();
		if (!insert) {
			jDataContainPanel.setCaption(patient.getName() + " (" + MessageBundle.getMessage("angal.common.code") + ": " + patient.getCode() + ")");
		} else {
			int nextcode = manager.getNextPatientCode();
			patient.setCode(nextcode);
			jDataContainPanel.setCaption(MessageBundle.getMessage("angal.patient.insertdataofnewpatient"));
		}
		layout.addComponent(jDataContainPanel);
		HorizontalLayout dataWindow = new HorizontalLayout();
		jDataContainPanel.setContent(dataWindow);
		dataWindow.addComponent(getJAnagraphPanel());
		dataWindow.addComponent(getJExtensionContent());
	}

	/**
	 * This method initializes jFatherPanel
	 * 
	 * @return javax.swing.Panel
	 */
	private Panel getJFatherPanel() {
		if (jFatherPanel == null) {
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
			if (!insert) {
				switch (patient.getFather()) {
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
	private Panel getJMotherPanel() {
		if (jMotherPanel == null) {
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
			if (!insert) {
				switch (patient.getMother()) {
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
	private Panel getJInsurancePanel() {
		if (jInsurancePanel == null) {
			jInsurancePanel = new Panel(MessageBundle.getMessage("angal.patient.hasinsurance"));
			insuranceGroup = new RadioButtonGroup();
			jInsurancePanel.setContent(insuranceGroup);
			insuranceGroup.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
			insuranceGroup.setItems(MessageBundle.getMessage("angal.patient.yes"),MessageBundle.getMessage("angal.patient.no"),MessageBundle.getMessage("angal.patient.unknown"));
			insuranceGroup.setValue(MessageBundle.getMessage("angal.patient.unknown"));
			if (!insert) {
				switch (patient.getHasInsurance()) {
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
	// private RadioButton getJInsurance_Yes() {
	// 	if (jInsurance_Yes == null) {
	// 		jInsurance_Yes = new RadioButton();
	// 		jInsurance_Yes.setClickShortcut(KeyEvent.VK_A + ('R' - 'A'));
	// 		jInsurance_Yes.setCaption(MessageBundle.getMessage("angal.patient.hasinsuranceyes"));
	// 	}
	// 	return jInsurance_Yes;
	// }

	/**
	 * This method initializes jInsuranceNoRadioButton
	 * 
	 * @return javax.swing.RadioButton
	 */
	// private RadioButton getJInsurance_No() {
	// 	if (jInsurance_No == null) {
	// 		jInsurance_No = new RadioButton();
	// 		jInsurance_No.setClickShortcut(KeyEvent.VK_A + ('P' - 'A'));
	// 		jInsurance_No.setCaption(MessageBundle.getMessage("angal.patient.hasinsuranceno"));
	// 	}
	// 	return jInsurance_No;
	// }

	/**
	 * This method initializes jInsuranceUnknownRadioButton
	 * 
	 * @return javax.swing.RadioButton
	 */
	// private RadioButton getJInsurance_Unknown() {
	// 	if (jInsurance_Unknown == null) {
	// 		jInsurance_Unknown = new RadioButton();
	// 		jInsurance_Unknown.setCaption(MessageBundle.getMessage("angal.patient.unknown"));
	// 		jInsurance_Unknown.setClickShortcut(KeyEvent.VK_A + ('U' - 'A'));
	// 		jInsurance_Unknown.setSelected(true);
	// 	}
	// 	return jInsurance_Unknown;
	// }

	/**
	 * This method initializes jParentPanel
	 * 
	 * @return javax.swing.Panel
	 */
	private Panel getJParentPanel() {
		if (jParentPanel == null) {
			jParentPanel = new Panel();
			jParentPanel.setCaption(MessageBundle.getMessage("angal.patient.parenttogether"));
			parentGroup = new RadioButtonGroup();
			parentGroup.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
			jParentPanel.setContent(parentGroup);
			parentGroup.setItems(MessageBundle.getMessage("angal.patient.yes"),MessageBundle.getMessage("angal.patient.no"),MessageBundle.getMessage("angal.patient.unknown"));
			parentGroup.setValue(MessageBundle.getMessage("angal.patient.unknown"));
			if (!insert) {
				switch (patient.getParentTogether()) {
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
	private VerticalLayout getJExtensionContent() {
		if (jExtensionContent == null) {
			jExtensionContent = new VerticalLayout();
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
	// private Panel setMyBorder(Panel c, String title) {
	// 	javax.swing.border.Border b1 = BorderFactory.createLineBorder(Color.lightGray);
		
	// 	 * javax.swing.border.Border b2 = BorderFactory.createCompoundBorder(
	// 	 * BorderFactory.createTitledBorder(title),null);
		 
	// 	javax.swing.border.Border b2 = BorderFactory.createTitledBorder(b1, title, javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP);

	// 	c.setBorder(b2);
	// 	return c;
	// }

	// private Panel setMyBorderCenter(Panel c, String title) {
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
	// private Panel getJFatherAlivePanel() {
	// 	if (jFatherAlivePanel == null) {
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
	// private TextArea getCaptionArea() {
	// 	if (jNoteTextArea == null) {
	// 		jNoteTextArea = new TextArea();
	// 		jNoteTextArea.setTabSize(4);
	// 		jNoteTextArea.setAutoscrolls(true);
	// 		jNoteTextArea.setLineWrap(true);
	// 		if (!insert) {
	// 			jNoteTextArea.setCaption(patient.getNote());
	// 		}
	// 	}
	// 	return jNoteTextArea;
	// }

	/**
	 * This method initializes jNotePanel
	 * 
	 * @return javax.swing.Panel
	 */
	private void getjRightLayout(HorizontalLayout layout) {
		if (jRightLayout == null) {
			jRightLayout = new VerticalLayout();
			// try {
			// 	// photoPanel = new PatientPhotoPanel(this, patient.getCode(), patient.getPhoto());
				
			// } catch (IOException e) {
			// 	logger.info("masuk ke catch photopanel");
			// }
			// if (photoPanel != null) jRightLayout.addComponent(photoPanel);
			jRightLayout.addComponent(getjNotePanel());

		}
		layout.addComponent(jRightLayout);
	}

	private Panel getjNotePanel() {
		if (jNotePanel == null) {
			jNotePanel = new Panel(MessageBundle.getMessage("angal.patient.note"));
			jNoteTextArea = new TextArea();
			jNotePanel.setContent(jNoteTextArea);
		}
		return jNotePanel;
	}

	/**
	 * This method initializes jFatherNameTextField
	 * 
	 * @return javax.swing.TextField
	 */
	private TextField getJFatherNameTextField() {
		if (jFatherNameTextField == null) {
			jFatherNameTextField = new TextField();
			jFatherNameTextField.setMaxLength(15);
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
	private TextField getJMotherNameTextField() {
		if (jMotherNameTextField == null) {
			jMotherNameTextField = new TextField();
			jMotherNameTextField.setMaxLength(15);
			if (!insert)
				jMotherNameTextField.setValue(patient.getMother_name());
		}
		return jMotherNameTextField;
	}
	
	// public void setPatientPhoto(Image photo) {
	// 	patient.setPhoto(photo);
	// }
}
