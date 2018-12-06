package org.isf.menu.gui;

import java.awt.event.KeyEvent;
import java.io.File;
import java.util.*;
import java.lang.*;

import org.isf.generaldata.GeneralData;
import org.isf.menu.manager.UserBrowsingManager;
import org.isf.menu.model.User;
import org.isf.menu.model.UserMenuItem;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.admission.gui.AdmittedPatientBrowser;
import org.isf.utils.jobjects.ModalWindow;
//import sms.service.SmsSender;
//import xmpp.gui.CommunicationFrame;
//import xmpp.service.Server;
import org.isf.utils.Logging;

import org.jivesoftware.smack.XMPPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.slf4j.MDC;

import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Image;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.server.ThemeResource;

public class MainMenu implements SubMenu.CommandListener{
	private static final long serialVersionUID = 7620582079916035164L;
	private boolean flag_Xmpp = false;
	private boolean flag_Sms = false;
	static {
	        // SLF4JBridgeHandler.removeHandlersForRootLogger();
	        SLF4JBridgeHandler.install();
	    }
	private final Logger logger = LoggerFactory.getLogger(MainMenu.class);

	// public void loginInserted(AWTEvent e) {
	// 	if (e.getSource() instanceof User) {
	// 		myUser = (User) e.getSource();
	// 		MDC.put("OHUser", myUser.getUserName());
	// 		MDC.put("OHUserGroup", myUser.getUserGroupName());
	// 		logger.info("Logging: \"" + myUser.getUserName() + "\" user has logged the system.");
	// 	}
	// }

	public void commandInserted(String aCommand) {
		launchApp(aCommand);
	}

	public static boolean checkUserGrants(String code) {

		Iterator<UserMenuItem> it = myMenu.iterator();
		while (it.hasNext()) {
			UserMenuItem umi = it.next();
			if (umi.getCode().equalsIgnoreCase(code)) {
				return true;
			}
		}
		return false;
	}

	public static String getUser() {
		return myUser.getUserName();
	}

	private int minButtonSize = 0;

	public void setMinButtonSize(int value) {
		minButtonSize = value;
	}

	public int getMinButtonSize() {
		return minButtonSize;
	}

	private static User myUser = null;
	private static ArrayList<UserMenuItem> myMenu = null;

	final int menuXPosition = 10;
	final int menuYDisplacement = 75;

	// singleUser=true : one user
	private boolean singleUser = false;
	// internalPharmacies=false : no internalPharmacies
	private boolean internalPharmacies = false;
	// debug mode
	private boolean debug = false;
	private MainMenu myFrame;
	private HorizontalLayout layout0;
	private VerticalLayout layout;
	private UI main;
	private Logging qlogger = new Logging();

