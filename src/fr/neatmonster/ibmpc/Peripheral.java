package fr.neatmonster.ibmpc;

public interface Peripheral {

    /**
     * Returns if a peripheral is connected to the specified port.
     *
     * @param port
     *            the port
     * @return true if connected, false otherwise
     */
    public boolean isConnected(final int port);

    /**
     * Reads input from the specified port.
     *
     * @param w
     *            word/byte operation
     * @param port
     *            the port
     * @return the value
     */
    public int portIn(final int w, final int port);

    /**
     * Write output to the specified port.
     *
     * @param w
     *            word/byte operation
     * @param port
     *            the port
     * @param val
     *            the value
     */
    public void portOut(final int w, final int port, final int val);
}
