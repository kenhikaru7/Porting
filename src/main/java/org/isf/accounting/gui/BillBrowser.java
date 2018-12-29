package org.isf.accounting.gui;

/**
 * Browsing of table BILLS
 * 
 * @author Mwithi
 * 
 */
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.Month;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import com.vaadin.ui.renderers.DateRenderer;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.shared.ui.datefield.DateResolution;

import de.steinwedel.messagebox.MessageBox;

import org.isf.accounting.gui.PatientBillEdit.PatientBillListener;
import org.isf.accounting.manager.BillBrowserManager;
import org.isf.accounting.model.Bill;
import org.isf.accounting.model.BillPayments;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.hospital.manager.HospitalBrowsingManager;
import org.isf.menu.gui.MainMenu;
// import org.isf.stat.manager.GenericReportBill;
// import org.isf.stat.manager.GenericReportFromDateToDate;
// import org.isf.stat.manager.GenericReportUserInDate;
import org.isf.utils.Logging;
import org.isf.utils.jobjects.BusyState;
import org.isf.utils.jobjects.ModalWindow;
import org.joda.time.DateTime;

import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JMonthChooser;
import com.toedter.calendar.JYearChooser;

public class BillBrowser extends ModalWindow implements PatientBillEdit.PatientBillListener {

	public void billInserted(Bill tempBill) {
		updateDataSet(dateFrom, dateTo);
		updateTables();
		updateTotals();
		
		if (tempBill != null){
			// bug, didnt select
			billsGrid.select(tempBill);
		}
	}
	
	private Logging logger = new Logging();

	private static final long serialVersionUID = 1L;
	private TabSheet billsTabs;
	private Grid<Bill> billsGrid;
	private Grid<Bill> pendingGrid;
	private Grid<Bill> closedGrid;
	private JTable jTableToday;
	private JTable jTablePeriod;
	private JTable jTableUser;
	private HorizontalLayout rangeLayout;
	private HorizontalLayout buttonsLayout;
	private HorizontalLayout southLayout;
	private VerticalLayout totalsLayout;
	private Button newButton;
	private Button editButton;
	private JButton jButtonPrintReceipt;
	private Button deleteButton;
	private Button closeButton;
	private JButton jButtonReport;
	private JComboBox jComboUsers;
	private ComboBox monthsComboBox;
	private TextField yearsTextField;
	private Label toLabel;
	private Label fromLabel;
	private DateField toDate;
	private DateField fromDate;
	private GregorianCalendar dateFrom = new GregorianCalendar();
	private GregorianCalendar dateTo = new GregorianCalendar();
	private GregorianCalendar dateToday0 = new GregorianCalendar();
	private GregorianCalendar dateToday24 = new GregorianCalendar();

