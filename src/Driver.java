import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

public class Driver extends JFrame implements ActionListener
{
	// Declare class data
	public GridBagConstraints layout;
	public JPanel userPanel;
	public JPanel mapPanel;
	public JComboBox<String> animationComboBox;
	public JCheckBox stopCheckBox;
	public JButton playButton;
	public JMapViewer map;
	public static Timer timer;
	Image icon = ImageIO.read(new File("raccoon.png"));
	
	int animationTime = 0;
	boolean includesStops;
	ArrayList<TripPoint> trip;
	int index = 1;

	public Driver() throws IOException
	{
		// Initialize components
		playButton = new JButton("Play");
		playButton.addActionListener(new ActionListener()
		{	
			@Override
			public void actionPerformed(ActionEvent e)
			{
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
				
				map.setDisplayPosition(new Coordinate(trip.get(0).getLat(), trip.get(0).getLon()), 5);
				map.removeAllMapPolygons();
				
				if (timer != null)
				{
					timer.stop();
				}
				
				index = 1;
				timer = new Timer(animationTime / trip.size(), Driver.this);
				timer.setInitialDelay(0);
				timer.start();
			}
		});

		stopCheckBox = new JCheckBox("Include Stops");
		animationComboBox = new JComboBox<String>(new String[] { "Animation Time", "15", "30", "60", "90" });

		// Create JMapViewer
		map = new JMapViewer();
		map.setTileSource(new OsmTileSource.TransportMap());
		// map.setDisplayPosition(new Coordinate(35.211037,-97.438866), 5);

		// Create user input panel and add components to it
		userPanel = new JPanel();
		userPanel.setLayout(new GridBagLayout());

		layout = new GridBagConstraints();
		layout.gridx = 0;
		layout.gridy = 0;
		layout.anchor = GridBagConstraints.LINE_START;
		userPanel.add(animationComboBox, layout);

		layout = new GridBagConstraints();
		layout.gridx = 1;
		layout.gridy = 0;
		userPanel.add(stopCheckBox, layout);

		layout = new GridBagConstraints();
		layout.gridx = 2;
		layout.gridy = 0;
		userPanel.add(playButton, layout);

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
		setSize(400, 400);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent event)
	{
		if(index < trip.size())
		{
			map.removeAllMapMarkers();
			Coordinate currentPoint = new Coordinate(trip.get(index).getLat(), trip.get(index).getLon());
			Coordinate previousPoint = new Coordinate(trip.get(index - 1).getLat(), trip.get(index - 1).getLon());
			
			IconMarker raccoon = new IconMarker(currentPoint, icon);
			map.addMapMarker(raccoon);
			MapPolygonImpl line = new MapPolygonImpl(currentPoint, previousPoint, previousPoint);
			line.setColor(Color.red);
			map.addMapPolygon(line);
			++index;
		}
		else
		{
			Timer source = (Timer) event.getSource();
			source.stop();
			index = 1;
		}
	}


	public static void main(String[] args) throws FileNotFoundException, IOException
	{
		// Read file and call stop detection
		TripPoint.readFile("triplog.csv");
		TripPoint.h2StopDetection();

		// Set up frame, include your name in the title
		new Driver();
	}
}