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
 *
 * @author Alexandre ADAMSKI <alexandre.adamski@etu.enseeiht.fr>
 */
public class Intel8255 implements Peripheral {
    /**
     * Intel 8259 - Programmable Interrupt Controller
     *
     * @see fr.neatmonster.ibmpc.Intel8259
     */
    private final Intel8259 pic;
    /**
     * 4 ports of the PIC (A, B, C and Control) as registers.
     */
    private final int[]     ports = new int[4];

    /**
     * Instantiate a new Intel 8255.
     *
     * @param pic
     *            the pic
     */
    public Intel8255(final Intel8259 pic) {
        this.pic = pic;
        ports[0] = 0x2c;
    }

    /**
     * Returns if a peripheral is connected to the specified port.
     *
     * @param port
     *            the port
     * @return true if connected, false else
     */
    @Override
    public boolean isConnected(final int port) {
        return port >= 0x60 && port < 0x64;
    }

    /**
     * Calls a keyboard interrupt for the specified scan code.
     *
     * @param scanCode
     *            the scan code
     */
    public void keyTyped(final int scanCode) {
        ports[0] = scanCode;
        pic.callIRQ(1);
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
    @Override
    public int portIn(final int w, final int port) {
        return ports[port & 0b11];
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
    @Override
    public void portOut(final int w, final int port, final int val) {
        ports[port & 0b11] = val;
    }
}
