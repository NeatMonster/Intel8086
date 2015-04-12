package fr.neatmonster.ibmpc;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * IBM Color/Graphics Monitor Adapter
 *
 * The IBM Color/Graphics Monitor Adapter is designed to attach to the IBM Color
 * Display, to a variety of television-frequency monitors, or to home television
 * sets (user-supplied RF modulator is required for home television sets). The
 * adapter is capable of operating in black-and-white or color. It provides
 * three video interfaces: a composite-video port, a direct-drive port, and a
 * connection interface for driving a user-supplied RF modulator. A light pen
 * interface is also provided.
 *
 * The adapter has two basic modes of operation: alphanumeric (A/N) and
 * all-points-addressable (APA) graphics. Additional modes are available within
 * the A/N or APA graphics modes. In the A/N mode, the display can be operated
 * in either a 40-column by 25-row mode for a low-resolution monitor or home
 * television, or in an 80-column by 25-row mode for high-resolution monitors.
 * In both modes, characters are defined in an 8-wide by 8-high character box
 * and are 7-wide by 7-high, double dotted characters with one descender. Both
 * uppercase and lowercase characters are supported in all modes.
 *
 * The character attributes of reverse video, blinking, and highlighting are
 * available in the black-and-white mode. In the color mode, 16 foreground and 8
 * background colors are available for each character. In addition, blinking on
 * a per-character basis is available.
 *
 * The monitor adapter contains 16K bytes of storage. As an example, a 40-column
 * by 25-row display screen uses 1000 bytes to store character information and
 * 1000 bytes to store attribute/color information. This means that up to eight
 * screens can be stored in the adapter memory. Similarly, in an 80-wide by
 * 25-row mode, four display screens can be stored in the adapter memory. The
 * entire 16K bytes of storage in the display adapter are directly accessible by
 * the processor, which allows maximum program flexibility in managing the
 * screen.
 *
 * In A/N color modes, it is also possible to select the color of the screen's
 * border. One of 16 colors can be selected.
 *
 * In the APA graphics mode, there are two resolutions available: a
 * medium-resolution color graphics mode (320 PELs by 200 rows) and a
 * high-resolution black-and-white graphics mode (640 PELs by 200 rows). In the
 * medium-resolution mode, each picture element (PEL) may have one of four
 * colors. The background color (Color 0) may be any of the 16 possible colors.
 * The remaining three colors come from one of the two program-selectable
 * palettes. One palette contains green/red/brown; the other contains
 * cyan/magenta/white.
 *
 * The high-resolution mode is available only in black-and-white because the
 * entire 16K bytes of storage in the adapter is used to define the on or off
 * state of the PELs.
 *
 * The adapter operates in noninterlace mode at either 7 or 14 MHz, depending on
 * the mode of the operation selected.
 *
 * In the A/N mode, characters are formed from a ROS character generator. The
 * character generator contains dot patterns for 256 different characters. The
 * character set contains the following major groupings of characters.
 * 16 special characters for game support
 *   - 15 characters for word-processing editing support
 *   - 96 characters for the standard ASCII graphics set
 *   - 48 characters for foreign-language support
 *   - 48 characters for business block-graphics support (for the drawing of
 *     charts, boxes, and tables using single and double lines)
 *   - 16 selected Greek characters
 *   - 15 selected scientific-notation characters
 *
 * The color/graphics monitor function is on a single adapter. The direct-drive
 * and composite-video ports are right-angle mounted connectors on the adapter,
 * and extend through the rear panel of the system unit. The direct-drive video
 * port is a 9-pin, D-shell, female connector. The composite-video port is a
 * standard female phono jack.
 *
 * The display adapter uses a Motorola 6845 CRT Controller device. This adapter
 * is highly programmable with respect to raster and character parameters.
 * Therefore, many additional modes are possible with programming of the
 * adapter.
 */
