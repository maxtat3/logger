package app;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Associates view and model.
 * When user change view state this controller update model.
 * When in model change data , he is update controller and he
 * update view elements.
 *
 * @see View
 * @see USART
 */
public class Controller implements ControllerCallback{

    // Commands channels selected to translate to COM port
    public static final String CH_1_CMD = String.valueOf(Channel.Channels.ONE.getNumber());
    public static final String CH_2_CMD = String.valueOf(Channel.Channels.TWO.getNumber());
    public static final String CH_3_CMD = String.valueOf(Channel.Channels.THREE.getNumber());
    public static final String CH_4_CMD = String.valueOf(Channel.Channels.FOUR.getNumber());
    // Numeric representation of channels
    public static final int CH_1_INT = Channel.Channels.ONE.getNumber();
    public static final int CH_2_INT = Channel.Channels.TWO.getNumber();
    public static final int CH_3_INT = Channel.Channels.THREE.getNumber();
    public static final int CH_4_INT = Channel.Channels.FOUR.getNumber();

    /**
     * Data model for communicate with device.
     * Created in this class.
     */
    private USART usart; //may be reorganize variables

    /**
     * Callback for UI.
     * Using it callback, Controller must be send data to UI.
     *
     * @see View
     * @see Controller
     */
    private ViewCallback viewCallback;

    /**
     * Calculate what channel is active in each time measure moment.
     * When sending the request to get data from COM port, this counter
     * is incremented value to 1 pointing to get data from next channel.
     * But limited to the maximum numbers of channels {@link #maxCh}
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
     *
     * @see #isRecord
     */
    private Result result = new Result();

    /**
     * Access to the channels and their samples per second.
     */
    private Channel channel = new Channel();


    public Controller(ViewCallback viewCallback) {
        this.viewCallback = viewCallback;
        usart = new USART(this);
    }

    /*
     * -------------------------------------------
     *      UI MUST BE CALLED NEXT PUBLIC METHODS
     * -------------------------------------------
     */

    /**
     * Turning on and do initialization USART module
     */
    public void turnOnUSART(String portName) { //todo - may be set return boolean result !?
        usart.init(portName);
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
     * @return true - is COM port closed
     */
    public boolean closeUSARTPort() {
        return usart.close();
    }

    /**
     * Send text to opened COM port.
     * @param text transmitted to open COM port text as String
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
    public void doRecord() { //todo simplify
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
            case 0: //todo change to channels const
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

    /**
     * Set samples per second for measure (sps) process.
     * Call this method closely related to {@link #setChannelsNum(int)} method.
     * When called setChannelsNum method, he are returned available variants
     * sample per seconds. For example fill checkbox. And the results are
     * transmitted in this method , which sends command to device.
     * This method must be called in UI.
     *
     * @param chooseValSps user selected sps value in UI
     *
     * @see #setChannelsNum(int)
     */
    public void setMCUSamplesPerSecond(String chooseValSps){//todo  "s" move to class
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
            Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
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

    /*
     * --------------------
     *   PRIVATE METHODS
     * --------------------
     */

    /**
     * Distributed of ADC data between channels.
     *
     * @param adcAtomicOnePoint ADC data one point of the one of channel.
     *                             From this points build a line on chart.
     */
    private void processReceivedADCData(int adcAtomicOnePoint) {
        if(channelCounter == CH_1_INT) {
            //possible activate accordance chart only one channel
            if (maxCh == CH_1_INT && accordanceChart(true)) {
                result.addChannel1Val(adcAtomicOnePoint);
                viewCallback.addChannel1Point(adcAtomicOnePoint);
            }

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
                sendString(CH_1_CMD);
                channelCounter = CH_1_INT;
            }
            else if (maxCh > CH_1_INT) {
                sendString(CH_2_CMD);
                channelCounter = CH_2_INT;
            }
        }
        else if (channelCounter == CH_2_INT){
            if (maxCh == CH_2_INT) {
                sendString(CH_1_CMD);
                channelCounter = CH_1_INT;
            }else if (maxCh > CH_2_INT) {
                sendString(CH_3_CMD);
                channelCounter = CH_3_INT;
            }
        }
        else if (channelCounter == CH_3_INT){
            if (maxCh == CH_3_INT) {
                sendString(CH_1_CMD);
                channelCounter = CH_1_INT;
            }else if (maxCh > CH_3_INT) {
                sendString(CH_4_CMD);
                channelCounter = CH_4_INT;
            }
        }
        else if (channelCounter == CH_4_INT){
            if (maxCh == CH_4_INT) {
                sendString(CH_1_CMD);
                channelCounter = CH_1_INT;
            }
        }
    }

}
