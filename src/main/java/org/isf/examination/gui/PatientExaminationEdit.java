package org.isf.examination.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.time.ZoneId;
import java.time.LocalDate;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.OverlayLayout;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Image;
import java.io.File;
import com.vaadin.server.FileResource;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Label;
import com.vaadin.ui.Slider;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.shared.ui.ContentMode;

import org.isf.examination.model.GenderPatientExamination;
import org.isf.examination.model.PatientExamination;
import org.isf.examination.service.ExaminationOperations;
import org.isf.generaldata.ExaminationParameters;
import org.isf.generaldata.MessageBundle;
import org.isf.utils.jobjects.VoIntegerTextField;
import org.isf.utils.jobjects.NumberField;
import org.isf.utils.jobjects.VoLimitedTextArea;
import org.isf.utils.jobjects.ModalWindow;
import org.isf.generaldata.GeneralData;
import org.isf.utils.Logging;

import com.toedter.calendar.JDateChooser;

public class PatientExaminationEdit extends ModalWindow {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private VerticalLayout examinationLayout;
	private HorizontalLayout buttonLayout;
	private Slider heightSlider;
	private Slider weightSlider;
	private NumberField hrTextField;
	private TextField tempTextField;
	private TextField saturationTextField;
	private TextArea noteTextArea;
	private NumberField heightTextField;
	private TextField weightTextField;
	private Label minAPLabel;
	private Label slashAPLabel;
	private Label maxAPLabel;
	private Slider hrSlider;
	private Slider tempSlider;
	private Slider saturationSlider;
	private DateField dateField;
	private NumberField minAPTextField;
	private NumberField maxAPTextField;
	private Label heightLabelAbb;
	private Label weightAbbLabel;
	private CheckBox apCheckBox;
	private CheckBox hrCheckBox;
	private CheckBox tempCheckBox;
	private CheckBox saturationCheckBox;
	private Button okButton;
	private Button jButtonCancel;
	private Action actionSavePatientExamination;
	private Action actionToggleAP;
	private Action actionToggleHR;
	private Action actionToggleTemp;
	private Action actionToggleSaturation;
	private AbsoluteLayout genderLayout;
	private Image jLabelGender;
	private Label bmiLabel;
	private Panel summaryPanel;
	private Label summaryLabel;
	
	private PatientExamination patex;
	private boolean isMale;
	private double bmi;
	Logging logger;
	
	private final String PATH_FEMALE_GENDER = "/WEB-INF/images/sagoma-donna-132x300.jpg"; //$NON-NLS-1$
	private final String PATH_MALE_GENDER = "/WEB-INF/images/sagoma-uomo-132x300.jpg"; //$NON-NLS-1$
	private final String SUMMARY_START_ROW = "<tr align=\"center\">";
	private final String SUMMARY_END_ROW = "</tr>";
	private final String STD = "<td>";
	private final String ETD = "</td>";
	private final String SUMMARY_HEADER = "" +
			"<html><head></head><body><table>"+
			SUMMARY_START_ROW+
			STD+MessageBundle.getMessage("angal.common.datem")+ETD+
			STD+MessageBundle.getMessage("angal.examination.height")+ETD+
			STD+MessageBundle.getMessage("angal.examination.weight")+ETD+
			STD+MessageBundle.getMessage("angal.examination.arterialpressureabbr")+ETD+
			STD+MessageBundle.getMessage("angal.examination.heartrateabbr")+ETD+
			STD+MessageBundle.getMessage("angal.examination.temperatureabbr")+ETD+
			STD+MessageBundle.getMessage("angal.examination.saturationabbr")+ETD+
			SUMMARY_END_ROW;
	private final String SUMMARY_FOOTER = "</table></body></html>";
	
	private final String DATE_FORMAT = "dd/MM/yy";

	/**
	 * Create the dialog.
	 */
	public PatientExaminationEdit() {
		super();
		initComponents();
		updateGUI();
	}
	
	public PatientExaminationEdit(GenderPatientExamination gpatex) {
		this.patex = gpatex.getPatex();
		this.isMale = gpatex.isMale();
		initComponents();//qqq
		updateGUI();
	}
	