public class IBMCGA extends JPanel {
    /**
     * Lookup table for conversions from CP437 to Unicode code points.
     */
    private static final char[]  mapping = new char[] {
        0x0000, 0x0001, 0x0002, 0x0003, 0x0004, 0x0005, 0x0006, 0x0007,
        0x0008, 0x0009, 0x000a, 0x000b, 0x000c, 0x000d, 0x000e, 0x000f,
        0x0010, 0x0011, 0x0012, 0x0013, 0x0014, 0x0015, 0x0016, 0x0017,
        0x0018, 0x0019, 0x001a, 0x001b, 0x001c, 0x001d, 0x001e, 0x001f,
        0x0020, 0x0021, 0x0022, 0x0023, 0x0024, 0x0025, 0x0026, 0x0027,
        0x0028, 0x0029, 0x002a, 0x002b, 0x002c, 0x002d, 0x002e, 0x002f,
        0x0030, 0x0031, 0x0032, 0x0033, 0x0034, 0x0035, 0x0036, 0x0037,
        0x0038, 0x0039, 0x003a, 0x003b, 0x003c, 0x003d, 0x003e, 0x003f,
        0x0040, 0x0041, 0x0042, 0x0043, 0x0044, 0x0045, 0x0046, 0x0047,
        0x0048, 0x0049, 0x004a, 0x004b, 0x004c, 0x004d, 0x004e, 0x004f,
        0x0050, 0x0051, 0x0052, 0x0053, 0x0054, 0x0055, 0x0056, 0x0057,
        0x0058, 0x0059, 0x005a, 0x005b, 0x005c, 0x005d, 0x005e, 0x005f,
        0x0060, 0x0061, 0x0062, 0x0063, 0x0064, 0x0065, 0x0066, 0x0067,
        0x0068, 0x0069, 0x006a, 0x006b, 0x006c, 0x006d, 0x006e, 0x006f,
        0x0070, 0x0071, 0x0072, 0x0073, 0x0074, 0x0075, 0x0076, 0x0077,
        0x0078, 0x0079, 0x007a, 0x007b, 0x007c, 0x007d, 0x007e, 0x007f,
        0x00c7, 0x00fc, 0x00e9, 0x00e2, 0x00e4, 0x00e0, 0x00e5, 0x00e7,
        0x00ea, 0x00eb, 0x00e8, 0x00ef, 0x00ee, 0x00ec, 0x00c4, 0x00c5,
        0x00c9, 0x00e6, 0x00c6, 0x00f4, 0x00f6, 0x00f2, 0x00fb, 0x00f9,
        0x00ff, 0x00d6, 0x00dc, 0x00a2, 0x00a3, 0x00a5, 0x20a7, 0x0192,
        0x00e1, 0x00ed, 0x00f3, 0x00fa, 0x00f1, 0x00d1, 0x00aa, 0x00ba,
        0x00bf, 0x2310, 0x00ac, 0x00bd, 0x00bc, 0x00a1, 0x00ab, 0x00bb,
        0x2591, 0x2592, 0x2593, 0x2502, 0x2524, 0x2561, 0x2562, 0x2556,
        0x2555, 0x2563, 0x2551, 0x2557, 0x255d, 0x255c, 0x255b, 0x2510,
        0x2514, 0x2534, 0x252c, 0x251c, 0x2500, 0x253c, 0x255e, 0x255f,
        0x255a, 0x2554, 0x2569, 0x2566, 0x2560, 0x2550, 0x256c, 0x2567,
        0x2568, 0x2564, 0x2565, 0x2559, 0x2558, 0x2552, 0x2553, 0x256b,
        0x256a, 0x2518, 0x250c, 0x2588, 0x2584, 0x258c, 0x2590, 0x2580,
        0x03b1, 0x00df, 0x0393, 0x03c0, 0x03a3, 0x03c3, 0x00b5, 0x03c4,
        0x03a6, 0x0398, 0x03a9, 0x03b4, 0x221e, 0x03c6, 0x03b5, 0x2229,
        0x2261, 0x00b1, 0x2265, 0x2264, 0x2320, 0x2321, 0x00f7, 0x2248,
        0x00b0, 0x2219, 0x00b7, 0x221a, 0x207f, 0x00b2, 0x25a0, 0x00a0
    };
    /**
     * Lookup table for conversions between the 4-bit palette and RGB.
     */
    private static final Color[] colors  = new Color[] {
        new Color(0,     0,   0), new Color(  0,   0, 170),
        new Color(0,   170,   0), new Color(  0, 170, 170),
        new Color(170,   0,   0), new Color(170,   0, 170),
        new Color(170,  85,   0), new Color(170, 170, 170),
        new Color( 85,  85,  85), new Color( 85,  85, 255),
        new Color( 85, 255,  85), new Color( 85, 255, 255),
        new Color(255,  85,  85), new Color(255,  85, 255),
        new Color(255, 255,  85), new Color(255, 255, 255)
    };

    /**
     * Intel 8086 - Central Processing Unit
     *
     * @see fr.neatmonster.ibmpc.Intel8086
     */
    private final Intel8086      cpu;

    /**
     * Instantiates a new display.
     *
     * @param cpu
     *            the cpu
     */
    public IBMCGA(final Intel8086 cpu) {
        this.cpu = cpu;
        setPreferredSize(new Dimension(560, 300));
        try {
            // Use CP437 TrueType font.
            setFont(Font.createFont(Font.TRUETYPE_FONT, new File("cp437.ttf")).deriveFont(12f));
        } catch (final Exception e) {
            e.printStackTrace();
        }
        setBackground(Color.black);
        setForeground(Color.white);
        final JFrame frame = new JFrame();
        frame.requestFocus();
        frame.add(this);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        new Timer().scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                repaint();
            }
        }, 0, 1000 / 60); // Refresh at a 60 FPS rate.
    }

    /**
     * (non-Javadoc)
     *
     * @see javax.swing.JComponent#paintComponent(Graphics)
     */
    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        for (int y = 0; y < 25; ++y)
            for (int x = 0; x < 80; ++x) {
                final int character = cpu.memory[0xb8000 + 2 * (x + y * 80)];
                final int attribute = cpu.memory[0xb8000 + 2 * (x + y * 80) + 1];
                // Draw background first.
                g.setColor(colors[attribute >>> 4 & 0b111]);
                g.fillRect(x * 7, y * 12, 7, 12);
                // Then write foreground.
                g.setColor(colors[attribute & 0b1111]);
                g.drawString("" + mapping[character], x * 7, y * 12 + 9);
            }
    }
}
