import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.io.*;

//TODO: Zoom, moving red nxn grid
public class MatrixBallGUI extends JPanel{
	private JFrame mainFrame;
	
	private Integer n = 10;
	private Integer maxInterval = 2;
	//private ePerm window = new ePerm(n, maxInterval);
	private ePerm window = new ePerm("[6, 1, 14, 3, 18, 19, 12, 15, 17, 10]");
	private Integer numX;
	private Integer numY;
	private Integer buffX = 21; // Have a 21 pixel white space for putting in numbers (will be replaced by calculation later)
	private Integer buffY = 21; // Have a 21 pixel white space for putting in numbers
	private Integer boxSizeX = 26;
	private Integer boxSizeY = 26;
	private Integer redGridX = 1;
	private Integer redGridY = 1;	
	MatrixBallPicture picture;
	
	// Southwest river data
	Set<Integer> southwestRiver;
	ArrayList<Integer> riverNumberingWindow;

	// Viewer position data
	private final int FR_DEFAULT = 1; // Default first row
	private final int FC_DEFAULT = 1; // Default first column
	private Integer fr = FR_DEFAULT; // First row to display
	private Integer fc = FC_DEFAULT; // First column to display
	
	// Mode related data
	private final int SW_RIVER_MODE = 0;
	private final int HOR_CUTOFF_MODE = 1;
	private Integer mode = SW_RIVER_MODE;

	// RSK data
	private final int NORM_RSK_MODE = 0;
	private final int MID_RSK_MODE_F = 1;
	private final int MID_RSK_MODE_B = 2;	
	private Integer RSKMode = NORM_RSK_MODE;
	private Tabloid P; 
	private Tabloid Q; 
	private ArrayList<Integer> R; 
	
	// Interface components
	JTextField nInput;
	JTextField maxIntervalInput;
	JTextField permutationInput;
	JButton buttonBB;
	JButton buttonB;
	JButton buttonF;
	JButton buttonFF;	
	JRadioButton modeSWRiver;
	JRadioButton modeHorCutoff;
	JTextArea RSKOutputP = new JTextArea(1,1);
	JTextArea RSKOutputQ = new JTextArea(1,1);
	JTextArea RSKOutputR = new JTextArea(1,1);
	
	// File chooser
	// private JFileChooser fileChooser = new JFileChooser();
	//private FileDialog loadDialog;
	//private FileDialog saveDialog;
	
