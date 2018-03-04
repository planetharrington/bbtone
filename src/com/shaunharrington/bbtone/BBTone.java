package com.shaunharrington.bbtone;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.JComboBox;
import javax.swing.JSpinner.NumberEditor;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class BBTone extends JFrame implements WindowListener {

	public static double M_PI = 3.14159265358979323846;

	public static double LOWEST_TONE_FREQUENCY = 20.0;

	public static double HIGHEST_TONE_FREQUENCY = 19999.9;

	public double toneFreq = 440.0;

	public int nOctave = 4;

	public int nNote = 0; // A

	private AudioFormat audioFormat;

	private SourceDataLine sourceDataLine;

	private JPanel mToolbar;

	private JPanel mToolbar2;

	private JButton playButton;

	private boolean boolPaused = false;

	PlayThread playThread = null;

	float[][] frequencies = {
			// A A# B C C# D D# E F F# G G#
			{ 27.5f, 29.1f, 30.9f, 32.7f, 34.6f, 36.7f, 38.9f, 41.2f, 43.7f,
					46.2f, 49.0f, 51.9f }, // 0
			{ 55.0f, 58.3f, 61.7f, 65.4f, 69.3f, 73.4f, 77.8f, 82.4f, 87.3f,
					92.5f, 98.0f, 103.8f }, // 1
			{ 110.0f, 116.5f, 123.5f, 130.8f, 138.6f, 146.8f, 155.6f, 164.8f,
					174.6f, 185.0f, 196.0f, 207.7f }, // 2
			{ 220.0f, 233.1f, 246.9f, 261.6f, 277.2f, 293.7f, 311.1f, 329.6f,
					349.2f, 370.0f, 392.0f, 415.3f }, // 3
			{ 440.0f, 466.2f, 493.9f, 523.3f, 554.4f, 587.3f, 622.3f, 659.3f,
					698.5f, 740.0f, 784.0f, 830.6f }, // 4
			{ 880.0f, 932.3f, 987.8f, 1046.5f, 1108.8f, 1174.7f, 1244.5f,
					1318.5f, 1396.9f, 1480.0f, 1568.0f, 1661.2f }, // 5
			{ 1760.0f, 1864.7f, 1975.5f, 2093.0f, 2217.5f, 2349.3f, 2489.0f,
					2637.0f, 2793.8f, 2960.0f, 3136.0f, 3322.4f }, // 6
			{ 3520.0f, 3729.3f, 3951.1f, 4186.0f, 4434.9f, 4698.6f, 4978.0f,
					5274.0f, 5587.7f, 5919.9f, 6271.9f, 6644.9f }, // 7
			{ 7040.0f, 7458.6f, 7902.1f, 8372.0f, 8869.8f, 9397.3f, 9956.1f,
					10548.1f, 11175.3f, 11839.8f, 12543.9f, 13289.8f }, // 8
			{ 14080.0f, 14917.2f, 15804.3f, 15804.3f, 15804.3f, 18794.6f,
					19912.2f, 21096.2f, 22350.6f, 23679.6f, 25087.8f, 26579.6f } // 9
	};

	String[] notes = { "A", "Bb/A#", "B", "C", "Db/C#", "D", "Eb/D#", "E", "F",
			"Gb/F#", "G", "Ab/G#" };

	private JComboBox comboNote = new JComboBox(notes);

	String[] octaves = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };

	private JComboBox comboOctave = new JComboBox(octaves);

	SpinnerNumberModel model = new SpinnerNumberModel(toneFreq,
			LOWEST_TONE_FREQUENCY, HIGHEST_TONE_FREQUENCY, .1);

	JSpinner jSpinner = new JSpinner(model);

	NumberEditor textField1 = new NumberEditor(jSpinner);

	public BBTone() throws Exception {
		super("BB-Tone by Shaun Harrington");
		initComponents();
	}

	private void initComponents() throws Exception {
		addWindowListener(this);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((screenSize.width - 280) / 2,
				((screenSize.height - 90) / 2) - 90, 280, 90);

		GridBagConstraints gridBagConstraints;
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.CENTER;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;

		// Build the tool bar... the play/stop button makes up the rest of the
		// pane.
		mToolbar = new JPanel();
		mToolbar.setLayout(new GridBagLayout());
		mToolbar.setMinimumSize(new Dimension(200, 50));

		gridBagConstraints.fill = GridBagConstraints.BOTH;
		mToolbar.add(jSpinner, gridBagConstraints);
		gridBagConstraints.gridx = 1;
		mToolbar.add(comboNote, gridBagConstraints);
		gridBagConstraints.gridx = -1;
		mToolbar.add(comboOctave, gridBagConstraints);
		comboOctave.setSelectedItem("4");
		comboNote.setToolTipText("Select the note."); //$NON-NLS-1$
		comboOctave.setToolTipText("Select the octave."); //$NON-NLS-1$

		jSpinner.setToolTipText("Enter the exact frequency value.");
		model.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Object source = e.getSource();
				toneFreq = ((Number) model.getValue()).doubleValue();

				if (playThread != null && playThread.isAlive()) {
					playThread.init();
				}
			};
		});

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(mToolbar, BorderLayout.NORTH);

		playButton = new JButton();
		mToolbar2 = new JPanel();
		mToolbar2.setLayout(new BorderLayout());
		mToolbar2.setMinimumSize(new Dimension(10, 50));
		playButton.setText("Play"); //$NON-NLS-1$
		playButton.setToolTipText("Play the indicated tone."); //$NON-NLS-1$
		playButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				playActionPerformed(e);
			}
		});
		comboOctave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				octaveActionPerformed(e);
			}
		});
		comboNote.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				noteActionPerformed(e);
			}
		});

		mToolbar2.add(playButton);
		getContentPane().add(mToolbar2, BorderLayout.CENTER);
	}

	protected void octaveActionPerformed(ActionEvent e) {
		Object source = e.getSource();
		String stringVal = comboOctave.getSelectedItem().toString();
		try {
			nOctave = Integer.parseInt(stringVal);
		} catch (Exception exc) {
			return;
		}

		toneFreq = frequencies[nOctave][nNote];
		model.setValue(new Double(toneFreq));
		if (playThread != null && playThread.isAlive()) {
			playThread.init();
		}
	}

	protected void noteActionPerformed(ActionEvent e) {
		Object source = e.getSource();
		nNote = comboNote.getSelectedIndex();
		toneFreq = frequencies[nOctave][nNote];
		model.setValue(new Double(toneFreq));
		if (playThread != null && playThread.isAlive()) {
			playThread.init();
		}
	}

	protected void playActionPerformed(ActionEvent e) {
		if (playAudio())
			((JButton) e.getSource()).setText("Stop"); //$NON-NLS-1$
		else
			((JButton) e.getSource()).setText("Play"); //$NON-NLS-1$
	}

	private boolean playAudio() {
		boolean boolRet = true;
		String strText2 = new String(textField1.getTextField().getValue()
				.toString());
		toneFreq = Double.parseDouble(strText2);
		try {
			if (playThread != null && playThread.isAlive()) {
				boolRet = false;
				playThread.stop = true;
			} else {
				// Get things set up for capture
				audioFormat = getAudioFormat();
				DataLine.Info dataLineInfo = new DataLine.Info(
						SourceDataLine.class, audioFormat);
				sourceDataLine = (SourceDataLine) AudioSystem
						.getLine(dataLineInfo);
				playThread = new PlayThread();
				playThread.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}// end catch
		return boolRet;
	}// end captureAudio method

	private AudioFormat getAudioFormat() {
		float sampleRate = 44100.0F;
		int sampleSizeInBits = 16;
		int channels = 1;
		boolean signed = true;
		boolean bigEndian = false;
		return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed,
				bigEndian);
	}// end getAudioFormat

	class PlayThread extends Thread {
		public boolean stop = false;

		double dblAngle = 0.0f;

		double dblAngleStep = M_PI * 2 * toneFreq / 44100.0;

		public void init() {
			dblAngleStep = M_PI * 2 * toneFreq / 44100.0;
		}

		public void run() {
			try {
				sourceDataLine.open(audioFormat);
				sourceDataLine.start();

				ByteBuffer byteBuffer = ByteBuffer.allocate(4000);
				byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

				while (!stop) {
					byteBuffer.rewind();
					FillBuffer(byteBuffer.asShortBuffer(), 2000);
					byteBuffer.rewind();
					sourceDataLine.write(byteBuffer.array(), 0, 4000);
				}
				sourceDataLine.stop();
				sourceDataLine.close();
			} catch (Exception e) {
				e.printStackTrace();
			}// end catch
		}// end run

		void FillBuffer(ShortBuffer shortBuffer, int dwLength) {
			double sample = 0.0f;
			for (int counter = 0; counter < dwLength; counter++) {
				sample = 10000 * Math.sin(dblAngle);
				// sample = 10000 * sign(sample); // Add to make square wave
				// form!
				dblAngle += dblAngleStep;
				shortBuffer.put(counter, (short) sample);
				// lpSamples[counter] = (short) sample;
			}
		}
	}// end inner class CaptureThread

	public void windowActivated(WindowEvent e) {
	}

	public void windowClosing(WindowEvent e) {
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
		if (playThread != null && playThread.isAlive()) {
			playThread.stop = true;
			boolPaused = true;
		}
	}

	public void windowDeiconified(WindowEvent e) {
		if (boolPaused) {
			boolPaused = false;
			playAudio();
		}
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public void windowGainedFocus(WindowEvent e) {
	}

	public void windowLostFocus(WindowEvent e) {
	}

	public void windowStateChanged(WindowEvent e) {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Throwable e1) {
			e1.printStackTrace();
		}
		EventQueue.invokeLater(new Runnable() {

			public void run() {
				try {
					new BBTone().setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}
