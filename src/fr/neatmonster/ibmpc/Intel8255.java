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
     * Write output to the specified CPU port.
     *
     * @param w
     *            word/byte operation
     * @param port
     *            the port
     * @return the value
     */
    public int portIn(final int w, final int port) {
        switch (port) {
        case 0x60:
            return 0xc; // 64K of RAM
        case 0x62:
            return 0x0; // No I/O Channel RAM
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
    public void portOut(final int w, final int port, final int val) {}
}
