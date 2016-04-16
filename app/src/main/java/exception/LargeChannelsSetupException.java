package exception;

import controller.Controller;

/**
 * Thrown when try to set a greater number of channels that can to accept handler.
 * Handler is set user selected channels in Controller by preparing to measure process.
 *
 * @see Controller#setChannelsNum(int)
 */
public class LargeChannelsSetupException extends Exception{

    /**
     * Constructs an <code>LargeChannelsSetupException</code> with not detail message.
     */
    public LargeChannelsSetupException() {
        super();
    }

    /**
     * Constructs an <code>LargeChannelsSetupException</code> with the specified detail message.
     *
     * @param message detail message.
     */
    public LargeChannelsSetupException(String message) {
        super(message);
    }
}
