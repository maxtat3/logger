package app;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class Controller implements ControllerCallback{

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

    private USART usart;
    private ViewCallback viewCallback;

    public Controller(ViewCallback viewCallback) {
        this.viewCallback = viewCallback;
        usart = new USART(this);
    }


    /**
     * Turning on and do initialization USART module
     */
    public void turnOnUSART(String portName) { //todo - may be set return boolean result !?
        usart.usartInit(portName);
    }


    /**
     * Check state - is open USART COM Port ?
     * @return true - COM port assigned in {@link Controller#turnOnUSART(String)} method open
     */
    public boolean isOpenUSARTPort() {
        return usart.isOpen();
    }

    /**
     * Close COM port
     * @return true - is COM port closed // todo is port closed , really returned true result  ?????
     */
    public boolean closeUSARTPort() {
        return usart.close();
    }

    /**
     * Send text to opened COM port.
     * @param text transmitted to port as String //todo подумать как правиально сформулировать !
     */
    public void sendString(String text) {
        usart.writeString(text);
    }


    private boolean startStopAction = false;
    private Result result = new Result();

    /**
     * Start or stop measure process
     */
    public void doStartStopMsr() {
        //стоп измерений
        if (startStopAction) {
            startStopAction = false;
            if (recordAction)
                try {
                    // 4 - may be dynamic change when user selected channel number
                    new Recorder().writeResultsToFile(
                            maxCh,
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
            sendString("1");
            startStopAction = true;
        }
    }

    private boolean recordAction = false;

    /**
     * Start or stop record measured channels data to file
     */
    public void doRecord() {
        if (recordAction) {
            recordAction = false;
        } else{
            recordAction = true;
        }
    }

    private int maxCh = 4;

    public String[] setChannelsNum(int channels) {
        switch (channels) {
            case 0:
                maxCh = 1;
//                setChoosesValuesSamplesPerSecond(maxCh);
//                setMCUSamplesPerSecond((String)comboBox_chooserSamplesPerSecond.getSelectedItem());
//                break;
                return new String[]{CH1_60_SPS_NOTDELAY[0], CH1_30_SPS_31_MS[0], CH1_5_SPS_190_MS[0]};
            case 1:
                maxCh = 2;
//                setChoosesValuesSamplesPerSecond(maxCh);
//                setMCUSamplesPerSecond((String)comboBox_chooserSamplesPerSecond.getSelectedItem());
//                break;
                return new String[]{CH2_30_SPS_NOTDELAY[0], CH2_10_SPS_50_MS[0], CH2_5_SPS_100_MS[0]};
            case 2:
                maxCh = 3;
//                setChoosesValuesSamplesPerSecond(maxCh);
//                setMCUSamplesPerSecond((String)comboBox_chooserSamplesPerSecond.getSelectedItem());
//                break;
                return new String[]{CH3_20_SPS_NOTDELAY[0], CH3_5_SPS_62_MS[0]};
            case 3:
                maxCh = 4;
//                setChoosesValuesSamplesPerSecond(maxCh);
//                setMCUSamplesPerSecond((String)comboBox_chooserSamplesPerSecond.getSelectedItem());
//                break;
                return new String[]{CH4_15_SPS_NOTDELAY[0], CH4_5_SPS_45_MS[0]};
            default:
                System.out.println("itemStateChanged > 4");
        }
        return null;
    }

    public void setMCUSamplesPerSecond(String chooseValSps){
        try {
            sendString("s");    //спец символ - установка mcu в режим выбора типа задержки
            Thread.sleep(100);

            if(chooseValSps.equals(CH1_60_SPS_NOTDELAY[0])){
                sendString(CH1_60_SPS_NOTDELAY[1]);
                System.out.println("[a]");
            }else if(chooseValSps.equals(CH1_30_SPS_31_MS[0])) {
                System.out.println("[b]");
                sendString(CH1_30_SPS_31_MS[1]);
            }else if(chooseValSps.equals(CH1_5_SPS_190_MS[0])){
                System.out.println("[c]");
                sendString(CH1_5_SPS_190_MS[1]);

            }else if(chooseValSps.equals(CH2_30_SPS_NOTDELAY[0])){
                System.out.println("[d]");
                sendString(CH2_30_SPS_NOTDELAY[1]);
            }else if(chooseValSps.equals(CH2_10_SPS_50_MS[0])){
                System.out.println("[e]");
                sendString(CH2_10_SPS_50_MS[1]);
            }else if(chooseValSps.equals(CH2_5_SPS_100_MS[0])){
                System.out.println("[f]");
                sendString(CH2_5_SPS_100_MS[1]);

            }else if(chooseValSps.equals(CH3_20_SPS_NOTDELAY[0])){
                System.out.println("[g]");
                sendString(CH3_20_SPS_NOTDELAY[1]);
            }else if(chooseValSps.equals(CH3_5_SPS_62_MS[0])){
                System.out.println("[h]");
                sendString(CH3_5_SPS_62_MS[1]);

            }else if(chooseValSps.equals(CH4_15_SPS_NOTDELAY[0])){
                System.out.println("[k]");
                sendString(CH4_15_SPS_NOTDELAY[1]);
            }else if(chooseValSps.equals(CH4_5_SPS_45_MS[0])){
                System.out.println("[l]");
                sendString(CH4_5_SPS_45_MS[1]);
            }

            System.out.println("||||||||| " + chooseValSps);
        }catch (InterruptedException ex) {
            Logger.getLogger(DynamicData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

//    public void setChoosesValuesSamplesPerSecond(int maxCh){
//        switch (maxCh) {
//            case 1:
//                comboBox_chooserSamplesPerSecond.setModel(new javax.swing.DefaultComboBoxModel(new String[] {CH1_60_SPS_NOTDELAY[0], CH1_30_SPS_31_MS[0], CH1_5_SPS_190_MS[0]}));
//                break;
//            case 2:
//                comboBox_chooserSamplesPerSecond.setModel(new javax.swing.DefaultComboBoxModel(new String[] {CH2_30_SPS_NOTDELAY[0], CH2_10_SPS_50_MS[0], CH2_5_SPS_100_MS[0]}));
//                break;
//            case 3:
//                comboBox_chooserSamplesPerSecond.setModel(new javax.swing.DefaultComboBoxModel(new String[] {CH3_20_SPS_NOTDELAY[0], CH3_5_SPS_62_MS[0]}));
//                break;
//            case 4:
//                comboBox_chooserSamplesPerSecond.setModel(new javax.swing.DefaultComboBoxModel(new String[] {CH4_15_SPS_NOTDELAY[0], CH4_5_SPS_45_MS[0]}));
//                break;
//        }
//    }

    /*
     * -------------------------------
     *   RETURNED VALUES FROM MODEL
     * -------------------------------
     */

    @Override
    public void setPortState(USART.PortStates portStates) {
        viewCallback.setPortState(portStates);
    }

    public  int chaneelCounter = 1;
    private double  dataValueMcu = 0;
    int sc = 0;

    @Override
    public void addCOMPortData(int adcAtomicOnePointRes) {
//        System.out.println("event.isRXCHAR() && event.getEventValue() > 0   is ok");
        if (startStopAction) {
            if(chaneelCounter == 1) {
//				series1.add(new Millisecond(), dataValueMcu);
                result.addChannel1Val(adcAtomicOnePointRes);
                viewCallback.addChannel1Point(adcAtomicOnePointRes);
                //---------------------------------------------
//				Т.к.наш осц может выдавть на 1 канал мксимум
//				60 выборок/сек, но jFreeChart не может добавлять
//				данные с такой частотой, поэтому в этом коде
//				происходит добваление через раз. Это в итоге
//				позволяет искуственно уменьшить добавление
//				данных до 30 в/с. Но завпись идет все равно
//				со скоростью в 60 в/с.
                if (sc == 1 && maxCh == 1) {
//                        series1.add(new Millisecond(), dataValueMcu);
//                    System.out.println("adc ch 1 = " + dataValueMcu); // by logging
                    sc = 0;
                }else{
                    sc ++;
                }
                if (maxCh > 1) {
//                        series1.add(new Millisecond(), dataValueMcu);
//                    System.out.println("adc ch 1 = " + dataValueMcu); // by logging
                }
                //---------------------------------------------
            }
//                else if(chaneelCounter == 2){
//                    iArr = serialPort.readIntArray();
//                    dataValueMcu = iArr[0];
//                    series2.add(new Millisecond(), dataValueMcu);
//                    result.addChannel2Val(iArr[0]);
//                }
//                else if(chaneelCounter == 3){
//                    iArr = serialPort.readIntArray();
//                    dataValueMcu = iArr[0];
//                    series3.add(new Millisecond(), dataValueMcu);
//                    result.addChannel3Val(iArr[0]);
//                }
//                else if(chaneelCounter == 4){
//                    iArr = serialPort.readIntArray();
//                    dataValueMcu = iArr[0];
//                    series4.add(new Millisecond(), dataValueMcu);
//                    result.addChannel4Val(iArr[0]);
//                }
            //и снова отправляем запрос для следующего канала
            if (chaneelCounter == 1) {
                if (maxCh == 1) {
                    usart.writeString("1");
                    chaneelCounter = 1;
                }
                else if (maxCh > 1) {
                    usart.writeString("2");
                    chaneelCounter = 2;
                }
            }
            else if (chaneelCounter == 2){
                if (maxCh == 2) {
                    usart.writeString("1");
                    chaneelCounter = 1;
                }else if (maxCh > 2) {
                    usart.writeString("3");
                    chaneelCounter = 3;
                }
            }
            else if (chaneelCounter == 3){
                if (maxCh == 3) {
                    usart.writeString("1");
                    chaneelCounter = 1;
                }else if (maxCh > 3) {
                    usart.writeString("4");
                    chaneelCounter = 4;
                }
            }
            else if (chaneelCounter == 4){
                if (maxCh == 4) {
                    usart.writeString("1");
                    chaneelCounter = 1;
                }
            }
        }
    }

}
