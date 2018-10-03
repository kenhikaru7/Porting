package org.isf.utils.jobjects;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.File;
import com.vaadin.server.FileResource;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Window;
/**
 * @author Santhosh Kumar T - santhosh@in.fiorano.com
 * 
 */
public class ModalWindow extends Window {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected final Window window = this;
	
	String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
	FileResource resource = new FileResource(new File(basepath +"/WEB-INF/icons/oh.png"));

	/**
	 * method to enable/disable a owner JFrame launching this ModalJFrame
	 * @param owner - the JFrame owner
	 */
	public void showAsModal() {
		
		setIcon(resource);
		setModal(true);
		// this.addWindowListener(new WindowAdapter() {
		// 	public void windowOpened(WindowEvent e) {
		// 		owner.setEnabled(false);
		// 	}

		// 	public void windowClosing(WindowEvent e) {
		// 		owner.setEnabled(true);
		// 		owner.toFront();
		// 		window.removeWindowListener(this);
		// 	}

		// 	public void windowClosed(WindowEvent e) {
		// 		owner.setEnabled(true);
		// 		owner.toFront();
		// 		window.removeWindowListener(this);
		// 	}
		// });
		
		// owner.addWindowListener(new WindowAdapter() {
		// 	public void windowActivated(WindowEvent e) {
		// 		if (window.isShowing()) {
		// 			window.setExtendedState(JFrame.NORMAL);
		// 			window.toFront();
		// 		} else {
		// 			owner.removeWindowListener(this);
		// 		}
		// 	}
		// });
		
		// window.setVisible(true);
	}
}