	private void initComponents() {
		logger = new Logging();
		ExaminationParameters.getExaminationParameters();
		UI.getCurrent().addWindow(this);
		VerticalLayout windowContent = new VerticalLayout();
		setContent(windowContent);
		HorizontalLayout northLayout = new HorizontalLayout();
		HorizontalLayout southLayout = new HorizontalLayout();
		windowContent.addComponents(northLayout, southLayout);
		northLayout.addComponents(getGenderLayout());
		northLayout.addComponent(getExaminationLayout());
		northLayout.addComponent(getSummaryPanel());
		southLayout.addComponent(getButtonLayout());
		updateSummary();
		updateBMI();
		// pack();
		// setResizable(false);
	}
	
	private HorizontalLayout getButtonLayout() {
		if (buttonLayout == null) {
			buttonLayout = new HorizontalLayout();
			buttonLayout.addComponent(getOKButton());
			// buttonLayout.addComponent(getCancelButton());
		}
		return buttonLayout;
	}
	
	//TODO: try to use JDOM...
	private void updateBMI() {
		this.bmi = patex.getBMI();
		StringBuilder bmi = new StringBuilder();
		bmi.append("<html><body>");
		bmi.append("<strong>");
		bmi.append(MessageBundle.getMessage("angal.examination.bmi") + ":");
		bmi.append("<br />");
		bmi.append("" + this.bmi);
		bmi.append("<br /><br />");
		bmi.append("<font color=\"red\">");
		bmi.append(getBMIdescription(this.bmi));
		bmi.append("</font>");
		bmi.append("</strong>");
		bmi.append("</body></html>");
		bmiLabel.setValue(bmi.toString());
	}
	
	private Object getBMIdescription(double bmi) {
		if (bmi < 16.5)
			return MessageBundle.getMessage("angal.examination.bmi.severeunderweight");
		if (bmi >= 16.5 && bmi < 18.5)
			return MessageBundle.getMessage("angal.examination.bmi.underweight");
		if (bmi >= 18.5 && bmi < 24.5)
			return MessageBundle.getMessage("angal.examination.bmi.normalweight");
		if (bmi >= 24.5 && bmi < 30)
			return MessageBundle.getMessage("angal.examination.bmi.overweight");
		if (bmi >= 30 && bmi < 35)
			return MessageBundle.getMessage("angal.examination.bmi.obesityclassilight");
		if (bmi >= 35 && bmi < 40)
			return MessageBundle.getMessage("angal.examination.bmi.obesityclassiimedium");
		if (bmi >= 40)
			return MessageBundle.getMessage("angal.examination.bmi.obesityclassiiisevere");
		return "";
	}

	//TODO: try to use JDOM...
	private void updateSummary() {
		StringBuilder summary = new StringBuilder();
		summary.append(SUMMARY_HEADER);
		ExaminationOperations examOperations = new ExaminationOperations();
		ArrayList<PatientExamination> patexList = examOperations.getLastNByPatID(patex.getPatient().getCode(), ExaminationParameters.LIST_SIZE);
		Collections.sort(patexList);
		
		for (PatientExamination patex : patexList) {
			summary.append(SUMMARY_START_ROW);
			summary.append(STD).append(new SimpleDateFormat(DATE_FORMAT).format(new Date(patex.getPex_date().getTime()))).append(ETD);
			summary.append(STD).append(patex.getPex_height()).append(ETD);
			summary.append(STD).append(patex.getPex_weight()).append(ETD);
			summary.append(STD).append(patex.getPex_pa_min()).append(" / ").append(patex.getPex_pa_max()).append(ETD);
			summary.append(STD).append(patex.getPex_fc()).append(ETD);
			summary.append(STD).append(patex.getPex_temp()).append(ETD);
			summary.append(STD).append(patex.getPex_sat()).append(ETD);
			summary.append(SUMMARY_END_ROW);
		}
		summary.append(SUMMARY_FOOTER);
		summaryLabel.setValue(summary.toString());
	}
	
