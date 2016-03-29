package app;

/**
 * What will be translated: controller -> UI
 * UI musts be implemented this interface
 *
 * @see DynamicData
 * @see Controller
 */
public interface ViewCallback {

    void setPortState(USART.PortStates portStates);

    void addCh1Data(int adcData);
}
