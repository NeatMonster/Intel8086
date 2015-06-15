package fr.neatmonster.ibmpc;

/**
 * Motorola 6845 - Motorola 6845 - Cathode Ray Tube Controller
 *
 * The MC6845 CRT Controller performs the interface to raster scan CRT displays.
 * It is intended for use in processor-based controllers for CRT terminals in
 * stand-alone or cluster configurations.
 *
 * The CRTC is optimized for hardware/software balance in order to achieve
 * integration of all key functions and maintain flexibility. For instance, all
 * keyboard functions, R/W, cursor movements, and editing are under processor
 * control; whereas the CRTC provides video timing and Refresh Memory
 * Addressing.
 *
 * @author Alexandre ADAMSKI <alexandre.adamski@etu.enseeiht.fr>
 */
public class Motorola6845 implements Peripheral {
    /** The index of the register to access. */
    private int         index;
    /** The registers accessed by the CPU. */
    private final int[] registers = new int[0x10];
    /** Vertical/horizontal retracing. */
    private int         retrace;

    /**
     * Returns the value of the register with the specified index.
     *
     * @param index
     *            the index
     * @return the value
     */
    public int getRegister(final int index) {
        return registers[index];
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
        return port >= 0x3d0 && port < 0x3e0;
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
        switch (port) {
        case 0x3da:
            // Simulate vertical/horizontal retracing.
            retrace = ++retrace % 4;
            switch (retrace) {
            case 0:
                return 8; // VR started
            case 1:
                return 0; // VR ended
            case 2:
                return 1; // HR started
            case 3:
                return 0; // HR ended
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
        switch (port) {
        case 0x3d4: // Index
            index = val;
            break;
        case 0x3d5: // Register
            registers[index] = val;
            break;
        }
    }
}
