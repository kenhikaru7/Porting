package org.isf.admission.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// import accounting.gui.PatientBillEdit;
// import accounting.manager.BillBrowserManager;
// import accounting.model.Bill;
import org.isf.admission.manager.AdmissionBrowserManager;
import org.isf.admission.model.Admission;
import org.isf.admission.model.AdmittedPatient;
import org.isf.examination.gui.PatientExaminationEdit;
import org.isf.examination.model.GenderPatientExamination;
import org.isf.examination.model.PatientExamination;
import org.isf.examination.service.ExaminationOperations;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.gui.MainMenu;
// import opd.gui.OpdEditExtended;
// import opd.model.Opd;
import org.isf.patient.gui.PatientInsert;
import org.isf.patient.gui.PatientInsertExtended;
import org.isf.patient.manager.PatientBrowserManager;
import org.isf.patient.model.Patient;
// import therapy.gui.TherapyEdit;qqd
import org.isf.utils.db.NormalizeString;
import org.isf.utils.jobjects.BusyState;
import org.isf.utils.time.TimeTools;
import org.isf.utils.jobjects.ModalWindow;
import org.isf.utils.Logging;
// import ward.manager.WardBrowserManager;qqd
// import ward.model.Ward;qqd

import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.Button;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Grid;
import de.steinwedel.messagebox.MessageBox;

/**
 * This class shows a list of all known patients and for each if (and where) they are actually admitted, 
 * you can:
 *  filter patients by ward and admission status
 *  search for patient with given name 
 *  add a new patient, edit or delete an existing patient record
 *  view extended data of a selected patient 
 *  add an admission record (or modify existing admission record, or set a discharge) of a selected patient
 * 
 * release 2.2 oct-23-06
 * 
 * @author flavio
 * 
 */


/*----------------------------------------------------------
 * modification history
 * ====================
 * 23/10/06 - flavio - lastKey reset
 * 10/11/06 - ross - removed from the list the deleted patients
 *                   the list is now in alphabetical  order (modified IoOperations)
 * 12/08/08 - alessandro - Patient Extended
 * 01/01/09 - Fabrizio   - The OPD button is conditioned to the extended funcionality of OPD.
 *                         Reorganized imports.
 * 13/02/09 - Alex - Search Key extended to patient code & notes
 * 29/05/09 - Alex - fixed mnemonic keys for Admission, OPD and PatientSheet
 * 14/10/09 - Alex - optimized searchkey algorithm and cosmetic changes to the code
 * 02/12/09 - Alex - search field get focus at begin and after Patient delete/update
 * 03/12/09 - Alex - added new button for merging double registered patients histories
 * 05/12/09 - Alex - fixed exception on filter after saving admission
 * 06/12/09 - Alex - fixed exception on filter after saving admission (ALL FILTERS)
 * 06/12/09 - Alex - Cosmetic changes to GUI
 -----------------------------------------------------------*/

