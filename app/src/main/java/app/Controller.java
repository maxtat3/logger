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

    public static final String CHANNEL_1 = "1";
    public static final String CHANNEL_2 = "2";
    public static final String CHANNEL_3 = "3";
    public static final String CHANNEL_4 = "4";

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
            if (isRecord)
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
            sendString(CHANNEL_1);
            startStopAction = true;
        }

        channelCounter = 1; //reset at each new measure
    }

    /**
     * Do recording data to file in measure process.
     * true - yes, do recorded to file
     */
    private boolean isRecord = false;

    /**
     * Start or stop record measured channels data to file
     */
    public void doRecord() {
        if (isRecord) {
            isRecord = false;
        } else{
            isRecord = true;
        }
    }

    /**
     * User selected channels numbers for one measuring process.
     */
    private int maxCh = 4;

    /**
     * Set channels numbers was user selected.
     * This limits number channels request to get ADC data from COM port.
     * This method must be called in UI.
     *
     * @param channels numbers of channels user selected.
     *                 Channels numbering begins at 0.
     *                 Maximum channels processed in this method implementation = 4 (0 ... 3).
     * @return available variants sample per seconds to each user selected channel.
     */
    public String[] setChannelsNum(int channels) throws LargeChannelsSetupException {
        switch (channels) {
            case 0:
                maxCh = 1;
                return new String[]{CH1_60_SPS_NOTDELAY[0], CH1_30_SPS_31_MS[0], CH1_5_SPS_190_MS[0]};

            case 1:
                maxCh = 2;
                return new String[]{CH2_30_SPS_NOTDELAY[0], CH2_10_SPS_50_MS[0], CH2_5_SPS_100_MS[0]};

            case 2:
                maxCh = 3;
                return new String[]{CH3_20_SPS_NOTDELAY[0], CH3_5_SPS_62_MS[0]};

            case 3:
                maxCh = 4;
                return new String[]{CH4_15_SPS_NOTDELAY[0], CH4_5_SPS_45_MS[0]};

            default:
                throw new LargeChannelsSetupException();
        }
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

    /*
     * -------------------------------
     *   RETURNED VALUES FROM MODEL
     * -------------------------------
     */

    @Override
    public void setPortState(USART.PortStates portStates) {
        viewCallback.setPortState(portStates);
    }

    /**
     * Calculate what channel is active in each time measure moment.
     * When sending the request to get data from COM port, this counter
     * is incremented value to 1 pointing to get data from next channel,
     * but limited to the maximum numbers of channels {@link #maxCh}
     * that user has selected.
     * Default value 1, this means is 1 channel number pointing.
     */
    private int channelCounter = 1;

    @Override
    public void addCOMPortData(int adcAtomicOnePointRes) {
        if (startStopAction) {
            processReceivedADCData(adcAtomicOnePointRes);
            sendNextChannelRequest(); // So send request to get data next channel
        }
    }

    /**
     * Distributed of ADC data between channels.
     *
     * @param adcAtomicOnePoint ADC data one point of the one of channel.
     *                             From this points build a line on chart.
     */
    private void processReceivedADCData(int adcAtomicOnePoint) {
        if(channelCounter == 1) {
            //possible activate accordance chart only one channel
            if (maxCh == 1 && accordanceChart(true))
                viewCallback.addChannel1Point(adcAtomicOnePoint);
            result.addChannel1Val(adcAtomicOnePoint);

            if (maxCh > 1) {
                result.addChannel1Val(adcAtomicOnePoint);
                viewCallback.addChannel1Point(adcAtomicOnePoint);
            }
        }
        else if(channelCounter == 2){
            result.addChannel2Val(adcAtomicOnePoint);
            viewCallback.addChannel2Point(adcAtomicOnePoint);
        }
        else if(channelCounter == 3){
            result.addChannel3Val(adcAtomicOnePoint);
            viewCallback.addChannel3Point(adcAtomicOnePoint);
        }
        else if(channelCounter == 4){
            result.addChannel4Val(adcAtomicOnePoint);
            viewCallback.addChannel4Point(adcAtomicOnePoint);
        }
    }

    /**
     * Counter used ony for accordance chart by converted 60sps to 30sps by one channel worked.
     */
    private int accordanceCounter = 0;

    /**
     * Логгер может выдавть на 1 канал максимум 60 выборок/сек,
     * но jFreeChart иногда могут возникать проблемы с добавлением данных
     * на такой частоте, поэтому в этом методе происходит добваление точек
     * на график через раз. Это в итоге позволяет искуственно уменьшить
     * добавление данных до 30 в/с. Но завпись идет все равно со скоростью в 60 в/с.
     *
     * @param isTuneToChart true - делать подстройку под библиотеку графика.
     * @return true - добавлять данные на график, false - не добавлять. По умолчание true.
     */
    private boolean accordanceChart(boolean isTuneToChart) {
        if (isTuneToChart) {

            if (accordanceCounter == 0 ) { //add data to chart
                accordanceCounter = 1;
                return true;
            }else { //pass data
                accordanceCounter --;
                return false;
            }
        }
        return true;
    }

    /**
     * Send request to get ADC data conversion from next channel.
     */
    private void sendNextChannelRequest() {
        if (channelCounter == 1) {
            if (maxCh == 1) {
                usart.writeString(CHANNEL_1);
                channelCounter = 1;
            }
            else if (maxCh > 1) {
                usart.writeString(CHANNEL_2);
                channelCounter = 2;
            }
        }
        else if (channelCounter == 2){
            if (maxCh == 2) {
                usart.writeString(CHANNEL_1);
                channelCounter = 1;
            }else if (maxCh > 2) {
                usart.writeString(CHANNEL_3);
                channelCounter = 3;
            }
        }
        else if (channelCounter == 3){
            if (maxCh == 3) {
                usart.writeString(CHANNEL_1);
                channelCounter = 1;
            }else if (maxCh > 3) {
                usart.writeString(CHANNEL_4);
                channelCounter = 4;
            }
        }
        else if (channelCounter == 4){
            if (maxCh == 4) {
                usart.writeString(CHANNEL_1);
                channelCounter = 1;
            }
        }
    }

}
