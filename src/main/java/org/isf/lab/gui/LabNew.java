package org.isf.lab.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import java.io.File;

import com.vaadin.server.FileResource;
import com.vaadin.server.VaadinService;
import de.steinwedel.messagebox.MessageBox;

import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateTimeField;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import org.isf.admission.manager.AdmissionBrowserManager;
import org.isf.admission.model.Admission;
import org.isf.exa.manager.ExamBrowsingManager;
import org.isf.exa.manager.ExamRowBrowsingManager;
import org.isf.exa.model.Exam;
import org.isf.exa.model.ExamRow;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.lab.manager.LabManager;
import org.isf.lab.model.Laboratory;
import org.isf.patient.gui.SelectPatient;
import org.isf.patient.gui.SelectPatient.SelectionListener;
import org.isf.patient.model.Patient;
import org.isf.utils.time.RememberDates;
import org.isf.utils.jobjects.ModalWindow;
import org.isf.utils.Logging;

import com.toedter.calendar.JDateChooser;

public class LabNew extends ModalWindow implements SelectionListener {

//LISTENER INTERFACE --------------------------------------------------------
	private List<LabListener> labListeners = new ArrayList<LabListener>();
	
	public interface LabListener{
		public void labInserted();
	}
	
	public void addLabListener(LabListener l) {
		labListeners.add(l);
	}
	
	private void fireLabInserted(){
		for(LabListener labListener : labListeners)
			labListener.labInserted();
	}
//---------------------------------------------------------------------------
	
	private Logging logger = new Logging();
	public void patientSelected(Patient patient) {
		patientSelected = patient;
		//INTERFACE
		patientTextField.setValue(patientSelected.getName());
		patientTextField.setEnabled(false);
		pickPatientButton.setCaption(MessageBundle.getMessage("angal.labnew.changepatient")); //$NON-NLS-1$
		pickPatientButton.setDescription(MessageBundle.getMessage("angal.labnew.tooltip.changethepatientassociatedwiththisexams")); //$NON-NLS-1$
		trashPatientButton.setEnabled(true);
		// inOut = getIsAdmitted();
		// if (inOut.equalsIgnoreCase("R")) jRadioButtonOPD.setSelected(true);
		// else jRadioButtonIPD.setSelected(true);
	}
	private String resPath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
	private static final long serialVersionUID = 1L;
	private Grid<Laboratory> examsGrid;
	private JScrollPane jScrollPaneTable;
	private VerticalLayout topLayout;
	private Button removeExamButton;
	private Button addExamButton;
	private HorizontalLayout examButtonsLayout;
	private VerticalLayout rightLayout;
	private HorizontalLayout bottomLayout;
	private FormLayout dateLayout;
	private HorizontalLayout patientLayout;
	private Label patientLabel;
	private TextField patientTextField;
	private Button pickPatientButton;
	private Button trashPatientButton;
	private DateTimeField date;
	private Panel materialPanel;
	private ComboBox materialComboBox;
	private ComboBox examResultComboBox;
	private Panel resultsPanel;
	private Panel notePanel;
	private HorizontalLayout buttonsLayout;
	private Button okButton;
	private Button cancelButton;
	private TextArea noteTextArea;
	private JScrollPane jScrollPaneNote;
	private JRadioButton jRadioButtonOPD;
	private JRadioButton jRadioButtonIPD;
	private ButtonGroup radioGroup;
	private JPanel jOpdIpdPanel;
	private String inOut;
	
	private static final Dimension PatientDimension = new Dimension(200,20);
	private static final Dimension LabelDimension = new Dimension(50,20);
	//private static final Dimension ResultDimensions = new Dimension(200,200);
	//private static final Dimension MaterialDimensions = new Dimension(150,20);
	//private static final Dimension TextAreaNoteDimension = new Dimension(500, 50);
	private static final int EastWidth = 200;
	private static final int ComponentHeight = 20;
	private static final int ResultHeight = 200;
	//private static final int ButtonHeight = 25;
	
