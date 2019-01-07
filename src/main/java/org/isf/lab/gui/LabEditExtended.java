
/*------------------------------------------
 * LabEdit - Add/edit a laboratory exam
 * -----------------------------------------
 * modification history
 * 
 *------------------------------------------*/

package org.isf.lab.gui;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.List;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.swing.event.EventListenerList;

import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.RadioButtonGroup;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import de.steinwedel.messagebox.MessageBox;

import org.isf.admission.manager.AdmissionBrowserManager;
import org.isf.exa.manager.ExamBrowsingManager;
import org.isf.exa.manager.ExamRowBrowsingManager;
import org.isf.exa.model.Exam;
import org.isf.exa.model.ExamRow;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.lab.manager.LabManager;
import org.isf.lab.manager.LabRowManager;
import org.isf.lab.model.Laboratory;
import org.isf.lab.model.LaboratoryRow;
import org.isf.patient.manager.PatientBrowserManager;
import org.isf.patient.model.Patient;
import org.isf.utils.jobjects.ModalWindow;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.time.RememberDates;

public class LabEditExtended extends ModalWindow {
	/**
	 * 
	 */
	
	//LISTENER INTERFACE --------------------------------------------------------
	private List<LabEditExtendedListener> labEditExtendedListeners = new ArrayList<LabEditExtendedListener>();
	
	public interface LabEditExtendedListener{
		public void labUpdated();
	}
	
	public void addLabEditExtendedListener(LabEditExtendedListener l) {
		labEditExtendedListeners.add(l);
		
	}
	
	private void fireLabUpdated(){
		for(LabEditExtendedListener labEditExtendedListener : labEditExtendedListeners)
			labEditExtendedListener.labUpdated();
	}
	//---------------------------------------------------------------------------
	
	//private static final String VERSION=MessageBundle.getMessage("angal.versione");
	private static final String VERSION="2.0";
	
	private boolean insert = false;

	private Laboratory lab = null;
	private VerticalLayout windowContent = null;
	private HorizontalLayout buttonPanel = null;
	private VerticalLayout dataLayout = null;
	private Panel resultPanel = null;
	private Label examLabel = null;
	private Label noteLabel = null;
	private Label patientLabel = null;
	private CheckBox inPatientCheckBox = null;
	private Label nameLabel = null;
	private Label ageLabel = null;
	private Label sexLabel = null;
	private Label examDateLabel = null;
	private Label matLabel = null;
	private Button okButton = null;
	private Button cancelButton = null;
	private ComboBox matComboBox = null;
	private ComboBox examComboBox = null;
	private ComboBox examRowComboBox = null;
	private ComboBox patientComboBox = null;
	private Exam examSelected = null;
	private JScrollPane noteScrollPane = null;

	private TextArea noteTextArea = null;

	private TextField patTextField = null;
	private TextField ageTextField = null;
	private TextField sexTextField = null;

	//ADDED: Alex
	private Panel dataPatientPanel = null;
	private TextField patientCodeTextField;
	private Patient labPat = null;
	private String lastKey;
	private String s;
	private ArrayList<Patient> pat = null;
	//private Button jSearchTrashButton = null;
	
