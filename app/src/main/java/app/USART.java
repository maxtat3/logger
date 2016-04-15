package app;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

/**
 * Receive and translate data and command to device from
 * Universal Synchronously Asynchronously Receiver Translator.
 * Represents RS232 interface. This data transmitted to controller
 * and he update view elements.
 *
 * @see Controller
 */
public class USART {

    private SerialPort serialPort;

    /**
     * Callback for Controller.
     * Using it callback, model must be send data to Controller.
     *
     * @see Controller
     * @see USART
     */
    private ControllerCallback controllerCallback;


    public USART(ControllerCallback controllerCallback) {
        this.controllerCallback = controllerCallback;
    }


    /**
     * Possible USART COM port states
     */
    public enum PortStates {
        OPEN,
        CLOSE
    }


    /**
     * Checked COM port is open ?
     *
     * @return true if port open
     */
    public boolean isOpen() {
        return serialPort.isOpened();
    }

    /**
     * Do close open COM port.
     *
     * @return true if port normally closed
     */
    public boolean close() {
        try {
            return serialPort.closePort();
        } catch (SerialPortException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Write String to opened COM port
     *
     * @param str String writes to port
     */
    public void writeString(String str) {
        try {
            serialPort.writeString(str);
        } catch (SerialPortException e) {
            e.printStackTrace();
        }
    }

    /**
     * Init and open COM port
     *
     * @param portName opened port name
     */
    public void init(String portName){
        serialPort = new SerialPort(portName);
        try {
            serialPort.openPort();
//
            controllerCallback.setPortState(PortStates.OPEN);
            serialPort.setParams(SerialPort.BAUDRATE_9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            //Включаем аппаратное управление потоком (для FT232 нжуно отключать)
//            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN |
//                                          SerialPort.FLOWCONTROL_RTSCTS_OUT);
            serialPort.addEventListener(new PortReader(), SerialPort.MASK_RXCHAR);
        }
        catch (SerialPortException ex) {
            ex.printStackTrace();
            System.out.println("Порт закрыт");
            controllerCallback.setPortState(PortStates.CLOSE);
        }
    }

    private class PortReader implements SerialPortEventListener {
        @Override
        public void serialEvent(SerialPortEvent event) {
            synchronized(event){
                if(event.isRXCHAR() && event.getEventValue() > 0){
                    try {
                        controllerCallback.addCOMPortData(serialPort.readIntArray()[0]);
                    } catch (SerialPortException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
