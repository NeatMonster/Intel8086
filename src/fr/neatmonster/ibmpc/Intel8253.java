package fr.neatmonster.ibmpc;

/**
 * The Intel 8253 is a programmable counter/timer device designed for use as an
 * Intel microcomputer peripheral. It uses NMOS technology with a single +5V
 * supply and is packaged in a 24-pin plastic DIP.
 *
 * It it organized as 3 independent 16-bit counters, each with a count rate of
 * up to 2.6 MHz. All modes of operation are software programmable.
 *
 * General
 *
 * The 8253 is a programmable interval timer/counter specifically designed for
 * use with the Intel Microcomputer systems. Its function is that of a general
 * purpose, multi-timing element that can be treated as an array of I/O ports in
 * the system software.
 *
 * The 8253 solves one of the most common problems in any microcomputer system,
 * the generation of accurate time delays under software control. Instead of
 * setting up timing loops in systems software, the programmer configures the
 * 8253 to match his requirements, initializes one of the counters of the 8253
 * with the desired quantity, then upon command the 8253 will count out the
 * delay and interrupt the CPU when it has completed its tasks. It is easy to
 * see that the software overhead is minimal and that multiple delays can easily
 * be maintained by assignment of priority levels.
 *
 * Other counter/timer functions that are non-delay in nature but also common to
 * most microcomputers can be implemented with the 8253.
 * - Programmable Rate Generator
 * - Event Counter
 * - Binary Rate Multiplier
 * - Real Time CLock
 * - Digital One-Shot
 * - Complex Motor Controller
 */
public class Intel8253 extends Thread {
    /** The last time counters were updated. */
    private long               last    = System.nanoTime();

    /** The actual value of each counter. */
    private volatile int[]     count   = new int[] { -1, -1, -1 };
    /** The initial value of each counter. */
    private volatile int[]     value   = new int[] { -1, -1, -1 };
    /** The latched value of each counter. */
    private volatile int[]     latch   = new int[] { -1, -1, -1 };
    /** The control word of each counter. */
    private volatile int[]     control = new int[3];
    /** The toggle for lsb, then msb reading. */
    private volatile boolean[] toggle  = new boolean[3];

    /**
     * Instantiates and starts the thread.
     */
    public Intel8253() {
        start();
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
        final int sc = port & 0b11;
        switch (sc) {
        case 0b00:
        case 0b01:
        case 0b10:
            // Read operation.
            final int rl = control[sc] >>> 4 & 0b11;
            // Use latch if set.
            int val = count[sc];
            if (latch[sc] > 0) {
                val = latch[sc];
                if (rl < 0b11 || !toggle[sc])
                    latch[sc] = -1;
            }
            switch (rl) {
            case 0b01: // Read least significant byte only.
                return val & 0xff;
            case 0b10: // Read most significant byte only.
                return val >>> 8 & 0xff;
            case 0b11: // Read lsb first, then msb.
                if (!toggle[sc]) {
                    toggle[sc] = true;
                    return val & 0xff;
                } else {
                    toggle[sc] = false;
                    return val >>> 8 & 0xff;
                }
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
    public void portOut(final int w, final int port, final int val) {
        int sc = port & 0b11;
        switch (sc) {
        case 0b00:
        case 0b01:
        case 0b10: {
            // Counter loading.
            final int rl = control[sc] >>> 4 & 0b11;
            switch (rl) {
            case 0b01: // Load least significant byte only.
                value[sc] = value[sc] & 0xff00 | (val > 0 ? val : 0);
                break;
            case 0b10: // Load most significant byte only.
                value[sc] = (val > 0 ? val : 0) << 8 | value[sc] & 0xff;
                break;
            case 0b11: // Load lsb first, then msb.
                if (!toggle[sc]) {
                    toggle[sc] = true;
                    value[sc] = value[sc] & 0xff00 | (val > 0 ? val : 0);
                } else {
                    toggle[sc] = false;
                    value[sc] = (val > 0 ? val : 0) << 8 | value[sc] & 0xff;
                }
                break;
            }
            if (rl < 0b11 || !toggle[sc])
                count[sc] = value[sc];
            break;
        }
        case 0b11:
            sc = val >>> 6 & 0b11;
            if ((val >>> 4 & 0b11) == 0b00)
                // Counter latching.
                latch[sc] = count[sc];
            else
                // Counter programming.
                control[sc] = val & 0xffff;
            break;
        }
    }

    /**
     * (non-Javadoc)
     *
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        long time;
        while (true) {
            time = System.nanoTime();
            // 1.193182 MHz - 838 ns
            if (time - last >= 838) {
                last = time;
                tick();
            }
        }
    }

    /**
     * Update all 3 counters.
     */
    public void tick() {
        for (int sc = 0b00; sc < 0b11; ++sc)
            if (count[sc] >= 0)
                switch (control[sc] >>> 1 & 0b111) {
                case 0b10:
                    /*
                     * Mode 2: Rate Generator
                     * 
                     * Divide by N counter. The output will be low for one
                     * period of the input clock. The period from one output
                     * pulse to the next equals the number of input counter in
                     * the count register. If the count register is reloaded
                     * between pulses the present period will not be affected,
                     * but the subsequent period will reflect the new value.
                     * 
                     * The gate input, when low, will force the output high.
                     * When the gate input goes high, the counter will start
                     * from the initial count. This, the gate input can be used
                     * to synchronize the counter.
                     * 
                     * When this mode is set; the output will remain high until
                     * after the count register is loaded. The output can also
                     * be synchronized by software.
                     */
                    count[sc] = --count[sc] & 0xffff;
                    if (count[sc] == 1)
                        count[sc] = value[sc];
                    break;
                }
    }
}