	//--------------------------------------------------------------------------------------------------------------------------------
	//Constructor
	public MatrixBallGUI() {
		//-------------------------
		//Initialize data
		//-------------------------
		// If we are in Southwest river mode, we need to initialize the SW river data
		if(mode == SW_RIVER_MODE){
			southwestRiver = window.southwestRiver();
			riverNumberingWindow = window.southwestRiverNumbering();
		}
		picture = new MatrixBallPicture();
		P = new Tabloid(n);
		Q = new Tabloid(n);
		R = new ArrayList<Integer>();
			
		//----------------------------
		// Build interface
		//----------------------------
		// Build control panel 1
		JPanel controlPanel = new JPanel();
		controlPanel.setBorder(BorderFactory.createEtchedBorder());
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
		// Panel to deal with size of permutations
		JPanel nPanel = new JPanel();
		nInput = new JTextField(Integer.toString(n), 2);	    
		nPanel.add(new JLabel("n = "));
		nPanel.add(nInput);
		nInput.addActionListener(new nHandler());
		// Panel to deal with how far randomness is allowed
		JPanel maxIntervalPanel = new JPanel();
		maxIntervalInput = new JTextField(Integer.toString(maxInterval), 2);	    
		maxIntervalPanel.add(new JLabel("Furthest randomness = n*"));
		maxIntervalPanel.add(maxIntervalInput);
		maxIntervalInput.addActionListener(new maxIntervalHandler());
		// Panel allowing to choose a permutation
		JPanel permutationPanel = new JPanel();
		permutationInput = new JTextField(window.toString(), 20);	    
		permutationPanel.add(permutationInput);
		permutationInput.addActionListener(new permHandler());
		// Panel for exit button
		JPanel exitPanel = new JPanel();
		JButton buttonExit = new JButton("Exit");
		buttonExit.addActionListener(new ButtonExitHandler());
		exitPanel.add(buttonExit);
		// Assembling control panel 1
		controlPanel.add(nPanel);
		controlPanel.add(maxIntervalPanel);
		controlPanel.add(permutationPanel);
		controlPanel.add(exitPanel);
		
		// Build control panel 2
		// Radio button for numbering mode switching
		modeChoiceHandler mcHandler = new modeChoiceHandler();
		modeSWRiver = new JRadioButton("Southwest river");
		modeSWRiver.setActionCommand("SWR");
		modeSWRiver.addActionListener(mcHandler);
		modeSWRiver.setSelected(true);
		modeHorCutoff = new JRadioButton("Horizontal line cutoff");
		modeHorCutoff.setActionCommand("HC");
		modeHorCutoff.addActionListener(mcHandler);
		JPanel modeChoicePanel = new JPanel();
		modeChoicePanel.setLayout(new BoxLayout(modeChoicePanel, BoxLayout.Y_AXIS));
		modeChoicePanel.add(modeSWRiver);
		modeChoicePanel.add(modeHorCutoff);
		ButtonGroup modeChoice = new ButtonGroup();
		modeChoice.add(modeSWRiver);
		modeChoice.add(modeHorCutoff);
		// Panel for RSK buttons
		JPanel RSKPanel = new JPanel();
		ButtonsRSKHandler RSKBts = new ButtonsRSKHandler();
		buttonBB = new JButton("<<");
		buttonBB.setActionCommand("BB");
		buttonBB.addActionListener(RSKBts);
		buttonBB.setEnabled(false);
		buttonB = new JButton("<");
		buttonB.setActionCommand("B");
		buttonB.addActionListener(RSKBts);
		buttonB.setEnabled(false);
		buttonF = new JButton(">");
		buttonF.setActionCommand("F");
		buttonF.addActionListener(RSKBts);
		buttonFF = new JButton(">>");
		buttonFF.setActionCommand("FF");
		buttonFF.addActionListener(RSKBts);
		RSKPanel.add(buttonBB);
		RSKPanel.add(buttonB);
		RSKPanel.add(new JLabel("AMBC"));
		RSKPanel.add(buttonF);
		RSKPanel.add(buttonFF);
		// RSK display window
		JPanel RSKOutputPanel = new JPanel();
		RSKInputWindowHandler RSKWindowListener = new RSKInputWindowHandler();
		RSKOutputPanel.add(RSKOutputP);
		RSKOutputPanel.add(Box.createHorizontalStrut(15));
		RSKOutputPanel.add(RSKOutputQ);
		RSKOutputPanel.add(Box.createHorizontalStrut(15));
		RSKOutputPanel.add(RSKOutputR);
		RSKOutputP.addKeyListener(RSKWindowListener);
		RSKOutputQ.addKeyListener(RSKWindowListener);
		RSKOutputR.addKeyListener(RSKWindowListener);
		// Documentation string
		JPanel docPanel = new JPanel();
		JLabel docString = new JLabel("<html>F -- Fast forward AMBC<br>B -- Fast backward AMBC<br>R -- Random permutation</html>");
		docPanel.add(docString);
		
		// Assembling control panel 2
		JPanel controlPanel2 = new JPanel();
		controlPanel2.setBorder(BorderFactory.createEtchedBorder());
		controlPanel2.setLayout(new BoxLayout(controlPanel2, BoxLayout.Y_AXIS));
		//controlPanel2.add(modeChoicePanel);
		controlPanel2.add(RSKPanel);
		controlPanel2.add(RSKOutputPanel);
		//controlPanel2.add(RSKInputPanel);
		controlPanel2.add(docPanel);
		// Keystrokes
		Action moveRight = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				fc = fc+1;
				picture.repaint();
			}
		};
		Action moveLeft = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				fc = fc-1;
				picture.repaint();
			}
		};
		Action moveUp = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				if(mode==HOR_CUTOFF_MODE){
					if(fr>0){
						fr = fr-1;
						picture.repaint();
					}
				}
				else if(mode==SW_RIVER_MODE){
					fr = fr-1;
					picture.repaint();
				}
			}
		};
		Action moveDown = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				fr = fr+1;
				picture.repaint();
			}
		};
		Action redRight = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				redGridX++;
				picture.repaint();
			}
		};
		Action redLeft = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				redGridX--;
				picture.repaint();
			}
		};
		Action redUp = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				redGridY--;
				picture.repaint();
			}
		};
		Action redDown = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				redGridY++;
				picture.repaint();
			}
		};


		Action RSKFastForward = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				buttonFF.doClick();
			}
		};
		Action RSKFastBackward = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				buttonBB.doClick();
			}
		};
		Action randomPerm = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				window = new ePerm(n, maxInterval);
				resetToDefaults();
				permutationInput.setText(window.toString());
			}
		};
		
		picture.getInputMap().put(KeyStroke.getKeyStroke("RIGHT"), "moveRight");
		picture.getInputMap().put(KeyStroke.getKeyStroke("LEFT"), "moveLeft");
		picture.getInputMap().put(KeyStroke.getKeyStroke("UP"), "moveUp");
		picture.getInputMap().put(KeyStroke.getKeyStroke("DOWN"), "moveDown");
		picture.getInputMap().put(KeyStroke.getKeyStroke("shift RIGHT"), "redRight");
		picture.getInputMap().put(KeyStroke.getKeyStroke("shift LEFT"), "redLeft");
		picture.getInputMap().put(KeyStroke.getKeyStroke("shift UP"), "redUp");
		picture.getInputMap().put(KeyStroke.getKeyStroke("shift DOWN"), "redDown");
		picture.getInputMap().put(KeyStroke.getKeyStroke("F"), "RSKFastForward");
		picture.getInputMap().put(KeyStroke.getKeyStroke("B"), "RSKFastBackward");
		picture.getInputMap().put(KeyStroke.getKeyStroke("R"), "randomPerm");
		picture.getActionMap().put("moveRight", moveRight);
		picture.getActionMap().put("moveLeft", moveLeft);
		picture.getActionMap().put("moveUp", moveUp);
		picture.getActionMap().put("moveDown", moveDown);
		picture.getActionMap().put("redRight", redRight);
		picture.getActionMap().put("redLeft", redLeft);
		picture.getActionMap().put("redUp", redUp);
		picture.getActionMap().put("redDown", redDown);
		picture.getActionMap().put("RSKFastForward", RSKFastForward);
		picture.getActionMap().put("RSKFastBackward", RSKFastBackward);
		picture.getActionMap().put("randomPerm", randomPerm);
		
		// General assembly
		setLayout(new BorderLayout());
		add(picture, BorderLayout.CENTER);
		add(controlPanel, BorderLayout.SOUTH);
		add(controlPanel2, BorderLayout.EAST);
		
	}
		
	//------------------------------------------------------------------------------------------------------------------------------
	// Classes for interface components
	private class nHandler implements ActionListener {
		public void actionPerformed(ActionEvent e){
				try {
					String tmp = nInput.getText();
					if(Integer.parseInt(tmp) < 0) {nInput.setText(Integer.toString(n)); nInput.requestFocus(); return;}
					n = Integer.parseInt(tmp);
					window = new ePerm(n, maxInterval);
					resetToDefaults();
					permutationInput.setText(window.toString());					
				}
				catch (NumberFormatException ex) {
					nInput.setText(Integer.toString(n));
					nInput.requestFocus();
					return;
				}
			}
	}
	private class maxIntervalHandler implements ActionListener {
			public void actionPerformed(ActionEvent e){
				try {
					String tmp = maxIntervalInput.getText();
					if(Integer.parseInt(tmp) < 0) {maxIntervalInput.setText(Integer.toString(maxInterval)); maxIntervalInput.requestFocus(); return;}
					maxInterval = Integer.parseInt(tmp);
					picture.requestFocus();
				}
				catch (NumberFormatException ex) {
					maxIntervalInput.setText(Integer.toString(maxInterval));
					maxIntervalInput.requestFocus();
					return;
				}
			}
		}
	private class permHandler implements ActionListener {
			public void actionPerformed(ActionEvent e){
				try {
					String tmp = permutationInput.getText();
					window = new ePerm(tmp);
					n = window.getSize();
					nInput.setText(Integer.toString(n));
					resetToDefaults();
				}
				catch (NumberFormatException ex) {
					return;
				}
			}
		}
	
	private class modeChoiceHandler implements ActionListener {
			public void actionPerformed(ActionEvent e){
				if(e.getActionCommand() == "SWR"){
					southwestRiver = window.southwestRiver();
					riverNumberingWindow = window.southwestRiverNumbering();
					mode = SW_RIVER_MODE;
					
				}
				else if(e.getActionCommand() == "HC"){
					fc = FC_DEFAULT;
					fr = FR_DEFAULT;
					mode = HOR_CUTOFF_MODE;
				}
				picture.repaint();
				picture.requestFocus();
			}
		}
	private class ButtonExitHandler implements ActionListener {
		public void actionPerformed(ActionEvent e){
			System.exit(0);
		}
	}
	private class ButtonsRSKHandler implements ActionListener {
		public void actionPerformed(ActionEvent e){
			String act = e.getActionCommand();
			if(act == "F"){
				if(RSKMode == MID_RSK_MODE_F){
					RSKMode = NORM_RSK_MODE;
					Row nextRow = window.forwardRSKStep();
					P.addRow(nextRow.r1);
					Q.addRow(nextRow.r2);
					R.add(nextRow.r);
					RSKOutputP.setText(P.toString());
					RSKOutputQ.setText(Q.toString());
					RSKOutputR.setText(colToString(R));
					if(mode == SW_RIVER_MODE){
						southwestRiver = window.southwestRiver();
						riverNumberingWindow = window.southwestRiverNumbering();
					}
					buttonB.setEnabled(true);
					buttonBB.setEnabled(true);
					if(window.isEmpty()){
						buttonF.setEnabled(false);
						buttonFF.setEnabled(false);
					}
				}
				else if(RSKMode == MID_RSK_MODE_B){
					RSKMode = NORM_RSK_MODE;
					if(window.isEmpty()){
						buttonF.setEnabled(false);
						buttonFF.setEnabled(false);
					}
				}
				else{
					RSKMode = MID_RSK_MODE_F;
					buttonB.setEnabled(true);
					buttonBB.setEnabled(true);
				}
			}
			else if(act == "B"){
				if(RSKMode == MID_RSK_MODE_B){
					RSKMode = NORM_RSK_MODE;
					window.backwardRSKStep();
					P.removeLastRow();
					Q.removeLastRow();
					R.remove(R.size()-1);
					RSKOutputP.setText(P.toString());
					RSKOutputQ.setText(Q.toString());
					RSKOutputR.setText(colToString(R));
					buttonF.setEnabled(true);
					buttonFF.setEnabled(true);
					if(window.isFull()){
						buttonB.setEnabled(false);
						buttonBB.setEnabled(false);
					}
				}
				else if(RSKMode == MID_RSK_MODE_F){
					RSKMode = NORM_RSK_MODE;
					if(window.isFull()){
						buttonB.setEnabled(false);
						buttonBB.setEnabled(false);
					}
				}
				else{
					RSKMode = MID_RSK_MODE_B;
					window.backwardRSKSetup(new Row(P.getRow(P.length()-1), Q.getRow(Q.length()-1), R.get(R.size()-1)));
					buttonF.setEnabled(true);
					buttonFF.setEnabled(true);
				}
			}
			else if(act == "FF"){
				Row nextRow;
				while(!window.isEmpty()){
					nextRow = window.forwardRSKStep();
					P.addRow(nextRow.r1);
					Q.addRow(nextRow.r2);
					R.add(nextRow.r);
				}
				RSKOutputP.setText(P.toString());
				RSKOutputQ.setText(Q.toString());
				RSKOutputR.setText(colToString(R));
				buttonF.setEnabled(false);
				buttonFF.setEnabled(false);
				buttonB.setEnabled(true);
				buttonBB.setEnabled(true);
				RSKMode = NORM_RSK_MODE;
			}
			else if(act == "BB"){
				while(!window.isFull()){
					window.backwardRSKSetup(new Row(P.removeLastRow(), Q.removeLastRow(), R.remove(R.size()-1)));
					window.backwardRSKStep();
				}
				buttonF.setEnabled(true);
				buttonFF.setEnabled(true);
				buttonB.setEnabled(false);
				buttonBB.setEnabled(false);
				RSKMode = NORM_RSK_MODE;
				RSKOutputP.setText(P.toString());
				RSKOutputQ.setText(Q.toString());
				RSKOutputR.setText(colToString(R));
			}
			picture.repaint();
			picture.requestFocus();
		}
	}	
	
	private class RSKInputWindowHandler implements KeyListener {
		public void keyReleased(KeyEvent e){}
		public void keyTyped(KeyEvent e){}
		public void keyPressed(KeyEvent e){
			
			if(e.getKeyCode() == KeyEvent.VK_ENTER)
			{
				if(e.isShiftDown()){
					JTextArea src = (JTextArea)(e.getSource());
					src.insert("\n",src.getCaretPosition());
					return;
				}
				e.consume();
			
				if(e.getSource() == RSKOutputP){
					P = new Tabloid(RSKOutputP.getText());
					RSKOutputP.setText(P.toString());
				}
				else if(e.getSource() == RSKOutputQ){
					Q = new Tabloid(RSKOutputQ.getText());
					RSKOutputQ.setText(Q.toString());
				}
				else if(e.getSource() == RSKOutputR){
					R = parseCol(RSKOutputR.getText());
					RSKOutputR.setText(colToString(R));
				}
				n = P.size();
				window.n = n;
				window.clear();				
				nInput.setText(n.toString());
				permutationInput.setText("");
				fc = FC_DEFAULT;
				fr = FR_DEFAULT;
				buttonF.setEnabled(false);
				buttonFF.setEnabled(false);
				buttonB.setEnabled(true);
				buttonBB.setEnabled(true);

				picture.repaint();
				picture.requestFocus();
			}
		}
		
	}
	
	// Define class for window containing graph & method for redrawing
	private class MatrixBallPicture extends JPanel{
		public void paintComponent(Graphics g){
			Graphics2D g2 = (Graphics2D) g;
			int width = getWidth();
			int height = getHeight();
			setBackground(Color.white);
			super.paintComponent(g2);
			
			// Figure out how many boxes are there
			numX = (width-buffX)/boxSizeX;
			numY = (height-buffY)/boxSizeY;
			// Draw the grid
			for(Integer i=0; i < numX+1; i++){
				g2.drawLine(buffX+boxSizeX*i, 0, buffX+boxSizeX*i, height);
				g2.drawString(Integer.toString(fc+i), 3+buffX+boxSizeX*i, 11);
			}
			for(int i=0; i < numY+1; i++){
				g2.drawLine(0,buffY+boxSizeY*i, width, buffY+boxSizeY*i);
				g2.drawString(Integer.toString(fr+i), 0, buffX+boxSizeX*i+3*boxSizeY/4);
			}
			g2.setStroke(new BasicStroke(3));
			g2.setColor(Color.red);
			//g2.drawLine(buffX,buffY-(fr)*boxSizeY, width, buffY-(fr)*boxSizeY);					
			//g2.drawLine(buffX-(fc)*boxSizeX,buffY , buffX-(fc)*boxSizeX, height);					
			g2.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] {boxSizeX/2}, 0));
			for(int i=fr-1; i < fr+numY; i++){
				if(modn(i) == modn(redGridY))
					g2.drawLine(buffX,buffY+boxSizeY*(i-fr), width, buffY+boxSizeY*(i-fr));					
			}
			for(int i=fc-1; i < fc+numX; i++){
				if(modn(i) == modn(redGridX))
					g2.drawLine(buffX+boxSizeX*(i-fc), buffY, buffX+boxSizeX*(i-fc), height);					
			}
			g2.setStroke(new BasicStroke(4));
			g2.setColor(Color.black);
			g2.drawLine(buffX,buffY, width, buffY);					
			g2.drawLine(buffX,buffY, buffX, height);					
			// Create list of Balls
			// Compute ball labels
			class Ball{
				Integer f; // "From"; the row of the ball
				Integer t; // "To"; the column of the ball
				Integer label = 0; // Label as in the numbering
				Color c = Color.black; // The color of the label; will be red for rivers
			}
			ArrayList<Ball> balls = new ArrayList<Ball>();
			if(mode == HOR_CUTOFF_MODE){
				// Have array which in i-th cell contains the minimal coordinate of ball with label i
				ArrayList<Integer> minCoord = new ArrayList<Integer>();
				minCoord.add(Integer.MIN_VALUE);
				int levelCounter = 0;
				for(int i = 0; i < fr+numY+1; i++){
					if(!window.isDefined(i))
						continue;
					Ball b = new Ball();
					b.f = i;
					b.t = window.get(i);
					levelCounter = 0;
					while(levelCounter < minCoord.size() &&  minCoord.get(levelCounter) < b.t)
						levelCounter++;
					b.label = levelCounter;
					if(levelCounter == minCoord.size()){
						minCoord.add(b.t);
					}
					else{
						minCoord.set(levelCounter, Math.min(b.t, minCoord.get(levelCounter)));
					}
					balls.add(b);
				}
			}
			else if(mode == SW_RIVER_MODE){
				for(int i = fr; i < fr + numY+1; i++){
					if(!window.isDefined(i))
						continue;
					Ball b = new Ball();
					b.f = i;
					b.t = window.get(i);
					if(window.isOnRiver(i))
						b.c = Color.red;
					b.label = window.getNumber(i);
					balls.add(b);
				}
			}
			// Draw balls
			Set<Integer> labelsPresent = new HashSet<Integer>();
			g2.setStroke(new BasicStroke(2));
			for(int i=0; i < balls.size();i++){
				Ball b = balls.get(i);
				if(b.t < fc || b.t > fc+numX+1)
					continue;
				g2.setColor(Color.black);
				g2.drawOval(buffX + (b.t-fc)*boxSizeX+1, buffY + (b.f-fr)*boxSizeY+1, boxSizeX-2, boxSizeY-2);
				g2.setColor(b.c);
				if(RSKMode == NORM_RSK_MODE)
					g2.drawString(Integer.toString(b.label), buffX + (b.t-fc)*boxSizeX+boxSizeX/5, buffY + (b.f-fr)*boxSizeY+3*boxSizeY/4);
				else if(RSKMode == MID_RSK_MODE_F){
					g2.drawString(Integer.toString(b.label), buffX + (b.t-fc)*boxSizeX+boxSizeX/5, buffY + (b.f-fr)*boxSizeY+3*boxSizeY/4);
					labelsPresent.add(b.label); // Record which labels have occurred so that mid-RSK picture can be drawn
				}
				else if(RSKMode == MID_RSK_MODE_B){
					g2.setColor(Color.blue);
					g2.drawString(Integer.toString(window.getBackNumber(b.f)), buffX + (b.t-fc)*boxSizeX+boxSizeX/5, buffY + (b.f-fr)*boxSizeY+3*boxSizeY/4);
				}
				
			}
			// If midway through RSK, draw corresponding picture
			if(RSKMode == MID_RSK_MODE_F){				
				Integer curBall;
				Integer nextBall;
				for(Integer label:labelsPresent){
					curBall = window.getFirst(label);
					// Draw the magenta vertical from from SW ball to the imaginary river spot
					if(window.get(curBall) >= fc){
						g2.setColor(Color.magenta);
						g2.drawLine(buffX + (window.get(curBall)-fc)*boxSizeX+boxSizeX/2, buffY + (curBall-fr)*boxSizeY,
								buffX + (window.getBackX(curBall)-fc)*boxSizeX + boxSizeX/2, buffY + Math.max((window.getBackY(curBall)-fr)*boxSizeY + boxSizeY/2, 0));
					}
					// Draw the green midpoints
					g2.setColor(Color.green);
					while(!window.isLast(curBall)){
						nextBall = window.getNext(curBall);
						if(curBall >= fr)
							g2.drawLine(buffX + Math.max((window.get(curBall)-fc)*boxSizeX + boxSizeX,0), buffY + (curBall-fr)*boxSizeY + boxSizeY/2, 
									buffX + Math.max((window.get(nextBall)-fc)*boxSizeX,0), buffY + (curBall-fr)*boxSizeY + boxSizeY/2);
						if(window.get(nextBall) >= fc)
							g2.drawLine(buffX + (window.get(nextBall)-fc)*boxSizeX+boxSizeX/2, buffY + Math.max((curBall-fr)*boxSizeY,0), 
									buffX + (window.get(nextBall)-fc)*boxSizeX+boxSizeX/2, buffY + Math.max((nextBall-fr)*boxSizeY+ boxSizeY,0));
						if(curBall >= fr && window.get(nextBall) >= fc)
						g2.drawOval(buffX + (window.get(nextBall)-fc)*boxSizeX+1, buffY + (curBall-fr)*boxSizeY+1, boxSizeX-2, boxSizeY-2);
						curBall = nextBall;
					}
					// Draw the magenta horizontal from from NE ball to the imaginary river spot
					if(curBall >= fr){
					g2.setColor(Color.magenta);
					g2.drawLine(buffX + (window.get(curBall)-fc)*boxSizeX, buffY + (curBall-fr)*boxSizeY + boxSizeY/2,
							buffX + Math.max((window.getBackX(curBall)-fc)*boxSizeX + boxSizeX/2,0), buffY + (window.getBackY(curBall)-fr)*boxSizeY + boxSizeY/2); 
					}
				}
			}
			if (RSKMode == MID_RSK_MODE_B){
				for(Integer i = fr; i < fr + numY+1; i++){
					if(!window.isBackRiver(i))
						continue;
					g2.setColor(Color.magenta);
					g2.drawString(Integer.toString(window.getBackRiverIndex(i)), buffX + (window.getBackRiverX(i)-fc)*boxSizeX+boxSizeX/5, buffY + (i-fr)*boxSizeY+3*boxSizeY/4);					
				}
			
			}
		}
	}
	private void resetToDefaults(){
		fc = FC_DEFAULT;
		fr = FR_DEFAULT;
		// If we are in Southwest river mode, we need to initialize the SW river data
		if(mode == SW_RIVER_MODE){
			southwestRiver = window.southwestRiver();
			riverNumberingWindow = window.southwestRiverNumbering();
		}
		RSKMode = NORM_RSK_MODE;
		buttonBB.setEnabled(false);
		buttonB.setEnabled(false);
		buttonF.setEnabled(true);
		buttonFF.setEnabled(true);
		P.clear();
		Q.clear();
		R.clear();
		RSKOutputP.setText(null);
		RSKOutputQ.setText(null);
		RSKOutputR.setText(null);
		picture.repaint();
		picture.requestFocus();

	}

	private String colToString(ArrayList<Integer> r){
		if(r.size() == 0)
			return("");
		StringBuilder ans = new StringBuilder();
		for(Integer i : r){
			ans.append(i.toString());
			ans.append("\n");
		}
		ans.deleteCharAt(ans.length()-1);
		return(ans.toString());
	}
	private ArrayList<Integer> parseCol(String s){
		List<String> l = Arrays.asList(s.split("\\s*[;\\n]\\s*"));
		ArrayList<Integer> ans = new ArrayList<Integer>();
		for(String sI : l){
			ans.add(Integer.parseInt(sI.replace("[", "").replace("]", "")));
		}
		return(ans);
	}
	//------------------------------
	// Miscellaneous private stuff
	//------------------------------
		
	// Correcting the mod n function
	private Integer modn(Integer k){
			return(((k%n)+n)%n);
		}
}