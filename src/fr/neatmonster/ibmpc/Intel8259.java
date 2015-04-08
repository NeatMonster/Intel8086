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
     * Write output to the specified CPU port.
     *
     * @param w
     *            word/byte operation
     * @param port
     *            the port
     * @return the value
     */
    public int portIn(final int w, final int port) {
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
