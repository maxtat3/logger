package app;

import org.jfree.ui.ApplicationFrame;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.FileWriter;
import java.io.IOException;
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
	public static  boolean recordAction = false;

	public static int counterNumber = 1;

	public static int[] arrNumbers = new int[10000]; //2700000 знач для 3ч при 250 изм/с
	public static int[] arrValues = new int[10000];

	public static Label labelPortState;
	public static String portClose = "Порт занят/закрыт";
	public static String portOpen = "Порт открыт";


	static class DemoPanel extends JPanel implements ActionListener{

		private JFreeChart createChart(XYDataset xydataset){
			JFreeChart jfreechart = ChartFactory.createTimeSeriesChart("Осциллограф - самописец", "Time", "Value", xydataset, true, true, false);
			XYPlot xyplot = (XYPlot)jfreechart.getPlot();
			ValueAxis valueaxis = xyplot.getDomainAxis();
			valueaxis.setAutoRange(true);
			valueaxis.setFixedAutoRange(60000D);
			valueaxis = xyplot.getRangeAxis();
			valueaxis.setRange(0.0D, 255D);
			return jfreechart;
		}

		@Override
		public void actionPerformed(ActionEvent actionevent){
			if (actionevent.getActionCommand().equals("START_STOP_MEASURE")){
				if (startStopAction) {
					startStopAction = false;
//                  System.out.println("start action = false");
					if (recordAction) writeTextToFile();
				} else{
					startStopAction = true;
//                  System.out.println("start action = true");
				}
			}
		}


		public DemoPanel(){
			super(new BorderLayout());
			series = new TimeSeries("ADC data", DynamicData.class$org$jfree$data$time$Millisecond != null ? DynamicData.class$org$jfree$data$time$Millisecond : (DynamicData.class$org$jfree$data$time$Millisecond = DynamicData.class$("org.jfree.data.time.Millisecond")));
			TimeSeriesCollection timeseriescollection = new TimeSeriesCollection(series);
			ChartPanel chartpanel = new ChartPanel(createChart(timeseriescollection));
			chartpanel.setPreferredSize(new Dimension(900, 500));

			JPanel jpanel = new JPanel();
			jpanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

			JButton btn_start_stop = new JButton("Start/stop");
			btn_start_stop.setActionCommand("START_STOP_MEASURE");
			btn_start_stop.addActionListener(this);

			Checkbox checkbox_record = new Checkbox("Record ");
			checkbox_record.setFont(new Font("tahoma", Font.BOLD, 18));
			checkbox_record.setState(recordAction);
			checkbox_record.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (recordAction) {
						recordAction = false;
					} else{
						recordAction = true;
					}
//                        System.out.println("chek box !");
//                        System.out.println("record action = " + recordAction);
				}
			});

			labelPortState = new Label(portClose);
			labelPortState.setFont(new Font("tahoma", Font.BOLD, 18));

			jpanel.add(labelPortState);
			jpanel.add(checkbox_record);
			jpanel.add(btn_start_stop);

			add(chartpanel);
			add(jpanel, "South");
		}
	}


	static Class class$org$jfree$data$time$Millisecond; /* synthetic field */

	public DynamicData(String s){
		super(s);
		DemoPanel demopanel = new DemoPanel();
		setContentPane(demopanel);
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
			labelPortState.setText(portOpen);
			labelPortState.setForeground(new Color(50, 205, 50));
			//Выставляем параметры
			serialPort.setParams(SerialPort.BAUDRATE_9600,
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
			System.out.println("Порт закрыт");
			labelPortState.setText(portClose);
			labelPortState.setForeground(Color.red);
		}
	}


	private static class PortReader implements SerialPortEventListener {

		@Override
		public void serialEvent(SerialPortEvent event) {
			synchronized(event){
				if(event.isRXCHAR() && event.getEventValue() > 0){
//		    System.out.println("event.isRXCHAR() && event.getEventValue() > 0   is ok");
					try {
						if (startStopAction) {

							iArr = serialPort.readIntArray();
							dataValueMcu = iArr[0];
							series.add(new Millisecond(), dataValueMcu);

							//сохраняем значения для файла
							arrNumbers[counterNumber] = counterNumber;
							arrValues[counterNumber] = (int)dataValueMcu;
							counterNumber ++;
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


	static String strTmp = "NUMBER" + "\t" + "VALUE" + "\n";

	private static void writeTextToFile() {
//        System.out.println("запись текст данных в файл");
		try {
			FileWriter file = new FileWriter("E:\\testOscilRecorder.txt");
//	    System.out.println("iter = " + arrValues.length);

//            file.write("test string 1" + "\n");
//            file.write(String.valueOf(strMeasurement));
			for (int i = 0; i < arrNumbers.length; i++) {
				strTmp += arrNumbers[i] + "\t" + arrValues[i] + "\n";
			}
			file.write(strTmp);

			file.flush(); //очистить буфер -> запись файл !!!
			file.close();
		} catch (IOException ex) {
			System.out.println("ошибка в методе writeTextToFile !");
//            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

}