	private Object[] examClasses = {Exam.class, String.class};
	private String[] examColumnNames = {MessageBundle.getMessage("angal.labnew.exam"), MessageBundle.getMessage("angal.labnew.result")}; //$NON-NLS-1$ //$NON-NLS-2$
	private int[] examColumnWidth = {200, 150};
	private boolean[] examResizable = {true, false};
	private String[] matList = {
			MessageBundle.getMessage("angal.lab.blood"), 
			MessageBundle.getMessage("angal.lab.urine"),
			MessageBundle.getMessage("angal.lab.stool"),
			MessageBundle.getMessage("angal.lab.sputum"),
			MessageBundle.getMessage("angal.lab.cfs"),
			MessageBundle.getMessage("angal.lab.swabs"),
			MessageBundle.getMessage("angal.lab.tissues")
	};

	//TODO private boolean modified;
	private Patient patientSelected = null;
	private Laboratory selectedLab = null;
	
	//Exams (ALL)
	ExamBrowsingManager exaManager = new ExamBrowsingManager();
	ArrayList<Exam> exaArray = exaManager.getExams();
	
	//Results (ALL)
	ExamRowBrowsingManager examRowManager = new ExamRowBrowsingManager();
	ArrayList<ExamRow> exaRowArray = examRowManager.getExamRow();
	
	//Arrays for this Patient
	ArrayList<ArrayList<String>> examResults = new ArrayList<ArrayList<String>>();
	ArrayList<Laboratory> examItems = new ArrayList<Laboratory>();
	
	public LabNew(){
		// super(owner, true);
		initComponents();
		// setLocationRelativeTo(null);
		// setDefaultCloseOperation(LabNew.DISPOSE_ON_CLOSE);
		setCaption(MessageBundle.getMessage("angal.labnew.title"));
		//setVisible(true);
	}

	private void initComponents(){
		showAsModal();
		UI.getCurrent().addWindow(this);
		VerticalLayout windowContent = new VerticalLayout();
		setContent(windowContent);

		HorizontalLayout centralLayout = new HorizontalLayout();
		centralLayout.setWidthUndefined();
		centralLayout.setHeight("300px");
		centralLayout.addComponent(getExamsGrid());
		centralLayout.addComponent(getRightLayout());

		windowContent.addComponent(getTopLayout());
		windowContent.addComponent(centralLayout);
		windowContent.addComponent(getNotePanel());
		windowContent.addComponent(getBottomLayout());
		// pack();
	}

	private TextArea getNoteTextArea(){
		if (noteTextArea == null) {
			noteTextArea = new TextArea();//3 row 50 columns
			noteTextArea.setValue("");
			noteTextArea.setWidth("100%");
			//noteTextArea.setPreferredSize(TextAreaNoteDimension);
		}
		return noteTextArea;
	}

	private Button getCancelButton(){
		if (cancelButton == null) {
			cancelButton = new Button();
			cancelButton.setCaption(MessageBundle.getMessage("angal.common.cancel"));
			////cancelButton.setClickShortcut(KeyEvent.VK_C);
			cancelButton.addClickListener(e->{
				close();
			});
		}
		return cancelButton;
	}

