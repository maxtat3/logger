package exception;

import utils.Recorder;

/**
 * Thrown to indicate that a transferred more then the numbers of measure
 * channels when can be written Recorder
 *
 * @see Recorder
 */
public class LargeChannelsRecordException extends Exception{

    /**
     * Constructs an <code>LargeChannelsRecordException</code> with not detail message.
     */
    public LargeChannelsRecordException() {
        super();
    }

    /**
     * Constructs an <code>LargeChannelsRecordException</code> with the specified detail message.
     *
     * @param message detail message.
     */
    public LargeChannelsRecordException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message detail message (which is saved for later retrieval
     *         by the {@link Throwable#getMessage()} method).
     * @param cause cause (which is saved for later retrieval by the
     *         {@link Throwable#getCause()} method).  A <tt>null</tt> value
     *         is permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public LargeChannelsRecordException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified cause.
     *
     * @param cause cause (which is saved for later retrieval by the
     *         {@link Throwable#getCause()} method).  A <tt>null</tt> value
     *         is permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public LargeChannelsRecordException(Throwable cause) {
        super(cause);
    }
}
