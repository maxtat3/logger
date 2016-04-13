package app;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class Controller implements ControllerCallback{

    //todo - may be pack to inner class and moved to model. Think about it.
    // Commands channels selected to translate to COM port
    public static final String CH_1_CMD = "1";
    public static final String CH_2_CMD = "2";
    public static final String CH_3_CMD = "3";
    public static final String CH_4_CMD = "4";
    // Numeric representation of channels
    // All
    public static final int CH_1_INT = 1;
    public static final int CH_2_INT = 2;
    public static final int CH_3_INT = 3;
    public static final int CH_4_INT = 4;

    private USART usart;
    private ViewCallback viewCallback;

    /**
     * Calculate what channel is active in each time measure moment.
     * When sending the request to get data from COM port, this counter
     * is incremented value to 1 pointing to get data from next channel,
     * but limited to the maximum numbers of channels {@link #maxCh}
     * that user has selected.
     * Default value 1, this means is 1 channel number pointing.
     */
    private int channelCounter = CH_1_INT;

    /**
     * User selected channels numbers for one measuring process.
     */
    private int maxCh = 4;

    /**
     * Do recording data to file in measure process.
     * true - yes, do recorded to file
     */
    private boolean isRecord = false;

    /**
     * Is started measure process action.
     * true - process started.
     */
    private boolean isStartAction = false;

    /**
     * Result object created when user selected record to file.
     * @see #isRecord
     */
    private Result result = new Result();

    private Channel channel = new Channel();


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

    /**
     * Start or stop measure process
     */
    public void doStartStopMsr() {
        //стоп измерений
        if (isStartAction) {
            isStartAction = false;
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
            sendString(CH_1_CMD);
            isStartAction = true;
        }

        channelCounter = CH_1_INT; //reset at each new measure
    }

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
                maxCh = CH_1_INT;
                return new String[]{
                        channel.getCh1And60sps().getSpsName(),
                        channel.getCh1And30sps().getSpsName(),
                        channel.getCh1And5sps().getSpsName(),
                };

            case 1:
                maxCh = CH_2_INT;
                return new String[]{
                        channel.getCh2And30sps().getSpsName(),
                        channel.getCh2And10sps().getSpsName(),
                        channel.getCh2And5sps().getSpsName()
                };

            case 2:
                maxCh = CH_3_INT;
                return new String[]{
                        channel.getCh3And20sps().getSpsName(),
                        channel.getCh3And5sps().getSpsName()
                };

            case 3:
                maxCh = CH_4_INT;
                return new String[]{
                        channel.getCh4And15sps().getSpsName(),
                        channel.getCh4And5sps().getSpsName()
                };

            default:
                throw new LargeChannelsSetupException();
        }
    }

    public void setMCUSamplesPerSecond(String chooseValSps){
        try {
            sendString("s");    //спец символ - установка mcu в режим выбора типа задержки
            Thread.sleep(100);

            if(chooseValSps.equals(channel.getCh1And60sps().getSpsName())){
                sendString(channel.getCh1And60sps().getCmd());
            }else if(chooseValSps.equals(channel.getCh1And30sps().getSpsName())) {
                sendString(channel.getCh1And30sps().getCmd());
            }else if(chooseValSps.equals(channel.getCh1And5sps().getSpsName())){
                sendString(channel.getCh1And5sps().getCmd());

            }else if(chooseValSps.equals(channel.getCh2And30sps().getSpsName())){
                sendString(channel.getCh2And30sps().getCmd());
            }else if(chooseValSps.equals(channel.getCh2And10sps().getSpsName())){
                sendString(channel.getCh2And10sps().getCmd());
            }else if(chooseValSps.equals(channel.getCh2And5sps().getSpsName())){
                sendString(channel.getCh2And5sps().getCmd());

            }else if(chooseValSps.equals(channel.getCh3And20sps().getSpsName())){
                sendString(channel.getCh3And20sps().getCmd());
            }else if(chooseValSps.equals(channel.getCh3And5sps().getSpsName())){
                sendString(channel.getCh3And5sps().getCmd());

            }else if(chooseValSps.equals(channel.getCh4And15sps().getSpsName())){
                sendString(channel.getCh4And15sps().getCmd());
            }else if(chooseValSps.equals(channel.getCh4And5sps().getSpsName())){
                sendString(channel.getCh4And5sps().getCmd());
            }

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

    @Override
    public void addCOMPortData(int adcAtomicOnePointRes) {
        if (isStartAction) {
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
        if(channelCounter == CH_1_INT) {
            //possible activate accordance chart only one channel
            if (maxCh == CH_1_INT && accordanceChart(true))
                viewCallback.addChannel1Point(adcAtomicOnePoint);
            result.addChannel1Val(adcAtomicOnePoint);

            if (maxCh > CH_1_INT) {
                result.addChannel1Val(adcAtomicOnePoint);
                viewCallback.addChannel1Point(adcAtomicOnePoint);
            }
        }
        else if(channelCounter == CH_2_INT){
            result.addChannel2Val(adcAtomicOnePoint);
            viewCallback.addChannel2Point(adcAtomicOnePoint);
        }
        else if(channelCounter == CH_3_INT){
            result.addChannel3Val(adcAtomicOnePoint);
            viewCallback.addChannel3Point(adcAtomicOnePoint);
        }
        else if(channelCounter == CH_4_INT){
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
        if (channelCounter == CH_1_INT) {
            if (maxCh == CH_1_INT) {
                usart.writeString(CH_1_CMD);
                channelCounter = CH_1_INT;
            }
            else if (maxCh > CH_1_INT) {
                usart.writeString(CH_2_CMD);
                channelCounter = CH_2_INT;
            }
        }
        else if (channelCounter == CH_2_INT){
            if (maxCh == CH_2_INT) {
                usart.writeString(CH_1_CMD);
                channelCounter = CH_1_INT;
            }else if (maxCh > CH_2_INT) {
                usart.writeString(CH_3_CMD);
                channelCounter = 3;
            }
        }
        else if (channelCounter == CH_3_INT){
            if (maxCh == CH_3_INT) {
                usart.writeString(CH_1_CMD);
                channelCounter = CH_1_INT;
            }else if (maxCh > CH_3_INT) {
                usart.writeString(CH_4_CMD);
                channelCounter = CH_4_INT;
            }
        }
        else if (channelCounter == CH_4_INT){
            if (maxCh == CH_4_INT) {
                usart.writeString(CH_1_CMD);
                channelCounter = CH_1_INT;
            }
        }
    }

}