	private Button getOkButton(){
		if (okButton == null) {
			okButton = new Button();
			okButton.setCaption(MessageBundle.getMessage("angal.common.ok"));
			////okButton.setClickShortcut(KeyEvent.VK_O);
			okButton.addClickListener(e->{
				//Check Results
				if(examItems.size() == 0){
					MessageBox.createError().withCaption("Error").withMessage(MessageBundle.getMessage("angal.labnew.noexamsinserted")).open();
					return;
				}
				for (Laboratory lab : examItems) {	
					if (lab.getResult() == null) {
						MessageBox.createError().withCaption("Error").withMessage(MessageBundle.getMessage("angal.labnew.someexamswithoutresultpleasecheck")).open();
						return;
					}
				}
				//Check Patient
				if (patientSelected == null) { 
					MessageBox.createError().withCaption("Error").withMessage(MessageBundle.getMessage("angal.labnew.pleaseselectapatient")).open();
					return;
				}
				//Check Date
				if (date.getValue() == null) {
					MessageBox.createError().withCaption("Error").withMessage(MessageBundle.getMessage("angal.labnew.pleaseinsertadate")).open();
					return;
				}
				//CREATING DB OBJECT
				GregorianCalendar newDate = new GregorianCalendar();
				newDate.setTimeInMillis((Date.from(date.getValue().atZone(ZoneId.systemDefault()).toInstant())).getTime());
				RememberDates.setLastLabExamDate(newDate);
				// String inOut = jRadioButtonOPD.isSelected() ? "R" : "I";
				String inOut = "R";
				Laboratory labOne = (Laboratory)examsGrid.getSelectedItems().toArray()[0];
				labOne.setNote(noteTextArea.getValue().trim()); //Workaround if Note typed just before saving
				
				for (Laboratory lab : examItems) {
					
					lab.setAge(patientSelected.getAge());
					lab.setDate(newDate);
					lab.setExamDate(newDate);
					lab.setInOutPatient(inOut);
					lab.setPatId(patientSelected.getCode());
					lab.setPatName(patientSelected.getName());
					lab.setSex(patientSelected.getSex()+"");
				}
				
				boolean result = false;
				LabManager labManager = new LabManager();
				Laboratory lab;
				for (int i = 0; i < examItems.size(); i++) {
					
					lab = examItems.get(i);
					if (lab.getExam().getProcedure() == 1) {
						result = labManager.newLabFirstProcedure(lab);
					} else {
						result = labManager.newLabSecondProcedure(lab, examResults.get(i));
					}
					if (!result){
						MessageBox.createError().withMessage(MessageBundle.getMessage("angal.labnew.thedatacouldnotbesaved")).open();
						return;
					}
				}
				fireLabInserted();
				close();
			});
		}
		return okButton;
	}
	
	private String getIsAdmitted(){
		AdmissionBrowserManager man = new AdmissionBrowserManager();
		Admission adm = new Admission();
		adm = man.getCurrentAdmission(patientSelected);
		return (adm==null?"R":"I");					
	}

	private HorizontalLayout getButtonsLayout(){
		if (buttonsLayout == null) {
			buttonsLayout = new HorizontalLayout();
			buttonsLayout.addComponent(getOkButton());
			buttonsLayout.addComponent(getCancelButton());
		}
		return buttonsLayout;
	}

	private Panel getNotePanel(){
		if (notePanel == null) {
			notePanel = new Panel(MessageBundle.getMessage("angal.labnew.note"));
			notePanel.setContent(getNoteTextArea());
		}
		return notePanel;
	}

	private Panel getResultsPanel(){
		if (resultsPanel == null) {
			resultsPanel = new Panel(MessageBundle.getMessage("angal.labnew.result"));
			resultsPanel.setHeight("100%");
			// resultsPanel.setPreferredSize(new Dimension(EastWidth, ResultHeight));
		} else {
			// resultsPanel.getContent().removeFromParent();
			// int selectedRow = examsGrid.getSelectedRow();
			try{final Laboratory selectedLab = (Laboratory)examsGrid.getSelectedItems().toArray()[0];}
			catch(Exception e){}
			Exam selectedExam = selectedLab.getExam();
			
			if (selectedExam.getProcedure() == 1) {
				examResultComboBox = new ComboBox();
				// examResultComboBox.setMaximumSize(new Dimension(EastWidth, ComponentHeight));
				// examResultComboBox.setMinimumSize(new Dimension(EastWidth, ComponentHeight));
				// examResultComboBox.setPreferredSize(new Dimension(EastWidth, ComponentHeight));
				ArrayList<String> tempArray = new ArrayList();
				for (ExamRow exaRow : exaRowArray) {
					if (selectedExam.getCode().compareTo(exaRow.getExamCode()) == 0) {
						// examResultComboBox.addItem(exaRow.getDescription());
						tempArray.add(exaRow.getDescription());
					}
				}
				examResultComboBox.setItems(tempArray.toArray());
				examResultComboBox.setSelectedItem(selectedLab.getResult());
				examResultComboBox.addValueChangeListener(e->{
					selectedLab.setResult(examResultComboBox.getSelectedItem().get().toString());
					examsGrid.setItems(examItems);
				});
				resultsPanel.setContent(examResultComboBox);
			} else {
				// resultsPanel.getContent().removeFromParent();
				// resultsPanel.setLayout(new GridLayout(14,1));
				VerticalLayout tempLayout = new VerticalLayout();
				resultsPanel.setContent(tempLayout);
				try{
					ArrayList<String> checking = examResults.get(examItems.indexOf((Laboratory)examsGrid.getSelectedItems().toArray()[0]));
					boolean checked;
					for (ExamRow exaRow : exaRowArray) {
						if (selectedExam.getCode().compareTo(exaRow.getExamCode()) == 0) {
							checked = false;
							if (checking.contains(exaRow.getDescription()))
								checked = true;
							tempLayout.addComponent(new CustomCheckBox(exaRow, checked));
						}
					}
				}
				catch(Exception ee){}
			}
		}
		return resultsPanel;
	}
	
