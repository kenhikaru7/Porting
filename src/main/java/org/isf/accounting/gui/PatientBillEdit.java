package org.isf.accounting.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EventListener;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.time.ZoneId;
import java.time.LocalDateTime;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import java.io.File;
import com.vaadin.server.FileResource;
import com.vaadin.shared.ui.ContentMode;

import com.vaadin.ui.renderers.HtmlRenderer;

import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateTimeField;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import de.steinwedel.messagebox.MessageBox;

import org.isf.accounting.manager.BillBrowserManager;
import org.isf.accounting.model.Bill;
import org.isf.accounting.model.BillItems;
import org.isf.accounting.model.BillPayments;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.generaldata.TxtPrinter;
import org.isf.hospital.manager.HospitalBrowsingManager;
import org.isf.menu.gui.MainMenu;
import org.isf.patient.gui.SelectPatient;
import org.isf.patient.gui.SelectPatient.SelectionListener;
import org.isf.patient.manager.PatientBrowserManager;
import org.isf.patient.model.Patient;
import org.isf.priceslist.manager.PriceListManager;
import org.isf.priceslist.model.List;
import org.isf.priceslist.model.Price;
import org.isf.pricesothers.manager.PricesOthersManager;
import org.isf.pricesothers.model.PricesOthers;
// import org.isf.stat.manager.GenericReportBill;
import org.isf.utils.Logging;
import org.isf.utils.time.RememberDates;
import org.isf.utils.jobjects.ModalWindow;

import com.toedter.calendar.JDateChooser;
/**
 * Create a single Patient Bill
 * it affects tables BILLS, BILLITEMS and BILLPAYMENTS
 * 
 * @author Mwithi
 * 
 */
public class PatientBillEdit extends ModalWindow implements SelectionListener {

//LISTENER INTERFACE --------------------------------------------------------
	private java.util.List<PatientBillListener> patientBillListeners = new ArrayList<PatientBillListener>();
	
	public interface PatientBillListener{
		public void billInserted(Bill aBill);
	}
	
	public void addPatientBillListener(PatientBillListener l) {
		patientBillListeners.add(l);
	}
	
	private void fireBillInserted(Bill aBill) {
		for(PatientBillListener patientBillListener : patientBillListeners){
			patientBillListener.billInserted(aBill);
		}
	}
//---------------------------------------------------------------------------
	
	public void patientSelected(Patient patient) {
		patientSelected = patient;
		ArrayList<Bill> patientPendingBills = billManager.getPendingBills(patient.getCode());
		logger.info("dklasfjn aldjfkasdjfasdlk");//qqq
		if (patientPendingBills.isEmpty()) {
			//BILL
			thisBill.setPatID(patientSelected.getCode());
			thisBill.setPatient(true);
			thisBill.setPatName(patientSelected.getName());
		} else {
			if (patientPendingBills.size() == 1) {
				MessageBox.create().withCaption(MessageBundle.getMessage("angal.admission.bill"))
				.withMessage(MessageBundle.getMessage("angal.admission.thispatienthasapendingbill")).open();
				setBill(patientPendingBills.get(0));
				insert = false;
			} else {
				MessageBox.createWarning().withCaption(MessageBundle.getMessage("angal.admission.bill"))
				.withMessage(MessageBundle.getMessage("angal.admission.thereismorethanonependingbillforthispatientcontinue")).open();
				return;
			}
		} 
		updateUI();
	}
	
	private static final long serialVersionUID = 1L;
	private Grid<BillItems> billGrid;
	private Button addMedicalButton;
	private Button addOperationButton;
	private Button addExamButton;
	private Button addOtherButton;
	private Button addPaymentButton;
	private VerticalLayout paymentAndActionButtonLayout;
	private HorizontalLayout dateLayout;
	private HorizontalLayout patientLayout;
	private Grid<BillPayments> paymentGrid;
	private TextField patientTextField;
	private ComboBox priceListComboBox;
	private Grid<TotalHeader> totalGrid;
	private Label totalLabel;
	private Grid<TotalHeader> bigTotalGrid;
	private Grid<TotalHeader> balanceGrid;
	private VerticalLayout topLayout;
	private DateTimeField dateCalendar;
	private Label dateLabel;
	private Label patientLabel;
	private Button removeItemButton;
	private Label priceListLabel;
	private Button removePaymentButton;
	private Button addRefundButton;
	private VerticalLayout paymentButtonsLayout;
	private VerticalLayout billButtons;
	private VerticalLayout actionButtonsLayout;
	private Button closeButton;
	private Button paidButton;
	private Button printPaymentButton;
	private Button saveButton;
	private Button balanceButton;
	private Button customButton;
	private Button pickPatientButton;
	private Button trashPatientButton;
	
	private static final Dimension PatientDimension = new Dimension(300,20);
	private static final Dimension LabelsDimension = new Dimension(60,20);
	private static final int PanelWidth = 450;
	private static final int ButtonWidth = 160;
	private static final int ButtonWidthBill = 160;
	private static final int ButtonWidthPayment = 160;
	private static final int PriceWidth = 150;
	private static final int CurrencyCodWidth = 40;
	private static final int QuantityWidth = 40;
	private static final int BillHeight = 200;
	private static final int TotalHeight = 20;
	private static final int BigTotalHeight = 20;
	private static final int PaymentHeight = 150;
	private static final int BalanceHeight = 20;
	//private static final int ActionHeight = 100;
	private static final int ButtonHeight = 25;
	
	private BigDecimal total = new BigDecimal(0);
	private BigDecimal bigTotal = new BigDecimal(0);
	private BigDecimal balance = new BigDecimal(0);
	private int billID;
	private List listSelected;
	private boolean insert;
	private boolean modified = false;
	private boolean keepDate = true;
	private boolean paid = false;
	private Bill thisBill;
	private Patient patientSelected;
	private boolean foundList;
	private GregorianCalendar billDate = new GregorianCalendar();
	private GregorianCalendar today = new GregorianCalendar();
	
