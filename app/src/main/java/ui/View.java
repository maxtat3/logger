package ui;

import controller.Controller;
import model.Channel;
import exception.LargeChannelsSetupException;
import model.USART;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.HighLowRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;


public class View extends ApplicationFrame implements ViewCallback {

	private static final String PORT_CLOSE_TXT = "   Порт занят/закрыт ";
	private static final String PORT_OPEN_TXT = "   Порт открыт ";
	private static final String[] CHANNELS_NAMES = {
			Channel.Channels.ONE.getName(),
			Channel.Channels.TWO.getName(),
			Channel.Channels.THREE.getName(),
			Channel.Channels.FOUR.getName()
	};
	private static final String CHANNEL_1_TITLE_TXT = "ADC data 1";
	private static final String CHANNEL_2_TITLE_TXT = "ADC data 2";
	private static final String CHANNEL_3_TITLE_TXT = "ADC data 3";
	private static final String CHANNEL_4_TITLE_TXT = "ADC data 4";
	private static final String START_STOP_TXT = "Старт/стоп";
	private static final String RECORD_TXT = "Запись ";
	private static final String TITLE_CHART_TXT = "Осциллограф - самописец";
	private static final String AXIS_X_NAME_TXT = "Время";
	private static final String AXIS_Y_NAME_TXT = "Значение";
	private static final double MIN_AXIS_VALUE = 0D;
	private static final double MAX_AXIS_VALUE = 255D;

	private TimeSeries series1;
	private TimeSeries series2;
	private TimeSeries series3;
	private TimeSeries series4;

	// View components
	private final JComboBox<String> cmbCOMPortNumber = new JComboBox<String>();
	private final JComboBox<String> cmbNumberOfChannels = new JComboBox<String>();
	private final JComboBox<String> cmbSpsSelector = new JComboBox<String>();
	private final Label lbPortState = new Label();
	private final Checkbox chbRecord = new Checkbox();
	private final JButton btnStartProcess = new JButton();

	private static Class class$org$jfree$data$time$Millisecond; /* synthetic field */
    private Controller controller;


	/**
	 * Create application main window.
	 *
	 * @param title title main window, placed in top
     */
    public View(String title){
        super(title);
	    controller = new Controller(this);
	    controller.connectToDevice(USART.DefaultCOMPort.NAME);
	    MainPanel mainPanel = new MainPanel();
	    setContentPane(mainPanel);
		initDirPanelComponents();
    }

	/**
	 * Main panel contained chart and directions panels
	 */
	class MainPanel extends JPanel {
		public MainPanel(){
			super(new BorderLayout());

			ChartPanel chartpanel = new ChartPanel(createChart());
			chartpanel.setPreferredSize(new Dimension(900, 500));

			// Panel which placed direction elements
			JPanel dirPanel = new JPanel();
			dirPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

			dirPanel.add(cmbCOMPortNumber);
			dirPanel.add(cmbNumberOfChannels);
			dirPanel.add(cmbSpsSelector);
			dirPanel.add(lbPortState);
			dirPanel.add(chbRecord);
			dirPanel.add(btnStartProcess);

			add(chartpanel);
			add(dirPanel, BorderLayout.SOUTH);
		}
		/**
		 * Create chart from 4 channels times series collection
		 *
		 * @return building chart
		 * @see TimeSeriesCollection
		 */
		private JFreeChart createChart(){
			series1 = new TimeSeries(CHANNEL_1_TITLE_TXT, View.class$org$jfree$data$time$Millisecond != null ? View.class$org$jfree$data$time$Millisecond : (View.class$org$jfree$data$time$Millisecond = View.class$("org.jfree.data.time.Millisecond")));
			series2 = new TimeSeries(CHANNEL_2_TITLE_TXT, View.class$org$jfree$data$time$Millisecond != null ? View.class$org$jfree$data$time$Millisecond : (View.class$org$jfree$data$time$Millisecond = View.class$("org.jfree.data.time.Millisecond")));
			series3 = new TimeSeries(CHANNEL_3_TITLE_TXT, View.class$org$jfree$data$time$Millisecond != null ? View.class$org$jfree$data$time$Millisecond : (View.class$org$jfree$data$time$Millisecond = View.class$("org.jfree.data.time.Millisecond")));
			series4 = new TimeSeries(CHANNEL_4_TITLE_TXT, View.class$org$jfree$data$time$Millisecond != null ? View.class$org$jfree$data$time$Millisecond : (View.class$org$jfree$data$time$Millisecond = View.class$("org.jfree.data.time.Millisecond")));

			TimeSeriesCollection xyCh1 = new TimeSeriesCollection(series1);
			TimeSeriesCollection xyCh2 = new TimeSeriesCollection(series2);
			TimeSeriesCollection xyCh3 = new TimeSeriesCollection(series3);
			TimeSeriesCollection xyCh4 = new TimeSeriesCollection(series4);

			JFreeChart jfreechart = ChartFactory.createTimeSeriesChart(
					TITLE_CHART_TXT,
					AXIS_X_NAME_TXT,
					AXIS_Y_NAME_TXT,
					xyCh1,
					true,
					true,
					false
			);

			XYPlot xyplot = (XYPlot)jfreechart.getPlot();
			xyplot.setDataset(2, xyCh2);
			xyplot.setDataset(3, xyCh3);
			xyplot.setDataset(4, xyCh4);

			xyplot.setRenderer(2, new HighLowRenderer());
			xyplot.setRenderer(3, new HighLowRenderer());
			xyplot.setRenderer(4, new HighLowRenderer());

			ValueAxis va = xyplot.getDomainAxis();
			va.setAutoRange(true);
			va.setFixedAutoRange(60000D);
			va = xyplot.getRangeAxis();
			va.setRange(MIN_AXIS_VALUE, MAX_AXIS_VALUE);
			return jfreechart;
		}
	}