	public class CustomCheckBox extends CheckBox {
		
		/**
		 * 
		 */
		private CheckBox check = this;
		
		public CustomCheckBox(ExamRow exaRow, boolean checked) {
			this.setCaption(exaRow.getDescription());
			this.setValue(checked);
			this.addValueChangeListener(e->{
				if (check.getValue()) {
					examResults.get(examItems.indexOf((Laboratory)examsGrid.getSelectedItems().toArray()[0])).add(e.getComponent().getCaption());
				} else {
					examResults.get(examItems.indexOf((Laboratory)examsGrid.getSelectedItems().toArray()[0])).remove(e.getComponent().getCaption());
				}
			});
		}
	}

	private ComboBox getMaterialComboBox(){
		if (materialComboBox == null) {
			materialComboBox = new ComboBox();
			materialComboBox.setWidth("100%");
			materialComboBox.setItems(matList);
			// materialComboBox.setPreferredSize(new Dimension(EastWidth, ComponentHeight));
			// materialComboBox.setMaximumSize(new Dimension(EastWidth, ComponentHeight));
			materialComboBox.setEnabled(false);
		}
		return materialComboBox;
	}

	private Panel getMaterialPanel(){
		if (materialPanel == null) {
			materialPanel = new Panel(MessageBundle.getMessage("angal.labnew.material"));
			materialPanel.setHeightUndefined();
			materialPanel.setContent(getMaterialComboBox());
		}
		return materialPanel;
	}
	
	private JPanel getJOpdIpdPanel(){
		if (jOpdIpdPanel == null) {
			jOpdIpdPanel = new JPanel();
			
			jRadioButtonOPD = new JRadioButton("OPD");
			jRadioButtonIPD = new JRadioButton("IP");
			
			radioGroup = new ButtonGroup();
			radioGroup.add(jRadioButtonOPD);
			radioGroup.add(jRadioButtonIPD);
			
			jOpdIpdPanel.add(jRadioButtonOPD);
			jOpdIpdPanel.add(jRadioButtonIPD);
			
			jRadioButtonOPD.setSelected(true);
		}
		return jOpdIpdPanel;
	}

	private Button getTrashPatientButton(){
		if (trashPatientButton == null) {
			trashPatientButton = new Button();
			////trashPatientButton.setClickShortcut(KeyEvent.VK_R);
			// trashPatientButton.setPreferredSize(new Dimension(25,25));
			trashPatientButton.setIcon(new FileResource(new File(resPath+"/WEB-INF/icons/remove_patient_button.png"))); //$NON-NLS-1$
			trashPatientButton.setDescription(MessageBundle.getMessage("angal.labnew.tooltip.removepatientassociationwiththisexam")); //$NON-NLS-1$
			trashPatientButton.addClickListener(e->{
				patientSelected = null;
				//INTERFACE
				patientTextField.setValue(""); //$NON-NLS-1$
				patientTextField.setEnabled(false);
				pickPatientButton.setCaption(MessageBundle.getMessage("angal.labnew.pickpatient"));
				pickPatientButton.setDescription(MessageBundle.getMessage("angal.labnew.tooltip.associateapatientwiththisexam")); //$NON-NLS-1$
				trashPatientButton.setEnabled(false);
			});
		}
		return trashPatientButton;
	}

