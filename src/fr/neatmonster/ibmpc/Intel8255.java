package fr.neatmonster.ibmpc;

/**
 * The Intel 8255 is a general purpose programmable I/O device designed for use
 * with Intel microprocessors. It has 24 I/O pins which may be individually
 * programmed in 2 groups of 12 and used in 3 major modes of operation. In the
 * first mode (MODE 0), each group of 12 I/O pins may be programmed in sets of 4
 * to be input or output. In MODE 1, the second mode, each group may be
 * programmed to have 8 lines of input or output. Of the remaining 4 pins, 3 are
 * used for handshaking and interrupt control signals. The third mode of
 * operation (MODE 2) is a bidirectional bus mode which uses 8 lines for a
 * bidirectional bus, and 5 lines, borrowing one from the other group, for
 * handshaking.
 */
public class Intel8255 {
    /**
     * Intel 8259 - Programmable Interrupt Controller
     *
     * @see fr.neatmonster.ibmpc.Intel8259
     */
    private final Intel8259 pic;

    /** Completion code from keyboard. */
    private int             keycode = -1;

    /**
     * Instantiate a new Intel 8255.
     *
     * @param pic
     *            the pic
     */
    public Intel8255(final Intel8259 pic) {
        this.pic = pic;
    }

    /**
     * Write output to the specified CPU port.
     *
     * @param w
     *            word/byte operation
     * @param port
     *            the port
     * @return the value
     */
    public int portIn(final int w, final int port) {
        if (port == 0x60 && keycode > 0) {
            // Keyboard reset
            final int code = keycode;
            keycode = -1;
            return code;
        }
        switch (port) {
        case 0x60: // PORT A
            // No B/W | No I/O RAM
            return 0x20 | 0xc;
        }
        return 0;
    }

    /**
     * Reads input from the specified CPU port.
     *
     * @param w
     *            word/byte operation
     * @param port
     *            the port
     * @param val
     *            the value
     */
    public void portOut(final int w, final int port, final int val) {
        switch (port) {
        case 0x61: // PORT B
            if (val == 0x4c) {
                // Keyboard reset
                keycode = 0xaa;
                pic.callIRQ(1);
            }
            break;
        }
    }
}
