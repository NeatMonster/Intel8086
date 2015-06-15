package fr.neatmonster.ibmpc;

/**
 * The 8237 Multimode Direct Memory Access (DMA) Controller is a peripheral
 * interface circuit for microprocessor systems. It is designed to improve
 * system performance by allowing external devices to directly transfer
 * information to or from the system memory. Memory-to-memory transfer
 * capability is also provided. The 8237 offers a wide variety of programmable
 * control features to enhance data throughput and system optimization and to
 * allow dynamic reconfiguration under program control.
 *
 * The 8237 is designed to be used in conjunction with an external 8-bit address
 * register such as the 8282. It contains four independent channels and may be
 * expanded to any number of channels by cascading additional controller chips.
 *
 * The thread basic transfer modes allow programmability of the type of DMA
 * service by the user. Each channel can be individually programmed to
 * Autoinitialize to its original condition following and End of Process (/EOP).
 *
 * Each channel has a full 64K address and word count capability.
 *
 * @author Alexandre ADAMSKI <alexandre.adamski@etu.enseeiht.fr>
 */
public class Intel8237 implements Peripheral {
    /*
     * Each channel has a pair of Base Address and Base Word Count registers.
     * These 16-bit registers store the original value of their associated
     * current registers. During Autoinitialize these values are used to restore
     * the current registers to their initial values. The base registers are
     * written simultaneously with their corresponding current registers in
     * 8-bit bytes in the Program Condition by the microprocessor. The registers
     * cannot be read by the microprocessor.
     */
    /** Base Address Register */
    private final int[]     addr     = new int[4];
    /** Word Count Register */
    private final int[]     cnt      = new int[4];
    /** The toggle for reading/writing. */
    private final boolean[] flipflop = new boolean[4];

    /**
     * Returns if a peripheral is connected to the specified port.
     *
     * @param port
     *            the port
     * @return true if connected, false else
     */
    @Override
    public boolean isConnected(final int port) {
        return port >= 0x00 && port < 0x20;
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
        int chan;
        switch (port) {
        case 0x00: // ADDR0
        case 0x02: // ADDR1
        case 0x04: // ADDR2
        case 0x06: // ADDR3
            chan = port / 2;
            if (!flipflop[chan]) {
                flipflop[chan] = true;
                return addr[chan] & 0xff;
            } else {
                flipflop[chan] = false;
                return addr[chan] >>> 8 & 0xff;
            }
        case 0x01: // CNT0
        case 0x03: // CNT1
        case 0x05: // CNT2
        case 0x07: // CNT3
            chan = (port - 1) / 2;
            if (!flipflop[chan]) {
                flipflop[chan] = true;
                return cnt[chan] & 0xff;
            } else {
                flipflop[chan] = false;
                return cnt[chan] >>> 8 & 0xff;
            }
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
    @Override
    public void portOut(final int w, final int port, final int val) {
        int chan;
        switch (port) {
        case 0x00: // ADDR0
        case 0x02: // ADDR1
        case 0x04: // ADDR2
        case 0x06: // ADDR3
            chan = port / 2;
            if (!flipflop[chan]) {
                flipflop[chan] = true;
                addr[chan] = addr[chan] & 0xff00 | val;
            } else {
                flipflop[chan] = false;
                addr[chan] = val << 8 | addr[chan] & 0xff;
            }
            break;
        case 0x01: // CNT0
        case 0x03: // CNT1
        case 0x05: // CNT2
        case 0x07: // CNT3
            chan = (port - 1) / 2;
            if (!flipflop[chan]) {
                flipflop[chan] = true;
                cnt[chan] = cnt[chan] & 0xff00 | val;
            } else {
                flipflop[chan] = false;
                cnt[chan] = val << 8 | cnt[chan] & 0xff;
            }
            break;
        }
    }
}
