package app;

/**
 * What will be translated: Model(USART) -> Controller.
 * In this interface list the methods called when the
 * Model {@link USART} transferred data to the Controller
 * {@link Controller} .
 * Controller must be implemented this interface
 *
 * @see Controller
 * @see USART
 */
public interface ControllerCallback {

    /**
     * Model inform Controller of the COM port state.
     *
     * @param portStates available USART port states lists
     *        in {@link app.USART.PortStates}
     */
    void setPortStateLabel(USART.PortStates portStates);

    void setADCData(int adcAtomicOnePointRes);
}
