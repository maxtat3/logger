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
import java.util.ArrayList;
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

	private TimeSeries series1;
	private TimeSeries series2;
	private TimeSeries series3;
	private TimeSeries series4;

	private SerialPort serialPort;

	private double  dataValueMcu = 0;
	private int[] iArr;

	private boolean startStopAction = false;
	private boolean recordAction = false;

	public Label label_portState;

	public static final  String PORT_CLOSE = "   Порт занят/закрыт ";
	public static final  String PORT_OPEN = "   Порт открыт ";
	public static final  String[] NUMBERS_OF_COM_PORTS = {"COM1","COM2","COM3","COM4","COM5","COM6","COM7","COM8","COM9","COM10","COM11","COM12","COM13","COM14","COM15","COM16"};
	public static final  String START_STOP_MEASURE = "obj.DynamicData.START_STOP_MEASURE";
	private static final String ADC_1_DATA = "ADC data 1";
	private static final String ADC_2_DATA = "ADC data 2";
	private static final String ADC_3_DATA = "ADC data 3";
	private static final String ADC_4_DATA = "ADC data 4";
	private static final String BTN_START_STOP = "Старт/стоп";
	private static final String CHECKBOX_RECORD = "Запись ";
	private static final String TITLE_CHART = "Осциллограф - самописец";
	private static final String AXIS_X_NAME = "Время";
	private static final String AXIS_Y_NAME = "Значение";
	private static final double MIN_AXIS_VALUE = 0D;
	private static final double MAX_AXIS_VALUE = 255D;
	private static final String SIGNATURE_HEADER_DATA_CHANNELS = "NUMBER" + "\t" + "CH1"+ "\t" + "CH2"+ "\t" + "CH3"+ "\t" + "CH4";

	StringBuilder strBuilderFile = new StringBuilder();
	//    String strTmp = "NUMBER" + "\t" + "VALUE" + "\n";
	String strTmp;

	public  JComboBox comboBox_chooserCOMPort = new JComboBox();

	public  int chaneelCounter = 1;

	ArrayList<Integer> values1 = new ArrayList<>();
	ArrayList<Integer> values2 = new ArrayList<>();
	ArrayList<Integer> values3 = new ArrayList<>();
	ArrayList<Integer> values4 = new ArrayList<>();


	class DemoPanel extends JPanel implements ActionListener{

		public DemoPanel(){
			super(new BorderLayout());
			series1 = new TimeSeries(ADC_1_DATA, DynamicData.class$org$jfree$data$time$Millisecond != null ? DynamicData.class$org$jfree$data$time$Millisecond : (DynamicData.class$org$jfree$data$time$Millisecond = DynamicData.class$("org.jfree.data.time.Millisecond")));
			series2 = new TimeSeries(ADC_2_DATA, DynamicData.class$org$jfree$data$time$Millisecond != null ? DynamicData.class$org$jfree$data$time$Millisecond : (DynamicData.class$org$jfree$data$time$Millisecond = DynamicData.class$("org.jfree.data.time.Millisecond")));
			series3 = new TimeSeries(ADC_3_DATA, DynamicData.class$org$jfree$data$time$Millisecond != null ? DynamicData.class$org$jfree$data$time$Millisecond : (DynamicData.class$org$jfree$data$time$Millisecond = DynamicData.class$("org.jfree.data.time.Millisecond")));
			series4 = new TimeSeries(ADC_4_DATA, DynamicData.class$org$jfree$data$time$Millisecond != null ? DynamicData.class$org$jfree$data$time$Millisecond : (DynamicData.class$org$jfree$data$time$Millisecond = DynamicData.class$("org.jfree.data.time.Millisecond")));

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
			JButton btn_startStop = new JButton(BTN_START_STOP);
			btn_startStop.setActionCommand(START_STOP_MEASURE);
			btn_startStop.addActionListener(this);

			//переключатель
			Checkbox checkbox_record = new Checkbox(CHECKBOX_RECORD);
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
			label_portState = new Label(PORT_CLOSE);
			label_portState.setFont(new Font("tahoma", Font.BOLD, 18));

			//раскрывающейся список
//		JComboBox comboBox_chooserCOMPort = new JComboBox();
//                comboBox_chooserCOMPort.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
			comboBox_chooserCOMPort.setModel(new javax.swing.DefaultComboBoxModel(NUMBERS_OF_COM_PORTS));
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
			jpanel.add(label_portState);
			jpanel.add(checkbox_record);
			jpanel.add(btn_startStop);

			add(chartpanel);
			add(jpanel, "South");
		}


		private JFreeChart createChart(XYDataset xydataset, XYDataset xydataset2, XYDataset xydataset3, XYDataset xydataset4){
			JFreeChart jfreechart = ChartFactory.createTimeSeriesChart(TITLE_CHART, AXIS_X_NAME, AXIS_Y_NAME, xydataset, true, true, false);

			XYPlot xyplot = (XYPlot)jfreechart.getPlot();
			xyplot.setDataset(2, xydataset2);
			xyplot.setDataset(3, xydataset3);
			xyplot.setDataset(4, xydataset4);

			xyplot.setRenderer(2, new HighLowRenderer());
			xyplot.setRenderer(3, new HighLowRenderer());
			xyplot.setRenderer(4, new HighLowRenderer());
			XYItemRenderer renderer = xyplot.getRenderer();



			ValueAxis valueaxis = xyplot.getDomainAxis();
			valueaxis.setAutoRange(true);
			valueaxis.setFixedAutoRange(60000D);
			valueaxis = xyplot.getRangeAxis();
			valueaxis.setRange(MIN_AXIS_VALUE, MAX_AXIS_VALUE);
			return jfreechart;
		}

		@Override
		public void actionPerformed(ActionEvent actionevent){
			if (actionevent.getActionCommand().equals(START_STOP_MEASURE)){
				if (startStopAction) {			    //стоп измерений
					startStopAction = false;
//                  System.out.println("start action = false");
					if (recordAction) writeTextToFile();
				} else{					    //старт измерений
					try {
						serialPort.writeString("1");
					} catch (Exception e) {
					}
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
			label_portState.setText(PORT_OPEN);
			label_portState.setForeground(new Color(50, 205, 50));
			//Выставляем параметры
			serialPort.setParams(SerialPort.BAUDRATE_19200,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);
			//Включаем аппаратное управление потоком (для FT232 нжуно отключать)
//            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | 
//                                          SerialPort.FLOWCONTROL_RTSCTS_OUT);
			//Устанавливаем ивент лисенер и маску
			serialPort.addEventListener(new PortReader(), SerialPort.MASK_RXCHAR);
			//Отправляем запрос устройству
			serialPort.writeString("1"); //начинаем с канала 1

		}
		catch (SerialPortException ex) {
			System.out.println(ex);
			System.out.println("Порт закрыт");
			label_portState.setText(PORT_CLOSE);
			label_portState.setForeground(Color.red);
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
							if(chaneelCounter == 1) {
								iArr = serialPort.readIntArray();
								dataValueMcu = iArr[0];
								series1.add(new Millisecond(), dataValueMcu);
								values1.add(iArr[0]);
							} else if(chaneelCounter == 2){
								iArr = serialPort.readIntArray();
								dataValueMcu = iArr[0];
								series2.add(new Millisecond(), dataValueMcu);
								values2.add(iArr[0]);
							} else if(chaneelCounter == 3){
								iArr = serialPort.readIntArray();
								dataValueMcu = iArr[0];
								series3.add(new Millisecond(), dataValueMcu);
								values3.add(iArr[0]);
							} else if(chaneelCounter == 4){
								iArr = serialPort.readIntArray();
								dataValueMcu = iArr[0];
								series4.add(new Millisecond(), dataValueMcu);
								values4.add(iArr[0]);
							}

							//и снова отправляем запрос 
							//для следующего канала
							if (chaneelCounter == 1) {
								serialPort.writeString("2");
								chaneelCounter = 2;
							} else if (chaneelCounter == 2){
								serialPort.writeString("3");
								chaneelCounter = 3;
							} else if (chaneelCounter == 3){
								serialPort.writeString("4");
								chaneelCounter = 4;
							} else if (chaneelCounter == 4){
								serialPort.writeString("1");
								chaneelCounter = 1;
							}

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
		try {
			FileWriter file = new FileWriter("E:\\testOscilRecorder.txt");

//	    values1.add((int)55.0);
			System.out.println("запись в файл");

			strBuilderFile.append(SIGNATURE_HEADER_DATA_CHANNELS).append("\n");

			System.out.println("size of values1 = " + values1.size());
			System.out.println("size of values2 = " + values2.size());
			System.out.println("size of values3 = " + values3.size());
			System.out.println("size of values4 = " + values4.size());

			int c = 0;
			while ( (values1.size() - 1) != c ){
//		System.out.println("number " + c + "  = " + 
//			values1.get(c) + "\t" + 
//			values2.get(c) + "\t" + 
//			values3.get(c) + "\t" + 
//			values4.get(c) + "\t");
				strBuilderFile.append(c).append("\t")
						.append(values1.get(c)).append("\t")
						.append(values2.get(c)).append("\t")
						.append(values3.get(c)).append("\t")
						.append(values4.get(c)).append("\t")
						.append("\n");
				c++;
			}
			System.out.println("values1 size = " + values1.size());
			values1.removeAll(values1);
			System.out.println("removed all numbers in values1 ");
			System.out.println("valuses1 size = " + values1.size());


			System.out.println("strBuilderFile = " + strBuilderFile);

			strTmp = String.valueOf(strBuilderFile);
			file.write(strTmp);

			file.flush(); //очистить буфер -> запись файл !!!
			file.close();
		} catch (IOException ex) {
			System.out.println("ошибка в методе writeTextToFile !");
//            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
		}
	}




}