	public MainMenu() {

		GeneralData.getGeneralData();
//asd
		try {
			singleUser = GeneralData.SINGLEUSER;
			internalPharmacies = GeneralData.INTERNALPHARMACIES;
			debug = GeneralData.DEBUG;
			if (debug) {
				logger.info("Debug: OpenHospital in debug mode.");
			}
			flag_Xmpp = GeneralData.XMPPMODULEENABLED;
			flag_Sms = GeneralData.SMSENABLED;
			// start connection with SMS service
//			if (flag_Sms) {
//				Thread thread = new Thread(new SmsSender());
//				thread.start();
//			}
		} catch (Exception e) {
			singleUser = true; // default for property not found
			internalPharmacies = false; // default for property not found
			debug = false; // default for property not found
		}

		if (singleUser) {
			logger.info("Logging: Single User mode.");
			myUser = new User("admin", "admin", "admin", "");
			MDC.put("OHUser", myUser.getUserName());
			MDC.put("OHUserGroup", myUser.getUserGroupName());
		} else {
			// get an user
			logger.info("Logging: Multi User mode.");
			new Login(this);

			if (myUser == null) {
				// Login failed
				actionExit(2);
			}
		}

		// get menu items
		UserBrowsingManager manager = new UserBrowsingManager();
		myMenu = manager.getMenu(myUser);
//asd
		// start connection with xmpp server if is enabled
//		if (flag_Xmpp) {
//			try {
//				Server.getInstance().login(myUser.getUserName(), myUser.getPasswd());
//				try {
//					Thread.sleep(500);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//				new CommunicationFrame();
//				/*
//				 * Interaction communication= new Interaction();
//				 * communication.incomingChat(); communication.receiveFile();
//				 */
//			} catch (XMPPException e) {
//				String message = e.getMessage();
//				if (message.contains("SASL authentication DIGEST-MD5 failed")) {
//					if (myUser.getUserName().equals("admin")) {
//						logger.error("Cannot use \"admin\" user, please consider to create another user under the admin group");
//					} else {
//						logger.error("Passwords not matching, please drop XMPP user and login OH again with the same user");
//					}
//				} else if (message.contains("XMPPError connecting")) {
//					logger.error("No XMPP Server seems to be running: set XMPPMODULEENABLED = false");
//				} else {
//					logger.error("An error occurs: " + e.getMessage());
//				}
//				flag_Xmpp = GeneralData.XMPPMODULEENABLED = false;
//			}
//
//		}
//qwe
		// if in singleUser mode remove "users" and "communication" menu
		if (singleUser) {
			ArrayList<UserMenuItem> junkMenu = new ArrayList<UserMenuItem>();
			Iterator<UserMenuItem> it = myMenu.iterator();
			while (it.hasNext()) {
				UserMenuItem umi = it.next();
				if (umi.getCode().equalsIgnoreCase("USERS") || umi.getMySubmenu().equalsIgnoreCase("USERS"))
					junkMenu.add(umi);
				if (umi.getCode().equalsIgnoreCase("communication")) {
					if (flag_Xmpp) {
						logger.info("Single user mode: set XMPPMODULEENABLED = false");
						flag_Xmpp = GeneralData.XMPPMODULEENABLED = false;
					}
					junkMenu.add(umi);
				}
			}
			Iterator<UserMenuItem> altIt = junkMenu.iterator();
			while (altIt.hasNext()) {
				UserMenuItem umi = altIt.next();
				if (myMenu.contains(umi))
					myMenu.remove(umi);
			}
		} else { // remove only "communication" if flag_Xmpp = false
			if (!flag_Xmpp) {
				ArrayList<UserMenuItem> junkMenu = new ArrayList<UserMenuItem>();
				Iterator<UserMenuItem> it = myMenu.iterator();
				while (it.hasNext()) {
					UserMenuItem umi = it.next();
					if (umi.getCode().equalsIgnoreCase("communication"))
						junkMenu.add(umi);
				}
				Iterator<UserMenuItem> altIt = junkMenu.iterator();
				while (altIt.hasNext()) {
					UserMenuItem umi = altIt.next();
					if (myMenu.contains(umi))
						myMenu.remove(umi);
				}
			}
		}

		// if not internalPharmacies mode remove "medicalsward" menu
		if (!internalPharmacies) {
			ArrayList<UserMenuItem> junkMenu = new ArrayList<UserMenuItem>();
			Iterator<UserMenuItem> it = myMenu.iterator();
			while (it.hasNext()) {
				UserMenuItem umi = it.next();
				if (umi.getCode().equalsIgnoreCase("MEDICALSWARD") || umi.getMySubmenu().equalsIgnoreCase("MEDICALSWARD"))
					junkMenu.add(umi);
			}
			Iterator<UserMenuItem> altIt = junkMenu.iterator();
			while (altIt.hasNext()) {
				UserMenuItem umi = altIt.next();
				if (myMenu.contains(umi))
					myMenu.remove(umi);
			}
		}

		// remove disabled buttons
		ArrayList<UserMenuItem> junkMenu = new ArrayList<UserMenuItem>();
		Iterator<UserMenuItem> it = myMenu.iterator();
		while (it.hasNext()) {
			UserMenuItem umi = it.next();
			if (!umi.isActive())
				junkMenu.add(umi);
		}
		Iterator<UserMenuItem> altIt = junkMenu.iterator();
		while (altIt.hasNext()) {
			UserMenuItem umi = altIt.next();
			if (myMenu.contains(umi))
				myMenu.remove(umi);
		}

		UI.getCurrent().getPage().setTitle(myUser.getUserName());


		ThemeResource resource = new ThemeResource("img/LogoMenu.jpg");
		Image image = new Image(null,resource);
		this.layout0 = new HorizontalLayout();
		this.layout = new VerticalLayout();
		this.layout.setSizeUndefined();
		image.setHeight(100,Unit.PERCENTAGE);
		this.layout0.addComponent(image);
		this.layout0.addComponent(this.layout);
		// add panel with buttons to frame
		MainPanel panel = new MainPanel(this);
		List<Component> qc = panel.getComponent();
		for(Component w:qc){
            this.layout.addComponent(w);
        }
		// compute menu position
		// Toolkit kit = Toolkit.getDefaultToolkit();
		// Dimension screenSize = kit.getScreenSize();
		// int screenHeight = screenSize.height;

		HorizontalLayout layout = getLayout();
		UI.getCurrent().setContent(layout);
	}