	private Button getPickPatientButton(){
		if (pickPatientButton == null) {
			pickPatientButton = new Button();
			pickPatientButton.setCaption(MessageBundle.getMessage("angal.labnew.pickpatient"));  //$NON-NLS-1$
			////pickPatientButton.setClickShortcut(KeyEvent.VK_P);
			pickPatientButton.setIcon(new FileResource(new File(resPath+"/WEB-INF/icons/pick_patient_button.png"))); //$NON-NLS-1$
			pickPatientButton.setDescription(MessageBundle.getMessage("angal.labnew.tooltip.associateapatientwiththisexam"));  //$NON-NLS-1$
			pickPatientButton.addClickListener(e->{
				SelectPatient sp = new SelectPatient(patientSelected);
				sp.addSelectionListener(this);
				// sp.pack();
				// sp.setVisible(true);
			});
		}
		return pickPatientButton;
	}

	private TextField getPatientTextField(){
		if (patientTextField == null) {
			patientTextField = new TextField();
			patientTextField.setValue(""); //$NON-NLS-1$
			// patientTextField.setPreferredSize(PatientDimension);
			patientTextField.setEnabled(false);
		}
		return patientTextField;
	}

	private Label getPatientLabel(){
		if (patientLabel == null) {
			patientLabel = new Label();
			patientLabel.setValue("Patient");
			// patientLabel.setPreferredSize(LabelDimension);
		}
		return patientLabel;
	}

	private HorizontalLayout getPatientLayout(){
		if (patientLayout == null) {
			patientLayout = new HorizontalLayout();
			patientLayout.addComponent(getPatientLabel());
			patientLayout.addComponent(getPatientTextField());
			patientLayout.addComponent(getPickPatientButton());
			patientLayout.addComponent(getTrashPatientButton());
			// patientLayout.addComponent(getJOpdIpdPanel());
		}
		return patientLayout;
	}

	private FormLayout getDateLayout(){
		if (dateLayout == null) {
			dateLayout = new FormLayout();
			dateLayout.setMargin(false);
			dateLayout.addComponent(getDate());
		}
		return dateLayout;
	}

	private DateTimeField getDate(){
		if (date == null) {
			date = new DateTimeField(null,LocalDateTime.ofInstant(RememberDates.getLastLabExamDateGregorian().getTime().toInstant(), ZoneId.systemDefault())); //To remind last used
			date.setCaption("Date");
			date.setLocale(new Locale(GeneralData.LANGUAGE));
			date.setDateFormat("dd/MM/yy (HH:mm:ss)"); //$NON-NLS-1$
		}
		return date;
	}
	
	private HorizontalLayout getBottomLayout(){
		if (bottomLayout == null) {
			bottomLayout = new HorizontalLayout();
			// bottomLayout.setLayout(new BoxLayout(bottomLayout, BoxLayout.Y_AXIS));
			bottomLayout.addComponent(getButtonsLayout());
		}
		return bottomLayout;
	}

	private VerticalLayout getRightLayout(){
		if (rightLayout == null) {
			rightLayout = new VerticalLayout();
			rightLayout.setHeight("100%");
			rightLayout.setWidthUndefined();
			rightLayout.setMargin(false);
			rightLayout.addComponent(getExamsButtonLayout());
			rightLayout.addComponent(getMaterialPanel());
			rightLayout.addComponent(getResultsPanel());
			rightLayout.setExpandRatio(resultsPanel,1);
		}
		return rightLayout;
	}