	private Object[] billClasses = {Price.class, Integer.class, Double.class};
	private String[] billColumnNames = {MessageBundle.getMessage("angal.newbill.item"), MessageBundle.getMessage("angal.newbill.qty"), MessageBundle.getMessage("angal.newbill.amount")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private Object[] paymentClasses = {Date.class, Double.class};
	
	private String currencyCod = new HospitalBrowsingManager().getHospitalCurrencyCod();
	
	//Prices and Lists (ALL)
	private PriceListManager prcManager = new PriceListManager();
	private ArrayList<Price> prcArray = prcManager.getPrices();
	private ArrayList<List> lstArray = prcManager.getLists();
	
	//PricesOthers (ALL)
	private PricesOthersManager othManager = new PricesOthersManager();
	private ArrayList<PricesOthers> othPrices = othManager.getOthers();

	//Items and Payments (ALL)
	private BillBrowserManager billManager = new BillBrowserManager();
	private PatientBrowserManager patManager = new PatientBrowserManager();
	
	//Prices, Items and Payments for the tables
	private ArrayList<BillItems> billItems = new ArrayList<BillItems>();
	private ArrayList<BillPayments> payItems = new ArrayList<BillPayments>();
	private ArrayList<Price> prcListArray = new ArrayList<Price>();
	private int billItemsSaved;
	private int payItemsSaved;
	
	//User
	private String user = MainMenu.getUser();

	private Logging logger = new Logging();
	
	public PatientBillEdit() {
		PatientBillEdit newBill = new PatientBillEdit(new Bill(), true);
		newBill.setVisible(true);
	}
	
	public PatientBillEdit(JFrame owner, Patient patient) {
		Bill bill = new Bill();
		bill.setPatient(true);
		bill.setPatID(patient.getCode());
		bill.setPatName(patient.getName());
		PatientBillEdit newBill = new PatientBillEdit(bill, true);
		newBill.setPatientSelected(patient);
		newBill.setVisible(true);
	}
	
	public PatientBillEdit(Bill bill, boolean inserting) {
		// super(owner, true);
		this.insert = inserting;
		setBill(bill);
		initComponents();
		// updateTotals();
		// setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		// setLocationRelativeTo(null);
		// setResizable(false);
	}
	
	private void setBill(Bill bill) {
		this.thisBill = bill;
		billDate = bill.getDate();
		billItems = billManager.getItems(thisBill.getId());
		payItems = billManager.getPayments(thisBill.getId());
		billItemsSaved = billItems.size();
		payItemsSaved = payItems.size();
		if (!insert) {
			checkBill();
		}
	}
	
	private void initComponents() {
		showAsModal();
		UI.getCurrent().addWindow(this);
		VerticalLayout windowContent = new VerticalLayout();
		setContent(windowContent);
		HorizontalLayout centerLayout = new HorizontalLayout();
		VerticalLayout leftCenterLayout = new VerticalLayout();
		leftCenterLayout.addComponent(getTotalGrid());
		leftCenterLayout.addComponent(getBillGrid());
		centerLayout.addComponent(leftCenterLayout);
		centerLayout.addComponent(getBillButtons());
		HorizontalLayout bottomLayout = new HorizontalLayout();
		VerticalLayout leftBottomLayout = new VerticalLayout();
		leftBottomLayout.addComponent(getBigTotalGrid());
		leftBottomLayout.addComponent(getPaymentGrid());
		leftBottomLayout.addComponent(getBalanceGrid());
		bottomLayout.addComponent(leftBottomLayout);
		bottomLayout.addComponent(getPaymentAndActionButtonLayout());
		windowContent.addComponent(getTopLayout());
		windowContent.addComponent(centerLayout);
		windowContent.addComponent(bottomLayout);
		if (insert) {
			setCaption(MessageBundle.getMessage("angal.newbill.title"));  //$NON-NLS-1$
		} else {
			setCaption(MessageBundle.getMessage("angal.newbill.title") + " " + thisBill.getId());  //$NON-NLS-1$
		}
		// pack();
	}

	//check if PriceList and Patient still exist
	private void checkBill() {
		
		foundList = false;
		if (thisBill.isList()) {
			for (List list : lstArray) {
				
				if (list.getId() == thisBill.getListID()) {
					listSelected = list;
					foundList = true;
					break;
				}
			}
			// if (!foundList) { //PriceList not found
			// 	Icon icon = new ImageIcon("rsc/icons/list_dialog.png"); //$NON-NLS-1$
			// 	List list = (List)JOptionPane.showInputDialog(
			// 	                    PatientBillEdit.this,
			// 	                    MessageBundle.getMessage("angal.newbill.pricelistassociatedwiththisbillnolongerexists") + //$NON-NLS-1$
			// 	                    "no longer exists", //$NON-NLS-1$
			// 	                    MessageBundle.getMessage("angal.newbill.selectapricelist"), //$NON-NLS-1$
			// 	                    JOptionPane.OK_OPTION,
			// 	                    icon,
			// 	                    lstArray.toArray(),
			// 	                    ""); //$NON-NLS-1$
			// 	if (list == null) {
					
			// 		JOptionPane.showMessageDialog(PatientBillEdit.this,
			// 				MessageBundle.getMessage("angal.newbill.nopricelistselected.part1") + //$NON-NLS-1$
			// 				lstArray.get(0).getName() + MessageBundle.getMessage("angal.newbill.nopricelistselected.part2"), //$NON-NLS-1$
			// 				"Error", //$NON-NLS-1$
			// 				JOptionPane.WARNING_MESSAGE);
			// 		list = lstArray.get(0);
			// 	}
			// 	thisBill.setListID(list.getId());
			// 	thisBill.setListName(list.getName());
				
			// }
		}
				
		if (thisBill.isPatient()) {
			
			Patient patient = patManager.getPatient(thisBill.getPatID());
			// if (patient != null) {
			// 	patientSelected = patient;
			// } else {  //Patient not found
			// 	Icon icon = new ImageIcon("rsc/icons/patient_dialog.png"); //$NON-NLS-1$
			// 	JOptionPane.showMessageDialog(PatientBillEdit.this,
			// 			MessageBundle.getMessage("angal.newbill.patientassociatedwiththisbillnolongerexists") + //$NON-NLS-1$
	  //                   "no longer exists", //$NON-NLS-1$
	  //                   "Warning", //$NON-NLS-1$
			// 			JOptionPane.WARNING_MESSAGE,
			// 			icon);
				
			// 	thisBill.setPatient(false);
			// 	thisBill.setPatID(0);
			// }
		}
	}
	
	private HorizontalLayout getPatientLayout() {
		if (patientLayout == null) {
			patientLayout = new HorizontalLayout();
			patientLayout.addComponent(getPatientLabel());
			patientLayout.addComponent(getPatientTextField());
			patientLayout.addComponent(getPriceListLabel());
			patientLayout.addComponent(getPriceListComboBox());
		}
		return patientLayout;
	}

	private Label getPatientLabel() {
		if (patientLabel == null) {
			patientLabel = new Label();
			patientLabel.setValue(MessageBundle.getMessage("angal.newbill.patient")); //$NON-NLS-1$
			// patientLabel.setPreferredSize(LabelsDimension);
		}
		return patientLabel;
	}

	
	private TextField getPatientTextField() {
		if (patientTextField == null) {
			patientTextField = new TextField();
			patientTextField.setValue(""); //$NON-NLS-1$
			// patientTextField.setPreferredSize(PatientDimension);
			if (thisBill.isPatient()) {
				patientTextField.setValue(thisBill.getPatName());
			}
			patientTextField.setEnabled(false);
		}
		return patientTextField;
	}
	
	private Label getPriceListLabel() {
		if (priceListLabel == null) {
			priceListLabel = new Label();
			priceListLabel.setValue(MessageBundle.getMessage("angal.newbill.list")); //$NON-NLS-1$
		}
		return priceListLabel;
	}
	
	private ComboBox getPriceListComboBox() {
		if (priceListComboBox == null) {
			priceListComboBox = new ComboBox();
			priceListComboBox.setEmptySelectionAllowed(false);
			List list = null;
			for (List lst : lstArray) {
				if (!insert)
					if (lst.getId() == thisBill.getListID())
						list = lst;
			}
			priceListComboBox.setItems(lstArray);
			if (list != null){
				priceListComboBox.setValue(list);
			}else{
				priceListComboBox.setValue(lstArray.get(0));
			}
			
			priceListComboBox.addValueChangeListener(e->{
				listSelected = (List)priceListComboBox.getValue();
				updateBill();
				billGrid.setItems(billItems);
				updateTotals();
			});
		}
		return priceListComboBox;
	}
	private LocalDateTime nowLocalDate;
	private DateTimeField getDateCalendar() {//detik na 00
		if (dateCalendar == null) {
			
			if (insert) {
				//To remind last used
				billDate.set(Calendar.YEAR, RememberDates.getLastBillDateGregorian().get(Calendar.YEAR));
				billDate.set(Calendar.MONTH, RememberDates.getLastBillDateGregorian().get(Calendar.MONTH));
				billDate.set(Calendar.DAY_OF_MONTH, RememberDates.getLastBillDateGregorian().get(Calendar.DAY_OF_MONTH));
				dateCalendar = new DateTimeField("",billDate.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
			} else { 
				//get BillDate
				dateCalendar = new DateTimeField("",thisBill.getDate().getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
				billDate.setTime(Date.from(dateCalendar.getValue().atZone(ZoneId.systemDefault()).toInstant()));
			}
			nowLocalDate = dateCalendar.getValue();
			dateCalendar.setLocale(new Locale(GeneralData.LANGUAGE));
			dateCalendar.setDateFormat("dd/MM/yy - HH:mm:ss"); //$NON-NLS-1$
			dateCalendar.addValueChangeListener(evt->{ //$NON-NLS-1$
				if (!insert) {
					if (keepDate && evt.getValue().toString().compareTo(
						evt.getOldValue().toString()) != 0 && nowLocalDate.toString().compareTo(
						evt.getOldValue().toString()) != 0) {
						MessageBox.createQuestion().withCaption("Warning").withIcon(new Image(null,new FileResource(new File("D:/nyobavaadin/vaadin-archetype-application/src/main/webapp" +"/WEB-INF/icons/clock_dialog.png"))))
						.withMessage(MessageBundle.getMessage("angal.newbill.doyoureallywanttochangetheoriginaldate"))
						.withYesButton(()->{
							keepDate = false;
							modified = true;
							// dateCalendar.setValue(((Calendar)evt.getValue()).getTime());
						})
						.withNoButton(()->{
							dateCalendar.setValue(evt.getOldValue());
						})
						.open();
					} else {
						// dateCalendar.setValue(((Calendar)evt.getValue()).getTime());
					}
					billDate.setTime(Date.from(dateCalendar.getValue().atZone(ZoneId.systemDefault()).toInstant()));
				} else {
					// dateCalendar.setValue(((Calendar)evt.getValue()).getTime());
					billDate.setTime(Date.from(dateCalendar.getValue().atZone(ZoneId.systemDefault()).toInstant()));
				}
			});
		}
		return dateCalendar;
	}
	
	private Label getDateLabel() {
		if (dateLabel == null) {
			dateLabel = new Label();
			dateLabel.setValue(MessageBundle.getMessage("angal.common.date")); //$NON-NLS-1$
			// dateLabel.setPreferredSize(LabelsDimension);
		}
		return dateLabel;
	}

	private HorizontalLayout getDateLayout() {
		if (dateLayout == null) {
			dateLayout = new HorizontalLayout();
			dateLayout.addComponent(getDateLabel());
			dateLayout.addComponent(getDateCalendar());
			dateLayout.addComponent(getPickPatientButton());
			dateLayout.addComponent(getTrashPatientButton());
		}
		return dateLayout;
	}

	private Button getTrashPatientButton() {
		if (trashPatientButton == null) {
			trashPatientButton = new Button();
			trashPatientButton.setClickShortcut(KeyEvent.VK_R);
			// trashPatientButton.setPreferredSize(new Dimension(25,25));
			trashPatientButton.setIcon(new FileResource(new File("D:/nyobavaadin/vaadin-archetype-application/src/main/webapp" +"/WEB-INF/icons/remove_patient_button.png"))); //$NON-NLS-1$
			trashPatientButton.setDescription(MessageBundle.getMessage("angal.newbill.tooltip.removepatientassociationwiththisbill")); //$NON-NLS-1$
			trashPatientButton.addClickListener(e->{
				patientSelected = null;
				//BILL
				thisBill.setPatient(false);
				thisBill.setPatID(0);
				thisBill.setPatName(""); //$NON-NLS-1$
				//INTERFACE
				patientTextField.setValue(""); //$NON-NLS-1$
				patientTextField.setEnabled(false);
				pickPatientButton.setCaption(MessageBundle.getMessage("angal.newbill.pickpatient"));
				pickPatientButton.setDescription(MessageBundle.getMessage("angal.newbill.tooltip.associateapatientwiththisbill")); //$NON-NLS-1$
				trashPatientButton.setEnabled(false);
			});
			if (!thisBill.isPatient()) {
				trashPatientButton.setEnabled(false);
			}
		}
		return trashPatientButton;
	}

	private Button getPickPatientButton() {
		if (pickPatientButton == null) {
			pickPatientButton = new Button();
			pickPatientButton.setCaption(MessageBundle.getMessage("angal.newbill.pickpatient")); //$NON-NLS-1$
			pickPatientButton.setClickShortcut(KeyEvent.VK_P);
			pickPatientButton.setIcon(new FileResource(new File("D:/nyobavaadin/vaadin-archetype-application/src/main/webapp" +"/WEB-INF/icons/pick_patient_button.png"))); //$NON-NLS-1$
			pickPatientButton.setDescription(MessageBundle.getMessage("angal.newbill.tooltip.associateapatientwiththisbill")); //$NON-NLS-1$
			pickPatientButton.addClickListener(e->{
				SelectPatient sp = new SelectPatient(patientSelected);
				sp.addSelectionListener(this);
				sp.setVisible(true);
			});
			if (thisBill.isPatient()) {
				pickPatientButton.setCaption(MessageBundle.getMessage("angal.newbill.changepatient")); //$NON-NLS-1$
				pickPatientButton.setDescription(MessageBundle.getMessage("angal.newbill.tooltip.changethepatientassociatedwiththisbill")); //$NON-NLS-1$
			}
		}
		return pickPatientButton;
	}

	public void setPatientSelected(Patient patientSelected) {
		this.patientSelected = patientSelected;
	}

	private VerticalLayout getTopLayout() {
		if (topLayout == null) {
			topLayout = new VerticalLayout();
			topLayout.addComponent(getDateLayout());
			topLayout.addComponent(getPatientLayout());
		}
		return topLayout;
	}

	private void updateBill(){
			HashMap<String,Price> priceHashTable = new HashMap<String,Price>();
			prcListArray = new ArrayList<Price>();
			if (listSelected == null) listSelected = lstArray.get(0);
			for (Price price : prcArray) {
				if (price.getList() == listSelected.getId()) 
		    		prcListArray.add(price);
		    }
			for (Price price : prcListArray) {
				priceHashTable.put(price.getList()+
  					  price.getGroup()+
  					  price.getItem(), price);
		    }
		    for (BillItems item : billItems) {
				
				if (item.isPrice()) {
					Price p = priceHashTable.get(listSelected.getId()+item.getPriceID());
					item.setItemDescription(p.getDesc());
					item.setItemAmount(p.getPrice());
				}
			}
		    updateTotal();
		    updateBigTotal();
			updateBalance();
	}

	private Grid getBillGrid() {
		if (billGrid == null) {
			billGrid = new Grid();
			updateBill();
			billGrid.setItems(billItems);
			billGrid.addColumn(BillItems::getItemDescription).setCaption("Item");
			billGrid.addColumn(BillItems::getItemQuantity).setCaption("Qty");
			billGrid.addColumn(BillItems::getBillAmount).setCaption("Amount");
			// billGrid.setModel(new BillTableModel());
			// billGrid.getColumnModel().getColumn(1).setMinWidth(QuantityWidth);
			// billGrid.getColumnModel().getColumn(1).setMaxWidth(QuantityWidth);
			// billGrid.getColumnModel().getColumn(2).setMinWidth(PriceWidth);
			// billGrid.getColumnModel().getColumn(2).setMaxWidth(PriceWidth);
			// billGrid.setAutoCreateColumnsFromModel(false);
		}
		return billGrid;
	}
	
	TotalHeader bigTotalData;

	private Grid getBigTotalGrid() {
		if (bigTotalGrid == null) {
			bigTotalGrid = new Grid();
			bigTotalData = new TotalHeader("<html><b>"+"TO PAY"+"</b></html>", currencyCod, bigTotal);
			bigTotalGrid.setHeaderVisible(false);
			bigTotalGrid.setHeightByRows(1.0);
			bigTotalGrid.setItems(bigTotalData);
			bigTotalGrid.addColumn(TotalHeader::getTotalString,new HtmlRenderer());
			bigTotalGrid.addColumn(TotalHeader::getCurrencyCod);
			bigTotalGrid.addColumn(TotalHeader::getTotalValue);
			// bigTotalGrid.getColumnModel().getColumn(1).setMinWidth(CurrencyCodWidth);
			// bigTotalGrid.getColumnModel().getColumn(1).setMaxWidth(CurrencyCodWidth);
			// bigTotalGrid.getColumnModel().getColumn(2).setMinWidth(PriceWidth);
			// bigTotalGrid.getColumnModel().getColumn(2).setMaxWidth(PriceWidth);
			// bigTotalGrid.setMaximumSize(new Dimension(PanelWidth, BigTotalHeight));
			// bigTotalGrid.setMinimumSize(new Dimension(PanelWidth, BigTotalHeight));
			// bigTotalGrid.setPreferredSize(new Dimension(PanelWidth, BigTotalHeight));
		}
		return bigTotalGrid;
	}

	public class TotalHeader{
		private Label totalLabel;
		private String currencyCode;
		private BigDecimal totalValue;
		public TotalHeader(String d, String e, BigDecimal f){
			totalLabel = new Label();
			totalLabel.setContentMode(ContentMode.HTML);
			totalLabel.setValue(d);
			currencyCode=e;
			totalValue=f;
		}
		public String getTotalString(){
			return totalLabel.getValue();
		}
		public String getCurrencyCod(){
			return currencyCode;
		}
		public BigDecimal getTotalValue(){
			return totalValue;
		}
		public void setTotalValue(BigDecimal value){
			totalValue=value;
		}
	}

	TotalHeader totalGridData;

	private Grid getTotalGrid() {
		if (totalGrid == null) {
			totalGridData = new TotalHeader("<html><b>"+MessageBundle.getMessage("angal.newbill.totalm")+"</b></html>",
						currencyCod,
						total);
			totalGrid = new Grid();
			totalGrid.setItems(totalGridData);
			totalGrid.setHeaderVisible(false);
			totalGrid.setHeightByRows(1.0);
			totalGrid.addColumn(TotalHeader::getTotalString,new HtmlRenderer());
			totalGrid.addColumn(TotalHeader::getCurrencyCod);
			totalGrid.addColumn(TotalHeader::getTotalValue);
			// totalGrid.getColumnModel().getColumn(1).setMinWidth(CurrencyCodWidth);
			// totalGrid.getColumnModel().getColumn(1).setMaxWidth(CurrencyCodWidth);
			// totalGrid.getColumnModel().getColumn(2).setMinWidth(PriceWidth);
			// totalGrid.getColumnModel().getColumn(2).setMaxWidth(PriceWidth);
			// totalGrid.setMaximumSize(new Dimension(PanelWidth, TotalHeight));
			// totalGrid.setMinimumSize(new Dimension(PanelWidth, TotalHeight));
			// totalGrid.setPreferredSize(new Dimension(PanelWidth, TotalHeight));
		}
		return totalGrid;
	}

	private Grid getPaymentGrid() {
		if (paymentGrid == null) {
			paymentGrid = new Grid();
			updateBalance();
			paymentGrid.setItems(payItems);
			paymentGrid.setHeaderVisible(false);
			paymentGrid.addColumn(BillPayments::getDateString);
			paymentGrid.addColumn(BillPayments::getAmount);
			// paymentGrid.getColumnModel().getColumn(1).setMinWidth(PriceWidth);
			// paymentGrid.getColumnModel().getColumn(1).setMaxWidth(PriceWidth);
		}
		return paymentGrid;
	}

	private TotalHeader balanceData;

	private Grid getBalanceGrid() {
		if (balanceGrid == null) {
			balanceGrid = new Grid();
			balanceData = new TotalHeader("<html><b>"+MessageBundle.getMessage("angal.newbill.balancem")+"</b></html>", currencyCod, balance);
			balanceGrid.setItems(balanceData);
			balanceGrid.setHeaderVisible(false);
			balanceGrid.setHeightByRows(1.0);
			balanceGrid.addColumn(TotalHeader::getTotalString,new HtmlRenderer());
			balanceGrid.addColumn(TotalHeader::getCurrencyCod);
			balanceGrid.addColumn(TotalHeader::getTotalValue);
			// balanceGrid.getColumnModel().getColumn(1).setMinWidth(CurrencyCodWidth);
			// balanceGrid.getColumnModel().getColumn(1).setMaxWidth(CurrencyCodWidth);
			// balanceGrid.getColumnModel().getColumn(2).setMinWidth(PriceWidth);
			// balanceGrid.getColumnModel().getColumn(2).setMaxWidth(PriceWidth);
			// balanceGrid.setMaximumSize(new Dimension(PanelWidth, BalanceHeight));
			// balanceGrid.setMinimumSize(new Dimension(PanelWidth, BalanceHeight));
			// balanceGrid.setPreferredSize(new Dimension(PanelWidth, BalanceHeight));
		}
		return balanceGrid;
	}
	
	private VerticalLayout getPaymentAndActionButtonLayout() {
		if (paymentAndActionButtonLayout == null) {
			paymentAndActionButtonLayout = new VerticalLayout();
			paymentAndActionButtonLayout.addComponent(getPaymentButtonsLayout());
			paymentAndActionButtonLayout.addComponent(getActionButtonsLayout());
		}
		return paymentAndActionButtonLayout;
	}
	
	private VerticalLayout getBillButtons() {
		if (billButtons == null) {
			billButtons = new VerticalLayout();
			billButtons.addComponent(getAddMedicalButton());
			billButtons.addComponent(getAddOperationButton());
			billButtons.addComponent(getAddExamButton());
			billButtons.addComponent(getAddOtherButton());
			billButtons.addComponent(getAddCustomButton());
			billButtons.addComponent(getRemoveItemButton());
			// billButtons.setMinimumSize(new Dimension(ButtonWidth, BillHeight+TotalHeight));
			// billButtons.setMaximumSize(new Dimension(ButtonWidth, BillHeight+TotalHeight));
			// billButtons.setPreferredSize(new Dimension(ButtonWidth, BillHeight+TotalHeight));

		}
		return billButtons;
	}

	private VerticalLayout getPaymentButtonsLayout() {
		if (paymentButtonsLayout == null) {
			paymentButtonsLayout = new VerticalLayout();
			paymentButtonsLayout.addComponent(getAddPaymentButton());
			paymentButtonsLayout.addComponent(getAddRefundButton());
			if (GeneralData.RECEIPTPRINTER) paymentButtonsLayout.addComponent(getPrintPaymentButton());
			paymentButtonsLayout.addComponent(getRemovePaymentButton());
			// paymentButtonsLayout.setMinimumSize(new Dimension(ButtonWidth, PaymentHeight));
			// paymentButtonsLayout.setMaximumSize(new Dimension(ButtonWidth, PaymentHeight));
			//paymentButtonsLayout.setPreferredSize(new Dimension(ButtonWidth, PaymentHeight));

		}
		return paymentButtonsLayout;
	}
	
	private VerticalLayout getActionButtonsLayout() {
		if (actionButtonsLayout == null) {
			actionButtonsLayout = new VerticalLayout();
			actionButtonsLayout.addComponent(getBalanceButton());
			actionButtonsLayout.addComponent(getSaveButton());
			actionButtonsLayout.addComponent(getPaidButton());
			actionButtonsLayout.addComponent(getCloseButton());
			//actionButtonsLayout.setMinimumSize(new Dimension(ButtonWidth, ActionHeight));
			//actionButtonsLayout.setMaximumSize(new Dimension(ButtonWidth, ActionHeight));
	}
	return actionButtonsLayout;
	}

	private Button getBalanceButton() {
		if (balanceButton == null) {
			balanceButton = new Button();
			balanceButton.setCaption(MessageBundle.getMessage("angal.newbill.givechange") + "..."); //$NON-NLS-1$
			balanceButton.setClickShortcut(KeyEvent.VK_B);
			// balanceButton.setMaximumSize(new Dimension(ButtonWidth, ButtonHeight));
			balanceButton.setIcon(new FileResource(new File("D:/nyobavaadin/vaadin-archetype-application/src/main/webapp" +"/WEB-INF/icons/money_button.png"))); //$NON-NLS-1$
			if(insert) balanceButton.setEnabled(false);
			balanceButton.addClickListener(e->{
				Image icon = new Image(null,new FileResource(new File("D:/nyobavaadin/vaadin-archetype-application/src/main/webapp" +"/WEB-INF/icons/money_dialog.png")));
				TextField tempTF = new TextField();
				tempTF.setValue("0");
				tempTF.setCaption(MessageBundle.getMessage("angal.newbill.entercustomercash"));
				tempTF.selectAll();
				MessageBox.create().withCaption(MessageBundle.getMessage("angal.newbill.givechange")).withIcon(icon).withMessage(tempTF)
				.withOkButton(()->{
					BigDecimal amount = new BigDecimal(0);
					if (!tempTF.isEmpty()) {
						try {
							amount = new BigDecimal(tempTF.getValue());
							if (amount.equals(new BigDecimal(0)) || amount.compareTo(balance) < 0) return;
							StringBuffer balanceBfr = new StringBuffer(MessageBundle.getMessage("angal.newbill.givechange"));
							balanceBfr.append(": ").append(amount.subtract(balance));
							MessageBox.create().withIcon(icon).withCaption(MessageBundle.getMessage("angal.newbill.givechange"))
							.withMessage(balanceBfr.toString()).withOkButton().open();
						} catch (Exception eee) {
							MessageBox.createError().withCaption(MessageBundle.getMessage("angal.newbill.invalidquantity"))
							.withMessage(MessageBundle.getMessage("angal.newbill.invalidquantitypleasetryagain")).open();
							return;
						}
					} else return;
				})
				.withCancelButton()
				.open();
			});
		}
		return balanceButton;
	}

	private Button getSaveButton() {
		if (saveButton == null) {
			saveButton = new Button();
			saveButton.setCaption(MessageBundle.getMessage("angal.common.save")); //$NON-NLS-1$
			saveButton.setClickShortcut(KeyEvent.VK_S);
			// saveButton.setMaximumSize(new Dimension(ButtonWidth, ButtonHeight));
			saveButton.setIcon(new FileResource(new File("D:/nyobavaadin/vaadin-archetype-application/src/main/webapp" +"/WEB-INF/icons/save_button.png"))); //$NON-NLS-1$
			saveButton.addClickListener(e->{
				GregorianCalendar upDate = new GregorianCalendar();
				GregorianCalendar firstPay = new GregorianCalendar();
				
				if (payItems.size() > 0) {
					firstPay = payItems.get(0).getDate();
					upDate = payItems.get(payItems.size()-1).getDate();
				} else {
					upDate = billDate;
				}
				
				if (billDate.after(today)) {
					MessageBox.createError().withCaption(MessageBundle.getMessage("angal.newbill.title"))
					.withMessage(MessageBundle.getMessage("angal.newbill.billsinfuturenotallowed")).open();
					return;
				}

				if (billDate.after(firstPay)) {
					MessageBox.createError().withCaption(MessageBundle.getMessage("angal.newbill.title"))
					.withMessage(MessageBundle.getMessage("angal.newbill.billdateafterfirstpayment")).open();
					return;
				}
				
				if (patientTextField.getValue().equals("")) { //$NON-NLS-1$
					MessageBox.createError().withCaption(MessageBundle.getMessage("angal.newbill.title"))
					.withMessage(MessageBundle.getMessage("angal.newbill.pleaseinsertanameforthepatient"))
					.open();
					return;
				}
				
				if (listSelected == null) {
					listSelected = lstArray.get(0); 
				}
				
				if (insert) {
					RememberDates.setLastBillDate(billDate);			//to remember for next INSERT
					Bill newBill = new Bill(0,							//Bill ID
							billDate,			 						//from calendar
							upDate,										//most recent payment 
							true,										//is a List?
							listSelected.getId(),						//List
							listSelected.getName(),						//List name
							thisBill.isPatient(),						//is a Patient?
							thisBill.isPatient() ? 
									thisBill.getPatID() : 0,			//Patient ID
							thisBill.isPatient() ? 
									patientSelected.getName() : 
									patientTextField.getValue(),		//Patient Name
							paid ? "C" : "O",							//CLOSED or OPEN
							total.doubleValue(),						//Total
							balance.doubleValue(),						//Balance
							user);										//User
					
					billID = billManager.newBill(newBill);
					if (billID == 0) {
						MessageBox.createError().withCaption(MessageBundle.getMessage("angal.newbill.title"))
						.withMessage(MessageBundle.getMessage("angal.newbill.failedtosavebill")).open();
						return;
					} else {
						newBill.setId(billID);
						billManager.newBillItems(billID, billItems);
						billManager.newBillPayments(billID, payItems);
						fireBillInserted(newBill);
					}
				} else {
					billID = thisBill.getId();
					Bill updateBill = new Bill(billID,					//Bill ID
							billDate,									//from calendar
							upDate,										//most recent payment
							true,										//is a List?
							listSelected.getId(),						//List
							listSelected.getName(),						//List name
							thisBill.isPatient(),						//is a Patient?
							thisBill.isPatient() ?
									thisBill.getPatID() : 0,			//Patient ID
							thisBill.isPatient() ?
									thisBill.getPatName() :
									patientTextField.getValue(),		//Patient Name
							paid ? "C" : "O",							//CLOSED or OPEN
							total.doubleValue(),						//Total
							balance.doubleValue(),						//Balance
							user);										//User
					
					billManager.updateBill(updateBill);
					billManager.newBillItems(billID, billItems);
					billManager.newBillPayments(billID, payItems);
					fireBillInserted(updateBill);
				}
				// if (paid && GeneralData.RECEIPTPRINTER) {
					
				// 	TxtPrinter.getTxtPrinter();
				// 	// if (TxtPrinter.PRINT_AS_PAID)
				// 		// new GenericReportBill(billID, GeneralData.PATIENTBILL, false, !TxtPrinter.PRINT_WITHOUT_ASK);
				// }
				close();
			});
		}
		return saveButton;
	}
	
	private Button getPrintPaymentButton() {
		if (printPaymentButton == null) {
			printPaymentButton = new Button();
			printPaymentButton.setCaption(MessageBundle.getMessage("angal.newbill.paymentreceipt")); //$NON-NLS-1$
			// printPaymentButton.setMaximumSize(new Dimension(ButtonWidthPayment, ButtonHeight));
			// printPaymentButton.setHorizontalAlignment(SwingConstants.LEFT);
			printPaymentButton.setIcon(new FileResource(new File("D:/nyobavaadin/vaadin-archetype-application/src/main/webapp" +"/WEB-INF/icons/receipt_button.png"))); //$NON-NLS-1$
			printPaymentButton.addClickListener(e->{
				// public void actionPerformed(ActionEvent e) {
				// 	TxtPrinter.getTxtPrinter();
				// 	// new GenericReportBill(thisBill.getId(), "PatientBillPayments", false, !TxtPrinter.PRINT_WITHOUT_ASK);
				// }
			});
		}
		if (insert) printPaymentButton.setEnabled(false);
		return printPaymentButton;
	}
	
	GregorianCalendar datePay = new GregorianCalendar();
	GregorianCalendar lastPay = new GregorianCalendar();
	private Button getPaidButton() {
		if (paidButton == null) {
			paidButton = new Button();
			paidButton.setCaption(MessageBundle.getMessage("angal.newbill.paid")); //$NON-NLS-1$
			paidButton.setClickShortcut(KeyEvent.VK_A);
			// paidButton.setMaximumSize(new Dimension(ButtonWidth, ButtonHeight));
			paidButton.setIcon(new FileResource(new File("D:/nyobavaadin/vaadin-archetype-application/src/main/webapp" +"/WEB-INF/icons/ok_button.png"))); //$NON-NLS-1$
			if(insert) paidButton.setEnabled(false);
			paidButton.addClickListener(e->{
				
				if (patientTextField.getValue().equals("")) { //$NON-NLS-1$
					MessageBox.createError().withCaption(MessageBundle.getMessage("angal.newbill.title"))
					.withMessage(MessageBundle.getMessage("angal.newbill.pleaseinsertanameforthepatient"))
					.open();
				}
				Image icon = new Image(null,new FileResource(new File("D:/nyobavaadin/vaadin-archetype-application/src/main/webapp" +"/WEB-INF/icons/money_dialog.png")));
				MessageBox.create().withIcon(icon).withCaption(MessageBundle.getMessage("angal.newbill.paid"))
				.withMessage(MessageBundle.getMessage("angal.newbill.doyouwanttosetaspaidcurrentbill"))
				.withYesButton(()->{
				})
				.withNoButton(()->{
					return;
				}).open();
				if (balance.compareTo(new BigDecimal(0)) > 0) {
					if (billDate.before(today)) { //if Bill is in the past the user will be asked for PAID date
						icon = new Image(null,new FileResource(new File("D:/nyobavaadin/vaadin-archetype-application/src/main/webapp" +"/WEB-INF/icons/calendar_dialog.png")));
						DateTimeField datePayChooser = new DateTimeField("",LocalDateTime.now());
						datePayChooser.setLocale(new Locale(GeneralData.LANGUAGE));
						datePayChooser.setDateFormat("dd/MM/yy - HH:mm:ss");
						MessageBox.create().withIcon(icon).withCaption(MessageBundle.getMessage("angal.newbill.dateofpayment"))
						.withMessage(datePayChooser).withOkButton(()->{
								datePay.setTime(Date.from(datePayChooser.getValue().atZone(ZoneId.systemDefault()).toInstant()));
						}).withCancelButton(()->{return;}).open();
			        
					    GregorianCalendar now = new GregorianCalendar();
					    
					    if (payItems.size() > 0) { 
					    	lastPay = payItems.get(payItems.size()-1).getDate();
						} else {
							lastPay = billDate;
						}
					    
					    if (datePay.before(lastPay)) {
					    	MessageBox.createError().withCaption(MessageBundle.getMessage("angal.newbill.invaliddate"))
					    	.withMessage(MessageBundle.getMessage("angal.newbill.datebeforelastpayment")).open();
					    	return;
					    } else if (datePay.after(now)) {
					    	MessageBox.createError().withCaption(MessageBundle.getMessage("angal.newbill.invaliddate"))
					    	.withMessage(MessageBundle.getMessage("angal.newbill.payementinthefuturenotallowed"))
					    	.open();
					    	return;
					    } else {
					    	addPayment(datePay, balance.doubleValue());
					    }
						
					} else {
						datePay = new GregorianCalendar();
						addPayment(datePay, balance.doubleValue());
					}
				}
				paid = true;
				updateBalance();
				saveButton.click();
			});
		}
		return paidButton;
	}
	
	private Button getCloseButton() {
		if (closeButton == null) {
			closeButton = new Button();
			closeButton.setCaption(MessageBundle.getMessage("angal.common.close")); //$NON-NLS-1$
			closeButton.setClickShortcut(KeyEvent.VK_C);
			// closeButton.setMaximumSize(new Dimension(ButtonWidth, ButtonHeight));
			closeButton.setIcon(new FileResource(new File("D:/nyobavaadin/vaadin-archetype-application/src/main/webapp" +"/WEB-INF/icons/close_button.png"))); //$NON-NLS-1$
			closeButton.addClickListener(e->{
				if (modified) {
					Image icon = new Image(null,new FileResource(new File("D:/nyobavaadin/vaadin-archetype-application/src/main/webapp" +"/WEB-INF/icons/save_dialog.png")));
					MessageBox.create().withIcon(icon).withCaption(MessageBundle.getMessage("angal.common.save"))
					.withMessage(MessageBundle.getMessage("angal.newbill.billhasbeenchangedwouldyouliketosavechanges"))
					.withYesButton(()->{
						saveButton.click();
					}).withNoButton(()->{close();}).open();
				} else {
					close();
				}
			});
		}
		return closeButton;
	}

	BigDecimal amount = new BigDecimal(0);
	private Button getAddRefundButton() {
		if (addRefundButton == null) {
			addRefundButton = new Button();
			addRefundButton.setCaption(MessageBundle.getMessage("angal.newbill.refund")); //$NON-NLS-1$
			// addRefundButton.setMaximumSize(new Dimension(ButtonWidthPayment, ButtonHeight));
			// addRefundButton.setHorizontalAlignment(SwingConstants.LEFT);
			addRefundButton.setIcon(new FileResource(new File("D:/nyobavaadin/vaadin-archetype-application/src/main/webapp" +"/WEB-INF/icons/plus_button.png"))); //$NON-NLS-1$
			addRefundButton.addClickListener(e->{
				Image icon = new Image(null,new FileResource(new File("D:/nyobavaadin/vaadin-archetype-application/src/main/webapp" +"/WEB-INF/icons/money_dialog.png")));
				
				datePay = new GregorianCalendar();
				TextField quantity = new TextField(MessageBundle.getMessage("angal.newbill.insertquantity"));
				quantity.setValue("0");
				quantity.selectAll();
				MessageBox.create().withIcon(icon).withCaption(MessageBundle.getMessage("angal.newbill.amount"))
				.withMessage(quantity).withOkButton(()->{
					if (!quantity.isEmpty()) {
						try {
							amount = new BigDecimal(quantity.getValue()).negate();
							if (amount.equals(new BigDecimal(0))) return;
						} catch (Exception eee) {
							MessageBox.createError().withCaption(MessageBundle.getMessage("angal.newbill.invalidquantity"))
							.withMessage(MessageBundle.getMessage("angal.newbill.invalidquantitypleasetryagain")).open();
							return;
						}
						if (billDate.before(today)) { //if is a bill in the past the user will be asked for date of payment
							// logger.info("masuk ka before today"+billDate.getTime());
							// logger.info(""+today.getTime());
							DateTimeField datePayChooser = new DateTimeField("",LocalDateTime.now());
							datePayChooser.setLocale(new Locale(GeneralData.LANGUAGE));
							datePayChooser.setDateFormat("dd/MM/yy - HH:mm:ss"); //$NON-NLS-1$
							
							MessageBox.createQuestion().withCaption(MessageBundle.getMessage("angal.newbill.dateofpayment"))
							.withMessage(datePayChooser).withOkButton(()->datePay.setTime(Date.from(datePayChooser.getValue().atZone(ZoneId.systemDefault()).toInstant())))
							.withCancelButton(()->{return;}).open();

						    GregorianCalendar now = new GregorianCalendar();
						    
						    if (datePay.before(billDate)) {
						    	MessageBox.createError().withCaption(MessageBundle.getMessage("angal.newbill.invaliddate"))
						    	.withMessage(MessageBundle.getMessage("angal.newbill.paymentbeforebilldate")).open();
						    } else if (datePay.after(now)) {
						    	MessageBox.createError().withCaption(MessageBundle.getMessage("angal.newbill.invaliddate"))
						    	.withMessage(MessageBundle.getMessage("angal.newbill.payementinthefuturenotallowed")).open();
						    } else {
						    	addPayment(datePay, amount.doubleValue());
						    }
						} else {
							datePay = new GregorianCalendar();
							addPayment(datePay, amount.doubleValue());
						}
					} else return;
				}).withCancelButton(()->{return;}).open();
			});
		}
		return addRefundButton;
	}
	
	private Button getAddPaymentButton() {
		if (addPaymentButton == null) {
			addPaymentButton = new Button();
			addPaymentButton.setCaption(MessageBundle.getMessage("angal.newbill.payment")); //$NON-NLS-1$
			addPaymentButton.setClickShortcut(KeyEvent.VK_Y);
			// addPaymentButton.setMaximumSize(new Dimension(ButtonWidthPayment, ButtonHeight));
			// addPaymentButton.setHorizontalAlignment(SwingConstants.LEFT);
			addPaymentButton.setIcon(new FileResource(new File("D:/nyobavaadin/vaadin-archetype-application/src/main/webapp" +"/WEB-INF/icons/plus_button.png"))); //$NON-NLS-1$
			addPaymentButton.addClickListener(e->{
				Image icon = new Image(null,new FileResource(new File("D:/nyobavaadin/vaadin-archetype-application/src/main/webapp" +"/WEB-INF/icons/money_dialog.png")));
				TextField tempTF = new TextField(MessageBundle.getMessage("angal.newbill.insertquantity"),"0");
				tempTF.selectAll();
				datePay = new GregorianCalendar();
				MessageBox.create().withIcon(icon).withCaption(MessageBundle.getMessage("angal.newbill.amount"))
				.withMessage(tempTF).withOkButton(()->{
					BigDecimal amount = new BigDecimal(0);
					if (!tempTF.isEmpty()){
						try {
							amount = new BigDecimal(tempTF.getValue());
							if (amount.equals(new BigDecimal(0))) return;
						} catch (Exception eee) {
							MessageBox.createError().withCaption(MessageBundle.getMessage("angal.newbill.invalidquantity"))
							.withMessage(MessageBundle.getMessage("angal.newbill.invalidquantitypleasetryagain")).open();
							return;
						}
						if (billDate.before(today)) { //if is a bill in the past the user will be asked for date of payment
							DateTimeField datePayChooser = new DateTimeField("",LocalDateTime.now());
							datePayChooser.setLocale(new Locale(GeneralData.LANGUAGE));
							datePayChooser.setDateFormat("dd/MM/yy - HH:mm:ss"); //$NON-NLS-1$
							
							MessageBox.createQuestion().withCaption(MessageBundle.getMessage("angal.newbill.dateofpayment"))
							.withMessage(datePayChooser).withOkButton(()->datePay.setTime(Date.from(datePayChooser.getValue().atZone(ZoneId.systemDefault()).toInstant())))
							.withCancelButton(()->{return;}).open();

						    GregorianCalendar now = new GregorianCalendar();
						    
						    if (datePay.before(billDate)) {
						    	MessageBox.createError().withCaption(MessageBundle.getMessage("angal.newbill.invaliddate"))
						    	.withMessage(MessageBundle.getMessage("angal.newbill.paymentbeforebilldate")).open();
						    } else if (datePay.after(now)) {
						    	MessageBox.createError().withCaption(MessageBundle.getMessage("angal.newbill.invaliddate"))
						    	.withMessage(MessageBundle.getMessage("angal.newbill.payementinthefuturenotallowed")).open();
						    } else {
						    	addPayment(datePay, amount.doubleValue());
						    }
						} else {
							datePay = new GregorianCalendar();
							addPayment(datePay, amount.doubleValue());
						}
					} else return;
				}).withCancelButton().open();
			});
		}
		return addPaymentButton;
	}

	private Button getRemovePaymentButton() {
		if (removePaymentButton == null) {
			removePaymentButton = new Button();
			removePaymentButton.setCaption(MessageBundle.getMessage("angal.newbill.removepayment")); //$NON-NLS-1$
			removePaymentButton.setClickShortcut(KeyEvent.VK_Y);
			// removePaymentButton.setMaximumSize(new Dimension(ButtonWidthPayment, ButtonHeight));
			// removePaymentButton.setHorizontalAlignment(SwingConstants.LEFT);
			removePaymentButton.setIcon(new FileResource(new File("D:/nyobavaadin/vaadin-archetype-application/src/main/webapp" +"/WEB-INF/icons/delete_button.png"))); //$NON-NLS-1$
			removePaymentButton.addClickListener(e->{
				try{
					removePayment((BillPayments) paymentGrid.getSelectedItems().toArray()[0]);
				}
				catch(Exception sd){	
				}
			});
		}
		return removePaymentButton;
	}
	
	boolean isPrice = true;
	int qty = 1;
	Image icon;
	private Button getAddOtherButton() {
		if (addOtherButton == null) {
			addOtherButton = new Button();
			addOtherButton.setCaption(MessageBundle.getMessage("angal.newbill.other")); //$NON-NLS-1$
			addOtherButton.setClickShortcut(KeyEvent.VK_T);
			// addOtherButton.setMaximumSize(new Dimension(ButtonWidthBill, ButtonHeight));
			// addOtherButton.setHorizontalAlignment(SwingConstants.LEFT);
			addOtherButton.setIcon(new FileResource(new File("D:/nyobavaadin/vaadin-archetype-application/src/main/webapp" +"/WEB-INF/icons/plus_button.png"))); //$NON-NLS-1$
			addOtherButton.addClickListener(e->{
				isPrice = true;
				HashMap<Integer,PricesOthers> othersHashMap = new HashMap<Integer,PricesOthers>();
				for (PricesOthers other : othPrices) {
			    	othersHashMap.put(other.getId(), other);
			    }
				
				ArrayList<Price> othArray = new ArrayList<Price>();
				for (Price price : prcListArray) {
					
					if (price.getGroup().equals("OTH")) //$NON-NLS-1$
						othArray.add(price);
				}
				
				icon = new Image(null,new FileResource(new File("D:/nyobavaadin/vaadin-archetype-application/src/main/webapp" +"/WEB-INF/icons/plus_dialog.png")));
				ComboBox oth = new ComboBox(MessageBundle.getMessage("angal.newbill.pleaseselectanitem"));
				oth.setEmptySelectionAllowed(false);
				oth.setItems(othArray.toArray());
				oth.setValue(othArray.toArray()[0]);
				MessageBox.create().withIcon(icon).withCaption(MessageBundle.getMessage("angal.newbill.item")).withMessage(oth)
				.withOkButton(()->{
					if (!oth.isEmpty()) {
						if (othersHashMap.get(Integer.valueOf(((Price)oth.getValue()).getItem())).isUndefined()) {
							icon = new Image(null,new FileResource(new File("D:/nyobavaadin/vaadin-archetype-application/src/main/webapp" +"/WEB-INF/icons/money_dialog.png")));
							TextField tempTF = new TextField(MessageBundle.getMessage("angal.newbill.howmuchisit"),"0");
							tempTF.selectAll();
							MessageBox.create().withIcon(icon).withCaption(MessageBundle.getMessage("angal.newbill.undefined"))
							.withMessage(tempTF).withOkButton(()->{
								try {
									if (tempTF.isEmpty()) return;
									double amount = Double.valueOf(tempTF.getValue());
									((Price)oth.getValue()).setPrice(amount);
									isPrice = false;
								} catch (Exception eee) {
									MessageBox.createError().withCaption(MessageBundle.getMessage("angal.newbill.invalidprice"))
									.withMessage(MessageBundle.getMessage("angal.newbill.invalidpricepleasetryagain")).open();
									return;
								}
							}).withCancelButton().open();
						}
						if (othersHashMap.get(Integer.valueOf(((Price)oth.getValue()).getItem())).isDischarge()) {
							double amount = ((Price)oth.getValue()).getPrice();
							((Price)oth.getValue()).setPrice(-amount);
						}
						if (othersHashMap.get(Integer.valueOf(((Price)oth.getValue()).getItem())).isDaily()) {
							qty=1;
							icon = new Image(null,new FileResource(new File("D:/nyobavaadin/vaadin-archetype-application/src/main/webapp" +"/WEB-INF/icons/calendar_dialog.png")));
							TextField tempTF = new TextField(MessageBundle.getMessage("angal.newbill.howmanydays"),""+qty);
							MessageBox.create().withIcon(icon).withCaption(MessageBundle.getMessage("angal.newbill.days"))
							.withMessage(tempTF).withOkButton(()->{
								try {
									if (tempTF.isEmpty()) return;
									qty = Integer.valueOf(tempTF.getValue());
									addItem(((Price)oth.getValue()), qty, isPrice);
								} catch (Exception eee) {
									MessageBox.createError().withCaption(MessageBundle.getMessage("angal.newbill.invalidquantity"))
									.withMessage(MessageBundle.getMessage("angal.newbill.invalidquantitypleasetryagain")).open();
								}
							}).withCancelButton().open();
						} else {
							addItem(((Price)oth.getValue()), 1, isPrice);
						}
					}
				}).withCancelButton().open();
			});
		}
		return addOtherButton;
	}

	private Button getAddExamButton() {
		if (addExamButton == null) {
			addExamButton = new Button();
			addExamButton.setCaption(MessageBundle.getMessage("angal.newbill.exam")); //$NON-NLS-1$
			addExamButton.setClickShortcut(KeyEvent.VK_E);
			// addExamButton.setMaximumSize(new Dimension(ButtonWidthBill, ButtonHeight));
			// addExamButton.setHorizontalAlignment(SwingConstants.LEFT);
			addExamButton.setIcon(new FileResource(new File("D:/nyobavaadin/vaadin-archetype-application/src/main/webapp" +"/WEB-INF/icons/plus_button.png"))); //$NON-NLS-1$
			addExamButton.addClickListener(e->{
				ArrayList<Price> exaArray = new ArrayList<Price>();
				for (Price price : prcListArray) {
					
					if (price.getGroup().equals("EXA")) //$NON-NLS-1$
						exaArray.add(price);
				}
				icon = new Image(null,new FileResource(new File("D:/nyobavaadin/vaadin-archetype-application/src/main/webapp" +"/WEB-INF/icons/exam_dialog.png")));
				ListSelect tempList = new ListSelect(MessageBundle.getMessage("angal.newbill.selectanexam"));
				tempList.setItems(exaArray.toArray());
				MessageBox.create().withIcon(icon).withCaption(MessageBundle.getMessage("angal.newbill.exam")).withMessage(tempList)
				.withOkButton(()->{
					if(!tempList.getValue().isEmpty())
						addItem((Price)tempList.getValue().toArray()[0], 1, true);	
				}).withCancelButton().open();
			});
		}
		return addExamButton;
	}

	private Button getAddOperationButton() {
		if (addOperationButton == null) {
			addOperationButton = new Button();
			addOperationButton.setCaption(MessageBundle.getMessage("angal.newbill.operation")); //$NON-NLS-1$
			addOperationButton.setClickShortcut(KeyEvent.VK_O);
			// addOperationButton.setMaximumSize(new Dimension(ButtonWidthBill, ButtonHeight));
			// addOperationButton.setHorizontalAlignment(SwingConstants.LEFT);
			addOperationButton.setIcon(new FileResource(new File("D:/nyobavaadin/vaadin-archetype-application/src/main/webapp" +"/WEB-INF/icons/plus_button.png"))); //$NON-NLS-1$
			addOperationButton.addClickListener(e->{
				ArrayList<Price> opeArray = new ArrayList<Price>();
				for (Price price : prcListArray) {	
					if (price.getGroup().equals("OPE")) //$NON-NLS-1$
						opeArray.add(price);
				}
				icon = new Image(null,new FileResource(new File("D:/nyobavaadin/vaadin-archetype-application/src/main/webapp" +"/WEB-INF/icons/operation_dialog.png")));
				ListSelect tempList = new ListSelect(MessageBundle.getMessage("angal.newbill.selectanoperation"));
				tempList.setItems(opeArray.toArray());
				MessageBox.create().withIcon(icon).withCaption(MessageBundle.getMessage("angal.newbill.operation")).withMessage(tempList)
				.withOkButton(()->addItem((Price)tempList.getValue().toArray()[0], 1, true)).withCancelButton().open();
			});
		}
		return addOperationButton;
	}

	private Button getAddMedicalButton() {
		if (addMedicalButton == null) {
			addMedicalButton = new Button();
			addMedicalButton.setCaption(MessageBundle.getMessage("angal.newbill.medical")); //$NON-NLS-1$
			addMedicalButton.setClickShortcut(KeyEvent.VK_M);
			// addMedicalButton.setMaximumSize(new Dimension(ButtonWidthBill, ButtonHeight));
			// addMedicalButton.setHorizontalAlignment(SwingConstants.LEFT);
			addMedicalButton.setIcon(new FileResource(new File("D:/nyobavaadin/vaadin-archetype-application/src/main/webapp" +"/WEB-INF/icons/plus_button.png"))); //$NON-NLS-1$
			addMedicalButton.addClickListener(e->{
				ArrayList<Price> medArray = new ArrayList<Price>();
				for (Price price : prcListArray) {
					if (price.getGroup().equals("MED")) //$NON-NLS-1$
						medArray.add(price);
				}
				ListSelect medicalList = new ListSelect();
				medicalList.setItems(medArray.toArray());
				medicalList.setCaption(MessageBundle.getMessage("angal.newbill.selectamedical"));
				Image icon = new Image(null,new FileResource(new File("D:/nyobavaadin/vaadin-archetype-application/src/main/webapp" +"/WEB-INF/icons/medical_dialog.png")));
				MessageBox.create().withCaption(MessageBundle.getMessage("angal.newbill.medical")).withIcon(icon).withMessage(medicalList)
				.withOkButton(()->{
					if (!medicalList.isEmpty()) {
						TextField quantity = new TextField(MessageBundle.getMessage("angal.newbill.insertquantity"),"1");
						quantity.selectAll();
						MessageBox.create().withCaption(MessageBundle.getMessage("angal.common.quantity")).withIcon(icon)
						.withMessage(quantity)
						.withOkButton(()->{
							int qty = 1;
							try {
								if (quantity == null || quantity.getValue().equals("")) return;
								qty = Integer.valueOf(quantity.getValue());
								addItem((Price) medicalList.getValue().toArray()[0], qty, true);
							} catch (Exception eee) {
								MessageBox.createError().withCaption(MessageBundle.getMessage("angal.newbill.invalidquantity"))
								.withMessage(MessageBundle.getMessage("angal.newbill.invalidquantitypleasetryagain")).withOkButton().open();
							}
						})
						.withCancelButton()
						.open();
					}
				})
				.withCancelButton().open();
			});
		}
		return addMedicalButton;
	}
	
	private Button getAddCustomButton() {
		if (customButton == null) {
			customButton = new Button();
			customButton.setCaption(MessageBundle.getMessage("angal.newbill.custom")); //$NON-NLS-1$
			customButton.setClickShortcut(KeyEvent.VK_U);
			// customButton.setMaximumSize(new Dimension(ButtonWidthBill, ButtonHeight));
			// customButton.setHorizontalAlignment(SwingConstants.LEFT);
			customButton.setIcon(new FileResource(new File("D:/nyobavaadin/vaadin-archetype-application/src/main/webapp" +"/WEB-INF/icons/plus_button.png"))); //$NON-NLS-1$
			customButton.addClickListener(e->{
				icon = new Image(null,new FileResource(new File("D:/nyobavaadin/vaadin-archetype-application/src/main/webapp" +"/WEB-INF/icons/custom_dialog.png")));
				TextField tempTF = new TextField(MessageBundle.getMessage("angal.newbill.chooseadescription"),MessageBundle.getMessage("angal.newbill.newdescription"));
				tempTF.selectAll();
				MessageBox.create().withIcon(icon).withCaption(MessageBundle.getMessage("angal.newbill.customitem"))
				.withMessage(tempTF).withOkButton(()->{
					if (tempTF.isEmpty() || tempTF.getValue().equals("")) { //$NON-NLS-1$
						return;
					} else {
						icon = new Image(null,new FileResource(new File("D:/nyobavaadin/vaadin-archetype-application/src/main/webapp" +"/WEB-INF/icons/money_dialog.png")));
						TextField tempTF1 = new TextField(MessageBundle.getMessage("angal.newbill.howmuchisit"),"0");
						tempTF1.selectAll();
						MessageBox.create().withIcon(icon).withCaption(MessageBundle.getMessage("angal.newbill.customitem"))
						.withMessage(tempTF1).withOkButton(()->{
							double amountVar;
							try {
								amountVar = Double.valueOf(tempTF1.getValue());
							} catch (Exception eee) {
								MessageBox.createError().withCaption(MessageBundle.getMessage("angal.newbill.invalidprice"))
								.withMessage(MessageBundle.getMessage("angal.newbill.invalidpricepleasetryagain")).open();
								return;
							}
							BillItems newItem = new BillItems(0,
									billID,
									false,
									"", //$NON-NLS-1$
									tempTF.getValue(),
									amountVar,
									1);
							addItem(newItem);
						}).withCancelButton().open();
					}
				}).withCancelButton().open();
			});
		}
		return customButton;
	}
	
	private Button getRemoveItemButton() {
		if (removeItemButton == null) {
			removeItemButton = new Button();
			removeItemButton.setCaption(MessageBundle.getMessage("angal.newbill.removeitem")); //$NON-NLS-1$
			removeItemButton.setClickShortcut(KeyEvent.VK_R);
			// removeItemButton.setMaximumSize(new Dimension(ButtonWidthBill, ButtonHeight));
			// removeItemButton.setHorizontalAlignment(SwingConstants.LEFT);
			removeItemButton.setIcon(new FileResource(new File("D:/nyobavaadin/vaadin-archetype-application/src/main/webapp" +"/WEB-INF/icons/delete_button.png"))); //$NON-NLS-1$
			removeItemButton.addClickListener(e->{
				try{
					removeItem((BillItems) billGrid.getSelectedItems().toArray()[0]);
				}
				catch(Exception sd){
				}
			});
		}
		return removeItemButton;
	}
	
	private void updateTotal() { //only positive items make the bill's total
		total = new BigDecimal(0);
		for (BillItems item : billItems) {
			double amount = item.getItemAmount();
			if (amount > 0) {
				BigDecimal itemAmount = new BigDecimal(Double.toString(amount));
				total = total.add(itemAmount.multiply(new BigDecimal(item.getItemQuantity())));
			}
		}
	}
	
	private void updateBigTotal() { //the big total (to pay) is made by all items
		bigTotal = new BigDecimal(0);
		for (BillItems item : billItems) {
			BigDecimal itemAmount = new BigDecimal(Double.toString(item.getItemAmount()));
			bigTotal = bigTotal.add(itemAmount.multiply(new BigDecimal(item.getItemQuantity())));			
		}
	}
	
	private void updateBalance() { //the balance is what remaining after payments
		balance = new BigDecimal(0);
		BigDecimal payments = new BigDecimal(0);
		for (BillPayments pay : payItems) {
			BigDecimal payAmount = new BigDecimal(Double.toString(pay.getAmount()));
			payments = payments.add(payAmount); 
		}
		balance = bigTotal.subtract(payments);
		if (paidButton != null) paidButton.setEnabled(balance.compareTo(new BigDecimal(0)) >= 0);
		if (balanceButton != null) balanceButton.setEnabled(balance.compareTo(new BigDecimal(0)) >= 0);
	}

	private void addItem(Price prc, int qty, boolean isPrice) {
		if (prc != null) {
			double amount = prc.getPrice();
			BillItems item = new BillItems(0, 
					billID, 
					isPrice, 
					prc.getGroup()+prc.getItem(),
					prc.getDesc(),
					amount,
					qty);
			billItems.add(item);
			modified = true;
			billGrid.setItems(billItems);
			updateTotals();
		}
	}
	
	private void updateUI() {
		// LocalDateTime.ofInstant(in.toInstant(), ZoneId.systemDefault());
		dateCalendar.setValue(thisBill.getDate().getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
		patientTextField.setValue(patientSelected.getName());
		patientTextField.setEnabled(false);
		pickPatientButton.setCaption(MessageBundle.getMessage("angal.newbill.changepatient")); //$NON-NLS-1$
		pickPatientButton.setDescription(MessageBundle.getMessage("angal.newbill.changethepatientassociatedwiththisbill")); //$NON-NLS-1$
		trashPatientButton.setEnabled(true);
		billGrid.setItems(billItems);
		paymentGrid.setItems(payItems);
		updateTotals();
	}

	/**
	 * 
	 */
	private void updateTotals() {
		updateTotal();
		updateBigTotal();
		updateBalance();
		totalGridData.setTotalValue(total);
		totalGrid.setItems(totalGridData);
		bigTotalData.setTotalValue(bigTotal);
		bigTotalGrid.setItems(bigTotalData);
		balanceData.setTotalValue(balance);
		balanceGrid.setItems(balanceData);
	}
	
	private void addItem(BillItems item) {
		if (item != null) {
			billItems.add(item);
			modified = true;
			billGrid.setItems(billItems);
			updateTotals();
		}
	}
	
	private void addPayment(GregorianCalendar datePay, double qty) {
		if (qty != 0) {
			BillPayments pay = new BillPayments(0,
					billID,
					datePay,
					qty,
					user);
			payItems.add(pay);
			modified = true;
			Collections.sort(payItems);
			paymentGrid.setItems(payItems);
			updateBalance();
			balanceData = new TotalHeader("<html><b>"+MessageBundle.getMessage("angal.newbill.balancem")+"</b></html>", currencyCod, balance);
			balanceGrid.setItems(balanceData);
		}
	}
	
	private void removeItem(BillItems item) {
		for(int i = 0; i < billItems.size(); i++){
			if (billItems.get(i).getId()==item.getId()){
				billItems.remove(i);
				break;
			}
		}
		billGrid.setItems(billItems);
		updateTotals();
	}
	
	private void removePayment(BillPayments item) {
		for(int i = 0; i < payItems.size(); i++){
			if (payItems.get(i).getId()==item.getId()){
				payItems.remove(i);
				break;
			}
		}
		paymentGrid.setItems(payItems);
		updateTotals();
	}

	public String formatDate(GregorianCalendar time) {
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");  //$NON-NLS-1$
		return format.format(time.getTime());
	}
	
	public String formatDateTime(GregorianCalendar time) {
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");  //$NON-NLS-1$
		return format.format(time.getTime());
	}
	
	public boolean isSameDay(GregorianCalendar billDate, GregorianCalendar today) {
		return (billDate.get(Calendar.YEAR) == today.get(Calendar.YEAR)) &&
			   (billDate.get(Calendar.MONTH) == today.get(Calendar.MONTH)) &&
			   (billDate.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH));
	}
}
