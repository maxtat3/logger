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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
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

	public static final  String PORT_CLOSE = "   Порт занят/закрыт ";
	public static final  String PORT_OPEN = "   Порт открыт ";
	private static final  String[] NUMBERS_OF_COM_PORTS = {
            "COM1","COM2","COM3","COM4","COM5","COM6","COM7","COM8",
            "COM9","COM10","COM11","COM12","COM13","COM14","COM15","COM16",
            "/dev/ttyUSB0", "/dev/ttyUSB1", "/dev/ttyUSB2", "/dev/ttyUSB3",
            "/dev/ttyACM0", "/dev/ttyACM1", "/dev/ttyACM2", "/dev/ttyACM3",
    };
	private static final String[] TOTAL_CHANNELS = {"1 канал", "2 канала", "3 канала", "4 канала"};
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

	//    private static final String[] SAMPLES_PER_SECOND = {"60 выборок/с", "30 выборок/с", "20 выборок/с", "15 выборок/с", "10 выборок/с", "5 выборок/с"};
	private static final String[] SAMPLES_PER_SECOND = {"60 выборок/с",
			"30 выборок/с", //1[]
			"5 выборок/с", //2[]

			"30 выборок/с ", //1[*]
			"10 выборок/с",
			"5 выборок/с ", //2[*]

			"20 выборок/с",
			"5 выборок/с  ", //2[**]

			"15 выборок/с",
			"5 выборок/с   "}; //2[***]
	private static final String[] CH1_60_SPS_NOTDELAY = {SAMPLES_PER_SECOND[0], "a"};
	private static final String[] CH1_30_SPS_31_MS = {SAMPLES_PER_SECOND[1], "b"};
	private static final String[] CH1_5_SPS_190_MS = {SAMPLES_PER_SECOND[2], "c"};

	private static final String[] CH2_30_SPS_NOTDELAY = {SAMPLES_PER_SECOND[3], "d"};
	private static final String[] CH2_10_SPS_50_MS = {SAMPLES_PER_SECOND[4], "e"};
	private static final String[] CH2_5_SPS_100_MS = {SAMPLES_PER_SECOND[5], "f"};

	private static final String[] CH3_20_SPS_NOTDELAY = {SAMPLES_PER_SECOND[6], "g"};
	private static final String[] CH3_5_SPS_62_MS = {SAMPLES_PER_SECOND[7], "h"};

	private static final String[] CH4_15_SPS_NOTDELAY = {SAMPLES_PER_SECOND[8], "k"};
	private static final String[] CH4_5_SPS_45_MS = {SAMPLES_PER_SECOND[9], "l"};

	private TimeSeries series1;
	private TimeSeries series2;
	private TimeSeries series3;
	private TimeSeries series4;

	private SerialPort serialPort;
	public Label label_portState;
	public JComboBox comboBox_chooserCOMPort = new JComboBox();
	public JComboBox comboBox_chooserSamplesPerSecond = new JComboBox();
	public JComboBox comboBox_chooserChaneels = new JComboBox();

	private boolean startStopAction = false;
	private boolean recordAction = false;

    private int maxCh = 4;
    public  int chaneelCounter = 1;
    private double  dataValueMcu = 0;
    private int[] iArr;
    StringBuilder strBuilderFile = new StringBuilder();

	String strTmp;
    private Result result = new Result();


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
			//кнопка - старт/стоп
			JButton btn_startStop = new JButton(BTN_START_STOP);
			btn_startStop.setActionCommand(START_STOP_MEASURE);
			btn_startStop.addActionListener(this);

			//переключатель - запись?
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
				}
			});

			//надпись для вывода текста - информация о состоянии COM порта
			label_portState = new Label(PORT_CLOSE);
			label_portState.setFont(new Font("tahoma", Font.BOLD, 18));

			//раскрывающейся список - выбор COM порта
			comboBox_chooserCOMPort.setModel(new javax.swing.DefaultComboBoxModel(NUMBERS_OF_COM_PORTS));
			comboBox_chooserCOMPort.setSelectedIndex(9);
			comboBox_chooserCOMPort.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if(serialPort.isOpened()){
						try {
							serialPort.closePort();
						} catch (SerialPortException ex) {
							Logger.getLogger(DynamicData.class.getName()).log(Level.SEVERE, null, ex);
						}
						uartInit();
					}else{
						uartInit();
					}
				}
			});

			//----------------------------------------------------------------
			//раскрывающейся список - выбор количества каналов
			//----------------------------------------------------------------
			comboBox_chooserChaneels.setModel(new javax.swing.DefaultComboBoxModel(TOTAL_CHANNELS));
			comboBox_chooserChaneels.setSelectedIndex(comboBox_chooserChaneels.getItemCount() - 1);
			comboBox_chooserChaneels.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					switch (comboBox_chooserChaneels.getSelectedIndex()) {
						case 0:
							maxCh = 1;
							setChoosesValuesSamplesPerSecond(maxCh);
							setMCUSamplesPerSecond((String)comboBox_chooserSamplesPerSecond.getSelectedItem());
							break;
						case 1:
							maxCh = 2;
							setChoosesValuesSamplesPerSecond(maxCh);
							setMCUSamplesPerSecond((String)comboBox_chooserSamplesPerSecond.getSelectedItem());
							break;
						case 2:
							maxCh = 3;
							setChoosesValuesSamplesPerSecond(maxCh);
							setMCUSamplesPerSecond((String)comboBox_chooserSamplesPerSecond.getSelectedItem());
							break;
						case 3:
							maxCh = 4;
							setChoosesValuesSamplesPerSecond(maxCh);
							setMCUSamplesPerSecond((String)comboBox_chooserSamplesPerSecond.getSelectedItem());
							break;
						default:
							System.out.println("itemStateChanged > 4");
					}
				}
			});

			//----------------------------------------------------------------
			//раскрывающейся список - выбор задержки между измерениями
			//т.е. количества выборок в секунду
			//----------------------------------------------------------------
