package sokoban;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultFocusManager;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class SokobanGUI extends JFrame implements ActionListener, KeyListener {
	
	private static final String LEFT = "LEFT";
	private static final String RIGHT = "RIGHT";
	private static final String UP = "UP";
	private static final String DOWN = "DOWN";
	private static final String RELOAD = "RELOAD";
	private static final String LOAD = "LOAD";
	private static final String EXIT = "EXIT";
	
	private String filename;
	private Map<Location, JLabel> levelMap;
	private Board board;
	private ImageIcon boxIcon;
	private ImageIcon boxAndStorageIcon;
	private ImageIcon playerIcon;
	private ImageIcon storageIcon;
	private ImageIcon wallIcon; 
	
	public SokobanGUI() throws IOException {
		super("Sokoban");
		this.board = new Board("level01.txt");
		this.filename = "level01.txt";
		this.levelMap = new HashMap<>();
		this.boxIcon = createImageIcon("box.png", "box");
		this.boxAndStorageIcon = createImageIcon("box_on_storage.png", "box on storage");
		this.playerIcon = createImageIcon("player.png", "player");
		this.storageIcon = createImageIcon("storage.png", "storage");
		this.wallIcon = createImageIcon("wall.png", "wall");
		
		this.setJMenuBar(this.makeMenu());
		
		this.initLevel();
	}
	
	private final void initLevel() {
		String title = "Sokoban";
		if (!this.filename.isEmpty()) {
			title += " (" + this.filename + ")";
		}
		this.setTitle(title);
		JPanel contentPanel = new JPanel();
		contentPanel.add(makeLevelPanel());
		contentPanel.add(makeButtonPanel());
		this.setContentPane(contentPanel);
		this.pack();
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.drawWalls();
		this.drawStorage();
		this.drawBoxes();
		this.drawPlayer();
		this.removeKeyListener(this);
		this.addKeyListener(this);
	}
	
	@Override
	public boolean isFocusTraversable ( ) {
		return true ;
	}

	@Override
	public void keyPressed (KeyEvent e) {
		Location loc = this.board.getPlayer().location();
		JLabel b = this.levelMap.get(loc);
		boolean moved = false;
		if(e.getKeyCode() == KeyEvent.VK_W | e.getKeyCode() == KeyEvent.VK_UP){
			moved = this.board.movePlayerUp();
		}else if(e.getKeyCode() == KeyEvent.VK_A | e.getKeyCode() == KeyEvent.VK_LEFT){
			moved = this.board.movePlayerLeft();
		}
		else if(e.getKeyCode() == KeyEvent.VK_S | e.getKeyCode() == KeyEvent.VK_DOWN){
			moved = this.board.movePlayerDown();
		}else if(e.getKeyCode() == KeyEvent.VK_D | e.getKeyCode() == KeyEvent.VK_RIGHT){
			moved = this.board.movePlayerRight();
		}
		if(e.getKeyCode() == KeyEvent.VK_R) {
			
			if (this.filename.isEmpty()) {
				this.board = new Board();
				this.initLevel();
			}
			else {
				try {
					this.board = new Board(this.filename);
					this.initLevel();
				}
				catch (IOException x) {
					JOptionPane.showMessageDialog(this, "Could not read the level file.");
				}
			}
		}
		
		if (moved) {
			b.setIcon(null);
			this.drawStorage();
			this.drawBoxes();
			this.drawPlayer();
			if (this.board.isSolved()) {
				JOptionPane.showMessageDialog(this, "You won! Click Menu in the top left to select a new level!");
			}
		}
	}

	@Override
	public void keyReleased (KeyEvent e) {
	//System.out.println(KeyEvent.getKeyText(e.getKeyCode()));
	}

	@Override
	public void keyTyped (KeyEvent e) {
	//System.out.println(e.getKeyChar());
	}
	
	private final ImageIcon createImageIcon(String path, String description) {
	    java.net.URL imgURL = getClass().getResource(path);
	    if (imgURL != null) {
	        return new ImageIcon(imgURL, description);
	    } else {
	        System.err.println("Couldn't find file: " + path);
	        return null;
	    }
	}
	
	private final JMenuBar makeMenu() {
		JMenuBar bar = new JMenuBar();
		JMenu menu = new JMenu("Menu");
		
		bar.add(menu);

		this.addMenuItem(menu, "Reload level", RELOAD);
		this.addMenuItem(menu, "Load level", LOAD);
		menu.addSeparator();
		this.addMenuItem(menu, "Exit", EXIT);
		return bar;
	}

	private final void addMenuItem(JMenu menu, String label, String action) {
		JMenuItem item = new JMenuItem(label);
		item.setActionCommand(action);
		item.addActionListener(this);
		menu.add(item);
	}
	
	private static JLabel makeLabel(String s) {
		Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 32);
		JLabel b = new JLabel(s);
		b.setPreferredSize(new Dimension(50, 50));
		b.setMaximumSize(b.getSize());
		b.setFont(font);
		b.setBackground(Color.WHITE);
		return b;
	}
	
	private JButton makeButton(String s, String cmd) {
		Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 16);
		JButton b = new JButton(s);
		b.setPreferredSize(new Dimension(50, 50));
		b.setMaximumSize(b.getSize());
		b.setFont(font);
		b.setBackground(Color.WHITE);
		b.setActionCommand(cmd);;
		b.addActionListener(this);
		return b;
	}
	
	private JPanel makeLevelPanel() {
		
		int width = this.board.width();
		int height = this.board.height();
		
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(height, width, 0, 0));
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				Location loc = new Location(x, y);
				JLabel b = makeLabel("");
				p.add(b);
				this.levelMap.put(loc, b);
			}
		}
		return p;
	}
	
	private JPanel makeButtonPanel() {
		JLabel controls = new JLabel("WASD To Move, R To Restart Level.\n Push all the boxes onto the targets without getting stuck");
		JPanel p = new JPanel();	
		p.add(controls);
		return p;
	}
	
	private void drawWalls() {
		List<Wall> walls = this.board.getWalls();
		for (Wall w : walls) {
			Location loc = w.location();
			JLabel b = this.levelMap.get(loc);
			b.setIcon(this.wallIcon);
		}
	}
	
	private void drawStorage() {
		List<Storage> storage = this.board.getStorage();
		for (Storage s : storage) {
			Location loc = s.location();
			JLabel b = this.levelMap.get(loc);
			if (this.board.hasBox(loc)) {
				b.setIcon(this.boxAndStorageIcon);
			}
			else {
				b.setIcon(this.storageIcon);
			}
			
		}
	}
	
	private void drawBoxes() {
		List<Box> boxes = this.board.getBoxes();
		for (Box box : boxes) {
			Location loc = box.location();
			JLabel b = this.levelMap.get(loc);
			if (this.board.hasStorage(loc)) {
				b.setIcon(this.boxAndStorageIcon);
			}
			else {
				b.setIcon(this.boxIcon);
			}
			
		}
	}
	
	private void drawPlayer() {
		Location loc = this.board.getPlayer().location();
		JLabel b = this.levelMap.get(loc);
		b.setIcon(this.playerIcon);
	}
	

	
	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		Location loc = this.board.getPlayer().location();
		JLabel b = this.levelMap.get(loc);
		boolean moved = false;
		if (cmd.equals(LEFT)) {
			moved = this.board.movePlayerLeft();
		}
		else if (cmd.equals(RIGHT)) {
			moved = this.board.movePlayerRight();
		}
		else if (cmd.equals(UP)) {
			moved = this.board.movePlayerUp();
		}
		else if (cmd.equals(DOWN)) {
			moved = this.board.movePlayerDown();
		}
		else if (cmd.equals(RELOAD)) {
			if (this.filename.isEmpty()) {
				this.board = new Board();
				this.initLevel();
			}
			else {
				try {
					this.board = new Board(this.filename);
					this.initLevel();
				}
				catch (IOException x) {
					JOptionPane.showMessageDialog(this, "Could not read the level file.");
				}
			}
		}
		else if (cmd.equals(LOAD)) {
			Path path = FileSystems.getDefault().getPath("src", "sokoban");
			final JFileChooser fc = new JFileChooser();
			fc.setCurrentDirectory(path.toFile());
			int returnVal = fc.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				try {
					this.board = new Board(file.getName());
					this.filename = file.getName();
					this.initLevel();
				}
				catch (IOException x) {
					JOptionPane.showMessageDialog(this, "Could not read the level file.");
				}
				
			}
			
		}
		else if (cmd.equals(EXIT)) {
			this.dispose();
		}
		
		if (moved) {
			b.setIcon(null);
			this.drawStorage();
			this.drawBoxes();
			this.drawPlayer();
			if (this.board.isSolved()) {
				JOptionPane.showMessageDialog(this, "You Beat The Level! Click Menu in the top left to select a new level!");
			}
		}
	}
	
	
	public static void main(String[] args) throws IOException {
		SokobanGUI gui = new SokobanGUI();
		gui.setVisible(true);
		
	}

}
