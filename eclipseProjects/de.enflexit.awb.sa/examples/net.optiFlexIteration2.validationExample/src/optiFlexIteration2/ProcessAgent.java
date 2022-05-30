package optiFlexIteration2;

import java.io.File;
import java.util.Set;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import agentgui.core.application.Application;
import de.enflexit.awb.sa.core.KnowledgeBase;
import de.enflexit.awb.sa.core.OwlMessageReceiveBehaviour;
import de.enflexit.awb.sa.core.SendInformBehaviour;
import de.enflexit.awb.sa.core.SendQueryBehaviour;
import de.enflexit.awb.sa.core.UtilityMethods;
import jade.core.AID;
import jade.core.Agent;


/**
 * @author Sebastian Törsleff, Helmut Schmidt University;
 */
public class ProcessAgent extends Agent {


	// --- static variables -----------------------------
	private static final long serialVersionUID = 3164333512327730902L;
	private static Logger rootLogger = Logger.getRootLogger();
	
	
	// --- conversation ID variables 
	private String cidBase;
	protected static int cidCnt = 0;
	
	@Override
	protected void setup() {
		

		
		// --- Logger configuration --------------------------------------
		rootLogger.removeAllAppenders(); //unschöner workaround. sollte besser an zentraler Stelle erfolgen und nicht bei jedem Agenten individuell
		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		rootLogger.addAppender(consoleAppender);
		rootLogger.setLevel(Level.DEBUG);	
		
		// Timeblocker 2s for setting up JADE sniffer
		try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
		
	}
}