//	    comboBox_chooserSamplesPerSecond.setModel(new javax.swing.DefaultComboBoxModel(SAMPLES_PER_SECOND));
			setChoosesValuesSamplesPerSecond(comboBox_chooserChaneels.getSelectedIndex());
			comboBox_chooserSamplesPerSecond.setSelectedItem(0);
			comboBox_chooserSamplesPerSecond.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					setMCUSamplesPerSecond((String)comboBox_chooserSamplesPerSecond.getSelectedItem());
//		    System.out.println("sel item =      " + (String)comboBox_chooserSamplesPerSecond.getSelectedItem());
//		    System.out.println("-----------");
				}
			});



			// добавляем разные управляющие элементы на панель
			jpanel.add(comboBox_chooserCOMPort);
			jpanel.add(comboBox_chooserSamplesPerSecond);
			jpanel.add(comboBox_chooserChaneels);
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
				//стоп измерений
				if (startStopAction) {
					startStopAction = false;
					if (recordAction)
                        try {
						// 4 - may be dynamic change when user selected channel number
						new Recorder().writeResultsToFile(
                                4,
                                result.getChannel1AllValues(),
                                result.getChannel2AllValues(),
                                result.getChannel3AllValues(),
                                result.getChannel4AllValues()
                        );
					} catch (LargeChannelsRecordException e) {
						e.printStackTrace();
					}
					//старт измерений
				}else{
					try {
						serialPort.writeString("1");
					} catch (Exception e) {
						System.out.println("actionevent.getActionCommand().equals(START_STOP_MEASURE)");
					}
					startStopAction = true;
				}
			}
		}

	} //------------------------------------ end class DemoModel

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


	public void setMCUSamplesPerSecond(String chooseValSps){
		try {
			serialPort.writeString("s");    //спец символ - установка mcu в режим выбора типа задержки
			Thread.sleep(100);

			if(chooseValSps.equals(CH1_60_SPS_NOTDELAY[0])){
				serialPort.writeString(CH1_60_SPS_NOTDELAY[1]);
				System.out.println("[a]");
			}else if(chooseValSps.equals(CH1_30_SPS_31_MS[0])) {
				System.out.println("[b]");
				serialPort.writeString(CH1_30_SPS_31_MS[1]);
			}else if(chooseValSps.equals(CH1_5_SPS_190_MS[0])){
				System.out.println("[c]");
				serialPort.writeString(CH1_5_SPS_190_MS[1]);

			}else if(chooseValSps.equals(CH2_30_SPS_NOTDELAY[0])){
				System.out.println("[d]");
				serialPort.writeString(CH2_30_SPS_NOTDELAY[1]);
			}else if(chooseValSps.equals(CH2_10_SPS_50_MS[0])){
				System.out.println("[e]");
				serialPort.writeString(CH2_10_SPS_50_MS[1]);
			}else if(chooseValSps.equals(CH2_5_SPS_100_MS[0])){
				System.out.println("[f]");
				serialPort.writeString(CH2_5_SPS_100_MS[1]);

			}else if(chooseValSps.equals(CH3_20_SPS_NOTDELAY[0])){
				System.out.println("[g]");
				serialPort.writeString(CH3_20_SPS_NOTDELAY[1]);
			}else if(chooseValSps.equals(CH3_5_SPS_62_MS[0])){
				System.out.println("[h]");
				serialPort.writeString(CH3_5_SPS_62_MS[1]);

			}else if(chooseValSps.equals(CH4_15_SPS_NOTDELAY[0])){
				System.out.println("[k]");
				serialPort.writeString(CH4_15_SPS_NOTDELAY[1]);
			}else if(chooseValSps.equals(CH4_5_SPS_45_MS[0])){
				System.out.println("[l]");
				serialPort.writeString(CH4_5_SPS_45_MS[1]);
			}

			System.out.println("||||||||| " + chooseValSps);
		} catch (SerialPortException ex) {
			Logger.getLogger(DynamicData.class.getName()).log(Level.SEVERE, null, ex);
		}catch (InterruptedException ex) {
			Logger.getLogger(DynamicData.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void setChoosesValuesSamplesPerSecond(int maxCh){
		switch (maxCh) {
			case 1:
				comboBox_chooserSamplesPerSecond.setModel(new javax.swing.DefaultComboBoxModel(new String[] {CH1_60_SPS_NOTDELAY[0], CH1_30_SPS_31_MS[0], CH1_5_SPS_190_MS[0]}));
				break;
			case 2:
				comboBox_chooserSamplesPerSecond.setModel(new javax.swing.DefaultComboBoxModel(new String[] {CH2_30_SPS_NOTDELAY[0], CH2_10_SPS_50_MS[0], CH2_5_SPS_100_MS[0]}));
				break;
			case 3:
				comboBox_chooserSamplesPerSecond.setModel(new javax.swing.DefaultComboBoxModel(new String[] {CH3_20_SPS_NOTDELAY[0], CH3_5_SPS_62_MS[0]}));
				break;
			case 4:
				comboBox_chooserSamplesPerSecond.setModel(new javax.swing.DefaultComboBoxModel(new String[] {CH4_15_SPS_NOTDELAY[0], CH4_5_SPS_45_MS[0]}));
				break;
		}
	}

	public  void uartInit(){
		//Передаём в конструктор имя порта
		serialPort = new SerialPort(comboBox_chooserCOMPort.getSelectedItem().toString());
		try {
			//Открываем порт
			serialPort.openPort();
			label_portState.setText(PORT_OPEN);
			label_portState.setForeground(new Color(50, 205, 50));
			//Выставляем параметры
			serialPort.setParams(SerialPort.BAUDRATE_9600,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);
			//Включаем аппаратное управление потоком (для FT232 нжуно отключать)
//            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | 
//                                          SerialPort.FLOWCONTROL_RTSCTS_OUT);
			//Устанавливаем ивент лисенер и маску
			serialPort.addEventListener(new PortReader(), SerialPort.MASK_RXCHAR);
			//Отправляем запрос устройству
//            serialPort.writeString("1"); //начинаем с канала 1
		}
		catch (SerialPortException ex) {
			System.out.println(ex);
			System.out.println("Порт закрыт");
			label_portState.setText(PORT_CLOSE);
			label_portState.setForeground(Color.red);
		}
	}

	int sc = 0;
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
//				series1.add(new Millisecond(), dataValueMcu);
								result.addChannel1Val(iArr[0]);
								//---------------------------------------------
//				Т.к.наш осц может выдавть на 1 канал мксимум
//				60 выборок/сек, но jFreeChart не может добавлять 
//				данные с такой частотой, поэтому в этом коде 
//				происходит добваление через раз. Это в итоге 
//				позволяет искуственно уменьшить добавление
//				данных до 30 в/с. Но завпись идет все равно
//				со скоростью в 60 в/с.
								if (sc == 1 && maxCh == 1) {
									series1.add(new Millisecond(), dataValueMcu);
									sc = 0;
								}else{
									sc ++;
								}
								if (maxCh > 1) {
									series1.add(new Millisecond(), dataValueMcu);
								}
								//---------------------------------------------
							}
							else if(chaneelCounter == 2){
								iArr = serialPort.readIntArray();
								dataValueMcu = iArr[0];
								series2.add(new Millisecond(), dataValueMcu);
								result.addChannel2Val(iArr[0]);
							}
							else if(chaneelCounter == 3){
								iArr = serialPort.readIntArray();
								dataValueMcu = iArr[0];
								series3.add(new Millisecond(), dataValueMcu);
								result.addChannel3Val(iArr[0]);
							}
							else if(chaneelCounter == 4){
								iArr = serialPort.readIntArray();
								dataValueMcu = iArr[0];
								series4.add(new Millisecond(), dataValueMcu);
								result.addChannel4Val(iArr[0]);
							}
							//и снова отправляем запрос для следующего канала
							if (chaneelCounter == 1) {
								if (maxCh == 1) {
									serialPort.writeString("1");
									chaneelCounter = 1;
								}
								else if (maxCh > 1) {
									serialPort.writeString("2");
									chaneelCounter = 2;
								}
							}
							else if (chaneelCounter == 2){
								if (maxCh == 2) {
									serialPort.writeString("1");
									chaneelCounter = 1;
								}else if (maxCh > 2) {
									serialPort.writeString("3");
									chaneelCounter = 3;
								}
							}
							else if (chaneelCounter == 3){
								if (maxCh == 3) {
									serialPort.writeString("1");
									chaneelCounter = 1;
								}else if (maxCh > 3) {
									serialPort.writeString("4");
									chaneelCounter = 4;
								}
							}
							else if (chaneelCounter == 4){
								if (maxCh == 4) {
									serialPort.writeString("1");
									chaneelCounter = 1;
								}
							}
						}
					}
					catch (SerialPortException ex) {
						System.out.println(ex);
						System.out.println("error  public void serialEvent(SerialPortEvent event)");
					}
				}
			}
		}
	}




}
