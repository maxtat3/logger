package app;

/**
 * What will be translated: controller -> UI
 * UI musts be implemented this interface
 *
 * @see DynamicData
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
}
