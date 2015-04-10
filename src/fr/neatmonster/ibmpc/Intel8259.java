package fr.neatmonster.ibmpc;

/**
 * The Intel 8259 Programmable Interrupt Controller handles up to eight vectored
 * priority interrupts for the CPU. It is cascadable for up to 64 vectored
 * priority interrupts without additional circuitry. It is packaged in a 28-pin
 * DIP, used NMOS technology and requires a single +5V supply. Circuitry is
 * static, requiring no clock input.
 *
 * The 8259 is designed to minimize software and real time overhead in handling
 * multi-level priority interrupts. It has several modes, permitting
 * optimization for a variety of system requirements.
 */
public class Intel8259 {
    /**
     * Interrupt Mask Register (IMR)
     *
     * 8-bit register which contains the interrupt request lines which are
     * masked.
     */
    private volatile int   imr;

    /**
     * Interrupt Request Register (IRR)
     *
     * 8-bit register which contains the levels requesting an interrupt to be
     * acknowledged. The highest request level is reset from the IRR when an
     * interrupt is acknowledged (not affected by IMR).
     */
    private volatile int   irr;

    /**
     * In-Service Register (ISR)
     *
     * 8-bit register which contains the priority levels that are being
     * serviced. The ISR is updated when an End of Interrupt Command is issued.
     */
    private volatile int   isr;

    /**
     * Initialization Command Words (ICWS)
     *
     * Whenever a command is issued with A0 = 0 and D4 = 1, this is
     * interpreted
     * as Initialization Command Word 1 (ICW1). ICW1 starts the initialization
     * sequence during which the following automatically occur:
     * (a) The edge sense circuit is reset which means that following
     *     initialization an interrupt request (IR) in- put must make a
     *     low-to-high transition to generate an interrupt.
     * (b) The Interrupt Mask Register is cleared.
     * (c) IR7 input is assigned priority 7.
     * (d) The slave mode address is set to 7.
     * (e) Special Mask Mode is cleared and Status Read is set to IRR.
     * (f) If IC4 = 0, then all functions selected in ICW4 are set to zero.
     */
    private volatile int[] icw     = new int[4];
    /** Keeps track of initialization progress. */
    private volatile int   icwStep = 0;

    /**
     * Call an interruption request on the specified line.
     *
     * @param line
     *            the line
     */
    public void callIRQ(final int line) {
        irr |= 1 << line;
    }

    /**
     * Returns if an interrupt request is waiting to be serviced.
     *
     * @return true if there is one, false otherwise
     */
    public boolean hasInt() {
        return (irr & ~imr) > 0;
    }

    /**
     * Returns the type of the interrupt request waiting to be serviced.
     *
     * @return the interrupt-type
     */
    public int nextInt() {
        final int bits = irr & ~imr;
        for (int i = 0; i < 8; ++i) {
            if ((bits >>> i & 0b1) > 0) {
                irr ^= 1 << i;
                isr |= 1 << i;
                return icw[2] + i;
            }
        }
        return 0;
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
        switch (port) {
        case 0x20:
            return irr;
        case 0x21:
            return imr;
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
        case 0x20:
            if ((val & 0x10) > 0) {
                imr = 0;
                icw[icwStep++] = val;
            }
            break;
        case 0x21:
            if (icwStep == 1) {
                icw[icwStep++] = val;
                if ((icw[0] & 0x02) > 0)
                    ++icwStep;
            } else if (icwStep < 4)
                icw[icwStep++] = val;
            else
                imr = val;
            break;
        }
    }
}
