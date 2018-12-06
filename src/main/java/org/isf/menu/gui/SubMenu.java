package org.isf.menu.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import org.isf.menu.model.*;
import org.isf.utils.Logging;

import javax.swing.*;
import javax.swing.event.EventListenerList;

import java.util.*;

import com.vaadin.ui.Window;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.Button;

public class SubMenu extends Window{
	private static final long serialVersionUID = 7620582079916035164L;
	
	
	private List<CommandListener> commandListeners = new ArrayList<CommandListener>();

    public interface CommandListener{
        public void commandInserted(String aCommand);
    }

    public void addCommandListener(CommandListener listener) {
    	commandListeners.add(listener);
    }

    public void removeCommandListener(CommandListener listener) {
    	commandListeners.remove(listener);
    }

    private void fireCommandInserted(String aCommand) {
        for (CommandListener commandListener : commandListeners)
            commandListener.commandInserted(aCommand);
    }
		
	
	private ArrayList<UserMenuItem> myMenu;
	private MainMenu mainMenu;
	private VerticalLayout subContent;
	Logging logger;
	
	private int prfButtonSize=0;
	public int getMinButtonSize(){
		return prfButtonSize;
	}
	
	
	public SubMenu(SubMenu parent, String code, ArrayList<UserMenuItem> menu, MainMenu mainMenu) {
		// super(parent, "     ", true);
		this.prfButtonSize=parent.getMinButtonSize();
		// initialize(mainMenu, code, menu, parent.getBounds());
	}
	
	public SubMenu(MainMenu mainMenu,String code, ArrayList<UserMenuItem> menu) {
		// super(parent, "     ", true);
		setCaption(code);
		setModal(true);
		this.subContent = new VerticalLayout();
        setContent(this.subContent);
        UI.getCurrent().addWindow(this);
		// this.prfButtonSize=parent.getMinButtonSize();
		initialize(mainMenu, code, menu, null);
	}
	
	
	private void initialize(MainMenu mainMenu ,String code, ArrayList<UserMenuItem> menu, String RectangleparentBounds){
		logger = new Logging();
		final int displacement = 50;
		
		addCommandListener(mainMenu);
		// q
		myMenu = menu;
		
		// add panel to frame
		SubPanel panel = new SubPanel(this,code);
		// add(panel);

		// // submenu leggermente spostato rispetto a menu
		// Rectangle r = parentBounds;
		// r.width = getBounds().width;
		// r.height = getBounds().height;
		// r.x += displacement;
		// r.y -= displacement;

		// setBounds(r);

		// setResizable(false);
		// pack();
		// setVisible(true);
	}
	
	private class SubPanel{

		private static final long serialVersionUID = 4338749100837551874L;

		private Button button[];
		private String title;
		
		public SubPanel(SubMenu dialogFrame, String subName) {

			int numItems = 0;
			
			for(UserMenuItem u : myMenu){
				if (u.getMySubmenu().equals(subName)) numItems++;
				if (u.getCode().equalsIgnoreCase(subName)) title=u.getButtonLabel();
			}	
				
			//System.out.println(numItems);
			
			button = new Button[numItems];

			int k=1;
			
			for(UserMenuItem u : myMenu)
				if (u.getMySubmenu().equals(subName)){
					button[k-1]= new Button(u.getButtonLabel());
					button[k-1].setClickShortcut(KeyEvent.VK_A	+ (int)(u.getShortcut() - 'A'));
					button[k-1].setIconAlternateText(u.getCode());
					if (!u.isActive())
						button[k-1].setEnabled(false);
					else 
						button[k-1].addClickListener(e->{
							String command = e.getButton().getIconAlternateText();
							for(UserMenuItem menu : myMenu){
								if (menu.getCode().equals(command)){
									if (menu.isASubMenu()){
										close();
										// new SubMenu(menu.getCode(), myMenu, mainMenu );					
										break;
									}
									else {
										close();
										fireCommandInserted(menu.getCode());					
										break;
									}
								}
							}
							
						}); 
					k++;
				}

			setButtonsSize(button);

			//setBackground(Color.WHITE);
			// GridBagLayout layout = new GridBagLayout();
			// setLayout(layout);
			// JPanel p = new JPanel();
			// JLabel l = new JLabel(title);
			// l.setFont(new Font("SansSerif",Font.BOLD,12));
			// p.add(l);
			// p.setPreferredSize(new Dimension(prfButtonSize,p.getPreferredSize().height));
			//p.setBackground(Color.WHITE);
			// p.setBorder(new javax.swing.border.LineBorder(Color.lightGray));
			
			final int insetsValue = 5;

			// add(p,new GBC(0, 0).setInsets(insetsValue));
						
			for (int i = 0; i < button.length; i++) {
				dialogFrame.subContent.addComponent(button[i]);
			}
			
		}
		
		private void setButtonsSize(Button button[]) {
			int maxH = 0;
			int maxMax = 0;
			int maxMin = 0;
			int maxPrf = 0;

			// for (int i = 0; i < button.length; i++) {
			// 	maxH = Math.max(maxH, button[i].getMaximumSize().height);
			// 	maxMax = Math.max(maxMax, button[i].getMaximumSize().width);
			// 	maxMin = Math.max(maxMin, button[i].getMinimumSize().width);
			// 	maxPrf = Math.max(maxPrf, button[i].getPreferredSize().width);
			// }
			// maxPrf = Math.max(maxPrf, prfButtonSize);
			
			// for (int i = 0; i < button.length; i++) {
			// 	button[i].setMaximumSize(new Dimension(maxMax, maxH));
			// 	button[i].setMinimumSize(new Dimension(maxMin, maxH));
			// 	button[i].setPreferredSize(new Dimension(maxPrf, maxH));
			// }
		}
	}
}