	private VerticalLayout getTopLayout(){
		if (topLayout == null) {
			topLayout = new VerticalLayout();
			topLayout.setMargin(false);
			topLayout.addComponent(getDateLayout());
			topLayout.addComponent(getPatientLayout());
		}
		return topLayout;
	}

	private Grid getExamsGrid(){
		if (examsGrid == null) {
			examsGrid = new Grid();
			examsGrid.setHeight("100%");
			// examsGrid.setModel(new ExamTableModel());
			examsGrid.setItems(examItems);
			examsGrid.addColumn(Laboratory::getDescription).setCaption("Exam");
			examsGrid.addColumn(Laboratory::getResult).setCaption("Result");
			// for (int i = 0; i < examColumnWidth.length; i++) {
				
			// 	examsGrid.getColumnModel().getColumn(i).setMinWidth(examColumnWidth[i]);
			// 	if (!examResizable[i]) examsGrid.getColumnModel().getColumn(i).setMaxWidth(examColumnWidth[i]);
			// }
			
			// examsGrid.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			// ListSelectionModel listSelectionModel = examsGrid.getSelectionModel();
			examsGrid.addItemClickListener(e->{
				//SAVE PREVIOUS EXAM SELECTED 
				if (selectedLab != null){
					selectedLab.setNote(noteTextArea.getValue().trim());
					selectedLab.setMaterial((String)materialComboBox.getValue());
				}
				//SHOW NEW EXAM SELECTED
				selectedLab = (Laboratory)e.getItem();
				materialComboBox.setValue(selectedLab.getMaterial());
				try{
					noteTextArea.setValue(selectedLab.getNote());
				}
				catch(Exception ee){
					// ee.printStackTrace();
				}
				resultsPanel = getResultsPanel();
				materialComboBox.setEnabled(true);
				
				//modified = false;
				// validate();
				// repaint();
			});
			examsGrid.addSelectionListener(e->{
				//SAVE PREVIOUS EXAM SELECTED 
				if (selectedLab != null){
					selectedLab.setNote(noteTextArea.getValue().trim());
					selectedLab.setMaterial((String)materialComboBox.getValue());
				}
				//SHOW NEW EXAM SELECTED
				try{
					selectedLab = (Laboratory)e.getAllSelectedItems().toArray()[0];
					materialComboBox.setValue(selectedLab.getMaterial());
					noteTextArea.setValue(selectedLab.getNote());
				}
				catch(Exception ee){
					// ee.printStackTrace();
				}
				resultsPanel = getResultsPanel();
				materialComboBox.setEnabled(true);
				
				//modified = false;
				// validate();
				// repaint();
			});
		}
		return examsGrid;
	}
	
	public HorizontalLayout getExamsButtonLayout(){
		if(examButtonsLayout == null) {
			examButtonsLayout = new HorizontalLayout();
			examButtonsLayout.setHeightUndefined();
			examButtonsLayout.setMargin(false);
			examButtonsLayout.addStyleName("noverticalpaddingmargin");
			examButtonsLayout.addComponent(getAddExamButton());
			examButtonsLayout.addComponent(getRemoveExamButton());
		}
		return examButtonsLayout;
	}
	
