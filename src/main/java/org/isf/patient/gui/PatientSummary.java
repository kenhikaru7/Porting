package org.isf.patient.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import java.io.File;
import com.vaadin.server.FileResource;
import com.vaadin.server.VaadinService;

// import com.vaadin.ui.Button;
// import com.vaadin.ui.FormLayout;
// import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
// import com.vaadin.ui.TextField;
// import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import com.vaadin.shared.ui.ContentMode;

import org.isf.generaldata.MessageBundle;
import org.isf.patient.model.Patient;
import org.isf.utils.time.TimeTools;
import org.isf.utils.Logging;

/**
 * A class to compose a summary of the data of a given patient
 * 
 * @author flavio
 * 
 */
public class PatientSummary {
	private String resPath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
	private Patient patient;
	
	private int maximumWidth = 350;
	private int borderTickness = 10;
	private int imageMaxWidth = 140;

	private Logging logger = new Logging();

	public PatientSummary(Patient patient) {
		// super();
		this.patient = patient;
	}

	/**
	 * a short summary in AdmissionBrowser
	 * 
	 * @return
	 */
	public JPanel getPatientDataPanel() {
		JPanel p = new JPanel(new BorderLayout(borderTickness, borderTickness));

		// p.addComponent(getPatientTitlePanel(), BorderLayout.NORTH);
		JPanel dataPanel = null;
		dataPanel = new JPanel();
		// dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));
		// dataPanel.add(setMyBorder(getPatientNamePanel(), MessageBundle.getMessage("angal.admission.namem")));
		// dataPanel.add(setMyBorder(getPatientTaxCodePanel(), MessageBundle.getMessage("angal.admission.taxcode")));
		// dataPanel.add(setMyBorder(getPatientSexPanel(), MessageBundle.getMessage("angal.admission.sexm")));
		// dataPanel.add(setMyBorder(getPatientAgePanel(), MessageBundle.getMessage("angal.admission.agem")));
		// dataPanel.add(setMyBorder(getPatientNotePanel(), MessageBundle.getMessage("angal.admission.patientnotes")));
		// p.addComponent(dataPanel, BorderLayout.CENTER);

		return p;
	}

	/**
	 * create and returns a JPanel with all patient's informations
	 * 
	 * @return
	 */
	public VerticalLayout getPatientCompleteSummary() {

		VerticalLayout p = new VerticalLayout();

		p.addComponent(getPatientCard());
		
		JPanel dataPanel = null;
		dataPanel = new JPanel();
		dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));

		p.addComponent(getPatientTaxCodePanel());
		p.addComponent(getPatientAddressAndCityLayout());
		p.addComponent(getPatientParentNewsPanel());
		p.addComponent(getPatientKinAndTelephoneLayout());
		p.addComponent(getPatientBloodAndEcoLayout());
		
		p.addComponent(getPatientNotePanel());
		