	//private VoDateTextField examDateField = null;
	private DateField examDateField = null;
	private DateFormat currentDateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.ITALIAN);
	private GregorianCalendar dateIn = null;

	
	private static final Integer panelWidth=500; 
	private static final Integer labelWidth=50; 
	private static final Integer dataLayoutHeight=90;
	private static final Integer dataPatientHeight=100;
	private static final Integer resultPanelHeight=350; 
	private static final Integer buttonPanelHeight=40; 

	
	private ArrayList<ExamRow> eRows = null;

	public LabEditExtended(Laboratory laboratory, boolean inserting) {
		insert = inserting;
		lab = laboratory;
		initialize();
	}

	private void initialize() {
		UI.getCurrent().addWindow(this);
		showAsModal();
		// this.setBounds(30,30,panelWidth+20,dataLayoutHeight+dataPatientHeight+resultPanelHeight+buttonPanelHeight+30);
		setContent(getWindowContent());
		// this.setResizable(false);
		if (insert) {
			setCaption(MessageBundle.getMessage("angal.lab.newlaboratoryexam")+"("+VERSION+")");
		} else {
			setCaption(MessageBundle.getMessage("angal.lab.editlaboratoryexam")+"("+VERSION+")");
		}
		// this.setDefaultclose()Operation(JFrame.DISPOSE_ON_close());
		// this.setLocationRelativeTo(null);
	}


	private VerticalLayout getWindowContent() {
		if (windowContent == null){
			windowContent = new VerticalLayout();
			windowContent.setMargin(false);
			// data panel
			windowContent.addComponent(getDataLayout());
			windowContent.addComponent(getDataPatient());
			resultPanel = new Panel();
			// // resultPanel.setBounds(0, dataLayoutHeight+dataPatientHeight, panelWidth, resultPanelHeight);
			if (!insert) {
				examSelected = lab.getExam();
				if (examSelected.getProcedure() == 1)
					resultPanel = getFirstPanel();
				else if (examSelected.getProcedure() == 2)
					resultPanel = getSecondPanel();
			}
			resultPanel.setCaption(MessageBundle.getMessage("angal.lab.result"));
			windowContent.addComponent(resultPanel);
			windowContent.addComponent(getButtonLayout()); // Generated
		}
		return windowContent;
	}

	private VerticalLayout getDataLayout() {
		if (dataLayout == null) {
			//initialize data panel
			dataLayout = new VerticalLayout();
			dataLayout.setMargin(false);
			// //exam date
			examDateLabel = new Label(MessageBundle.getMessage("angal.common.date"));
			examDateField = getExamDateField();
			examDateField.setLocale(new Locale(GeneralData.LANGUAGE));
			examDateField.setDateFormat("dd/MM/yy");
			// // examDateField.setBounds(labelWidth+5, 10, 90, 20);
			// //material
			matLabel = new Label(MessageBundle.getMessage("angal.lab.material"));
			// // matLabel.setBounds(155, 10, labelWidth, 20);
			matComboBox= getMatComboBox();
			// // matComboBox.setBounds(215, 10, 280, 20);
			// //exam combo
			examLabel = new Label(MessageBundle.getMessage("angal.lab.exam"));
			// examLabel.setBounds(5, 35, labelWidth, 20);
			examComboBox=getExamComboBox();
			// examComboBox.setBounds(labelWidth+5, 35, 440, 20);

			// //patient (in or out) data
			patientLabel = new Label(MessageBundle.getMessage("angal.lab.patientcode"));
			// patientLabel.setBounds(labelWidth+5, 60, 110 , 20);
			
			// //ADDED: Alex
			patientCodeTextField = new TextField();//20col
			patientCodeTextField.setMaxLength(200);
			// patientCodeTextField.setBounds(labelWidth+50,60,100,20);
			
			// patientCodeTextField.addKeyListener(new KeyListener() {
			// 	public void keyTyped(KeyEvent e) 
			// 	{
			// 		lastKey = "";
			// 		String s = "" + e.getKeyChar();
			// 		if (Character.isLetterOrDigit(e.getKeyChar())) {
			// 			lastKey = s;
			// 		}
			// 		s = patientCodeTextField.getText() + lastKey;
			// 		s.trim();
					
			// 		filterPatient(s);
			// 	}

			// 	//@Override
			// 	public void keyPressed(KeyEvent e) {}

			// 	//@Override
			// 	public void keyReleased(KeyEvent e) {}
			// });
			patientComboBox = getPatientComboBox(s);
			// patientComboBox.setBounds(labelWidth+160, 60, 285, 20);

			// //add all to the data panel
			HorizontalLayout firstLayout = new HorizontalLayout();
			firstLayout.addComponent(examDateLabel);
			firstLayout.addComponent(examDateField);
			firstLayout.addComponent(matLabel);
			firstLayout.addComponent(matComboBox);
			dataLayout.addComponent(firstLayout);
			HorizontalLayout secondLayout = new HorizontalLayout();
			secondLayout.addComponent(examLabel);
			secondLayout.addComponent(examComboBox);
			dataLayout.addComponent(secondLayout);
			HorizontalLayout thirdLayout = new HorizontalLayout();
			thirdLayout.addComponent(patientLabel);
			thirdLayout.addComponent(patientCodeTextField);
			thirdLayout.addComponent(patientComboBox);
			dataLayout.addComponent(thirdLayout);
			// dataLayout.setPreferredSize(new Dimension(150,200));
						
		}
		return dataLayout;
	}

	private Panel getDataPatient() {
		if (dataPatientPanel == null) {
			dataPatientPanel = new Panel(MessageBundle.getMessage("angal.lab.datapatient"));			
			nameLabel = new Label(MessageBundle.getMessage("angal.lab.name"));
			// nameLabel.setBounds(10, 20, labelWidth, 20);
			patTextField=getPatientTextField();
			// patTextField.setBounds(labelWidth+5, 20, 180, 20);
			ageLabel = new Label(MessageBundle.getMessage("angal.lab.age"));
			// ageLabel.setBounds(255, 20, 35, 20);
			ageTextField=getAgeTextField();
			// ageTextField.setBounds(295, 20, 50, 20);
			sexLabel = new Label(MessageBundle.getMessage("angal.lab.sexmf"));
			// sexLabel.setBounds(370, 20, 80, 20);
			sexTextField=getSexTextField();
			// sexTextField.setBounds(440, 20, 50, 20);
			// //note			
			noteLabel = new Label(MessageBundle.getMessage("angal.lab.note"));
			// noteLabel.setBounds(10, 50, labelWidth, 20);
			noteTextArea = getNoteTextArea();
			// noteTextArea.setBounds(labelWidth+5, 50, 440, 35);
			noteTextArea.setEnabled(true);
			// noteTextArea.setAutoscrolls(true);
			
			// /*
			//  * Teo : Adding scroll capabilities at note textArea
			//  */
			// if(noteScrollPane == null)
			// {
			// 	noteScrollPane = new JScrollPane(noteTextArea);
			// 	noteScrollPane.setBounds(labelWidth+5, 50, 440, 35);
			// 	noteScrollPane.createVerticalScrollBar();
			// 	noteScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			// 	noteScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			// 	noteScrollPane.setAutoscrolls(true);
			// 	dataPatientLayout.addComponent(noteScrollPane);
			// }
			VerticalLayout dataPatientLayout = new VerticalLayout();
			dataPatientPanel.setContent(dataPatientLayout);
			HorizontalLayout firstLayout = new HorizontalLayout();
			firstLayout.addComponent(nameLabel);
			firstLayout.addComponent(patTextField);
			firstLayout.addComponent(ageLabel);
			firstLayout.addComponent(ageTextField);
			firstLayout.addComponent(sexLabel);
			firstLayout.addComponent(sexTextField);
			dataPatientLayout.addComponent(firstLayout);
			dataPatientLayout.addComponent(noteTextArea);
			
			patTextField.setEnabled(false);
			ageTextField.setEnabled(false);
			sexTextField.setEnabled(false);
			noteTextArea.setEnabled(true);
			
		}
		return dataPatientPanel;
	}

	private DateField getExamDateField() {
		java.util.Date myDate = null;
		if (insert) {
			dateIn = RememberDates.getLastLabExamDateGregorian();
		} else { 
			dateIn = lab.getExamDate();
		}
		if (dateIn != null) {
			myDate = dateIn.getTime();
		}
		return (new DateField(null,myDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()));
	}
	
	private CheckBox getInPatientCheckBox() {
		if (inPatientCheckBox == null) {
			inPatientCheckBox = new CheckBox(MessageBundle.getMessage("angal.lab.in"));
			// if (!insert)
			// 	inPatientCheckBox.setSelected(lab.getInOutPatient().equalsIgnoreCase("I"));
			// lab.setInOutPatient((inPatientCheckBox.isSelected()?"I":"R"));
		}
		return inPatientCheckBox;
	}

	private ComboBox getPatientComboBox(String s) {
		
		//String key = s;
		PatientBrowserManager patBrowser = new PatientBrowserManager();
		if (insert) pat = patBrowser.getPatient();
			
		if (patientComboBox == null) {
			patientComboBox = new ComboBox();
			// patientComboBox.addItem(MessageBundle.getMessage("angal.lab.selectapatient"));
			
			if (!insert) {
				labPat = patBrowser.getPatientAll(lab.getPatId());
				patientComboBox.setItems(labPat);
				patientComboBox.setSelectedItem(labPat);
				patientComboBox.setEnabled(false);
				patientCodeTextField.setValue(String.valueOf(labPat.getCode()));
				patientCodeTextField.setEnabled(false);
				return patientComboBox;
			}
			
			// for (Patient elem : pat) {
			// 	patientComboBox.addItem(elem);
			// }
			
			// patientComboBox.addActionListener(new ActionListener() {
			// 	public void actionPerformed(ActionEvent arg0) {
			// 		if (patientComboBox.getSelectedIndex()>0) {
			// 			AdmissionBrowserManager admMan = new AdmissionBrowserManager();
			// 			labPat=(Patient)patientComboBox.getValue();
			// 			setPatient(labPat);
			// 			inPatientCheckBox.setSelected(admMan.getCurrentAdmission(labPat) != null ? true : false);
			// 		}
			// 	}
			// });
		}
		return patientComboBox;
	}
	
	private void filterPatient(String key) {
		// patientComboBox.removeAllItems();
				
		// if (key == null || key.compareTo("") == 0) {
		// 	patientComboBox.addItem(MessageBundle.getMessage("angal.lab.selectapatient"));
		// 	resetLabPat();
		// }
		
		// for (Patient elem : pat) {
		// 	if (key != null) {
		// 		//Search key extended to name and code
		// 		StringBuilder sbName = new StringBuilder();
		// 		sbName.append(elem.getSecondName().toUpperCase());
		// 		sbName.append(elem.getFirstName().toUpperCase());
		// 		sbName.append(elem.getCode());
		// 		String name = sbName.toString();
				
		// 		if(name.toLowerCase().contains(key.toLowerCase())) {
		// 			patientComboBox.addItem(elem);
		// 		}
		// 	} else {
		// 		patientComboBox.addItem(elem);
		// 	}
		// }
		
		// if (patientComboBox.getItemCount() == 1) {
		// 	labPat=(Patient)patientComboBox.getValue();
		// 	setPatient(labPat);
		// }
		
		// if (patientComboBox.getItemCount() > 0) {
		// 	if (patientComboBox.getItemAt(0) instanceof Patient) {
		// 		labPat = (Patient)patientComboBox.getItemAt(0);
		// 		setPatient(labPat);
		// 	}
		// }
	}

	private void resetLabPat() {
		// patTextField.setText("");
		// ageTextField.setText("");
		// sexTextField.setText("");
		// noteTextArea.setText("");
		// labPat = null;
	}

	private void setPatient(Patient labPat) {
		// patTextField.setText(labPat.getName());
		// ageTextField.setText(labPat.getAge()+"");
		// sexTextField.setText(labPat.getSex()+"");
		// noteTextArea.setText(labPat.getNote());		
	}

	private HorizontalLayout getButtonLayout() {
		if (buttonPanel == null) {
			buttonPanel = new HorizontalLayout();
			// buttonPanel.setBounds(0, dataLayoutHeight+dataPatientHeight+resultPanelHeight, panelWidth, buttonPanelHeight);
			buttonPanel.addComponent(getOkButton());
			buttonPanel.addComponent(getCancelButton());
		}
		return buttonPanel;
	}

	private ComboBox getExamComboBox() {
		if (examComboBox == null) {
			examComboBox = new ComboBox();
			Exam examSel=null;
			ExamBrowsingManager manager = new ExamBrowsingManager();
			ArrayList<Exam> exams = manager.getExams();
			for (Exam elem : manager.getExams()) {
				if (!insert && elem.getCode()!=null) {
					if (elem.getCode().equalsIgnoreCase((lab.getExam().getCode()))) {
						examSel=elem;
					}
				}
			}
			examComboBox.setEmptySelectionAllowed(false);
			examComboBox.setItems(exams);
			examComboBox.setValue(examSel);
			
			examComboBox.addValueChangeListener(e->{
				if (!(examComboBox.getValue() instanceof String)) {
					examSelected = (Exam) examComboBox
							.getValue();

					if (examSelected.getProcedure() == 1)
						resultPanel = getFirstPanel();
					else if (examSelected.getProcedure() == 2)
						resultPanel = getSecondPanel();

					// validate();
					// repaint();
				}
			});
			resultPanel = null;
		}
		return examComboBox;
	}

	
	private ComboBox getMatComboBox() {
		if (matComboBox == null) {
			matComboBox = new ComboBox();
			List mat = new ArrayList();
			mat.add(MessageBundle.getMessage("angal.lab.blood"));
			mat.add(MessageBundle.getMessage("angal.lab.urine"));
			mat.add(MessageBundle.getMessage("angal.lab.stool"));
			mat.add(MessageBundle.getMessage("angal.lab.sputum"));
			mat.add(MessageBundle.getMessage("angal.lab.cfs"));
			mat.add(MessageBundle.getMessage("angal.lab.swabs"));
			mat.add(MessageBundle.getMessage("angal.lab.tissues"));
			matComboBox.setItems(mat);
			if (!insert) {
				try {	
					matComboBox.setValue(lab.getMaterial());
					}
				catch (Exception e) {}
			}
		}
		return matComboBox;
	}

	
	//prova per gestire un campo note al posto di uno volimited
	private TextArea getNoteTextArea() {
		if (noteTextArea == null) {
			noteTextArea = new TextArea();//r10,c30
			noteTextArea.setWidth("100%");
			noteTextArea.setCaption(MessageBundle.getMessage("angal.lab.note"));
			if (!insert){
				noteTextArea.setValue(lab.getNote());
			}
			noteTextArea.setWordWrap(true);
			// noteTextArea.setPreferredSize(new Dimension(10,30));
			// noteTextArea.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		}
		return noteTextArea;
	}
	
	
	
	private TextField getPatientTextField() {
		if (patTextField == null) {
			patTextField = new TextField();
			patTextField.setMaxLength(100);
			if (!insert) {
				patTextField.setValue(lab.getPatName());
			}
		}
		return patTextField;
	}

	
	private TextField getAgeTextField() {
		if (ageTextField == null) {
			ageTextField = new TextField();
			ageTextField.setMaxLength(3);
			if (insert) {
				ageTextField.setValue("");
				}
			else {
				try {	
					Integer intAge=lab.getAge();
					ageTextField.setValue(intAge.toString());
					}
				catch (Exception e) {
					ageTextField.setValue("");
					}
				}
			}
		return ageTextField;
	}
	
	private TextField getSexTextField() {
		if (sexTextField == null) {
			sexTextField = new TextField();
			sexTextField.setMaxLength(1);
			if (!insert) {
				sexTextField.setValue(lab.getSex());
			}
		}
		return sexTextField;
	}
	
	private Button getCancelButton() {
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
	private Date getDate(LocalDate localDate){
		return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}
	private Button getOkButton() {
		if (okButton == null) {
			okButton = new Button();
			okButton.setCaption(MessageBundle.getMessage("angal.common.ok"));
			////okButton.setClickShortcut(KeyEvent.VK_O);
			okButton.addClickListener(e->{
				String matSelected=(String)matComboBox.getValue();
				examSelected=(Exam)examComboBox.getValue();
				labPat=(Patient)patientComboBox.getValue();
				// exam  date
				String d = currentDateFormat.format(getDate(examDateField.getValue()));
				GregorianCalendar gregDate = new GregorianCalendar();
				gregDate.setTime(getDate(examDateField.getValue()));
				
				ArrayList<String> labRow = new ArrayList<String>();
				LabManager manager = new LabManager();
				lab.setDate(new GregorianCalendar());
				lab.setExamDate(gregDate);
				RememberDates.setLastLabExamDate(gregDate);
				lab.setMaterial(matSelected);
				lab.setExam(examSelected);
				lab.setNote(noteTextArea.getValue());
				lab.setPatId(labPat.getCode());
				lab.setPatName(labPat.getName());
				lab.setSex(labPat.getSex()+"");
				
				if (examSelected.getProcedure() == 1)
					lab.setResult(examRowComboBox.getValue()
							.toString());
				else if (examSelected.getProcedure() == 2) {
					lab.setResult(MessageBundle.getMessage("angal.lab.multipleresults"));
					for (int i = 0; i < ((VerticalLayout) resultPanel.getContent()).getComponentCount(); i++) {
						if (((ExamRadio) ((VerticalLayout) resultPanel.getContent()).getComponent(i))
								.getSelectedResult().equalsIgnoreCase("P")) {
							labRow.add(eRows.get(i).getDescription());
						}
					}
				} 
				boolean result = false;
				if (insert) {
			// 		lab.setAge(labPat.getAge());
			// 		if (examSelected.getProcedure() == 1)
			// 			result = manager.newLabFirstProcedure(lab);
			// 		else if (examSelected.getProcedure() == 2)
			// 			result = manager.newLabSecondProcedure(lab,	labRow);
				}
				else {
					if (examSelected.getProcedure() == 1)
						result = manager.editLabFirstProcedure(lab);
					else if (examSelected.getProcedure() == 2)
						result = manager.editLabSecondProcedure(lab, labRow);
				}

				if (!result) MessageBox.create().withMessage(MessageBundle.getMessage("angal.lab.thedatacouldnotbesaved")).open();
				else {
					fireLabUpdated();
					close();
				}
			});
		}
		return okButton;
	}

	private Panel getFirstPanel() {
		// resultPanel.removeAll();
		String result="";
		examRowComboBox = new ComboBox();
		examRowComboBox.setEmptySelectionAllowed(false);
		// examRowComboBox.setMaximumSize(new Dimension(200, 25));
		// examRowComboBox.setMinimumSize(new Dimension(200, 25));
		// examRowComboBox.setPreferredSize(new Dimension(200, 25));
		if (insert) {
			result=examSelected.getDefaultResult();
		} else {
			result=lab.getResult();
		}
		ArrayList examRows = new ArrayList();
		examRows.add(result);

		ExamRowBrowsingManager rowManager = new ExamRowBrowsingManager();
		ArrayList<ExamRow> rows = rowManager.getExamRow(examSelected.getCode());
		for (ExamRow r : rows) {
			if (!r.getDescription().equals(result))
				examRows.add(r.getDescription());
		}
		examRowComboBox.setItems(examRows);
		examRowComboBox.setValue(result);
		resultPanel.setContent(examRowComboBox);

		return resultPanel;
	}

	private Panel getSecondPanel() {
		// resultPanel.removeAll();
		// resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
		String examId = examSelected.getCode();
		ExamRowBrowsingManager eRowManager = new ExamRowBrowsingManager();
		eRows = null;
		eRows = eRowManager.getExamRow(examId);
		if (insert) {
			// for (ExamRow r : eRows)
			// 	resultPanel.add(new ExamRadio(r, "N"));
		} else {
			LabRowManager lRowManager = new LabRowManager();

			ArrayList<LaboratoryRow> lRows = lRowManager.getLabRow(lab
					.getCode());
			boolean find;
			VerticalLayout tempLayout = new VerticalLayout();
			resultPanel.setContent(tempLayout);
			for (ExamRow r : eRows) {
				find = false;
				for (LaboratoryRow lR : lRows) {
					if (r.getDescription()
							.equalsIgnoreCase(lR.getDescription()))
						find = true;
				}
				if (find) {
					tempLayout.addComponent(new ExamRadio(r, "P"));
				} else {
					tempLayout.addComponent(new ExamRadio(r, "N"));
				}
			}
		}
		return resultPanel;
	}

	class ExamRadio extends RadioButtonGroup {

		public ExamRadio(ExamRow row, String result) {
			setCaption(row.getDescription());
			addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
			setItems(MessageBundle.getMessage("angal.lab.p"),MessageBundle.getMessage("angal.lab.n"));
			if (result.equals(MessageBundle.getMessage("angal.lab.p")))
				setValue(MessageBundle.getMessage("angal.lab.p"));
			else
				setValue(MessageBundle.getMessage("angal.lab.n"));
		}

		public String getSelectedResult() {
			return ""+getValue();
		}

	}

}
