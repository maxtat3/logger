package app;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.HighLowRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
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
import java.util.logging.Level;
import java.util.logging.Logger;


public class DynamicData extends ApplicationFrame implements ViewCallback{

	public static final  String PORT_CLOSE = "   Порт занят/закрыт ";
	public static final  String PORT_OPEN = "   Порт открыт ";
	public static final  String[] NUMBERS_OF_COM_PORTS = {
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


	private TimeSeries series1;
	private TimeSeries series2;
	private TimeSeries series3;
	private TimeSeries series4;


	public Label label_portState;
	public JComboBox comboBox_chooserCOMPort = new JComboBox();
	public JComboBox comboBox_chooserSamplesPerSecond = new JComboBox();
	public JComboBox comboBox_chooserChaneels = new JComboBox();









    static Class class$org$jfree$data$time$Millisecond; /* synthetic field */

    private Controller controller;



    public DynamicData (String s){
        super(s);
        DemoPanel demopanel = new DemoPanel();
        setContentPane(demopanel);
        controller = new Controller(this);
        //init USART
        controller.turnOnUSART(NUMBERS_OF_COM_PORTS[16]);
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
            label_portState.setText(PORT_OPEN);
            label_portState.setForeground(new Color(50, 205, 50));

        } else if (portStates == USART.PortStates.CLOSE) {
            System.out.println("port close");
            label_portState.setText(PORT_CLOSE);
            label_portState.setForeground(Color.red);
        }
    }

    @Override
    public void addChannel1Point(int channel1Point) {
        series1.add(new Millisecond(), channel1Point);
    }

//todo добавить bool isOpenPort. Эта переменная может применятся для блокировки UI если нет подключения к порту

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
			checkbox_record.setState(false); // record not set by default
			checkbox_record.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {

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
					if(controller.isOpenUSARTPort()){
                        controller.closeUSARTPort();
                        // todo - may be set delay
						controller.turnOnUSART(comboBox_chooserCOMPort.getSelectedItem().toString());
					}else{
						controller.turnOnUSART(comboBox_chooserCOMPort.getSelectedItem().toString());
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
                    String[] availableSPS = controller.setChannelsNum(comboBox_chooserChaneels.getSelectedIndex());
                    comboBox_chooserSamplesPerSecond.setModel(new javax.swing.DefaultComboBoxModel(availableSPS));
                }
			});

			//----------------------------------------------------------------
			//раскрывающейся список - выбор задержки между измерениями
			//т.е. количества выборок в секунду
			//----------------------------------------------------------------
//	    comboBox_chooserSamplesPerSecond.setModel(new javax.swing.DefaultComboBoxModel(SAMPLES_PER_SECOND));
//			setChoosesValuesSamplesPerSecond(comboBox_chooserChaneels.getSelectedIndex());
			comboBox_chooserSamplesPerSecond.setSelectedItem(0);
			comboBox_chooserSamplesPerSecond.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					controller.setMCUSamplesPerSecond((String)comboBox_chooserSamplesPerSecond.getSelectedItem());
//        		    System.out.println("sel item =      " + (String)comboBox_chooserSamplesPerSecond.getSelectedItem());
//        		    System.out.println("-----------");
				}
			});

            comboBox_chooserChaneels.setSelectedItem(0);
            comboBox_chooserSamplesPerSecond.setSelectedItem(0);


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
                controller.doStartStopMsr();
			}
		}

	} //------------------------------------ end class DemoModel






//	public void setChoosesValuesSamplesPerSecond(String[] samplePerSeconds){
//		switch (maxCh) {
//			case 1:
//				comboBox_chooserSamplesPerSecond.setModel(new javax.swing.DefaultComboBoxModel(new String[] {CH1_60_SPS_NOTDELAY[0], CH1_30_SPS_31_MS[0], CH1_5_SPS_190_MS[0]}));
//				break;
//			case 2:
//				comboBox_chooserSamplesPerSecond.setModel(new javax.swing.DefaultComboBoxModel(new String[] {CH2_30_SPS_NOTDELAY[0], CH2_10_SPS_50_MS[0], CH2_5_SPS_100_MS[0]}));
//				break;
//			case 3:
//				comboBox_chooserSamplesPerSecond.setModel(new javax.swing.DefaultComboBoxModel(new String[] {CH3_20_SPS_NOTDELAY[0], CH3_5_SPS_62_MS[0]}));
//				break;
//			case 4:
//				comboBox_chooserSamplesPerSecond.setModel(new javax.swing.DefaultComboBoxModel(new String[] {CH4_15_SPS_NOTDELAY[0], CH4_5_SPS_45_MS[0]}));
//				break;
//		}
//	}






}
