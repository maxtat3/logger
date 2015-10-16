package app;

import org.jfree.chart.renderer.xy.HighLowRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.ui.ApplicationFrame;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.*;

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

	public TimeSeries series1;
	public TimeSeries series2;
	public TimeSeries series3;
	public TimeSeries series4;
	private SerialPort serialPort;

	public  double  dataValueMcu = 50D;
	public  char[] chArr;
	public  byte[] bArr;
	public  int[] iArr;

	public   boolean startStopAction = false;
	public   boolean recordAction = false;

	public  int counterNumber = 1;

	public  int[] arrNumbers = new int[10000]; //2700000 знач для 3ч при 250 изм/с
	public  int[] arrValues = new int[10000];

	public  Label labelPortState;
	public static String portClose = "   Порт занят/закрыт ";
	public static String portOpen = "   Порт открыт ";

	public static String[] numbersCOMPorts = {"COM1","COM2","COM3","COM4","COM5","COM6","COM7","COM8","COM9","COM10","COM11","COM12","COM13","COM14","COM15","COM16"};

	String strTmp = "NUMBER" + "\t" + "VALUE" + "\n";

	public  JComboBox comboBox_chooserCOMPort = new JComboBox();

	public  int chaneelCounter = 1;





	class DemoPanel extends JPanel implements ActionListener{

		public DemoPanel(){
			super(new BorderLayout());
			series1 = new TimeSeries("ADC data 1", DynamicData.class$org$jfree$data$time$Millisecond != null ? DynamicData.class$org$jfree$data$time$Millisecond : (DynamicData.class$org$jfree$data$time$Millisecond = DynamicData.class$("org.jfree.data.time.Millisecond")));
			series2 = new TimeSeries("ADC data 2", DynamicData.class$org$jfree$data$time$Millisecond != null ? DynamicData.class$org$jfree$data$time$Millisecond : (DynamicData.class$org$jfree$data$time$Millisecond = DynamicData.class$("org.jfree.data.time.Millisecond")));
			series3 = new TimeSeries("ADC data 3", DynamicData.class$org$jfree$data$time$Millisecond != null ? DynamicData.class$org$jfree$data$time$Millisecond : (DynamicData.class$org$jfree$data$time$Millisecond = DynamicData.class$("org.jfree.data.time.Millisecond")));
			series4 = new TimeSeries("ADC data 4", DynamicData.class$org$jfree$data$time$Millisecond != null ? DynamicData.class$org$jfree$data$time$Millisecond : (DynamicData.class$org$jfree$data$time$Millisecond = DynamicData.class$("org.jfree.data.time.Millisecond")));

			TimeSeriesCollection timeseriescollection1 = new TimeSeriesCollection(series1);
			TimeSeriesCollection timeseriescollection2 = new TimeSeriesCollection(series2);
			TimeSeriesCollection timeseriescollection3 = new TimeSeriesCollection(series3);
			TimeSeriesCollection timeseriescollection4 = new TimeSeriesCollection(series4);

			ChartPanel chartpanel = new ChartPanel(createChart(timeseriescollection1, timeseriescollection2, timeseriescollection3, timeseriescollection4));
			chartpanel.setPreferredSize(new Dimension(900, 500));

			// добавляем панель
			JPanel jpanel = new JPanel();
			jpanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

			// создаем  управляющие элементы ...
			//кнопка
			JButton btn_start_stop = new JButton("Старт/стоп");
			btn_start_stop.setActionCommand("START_STOP_MEASURE");
			btn_start_stop.addActionListener(this);

			//переключатель
			Checkbox checkbox_record = new Checkbox("Запись ");
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

			//надпись для вывода текста
			labelPortState = new Label(portClose);
			labelPortState.setFont(new Font("tahoma", Font.BOLD, 18));

			//раскрывающейся список
//		JComboBox comboBox_chooserCOMPort = new JComboBox();
//                comboBox_chooserCOMPort.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
			comboBox_chooserCOMPort.setModel(new javax.swing.DefaultComboBoxModel(numbersCOMPorts));
			comboBox_chooserCOMPort.setSelectedIndex(6);
//		comboBox_chooserCOMPort.addItemListener(new ItemListener() {
//		    @Override
//		    public void itemStateChanged(ItemEvent e) {
//			System.out.println("addItemListener - ItemListener - itemStateChanged");
//			System.out.println("chooser com port = " + );
//		    }
//		});


			// добавляем разные управляющие элементы на панель
			jpanel.add(comboBox_chooserCOMPort);
			jpanel.add(labelPortState);
			jpanel.add(checkbox_record);
			jpanel.add(btn_start_stop);

			add(chartpanel);
			add(jpanel, "South");
		}


		private JFreeChart createChart(XYDataset xydataset, XYDataset xydataset2, XYDataset xydataset3, XYDataset xydataset4){
			JFreeChart jfreechart = ChartFactory.createTimeSeriesChart("Осциллограф - самописец", "Time", "Value", xydataset, true, true, false);

			XYPlot xyplot = (XYPlot)jfreechart.getPlot();
			xyplot.setDataset(2, xydataset2);
			xyplot.setDataset(3, xydataset3);
			xyplot.setDataset(4, xydataset4);

			xyplot.setRenderer(2, new HighLowRenderer());
//	    xyplot.setRenderer(3, new CyclicXYItemRenderer(2));
			xyplot.setRenderer(3, new HighLowRenderer());
			xyplot.setRenderer(4, new HighLowRenderer());
			XYItemRenderer renderer = xyplot.getRenderer();



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



	}


	static Class class$org$jfree$data$time$Millisecond; /* synthetic field */

	public DynamicData (String s){
		super(s);
		DemoPanel demopanel = new DemoPanel();
		setContentPane(demopanel);
	}

	public JPanel createDemoPanel(){
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





	public  void uartInit(){
		//Передаём в конструктор имя порта
//        serialPort = new SerialPort("COM7");
		serialPort = new SerialPort(comboBox_chooserCOMPort.getSelectedItem().toString());
		try {
			//Открываем порт
			serialPort.openPort();
			labelPortState.setText(portOpen);
			labelPortState.setForeground(new Color(50, 205, 50));
			//Выставляем параметры
			serialPort.setParams(SerialPort.BAUDRATE_1200,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);
			//Включаем аппаратное управление потоком (для FT232 нжуно отключать)
//            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN |
//                                          SerialPort.FLOWCONTROL_RTSCTS_OUT);
			//Устанавливаем ивент лисенер и маску
			serialPort.addEventListener(new PortReader(), SerialPort.MASK_RXCHAR);
			//Отправляем запрос устройству
			serialPort.writeString("1");

		}
		catch (SerialPortException ex) {
			System.out.println(ex);
			System.out.println("Порт закрыт");
			labelPortState.setText(portClose);
			labelPortState.setForeground(Color.red);
		}
	}


	private class PortReader implements SerialPortEventListener {

		@Override
		public void serialEvent(SerialPortEvent event) {
			synchronized(event){
				if(event.isRXCHAR() && event.getEventValue() > 0){
//		    System.out.println("event.isRXCHAR() && event.getEventValue() > 0   is ok");
					try {
						if (startStopAction) {

//                            iArr = serialPort.readIntArray();
//                            dataValueMcu = iArr[0];
//                            series1.add(new Millisecond(), dataValueMcu);
//			    series2.add(new Millisecond(), dataValueMcu + 10.0);
//
//                            //сохраняем значения для файла
//                            arrNumbers[counterNumber] = counterNumber;
//                            arrValues[counterNumber] = (int)dataValueMcu;
//                            counterNumber ++;

							if (chaneelCounter == 1) {
								iArr = serialPort.readIntArray();
								dataValueMcu = iArr[0];
								series1.add(new Millisecond(), dataValueMcu);
								chaneelCounter = 2;
							} else {
								iArr = serialPort.readIntArray();
								dataValueMcu = iArr[0];
								series2.add(new Millisecond(), dataValueMcu);
								series3.add(new Millisecond(), dataValueMcu + 5.0);
								series4.add(new Millisecond(), dataValueMcu + 25.0);
								chaneelCounter = 1;
							}
						}
						//и снова отправляем запрос
						// serialPort.writeString("z");
						if (chaneelCounter == 1) {
							serialPort.writeString("1");
//			    chaneelCounter = 2;
						}else{
							serialPort.writeString("2");
//			    chaneelCounter = 1;
						}
					}
					catch (SerialPortException ex) {
						System.out.println(ex);
					}
				}
			}
		}
	}




	private void writeTextToFile() {
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
