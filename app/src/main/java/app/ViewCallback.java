package app;

/**
 * What will be translated: controller -> UI
 * UI musts be implemented this interface
 *
 * @see View
 * @see Controller
 */
public interface ViewCallback {

    /**
     * Set COM port state in UI.
     * List port states in {@link app.USART.PortStates}
     * This set COM port state in UI label.
     *
     * @param portStates available USART port states lists
     *        in {@link app.USART.PortStates}
     */
    void setPortState(USART.PortStates portStates);

    /**
     * Add channel 1 point data to chart.
     *
     * @param channel1Point channel 1 point data
     */
    void addChannel1Point(int channel1Point);

    /**
     * Add channel 2 point data to chart.
     *
     * @param channel2Point channel 2 point data
     */
    void addChannel2Point(int channel2Point);

    /**
     * Add channel 3 point data to chart.
     *
     * @param channel3Point channel 3 point data
     */
    void addChannel3Point(int channel3Point);

    /**
     * Add channel 4 point data to chart.
     *
     * @param channel4Point channel 4 point data
     */
    void addChannel4Point(int channel4Point);
}
