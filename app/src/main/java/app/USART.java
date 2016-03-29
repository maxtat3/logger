package app;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

/**
 * Receive and translate data and command to device from
 * Universal Synchronously Asynchronously Receiver Translator.
 * Represents RS232 interface.
 */
public class USART {

    private SerialPort serialPort;

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


    public boolean isOpen() {
        return serialPort.isOpened();
    }

    public boolean close() {
        try {
            return serialPort.closePort();
        } catch (SerialPortException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void writeString(String str) {
        try {
            serialPort.writeString(str);
        } catch (SerialPortException e) {
            e.printStackTrace();
        }
    }

    public void usartInit(String portName){
        //Передаём в конструктор имя порта
        serialPort = new SerialPort(portName);
        try {
            //Открываем порт
            serialPort.openPort();
//
            controllerCallback.setPortStateLabel(PortStates.OPEN);
            //Выставляем параметры
            serialPort.setParams(SerialPort.BAUDRATE_9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            //Включаем аппаратное управление потоком (для FT232 нжуно отключать)
//            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN |
//                                          SerialPort.FLOWCONTROL_RTSCTS_OUT);
            //Устанавливаем ивент лисенер и маску
            serialPort.addEventListener(new PortReader(), SerialPort.MASK_RXCHAR);
            //Отправляем запрос устройству
//            serialPort.writeString("1"); //начинаем с канала 1
        }
        catch (SerialPortException ex) {
            ex.printStackTrace();
            System.out.println("Порт закрыт");
            controllerCallback.setPortStateLabel(PortStates.CLOSE);
        }
    }

    int sc = 0;
    private class PortReader implements SerialPortEventListener {
        @Override
        public void serialEvent(SerialPortEvent event) {
            synchronized(event){
                if(event.isRXCHAR() && event.getEventValue() > 0){
                    try {
                        controllerCallback.setADCData(serialPort.readIntArray()[0]);
                    } catch (SerialPortException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
