package org.isf.patient.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.EventListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import java.io.File;
import com.vaadin.server.FileResource;
import com.vaadin.server.VaadinService;

import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.patient.manager.PatientBrowserManager;
import org.isf.patient.model.Patient;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.jobjects.ModalWindow;
import org.isf.utils.Logging;

public class SelectPatient extends ModalWindow {
	
//LISTENER INTERFACE --------------------------------------------------------
	private EventListenerList selectionListener = new EventListenerList();
	
	public interface SelectionListener extends EventListener {
		public void patientSelected(Patient patient);
	}
	
	public void addSelectionListener(SelectionListener l) {
		selectionListener.add(SelectionListener.class, l);
		
	}
	
	private void fireSelectedPatient(Patient patient) {
		new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;};
		
		EventListener[] listeners = selectionListener.getListeners(SelectionListener.class);
		for (int i = 0; i < listeners.length; i++)
			((SelectionListener)listeners[i]).patientSelected(patient);
	}
//---------------------------------------------------------------------------	
	private String resPath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
	private static final long serialVersionUID = 1L;
	private HorizontalLayout buttonsLayout;
	private HorizontalLayout topLayout;
	private HorizontalLayout centerLayout;
	private Grid<Patient> patientGrid;
	private JScrollPane jScrollPaneTablePatient;
	private Button cancelButton;
	private Button buttonSelect;
	private Label searchLabel;
	private TextField searchPatientTextField;
	private Button searchButton;
	private VerticalLayout patientDataLayout;
	private Patient patient;
	private PatientSummary ps;
	private String[] patColums = { MessageBundle.getMessage("angal.common.code"), MessageBundle.getMessage("angal.patient.name") }; 
	private int[] patColumsWidth = { 100, 250 };
	private boolean[] patColumsResizable = { false, true };

	private Logging logger = new Logging();

	PatientBrowserManager patManager = new PatientBrowserManager();
	ArrayList<Patient> patArray = new ArrayList<Patient>();
	ArrayList<Patient> patSearch = new ArrayList<Patient>();
	private String lastKey = "";
	
	public SelectPatient(Patient pat) {
		// super(owner, true);
		if (!GeneralData.ENHANCEDSEARCH) {
			patArray = patManager.getPatientWithHeightAndWeight(null);
			patSearch = patArray;
		}
		if (pat == null) {
			patient = null;
		} else
			patient = pat;
		ps = new PatientSummary(patient);
		initComponents();
		// addWindowListener(new WindowAdapter(){
			
		// 	public void windowClosing(WindowEvent e) {
		// 		//to free memory
		// 		patArray.clear();
		// 		patSearch.clear();
		// 		close();
		// 	}			
		// });
		// setLocationRelativeTo(null);
	}
	
	public SelectPatient(JDialog owner, String search) {
		// super(owner, true);
		// if (!GeneralData.ENHANCEDSEARCH) {
		// 	patArray = patManager.getPatientWithHeightAndWeight(null);
		// 	patSearch = patArray;
		// }
		// ps = new PatientSummary(patient);
		// initComponents();
		// addWindowListener(new WindowAdapter(){
			
		// 	public void windowClosing(WindowEvent e) {
		// 		//to free memory
		// 		patArray.clear();
		// 		patSearch.clear();
		// 		close();
		// 	}			
		// });
		// setLocationRelativeTo(null);
		// searchPatientTextField.setText(search);
		// if (GeneralData.ENHANCEDSEARCH) {
		// 	searchButton.doClick();
		// }
	}

	private void initComponents(){
		UI.getCurrent().addWindow(this);
		VerticalLayout windowContent = new VerticalLayout();
		showAsModal();
		setContent(windowContent);
		windowContent.addComponent(getTopLayout());
		windowContent.addComponent(getCenterLayout());
		windowContent.addComponent(getButtonsLayout());
		setCaption(MessageBundle.getMessage("angal.patient.patientselection"));
		// pack();
	}

	private VerticalLayout getPatientDataLayout(){
		if (patientDataLayout == null) {
			patientDataLayout = ps.getPatientCompleteSummary();
			patientDataLayout.setMargin(false);
		}
		return patientDataLayout;
	}

	private TextField getSearchPatientTextField(){
		if (searchPatientTextField == null) {
			searchPatientTextField = new TextField();//20col
			searchPatientTextField.setMaxLength(100);
			searchPatientTextField.setValue("");
			searchPatientTextField.selectAll();
			if (GeneralData.ENHANCEDSEARCH) {
				searchPatientTextField.addValueChangeListener(e->{
	
					// public void keyPressed(KeyEvent e) {
					// 	int key = e.getKeyCode();
					//      if (key == KeyEvent.VK_ENTER) {
					//     	 searchButton.doClick();
					//      }
					// }
	
					// public void keyReleased(KeyEvent e) {
					// }
	
					// public void keyTyped(KeyEvent e) {
					// }
				});
			} else {
				searchPatientTextField.addValueChangeListener(e->{
					lastKey = "";
					String s = "" + e.getValue();
					if (e.getValue().matches("[A-Za-z0-9]+")) {
						lastKey = s;
					}
					filterPatient();
				});
			}
		}
		return searchPatientTextField;
	}

	private void filterPatient(){
		
		String s = searchPatientTextField.getValue();
		s.trim();
		String[] s1 = s.split(" ");
		
		//System.out.println(s);

		patSearch = new ArrayList<Patient>();
		for (Patient pat : patArray) {
			if (!s.equals("")) {
				String name = pat.getSearchString();
				int a = 0;
				for (int i = 0; i < s1.length ; i++) {
					if (name.contains(s1[i].toLowerCase())) {
						a++;
					}
				}
				if (a == s1.length) patSearch.add(pat);
			} else {
				patSearch.add(pat);
			}
		}
		if (patSearch.size() == 0) {
			
			patient = null;
			updatePatientSummary();
		}
		if (patSearch.size() == 1) {
			
			patient = patSearch.get(0);
			updatePatientSummary();
		}
		patientGrid.setItems(patSearch);
		searchPatientTextField.focus();
	}
	
	private Label getSearchLabel(){
		if (searchLabel == null) {
			searchLabel = new Label();
			searchLabel.setValue(MessageBundle.getMessage("angal.patient.searchpatient"));
		}
		return searchLabel;
	}

	private Button getSelectButton(){
		if (buttonSelect == null) {
			buttonSelect = new Button();
			////buttonSelect.setClickShortcut(KeyEvent.VK_S);
			buttonSelect.setCaption(MessageBundle.getMessage("angal.patient.select"));
			buttonSelect.addClickListener(e->{
				if (patient != null) {
					//to free memory
					patArray.clear();
					patSearch.clear();
					fireSelectedPatient(patient);
					close();
				} else return;				
			});
		}
		return buttonSelect;
	}

	private Button getCancelButton(){
		if (cancelButton == null) {
			cancelButton = new Button();
			////cancelButton.setClickShortcut(KeyEvent.VK_C);
			cancelButton.setCaption(MessageBundle.getMessage("angal.common.cancel"));
			cancelButton.addClickListener(e->{
				//to free memory
				patArray.clear();
				patSearch.clear();
				close();
			});
		}
		return cancelButton;
	}

	private Grid getPatientGrid(){
		if (patientGrid == null) {
			patientGrid = new Grid();
			patientGrid.setItems(patSearch);
			patientGrid.addColumn(Patient::getCode).setCaption("Code");
			patientGrid.addColumn(Patient::getName).setCaption("Name");
			// for (int i = 0 ; i < patColums.length; i++) {
			// 	patientGrid.getColumnModel().getColumn(i).setMinWidth(patColumsWidth[i]);
			// 	if (!patColumsResizable[i]) patientGrid.getColumnModel().getColumn(i).setMaxWidth(patColumsWidth[i]);
			// }
			// patientGrid.setAutoCreateColumnsFromModel(false);
			// patientGrid.getColumnModel().getColumn(0).setCellRenderer(new CenterTableCellRenderer());
			
			patientGrid.addItemClickListener(e->{
				// if (!e.getValueIsAdjusting()) {
				int index = e.getRowIndex();
				// patient = (Patient)patientGrid.getValueAt(index, -1);
				patient = e.getItem();
				updatePatientSummary();	
				// }
			});
			// ListSelectionModel listSelectionModel = patientGrid.getSelectionModel();
			// listSelectionModel.addListSelectionListener(new ListSelectionListener(){

			// 	public void valueChanged(ListSelectionEvent e) {
			// 		if (!e.getValueIsAdjusting()) {
						
			// 			int index = patientGrid.getSelectedRow();
			// 			patient = (Patient)patientGrid.getValueAt(index, -1);
			// 			updatePatientSummary();
						
			// 		}
			// 	}
			// });
			
			// patientGrid.addMouseListener(new MouseListener(){
				
			// 	public void mouseReleased(MouseEvent e) {}
				
			// 	public void mousePressed(MouseEvent e) {}
				
			// 	public void mouseExited(MouseEvent e) {}
				
			// 	public void mouseEntered(MouseEvent e) {}
				
			// 	public void mouseClicked(MouseEvent e) {
			// 		if (e.getClickCount() == 2 && !e.isConsumed()) {
			// 			e.consume();
			// 			buttonSelect.doClick();
			// 		}
			// 	}
			// });
		}
		return patientGrid;
	}

	private void updatePatientSummary(){
		centerLayout.removeComponent(patientDataLayout);
		ps = new PatientSummary(patient);
		patientDataLayout = ps.getPatientCompleteSummary();
		// patientDataLayout.setAlignmentY(Box.TOP_ALIGNMENT);
		
		centerLayout.addComponent(patientDataLayout);
		// centerLayout.validate();
		// centerLayout.repaint();
	}
	
	private HorizontalLayout getCenterLayout(){
		if (centerLayout == null) {
			centerLayout = new HorizontalLayout();
			centerLayout.addComponent(getPatientGrid());
			centerLayout.addComponent(getPatientDataLayout());

			// if (patient != null) {
			// 	for (int i = 0; i < patSearch.size(); i++) {
			// 		if (patSearch.get(i).getCode().equals(patient.getCode())) {
			// 			patientGrid.addRowSelectionInterval(i, i);
			// 			int j = 0;
			// 			if (i > 10) j = i-10; //to center the selected row
			// 			patientGrid.scrollRectToVisible(patientGrid.getCellRect(j,i,true));
			// 			break;
			// 		}
			// 	}
			// }
		}
		return centerLayout;
	}

	private HorizontalLayout getTopLayout(){
		if (topLayout == null) {
			topLayout = new HorizontalLayout();
			topLayout.addComponent(getSearchLabel());
			topLayout.addComponent(getSearchPatientTextField());
			if (GeneralData.ENHANCEDSEARCH) topLayout.addComponent(getSearchButton());
		}
		return topLayout;
	}

	private Button getSearchButton(){
		if (searchButton == null) {
			searchButton = new Button();	
			searchButton.setIcon(new FileResource(new File(resPath+"/WEB-INF/icons/zoom_r_button.png")));
			// searchButton.setPreferredSize(new Dimension(20, 20));
			searchButton.addClickListener(e->{
				patArray = patManager.getPatientWithHeightAndWeight(searchPatientTextField.getValue());
				filterPatient();
			});
		}
		return searchButton;
	}

	private HorizontalLayout getButtonsLayout(){
		if (buttonsLayout == null) {
			buttonsLayout = new HorizontalLayout();
			buttonsLayout.addComponent(getSelectButton());
			buttonsLayout.addComponent(getCancelButton());
		}
		return buttonsLayout;
	}

	class CenterTableCellRenderer extends DefaultTableCellRenderer {  
		   
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
				boolean hasFocus, int row, int column) {  
		   
			Component cell=super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
			cell.setForeground(Color.BLACK);
			setHorizontalAlignment(CENTER);	   
			return cell;
	   }
	}
}
