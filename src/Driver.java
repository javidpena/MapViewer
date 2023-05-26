import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

public class Driver extends JFrame implements ActionListener
{
	private static final long serialVersionUID = 1L;
	// Declare class data
	public GridBagConstraints layout;
	public JPanel userPanel;
	public JLabel fileLabel;
	public JButton openFileButton;
	public JComboBox<String> animationComboBox;
	public JCheckBox stopCheckBox;
	public JButton playButton;
	public JProgressBar progressBar;
	public JFileChooser fileChooser;
	public JPanel mapPanel;
	public JMapViewer map;
	
	public static Timer timer;
	BufferedImage icon = ImageIO.read(new File("arrow.png"));
	
	private static final int ZOOM_LEVEL = 5;
	String filename;
	int animationTime = 0;
	boolean includesStops;
	ArrayList<TripPoint> trip;
	int index = 1;
	boolean isPlaying = false;

	public Driver() throws IOException
	{
//		// Read file and call stop detection
//		TripPoint.readFile(filename);
//		TripPoint.h2StopDetection();
		
		// Initialize components
		playButton = new JButton("Play");
		playButton.addActionListener(new ActionListener()
		{	
			@Override
			public void actionPerformed(ActionEvent e)
			{		
				if(!isPlaying)
				{
					setupAnimation();
					
					if(trip == null)
					{
						return;
					}
					
					centerMap(new Coordinate(trip.get(0).getLat(), trip.get(0).getLon()));
					
//					if (timer != null)
//					{
//						timer.stop();
//					}
					
//					index = 1;
					progressBar.setMinimum(index);
					progressBar.setMaximum(trip.size());
					progressBar.setStringPainted(true);
					timer = new Timer(animationTime / trip.size(), Driver.this);
					timer.setInitialDelay(0);
					timer.start();
				}
				else
				{
					timer.stop();
					index = 1;
					centerMap(new Coordinate(trip.get(index).getLat(), trip.get(index).getLon()));
					map.removeAllMapMarkers();
					map.removeAllMapPolygons();
					progressBar.setStringPainted(false);
					progressBar.setValue(index);
				}
				
				isPlaying = !isPlaying;
				playButton.setText(isPlaying ? "Stop" : "Play");
			}
		});
		
		openFileButton = new JButton("Open File");
		openFileButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				int returnValue = fileChooser.showOpenDialog(Driver.this);
				if(returnValue == JFileChooser.APPROVE_OPTION)
				{
					filename = fileChooser.getName(fileChooser.getSelectedFile());
					fileLabel.setText(filename + " selected.");
				}
				else
				{
					JOptionPane.showMessageDialog(Driver.this, "Please select a file.");
				}
			}
		});

		stopCheckBox = new JCheckBox("Include Stops");
		animationComboBox = new JComboBox<String>(new String[] { "Animation Time", "15", "30", "60", "90" });
		progressBar = new JProgressBar();
		fileLabel = new JLabel("Select File: ");
		fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new FileNameExtensionFilter("CSV files", "csv"));

		// Create JMapViewer
		map = new JMapViewer();
		map.setTileSource(new OsmTileSource.TransportMap());
		map.setDisplayPosition(new Coordinate(35.211037,-97.438866), 5);

		// Create user input panel and add components to it
		userPanel = new JPanel();
		userPanel.setLayout(new GridBagLayout());

		layout = new GridBagConstraints();
		layout.gridx = 0;
		layout.gridy = 0;
		layout.insets = new Insets(0, 10, 0, 5);
		userPanel.add(fileLabel, layout);
		
		layout = new GridBagConstraints();
		layout.gridx = 1;
		layout.gridy = 0;
		layout.insets = new Insets(0, 0, 0, 10);
		userPanel.add(openFileButton, layout);
		
		layout = new GridBagConstraints();
		layout.gridx = 2;
		layout.gridy = 0;
		layout.insets = new Insets(0, 10, 0, 10);
		layout.anchor = GridBagConstraints.LINE_START;
		userPanel.add(animationComboBox, layout);

		layout = new GridBagConstraints();
		layout.gridx = 3;
		layout.gridy = 0;
		layout.insets = new Insets(0, 10, 0, 10);
		userPanel.add(stopCheckBox, layout);

		layout = new GridBagConstraints();
		layout.gridx = 4;
		layout.gridy = 0;
		layout.insets = new Insets(0, 10, 0, 10);
		userPanel.add(playButton, layout);
		
		layout = new GridBagConstraints();
		layout.gridx = 5;
		layout.gridy = 0;
		layout.insets = new Insets(0, 10, 0, 10);
		userPanel.add(progressBar, layout);

		// Create map panel and add JMapViewer
		mapPanel = new JPanel();
		mapPanel.setLayout(new GridBagLayout());
		layout = new GridBagConstraints();
		layout.weightx = 1;
		layout.weighty = 1;
		layout.fill = GridBagConstraints.BOTH;
		mapPanel.add(map, layout);

		// Add user panel to frame
		setLayout(new GridBagLayout());
		layout = new GridBagConstraints();
		layout.gridy = 0;
		layout.weightx = 1.0;
		layout.weighty = 0.05;
		layout.fill = GridBagConstraints.BOTH;
		add(userPanel, layout);

		layout = new GridBagConstraints();
		layout.gridy = 1;
		layout.weightx = 1.0;
		layout.weighty = 0.95;
		layout.fill = GridBagConstraints.BOTH;
		layout.insets = new Insets(5, 5, 5, 5);
		add(mapPanel, layout);
		
		// frame setup
		setTitle("Project 5 - Javid Pena-Limones");
		setExtendedState(MAXIMIZED_BOTH);
		//setSize(400,400);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent event)
	{
		if(index < trip.size())
		{
			mapPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			map.removeAllMapMarkers();
			
			Coordinate currentPoint = new Coordinate(trip.get(index).getLat(), trip.get(index).getLon());
			Coordinate previousPoint = new Coordinate(trip.get(index - 1).getLat(), trip.get(index - 1).getLon());
			
			double angle = Math.atan2((currentPoint.getLon() - previousPoint.getLon()), (currentPoint.getLat() - previousPoint.getLat()));			
			
			BufferedImage rotatedArrow = new BufferedImage(icon.getWidth(), icon.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = rotatedArrow.createGraphics();
			g2d.rotate(angle, (icon.getWidth() / 2), (icon.getHeight() / 2));
			g2d.drawImage(icon, null, 0,0);
			g2d.dispose();
			
			IconMarker raccoon = new IconMarker(currentPoint, rotatedArrow);
			map.addMapMarker(raccoon);
			MapPolygonImpl line = new MapPolygonImpl(currentPoint, previousPoint, previousPoint);
			line.setColor(Color.red);
			map.addMapPolygon(line);
			progressBar.setValue(index);
			++index;
			centerMap(currentPoint);
		}
		else
		{
			mapPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			Timer source = (Timer) event.getSource();
			source.stop();
			index = 1;
		}
	}
	
	public void setupAnimation()
	{
		// Read file and call stop detection
		try
		{
			TripPoint.readFile(filename);
			TripPoint.h2StopDetection();
		} catch (Exception e)
		{
			JOptionPane.showMessageDialog(Driver.this, "Select a file.");
			return;
		}
		
		String comboSelection = (String) animationComboBox.getSelectedItem();
		if(comboSelection.equals("Animation Time"))
		{
			JOptionPane.showMessageDialog(Driver.this, "Select a time for the animation.");
			return;
		}
		animationTime = Integer.parseInt(comboSelection) * 1000;
		
		includesStops = stopCheckBox.isSelected();
		if(includesStops)
		{
			trip = TripPoint.getTrip();
		}
		else
		{
			trip = TripPoint.getMovingTrip();
		}
	}
	
	public void centerMap(Coordinate coord)
	{
		map.setDisplayPosition(coord, ZOOM_LEVEL);
	}

	public static void main(String[] args) throws FileNotFoundException, IOException
	{
		new Driver();
	}
}