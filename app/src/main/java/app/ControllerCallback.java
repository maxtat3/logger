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
     * Controller must be set port state in UI.
     *
     * @param portStates available USART port states lists
     *        in {@link app.USART.PortStates}
     */
    void setPortState(USART.PortStates portStates);

    /**
     * Model translate received data from
     * USART COM port to Controller. In the Controller do processed
     * this received data and notify UI actions.
     * For example UI actions: changed port state, add point data to chart, etc.
     * Full notify actions see {@link ViewCallback} interface.
     * Model does not processed any received data from USART COM port.
     *
     * @param adcAtomicOnePointRes data and/or commands from USART COM port.
     */
    void addCOMPortData(int adcAtomicOnePointRes);
}