public class AdmittedPatientBrowser extends ModalWindow implements
		// PatientInsert.PatientListener//, AdmissionBrowser.AdmissionListener,
		PatientInsertExtended.PatientListener//, AdmissionBrowser.AdmissionListener, //by Alex
		//PatientDataBrowser.DeleteAdmissionListener
		{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private UI main;
	private VerticalLayout windowContent;

	private String[] patientClassItems = { MessageBundle.getMessage("angal.admission.all"), MessageBundle.getMessage("angal.admission.admitted"), MessageBundle.getMessage("angal.admission.notadmitted") };
	// private JComboBox patientClassBox = new JComboBox(patientClassItems);
	private TextField searchString = null;
	private Button jSearchButton = null;
	private Button examinationButton;
	private String lastKey = "";
	// private ArrayList<Ward> wardList = null;qqd
	private Label rowCounter = null;
	private String rowCounterText = MessageBundle.getMessage("angal.admission.count");
	private ArrayList<AdmittedPatient> pPatient = new ArrayList<AdmittedPatient>();
	private String informations = MessageBundle.getMessage("angal.admission.city") + " / " + MessageBundle.getMessage("angal.admission.addressm") + " / " + MessageBundle.getMessage("angal.admission.telephone") + " / " + MessageBundle.getMessage("angal.patient.note");
	private String[] pColums = { MessageBundle.getMessage("angal.common.code"), MessageBundle.getMessage("angal.admission.name"), MessageBundle.getMessage("angal.admission.age"), MessageBundle.getMessage("angal.admission.sex"), informations, MessageBundle.getMessage("angal.admission.ward") };
	private int[] pColumwidth = { 100, 200, 80, 50, 150, 100 };
	private boolean[] pColumResizable = {false, false, false, false, true, false};
	private AdmittedPatient patient;
	private Grid<Patient> grid;
	// private JScrollPane scrollPane;
	private AdmittedPatientBrowser myFrame;
	private AdmissionBrowserManager manager = new AdmissionBrowserManager();
	protected boolean altKeyReleased = true;
	Logging logger;
	AdmittedPatientBrowserModel data;
	List<Patient> patients;
	
	public void fireMyDeletedPatient(Patient p){
				
		int cc = 0;
		boolean found = false;
		for (AdmittedPatient elem : pPatient) {
			if (elem.getPatient().getCode() == p.getCode()) {
				found = true;
				break;
			}
			cc++;
		}
		if (found){
			pPatient.remove(cc);
			lastKey = "";
			filterPatient(searchString.getValue());
		}
	}
	
	/*
	 * manage PatientDataBrowser messages
	 */
	public void deleteAdmissionUpdated(AWTEvent e) {
		Admission adm = (Admission) e.getSource();
		
		//remember selected row
		// int row = grid.getSelectedRow();
		
		for (AdmittedPatient elem : pPatient) {
			if (elem.getPatient().getCode() == adm.getPatId()) {
				//found same patient in the list
				Admission elemAdm = elem.getAdmission();
				if (elemAdm != null) {
					//the patient is admitted
					if (elemAdm.getId() == adm.getId())
						//same admission --> delete
						elem.setAdmission(null);	
				}
				break;
			}
		}
		lastKey = "";
		// filterPatient(searchString.getText());
		// try {
		// 	if (grid.getRowCount() > 0)
		// 		grid.setRowSelectionInterval(row, row);
		// } catch (Exception e1) {
		// }
		
	}

	/*
	 * manage AdmissionBrowser messages
	 */
	public void admissionInserted(AWTEvent e) {
		// Admission adm = (Admission) e.getSource();
		
		// //remember selected row
		// int row = grid.getSelectedRow();
		// int patId = adm.getPatId();
		
		// for (AdmittedPatient elem : pPatient) {
		// 	if (elem.getPatient().getCode() == patId) {
		// 		//found same patient in the list
		// 		elem.setAdmission(adm);
		// 		break;
		// 	}
		// }
		// lastKey = "";
		// // filterPatient(searchString.getText());
		// try {
		// 	if (grid.getRowCount() > 0)
		// 		grid.setRowSelectionInterval(row, row);
		// } catch (Exception e1) {
		// }
	}

	/*
	 * param contains info about patient admission,
	 * ward can varying or patient may be discharged
	 * 
	 */
	public void admissionUpdated(AWTEvent e) {
		// Admission adm = (Admission) e.getSource();
		
		// //remember selected row
		// int row = grid.getSelectedRow();
		// int admId = adm.getId();
		// int patId = adm.getPatId();
		
		// for (AdmittedPatient elem : pPatient) {
		// 	if (elem.getPatient().getCode() == patId) {
		// 		//found same patient in the list
		// 		Admission elemAdm = elem.getAdmission();
		// 		if (adm.getDisDate() != null) {
		// 			//is a discharge
		// 			if (elemAdm != null) {
		// 				//the patient is not discharged
		// 				if (elemAdm.getId() == admId)
		// 					//same admission --> discharge
		// 					elem.setAdmission(null);
		// 			}
		// 		} else {
		// 			//is not a discharge --> patient admitted
		// 			elem.setAdmission(adm);
		// 		}
		// 		break;
		// 	}
		// }
		// lastKey = "";
		// // filterPatient(searchString.getText());
		// try {
		// 	if (grid.getRowCount() > 0)
		// 		grid.setRowSelectionInterval(row, row);
			
		// } catch (Exception e1) {
		// }
	}

	/*
	 * manage PatientEdit messages
	 * 
	 * mind PatientEdit return a patient patientInserted create a new
	 * AdmittedPatient for grid
	 */
	public void patientInserted(Patient aPatient) {
		pPatient.add(0, new AdmittedPatient(aPatient, null));
		patients = data.getPatientList();
		grid.setItems(patients);
		lastKey = "";
		filterPatient(searchString.getValue());
		rowCounter.setCaption(rowCounterText + ": " + pPatient.size());
		grid.select(aPatient);
		searchString.focus();
		searchString.focus();
	}

	public void patientUpdated(Patient aPatient) {
		//remember selected row
		// int row = grid.getSelectedRow();
		for (int i = 0; i < pPatient.size(); i++) {
			if ((pPatient.get(i).getPatient().getCode()).equals(aPatient.getCode())) {
				Admission admission = pPatient.get(i).getAdmission();
				pPatient.remove(i);
				pPatient.add(i, new AdmittedPatient(aPatient, admission));
				break;
			}
		}
		patients = data.getPatientList();
		grid.setItems(patients);
		lastKey = "";
		filterPatient(searchString.getValue());
		grid.select(aPatient);
		rowCounter.setCaption(rowCounterText + ": " + pPatient.size());
		searchString.focus();
		searchString.focus();
	}

	public AdmittedPatientBrowser() {
		logger = new Logging();
		setCaption(MessageBundle.getMessage("angal.admission.patientsbrowser"));
		this.windowContent = new VerticalLayout();
        setContent(this.windowContent);
        UI.getCurrent().addWindow(this);
		myFrame = this;
		if (!GeneralData.ENHANCEDSEARCH) {
			//Load the whole list of patients
			pPatient = manager.getAdmittedPatients(null);
		}
		initComponents();
		// pack();
		// setLocationRelativeTo(null);
		// setVisible(true);
		
		// rowCounter.setText(rowCounterText + ": " + pPatient.size());
		// searchString.requestFocus();

		// myFrame.addWindowListener(new WindowAdapter(){
			
		// 	public void windowClosing(WindowEvent e) {
		// 		//to free memory
		// 		if (pPatient != null) pPatient.clear();
		// 		// if (wardList != null) wardList.clear();
		// 		// dispose();
		// 	}			
		// });
	}

	private void initComponents() {
		getDataAndControlPanel();//topSubContent
		getButtonPanel();//botSubContent
	}

	private void getDataAndControlPanel() {
		HorizontalLayout topSubContent = new HorizontalLayout();
		topSubContent.setWidth("100%");
		this.windowContent.addComponent(topSubContent);
		getControlPanel(topSubContent);
		getScrollPane(topSubContent);//data panel
	}
	
	/*
	 * panel with filtering controls
	 */
	private void getControlPanel(HorizontalLayout layout) {

		Panel panel = new Panel();
		panel.setHeight("100%");
		layout.addComponent(panel);
		layout.setExpandRatio(panel,1);
		VerticalLayout subLayout = new VerticalLayout();
		panel.setContent(subLayout);


		// JPanel southPanel = new JPanel(new BorderLayout());

		searchString = new TextField(MessageBundle.getMessage("angal.admission.searchkey")+":");

		searchString.setMaxLength(15);
		if (GeneralData.ENHANCEDSEARCH) {
			searchString.addValueChangeListener(event -> {
				// int key = event.getValue();
				// if (key == KeyEvent.VK_ENTER) {
				// 	jSearchButton.click();
				// }
			});
		} else {
			searchString.addValueChangeListener(event -> {
				filterPatient(event.getValue());
			});
		}
		searchString.focus();
		subLayout.addComponent(searchString);
		// if (GeneralData.ENHANCEDSEARCH) subLayout.addComponent(getJSearchButton());

		rowCounter = new Label();
		rowCounter.setCaption(rowCounterText + ": " + pPatient.size());
		subLayout.addComponent(rowCounter);
	}

	private void getScrollPane(HorizontalLayout layout){
		Panel panel = new Panel();
		panel.setWidth("100%");
		layout.addComponent(panel);
		layout.setExpandRatio(panel,4);
		grid = new Grid<Patient>();//qqq
		grid.setWidth("100%");
		data = new AdmittedPatientBrowserModel(null);
		patients = data.getPatientList();
		grid.setItems(patients);
		grid.addColumn(Patient::getCode).setCaption("Code");
		grid.addColumn(Patient::getName).setCaption("Name");
		grid.addColumn(Patient::getAge).setCaption("Age");
		grid.addColumn(Patient::getSex).setCaption("Sex");
		grid.addColumn(Patient::getCity).setCaption("City");
		grid.addColumn(Patient::getAddress).setCaption("Address");
		grid.addColumn(Patient::getTelephone).setCaption("Telephone");
		grid.addColumn(Patient::getNote).setCaption("Note");
		
		// for (int i=0;i<pColums.length; i++){
		// 	grid.getColumnModel().getColumn(i).setMinWidth(pColumwidth[i]);
		// 	if (!pColumResizable[i]) grid.getColumnModel().getColumn(i).setMaxWidth(pColumwidth[i]);
		// }
		
		// grid.getColumnModel().getColumn(0).setCellRenderer(new CenterTableCellRenderer());
		// grid.getColumnModel().getColumn(2).setCellRenderer(new CenterTableCellRenderer());
		// grid.getColumnModel().getColumn(3).setCellRenderer(new CenterTableCellRenderer());

		// int tableWidth = 0;
		// for (int i = 0; i<pColumwidth.length; i++){
		// 	tableWidth += pColumwidth[i];
		// }
		
		panel.setContent(grid);
		// scrollPane.setPreferredSize(new Dimension(tableWidth+200, 200));
		// return scrollPane;
	}

	private void getButtonPanel() {
		HorizontalLayout botSubContent = new HorizontalLayout();
		this.windowContent.addComponent(botSubContent);
		if (MainMenu.checkUserGrants("btnadmnew")) botSubContent.addComponent(getButtonNew());
		if (MainMenu.checkUserGrants("btnadmedit")) botSubContent.addComponent(getButtonEdit());
		if (MainMenu.checkUserGrants("btnadmdel")) botSubContent.addComponent(getDeleteButton());
		// if (MainMenu.checkUserGrants("btnadmadm")) botSubContent.addComponent(getButtonAdmission());
		if (MainMenu.checkUserGrants("btnadmexamination")) botSubContent.addComponent(getExaminationButton());
		// if (GeneralData.OPDEXTENDED && MainMenu.checkUserGrants("btnadmopd")) botSubContent.addComponent(getButtonOpd());
		// if (MainMenu.checkUserGrants("btnadmbill")) botSubContent.addComponent(getButtonBill());
		// if (MainMenu.checkUserGrants("data")) botSubContent.addComponent(getButtonData());
		// if (MainMenu.checkUserGrants("btnadmpatientfolder")) botSubContent.addComponent(getButtonPatientFolderBrowser());
		// if (MainMenu.checkUserGrants("btnadmtherapy")) botSubContent.addComponent(getButtonTherapy());
		// if (GeneralData.MERGEFUNCTION && MainMenu.checkUserGrants("btnadmmer")) botSubContent.addComponent(getButtonMerge());
		// botSubContent.addComponent(getButtonClose());
	}
	
	private Button getExaminationButton() {
		if (examinationButton == null) {
			examinationButton = new Button(MessageBundle.getMessage("angal.opd.examination"));
			// examinationButton.setMnemonic(KeyEvent.VK_E);
			examinationButton.addClickListener(e->{
				if (grid.getSelectedItems().isEmpty()) {
					MessageBox.createInfo().withCaption(MessageBundle.getMessage("angal.admission.editpatient"))
					.withMessage(MessageBundle.getMessage("angal.common.pleaseselectarow"))
					.withOkButton().open();
					return;
				}
				Patient pat = ((Patient)grid.getSelectedItems().toArray()[0]);
				
				PatientExamination patex;
				ExaminationOperations examOperations = new ExaminationOperations();
				
				PatientExamination lastPatex = examOperations.getLastByPatID(pat.getCode());
				if (lastPatex != null) {
					patex = examOperations.getFromLastPatientExamination(lastPatex);
				} else {
					patex = examOperations.getDefaultPatientExamination(pat);
				}
				
				GenderPatientExamination gpatex = new GenderPatientExamination(patex, pat.getSex() == 'M');
				
				PatientExaminationEdit dialog = new PatientExaminationEdit(gpatex);
				// dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				// dialog.pack();
				// dialog.setLocationRelativeTo(null);
				// dialog.setVisible(true);
			});
		}
		return examinationButton;
	}
	private Button getButtonNew() {
		Button buttonNew = new Button(MessageBundle.getMessage("angal.admission.newpatient"));
		////buttonNew.setClickShortcut(KeyEvent.VK_N);
		buttonNew.addClickListener(e -> {
			if (GeneralData.PATIENTEXTENDED) {
				PatientInsertExtended newrecord = new PatientInsertExtended(new Patient(), true, this);
				newrecord.addPatientListener(this);
			// 	newrecord.setVisible(true);
			// } else {
			// 	PatientInsert newrecord = new PatientInsert(AdmittedPatientBrowser.this, new Patient(), true);
			// 	newrecord.addPatientListener(AdmittedPatientBrowser.this);
			// 	newrecord.setVisible(true);
			}
		});
		return buttonNew;
	}

	private Button getButtonEdit() {
		Button buttonEdit = new Button(MessageBundle.getMessage("angal.admission.editpatient"));
		////buttonEdit.setClickShortcut(KeyEvent.VK_E);
		buttonEdit.addClickListener(e-> {
			if (grid.getSelectedItems().isEmpty()) {
				MessageBox.createInfo().withCaption(MessageBundle.getMessage("angal.admission.editpatient")).withMessage(MessageBundle.getMessage("angal.common.pleaseselectarow"))
				.withOkButton().open();
				return;
			}
			// Patient patientArray = (Patient)grid.getSelectedItems().toArray()[0];
			// patient = patientArray[0];
			// patient = (AdmittedPatient) grid.getValueAt(grid.getSelectedRow(), -1);
			
			if (GeneralData.PATIENTEXTENDED) {
				PatientInsertExtended editrecord = new PatientInsertExtended((Patient)grid.getSelectedItems().toArray()[0], false, this);
				editrecord.addPatientListener(this);
				// editrecord.setVisible(true);
			} //else {
// 			// 	PatientInsert editrecord = new PatientInsert(AdmittedPatientBrowser.this, patient.getPatient(), false);
// 			// 	editrecord.addPatientListener(AdmittedPatientBrowser.this);
// 			// 	editrecord.setVisible(true);
			// }
		});
		return buttonEdit;
	}

	private Button getDeleteButton() {
		Button buttonDel = new Button(MessageBundle.getMessage("angal.admission.deletepatient"));
		////buttonDel.setClickShortcut(KeyEvent.VK_T);
		buttonDel.addClickListener(e-> {
			if (grid.getSelectedItems().isEmpty()) {
				MessageBox.createInfo().withCaption(MessageBundle.getMessage("angal.admission.deletepatient")).withMessage(MessageBundle.getMessage("angal.common.pleaseselectarow"))
				.withOkButton().open();
				return;
			}
			Patient pat = (Patient)grid.getSelectedItems().toArray()[0];
			MessageBox.createQuestion().withCaption(MessageBundle.getMessage("angal.admission.deletepatient"))
			.withMessage(MessageBundle.getMessage("angal.admission.deletepatient") + " " +pat.getName() + "?")
			.withYesButton(()-> {
				PatientBrowserManager manager = new PatientBrowserManager();
				boolean result = manager.deletePatient(pat);
				if (result){
					fireMyDeletedPatient(pat);
				}
			})
			.withNoButton(()-> {
			}).open();
		});
		return buttonDel;
	}

	// private JButton getButtonAdmission() {
	// 	JButton buttonAdmission = new JButton(MessageBundle.getMessage("angal.admission.admission"));
	// 	buttonAdmission.setMnemonic(KeyEvent.VK_A);
	// 	buttonAdmission.addActionListener(new ActionListener() {
	// 		public void actionPerformed(ActionEvent event) {
	// 			// if (grid.getSelectedRow() < 0) {
	// 			// 	JOptionPane.showMessageDialog(AdmittedPatientBrowser.this, MessageBundle.getMessage("angal.common.pleaseselectarow"),
	// 			// 			MessageBundle.getMessage("angal.admission.admission"), JOptionPane.PLAIN_MESSAGE);
	// 			// 	return;
	// 			// }
	// 			// patient = (AdmittedPatient) grid.getValueAt(grid.getSelectedRow(), -1);
				
	// 			// if (patient.getAdmission() != null) {
	// 			// 	// edit previous admission or dismission
	// 			// 	new AdmissionBrowser(myFrame, patient, true);
	// 			// } else {
	// 			// 	// new admission
	// 			// 	new AdmissionBrowser(myFrame, patient, false);
	// 			// }
	// 		}
	// 	});
	// 	return buttonAdmission;
	// }

	// private JButton getButtonOpd() {
	// 	JButton buttonOpd = new JButton(MessageBundle.getMessage("angal.admission.opd"));
	// 	buttonOpd.setMnemonic(KeyEvent.VK_O);
	// 	buttonOpd.addActionListener(new ActionListener() {
	// 		public void actionPerformed(ActionEvent event) {
	// 			// if (grid.getSelectedRow() < 0) {
	// 			// 	JOptionPane.showMessageDialog(AdmittedPatientBrowser.this, MessageBundle.getMessage("angal.common.pleaseselectarow"),
	// 			// 			MessageBundle.getMessage("angal.admission.opd"), JOptionPane.PLAIN_MESSAGE);
	// 			// 	return;
	// 			// }
	// 			// patient = (AdmittedPatient) grid.getValueAt(grid.getSelectedRow(), -1);
				
	// 			// if (patient  != null) {
	// 			// 	Opd opd = new Opd(0,' ',-1,"0",0);
	// 			// 	OpdEditExtended newrecord = new OpdEditExtended(myFrame, opd, patient.getPatient(), true);
	// 			// 	newrecord.setVisible(true);
					
	// 			// } /*else {
	// 				//new OpdBrowser(true);
	// 			// }*/
	// 		}
	// 	});
	// 	return buttonOpd;
	// }
	
	// private JButton getButtonBill() {
	// 	JButton buttonBill = new JButton(MessageBundle.getMessage("angal.admission.bill"));
	// 	buttonBill.setMnemonic(KeyEvent.VK_B);
	// 	buttonBill.addActionListener(new ActionListener() {
	// 		public void actionPerformed(ActionEvent event) {
	// 			// if (grid.getSelectedRow() < 0) {
	// 			// 	JOptionPane.showMessageDialog(AdmittedPatientBrowser.this, MessageBundle.getMessage("angal.common.pleaseselectarow"),
	// 			// 			MessageBundle.getMessage("angal.admission.bill"), JOptionPane.PLAIN_MESSAGE);
	// 			// 	return;
	// 			// }
	// 			// patient = (AdmittedPatient) grid.getValueAt(grid.getSelectedRow(), -1);
				
	// 			if (patient  != null) {
	// 				Patient pat = patient.getPatient();
	// 				// BillBrowserManager billManager = new BillBrowserManager();
	// 				// ArrayList<Bill> patientPendingBills = billManager.getPendingBills(pat.getCode());
	// 				// if (patientPendingBills.isEmpty()) {
	// 				// 	new PatientBillEdit(AdmittedPatientBrowser.this, pat);
	// 				// 	//dispose();
	// 				// } else {
	// 				// 	if (patientPendingBills.size() == 1) {
	// 				// 		JOptionPane.showMessageDialog(AdmittedPatientBrowser.this, MessageBundle.getMessage("angal.admission.thispatienthasapendingbill"),
	// 				// 				MessageBundle.getMessage("angal.admission.bill"), JOptionPane.PLAIN_MESSAGE);
	// 				// 		PatientBillEdit pbe = new PatientBillEdit(AdmittedPatientBrowser.this, patientPendingBills.get(0), false);
	// 				// 		pbe.setVisible(true);
	// 				// 		//dispose();
	// 				// 	} else {
	// 				// 		int ok = JOptionPane.showConfirmDialog(AdmittedPatientBrowser.this, MessageBundle.getMessage("angal.admission.thereismorethanonependingbillforthispatientcontinue"),
	// 				// 				MessageBundle.getMessage("angal.admission.bill"), JOptionPane.WARNING_MESSAGE);
	// 				// 		if (ok == JOptionPane.OK_OPTION) {
	// 				// 			new PatientBillEdit(AdmittedPatientBrowser.this, pat);
	// 				// 			//dispose();
	// 				// 		} else return;
	// 				// 	}
	// 				// } 
	// 			} /*else {
	// 				//new OpdBrowser(true);
	// 			}*/
	// 		}
	// 	});
	// 	return buttonBill;
	// }

	// private JButton getButtonData() {
	// 	JButton buttonData = new JButton(MessageBundle.getMessage("angal.admission.data"));
	// 	buttonData.setMnemonic(KeyEvent.VK_D);
	// 	buttonData.addActionListener(new ActionListener() {
	// 		public void actionPerformed(ActionEvent event) {
	// 			// if (grid.getSelectedRow() < 0) {
	// 			// 	JOptionPane.showMessageDialog(AdmittedPatientBrowser.this, MessageBundle.getMessage("angal.common.pleaseselectarow"),
	// 			// 			MessageBundle.getMessage("angal.admission.data"), JOptionPane.PLAIN_MESSAGE);
	// 			// 	return;
	// 			// }
	// 			// patient = (AdmittedPatient) grid.getValueAt(grid.getSelectedRow(), -1);
				
	// 			// PatientDataBrowser pdb = new PatientDataBrowser(myFrame, patient.getPatient());
	// 			// pdb.addDeleteAdmissionListener(myFrame);
	// 			// pdb.showAsModal(AdmittedPatientBrowser.this);
	// 		}
	// 	});
	// 	return buttonData;
	// }

	// private JButton getButtonPatientFolderBrowser() {
	// 	JButton buttonPatientFolderBrowser = new JButton(MessageBundle.getMessage("angal.admission.patientfolder"));
	// 	buttonPatientFolderBrowser.setMnemonic(KeyEvent.VK_S);
	// 	buttonPatientFolderBrowser.addActionListener(new ActionListener() {
	// 		public void actionPerformed(ActionEvent event) {
	// 			// if (grid.getSelectedRow() < 0) {
	// 			// 	JOptionPane.showMessageDialog(AdmittedPatientBrowser.this, MessageBundle.getMessage("angal.common.pleaseselectarow"),
	// 			// 			MessageBundle.getMessage("angal.admission.patientfolder"), JOptionPane.PLAIN_MESSAGE);
	// 			// 	return;
	// 			// }
	// 			// patient = (AdmittedPatient) grid.getValueAt(grid.getSelectedRow(), -1);
	// 			// new PatientFolderBrowser(myFrame, patient.getPatient()).showAsModal(AdmittedPatientBrowser.this);
	// 		}
	// 	});
	// 	return buttonPatientFolderBrowser;
	// }

	// private JButton getButtonTherapy() {
	// 	JButton buttonTherapy = new JButton(MessageBundle.getMessage("angal.admission.therapy"));
	// 	buttonTherapy.setMnemonic(KeyEvent.VK_T);
	// 	buttonTherapy.addActionListener(new ActionListener() {
	// 		public void actionPerformed(ActionEvent event) {
	// 			// if (grid.getSelectedRow() < 0) {
	// 			// 	JOptionPane.showMessageDialog(AdmittedPatientBrowser.this, MessageBundle.getMessage("angal.common.pleaseselectarow"),
	// 			// 			MessageBundle.getMessage("angal.admission.therapy"), JOptionPane.PLAIN_MESSAGE);
	// 			// 	return;
	// 			// }
	// 			// patient = (AdmittedPatient) grid.getValueAt(grid.getSelectedRow(), -1);
	// 			// TherapyEdit therapy = new TherapyEdit(AdmittedPatientBrowser.this, patient.getPatient(), patient.getAdmission() != null);
	// 			// therapy.setLocationRelativeTo(null);
	// 			// therapy.setVisible(true);
				
	// 		}
	// 	});
	// 	return buttonTherapy;
	// }

	// private JButton getButtonMerge() {
	// 	JButton buttonMerge = new JButton(MessageBundle.getMessage("angal.admission.merge"));
	// 	buttonMerge.setMnemonic(KeyEvent.VK_M);
	// 	buttonMerge.addActionListener(new ActionListener() {
	// 		public void actionPerformed(ActionEvent event) {
	// 			// if (grid.getSelectedRowCount() != 2) {
	// 			// 	JOptionPane.showMessageDialog(null, MessageBundle.getMessage("angal.admission.pleaseselecttwopatients"),
	// 			// 			MessageBundle.getMessage("angal.admission.merge"), JOptionPane.PLAIN_MESSAGE);
	// 			// 	return;
	// 			// }
				
	// 			// int[] indexes = grid.getSelectedRows();
				
	// 			// Patient mergedPatient;
	// 			// Patient patient1 = ((AdmittedPatient)grid.getValueAt(indexes[0], -1)).getPatient();
	// 			// Patient patient2 = ((AdmittedPatient)grid.getValueAt(indexes[1], -1)).getPatient();
				
	// 			// //MergePatient mergedPatient = new MergePatient(patient1, patient2);
				
	// 			// if (patient1.getSex() != patient2.getSex()) {
	// 			// 	JOptionPane.showMessageDialog(null, MessageBundle.getMessage("angal.admission.selectedpatientshavedifferentsex"),
	// 			// 			MessageBundle.getMessage("angal.admission.merge"), JOptionPane.WARNING_MESSAGE);
	// 			// 	return;
	// 			// }
				
	// 			// //Select most recent patient
	// 			// if (patient1.getCode() > patient2.getCode()) { 
	// 			// 	mergedPatient = patient1;
	// 			// }
	// 			// else { 
	// 			// 	mergedPatient = patient2;
	// 			// 	patient2 = patient1;
	// 			// }
	// 			//System.out.println("mergedPatient: " + mergedPatient.getCode());

	// 			//ASK CONFIRMATION
	// 			// int ok = JOptionPane.showConfirmDialog(null, 
	// 			// 		MessageBundle.getMessage("angal.admission.withthisoperationthepatient")+"\n"+MessageBundle.getMessage("angal.common.code")+": "+
	// 			// 		patient2.getCode() + " " + patient2.getName() + " " + patient2.getAge() + " " + patient2.getAddress() +"\n"+
	// 			// 		MessageBundle.getMessage("angal.admission.willbedeletedandhisherhistorytransferedtothepatient")+"\n"+MessageBundle.getMessage("angal.common.code")+": "+
	// 			// 		mergedPatient.getCode() + " " + mergedPatient.getName() + " " + mergedPatient.getAge() + " " + mergedPatient.getAddress() +"\n"+
	// 			// 		MessageBundle.getMessage("angal.admission.continue"),
	// 			// 		MessageBundle.getMessage("angal.admission.merge"), 
	// 			// 		JOptionPane.YES_NO_OPTION);
	// 			// if (ok != JOptionPane.YES_OPTION) return;
				
	// 			// if (mergedPatient.getName().toUpperCase().compareTo(
	// 			// 		patient2.getName().toUpperCase()) != 0) {
	// 			// 	String[] names = {mergedPatient.getName(), patient2.getName()};
	// 			// 	String whichName = (String) JOptionPane.showInputDialog(null, 
	// 			// 			MessageBundle.getMessage("angal.admission.pleaseselectthefinalname"), 
	// 			// 			MessageBundle.getMessage("angal.admission.differentnames"), 
	// 			// 			JOptionPane.INFORMATION_MESSAGE, 
	// 			// 			null, 
	// 			// 			names, 
	// 			// 			null);
	// 			// 	if (whichName == null) return;
	// 			// 	if (whichName.compareTo(names[1]) == 0) {
	// 			// 		//patient2 name selected
	// 			// 		mergedPatient.setFirstName(patient2.getFirstName());
	// 			// 		mergedPatient.setSecondName(patient2.getSecondName());
	// 			// 	}
	// 			// }
	// 			// if (mergedPatient.getBirthDate() != null &&
	// 			// 		mergedPatient.getAgetype().compareTo("") == 0) {
	// 			// 	//mergedPatient only Age
	// 			// 	Date bdate2 = patient2.getBirthDate();
	// 			// 	int age2 = patient2.getAge();
	// 			// 	String ageType2 = patient2.getAgetype();
	// 			// 	if (bdate2 != null) {
	// 			// 		//patient2 has BirthDate
	// 			// 		mergedPatient.setAge(age2);
	// 			// 		mergedPatient.setBirthDate(bdate2);
	// 			// 	}
	// 			// 	if (bdate2 != null && ageType2.compareTo("") != 0) {
	// 			// 		//patient2 has AgeType 
	// 			// 		mergedPatient.setAge(age2);
	// 			// 		mergedPatient.setAgetype(ageType2);
	// 			// 	}
	// 			// }
				
	// 			// if (mergedPatient.getAddress().compareTo("") == 0)
	// 			// 	mergedPatient.setAddress(patient2.getAddress());
				
	// 			// if (mergedPatient.getCity().compareTo("") == 0)
	// 			// 	mergedPatient.setCity(patient2.getCity());
				
	// 			// if (mergedPatient.getNextKin().compareTo("") == 0)
	// 			// 	mergedPatient.setNextKin(patient2.getNextKin());
				
	// 			// if (mergedPatient.getTelephone().compareTo("") == 0)
	// 			// 	mergedPatient.setTelephone(patient2.getTelephone());
				
	// 			// if (mergedPatient.getMother_name().compareTo("") == 0)
	// 			// 	mergedPatient.setMother_name(patient2.getMother_name());
				
	// 			// if (mergedPatient.getMother() == 'U')
	// 			// 	mergedPatient.setMother(patient2.getMother());
				
	// 			// if (mergedPatient.getFather_name().compareTo("") == 0)
	// 			// 	mergedPatient.setFather_name(patient2.getFather_name());
				
	// 			// if (mergedPatient.getFather() == 'U')
	// 			// 	mergedPatient.setFather(patient2.getFather());
				
	// 			// if (mergedPatient.getBloodType().compareTo("") == 0)
	// 			// 	mergedPatient.setBloodType(patient2.getBloodType());
				
	// 			// if (mergedPatient.getHasInsurance() == 'U')
	// 			// 	mergedPatient.setHasInsurance(patient2.getHasInsurance());
				
	// 			// if (mergedPatient.getParentTogether() == 'U')
	// 			// 	mergedPatient.setParentTogether(patient2.getParentTogether());
				
	// 			// if (mergedPatient.getNote().compareTo("") == 0)
	// 			// 	mergedPatient.setNote(patient2.getNote());
	// 			// else {
	// 			// 	String note = mergedPatient.getNote();
	// 			// 	mergedPatient.setNote(patient2.getNote()+"\n\n"+note);
	// 			// }

	// 			// PatientBrowserManager patManager = new PatientBrowserManager();
	// 			// if (patManager.mergePatientHistory(mergedPatient, patient2)) {
	// 			// 	fireMyDeletedPatient(patient2);
	// 			// }
	// 		}
	// 	});
	// 	return buttonMerge;
	// }

	// private JButton getButtonClose() {
	// 	JButton buttonClose = new JButton(MessageBundle.getMessage("angal.common.close"));
	// 	buttonClose.setMnemonic(KeyEvent.VK_C);
	// 	buttonClose.addActionListener(new ActionListener() {
	// 		public void actionPerformed(ActionEvent event) {
	// 			//to free Memory
	// 			if (pPatient != null) pPatient.clear();
	// 			// if (wardList != null) wardList.clear();
	// 			// dispose();
	// 		}
	// 	});
	// 	return buttonClose;
	// }
	
	private void filterPatient(String key) {
		data = new AdmittedPatientBrowserModel(key);
		patients = data.getPatientList();
		grid.setItems(patients);
		rowCounter.setCaption(rowCounterText + ": " + data.getRowCount());
		// searchString.requestFocus();
	}
	
	private void searchPatient() {
		// String key = searchString.getText();
		// if (key.equals("")) {
			// int ok = JOptionPane.showConfirmDialog(AdmittedPatientBrowser.this, 
			// 		MessageBundle.getMessage("angal.admission.thiscouldretrievealargeamountofdataproceed"),
			// 		MessageBundle.getMessage("angal.hospital"),
			// 		JOptionPane.OK_CANCEL_OPTION);
			// if (ok != JOptionPane.OK_OPTION) return;
		// }
		// pPatient = manager.getAdmittedPatients(key);
		// filterPatient(null);
	}
	
	// private Button getJSearchButton() {
	// 	if (jSearchButton == null) {
	// 		jSearchButton = new Button();
	// 		// jSearchButton.setIcon(new ImageIcon("rsc/icons/zoom_r_button.png"));
	// 		// jSearchButton.setPreferredSize(new Dimension(20, 20));
	// 		jSearchButton.addClickListener(e -> {
	// 			((Button) e.getSource()).setEnabled(false);
	// 			SwingUtilities.invokeLater(new Runnable() {
	// 				public void run() {
	// 					searchPatient();
	// 					EventQueue.invokeLater(new Runnable() {
	// 						public void run() {
	// 							((JButton) e.getSource()).setEnabled(true);
	// 						}
	// 					});
	// 				}
	// 			});
	// 		});
	// 	}
	// 	return jSearchButton;
	// }
	
	// private JPanel setMyBorder(JPanel c, String title) {
	// 	javax.swing.border.Border b2 = BorderFactory.createCompoundBorder(
	// 			BorderFactory.createTitledBorder(title), BorderFactory
	// 					.createEmptyBorder(0, 0, 0, 0));
	// 	c.setBorder(b2);
	// 	return c;
	// }

	class AdmittedPatientBrowserModel{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		ArrayList<AdmittedPatient> patientList = new ArrayList<AdmittedPatient>();
		
		public AdmittedPatientBrowserModel(String key) {//qqq
			for (AdmittedPatient ap : pPatient) {
				Admission adm = ap.getAdmission();
				// if not admitted stripes admitted
				// if (((String) patientClassBox.getSelectedItem())
				// 		.equals(patientClassItems[2])) {
				// 	if (adm != null)
				// 		continue;
				// }
				// // if admitted stripes not admitted
				// else if (((String) patientClassBox.getSelectedItem())
				// 		.equals(patientClassItems[1])) {
				// 	if (adm == null)
				// 		continue;
				// }

				// if all or admitted filters not matching ward
				// if (!((String) patientClassBox.getSelectedItem())
				// 		.equals(patientClassItems[2])) {
				// 	// if (adm != null) {
				// 	// 	int cc = -1;
				// 	// 	for (int j = 0; j < wardList.size(); j++) {
				// 	// 		if (adm.getWardId().equalsIgnoreCase(
				// 	// 				wardList.get(j).getCode())) {
				// 	// 			cc = j;
				// 	// 			break;
				// 	// 		}
				// 	// 	}
				// 	// 	if (!wardCheck[cc].isSelected())
				// 	// 		continue;
				// 	// }
				// }

				if (key != null) {
					
					String s = key + lastKey;
					s.trim();
					String[] tokens = s.split(" ");

					if (!s.equals("")) {
						String name = ap.getPatient().getSearchString();
						int a = 0;
						for (int j = 0; j < tokens.length ; j++) {
							String token = tokens[j].toLowerCase();
							if (NormalizeString.normalizeContains(name, token)) {
								a++;
							}
						}
						if (a == tokens.length) patientList.add(ap);
					} else patientList.add(ap);
				} else patientList.add(ap);
			}
		}

		public int getRowCount() {
			if (patientList == null)
				return 0;
			return patientList.size();
		}

		public String getColumnName(int c) {
			return pColums[c];
		}

		public int getColumnCount() {
			return pColums.length;
		}

		public List<Patient> getPatientList(){
			List<Patient> qPatient=new ArrayList<Patient>();
			AdmittedPatient admPat;
			for(int i = 0; i < this.getRowCount(); i++){
				admPat = patientList.get(i);
				qPatient.add(admPat.getPatient());
			}
			return qPatient;
		}

		public Object getValueAt(int r, int c) {
			AdmittedPatient admPat = patientList.get(r);
			Patient patient = admPat.getPatient();
			Admission admission = admPat.getAdmission();
			if (c == -1) {
				return admPat;
			} else if (c == 0) {
				return patient.getCode();
			} else if (c == 1) {
				return patient.getName();
			} else if (c == 2) {
				return TimeTools.getFormattedAge(patient.getBirthDate());
			} else if (c == 3) {
				return patient.getSex();
			} else if (c == 4) {
				return patient.getInformations();
			} else if (c == 5) {
				if (admission == null) {
					return new String("");
				} else {
					// for (int i = 0; i < wardList.size(); i++) {
					// 	if (wardList.get(i).getCode()
					// 			.equalsIgnoreCase(admission.getWardId())) {
					// 		return wardList.get(i).getDescription();
					// 	}
					// }
					return new String("?");
				}
			}

			return null;
		}

		// @Override
		// public boolean isCellEditable(int arg0, int arg1) {
		// 	return false;
		// }
	}
	
	
	// class CenterTableCellRenderer extends DefaultTableCellRenderer {  
		   
	// 	/**
	// 	 * 
	// 	 */
	// 	private static final long serialVersionUID = 1L;

	// 	public Component getTableCellRendererComponent(JTable grid, Object value, boolean isSelected, 
	// 			boolean hasFocus, int row, int column) {  
		   
	// 		Component cell=super.getTableCellRendererComponent(grid,value,isSelected,hasFocus,row,column);
	// 		cell.setForeground(Color.BLACK);
	// 		setHorizontalAlignment(CENTER);	   
	// 		return cell;
	//    }
	// }

}
