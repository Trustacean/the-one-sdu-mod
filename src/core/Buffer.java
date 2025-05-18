/* 
 * Copyright 2024 Bryan (HaiPigGi)
 */
package core;

import core.*;
import java.util.*;

public class Buffer {
    /**
     * Message buffer size - setting id {@value}
     */
    public static final String B_SIZE_S = "bufferSize";

    /**
     * Size of the buffer
     */
    private int bufferSize;

    /**
     * Get the size of the buffer for a specific DTNHost.
     * 
     * @param host The DTNHost for which to get the buffer size
     * @return The size of the buffer for the specified DTNHost
     */
    public int getBufferSize(DTNHost host) {
         bufferSize = Integer.MAX_VALUE;

        Settings s = new Settings(); // Get settings from the router of the host

        if (s.contains(B_SIZE_S)) {
            bufferSize = s.getInt(B_SIZE_S);
        }

        return bufferSize;
    }

     /**
     * Returns an iterator over the messages in this buffer.
     * 
     * @return an iterator over the messages in this buffer
     */
    public Iterator<Message> iterator() {
        // Assuming messages are stored in a list called 'messages'
        List<Message> messages = new ArrayList<>(); // Initialize this list with actual messages
        return messages.iterator();
    }

}