	public HorizontalLayout getLayout(){
		return this.layout0;
	}

	private void actionExit(int status) {
		if (status == 2)
			logger.info("Login failed.");
		logger.info("\n=====================\n OpenHospital closed \n=====================\n");
		System.exit(status);
	}

	/*
	 * 
	 */
	public void actionPerformed(Button.ClickEvent e) {
		String command = e.getButton().getIconAlternateText();
		launchApp(command);
        // this.layout.addComponent(new Label("Thanks " + e.getButton().getCaption() + ", it works!"));
	}

	/**
	 * 
	 * @param itemMenuCode
	 */
	private void launchApp(String itemMenuCode) {
		for (UserMenuItem u : myMenu) {
			if (u.getCode().equals(itemMenuCode)) {
				if (u.getCode().equalsIgnoreCase("EXIT")) {
					actionExit(0);
				} else if (u.isASubMenu()) {
					new SubMenu(this, u.getCode(), myMenu);
					break;
				} else {
					String app = u.getMyClass();
					// an empty menu item
					if (app.equalsIgnoreCase("none"))
						return;
					try{
						Object target = Class.forName(app).newInstance();
						try {
							((ModalWindow) target).showAsModal();
						} catch (ClassCastException noModalJFrame) {
							// try {
							// 	((JFrame) target).setEnabled(true);
							// } catch (ClassCastException noJFrame) {
							// 	((JDialog) target).setEnabled(true);
							// }
							// logger.info("ClassCastException noModalJFrame");
						}
					} catch (InstantiationException ie) {
						logger.info("InstantiationException");
					} catch (IllegalAccessException iae) {
						logger.info("IllegalAccessException");
					} catch (ClassNotFoundException cnfe) {
						logger.info("ClassNotFoundException");
					}
					break;
				}
			}
		}
	}

	private class MainPanel// extends JPanel
	{
		private static final long serialVersionUID = 4338749100837551874L;

		private Button button[];
		private MainMenu parentFrame = null;
		private List<Component> listComponent = new ArrayList<Component>();

		public MainPanel(MainMenu parentFrame) {
			this.parentFrame = parentFrame;
			int numItems = 0;

			for (UserMenuItem u : myMenu)
				if (u.getMySubmenu().equals("main"))
					numItems++;

			button = new Button[numItems];

			int k = 1;

			for (UserMenuItem u : myMenu)
				if (u.getMySubmenu().equals("main")) {
					button[k - 1] = new Button(u.getButtonLabel());

					button[k - 1].setClickShortcut(KeyEvent.VK_A + (int) (u.getShortcut() - 'A'));

					button[k - 1].addClickListener(parentFrame::actionPerformed);
					button[k - 1].setIconAlternateText(u.getCode());
					this.listComponent.add(button[k-1]);
					k++;
				}

			setButtonsSize(button);
		}

		private List<Component> getComponent(){
			return this.listComponent;
		}

		private void setButtonsSize(Button button[]) {
			int max = 0;
			int ii=0;
			// for (int i = 0; i < button.length; i++) {
			// 	max = Math.max(max, button[i].getWidth());\
			// }
			for (int i = 0; i < button.length; i++) {
				if(button[i].getCaption().length()>max){
					max = button[i].getCaption().length();
					ii = i;
				}
				button[i].setWidth("100%");
				// parentFrame.layout.setExpandRatio(button[i], 1.0f);
			}
			button[ii].setWidthUndefined();
		}
	}// :~MainPanel
}// :~MainMenu