// 		Dimension dim = p.getPreferredSize();
// 		p.setMaximumSize(new Dimension(maximumWidth, dim.height));

		return p;
	}

	private HorizontalLayout getPatientAddressAndCityLayout() {
		HorizontalLayout addressAndCityLayout = null;
		addressAndCityLayout = new HorizontalLayout();
		addressAndCityLayout.addComponent(getPatientAddressPanel());
		addressAndCityLayout.addComponent(getPatientCityPanel());
		return addressAndCityLayout;
	}

	private HorizontalLayout getPatientBloodAndEcoLayout() {
		HorizontalLayout tempLayout = null;
		tempLayout = new HorizontalLayout();
		tempLayout.addComponent(getPatientBloodTypePanel());
		tempLayout.addComponent(getPatientEcoStatusPanel());
		return tempLayout;
	}

	private HorizontalLayout getPatientKinAndTelephoneLayout() {
		HorizontalLayout tempLayout = null;
		tempLayout = new HorizontalLayout();
		tempLayout.addComponent(getPatientKinPanel());
		tempLayout.addComponent(getPatientTelephonePanel());
		return tempLayout;
	}

	final int insetSize = 5;

	private JPanel getPatientTitlePanel() {
		JLabel l = new JLabel(MessageBundle.getMessage("angal.admission.patientsummary") + " (" + MessageBundle.getMessage("angal.common.code") + ": " + patient.getCode() + ")");
		l.setBackground(Color.CYAN);
		JPanel lP = new JPanel(new FlowLayout(FlowLayout.CENTER, insetSize, insetSize));
		// lP.setContent(l);
		return lP;
	}
	
	private Image scaleImage(int maxDim, Image photo) {
		double scale = (double) maxDim / (double) photo.getHeight(null);
		if (photo.getWidth(null) > photo.getHeight(null))
		{
			scale = (double) maxDim / (double) photo.getWidth(null);
		}
		int scaledW = (int) (scale * photo.getWidth(null));
		int scaledH = (int) (scale * photo.getHeight(null));
		
		return photo.getScaledInstance(scaledW, scaledH, Image.SCALE_SMOOTH);
	}
	
	private HorizontalLayout getPatientCard() {
		HorizontalLayout cardLayout = new HorizontalLayout();
		// cardLayout.setLayout(new BoxLayout(cardLayout, BoxLayout.X_AXIS));
		// cardLayout.setBackground(Color.WHITE);
		// cardLayout.setBorder(BorderFactory.createEmptyBorder(insetSize, insetSize, insetSize, insetSize));
		
		VerticalLayout patientData = new VerticalLayout();
		// patientData.setLayout(new BoxLayout(patientData, BoxLayout.Y_AXIS));
		// patientData.setBackground(Color.WHITE);
		// patientData.setBorder(BorderFactory.createEmptyBorder(insetSize, insetSize, insetSize, insetSize));
		
		if (patient == null) patient = new Patient();
		Integer code = patient.getCode();
		Label patientCode = null;
		if (code != null) 
			patientCode = new Label(MessageBundle.getMessage("angal.common.code") + ": " + code.toString());
		else 
			patientCode = new Label(" ");
		Label patientName = new Label(MessageBundle.getMessage("angal.patient.name") + ": " + filtra(patient.getName()));
		Label patientAge = new Label(MessageBundle.getMessage("angal.patient.age") + ": " + TimeTools.getFormattedAge(patient.getBirthDate()));
		Label patientSex = new Label(MessageBundle.getMessage("angal.patient.sex") + ": " + patient.getSex());
		Label patientTOB = new Label(MessageBundle.getMessage("angal.patient.tobm") + ": " + filtra(patient.getBloodType()));
		
		Label patientPhoto = new Label();
		Image photo = patient.getPhoto();
		
		if (photo != null) {
			// patientPhoto.setIcon(new ImageIcon(scaleImage(imageMaxWidth, photo)));
		} else {
			try {
				patientPhoto.setIcon(new FileResource(new File(resPath+"/WEB-INF/images/nophoto.png")));
			} catch (Exception ioe) {
				System.out.println("rsc/images/nophoto.png is missing...");
			}
		}
		
		patientData.addComponent(patientCode);
		// patientData.addComponent(Box.createVerticalStrut(insetSize));
		patientData.addComponent(patientName);
		patientData.addComponent(patientAge);
		patientData.addComponent(patientSex);
		// patientData.addComponent(Box.createVerticalGlue());
		patientData.addComponent(patientTOB);
		
		cardLayout.addComponent(patientPhoto);
		// cardLayout.addComponent(Box.createHorizontalStrut(insetSize));
		cardLayout.addComponent(patientData);
		return cardLayout;
	}

	private String filtra(String string) {
		if (string == null) return " ";
		if (string.equalsIgnoreCase("Unknown")) return " ";
		return string;
	}
	
	private JPanel getPatientNamePanel() {
		JLabel l = new JLabel(patient.getSecondName() + " " + patient.getFirstName());
		JPanel lP = new JPanel(new FlowLayout(FlowLayout.LEFT, insetSize, insetSize));
		// lP.setContent(l);
		return lP;
	}

	private Panel getPatientTaxCodePanel() {
		Label l = new Label(patient.getTaxCode() + " ");
		// Panel lP = new Panel(new FlowLayout(FlowLayout.LEFT, insetSize, insetSize));
		Panel lP = new Panel(MessageBundle.getMessage("angal.admission.taxcode"));
		lP.setContent(l);
		return lP;
	}

	private Panel getPatientKinPanel() {
		Label l = null;
		if (patient.getNextKin() == null || patient.getNextKin().equalsIgnoreCase("")) {
			l = new Label(" ");
		} else {
			l = new Label(patient.getNextKin());
		}
		Panel lP = new Panel(MessageBundle.getMessage("angal.admission.nextkin"));
		lP.setContent(l);
		return lP;
	}

	private Panel getPatientTelephonePanel() {
		Label l = null;
		if (patient.getTelephone() == null || patient.getTelephone().equalsIgnoreCase("")) {
			//l = new JLabel(MessageBundle.getMessage("angal.admission.unknown"));
			l = new Label(" ");
		} else {
			l = new Label("" + patient.getTelephone());
		}
		Panel lP = new Panel(MessageBundle.getMessage("angal.admission.telephone"));
		lP.setContent(l);
		return lP;
	}
	
	private Panel getPatientAddressPanel() {
		Label l = null;
		if (patient.getAddress() == null || patient.getAddress().equalsIgnoreCase("")) {
			l = new Label(" ");
		} else {
			l = new Label("" + patient.getAddress());
		}
		Panel lP = new Panel(MessageBundle.getMessage("angal.admission.addressm"));
		lP.setContent(l);
		return lP;
	}

	private Panel getPatientCityPanel() {
		Label l = null;
		if (patient.getCity() == null || patient.getCity().equalsIgnoreCase("")) {
			l = new Label(" ");
		} else {
			l = new Label("" + patient.getCity());
		}
		Panel lP = new Panel(MessageBundle.getMessage("angal.admission.city"));
		lP.setContent(l);
		return lP;
	}

	// Panel for Blood Type
	private Panel getPatientBloodTypePanel() {
		Label l = null;
		String c = new String(patient.getBloodType());
		if (c == null || c.equalsIgnoreCase("Unknown")) {
			l = new Label(MessageBundle.getMessage("angal.admission.bloodtypeisunknown"));
			l = new Label(" ");
		} else {
			l = new Label(c); // Added - Bundle is not necessary here
		}
		Panel lP = new Panel(MessageBundle.getMessage("angal.admission.bloodtype"));
		lP.setContent(l);
		return lP;
	}

	private Panel getPatientEcoStatusPanel() {
		Label l = null;
		char c = patient.getHasInsurance();
		if (c == 'Y') {
			l = new Label(MessageBundle.getMessage("angal.admission.hasinsuranceyes"));
		} else if (c == 'N') {
			l = new Label(MessageBundle.getMessage("angal.admission.hasinsuranceno"));
		} else {
			l = new Label(MessageBundle.getMessage("angal.admission.unknown"));
			l = new Label(" ");
		}
		Panel lP = new Panel(MessageBundle.getMessage("angal.admission.insurance"));
		lP.setContent(l);
		return lP;
	}

	private JPanel getPatientAgePanel() {
		JLabel l = new JLabel("" + patient.getAge());
		JPanel lP = new JPanel(new FlowLayout(FlowLayout.LEFT, insetSize, insetSize));
		// lP.setContent(l);
		return lP;
	}

	private JPanel getPatientSexPanel() {
		JLabel l = new JLabel((patient.getSex() == 'F' ? MessageBundle.getMessage("angal.admission.female") : MessageBundle.getMessage("angal.admission.male")));
		JPanel lP = new JPanel(new FlowLayout(FlowLayout.LEFT, insetSize, insetSize));
		// lP.setContent(l);
		return lP;
	}

	private Panel getPatientParentNewsPanel() {
		StringBuffer labelBfr = new StringBuffer("<html>");
		if (patient.getMother() == 'A')
			labelBfr.append(MessageBundle.getMessage("angal.admission.motherisalive"));
		else if (patient.getMother() == 'D')
			labelBfr.append(MessageBundle.getMessage("angal.admission.motherisdead"));;
		// added
			labelBfr.append((patient.getMother_name() == null || patient.getMother_name().compareTo("") == 0 ? "<BR>" : "(" + patient.getMother_name() + ")<BR>"));
		if (patient.getFather() == 'A')
			labelBfr.append(MessageBundle.getMessage("angal.admission.fatherisalive"));
		else if (patient.getFather() == 'D')
			labelBfr.append(MessageBundle.getMessage("angal.admission.fatherisdead"));
		// added
		labelBfr.append((patient.getFather_name() == null || patient.getFather_name().compareTo("") == 0 ? "<BR>" : "(" + patient.getFather_name() + ")<BR>"));
		if (patient.getParentTogether() == 'Y')
			labelBfr.append(MessageBundle.getMessage("angal.admission.parentslivetoghether"));
		else if (patient.getParentTogether() == 'N')
			labelBfr.append(MessageBundle.getMessage("angal.admission.parentsnotlivingtogether"));
		else 
			labelBfr.append("<BR>");
		labelBfr.append("</html>");
		Label l = new Label(labelBfr.toString());
		l.setContentMode(ContentMode.HTML);
		Panel lP = new Panel(MessageBundle.getMessage("angal.admission.parents"));
		lP.setContent(l);
		return lP;
	}

	// alex: modified with scroolbar
	private Panel getPatientNotePanel() {
		TextArea textArea = new TextArea();//3, 40
		try{
			textArea.setValue(patient.getNote());
		}
		catch(Exception e){
			textArea.setValue("");
		}
		textArea.setEnabled(false);
		textArea.setWordWrap(true);

		// JScrollPane scrollPane = new JScrollPane(textArea);
		// scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		// scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		Panel lP = new Panel(MessageBundle.getMessage("angal.admission.patientnotes"));
		lP.setContent(textArea);

		return lP;
	}

	private JPanel setMyBorder(JPanel c, String title) {
		javax.swing.border.Border b1 = BorderFactory.createLineBorder(Color.lightGray);
		javax.swing.border.Border b2 = BorderFactory.createTitledBorder(b1, title, javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP);
		c.setBorder(b2);
		return c;
	}

}