	public Button getAddExamButton(){
		if (addExamButton == null) {
			addExamButton = new Button();
			addExamButton.setCaption(MessageBundle.getMessage("angal.labnew.exam")); //$NON-NLS-1$
			////addExamButton.setClickShortcut(KeyEvent.VK_E);
			addExamButton.setIcon(new FileResource(new File(resPath+"/WEB-INF/icons/plus_button.png"))); //$NON-NLS-1$
			addExamButton.addClickListener(e->{
				Laboratory lab = new Laboratory();
				Image icon = new Image(null,new FileResource(new File(resPath+"/WEB-INF/icons/material_dialog.png")));
				ComboBox mat = new ComboBox(MessageBundle.getMessage("angal.labnew.selectamaterial"));
				mat.setEmptySelectionAllowed(false);
				mat.setItems(matList);
				mat.setValue(matList[0]);
				MessageBox.create().withIcon(icon).withCaption(MessageBundle.getMessage("angal.labnew.material")).withMessage(mat)
				.withOkButton(()->{
					if(mat.isEmpty())
						return;
					Image icon0 = new Image(null,new FileResource(new File(resPath+"/WEB-INF/icons/exam_dialog.png")));
					ListSelect exa = new ListSelect(MessageBundle.getMessage("angal.labnew.selectanexam"));
					exa.setItems(exaArray.toArray());
					MessageBox.create().withIcon(icon0).withCaption(MessageBundle.getMessage("angal.labnew.exam")).withMessage(exa)
					.withOkButton(()->{
						if (exa.getValue().isEmpty()) return;
						for (Laboratory labItem : examItems) {
							if (labItem.getExam() == (Exam)exa.getValue().toArray()[0]){
								MessageBox.createError().withMessage(MessageBundle.getMessage("angal.labnew.thisexamisalreadypresent"))
								.withCaption("Error");
								return;
							}
						}
						if (((Exam)exa.getValue().toArray()[0]).getProcedure() == 1){
							ArrayList<ExamRow> exaRowTemp = new ArrayList<ExamRow>();
							for (ExamRow exaRow : exaRowArray) {
								if (((Exam)exa.getValue().toArray()[0]).getCode().compareTo(exaRow.getExamCode()) == 0) {
									exaRowTemp.add(exaRow);
								}
							}
							Image icon1 = new Image(null,new FileResource(new File(resPath+"/WEB-INF/icons/list_dialog.png")));
							ComboBox exaRow = new ComboBox(MessageBundle.getMessage("angal.labnew.selectaresult"));
							exaRow.setItems(exaRowTemp.toArray());
							exaRow.setEmptySelectionAllowed(false);
							exaRow.setValue(exaRowTemp.get(0));
							MessageBox.create().withIcon(icon1).withCaption(MessageBundle.getMessage("angal.labnew.result")).withMessage(exaRow)
							.withOkButton(()->{
								if (!exaRow.isEmpty()) lab.setResult(((ExamRow)exaRow.getValue()).getDescription());
								else return;
								lab.setExam((Exam)exa.getValue().toArray()[0]);
								lab.setMaterial((String)mat.getValue());
								addItem(lab);
							}).withCancelButton().open();
						} else {
							lab.setResult(MessageBundle.getMessage("angal.labnew.multipleresults"));
							lab.setExam((Exam)exa.getValue().toArray()[0]);
							lab.setMaterial((String)mat.getValue());
							addItem(lab);
						}
					}).withCancelButton().open();
				}).withCancelButton().open();
			});
		}
		return addExamButton;
	}
	
	private void addItem(Laboratory lab) {
		examItems.add(lab);
		examResults.add(new ArrayList<String>());
		examsGrid.setItems(examItems);
		int index = examItems.size()-1;
		examsGrid.select(lab);
		//D/examsGrid.setRowSelectionInterval(index, index);
	}

	private void removeItem(int selectedRow) {
		examItems.remove(selectedRow);
		examsGrid.setItems(examItems);
	}
	
	private Button getRemoveExamButton(){
		if (removeExamButton == null) {
			removeExamButton = new Button();
			removeExamButton.setCaption(MessageBundle.getMessage("angal.labnew.remove")); //$NON-NLS-1$
			removeExamButton.setIcon(new FileResource(new File(resPath+"/WEB-INF/icons/delete_button.png"))); //$NON-NLS-1$
			removeExamButton.addClickListener(e->{
				if(examsGrid.getSelectedItems().isEmpty()){
					MessageBox.createError().withCaption("Error").withMessage(MessageBundle.getMessage("angal.labnew.pleaseselectanexam")).open();
				} else {
					removeItem(examItems.indexOf((Laboratory)examsGrid.getSelectedItems().toArray()[0]));
					VerticalLayout dump = new VerticalLayout();
					resultsPanel.setContent(dump);
					//validate();
					// repaint();
					materialComboBox.setEnabled(false);
				}
			});
		}
		return removeExamButton;
	}
	
	
}