	private void updateGUI() {
		dateField.setValue((new Date(patex.getPex_date().getTime())).toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
		heightTextField.setValue(String.valueOf(patex.getPex_height()));
		heightSlider.setValue((double)patex.getPex_height());
		weightTextField.setValue(String.valueOf(patex.getPex_weight()));
		weightSlider.setValue(Double.valueOf(convertFromDoubleToInt(patex.getPex_weight(), ExaminationParameters.WEIGHT_MIN, ExaminationParameters.WEIGHT_STEP, ExaminationParameters.WEIGHT_MAX)));
		minAPTextField.setValue(String.valueOf(patex.getPex_pa_min()));
		maxAPTextField.setValue(String.valueOf(patex.getPex_pa_max()));
		hrSlider.setValue((double)patex.getPex_fc());
		hrTextField.setValue(String.valueOf(patex.getPex_fc()));
		tempSlider.setValue(patex.getPex_temp());
		tempTextField.setValue(String.valueOf(convertFromDoubleToInt(patex.getPex_temp(), ExaminationParameters.TEMP_MIN, ExaminationParameters.TEMP_STEP, ExaminationParameters.TEMP_MAX)));
		saturationSlider.setValue(Double.valueOf(convertFromDoubleToInt(patex.getPex_sat(), ExaminationParameters.SAT_MIN, ExaminationParameters.SAT_STEP, ExaminationParameters.SAT_MAX)));
		saturationTextField.setValue(String.valueOf(patex.getPex_sat()));
		noteTextArea.setValue(patex.getPex_note());
		disableAP();
		disableHR();
		disableTemp();
		disableSaturation();
	}

	private VerticalLayout getExaminationLayout() {
		if (examinationLayout == null) {
			examinationLayout = new VerticalLayout();
			
			HorizontalLayout dateLayout = new HorizontalLayout();
			examinationLayout.addComponent(dateLayout);
			Label dateLabel = new Label(MessageBundle.getMessage("angal.common.date")); //$NON-NLS-1$
			dateLayout.addComponent(dateLabel);
			dateLayout.addComponent(getDateField());
			
			HorizontalLayout heightLayout = new HorizontalLayout();
			examinationLayout.addComponent(heightLayout);
			heightLabelAbb = new Label(MessageBundle.getMessage("angal.examination.heightabbr")); //$NON-NLS-1$
			heightLayout.addComponent(heightLabelAbb);
			Label heightLabel = new Label(MessageBundle.getMessage("angal.examination.height")); //$NON-NLS-1$
			heightLayout.addComponent(heightLabel);
			heightLayout.addComponent(getHeightSlider());
			Label heightLabelUnit = new Label(ExaminationParameters.HEIGHT_UNIT);
			heightLayout.addComponent(heightLabelUnit);
			heightLayout.addComponent(getHeightTextField());
			
			HorizontalLayout weightLayout = new HorizontalLayout();
			examinationLayout.addComponent(weightLayout);
			weightAbbLabel = new Label(MessageBundle.getMessage("angal.examination.weightabbr")); //$NON-NLS-1$
			weightLayout.addComponent(weightAbbLabel);
			Label jLabelWeight = new Label(MessageBundle.getMessage("angal.examination.weight")); //$NON-NLS-1$
			weightLayout.addComponent(jLabelWeight);
			weightLayout.addComponent(getWeightSlider());
			Label jLabelWeightUnit = new Label(ExaminationParameters.WEIGHT_UNIT);
			weightLayout.addComponent(jLabelWeightUnit);
			weightLayout.addComponent(getWeightTextField());
			
			HorizontalLayout apLayout = new HorizontalLayout();
			examinationLayout.addComponent(apLayout);
			apLayout.addComponent(getAPCheckBox());
			Label minAPLabel = new Label(MessageBundle.getMessage("angal.examination.arterialpressure")); //$NON-NLS-1$
			apLayout.addComponent(minAPLabel);
			minAPLabel = new Label(MessageBundle.getMessage("angal.examination.ap.min")); //$NON-NLS-1$
			apLayout.addComponent(minAPLabel);
			apLayout.addComponent(getMinAPTextField());//jspinner
			slashAPLabel = new Label("/"); //$NON-NLS-1$
			apLayout.addComponent(slashAPLabel);
			apLayout.addComponent(getMaxAPTextField());
			maxAPLabel = new Label(MessageBundle.getMessage("angal.examination.ap.max")); //$NON-NLS-1$
			apLayout.addComponent(maxAPLabel);
			Label jLabelAPUnit = new Label(ExaminationParameters.AP_UNIT);
			apLayout.addComponent(jLabelAPUnit);
			
			HorizontalLayout hrLayout = new HorizontalLayout();
			examinationLayout.addComponent(hrLayout);
			hrLayout.addComponent(getHRCheckBox());
			Label jLabelHR = new Label(MessageBundle.getMessage("angal.examination.heartrate")); //$NON-NLS-1$
			hrLayout.addComponent(jLabelHR);
			hrLayout.addComponent(getHRSlider());
			Label jLabelHRUnit = new Label(ExaminationParameters.HR_UNIT);
			hrLayout.addComponent(jLabelHRUnit);
			hrLayout.addComponent(getHRTextField());

			HorizontalLayout tempLayout = new HorizontalLayout();
			examinationLayout.addComponent(tempLayout);
			tempLayout.addComponent(getTempCheckBox());
			Label jLabelTemp = new Label(MessageBundle.getMessage("angal.examination.temperature")); //$NON-NLS-1$
			tempLayout.addComponent(jLabelTemp);
			tempLayout.addComponent(getTempSlider());
			Label jLabelTempUnit = new Label(ExaminationParameters.TEMP_UNIT);
			tempLayout.addComponent(jLabelTempUnit);
			tempLayout.addComponent(getTempTextField());

			HorizontalLayout saturationLayout = new HorizontalLayout();
			examinationLayout.addComponent(saturationLayout);
			saturationLayout.addComponent(getSaturationCheckBox());
			Label jLabelSaturation = new Label(MessageBundle.getMessage("angal.examination.saturation")); //$NON-NLS-1$
			saturationLayout.addComponent(jLabelSaturation);
			saturationLayout.addComponent(getSaturationSlider());
			saturationLayout.addComponent(getSaturationTextField());
			Label jLabelNote = new Label(MessageBundle.getMessage("angal.examination.note")); //$NON-NLS-1$
			
			examinationLayout.addComponent(jLabelNote);
			examinationLayout.addComponent(getNoteTextArea());
		}
		return examinationLayout;
	}
	
	private CheckBox getSaturationCheckBox() {
		if (saturationCheckBox == null) {
			saturationCheckBox = new CheckBox(); //$NON-NLS-1$
			saturationCheckBox.setDescription(MessageBundle.getMessage("angal.examination.tooltip.togglesaturationexamination"));
			saturationCheckBox.addValueChangeListener(e->{
				if (!saturationCheckBox.getValue()) {
					disableSaturation();
				} else {
					enableSaturation();
				}
			});
		}
		return saturationCheckBox;
	}
	
	private CheckBox getTempCheckBox() {
		if (tempCheckBox == null) {
			tempCheckBox = new CheckBox(); //$NON-NLS-1$
			tempCheckBox.setDescription(MessageBundle.getMessage("angal.examination.tooltip.toggletemperatureexamination"));
			tempCheckBox.addValueChangeListener(e->{
				if (!tempCheckBox.getValue()) {
					disableTemp();
				} else {
					enableTemp();
				}
			});
		}
		return tempCheckBox;
	}

	private CheckBox getHRCheckBox() {
		if (hrCheckBox == null) {
			hrCheckBox = new CheckBox(); //$NON-NLS-1$
			hrCheckBox.setDescription(MessageBundle.getMessage("angal.examination.tooltip.toggleheartrateexamination"));
			hrCheckBox.addValueChangeListener(e->{
				if (!hrCheckBox.getValue()) {
					disableHR();
				} else {
					enableHR();
				}
			});
		}
		return hrCheckBox;
	}

	private CheckBox getAPCheckBox() {
		if (apCheckBox == null) {
			apCheckBox = new CheckBox(""); //$NON-NLS-1$
			apCheckBox.setDescription(MessageBundle.getMessage("angal.examination.tooltip.togglearterialpressureexamination"));
			apCheckBox.addValueChangeListener(e->{
				if (!apCheckBox.getValue()) {
					disableAP();
				} else {
					enableAP();
				}
			});
		}
		return apCheckBox;
	}

	private DateField getDateField() {
		if (dateField == null) {
			// opdDateFieldCal = new DateField("",currentDateFormat.parse(d).toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
			// opdDateFieldCal.setLocale(new Locale(GeneralData.LANGUAGE));
			// opdDateFieldCal.setDateFormat("dd/MM/yy");
			// dateField = new DateField("",currentDateFormat.parse(d).toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
			dateField = new DateField("",LocalDate.now());
			dateField.setLocale(new Locale(GeneralData.LANGUAGE));
			dateField.setDateFormat("dd/MM/yy"); //$NON-NLS-1$
			dateField.addValueChangeListener(evt->{
				Date date = Date.from(evt.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
				patex.setPex_date(new Timestamp(date.getTime()));
			});
		}
		return dateField;
	}

	private TextArea getNoteTextArea() {
		if (noteTextArea == null) {
			noteTextArea = new TextArea();//row 6 column 20
			noteTextArea.setMaxLength(300);
			// noteTextArea.setMargin(new Insets(0, 5, 0, 0));
			noteTextArea.addValueChangeListener(e->{
				// super.focusLost(e);
				patex.setPex_note(noteTextArea.getValue());
			});
		}
		return noteTextArea;
	}

	private NumberField getMinAPTextField() {
		if (minAPTextField == null) {
			minAPTextField = new NumberField();
			minAPTextField.addValueChangeListener(e->{
				patex.setPex_pa_min(Integer.valueOf(minAPTextField.getValue()));
			});
		}
		return minAPTextField;
	}
	
	private NumberField getMaxAPTextField() {
		if (maxAPTextField == null) {
			maxAPTextField = new NumberField();
			maxAPTextField.addValueChangeListener(e->{
				patex.setPex_pa_max(Integer.valueOf(maxAPTextField.getValue()));
			});
		}
		return maxAPTextField;
	}

	private NumberField getHeightTextField() {
		if (heightTextField == null) {
			heightTextField = new NumberField();//5col
			heightTextField.addValueChangeListener(e->{
				int height = Integer.parseInt(heightTextField.getValue());
				heightSlider.setValue((double)height);
				patex.setPex_height(height);
			});
		}
		return heightTextField;
	}
	
	private Slider getWeightSlider() {
		if (weightSlider == null) {
			weightSlider = new Slider(0, 2000);//max 4000
			weightSlider.addValueChangeListener(e->{
				int value = weightSlider.getValue().intValue();
				double weight = (double) value / 10;
				weightTextField.setValue(String.valueOf(weight));
				patex.setPex_weight(weight);
				updateBMI();
			});
		}
		return weightSlider;
	}
	
	private TextField getWeightTextField() {
		if (weightTextField == null) {
			weightTextField = new TextField();
			weightTextField.addValueChangeListener(e->{
				double weight = Double.parseDouble(weightTextField.getValue());
				weightSlider.setValue(Double.valueOf(convertFromDoubleToInt(weight, ExaminationParameters.WEIGHT_MIN, ExaminationParameters.WEIGHT_STEP, ExaminationParameters.WEIGHT_MAX)));
				patex.setPex_weight(weight);
			});
		}
		return weightTextField;
	}
	
	private TextField getTempTextField() {
		if (tempTextField == null) {
			tempTextField = new TextField();
			tempTextField.addValueChangeListener(e->{
				double temp = Double.parseDouble(tempTextField.getValue());
				tempSlider.setValue(Double.valueOf(convertFromDoubleToInt(temp, ExaminationParameters.TEMP_MIN, ExaminationParameters.TEMP_STEP, ExaminationParameters.TEMP_MAX)));
				patex.setPex_temp(temp);
			});
		}
		return tempTextField;
	}
	
	private TextField getSaturationTextField() {
		if (saturationTextField == null) {
			saturationTextField = new TextField();
			saturationTextField.addValueChangeListener(e->{
				double sat = Double.parseDouble(saturationTextField.getValue());
				saturationSlider.setValue(Double.valueOf(convertFromDoubleToInt(sat, ExaminationParameters.SAT_MIN, ExaminationParameters.SAT_STEP, ExaminationParameters.SAT_MAX)));
				patex.setPex_sat(sat);
			});
		}
		return saturationTextField;
	}
	
	private NumberField getHRTextField() {
		if (hrTextField == null) {
			hrTextField = new NumberField();
			hrTextField.addValueChangeListener(e->{
				int hr = Integer.parseInt(hrTextField.getValue());
				hrSlider.setValue(Double.valueOf(hr));
				patex.setPex_fc(hr);
			});
		}
		return hrTextField;
	}

	private Slider getHeightSlider() {
		if (heightSlider == null) {
			heightSlider = new Slider(0, 250);
			heightSlider.addValueChangeListener(e->{
				int height = (heightSlider.getValue()).intValue();
				heightTextField.setValue(String.valueOf(height));
				patex.setPex_height(height);
				updateBMI();
			});
		}
		return heightSlider;
	}
	
	private int convertFromDoubleToInt(double value, double min, double step, double max) {
		if (value > max) {
			return (int) (max * (1. / step));
		} else if (value < step) {
			return 0;
		} else {
			return (int) Math.round(value * (1. / step));
		}
	}
	
	private Slider getTempSlider() {
		if (tempSlider == null) {
			tempSlider = new Slider(0, 500);
			tempSlider.addValueChangeListener(e->{
				int value = (tempSlider.getValue()).intValue();
				double temp = (double) value / 10;
				tempTextField.setValue(String.valueOf(temp));
				patex.setPex_temp(temp);
			});
		}
		return tempSlider;
	}
	
	private Slider getSaturationSlider() {
		if (saturationSlider == null) {
			saturationSlider = new Slider(0, 1000); //MAX / STEP
			saturationSlider.addValueChangeListener(e->{
				int value = (saturationSlider.getValue()).intValue();
				double sat = (double) value / 10;
				saturationTextField.setValue(String.valueOf(sat));
				patex.setPex_sat(sat);
			});
		}
		return saturationSlider;
	}
	
	private Slider getHRSlider() {
		if (hrSlider == null) {
			hrSlider = new Slider(0, 200);
			hrSlider.addValueChangeListener(e->{
				int hr = (hrSlider.getValue()).intValue();
				hrTextField.setValue(String.valueOf(hr));
				patex.setPex_fc(hr);
			});
		}
		return hrSlider;
	}
	
	private Button getOKButton() {
		if (okButton == null) {
			okButton = new Button(MessageBundle.getMessage("angal.common.savem"));
			okButton.setDescription(MessageBundle.getMessage("angal.examination.tooltip.savepatientexamination"));
			okButton.setClickShortcut(KeyEvent.VK_O);
			okButton.addClickListener(e->{				
				ExaminationOperations ioOperations = new ExaminationOperations();
				ioOperations.saveOrUpdate(patex);
				close();
			});
		}
		return okButton;
	}
	
	private Button getCancelButton() {
		if (jButtonCancel == null) {
			jButtonCancel = new Button(MessageBundle.getMessage("angal.common.cancel")); //$NON-NLS-1$
			jButtonCancel.setClickShortcut(KeyEvent.VK_C);
			jButtonCancel.addClickListener(e->{
				close();
			});
		}
		return jButtonCancel;
	}
	
	private AbsoluteLayout getGenderLayout() {
		if (genderLayout == null) {
			genderLayout = new AbsoluteLayout();
			genderLayout.setWidth("150px");
			genderLayout.setHeight("400px");
			genderLayout.addComponent(getGenderImage());//qqpos
			genderLayout.addComponent(getBMILabel());
			
		}
		return genderLayout;
	}
	
	private Label getBMILabel() {
		if (bmiLabel == null) {
			bmiLabel = new Label();
			bmiLabel.setWidth("150px");
			// bmiLabel.setFont(new Font("Arial", Font.BOLD, 14));
			bmiLabel.setContentMode(ContentMode.HTML);
			// bmiLabel.setEditable(false);
			// bmiLabel.setOpaque(false);
			// bmiLabel.setMinimumSize(new Dimension(132, 300));
			// bmiLabel.setPreferredSize(new Dimension(132, 300));
			// bmiLabel.setMaximumSize(new Dimension(132, 300));
		}
		return bmiLabel;
	}

	private Image getGenderImage(){
		if (jLabelGender == null) {
			String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
			FileResource resource;
			if (isMale)
				resource = new FileResource(new File(basepath +PATH_MALE_GENDER));
			else
				resource = new FileResource(new File(basepath +PATH_FEMALE_GENDER));
			jLabelGender = new Image(null,resource);
			// jLabelGender.setAlignmentX(0.5f);
			// jLabelGender.setAlignmentY(0.5f);
		}
		return jLabelGender;
	}
	
	private void enableAP() {
		minAPTextField.setEnabled(true);
		patex.setPex_pa_min(Integer.parseInt(minAPTextField.getValue()));
		maxAPTextField.setEnabled(true);
		patex.setPex_pa_max(Integer.parseInt(maxAPTextField.getValue()));
	}

	private void disableAP() {
		minAPTextField.setEnabled(false);
		patex.setPex_pa_min(0);
		maxAPTextField.setEnabled(false);
		patex.setPex_pa_max(0);
	}
	
	private void enableTemp() throws NumberFormatException {
		tempSlider.setEnabled(true);
		tempTextField.setEnabled(true);
		String text = tempTextField.getValue();
		if (!text.equals("")) {
			patex.setPex_temp(Double.parseDouble(text));
		} else {
			patex.setPex_temp(0);
		}
	}

	private void disableTemp() {
		tempSlider.setEnabled(false);
		tempTextField.setEnabled(false);
		patex.setPex_temp(0);
	}
	
	private void enableSaturation() throws NumberFormatException {
		saturationSlider.setEnabled(true);
		saturationTextField.setEnabled(true);
		String text = saturationTextField.getValue();
		if (!text.equals("")) {
			patex.setPex_sat(Double.parseDouble(text));
		} else {
			patex.setPex_sat(0);
		}
	}

	private void disableSaturation() {
		saturationSlider.setEnabled(false);
		saturationTextField.setEnabled(false);
		patex.setPex_sat(0);
	}
	
	private void enableHR() throws NumberFormatException {
		hrSlider.setEnabled(true);
		hrTextField.setEnabled(true);
		patex.setPex_fc(Integer.parseInt(hrTextField.getValue()));
	}

	private void disableHR() {
		hrSlider.setEnabled(false);
		hrTextField.setEnabled(false);
		patex.setPex_fc(0);
	}
	
	private Panel getSummaryPanel() {
		if (summaryPanel == null) {
			summaryPanel = new Panel();
			summaryPanel.setContent(getSummaryLabel());
		}
		return summaryPanel;
	}

	private Label getSummaryLabel() {
		if (summaryLabel == null) {
			summaryLabel = new Label();
			// summaryLabel.setFont(new Font("Arial", Font.PLAIN, 11));
			summaryLabel.setContentMode(ContentMode.HTML);
			// summaryLabel.setEditable(false);
			// summaryLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
			
		}
		return summaryLabel;
	}
	
//	/**
//	 * Launch the application.
//	 */
//	public static void main(String[] args) {
//		
//		/*
//		 * Default Values
//		 */
//		final int INITIAL_HEIGHT = 170;
//		final int INITIAL_WEIGHT = 80;
//		final int INITIAL_AP_MIN = 80;
//		final int INITIAL_AP_MAX = 120;
//		final int INITIAL_HR = 60;
//		
//		try {
//			
//			PatientExamination patex = new PatientExamination();
//			Patient patient = new Patient();
//			patient.setCode(1);
//			patex.setPatient(patient);
//			patex.setPex_date(new Timestamp(new Date().getTime()));
//			patex.setPex_height(INITIAL_HEIGHT);
//			patex.setPex_weight(INITIAL_WEIGHT);
//			patex.setPex_pa_min(INITIAL_AP_MIN);
//			patex.setPex_pa_max(INITIAL_AP_MAX);
//			patex.setPex_fc(INITIAL_HR);
//			
//			GenderPatientExamination gpatex = new GenderPatientExamination(patex, false);
//			
//			PatientExaminationEdit dialog = new PatientExaminationEdit(gpatex);
//			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
//			dialog.pack();
//			dialog.setLocationRelativeTo(null);
//			dialog.setVisible(true);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
}
