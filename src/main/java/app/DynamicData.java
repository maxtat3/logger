package app;

import org.jfree.ui.ApplicationFrame;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;


public class DynamicData extends ApplicationFrame{

	public static  TimeSeries series;
	private static  SerialPort serialPort;

	public static double  dataValueMcu = 50D;
	public static char[] chArr;
	public static byte[] bArr;
	public static int[] iArr;

	public static  boolean startStopAction = false;


	/**
	 * Constructs a new application frame.
	 * @param title the frame title.
	 */
	public DynamicData(String title) {
		super(title);
		DemoPanel demopanel = new DemoPanel();
		setContentPane(demopanel);
	}

	static Class class$org$jfree$data$time$Millisecond; /* synthetic field */

	static class DemoPanel extends JPanel implements ActionListener{

		public DemoPanel(){
			super(new BorderLayout());
			series = new TimeSeries("ADC data", DynamicData.class$org$jfree$data$time$Millisecond != null ?
					DynamicData.class$org$jfree$data$time$Millisecond :
					(DynamicData.class$org$jfree$data$time$Millisecond = DynamicData.class$("org.jfree.data.time.Millisecond")));
			TimeSeriesCollection timeseriescollection = new TimeSeriesCollection(series);
			ChartPanel chartpanel = new ChartPanel(createChart(timeseriescollection));
			chartpanel.setPreferredSize(new Dimension(900, 500));

			JPanel jpanel = new JPanel();
			jpanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

			JButton btn_start_stop = new JButton("Start/stop");
			btn_start_stop.setActionCommand("START_STOP_MEASURE");
			btn_start_stop.addActionListener(this);
			jpanel.add(btn_start_stop);

			add(chartpanel);
			add(jpanel, "South");
		}

		private JFreeChart createChart(XYDataset xydataset){
			JFreeChart jfreechart = ChartFactory.createTimeSeriesChart("Осциллограф - самописец", "Time", "Value", xydataset, true, true, false);
			XYPlot xyplot = (XYPlot)jfreechart.getPlot();
			ValueAxis valueaxis = xyplot.getDomainAxis();
			valueaxis.setAutoRange(true);
			valueaxis.setFixedAutoRange(60000D);
			valueaxis = xyplot.getRangeAxis();
			valueaxis.setRange(0.0D, 200D);
			return jfreechart;
		}


		public void actionPerformed(ActionEvent actionevent){
			if (actionevent.getActionCommand().equals("START_STOP_MEASURE")){
				if (startStopAction) {
					startStopAction = false;
//                  System.out.println("start action = false");
				} else{
					startStopAction = true;
//                  System.out.println("start action = true");
				}
			}
		}
	}



	public static JPanel createDemoPanel(){
		return new DemoPanel();
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



	public static void uartInit(){
		//Передаём в конструктор имя порта
		serialPort = new SerialPort("COM4");
		try {
			//Открываем порт
			serialPort.openPort();
			//Выставляем параметры
			serialPort.setParams(SerialPort.BAUDRATE_1200,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);
			//Включаем аппаратное управление потоком
			serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN |
					SerialPort.FLOWCONTROL_RTSCTS_OUT);
			//Устанавливаем ивент лисенер и маску
			serialPort.addEventListener(new PortReader(), SerialPort.MASK_RXCHAR);
			//Отправляем запрос устройству
			serialPort.writeString("q");

		}
		catch (SerialPortException ex) {
			System.out.println(ex);
		}
	}


	private static class PortReader implements SerialPortEventListener {

		@Override
		public void serialEvent(SerialPortEvent event) {
			synchronized(event){
				if(event.isRXCHAR() && event.getEventValue() > 0){
					try {
						if (startStopAction) {

							iArr = serialPort.readIntArray();
							dataValueMcu = iArr[0];
							series.add(new Millisecond(), dataValueMcu);
						}
						serialPort.writeString("z"); //И снова отправляем запрос
					}
					catch (SerialPortException ex) {
						System.out.println(ex);
					}
				}
			}
		}
	}

}