	/**
	 * Initialization and settings view components placed in direction panel.
	 */
	private void initDirPanelComponents() {
		cmbCOMPortNumber.setModel(new DefaultComboBoxModel<String>(USART.NUMBERS_OF_COM_PORTS));
		cmbCOMPortNumber.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				controller.connectToDevice(cmbCOMPortNumber.getSelectedItem().toString());
			}
		});
		cmbCOMPortNumber.setSelectedIndex(USART.DefaultCOMPort.NUMBER);

		cmbNumberOfChannels.setModel(new DefaultComboBoxModel<String>(CHANNELS_NAMES));
		cmbNumberOfChannels.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				setAvailableSPSInSelector();
			}
		});
		cmbNumberOfChannels.setSelectedIndex(cmbNumberOfChannels.getItemCount() - 1);

		cmbSpsSelector.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				controller.setSPS((String) cmbSpsSelector.getSelectedItem());
			}
		});

		setAvailableSPSInSelector(); //set default sps for selected channels when start app

		lbPortState.setFont(new Font("tahoma", Font.BOLD, 18));

		chbRecord.setLabel(RECORD_TXT);
		chbRecord.setFont(new Font("tahoma", Font.BOLD, 18));
		chbRecord.setState(false); // record not set by default
		chbRecord.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				controller.doRecord();
			}
		});

		btnStartProcess.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.doStartStopMsr();
			}
		});
		btnStartProcess.setText(START_STOP_TXT);
	}

	/**
	 * Stored temp sps for selected channel
	 */
	String[] availableSPS = new String[0];

	/**
	 * Set available samples per second (sps) for select channel in combobox selector
	 *
	 * @see #cmbSpsSelector
	 */
	private void setAvailableSPSInSelector() {
		try {
			availableSPS = controller.setChannelsNum(cmbNumberOfChannels.getSelectedIndex());
		} catch (LargeChannelsSetupException e1) {
			e1.printStackTrace();
		}
		cmbSpsSelector.setModel(new DefaultComboBoxModel<String>(availableSPS));
		controller.setSPS((String) cmbSpsSelector.getSelectedItem());
	}

    static Class class$(String s){
        Class clazz = null;
        try {
            clazz= Class.forName(s);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return clazz;
    }

    @Override
    public void setPortState(USART.PortStates portStates) {
		if (portStates == USART.PortStates.OPEN) {
            lbPortState.setText(PORT_OPEN_TXT);
            lbPortState.setForeground(new Color(50, 205, 50));

		} else if (portStates == USART.PortStates.CLOSE) {
			lbPortState.setText(PORT_CLOSE_TXT);
			lbPortState.setForeground(Color.red);
		}
    }

    @Override
    public void addChannel1Point(int channel1Point) {
        series1.add(new Millisecond(), channel1Point);
    }

	@Override
	public void addChannel2Point(int channel2Point) {
		series2.add(new Millisecond(), channel2Point);
	}

	@Override
	public void addChannel3Point(int channel3Point) {
		series3.add(new Millisecond(), channel3Point);
	}


	@Override
	public void addChannel4Point(int channel4Point) {
		series4.add(new Millisecond(), channel4Point);
	}


}