	private Button todayButton;
	
//	private String status;
	private String[] columsNames = {MessageBundle.getMessage("angal.billbrowser.id"),
			MessageBundle.getMessage("angal.common.date"), 
			MessageBundle.getMessage("angal.billbrowser.patientID"),
			MessageBundle.getMessage("angal.billbrowser.patient"), 
			MessageBundle.getMessage("angal.billbrowser.amount"), 
			MessageBundle.getMessage("angal.billbrowser.lastpayment"), 
			MessageBundle.getMessage("angal.billbrowser.status"), 
			MessageBundle.getMessage("angal.billbrowser.balance")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
	private int[] columsWidth = {50, 150, 50, 50, 100, 150, 50, 100};
	private int[] maxWidth = {150, 150, 150, 200, 100, 150, 50, 100};
	private boolean[] columsResizable = {false, false, false, true, false, false, false, false};
	private Class<?>[] columsClasses = {Integer.class, String.class, String.class, String.class, Double.class, String.class, String.class, Double.class};
	private boolean[] alignCenter = {true, true, true, false, false, true, true, false};
	private boolean[] boldCenter = {true, false, false, false, false, false, false, false};
	
//	private final int TabbedWidth = 800;
//	private final int TabbedHeight = 400;
//	private final int TotalWidth = TabbedWidth / 2;
//	private final int TotalHeight = 20;
	
	private BigDecimal totalToday;
	private BigDecimal balanceToday;
	private BigDecimal totalPeriod;
	private BigDecimal balancePeriod;
	private BigDecimal userToday;
	private BigDecimal userPeriod;
	private int month;
	private int year;
	
	//Bills & Payments
	private BillBrowserManager billManager = new BillBrowserManager();
	private ArrayList<Bill> billPeriod;
	private HashMap<Integer, Bill> mapBill = new HashMap<Integer, Bill>();
	private ArrayList<BillPayments> paymentsPeriod;
	private ArrayList<Bill> billFromPayments;
	
	private String currencyCod = new HospitalBrowsingManager().getHospitalCurrencyCod();
	
	//Users
	private String user = MainMenu.getUser();
	private ArrayList<String> users = billManager.getUsers();
	
	public BillBrowser() {
		updateDataSet();
		initComponents();
		// setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		// //setResizable(false);
		// setLocationRelativeTo(null);
		// setVisible(true);
	}
	
	private void initComponents() {
		UI.getCurrent().addWindow(this);
		VerticalLayout windowContent = new VerticalLayout();
		setContent(windowContent);
		windowContent.addComponent(getRangeLayout());
		windowContent.addComponent(getBillsTabs());
		windowContent.addComponent(getSouthLayout());
		setCaption(MessageBundle.getMessage("angal.billbrowser.title")); //$NON-NLS-1$
		// setMinimumSize(new Dimension(900,600));
		// addWindowListener(new WindowAdapter(){
			
		// 	public void windowClosing(WindowEvent e) {
		// 		//to free memory
		// 		billPeriod.clear();
		// 		mapBill.clear();
		// 		users.clear();
		// 		dispose();
		// 	}			
		// });
		// pack();
	}

	private HorizontalLayout getSouthLayout() {
		if (southLayout == null) {
			southLayout = new HorizontalLayout();
			southLayout.addComponent(getTotalsLayout());
			southLayout.addComponent(getButtonsLayout());
		}
		return southLayout;
	}
	Label balanceTodayLabel;
	Label balancePeriodLabel;
	private VerticalLayout getTotalsLayout() {
		if (totalsLayout == null) {
			totalsLayout = new VerticalLayout();
			balanceTodayLabel = new Label();
			balanceTodayLabel.setValue("<html><b>"+MessageBundle.getMessage("angal.billbrowser.todaym")+ "</b></html>"+
								currencyCod+
								totalToday+ 
								"<html><b>"+MessageBundle.getMessage("angal.billbrowser.notpaid")+ "</b></html>"+ 
								currencyCod+
								balanceToday);
			balanceTodayLabel.setContentMode(ContentMode.HTML);
			totalsLayout.addComponent(balanceTodayLabel);
			balancePeriodLabel = new Label();
			balancePeriodLabel.setValue("<html><b>"+MessageBundle.getMessage("angal.billbrowser.periodm")+"</b></html>"+ 
								currencyCod+
								totalPeriod+ 
								"<html><b>"+MessageBundle.getMessage("angal.billbrowser.notpaid")+"</b></html>"+ 
								currencyCod+
								balancePeriod);
			balancePeriodLabel.setContentMode(ContentMode.HTML);
			totalsLayout.addComponent(balancePeriodLabel);
			// if (!GeneralData.SINGLEUSER) totalsLayout.addComponent(getJTableUser());

			if(currencyCod==null){
				currencyCod="";
				logger.info("curnul");
			}
			updateTotals();
		}
		return totalsLayout;
	}

	private Label getToLabel() {
		if (toLabel == null) {
			toLabel = new Label();
			toLabel.setValue(MessageBundle.getMessage("angal.billbrowser.to")); //$NON-NLS-1$
		}
		return toLabel;
	}
	private LocalDate dateToLocalDate(Date date){
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}
	private Date localDateToDate(LocalDate localDate){
		return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}
	private DateField getFromDate(){
		if (fromDate == null) {
			dateFrom.set(GregorianCalendar.HOUR_OF_DAY, 0);
			dateFrom.set(GregorianCalendar.MINUTE, 0);
			dateFrom.set(GregorianCalendar.SECOND, 0);
			dateToday0.setTime(dateFrom.getTime());
			fromDate = new DateField("",dateToLocalDate(dateFrom.getTime())); // Calendar
			fromDate.setLocale(new Locale(GeneralData.LANGUAGE));
			fromDate.setDateFormat("dd/MM/yy"); //$NON-NLS-1$
			fromDate.addValueChangeListener(evt->{ //$NON-NLS-1$
				// fromDate.setva((Date) evt.getNewValue());
				dateFrom.setTime(localDateToDate(evt.getValue()));
				dateFrom.set(GregorianCalendar.HOUR_OF_DAY, 0);
				dateFrom.set(GregorianCalendar.MINUTE, 0);
				dateFrom.set(GregorianCalendar.SECOND, 0);
				// SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy");
				todayButton.setEnabled(true);
				billInserted(null);
			});
		}			
		return fromDate;
	}

	private DateField getToDate() {
		if (toDate == null) {
			dateTo.set(GregorianCalendar.HOUR_OF_DAY, 23);
			dateTo.set(GregorianCalendar.MINUTE, 59);
			dateTo.set(GregorianCalendar.SECOND, 59);
			dateToday24.setTime(dateTo.getTime());
			toDate = new DateField("",dateToLocalDate(dateTo.getTime())); // Calendar
			toDate.setLocale(new Locale(GeneralData.LANGUAGE));
			toDate.setDateFormat("dd/MM/yy"); //$NON-NLS-1$
			toDate.addValueChangeListener(evt->{ //$NON-NLS-1$
				// toDate.setDate(localDate(evt.getNewValue()));
				dateTo.setTime(localDateToDate(evt.getValue()));
				dateTo.set(GregorianCalendar.HOUR_OF_DAY, 23);
				dateTo.set(GregorianCalendar.MINUTE, 59);
				dateTo.set(GregorianCalendar.SECOND, 59);
				//dateToday24.setTime(dateTo.getTime());
				todayButton.setEnabled(true);
				billInserted(null);
			});
		}
		return toDate;
	}
	
	private Label getFromLabel() {
		if (fromLabel == null) {
			fromLabel = new Label();
			fromLabel.setValue(MessageBundle.getMessage("angal.billbrowser.from")); //$NON-NLS-1$
		}
		return fromLabel;
	}
	
	private JButton getJButtonReport() {
		if (jButtonReport == null) {
			jButtonReport = new JButton();
			jButtonReport.setMnemonic(KeyEvent.VK_R);
			jButtonReport.setText(MessageBundle.getMessage("angal.billbrowser.report")); //$NON-NLS-1$
			jButtonReport.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ArrayList<String> options = new ArrayList<String>();
					options.add(MessageBundle.getMessage("angal.billbrowser.todayclosure"));
					options.add(MessageBundle.getMessage("angal.billbrowser.today"));
					options.add(MessageBundle.getMessage("angal.billbrowser.period"));
					options.add(MessageBundle.getMessage("angal.billbrowser.thismonth"));
					options.add(MessageBundle.getMessage("angal.billbrowser.othermonth"));
					
					Icon icon = new ImageIcon("rsc/icons/calendar_dialog.png"); //$NON-NLS-1$
					// String option = (String) JOptionPane.showInputDialog(BillBrowser.this, 
					// 		MessageBundle.getMessage("angal.billbrowser.pleaseselectareport"), 
					// 		MessageBundle.getMessage("angal.billbrowser.report"), 
					// 		JOptionPane.INFORMATION_MESSAGE, 
					// 		icon, 
					// 		options.toArray(), 
					// 		options.get(0));
					
					// if (option == null) return;
					
					// String from = null;
					// String to = null;
					
					// int i = 0;
					
					// if (options.indexOf(option) == i) {
							
					// 		from = formatDateTimeReport(dateToday0);
					// 		to = formatDateTimeReport(dateToday24);
					// 		String user;
					// 		if (GeneralData.SINGLEUSER) {
					// 			user = "admin";
					// 		} else {
					// 			user = MainMenu.getUser();
					// 		}
					// 		// new GenericReportUserInDate(from, to, user, "BillsReportUserInDate");
					// 		return;
							
					// 	}
					// if (options.indexOf(option) == ++i) {
						
					// 	from = formatDateTimeReport(dateToday0);
					// 	to = formatDateTimeReport(dateToday24);
					// }
					// if (options.indexOf(option) == ++i) {
						
					// 	from = formatDateTimeReport(dateFrom);
					// 	to = formatDateTimeReport(dateTo);
					// }
					// if (options.indexOf(option) == ++i) {
						
					// 	month = monthsComboBox.getMonth();
					// 	GregorianCalendar thisMonthFrom = dateFrom;
					// 	GregorianCalendar thisMonthTo = dateTo;
					// 	thisMonthFrom.set(GregorianCalendar.MONTH, month);
					// 	thisMonthFrom.set(GregorianCalendar.DAY_OF_MONTH, 1);
					// 	thisMonthTo.set(GregorianCalendar.MONTH, month);
					// 	thisMonthTo.set(GregorianCalendar.DAY_OF_MONTH, dateFrom.getActualMaximum(GregorianCalendar.DAY_OF_MONTH));
					// 	from = formatDateTimeReport(thisMonthFrom);
					// 	to = formatDateTimeReport(thisMonthTo);
					// }
					// if (options.indexOf(option) == ++i) {
						
					// 	icon = new ImageIcon("rsc/icons/calendar_dialog.png"); //$NON-NLS-1$
						
					// 	int month;
					// 	JMonthChooser monthChooser = new JMonthChooser();
					// 	monthChooser.setLocale(new Locale(GeneralData.LANGUAGE));
						
				 //        // int r = JOptionPane.showConfirmDialog(BillBrowser.this, 
				 //        // 		monthChooser, 
				 //        // 		MessageBundle.getMessage("angal.billbrowser.month"), 
				 //        // 		JOptionPane.OK_CANCEL_OPTION, 
				 //        // 		JOptionPane.PLAIN_MESSAGE,
				 //        // 		icon);

				 //        // if (r == JOptionPane.OK_OPTION) {
				 //        // 	month = monthChooser.getMonth();
				 //        // } else {
				 //        //     return;
				 //        // }
				        
					// 	GregorianCalendar thisMonthFrom = dateFrom;
					// 	GregorianCalendar thisMonthTo = dateTo;
					// 	thisMonthFrom.set(GregorianCalendar.MONTH, month);
					// 	thisMonthFrom.set(GregorianCalendar.DAY_OF_MONTH, 1);
					// 	thisMonthTo.set(GregorianCalendar.MONTH, month);
					// 	thisMonthTo.set(GregorianCalendar.DAY_OF_MONTH, dateFrom.getActualMaximum(GregorianCalendar.DAY_OF_MONTH));
					// 	from = formatDateTimeReport(thisMonthFrom);
					// 	to = formatDateTimeReport(thisMonthTo);
					// }

					options = new ArrayList<String>();
					options.add(MessageBundle.getMessage("angal.billbrowser.shortreportonlybaddebts"));
					options.add(MessageBundle.getMessage("angal.billbrowser.fullreportallbills"));
										
					icon = new ImageIcon("rsc/icons/list_dialog.png"); //$NON-NLS-1$
					// option = (String) JOptionPane.showInputDialog(BillBrowser.this, 
					// 		MessageBundle.getMessage("angal.billbrowser.pleaseselectareport"), 
					// 		MessageBundle.getMessage("angal.billbrowser.report"), 
					// 		JOptionPane.INFORMATION_MESSAGE, 
					// 		icon, 
					// 		options.toArray(), 
					// 		options.get(0));
					
					// if (option == null) return;
					
					// if (options.indexOf(option) == 0) {
					// 	new GenericReportFromDateToDate(from, to, GeneralData.BILLSREPORTMONTH, MessageBundle.getMessage("angal.billbrowser.shortreportonlybaddebts"), false);
					// }
					// if (options.indexOf(option) == 1) {
					// 	new GenericReportFromDateToDate(from, to, GeneralData.BILLSREPORT, MessageBundle.getMessage("angal.billbrowser.fullreportallbills"), false);
					// }
				}
			});
		}
		return jButtonReport;
	}
	
	private Button getCloseButton() {
		if (closeButton == null) {
			closeButton = new Button();
			closeButton.setCaption(MessageBundle.getMessage("angal.common.close")); //$NON-NLS-1$
			////closeButton.setClickShortcut(KeyEvent.VK_C);
			closeButton.addClickListener(e->{
				//to free memory
				billPeriod.clear();
				mapBill.clear();
				users.clear();
				close();
			});
		}
		return closeButton;
	}
	private Object getSelectedRow(Grid grid){
		return grid.getSelectedItems().toArray()[0];
	}
	private Button getEditButton() {
		if (editButton == null) {
			editButton = new Button();
			editButton.setCaption(MessageBundle.getMessage("angal.billbrowser.editbill")); //$NON-NLS-1$
			////editButton.setClickShortcut(KeyEvent.VK_E);
			editButton.addClickListener(e->{
				try {
					if (billsTabs.getTabPosition(billsTabs.getTab(billsTabs.getSelectedTab()))==0) {
						Bill editBill = (Bill)getSelectedRow(billsGrid);
						if (user.equals("admin") || editBill.getStatus().equals("O")) { //$NON-NLS-1$
							PatientBillEdit pbe = new PatientBillEdit(editBill, false);
							pbe.addPatientBillListener(this);
							pbe.setVisible(true);
						} else {
							// new GenericReportBill(editBill.getId(), GeneralData.PATIENTBILL);
						}
					}
					if (billsTabs.getTabPosition(billsTabs.getTab(billsTabs.getSelectedTab()))==1) {
						Bill editBill = (Bill)getSelectedRow(pendingGrid);
						PatientBillEdit pbe = new PatientBillEdit(editBill, false);
						pbe.addPatientBillListener(this);
						pbe.setVisible(true);
					}
					if (billsTabs.getTabPosition(billsTabs.getTab(billsTabs.getSelectedTab()))==2) {
						Bill editBill = (Bill)getSelectedRow(closedGrid);
						// new GenericReportBill(editBill.getId(), GeneralData.PATIENTBILL);
					}
				} catch (Exception ex) {
					MessageBox.create().withCaption(MessageBundle.getMessage("angal.billbrowser.title"))
					.withMessage(MessageBundle.getMessage("angal.billbrowser.pleaseselectabillfirst")).open();
				}
			});
		}
		return editButton;
	}
	
	private JButton getJButtonPrintReceipt() {
		if (jButtonPrintReceipt == null) {
			jButtonPrintReceipt = new JButton();
			jButtonPrintReceipt.setText(MessageBundle.getMessage("angal.billbrowser.receipt")); //$NON-NLS-1$
			jButtonPrintReceipt.setMnemonic(KeyEvent.VK_R);
			jButtonPrintReceipt.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					try {
						// if (jScrollPaneBills.isShowing()) {
							// int rowSelected = billsGrid.getSelectedRow();
							// Bill editBill = (Bill)billsGrid.getValueAt(rowSelected, -1);
							// if (editBill.getStatus().equals("C")) { //$NON-NLS-1$
							// 	// new GenericReportBill(editBill.getId(), GeneralData.PATIENTBILL, false, true);
							// } else {
								// if (editBill.getStatus().equals("D")) {
								// 	JOptionPane.showMessageDialog(BillBrowser.this,
								// 			MessageBundle.getMessage("angal.billbrowser.billdeleted"),  //$NON-NLS-1$
								// 			MessageBundle.getMessage("angal.hospital"),  //$NON-NLS-1$
								// 			JOptionPane.CANCEL_OPTION);
								// 	return;
								// }
								// JOptionPane.showMessageDialog(BillBrowser.this,
								// 		MessageBundle.getMessage("angal.billbrowser.billnotyetclosed"),  //$NON-NLS-1$
								// 		MessageBundle.getMessage("angal.hospital"),  //$NON-NLS-1$
								// 		JOptionPane.CANCEL_OPTION);
								// return;
							// }
						// }
						// if (jScrollPanePending.isShowing()) {
						// 	int rowSelected = pendingGrid.getSelectedRow();
						// 	Bill editBill = (Bill)pendingGrid.getValueAt(rowSelected, -1);
						// 	PatientBillEdit pbe = new PatientBillEdit(editBill, false);
						// 	pbe.addPatientBillListener(BillBrowser.this);
						// 	pbe.setVisible(true);
						// }
						// if (jScrollPaneClosed.isShowing()) {
						// 	int rowSelected = closedGrid.getSelectedRow();
						// 	Bill editBill = (Bill)closedGrid.getValueAt(rowSelected, -1);
						// 	// new GenericReportBill(editBill.getId(), GeneralData.PATIENTBILL);
						// }
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(null,
								MessageBundle.getMessage("angal.billbrowser.pleaseselectabillfirst"), //$NON-NLS-1$
								MessageBundle.getMessage("angal.billbrowser.title"), //$NON-NLS-1$
								JOptionPane.PLAIN_MESSAGE);
					}
				}
			});
		}
		return jButtonPrintReceipt;
	}

	private Button getNewButton() {
		if (newButton == null) {
			newButton = new Button();
			newButton.setCaption(MessageBundle.getMessage("angal.billbrowser.newbill")); //$NON-NLS-1$
			////newButton.setClickShortcut(KeyEvent.VK_N);
			newButton.addClickListener(e->{
				PatientBillEdit newBill = new PatientBillEdit(new Bill(), true);
				newBill.addPatientBillListener(this);
				newBill.setVisible(true);
			});
		}
		return newButton;
	}
	
	Bill deleteBill = null;
	private Button getDeleteButton() {
		if (deleteButton == null) {
			deleteButton = new Button();
			deleteButton.setCaption(MessageBundle.getMessage("angal.billbrowser.deletebill")); //$NON-NLS-1$
			////deleteButton.setClickShortcut(KeyEvent.VK_D);
			deleteButton.addClickListener(e->{
				try {
					int ok = JOptionPane.NO_OPTION;
					if (billsTabs.getTabPosition(billsTabs.getTab(billsTabs.getSelectedTab()))==0) {
						deleteBill = (Bill)getSelectedRow(billsGrid);
						MessageBox.create().withCaption(MessageBundle.getMessage("angal.common.delete"))
						.withMessage(MessageBundle.getMessage("angal.billbrowser.doyoureallywanttodeletetheselectedbill"))
						.withYesButton(()->{
							billManager.deleteBill(deleteBill);
							billInserted(null);
						}).withNoButton().open();
					}
					// if (jScrollPanePending != null && jScrollPanePending.isShowing()) {
					if (billsTabs.getTabPosition(billsTabs.getTab(billsTabs.getSelectedTab()))==1) {
						deleteBill = (Bill)getSelectedRow(pendingGrid);
						MessageBox.create().withCaption(MessageBundle.getMessage("angal.common.delete"))
						.withMessage(MessageBundle.getMessage("angal.billbrowser.doyoureallywanttodeletetheselectedbill"))
						.withYesButton(()->{
							billManager.deleteBill(deleteBill);
							billInserted(null);
						}).withNoButton().open();
					}
					if (billsTabs.getTabPosition(billsTabs.getTab(billsTabs.getSelectedTab()))==2) {
						deleteBill = (Bill)getSelectedRow(closedGrid);
						MessageBox.create().withCaption(MessageBundle.getMessage("angal.common.delete"))
						.withMessage(MessageBundle.getMessage("angal.billbrowser.doyoureallywanttodeletetheselectedbill"))
						.withYesButton(()->{
							billManager.deleteBill(deleteBill);
							billInserted(null);
						}).withNoButton().open();
					}
				} catch (Exception ex) {
					MessageBox.create().withCaption(MessageBundle.getMessage("angal.hospital"))
					.withMessage(MessageBundle.getMessage("angal.billbrowser.pleaseselectabillfirst")).open();
				}
			});
		}
		return deleteButton;
	}

	private HorizontalLayout getButtonsLayout() {
		if (buttonsLayout == null) {
			buttonsLayout = new HorizontalLayout();
			if (MainMenu.checkUserGrants("btnbillnew")) buttonsLayout.addComponent(getNewButton());
			if (MainMenu.checkUserGrants("btnbilledit")) buttonsLayout.addComponent(getEditButton());
			if (MainMenu.checkUserGrants("btnbilldelete")) buttonsLayout.addComponent(getDeleteButton());
			// if (MainMenu.checkUserGrants("btnbillreceipt") && GeneralData.RECEIPTPRINTER) buttonsLayout.addComponent(getJButtonPrintReceipt());
			// if (MainMenu.checkUserGrants("btnbillreport")) buttonsLayout.addComponent(getJButtonReport());
			buttonsLayout.addComponent(getCloseButton());
		}
		return buttonsLayout;
	}

	private HorizontalLayout getRangeLayout() {
		if (rangeLayout == null) {
			rangeLayout = new HorizontalLayout();
			// if (!GeneralData.SINGLEUSER && user.equals("admin")) 
			// 	rangeLayout.addComponent(getJComboUsers());
				rangeLayout.addComponent(getTodayButton());
				rangeLayout.addComponent(getFromLabel());
				rangeLayout.addComponent(getFromDate());
				rangeLayout.addComponent(getToLabel());
				rangeLayout.addComponent(getToDate());
				rangeLayout.addComponent(getMonthsComboBox());
				rangeLayout.addComponent(getYearsTextField());
						
		}
		return rangeLayout;
	}

	private JComboBox getJComboUsers() {
		if (jComboUsers == null) {
			jComboUsers = new JComboBox();
			for (String user : users) 
				jComboUsers.addItem(user);
			
			jComboUsers.addActionListener(new ActionListener() {				
				public void actionPerformed(ActionEvent arg0) {
					user = (String) jComboUsers.getSelectedItem();
					jTableUser.setValueAt("<html><b>"+user+"</b></html>", 0, 0);
					updateTotals();
				}
			});
		}
		return jComboUsers;
	}
	
	private Button getTodayButton() {
		if (todayButton == null) {
			todayButton = new Button();
			todayButton.setCaption(MessageBundle.getMessage("angal.billbrowser.today")); //$NON-NLS-1$
			////todayButton.setClickShortcut(KeyEvent.VK_T);
			todayButton.addClickListener(e->{
				dateFrom.setTime(dateToday0.getTime());
				dateTo.setTime(dateToday24.getTime());
				
				fromDate.setValue(dateToLocalDate(dateFrom.getTime()));
				toDate.setValue(dateToLocalDate(dateTo.getTime()));
				
				todayButton.setEnabled(false);
			});
			todayButton.setEnabled(false);
		}
		return todayButton;
	}

	private ComboBox getMonthsComboBox() {
		if (monthsComboBox == null) {
			monthsComboBox = new ComboBox();
			monthsComboBox.setEmptySelectionAllowed(false);
			monthsComboBox.setItems(Month.values());
			monthsComboBox.setValue(LocalDate.now().getMonth());
			monthsComboBox.addValueChangeListener(evt->{ //$NON-NLS-1$
				month =((Month)monthsComboBox.getValue()).getValue()-1;
				dateFrom.set(GregorianCalendar.MONTH, month);
				dateFrom.set(GregorianCalendar.DAY_OF_MONTH, 1);
				dateTo.set(GregorianCalendar.MONTH, month);
				dateTo.set(GregorianCalendar.DAY_OF_MONTH, dateFrom.getActualMaximum(GregorianCalendar.DAY_OF_MONTH));
				
				fromDate.setValue(dateToLocalDate(dateFrom.getTime()));
				toDate.setValue(dateToLocalDate(dateTo.getTime()));
			});
		}
		return monthsComboBox;
	}

	private TextField getYearsTextField() {//changed behavior
		if (yearsTextField == null) {
			yearsTextField = new TextField();
			yearsTextField.setValue(""+LocalDate.now().getYear());
			yearsTextField.addValueChangeListener(evt->{ //$NON-NLS-1$
				year = Integer.parseInt(yearsTextField.getValue());
				dateFrom.set(GregorianCalendar.YEAR, year);
				dateFrom.set(GregorianCalendar.MONTH, 1);
				dateFrom.set(GregorianCalendar.DAY_OF_YEAR, 1);
				dateTo.set(GregorianCalendar.YEAR, year);
				dateTo.set(GregorianCalendar.MONTH, 12);
				dateTo.set(GregorianCalendar.DAY_OF_YEAR, dateFrom.getActualMaximum(GregorianCalendar.DAY_OF_YEAR));
				fromDate.setValue(dateToLocalDate(dateFrom.getTime()));
				toDate.setValue(dateToLocalDate(dateTo.getTime()));
			});
		}
		return yearsTextField;
	}

	private Grid getBillsGrid() {
		if (billsGrid == null) {
			billsGrid = new Grid();
			billsGrid.setWidth("100%");
			// billsGrid.setModel(new BillTableModel("ALL")); //$NON-NLS-1$
			loadData("ALL");
			billsGrid.setItems(billsArray);
			billsGrid.addColumn(Bill::getId).setCaption("ID");
			billsGrid.addColumn(Bill::getDateString).setCaption("Date");
			billsGrid.addColumn(Bill::getPatID).setCaption("Pat.ID");
			billsGrid.addColumn(Bill::getPatName).setCaption("Patient");
			billsGrid.addColumn(Bill::getAmount).setCaption("Amount");
			billsGrid.addColumn(Bill::getLastPayment).setCaption("Last Payment");
			billsGrid.addColumn(Bill::getStatus).setCaption("Status");
			billsGrid.addColumn(Bill::getBalance).setCaption("Balance");
			// for (int i=0;i<columsWidth.length; i++){
			// 	billsGrid.getColumnModel().getColumn(i).setMinWidth(columsWidth[i]);
			// 	if (!columsResizable[i]) billsGrid.getColumnModel().getColumn(i).setMaxWidth(maxWidth[i]);
			// 	if (alignCenter[i]) {
			// 		billsGrid.getColumnModel().getColumn(i).setCellRenderer(new StringCenterTableCellRenderer());
			// 		if (boldCenter[i]) {
			// 			billsGrid.getColumnModel().getColumn(i).setCellRenderer(new CenterBoldTableCellRenderer());
			// 		}
			// 	}
			// }
			// billsGrid.setAutoCreateColumnsFromModel(false);
			// billsGrid.setDefaultRenderer(String.class, new StringTableCellRenderer());
			// billsGrid.setDefaultRenderer(Integer.class, new IntegerTableCellRenderer());
			// billsGrid.setDefaultRenderer(Double.class, new DoubleTableCellRenderer());
		}
		return billsGrid;
	}
	private Grid getPendingGrid() {
		if (pendingGrid == null) {
			pendingGrid = new Grid();
			pendingGrid.setWidth("100%");
			// pendingGrid.setModel(new BillTableModel("ALL")); //$NON-NLS-1$
			loadData("O");
			pendingGrid.setItems(pendingArray);
			pendingGrid.addColumn(Bill::getId).setCaption("ID");
			pendingGrid.addColumn(Bill::getDateString).setCaption("Date");
			pendingGrid.addColumn(Bill::getPatID).setCaption("Pat.ID");
			pendingGrid.addColumn(Bill::getPatName).setCaption("Patient");
			pendingGrid.addColumn(Bill::getAmount).setCaption("Amount");
			pendingGrid.addColumn(Bill::getLastPayment).setCaption("Last Payment");
			pendingGrid.addColumn(Bill::getStatus).setCaption("Status");
			pendingGrid.addColumn(Bill::getBalance).setCaption("Balance");
			// for (int i=0;i<columsWidth.length; i++){
			// 	pendingGrid.getColumnModel().getColumn(i).setMinWidth(columsWidth[i]);
			// 	if (!columsResizable[i]) pendingGrid.getColumnModel().getColumn(i).setMaxWidth(maxWidth[i]);
			// 	if (alignCenter[i]) {
			// 		pendingGrid.getColumnModel().getColumn(i).setCellRenderer(new StringCenterTableCellRenderer());
			// 		if (boldCenter[i]) {
			// 			pendingGrid.getColumnModel().getColumn(i).setCellRenderer(new CenterBoldTableCellRenderer());
			// 		}
			// 	}
			// }
			// pendingGrid.setAutoCreateColumnsFromModel(false);
			// pendingGrid.setDefaultRenderer(String.class, new StringTableCellRenderer());
			// pendingGrid.setDefaultRenderer(Integer.class, new IntegerTableCellRenderer());
			// pendingGrid.setDefaultRenderer(Double.class, new DoubleTableCellRenderer());
		}
		return pendingGrid;
	}
	private Grid getClosedGrid() {
		if (closedGrid == null) {
			closedGrid = new Grid();
			closedGrid.setWidth("100%");
			// closedGrid.setModel(new BillTableModel("ALL")); //$NON-NLS-1$
			loadData("C");
			closedGrid.setItems(closedArray);
			closedGrid.addColumn(Bill::getId).setCaption("ID");
			closedGrid.addColumn(Bill::getDateString).setCaption("Date");
			closedGrid.addColumn(Bill::getPatID).setCaption("Pat.ID");
			closedGrid.addColumn(Bill::getPatName).setCaption("Patient");
			closedGrid.addColumn(Bill::getAmount).setCaption("Amount");
			closedGrid.addColumn(Bill::getLastPayment).setCaption("Last Payment");
			closedGrid.addColumn(Bill::getStatus).setCaption("Status");
			closedGrid.addColumn(Bill::getBalance).setCaption("Balance");
			// for (int i=0;i<columsWidth.length; i++){
			// 	closedGrid.getColumnModel().getColumn(i).setMinWidth(columsWidth[i]);
			// 	if (!columsResizable[i]) closedGrid.getColumnModel().getColumn(i).setMaxWidth(maxWidth[i]);
			// 	if (alignCenter[i]) {
			// 		closedGrid.getColumnModel().getColumn(i).setCellRenderer(new StringCenterTableCellRenderer());
			// 		if (boldCenter[i]) {
			// 			closedGrid.getColumnModel().getColumn(i).setCellRenderer(new CenterBoldTableCellRenderer());
			// 		}
			// 	}
			// }
			// closedGrid.setAutoCreateColumnsFromModel(false);
			// closedGrid.setDefaultRenderer(String.class, new StringTableCellRenderer());
			// closedGrid.setDefaultRenderer(Integer.class, new IntegerTableCellRenderer());
			// closedGrid.setDefaultRenderer(Double.class, new DoubleTableCellRenderer());
		}
		return closedGrid;
	}

	private TabSheet getBillsTabs() {
		if (billsTabs == null) {
			billsTabs = new TabSheet();
			billsTabs.addTab(getBillsGrid(),MessageBundle.getMessage("angal.billbrowser.bills"));
			billsTabs.addTab(getPendingGrid(),MessageBundle.getMessage("angal.billbrowser.pending"));
			billsTabs.addTab(getClosedGrid(),MessageBundle.getMessage("angal.billbrowser.closed")); //$NON-NLS-1$
		}
		return billsTabs;
	}

	private JTable getJTableToday() {
		if (jTableToday == null) {
			jTableToday = new JTable();
			jTableToday.setModel(
					new DefaultTableModel(new Object[][] {
							{
								"<html><b>"+MessageBundle.getMessage("angal.billbrowser.todaym")+ "</b></html>",
								currencyCod,
								totalToday, 
								"<html><b>"+MessageBundle.getMessage("angal.billbrowser.notpaid")+ "</b></html>", 
								currencyCod,
								balanceToday}
							}, 	
							new String[] {"", "", "", "", "", ""}) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				private static final long serialVersionUID = 1L;
				Class<?>[] types = new Class<?>[] { JLabel.class, JLabel.class, Double.class, JLabel.class, JLabel.class, Double.class};
	
				public Class<?> getColumnClass(int columnIndex) {
					return types[columnIndex];
				}

				public boolean isCellEditable(int row, int column) {
					return false;
				}
			});
			jTableToday.getColumnModel().getColumn(1).setPreferredWidth(3);
			jTableToday.getColumnModel().getColumn(4).setPreferredWidth(3);
			jTableToday.setRowSelectionAllowed(false);
			jTableToday.setGridColor(Color.WHITE);

		}
		return jTableToday;
	}
	
	private JTable getJTablePeriod() {
		if (jTablePeriod == null) {
			jTablePeriod = new JTable();
			jTablePeriod.setModel(new DefaultTableModel(
					new Object[][] {
							{
								"<html><b>"+MessageBundle.getMessage("angal.billbrowser.periodm")+"</b></html>", 
								currencyCod,
								totalPeriod, 
								"<html><b>"+MessageBundle.getMessage("angal.billbrowser.notpaid")+"</b></html>", 
								currencyCod,
								balancePeriod}
							}, 
							new String[] {"","","","","",""}) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				private static final long serialVersionUID = 1L;
				Class<?>[] types = new Class<?>[] { JLabel.class, JLabel.class, Double.class, JLabel.class, JLabel.class, Double.class};
	
				public Class<?> getColumnClass(int columnIndex) {
					return types[columnIndex];
				}

				public boolean isCellEditable(int row, int column) {
					return false;
				}
			});
			jTablePeriod.getColumnModel().getColumn(1).setPreferredWidth(3);
			jTablePeriod.getColumnModel().getColumn(4).setPreferredWidth(3);
			jTablePeriod.setRowSelectionAllowed(false);
			jTablePeriod.setGridColor(Color.WHITE);

		}
		return jTablePeriod;
	}
	
	private JTable getJTableUser() {
		if (jTableUser == null) {
			jTableUser = new JTable();
			jTableUser.setModel(new DefaultTableModel(new Object[][] {{"<html><b>"+user+"</b></html>", userToday, "<html><b>"+MessageBundle.getMessage("angal.billbrowser.period")+"</b></html>", userPeriod}}, new String[] {"","","",""}) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				private static final long serialVersionUID = 1L;
				Class<?>[] types = new Class<?>[] { JLabel.class, Double.class, JLabel.class, Double.class};
	
				public Class<?> getColumnClass(int columnIndex) {
					return types[columnIndex];
				}

				public boolean isCellEditable(int row, int column) {
					return false;
				}
			});
			jTableUser.setRowSelectionAllowed(false);
			jTableUser.setGridColor(Color.WHITE);

		}
		return jTableUser;
	}
	
	private void updateTables() {
		loadData("ALL");
		billsGrid.setItems(billsArray);
		loadData("O");
		pendingGrid.setItems(pendingArray);
		loadData("C");
		closedGrid.setItems(closedArray);
	}
	
	private void updateDataSet() {
//		System.out.println(formatDateTime(new DateTime().minusMonths(5).toDateMidnight().toGregorianCalendar()));
//		System.out.println(formatDateTime(new DateTime().toDateMidnight().plusDays(1).toGregorianCalendar()));
		updateDataSet(new DateTime().toDateMidnight().toGregorianCalendar(), new DateTime().toDateMidnight().plusDays(1).toGregorianCalendar());
		
	}
	
	private void updateDataSet(GregorianCalendar dateFrom, GregorianCalendar dateTo) {
		/*
		 * Bills in the period
		 */
		billPeriod = billManager.getBills(dateFrom, dateTo);
		/*
		 * Payments in the period
		 */
		paymentsPeriod = billManager.getPayments(dateFrom, dateTo);
		
		/*
		 * Bills not in the period but with payments in the period
		 */
		billFromPayments = billManager.getBills(paymentsPeriod);
	}
	
	private void updateTotals() {
		ArrayList<Bill> billToday;
		ArrayList<BillPayments> paymentsToday;
		if (MainMenu.getUser().equals("admin")) {
			billToday = billManager.getBills(dateToday0, dateToday24);
			paymentsToday = billManager.getPayments(dateToday0, dateToday24);
		} else {
			billToday = billPeriod;
			paymentsToday = paymentsPeriod;
		}
		
		totalPeriod = new BigDecimal(0);
		balancePeriod = new BigDecimal(0);
		totalToday = new BigDecimal(0);
		balanceToday = new BigDecimal(0);
		userToday = new BigDecimal(0);
		userPeriod = new BigDecimal(0);
		
		ArrayList<Integer> notDeletedBills = new ArrayList<Integer>();
				
		//Bills in range contribute for Not Paid (balance)
		for (Bill bill : billPeriod) {
			if (!bill.getStatus().equals("D")) {
				notDeletedBills.add(bill.getId());
				BigDecimal balance = new BigDecimal(Double.toString(bill.getBalance()));
				balancePeriod = balancePeriod.add(balance);
			}
		}
		
		//Payments in range contribute for Paid Period (total)
		for (BillPayments payment : paymentsPeriod) {
			if (notDeletedBills.contains(payment.getBillID())) {
				BigDecimal payAmount = new BigDecimal(Double.toString(payment.getAmount()));
				String payUser = payment.getUser();
				
				totalPeriod = totalPeriod.add(payAmount);
					
				if (!GeneralData.SINGLEUSER && payUser.equals(user))
					userPeriod = userPeriod.add(payAmount);
			}
		}
		
		//Bills in today contribute for Not Paid Today (balance)
		for (Bill bill : billToday) {
			if (!bill.getStatus().equals("D")) {
				BigDecimal balance = new BigDecimal(Double.toString(bill.getBalance()));
				balanceToday = balanceToday.add(balance);
			}
		}
		
		//Payments in today contribute for Paid Today (total)
		for (BillPayments payment : paymentsToday) {
			if (notDeletedBills.contains(payment.getBillID())) {
				BigDecimal payAmount = new BigDecimal(Double.toString(payment.getAmount()));
				String payUser = payment.getUser();
				totalToday = totalToday.add(payAmount);
				if (!GeneralData.SINGLEUSER && payUser.equals(user))
					userToday = userToday.add(payAmount);
			}
		}
		balanceTodayLabel.setValue("<html><b>"+MessageBundle.getMessage("angal.billbrowser.todaym")+ "</b></html>"+
		currencyCod+
		totalToday+ 
		"<html><b>"+MessageBundle.getMessage("angal.billbrowser.notpaid")+ "</b></html>"+ 
		currencyCod+
		balanceToday);
		balancePeriodLabel.setValue("<html><b>"+MessageBundle.getMessage("angal.billbrowser.periodm")+"</b></html>"+ 
		currencyCod+
		totalPeriod+ 
		"<html><b>"+MessageBundle.getMessage("angal.billbrowser.notpaid")+"</b></html>"+
		currencyCod+
		balancePeriod);
		// jTableToday.setValueAt(totalToday, 0, 2);
		// jTableToday.setValueAt(balanceToday, 0, 5);
		// jTablePeriod.setValueAt(totalPeriod, 0, 2);
		// jTablePeriod.setValueAt(balancePeriod, 0, 5);
		// if (jTableUser != null) {
		// 	jTableUser.setValueAt(userToday, 0, 1);
		// 	jTableUser.setValueAt(userPeriod, 0, 3);
		// }
	}

	private ArrayList<Bill> billsArray = new ArrayList<Bill>();
	private ArrayList<Bill> pendingArray = new ArrayList<Bill>();
	private ArrayList<Bill> closedArray = new ArrayList<Bill>();
	private ArrayList<Bill> billAll = new ArrayList<Bill>();

	private void loadData(String status) {
		
		mapBill.clear();
		
		mapping(status);
	}

	private void mapping(String status) {
			
		/*
		 * Mappings Bills in the period 
		 */
		for (Bill bill : billPeriod) {
			//mapBill.clear();
			mapBill.put(bill.getId(), bill);
		}
		
		/*
		 * Merging the two bills lists
		 */
		
		if (status.equals("O")) {
			pendingArray.clear();
			pendingArray = billManager.getPendingBills(0);
		} else if (status.equals("ALL")) {
			billAll.clear();
			billAll.addAll(billPeriod);
			for (Bill bill : billFromPayments) {
				if (mapBill.get(bill.getId()) == null)
					billAll.add(bill);
			}
			// billsArray.clear();
			billsArray = null;
			Collections.sort(billAll);
			billsArray = billAll;
		} else if (status.equals("C")) {
			for (Bill bill : billPeriod) {
				// closedArray.clear();
				if (bill.getStatus().equals(status))
					closedArray.add(bill);
			}
		}
		Collections.sort(billsArray, Collections.reverseOrder());
		Collections.sort(pendingArray, Collections.reverseOrder());
		Collections.sort(closedArray, Collections.reverseOrder());
	}
	
	public class BillTableModel extends DefaultTableModel {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private ArrayList<Bill> tableArray = new ArrayList<Bill>();
		
		/*
		 * All Bills
		 */
		private ArrayList<Bill> billAll = new ArrayList<Bill>();
		
		public BillTableModel(String status) {
			loadData(status);
		}
		
		private void loadData(String status) {
			
			tableArray.clear();
			mapBill.clear();
			
			mapping(status);
		}
		
		private void mapping(String status) {
			
			/*
			 * Mappings Bills in the period 
			 */
			for (Bill bill : billPeriod) {
				//mapBill.clear();
				mapBill.put(bill.getId(), bill);
			}
			
			/*
			 * Merging the two bills lists
			 */
			billAll.addAll(billPeriod);
			for (Bill bill : billFromPayments) {
				if (mapBill.get(bill.getId()) == null)
					billAll.add(bill);
			}
			
			if (status.equals("O")) {
				tableArray = billManager.getPendingBills(0);
				
			} else if (status.equals("ALL")) {
				
				Collections.sort(billAll);
				tableArray = billAll;

			} else if (status.equals("C")) {
				for (Bill bill : billPeriod) {
					
					if (bill.getStatus().equals(status)) 
						tableArray.add(bill);
				}
			}
			
			Collections.sort(tableArray, Collections.reverseOrder());
		}
		
		public Class<?> getColumnClass(int columnIndex) {
			return columsClasses[columnIndex];
		}

		public int getColumnCount() {
			return columsNames.length;
		}

		public String getColumnName(int columnIndex) {
			return columsNames[columnIndex];
		}

		public int getRowCount() {
			if (tableArray == null)
				return 0;
			return tableArray.size();
		}
		
		//["Date", "Patient", "Balance", "Update", "Status", "Amount"};

		public Object getValueAt(int r, int c) {
			int index = -1;
			Bill thisBill = tableArray.get(r);
			if (c == index) {
				return thisBill;
			}
			if (c == ++index) {
				return thisBill.getId();
			}
			if (c == ++index) {
				return formatDateTime(thisBill.getDate());
			}
			if (c == ++index) {
				int patID = thisBill.getPatID();
				return patID == 0 ? "" : String.valueOf(patID);
			}
			if (c == ++index) {
				return thisBill.getPatName();
			}
			if (c == ++index) {
				return thisBill.getAmount();
			}
			if (c == ++index) {
				return formatDateTime(thisBill.getUpdate());
			}
			if (c == ++index) {
				return thisBill.getStatus();
			}
			if (c == ++index) {
				return thisBill.getBalance();
			}
			return null;
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

	}
	
	public String formatDate(GregorianCalendar time) {
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy");  //$NON-NLS-1$
		return format.format(time.getTime());
	}
	
	public String formatDateTime(GregorianCalendar time) {
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy - HH:mm:ss");  //$NON-NLS-1$
		return format.format(time.getTime());
	}
	
	public String formatDateTimeReport(GregorianCalendar time) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  //$NON-NLS-1$
		return format.format(time.getTime());
	}
	
	public boolean isSameDay(GregorianCalendar aDate, GregorianCalendar today) {
		return (aDate.get(Calendar.YEAR) == today.get(Calendar.YEAR)) &&
			   (aDate.get(Calendar.MONTH) == today.get(Calendar.MONTH)) &&
			   (aDate.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH));
	}
	
	class StringTableCellRenderer extends DefaultTableCellRenderer {  
	   
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
				boolean hasFocus, int row, int column) {  
		   
			Component cell=super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
			cell.setForeground(Color.BLACK);
			if (((String)table.getValueAt(row, 6)).equals("C")) { //$NON-NLS-1$
				cell.setForeground(Color.GRAY);
			}
			if (((String)table.getValueAt(row, 6)).equals("D")) { //$NON-NLS-1$
				cell.setForeground(Color.RED);
			}
			return cell;
	   }
	}
	
	class StringCenterTableCellRenderer extends DefaultTableCellRenderer {  
		   
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
				boolean hasFocus, int row, int column) {  
		   
			Component cell=super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
			cell.setForeground(Color.BLACK);
			setHorizontalAlignment(CENTER);
			if (((String)table.getValueAt(row, 6)).equals("C")) { //$NON-NLS-1$
				cell.setForeground(Color.GRAY);
			}
			if (((String)table.getValueAt(row, 6)).equals("D")) { //$NON-NLS-1$
				cell.setForeground(Color.RED);
			}
			return cell;
	   }
	}
	
	class IntegerTableCellRenderer extends DefaultTableCellRenderer {  
		   
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
				boolean hasFocus, int row, int column) {  
		   
			Component cell=super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
			cell.setForeground(Color.BLACK);
			cell.setFont(new Font(null, Font.BOLD, 12));
			setHorizontalAlignment(CENTER);
			if (((String)table.getValueAt(row, 6)).equals("C")) { //$NON-NLS-1$
				cell.setForeground(Color.GRAY);
			}
			if (((String)table.getValueAt(row, 6)).equals("D")) { //$NON-NLS-1$
				cell.setForeground(Color.RED);
			}
			return cell;
	   }
	}
	
	class DoubleTableCellRenderer extends DefaultTableCellRenderer {  
		   
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
				boolean hasFocus, int row, int column) {  
		   
			Component cell=super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
			cell.setForeground(Color.BLACK);
			setHorizontalAlignment(RIGHT);
			if (((String)table.getValueAt(row, 6)).equals("C")) { //$NON-NLS-1$
				cell.setForeground(Color.GRAY);
			}
			if (((String)table.getValueAt(row, 6)).equals("D")) { //$NON-NLS-1$
				cell.setForeground(Color.RED);
			}
			return cell;
	   }
	}
	
	class CenterBoldTableCellRenderer extends DefaultTableCellRenderer {  
		   
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
				boolean hasFocus, int row, int column) {  
		   
			Component cell=super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
			cell.setForeground(Color.BLACK);
			setHorizontalAlignment(CENTER);
			cell.setFont(new Font(null, Font.BOLD, 12));
			if (((String)table.getValueAt(row, 6)).equals("C")) { //$NON-NLS-1$
				cell.setForeground(Color.GRAY);
			}
			if (((String)table.getValueAt(row, 6)).equals("D")) { //$NON-NLS-1$
				cell.setForeground(Color.RED);
			}
			return cell;
	   }
	}
}
