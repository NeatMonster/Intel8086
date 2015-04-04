package fr.neatmonster.intel8086;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * The 8086 CPU is characterized by:
 * - a standard operating speed of 5 MHz (200 ns a cycle time).
 * - chips housed in reliable 40-pin packages.
 * - a processor that operates on both 8- and 16-bit data types.
 * - up to 1 megabyte of memory that can be addressed, along with a separate
 * 64k byte I/O space.
 *
 * CPU Architecture
 *
 * CPU incorporates two separate processing units; the Execution Unit or "EU"
 * and the Bus Interface Unit or "BIU". The BIU for the 8086 incorporates a
 * 16-bit data bus and a 6-byte instruction queue.
 *
 * The EU is responsible for the execution of all instructions, for providing
 * data and addresses to the BIU, and for manipulating the general registers
 * and the flag register. Except for a few control pins, the EU is completely
 * isolated from the "outside world." The BIU is responsible for executing all
 * external bus cycles and consists of the segment and communication registers,
 * the instruction pointer and the instruction object code queue. The BIU
 * combines segment and offset values in its dedicated adder to derive 20-bit
 * addresses, transfers data to and from the EU on the ALU data bus and loads
 * or "prefetches" instructions into the queue from which they are fetched by
 * the EU.
 *
 * The EU, when it is ready to execute an instruction, fetches the instruction
 * object code byte from the BIU's instruction queue and then executes the
 * instruction. If the queue is empty when the EU is ready to fetch an
 * instruction byte, the EU waits for the instruction byte to be fetched. In
 * the course of instruction execution, if a memory location or I/O port must
 * be accessed, the EU requests the BIU to perform the required bus cycle.
 *
 * The two processing sections of the CPU operate independently. In the 8086
 * CPU, when two or more bytes of the 6-byte instruction queue are empty and
 * the EU does not require the BIU to perform a bus cycle, the BIU executes
 * instruction fetch cycles to refill the queue. Note that the 8086 CPU, since
 * it has a 16-bit data bus, can access two instructions object code bytes in a
 * single bus cycle. If the EU issues a request for bus access while the BIU is
 * in the process of an instruction fetch bus cycle, the BIU completes the
 * cycle before honoring the EU's request.
 */
public class Intel8086 {
    /**
     * Entry point. For now it executes a little test program.
     */
    public static void main(final String[] args) {
        // Instantiate a new CPU.
        final Intel8086 cpu = new Intel8086();
        // Reset the CPU.
        cpu.reset();
        try {
            // Try reading test file.
            final byte[] src = Files.readAllBytes(Paths.get("codegolf"));
            // Convert bytes to integers.
            final int[] instrs = new int[src.length];
            for (int i = 0; i < src.length; ++i)
                instrs[i] = src[i];
            // Load instructions.
            cpu.load(instrs);
            // Execute all instructions.
            cpu.run();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns if the most significant byte of a value is set.
     *
     * @param w
     *            word/byte operation
     * @param x
     *            the value
     * @return true if MSB is set, false if it is cleared
     */
    private static boolean msb(final int w, final int x) {
        return (x & SIGN[w]) == SIGN[w];
    }

    /**
     * Shifts left a value by a given number of positions, or right if that
     * number is negative.
     *
     * @param x
     *            the value
     * @param n
     *            the number of positions
     * @return the new value
     */
    private static int shift(final int x, final int n) {
        return n >= 0 ? x << n : x >>> -n;
    }

    /**
     * Converts an unsigned value to a signed value.
     *
     * @param w
     *            word/byte operation
     * @param x
     *            the value
     * @return the new value
     */
    private static int signconv(final int w, final int x) {
        return x << 32 - BITS[w] >> 32 - BITS[w];
    }

    /**
     * CF (carry flag)
     *
     * If an addition results in a carry out of the high-order bit of the
     * result, then CF is set; otherwise CF is cleared. If a subtraction
     * results in a borrow into the high-order bit of the result, then CF is
     * set; otherwise CF is cleared. Note that a signed carry is indicated by
     * CF ≠ OF. CF can be used to detect an unsigned overflow. Two
     * instructions, ADC (add with carry) and SBB (subtract with borrow),
     * incorporate the carry flag in their operations and can be used to
     * perform multibyte (e.g., 32-bit, 64-bit) addition and subtraction.
     */
    private static final int   CF     = 1 << 0;

    /**
     * PF (parity flag)
     *
     * If the low-order eight bits of an arithmetic or logical operation is
     * zero contain an even number of 1-bits, then the parity flag is set,
     * otherwise it is cleared. PF is provided for 8080/8085 compatibility; it
     * can also be used to check ASCII characters for correct parity.
     */
    private static final int   PF     = 1 << 2;

    /**
     * AF (auxiliary carry flag)
     *
     * If an addition results in a carry out of the low-order half-byte of the
     * result, then AF is set; otherwise AF is cleared. If a subtraction
     * results in a borrow into the low-order half-byte of the result, then AF
     * is set; otherwise AF is cleared. The auxiliary carry flag is provided
     * for the decimal adjust instructions and ordinarily is not used for any
     * other purpose.
     */
    private static final int   AF     = 1 << 4;

    /**
     * ZF (zero flag)
     *
     * If the result of an arithmetic or logical operation is zero, then ZF is
     * set; otherwise ZF is cleared. A conditional jump instruction can be used
     * to alter the flow of the program if the result is or is not zero.
     */
    private static final int   ZF     = 1 << 6;

    /**
     * SF (sign flag)
     *
     * Arithmetic and logical instructions set the sign flag equal to the
     * high-order bit (bit 7 or 15) of the result. For signed binary numbers,
     * the sign flag will be 0 for positive results and 1 for negative results
     * (so long as overflow does not occur). A conditional jump instruction can
     * be used following addition or subtraction to alter the flow of the
     * program depending on the sign of the result. Programs performing
     * unsigned operations typically ignore SF since the high-order bit of the
     * result is interpreted as a digit rather than a sign.
     */
    private static final int   SF     = 1 << 7;

    /**
     * TF (trap flag)
     *
     * Settings TF puts the processor into single-step mode for debugging. In
     * this mode, the CPU automatically generates an internal interrupt after
     * each instruction, allowing a program to be inspected as it executes
     * instruction by instruction.
     */
    private static final int   TF     = 1 << 8;

    /**
     * IF (interrupt-enable flag)
     *
     * Setting IF allows the CPU to recognize external (maskable) interrupt
     * requests. Clearing IF disables these interrupts. IF has no affect on
     * either non-maskable external or internally generated interrupts.
     */
    private static final int   IF     = 1 << 9;

    /**
     * DF (direction flag)
     *
     * Setting DF causes string instructions to auto-decrement; that is, to
     * process strings from the high addresses to low addresses, or from "right
     * to left". Clearing DF causes string instructions to auto-increment, or
     * to process strings from "left to right."
     */
    private static final int   DF     = 1 << 10;

    /**
     * OF (overflow flag)
     *
     * If the result of an operation is too large a positive number, or too
     * small a negative number to fit in the destination operand (excluding the
     * sign bit), then OF is set; otherwise OF is cleared. OF thus indicates
     * signed arithmetic overflow; it can be tested with a conditional jump or
     * the INFO (interrupt on overflow) instruction. OF may be ignored when
     * performing unsigned arithmetic.
     */
    private static final int   OF     = 1 << 11;

    /** Instruction operates on byte data. */
    private static final int   B      = 0b0;
    /** Instruction operates on word data. */
    private static final int   W      = 0b1;

    /** Register AX is one of the instruction operands. */
    private static final int   AX     = 0b000;
    /** Register CX is one of the instruction operands. */
    private static final int   CX     = 0b001;
    /** Register DX is one of the instruction operands. */
    private static final int   DX     = 0b010;
    /** Register BX is one of the instruction operands. */
    private static final int   BX     = 0b011;

    /** Lookup table used for clipping results. */
    private static final int[] MASK   = new int[] { 0xff, 0xffff };
    /** Lookup table used for setting the parity flag. */
    private static final int[] PARITY = {
        1, 0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1,
        0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 1, 0,
        0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 1, 0,
        1, 0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1,
        0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 1, 0,
        1, 0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1,
        1, 0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1,
        0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 1, 0,
        0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 1, 0,
        1, 0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1,
        1, 0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1,
        0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 1, 0,
        1, 0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1,
        0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 1, 0,
        0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 1, 0,
        1, 0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1
    };
    /** Lookup table used for setting the sign and overflow flags. */
    private static final int[] BITS   = new int[] { 8, 16 };
    /** Lookup table used for setting the overflow flag. */
    private static final int[] SIGN   = new int[] { 0x80, 0x8000 };

    /*
     * General Registers
     *
     * CPU has a complement of eight 16-bit general registers. The general
     * registers are subdivided into two sets of four registers each; the data
     * registers (sometimes called the H & L group for "high" and "low"), and
     * the pointer and index registers (sometimes called the P & I group).
     *
     * The data registers are unique in that their upper (high) and lower
     * halves are separately addressable. This means that each data register
     * can be used interchangeably as a 16-bit register, or as two 8-bit
     * registers. The other CPU registers always are accessed as 16-bit units
     * only. The data registers can be used without constraint in most
     * arithmetic and logic operations. In addition, some instructions use
     * certain registers implicitly thus allowing compact yet powerful
     * encoding.
     *
     * The pointer and index registers can also participate in most arithmetic
     * and logical operations. In fact, all eight registers fit the definition
     * of "accumulator" as used in first and second generation microprocessors.
     * The P & I registers (exception BP) also are used implicitly in some
     * instructions.
     */
    /**
     * AX (accumulator)
     *
     * Implicit Use:
     * AX - Word Multiply, Word Divide, Word I/O
     * AL - Byte Multiply, Byte Divide, Byte I/O, Translate, Decimal Arithmetic
     * AH - Byte Multiply, Byte Divide
     */
    private int                ah, al;

    /**
     * CX (count)
     *
     * Implicit Use:
     * CX - String Operations, Loops
     * CL - Variable Shift and Rotate
     */
    private int                ch, cl;

    /**
     * DX (data)
     *
     * Implicit Use:
     * DX - Word Multiply, Word Divide, Indirect I/O
     */
    private int                dh, dl;

    /**
     * BX (base)
     *
     * Implicit Use:
     * BX - Translate
     */
    private int                bh, bl;

    /**
     * SP (stack pointer)
     *
     * Stacks in the 8086 are implemented in memory and located by the stack
     * segment register (SS) and the stack pointer register (SP). A system may
     * have an unlimited number of stacks, and a stack may be up to 64k bytes
     * long, the maximum length of a segment. (An attempt to expand a stack
     * beyond 64k bytes overwrites the beginning of the stack.) One stack is
     * directly addressable at a time; this is the current stack, often
     * referred to simply as "the" stack. SS contains the base base address of
     * the current stack and SP points to the top of the stack (TOS). In other
     * words, SP contains the offset of the top of the stack from the stack
     * segment's base address. Note, however, that the stack's base address
     * (contained in SS) is not the "bottom" of the stack.
     *
     * 8086 stacks are 16 bits wide; instructions that operate on a stack add
     * and remove stack items one word at a time. An item is pushed onto the
     * stack by decrementing SP by 2 and writing the item at the new TOS. An
     * item is popped of the stack by copying it from TOS and then incrementing
     * SP by 2. In other words, the stack grows down in memory toward its base
     * address. Stack operations never move items on the stack, nor do they
     * erase them. The top of the stack changes only as the result of updating
     * the stack pointer.
     */
    private int                sp;

    /**
     * BP (base pointer)
     *
     * When register BP, the pointer register, is designated as a base register
     * register in an instruction, the variable is assumed to reside in the
     * current stack segment. Register BP thus provides a convenient way to
     * address data on the stack; BP can be sued, however, to access data in
     * any of the other currently addressable segments.
     */
    private int                bp;

    /**
     * SI (source index)
     *
     * String are addressed differently than other variables. The source
     * operand of a string instruction is assumed to lie in the current data
     * segment, but another currently addressable segment may be specified. Its
     * offset is taken from the register SI, the source index register.
     */
    private int                si;

    /**
     * DI (destination index)
     *
     * The destination operand of a string instruction always resides in the
     * current extra segment; its offset is take from DI, the destination index
     * register. The string instructions automatically adjust SI and DI as they
     * process the string one byte or word at a time.
     */
    private int                di;

    /*
     * Segment Registers
     *
     * The megabyte of 8086 memory space is divided into logical segments of up
     * to 64k bytes each. The CPU has direct access to four segments at a time;
     * their base address (starting locations) are contained in the segment
     * registers.
     *
     * The segment registers are accessible to programs and can be manipulated
     * with several instructions. Good programming practice and consideration
     * of compatibility with future Intel hardware and software products
     * dictate that segment registers be used in a disciplined fashion.
     */
    /**
     * CS (code segment)
     *
     * The CS register points to the current code segment; instructions are
     * fetched from this segment.
     */
    private int                cs;

    /**
     * DS (data segment)
     *
     * The DS register points to the current data segment; it generally
     * contains program variables.
     */
    private int                ds;

    /**
     * SS (stack segment)
     *
     * The SS register points to the current stack segment, stack operations
     * are performed on locations in this segment.
     */
    private int                ss;

    /**
     * ES (extra segment)
     *
     * The ES register points to the current extra segment, which is also
     * typically used for data storage.
     */
    private int                es;

    /**
     * OS (overridden segment)
     *
     * The OS register contains the overridden segment.
     */
    private int                os;

    /**
     * IP (instruction pointer)
     *
     * The 16-bit instruction pointer (IP) is updated by the BIU so that it
     * contains the offset (distance in bytes) of the next instruction from the
     * beginning of the current code segment; i.e., IP points to the next
     * instruction. During normal execution, IP contains the offset of the next
     * instruction to be fetched by the BUI; whenever IP is saved on the stack,
     * however, it first is automatically adjusted to point to the next
     * instruction to be executed. Programs do not have direct access to the
     * instruction pointer, but instructions cause it to change and to be saved
     * and restored from the stack.
     */
    private int                ip;

    /**
     * Flags
     *
     * The 8086 has six 1-bit status flags that the EU posts to reflect certain
     * properties of the result of an arithmetic or logic operation. A group of
     * instructions is available that allow a program to alter its execution
     * depending of the state of these flags, that is, on the result of a prior
     * operation. Different instructions affect the status flags differently.
     */
    private int                flags;

    /**
     * Queue
     *
     * During periods when the EU is busy executing instructions, the BUI
     * "looks ahead" and fetches more instructions from memory. The
     * instructions are stored in an internal RAM array called the instruction
     * stream queue. The 8086 queue can store up to six instruction bytes. This
     * queue size allow the BIU to keep the EU supplied with prefetched
     * instructions under most conditions without monopolizing the system bus.
     * The 8086 BIU fetches another instruction byte whenever two bytes in its
     * queue are empty and there is no active request for bus access from the
     * EU. It normally obtains two instruction bytes per fetch; if a program
     * transfer forces fetching for an odd address, the 8086 BIU automatically
     * reads one byte for the odd address and then resumes fetching two-byte
     * word from the subsequent even addresses.
     *
     * Under most circumstances the queue contain at least one byte of
     * instruction stream and EU does not have to wait for instructions to be
     * fetched. The instructions in the queue are those stored in the memory
     * locations immediately adjacent to and higher than the instruction
     * currently being executed. That is, they are the next logical
     * instructions so long as execution proceeds serially. If the EU executes
     * an instruction that transfers control to another location, the BIU
     * resets the queue, fetches the instruction from the new address, passes
     * it immediately to the EU, and then begin refilling the queue from the
     * new location. In addition, the BIU suspends instruction fetching
     * whenever the EU requests a memory or I/O read or write (except that a
     * fetch already in progress is completed before executing the EU's bus
     * request).
     */
    private final int[]        queue  = new int[6];

    /**
     * Memory
     *
     * The 8086 can accommodate up to 1,048,576 bytes of memory in both minimum
     * and maximum mode. This section describes how memory is functionally
     * organized and used.
     *
     * Storage Organization
     *
     * From a storage point of view, the 8086 memory space is organized as an
     * array of 8-bit bytes. Instructions, byte data and word data may be
     * freely stored at any byte address without regard for alignment thereby
     * saving memory space by allowing code to be densely packed in memory.
     * Odd-addressed (unaligned) word variables, however, do not take advantage
     * of the 8086's ability to transfer 16-bits at a time. Instruction
     * alignment does not materially affect the performance of the processor.
     *
     * Following Intel convention, word data always is stored with the most-
     * significant byte in the higher memory location. Most of the time this
     * storage convention is "invisible" to anyone working with the processors;
     * exceptions may occur when monitoring the system bus or when reading
     * memory dumps.
     *
     * A special class of data is stored as doublewords; i.e., two consecutive
     * words. These are called pointers and are used to address data and code
     * that are outside the currently-addressable segments. The lower-addressed
     * word of a pointer contains an offset value, and the higher-addressed
     * word contains a segment base address. Each word is stored conventionally
     * with the higher-addressed byte containing the most significant eight
     * bits of the word.
     *
     * Segmentation
     *
     * 8086 programs "view" the megabyte of memory space as a group of segments
     * that are defined by the application. A segment is a logical unit of
     * memory that may be up to 64k bytes long. Each segment is made up of
     * contiguous memory locations and is an independent, separately-
     * addressable unit. Every segment is assigned (by software) a base
     * address, which is its starting location in the memory space. All
     * segments begin on 16-byte memory boundaries. There are no other
     * restrictions on segment locations; segments may be adjacent, disjoint,
     * partially overlapped, or fully overlapped. A physical memory location
     * may be mapped into (contained in) one or more logical segments.
     *
     * The segment registers point to (contain the vase address values of) the
     * four currently addressable segments. Programs obtain access to code and
     * data in other segments by changing the segment registers to point to the
     * desired segments.
     *
     * Every application will define and use segments differently. The
     * currently addressable segments provide a generous work space: 64k bytes
     * for code, a 64k byte stack and 128k bytes of data storage. Many
     * applications can be written to simply initialize the segments registers
     * and then forget them. Larger applications should be designed with
     * careful consideration given to segment definition.
     *
     * The segmented structure of the 8086 memory space supports modular
     * software design by discouraging huge, monolithic programs. The segments
     * also can be used to advantage in many programming situations. Take, for
     * example, the case of an editor for several on-line terminals. A 64k text
     * buffer (probably an extra segment) could be assigned to each terminal. A
     * single program could maintain all the buffers by simply changing
     * register ES to point to the buffer of the terminal requesting service.
     *
     * Physical Address Generation
     *
     * It is useful to think of every memory location as having two kinds of
     * addresses, physical and logical. A physical address is the 20-bit value
     * that uniquely identifies each byte location in the megabyte memory
     * space. Physical addresses may range from 0H through FFFFFH. All
     * exchanges between the CPU and memory components use this physical
     * address.
     *
     * Programs deal with logical, rather than physical addresses and allow
     * code to be developed without prior knowledge of where the code is to be
     * located in memory and facilitate dynamic management of memory resources.
     * A logical address consists of a segment base value and an offset value.
     * For any given memory location, the segment base value locate the first
     * byte of the containing segment and the offset value is the distance, in
     * bytes, of the target location from the beginning of the segment. Segment
     * base and offset values are unsigned 16-bit quantities; the lowest-
     * addressed byte in a segment has an offset of 0. Many different logical
     * addresses can map to the same physical location.
     *
     * Whenever the BIU accesses memory--to fetch an instruction or to obtain
     * or store a variable--it generates a physical address from a logical
     * address. This is done by shifting the segment base value four bit
     * positions and adding the offset. Note that this addition process
     * provides for modulo 64k addressing (addresses wrap around from the end
     * of a segment to the beginning of the same segment).
     *
     * The BIU obtains the logical address of a memory location from different
     * sources depending on the type of reference that is being made.
     * Instructions always are fetched from the current code segment; IP
     * contains the offset of the target instruction from the beginning of the
     * segment. Stack instructions always operate on the current stack segment;
     * SP contains the offset of the top of the stack. Most variables (memory
     * operands) are assumed to reside in the current data segment, although a
     * program can instruct the BIU to access a variable in one of the other
     * currently addressable segments. The offset of a memory variable is
     * calculated by the EU. This calculation is based on the addressing mode
     * specified in the instruction, the result is called the operand's
     * effective address (EA).
     *
     * In most cases, the BIU's segment assumptions are a convenience to
     * programmers. It is possible, however, for a programmer to explicitly
     * direct the BIU to access a variable in any of the currently addressable
     * segments (the only exception is the destination operand of a string
     * instruction which must be in the extra segment). This is done by
     * preceding an instruction with a segment override prefix. This one-byte
     * machine instruction tells the BIU which segment register to use to
     * access a variable referenced in the following instruction.
     *
     * Dedicated and Reserved Memory Locations
     *
     * Two areas in extreme low and high memory are dedicated to specific
     * processor functions or are reserved by Intel Corporation for use by
     * Intel hardware and software products. The locations are: 0H through 7FH
     * (128 bytes) and FFFF0H through FFFFFH (16 bytes). These areas are used
     * for interrupt and system reset processing; 8086 application systems
     * should not use these areas for any other purpose. Doing so may make
     * these systems incompatible with future Intel products.
     */
    private final int[]        memory = new int[0x100000];

    /*
     * Typical 8086 Machine Instruction Format
     *
     * |     BYTE 1     |     BYTE 2      |     BYTE 3    |     BYTE 4     |  BYTE 5  |  BYTE 6   |
     * | OPCODE | D | W | MOD | REG | R/M | LOW DISP/DATA | HIGH DISP/DATA | LOW DATA | HIGH DATA |
     */
    /** Operation (Instruction) code */
    private int                op;
    /** Direction is to register/Direction is from register */
    private int                d;
    /** Word/Byte operation */
    private int                w;
    /** Register mode/Memory mode with displacement length */
    private int                mod;
    /** Register operand/Extension of opcode */
    private int                reg;
    /** Register operand/Registers to use in EA calculation */
    private int                rm;

    /**
     * Performs addition with carry and sets flags accordingly.
     *
     * @param w
     *            word/byte operation
     * @param dst
     *            the first operand
     * @param src
     *            the second operand
     * @return the result
     */
    private int adc(final int w, final int dst, final int src) {
        final int carry = (flags & CF) == CF ? 1 : 0;
        final int res = dst + src + carry & MASK[w];

        setFlag(CF, carry == 1 ? res <= dst : res < dst);
        setFlag(AF, ((res ^ dst ^ src) & AF) > 0);
        setFlag(OF, (shift((dst ^ src ^ -1) & (dst ^ res), 12 - BITS[w]) & OF) > 0);
        setFlags(w, res);

        return res;
    }

    /**
     * Performs addition and sets flags accordingly.
     *
     * @param w
     *            word/byte operation
     * @param dst
     *            the first operand
     * @param src
     *            the second operand
     * @return the result
     */
    private int add(final int w, final int dst, final int src) {
        final int res = dst + src & MASK[w];

        setFlag(CF, res < dst);
        setFlag(AF, ((res ^ dst ^ src) & AF) > 0);
        setFlag(OF, (shift((dst ^ src ^ -1) & (dst ^ res), 12 - BITS[w]) & OF) > 0);
        setFlags(w, res);

        return res;
    }

    /**
     * Decrements an operand and sets flags accordingly.
     *
     * @param w
     *            word/byte operation
     * @param dst
     *            the operand
     * @return the result
     */
    private int dec(final int w, final int dst) {
        final int res = dst - 1 & MASK[w];

        setFlag(AF, ((res ^ dst ^ 1) & AF) > 0);
        setFlag(OF, res == SIGN[w] - 1);
        setFlags(w, res);

        return res;
    }

    /**
     * Decodes the second byte of the instruction and increments IP accordingly.
     */
    private void decode() {
        mod = queue[1] >>> 6 & 0b11;
        reg = queue[1] >>> 3 & 0b111;
        rm  = queue[1]       & 0b111;

        if (mod == 0b01)
            // 8-bit displacement follows
            ip += 2;
        else if (mod == 0b00 && rm == 0b110 || mod == 0b10)
            // 16-bit displacement follows
            ip += 3;
        else
            // No displacement
            ip += 1;
    }

    /**
     * Gets the absolute address from a segment and an offset.
     *
     * @param seg
     *            the segment
     * @param off
     *            the offset
     * @return the value
     */
    private int getAddr(final int seg, final int off) {
        return seg << 4 | off;
    }

    /**
     * Gets the effective address of the operand.
     *
     * The Effective Address
     *
     * The offset that the EU calculates for a memory operands is called the
     * operand's effective address or EA. It is an unsigned 16-bit number that
     * expresses the operand's distance in bytes from the beginning of the
     * segment in which it resides. The EU can calculate the effective address
     * in different ways. Information encoded in the second byte of the
     * instruction tells the EU how to calculate the operand. A compiler or
     * assembler derives this information from the statement or instruction
     * written by the programmer. Assembly language programmers have access to
     * all addressing modes.
     *
     * The execution unit calculate the EA by summing a displacement, the
     * content of a base register and the content of an index register. The
     * fact that any combination of these three given components may be present
     * in a given instruction gives rise to the variety of 8086 memory
     * addressing modes.
     *
     * The displacement element is a 8- or 16-bit number that is contained in
     * the instruction. The displacement generally is derived from the position
     * of the operand name (a variable or label) in the program. It also is
     * possible for a programmer to modify this value or to specify the
     * displacement explicitly.
     *
     * A programmer may specify that either BX or BP is to serve as a base
     * register whose content is to be used in the EA computation. Similarly,
     * either SI or DI may be specified as in index register. Whereas the
     * displacement value is a constant, the contents of the base and index
     * registers may change during execution. This makes it possible for one
     * instruction to access different memory locations as determined by the
     * current values in the base and/or index registers.
     *
     * It takes time for the EU to calculate a memory operand's effective
     * address. In general, the more elements in the calculation, the longer it
     * takes.
     *
     * Direct Addressing
     *
     * Direct addressing is the simplest memory addressing mode. No registers
     * are involved; the EA is take directly from the displacement field of the
     * instruction. Direct addressing typically is used to access simple
     * variables (scalars).
     *
     * Register Indirect Addressing
     *
     * The effective address of a memory operand may be taken directly from one
     * of the base or index register. One instruction can operate on many
     * different memory locations if the value in the base or index register is
     * updated appropriately. The LEA (load effective address) and arithmetic
     * instructions might be used to change the register value.
     *
     * Note that any 16-bit general register may be used for register indirect
     * addressing with the JMP or CALL instructions.
     *
     * Based Addressing
     *
     * In based addressing, the effective address is the sum of displacement
     * value and the content of register BX or register BP. Recall that
     * specifying BP as a base register directs the BIU to obtain the operand
     * from the current stack segment (unless a segment override prefix is
     * present). This makes based addressing with BP a very convenient way to
     * access stack data.
     *
     * Based addressing also provides a straightforward way to address
     * structures which may be located at different places in memory. A base
     * register can be pointed at the base of the structure and elements of the
     * structure addressed by their displacement from the base. Different
     * copies of the same structure can be accessed by simply changing the base
     * register.
     *
     * Indexed Addressing
     *
     * In indexed addressing, the effective address is calculated from the sum
     * of a displacement plus the content of an index register (SI or DI).
     * Indexed addressing often is used to access elements in an array. The
     * displacement locates the beginning of the array, and the value of the
     * index register selects one element (the first element is selected if the
     * index register contains 0). Since all array elements are the same
     * length, simple arithmetic on the index register will select any element.
     *
     * Based Indexed Addressing
     *
     * Based indexed addressing generates an effective address that is the sum
     * of a base register, an index register and a displacement. Based indexed
     * addressing is a very flexible ode because two address components can be
     * varied at execution time.
     *
     * Based indexed addressing provides a convenient way for a procedure to
     * address an array allocated on a stack. Register BP can contain the
     * offset of a reference point on the stack, typically the top of the stack
     * after the procedure has saved registers and allocated local storage. The
     * offset of the beginning of the array from the reference point can be
     * expressed by a displacement value, and an index register can be used to
     * access individual array elements.
     *
     * Arrays contained in structures and matrices (two-dimension arrays) also
     * could be accessed with base indexed addressing.
     *
     * String Addressing
     *
     * String instructions do not use the normal memory addressing modes to
     * access their operands. Instead, the index registers are used implicitly.
     * When a string instruction is executed, SI is assumed to point to the
     * first byte or word of the source string, and DI is assumed to point to
     * the first byte or word of the destination string. In a repeated string
     * operation, the CPU automatically adjusts SI and DI to obtain subsequent
     * bytes or words.
     *
     * I/O Port Addressing
     *
     * If an I/O port is memory mapped, any of the memory operand addressing
     * modes may be used to access the port. For example, a group of terminals
     * can be accessed as an "array". String instructions also can be used to
     * transfer data to memory-mapped ports with an appropriate hardware
     * interface.
     *
     * Two different addressing modes can be used to access ports located in
     * the I/O space. In direct port accessing, the port number is an 8-bit
     * immediate operand. This allows fixed access to ports numbered 0-255.
     * Indirect port addressing is similar to register indirect addressing of
     * memory operands. The port number is taken from register DX and can range
     * from 0 to 65,535. By previously adjusting the content of register DX,
     * one instruction can access any port in the I/O space. A group of
     * adjacent ports can be accessed using a simple software loop that adjusts
     * the value in DX.
     *
     * @param mod
     *            the mode field
     * @param rm
     *            the register/memory field
     * @return the effective address
     */
    private int getEA(final int mod, final int rm) {
        int disp = 0;
        if (mod == 0b01)
            // 8-bit displacement follows
            disp = queue[2];
        else if (mod == 0b10)
            // 16-bit displacement follows
            disp = queue[3] << 8 | queue[2];

        int ea = 0;
        switch (rm) {
        case 0b000: // EA = (BX) + (SI) + DISP
            ea = bh << 8 | bl + si + disp;
            break;
        case 0b001: // EA = (BX) + (DI) + DISP
            ea = bh << 8 | bl + di + disp;
            break;
        case 0b010: // EA = (BP) + (SI) + DISP
            ea = bp + si + disp;
            break;
        case 0b011: // EA = (BP) + (DI) + DISP
            ea = bp + di + disp;
            break;
        case 0b100: // EA = (SI) + DISP
            ea = si + disp;
            break;
        case 0b101: // EA = (DI) + DISP
            ea = di + disp;
            break;
        case 0b110:
            if (mod == 0b00) {
                // Direct address
                ea = queue[3] << 8 | queue[2];
            } else
                // EA = (BP) + DISP
                ea = bp + disp;
            break;
        case 0b111: // EA = (BX) + DISP
            ea = bh << 8 | bl + disp;
            break;
        }
        return os << 4 | ea & 0xffff;
    }

    /**
     * Gets the state of a flag.
     *
     * @param flag
     *            the flag to check
     * @return true if set, false if cleared
     */
    private boolean getFlag(final int flag) {
        return (flags & flag) > 0;
    }

    /**
     * Gets the value pointed by the instruction pointer.
     *
     * @param w
     *            word/byte operation
     * @return the value
     */
    private int getMem(final int w) {
        final int addr = getAddr(cs, ip);
        ip += 1 + w;
        return getMem(w, addr);
    }

    /**
     * Gets the value at the specified address.
     *
     * @param w
     *            word/byte operation
     * @param addr
     *            the address
     * @return the value
     */
    private int getMem(final int w, final int addr) {
        int val = memory[addr];
        if (w == W)
            val |= memory[addr + 1] << 8;
        return val;
    }

    /**
     * Gets the value of the register.
     *
     * The REG (register) field identifies a register that is one of the
     * instruction operands.
     *
     * @param w
     *            word/byte operation
     * @param reg
     *            the register field
     * @return the value
     */
    private int getReg(final int w, final int reg) {
        if (w == B)
            // Byte data
            switch (reg) {
            case 0b000: // AL
                return al;
            case 0b001: // CL
                return cl;
            case 0b010: // DL
                return dl;
            case 0b011: // BL
                return bl;
            case 0b100: // AH
                return ah;
            case 0b101: // CH
                return ch;
            case 0b110: // DH
                return dh;
            case 0b111: // BH
                return bh;
            }
        else
            // Word data
            switch (reg) {
            case 0b000: // AX
                return ah << 8 | al;
            case 0b001: // CX
                return ch << 8 | cl;
            case 0b010: // DX
                return dh << 8 | dl;
            case 0b011: // BX
                return bh << 8 | bl;
            case 0b100: // SP
                return sp;
            case 0b101: // BP
                return bp;
            case 0b110: // SI
                return si;
            case 0b111: // DI
                return di;
            }
        return 0;
    }

    /**
     * Gets the value of the register/memory.
     *
     * The MOD (mode) field indicates whether one of the operands is in memory
     * or whether both operands are registers. The encoding of the R/M
     * (register/memory) field depends on how the mode field is set.
     *
     * If MOD = 11 (register-to-register mode), then R/M identifies the second
     * register operand.
     * If MOD selects memory mode, then R/M indicates how the effective address
     * of the memory operand is to be calculated.
     *
     * @param w
     *            word/byte instruction
     * @param mod
     *            the mode field
     * @param rm
     *            the register/memory field
     * @return the value
     */
    private int getRM(final int w, final int mod, final int rm) {
        if (mod == 0b11)
            // Register-to-register mode
            return getReg(w, rm);
        else
            // Memory mode
            return getMem(w, getEA(mod, rm));
    }

    /**
     * Gets the value of the segment register.
     *
     * The REG (register) field identifies a register that is one of the
     * instruction operands.
     *
     * @param reg
     *            the register field
     * @return the value
     */
    private int getSegReg(final int reg) {
        switch (reg) {
        case 0b00: // ES
            return es;
        case 0b01: // CS
            return cs;
        case 0b10: // SS
            return ss;
        case 0b11: // DS
            return ds;
        }
        return 0;
    }

    /**
     * Increments an operand and sets flags accordingly.
     *
     * @param w
     *            word/byte operation
     * @param dst
     *            the operand
     * @return the result
     */
    private int inc(final int w, final int dst) {
        final int res = dst + 1 & MASK[w];

        setFlag(AF, ((res ^ dst ^ 1) & AF) > 0);
        setFlag(OF, res == SIGN[w]);
        setFlags(w, res);

        return res;
    }

    /**
     * Loads a binary file into memory at the specified address.
     *
     * @param bin
     *            the binary file
     */
    public void load(final int[] bin) {
        for (int i = 0; i < bin.length; i++)
            memory[i] = bin[i] & 0xff;
    }

    /**
     * Sets flags according to the result of a logical operation.
     *
     * @param w
     *            word/byte operation
     * @param res
     *            the result
     */
    private void logic(final int w, final int res) {
        setFlag(CF, false);
        setFlag(OF, false);
        setFlags(w, res);
    }

    /**
     * Pops a value at the top of the stack.
     *
     * @return the value
     */
    private int pop() {
        final int val = getMem(W, getAddr(ss, sp));
        sp += 2;
        return val;
    }

    /**
     * Pushes a value to the top of the stack.
     *
     * @param val
     *            the value
     */
    private void push(final int val) {
        sp -= 2;
        setMem(W, getAddr(ss, sp), val);
    }

    /**
     * Resets the CPU to its default state.
     */
    public void reset() {
        flags = 0;
        ip = 0x0000;
        sp = 0x0100;
        //cs = 0xffff;
        ds = 0x0000;
        ss = 0x0000;
        es = 0x0000;
        for (int i = 0; i < 6; i++)
            queue[i] = 0;
    }

    /**
     * Execute all instructions.
     */
    public void run() {
        while (tick()) {
            //TODO Replace by the real text mode.
            for (int y = 0; y < 25; y++)
                for (int x = 0; x < 80; x++) {
                    final char c = (char) memory[0x8000 + y * 80 + x];
                    System.out.print(c == 0 ? " " : c);
                    if (x == 79)
                        System.out.println("");
                }
        }
    }

    /**
     * Performs subtraction with borrow and sets flags accordingly.
     *
     * @param w
     *            word/byte operation
     * @param dst
     *            the first operand
     * @param src
     *            the second operand
     * @return the result
     */
    private int sbb(final int w, final int dst, final int src) {
        final int carry = (flags & CF) == CF ? 1 : 0;
        final int res = dst - src - carry & MASK[w];

        setFlag(CF, carry > 0 ? dst <= src : dst < src);
        setFlag(AF, ((res ^ dst ^ src) & AF) > 0);
        setFlag(OF, (shift((dst ^ src) & (dst ^ res), 12 - BITS[w]) & OF) > 0);
        setFlags(w, res);

        return res;
    }

    /**
     * Sets or clears a flag.
     *
     * @param flag
     *            the flag to affect
     * @param set
     *            true to set, false to clear
     */
    private void setFlag(final int flag, final boolean set) {
        if (set)
            flags |= flag;
        else
            flags &= ~flag;
    }

    /**
     * Sets the parity, zero and sign flags.
     *
     * @param w
     *            word/byte operation
     * @param res
     *            the result
     */
    private void setFlags(final int w, final int res) {
        setFlag(PF, PARITY[res & 0xff] > 0);
        setFlag(ZF, res == 0);
        setFlag(SF, (shift(res, 8 - BITS[w]) & SF) > 0);
    }

    /**
     * Sets the value at the specified address.
     *
     * @param w
     *            word/byte operation
     * @param addr
     *            the address
     * @param val
     *            the new value
     */
    private void setMem(final int w, final int addr, final int val) {
        memory[addr] = val & 0xff;
        if (w == W)
            memory[addr + 1] = val >>> 8 & 0xff;
    }

    /**
     * Sets the value of the register.
     *
     * The REG (register) field identifies a register that is one of the
     * instruction operands.
     *
     * @param w
     *            word/byte operation
     * @param reg
     *            the register field
     * @param val
     *            the new value
     */
    private void setReg(final int w, final int reg, final int val) {
        if (w == B)
            // Byte data
            switch (reg) {
            case 0b000: // AL
                al = val & 0xff;
                break;
            case 0b001: // CL
                cl = val & 0xff;
                break;
            case 0b010: // DL
                dl = val & 0xff;
                break;
            case 0b011: // BL
                bl = val & 0xff;
                break;
            case 0b100: // AH
                ah = val & 0xff;
                break;
            case 0b101: // CH
                ch = val & 0xff;
                break;
            case 0b110: // DH
                dh = val & 0xff;
                break;
            case 0b111: // BH
                bh = val & 0xff;
                break;
            }
        else
            // Word data
            switch (reg) {
            case 0b000: // AX
                al = val & 0xff;
                ah = val >>> 8 & 0xff;
                break;
            case 0b001: // CX
                cl = val & 0xff;
                ch = val >>> 8 & 0xff;
                break;
            case 0b010: // DX
                dl = val & 0xff;
                dh = val >>> 8 & 0xff;
                break;
            case 0b011: // BX
                bl = val & 0xff;
                bh = val >>> 8 & 0xff;
                break;
            case 0b100: // SP
                sp = val & 0xffff;
                break;
            case 0b101: // BP
                bp = val & 0xffff;
                break;
            case 0b110: // SI
                si = val & 0xffff;
                break;
            case 0b111: // DI
                di = val & 0xffff;
                break;
            }
    }

    /**
     * Sets the value of the register/memory.
     *
     * The MOD (mode) field indicates whether one of the operands is in memory
     * or whether both operands are registers. The encoding of the R/M
     * (register/memory) field depends on how the mode field is set.
     *
     * If MOD = 11 (register-to-register mode), then R/M identifies the second
     * register operand.
     * If MOD selects memory mode, then R/M indicates how the effective address
     * of the memory operand is to be calculated.
     *
     * @param w
     *            word/byte instruction
     * @param mod
     *            the mode field
     * @param rm
     *            the register/memory field
     * @param val
     *            the new value
     */
    private void setRM(final int w, final int mod, final int rm, final int val) {
        if (mod == 0b11)
            // Register-to-register mode
            setReg(w, rm, val);
        else
            // Memory mode
            setMem(w, getEA(mod, rm), val);
    }

    /**
     * Sets the value of the segment register.
     *
     * The REG (register) field identifies a register that is one of the
     * instruction operands.
     *
     * @param reg
     *            the register field
     * @param val
     *            the new value
     */
    private void setSegReg(final int reg, final int val) {
        switch (reg) {
        case 0b00: // ES
            es = val & 0xffff;
            break;
        case 0b01: // CS
            cs = val & 0xffff;
            break;
        case 0b10: // SS
            ss = val & 0xffff;
            break;
        case 0b11: // DS
            ds = val & 0xffff;
            break;
        }
    }

    /**
     * Performs subtraction and sets flags accordingly.
     *
     * @param w
     *            word/byte operation
     * @param dst
     *            the first operand
     * @param src
     *            the second operand
     * @return the result
     */
    private int sub(final int w, final int dst, final int src) {
        final int res = dst - src & MASK[w];

        setFlag(CF, dst < src);
        setFlag(AF, ((res ^ dst ^ src) & AF) > 0);
        setFlag(OF, (shift((dst ^ src) & (dst ^ res), 12 - BITS[w]) & OF) > 0);
        setFlags(w, res);

        return res;
    }

    /**
     * Fetches and executes an instruction.
     *
     * @return true if instructions remain, false otherwise
     */
    private boolean tick() {
        int rep = 0;
        prefixes: while (true) {
            // Segment prefix check.
            switch (getMem(B)) {
            case 0x26: // ES: (segment override prefix)
                os = es;
                break;
            case 0x2e: // CS: (segment override prefix)
                os = cs;
                break;
            case 0x36: // SS: (segment override prefix)
                os = ss;
                break;
            case 0x3e: // DS: (segment override prefix)
                os = ds;
                break;
            // Repeat prefix check.
            case 0xf2: // REPNE/REPNZ
                rep = 2;
                break;
            case 0xf3: // REP/REPE/REPZ
                rep = 1;
                break;
            default:
                os = ds;
                --ip;
                break prefixes;
            }
        }

        // Fetch instruction from memory.
        for (int i = 0; i < 6; ++i)
            queue[i] = getMem(B, getAddr(cs, ip + i));

        // Decode first byte.
        op = queue[0];
        d  = op >>> 1 & 0b1;
        w  = op       & 0b1;
        ++ip; // Increment IP.

        // Only repeat string instructions.
        if (rep > 0 && (op < 0xa4 || op > 0xaf || op > 0xa7 && op < 0xaa))
            rep = 0;

        do {
            // Repeat prefix present.
            if (rep > 0) {
                final int cx = getReg(W, CX);

                // Reached EOS.
                if (cx == 0)
                    break;

                // Decrement CX.
                setReg(W, CX, cx - 1);
            }

            int dst, src, res = 0;
            switch (op) {
            /*
             * Data Transfer Instructions
             *
             * The 14 data transfer instructions move single bytes and words
             * between memory and registers as well as between registers AL or
             * AX and I/O ports. The stack manipulation instructions are
             * included in this group as are instructions for transferring flags
             * contents and for loading segment registers.
             */
            /*
             * General Purpose Data Transfers
             */
            /*
             * MOV destination,source
             *
             * MOV transfers a byte or a word from the source operand to the
             * destination operand.
             */
            // Register/Memory to/from Register
            case 0x88: // MOV REG8/MEM8,REG8
            case 0x89: // MOV REG16/MEM16,REG16
            case 0x8a: // MOV REG8,REG8/MEM8
            case 0x8b: // MOV REG16,REG16/MEM16
                decode();
                if (d == 0b0) {
                    src = getReg(w, reg);
                    setRM(w, mod, rm, src);
                } else {
                    src = getRM(w, mod, rm);
                    setReg(w, reg, src);
                }
                break;

            // Immediate to Register/Memory
            case 0xc6: // MOV REG8/MEM8,IMMED8
            case 0xc7: // MOV REG16/MEM16,IMMED16
                decode();
                switch (reg) {
                case 0b000:
                    src = getMem(w);
                    setRM(w, mod, rm, src);
                }
                break;

            // Immediate to Register
            case 0xb0: // MOV AL,IMMED8
            case 0xb1: // MOV CL,IMMED8
            case 0xb2: // MOV DL,IMMED8
            case 0xb3: // MOV BL,IMMED8
            case 0xb4: // MOV AH,IMMED8
            case 0xb5: // MOV CH,IMMED8
            case 0xb6: // MOV DH,IMMED8
            case 0xb7: // MOV BH,IMMED8
            case 0xb8: // MOV AX,IMMED16
            case 0xb9: // MOV CX,IMMED16
            case 0xba: // MOV DX,IMMED16
            case 0xbb: // MOV BX,IMMED16
            case 0xbc: // MOV SP,IMMED16
            case 0xbd: // MOV BP,IMMED16
            case 0xbe: // MOV SI,IMMED16
            case 0xbf: // MOV DI,IMMED16
                w   = op >>> 3 & 0b1;
                reg = op       & 0b111;
                src = getMem(w);
                setReg(w, reg, src);
                break;

            // Memory to/from Accumulator
            case 0xa0: // MOV AL,MEM8
            case 0xa1: // MOV AX,MEM16
            case 0xa2: // MOV MEM8,AL
            case 0xa3: // MOV MEM16,AX
                dst = getMem(W);
                if (d == 0b0) {
                    src = getMem(w, getAddr(os, dst));
                    setReg(w, AX, src);
                } else {
                    src = getReg(w, AX);
                    setMem(w, getAddr(os, dst), src);
                }
                break;

            // Register/Memory to/from Segment Register
            case 0x8e: // MOV SEGREG,REG16/MEM16
            case 0x8c: // MOV REG16/MEM16,SEGREG
                decode();
                if (d == 0b0) {
                    src = getRM(W, mod, rm);
                    setSegReg(reg, src);
                } else {
                    src = getSegReg(reg);
                    setRM(W, mod, rm, src);
                }
                break;

            /*
             * PUSH source
             *
             * PUSH decrements SP (the stack pointer) by two and then transfers
             * a word from the source operand to the top of the stack now
             * pointer by SP. PUSH often is used to place parameters on the
             * stack before calling a procedure; more generally, it is the basic
             * means of storing temporary data on the stack.
             */
            // Register
            case 0x50: // PUSH AX
            case 0x51: // PUSH CX
            case 0x52: // PUSH DX
            case 0x53: // PUSH BX
            case 0x54: // PUSH SP
            case 0x55: // PUSH BP
            case 0x56: // PUSH SI
            case 0x57: // PUSH DI
                reg = op & 0b111;
                src = getReg(W, reg);
                push(src);
                break;

            // Segment Register
            case 0x06: // PUSH ES
            case 0x0e: // PUSH CS
            case 0x16: // PUSH SS
            case 0x1e: // PUSH DS
                reg = op >>> 3 & 0b111;
                src = getSegReg(reg);
                push(src);
                break;

            /*
             * POP destination
             *
             * POP transfers the word at the current top of the stack (pointed
             * to by the SP) to the destination operand, and then increments SP
             * by two to point to the new top of the stack. POP can be used to
             * move temporary variables from the stack to registers or memory.
             */
            // Register
            case 0x58: // POP AX
            case 0x59: // POP CX
            case 0x5a: // POP DX
            case 0x5b: // POP BX
            case 0x5c: // POP SP
            case 0x5d: // POP BP
            case 0x5e: // POP SI
            case 0x5f: // POP DI
                reg = op & 0b111;
                src = pop();
                setReg(W, reg, src);
                break;

            // Segment Register
            case 0x07: // POP ES
            case 0x0f: // POP CS
            case 0x17: // POP SS
            case 0x1f: // POP DS
                reg = op >>> 3 & 0b111;
                src = pop();
                setSegReg(reg, src);
                break;

            /*
             * XCHG destination,source
             *
             * XCHG (exchange) switches the contents of the source and
             * destination (byte or word) operands. When used in conjunction
             * with the LOCK prefix, XCHG can test and set a semaphore that
             * controls access to a resource shared by multiple processors.
             */
            // Register/Memory with Register
            case 0x86: // XCHG REG8,REG8/MEM8
            case 0x87: // XCHG REG16,REG16/MEM16
                decode();
                dst = getReg(w, reg);
                src = getRM(w, mod, rm);
                setReg(w, reg, src);
                setRM(w, mod, rm, dst);
                break;

            // Register with Accumulator
            case 0x91: // XCHG AX,CX
            case 0x92: // XCHG AX,DX
            case 0x93: // XCHG AX,BX
            case 0x94: // XCHG AX,SP
            case 0x95: // XCHG AX,BP
            case 0x96: // XCHG AX,SI
            case 0x97: // XCHG AX,DI
                reg = op & 0b111;
                dst = getReg(W, AX);
                src = getReg(W, reg);
                setReg(W, AX, src);
                setReg(W, reg, dst);
                break;

            /*
             * XLAT translate-table
             *
             * XLAT (translate) replaces a byte in the AL register with a byte
             * from a 256-byte, user-coded translation table. Register BX is
             * assumed to point to the beginning of the table. The byte in AL is
             * used as an index into the table and is replaced by the byte at
             * the offset in the table corresponding to AL's binary value. The
             * first byte in the table has an offset of 0. For example, if AL
             * contains 5H, and the sixth element of the translation table
             * contains 33H, then AL will contain 33H following the instruction.
             * XLAT is useful for translating characters from one code to
             * another, the classic example being ASCII to EBCDIC or the
             * reverse.
             */
            case 0xd7: // XLAT SOURCE-TABLE
                al = getMem(B, getAddr(os, getReg(W, BX) + al));
                break;

            /*
             * Address Object Transfers
             *
             * The instructions manipulate the addresses of the variables rather
             * than the contents or values of variables. They are most useful
             * for list processing, based variables, and string operations.
             */
            /*
             * LEA destination,source
             *
             * LEA (load effective address) transfers the offset of the source
             * operand (rather than its value) to the destination operand. The
             * source must be a memory operand, and the destination operand must
             * be a 16-bit general register. LEA does not affect any flags. The
             * XLAT and string instructions assume that certain registers point
             * to operands; LEA can be used to load these register (e.g.,
             * loading BX with the address of the translate table used by the
             * XLAT instruction).
             */
            case 0x8d: // LEA REG16,MEM16
                decode();
                src = getEA(mod, rm) - (os << 4);
                setReg(w, reg, src);
                break;

            /*
             * LDS destination,source
             *
             * LDS (load pointer using DS) transfers a 32-bit pointer variable
             * from the source operand, which must be a memory operand, to the
             * destination operand and register DS. The offset word of the
             * pointer is transferred to the destination operand, which may be
             * any 16-bit general register. The segment word of the pointer is
             * transferred to register DS. Specifying SI as the destination
             * operand is a convenient way to prepare to process a source string
             * that is not in the current data segment (string instructions
             * assume that the source string is located in the current data
             * segment and that SI contains the offset of the string).
             */
            case 0xc5: // LDS REG16,MEM16
                decode();
                src = getEA(mod, rm);
                setReg(w, reg, getMem(W, src));
                ds = getMem(W, src + 2);
                break;

            /*
             * LES destination,source
             *
             * LES (load pointer using ES) transfers a 32-bit pointer variable
             * from the source operand, which must be a memory operand, to the
             * destination operand and register ES. The offset word of the
             * pointer is transferred to the destination operand, which may be
             * any 16-bit general register. The segment word of the pointer is
             * transferred to register ES. Specifying DI as the destination
             * operand is a convenient way to prepare to process a destination
             * string that is not in the current extra segment. (The destination
             * string must be located in the extra segment, and DI must contain
             * the offset of the string).
             */
            case 0xc4: // LES REG16,MEM16
                decode();
                src = getEA(mod, rm);
                setReg(w, reg, getMem(W, src));
                es = getMem(W, src + 2);
                break;

            /*
             * Flag Transfers
             */
            /*
             * LAHF
             *
             * LAHF (load register AH from flags) copies SF, ZF, AF, PF and CF
             * into the bits 7, 6, 4, 2 and 0, respectively, of register AH. The
             * content of bits 5, 3 and 1 is undefined; the flags themselves are
             * not affected. LAHF is provided primarily for converting 8080/8085
             * assembly language programs to run on an 8086.
             */
            case 0x9f: // LAHF
                ah = flags & 0xff;
                break;

            /*
             * SAHF
             *
             * SAHF (store register AH into flags) transfers bits 7, 6, 4, 2 and
             * 0 from register AH into SF, ZF, AF, PF and CF, respectively,
             * replacing whatever values these flags previously had. OF, DF, IF
             * and TF are not affected. This instruction is provided from
             * 8080/8085 compatibility.
             */
            case 0x9e: // SAHF
                flags = flags & 0xff00 | ah;
                break;

            /*
             * PUSHF
             *
             * PUSH decrements SP (the stack pointer) by two and then transfers
             * all flags to the word at the top of stack pointed to by SP. The
             * flags themselves are not affected.
             */
            case 0x9c: // PUSHF
                push(flags);
                break;

            /*
             * POPF
             *
             * POPF transfers specific bits from the word at the current top of
             * stack (pointed to by register SP) into the 8086 flags, replacing
             * whatever values the flags previously contained. SP is then
             * incremented by two to point at the new top of stack. PUSHF and
             * POPF allow a procedure to save and restore a calling program's
             * flags. They also allow a program to change the setting of TF
             * (there is no instruction for updating this flag directly). The
             * change is accomplished by pushing the flags, altering bit 8 of
             * the memory- image and then popping the flags.
             */
            case 0x9d: // POPF
                flags = pop();
                break;

            /*
             * Arithmetic Instructions
             *
             * Arithmetic Data Formats
             *
             * 8086 arithmetic operations may be performed on four types of
             * numbers: unsigned binary, signed binary (integers), unsigned
             * packed decimal and unsigned unpacked decimal. Binary numbers may
             * be 8 or 16 bits long. Decimal numbers are stored in bytes, two
             * digits per byte for packed decimals and one digit per byte for
             * unpacked decimal. The processor always assumes that the operands
             * specified in arithmetic instructions contain data that represents
             * valid numbers for the type of instructions being performed.
             * Invalid data may produce unpredictable results.
             *
             * Unsigned binary numbers may be either 8 or 16 bits long; all bits
             * are considered in determining a number's magnitude. The value
             * range of an 8-bit unsigned binary number is 0-255; 16 bits can
             * represent values from 0 through 65,535. Addition, subtraction,
             * multiplication and division operations are available for unsigned
             * binary numbers.
             *
             * Signed binary numbers (integer) may be either 8 or 16 bits long.
             * The high-order (leftmost) bit is interpreted as the number's
             * sign: 0 = positive and 1 = negative. Negative numbers are
             * represented in standard two's complement notation. Since the
             * high-order is used for a sign, the range of an 8-bit integer is
             * -128 through +127; 16-bit integer may range from -32,768 through
             * +32,767. The value zero has a positive sign. Multiplication and
             * division operations are provided for signed binary numbers.
             * Addition and subtraction are performed with the unsigned binary
             * instructions. Conditional jump instructions, as well as an
             * "interrupt on overflow" instruction, can be used following an
             * unsigned operation on an integer to detect overflow into the sign
             * bit.
             *
             * Packed decimal numbers are stored as unsigned byte quantities.
             * The byte is treated as having one decimal digit in each half-byte
             * (nibble); the digit in the high-order half-byte is the most
             * significant. Hexadecimal values 0-9 are valid in each half-byte,
             * and the range of a packed decimal number is 0-99. Addition and
             * subtraction are performed in two steps. First an unsigned binary
             * instruction is used to produce an intermediate result in register
             * AL. The an adjustment operation is performed which changes the
             * intermediate value in AL to a final correct packed decimal
             * result. Multiplication and division adjustments are not available
             * for packed decimal numbers.
             *
             * Unpacked decimal numbers are stored as unsigned byte quantities.
             * The magnitude of the number is determined from the low-order
             * half-byte; hexadecimal values 0-9 are valid and are interpreted
             * as decimal numbers. The high-order half-byte must be zero for
             * multiplication and division; it may contain any value from
             * addition and subtraction. Arithmetic on unpacked decimal numbers
             * is performed in two steps. The unsigned binary addition,
             * subtraction and multiplication operations are used to produce an
             * intermediate result in register AL. An adjustment instruction
             * then changes the value in AL to a final correct unpacked decimal
             * number. Division is performed similarly, except that the
             * adjustment is carried out on the numerator operand in register AL
             * first, then a following unsigned binary division instruction
             * produces a correct result.
             *
             * Unpacked decimal numbers are similar to the ASCII character
             * representations of the digits 0-9. Note, however, that the high-
             * order half-byte of an ASCII numeral is always 3H. Unpacked
             * decimal arithmetic may be performed on ASCII number characters
             * under the following conditions: - the high-order half-byte of an
             * ASCII numeral must be set to 0H prior to multiplication or
             * division. - unpacked decimal arithmetic leaves the high-order
             * half-byte set to 0H; it must be set to 3H to produce a valid
             * ASCII numeral.
             *
             * Arithmetic Instructions and Flags
             *
             * The 8086 arithmetic instructions post certain characteristics of
             * the result of the operation to six flags. Most of these flags can
             * be tested by following the arithmetic instruction with a
             * conditional jump instruction; the INTO (interrupt on overflow)
             * instruction may also be used. The various instructions affect the
             * flags differently, as explained in the instruction descriptions.
             */
            /*
             * Addition
             */
            /*
             * ADD destination,source
             *
             * The sum of the two operands, which may be bytes or words,
             * replaces the destination operand. Both operands may be signed or
             * unsigned binary numbers. ADD updates AF, CF, OF, PF, SF and ZF.
             */
            // Reg./Memory and Register to Either
            case 0x00: // ADD REG8/MEM8,REG8
            case 0x01: // ADD REG16/MEM16,REG16
            case 0x02: // ADD REG8,REG8/MEM8
            case 0x03: // ADD REG16,REG16/MEM16
                decode();
                if (d == 0b0) {
                    dst = getRM(w, mod, rm);
                    src = getReg(w, reg);
                } else {
                    dst = getReg(w, reg);
                    src = getRM(w, mod, rm);
                }
                res = add(w, dst, src);
                if (d == 0b0)
                    setRM(w, mod, rm, res);
                else
                    setReg(w, reg, res);
                break;

            // Immediate to Accumulator
            case 0x04: // ADD AL,IMMED8
            case 0x05: // ADD AX,IMMED16
                dst = getReg(w, 0);
                src = getMem(w);
                res = add(w, dst, src);
                setReg(w, AX, res);
                break;

            /*
             * ADC destination,source
             *
             * ADC (Add with Carry) sums the operands, which may be bytes or
             * words, adds one if CF is set and replaces the destination operand
             * with the result. Both operands may be signed or unsigned binary
             * numbers. ADC updates AF, CF, OF, PF, SF and ZF. Since ADC
             * incorporates a carry from a previous operation, it can be used to
             * write routines to add numbers longer than 16 bits.
             */
            // Reg./Memory with Register to Either
            case 0x10: // ADC REG8/MEM8,REG8
            case 0x11: // ADC REG16/MEM16,REG16
            case 0x12: // ADC REG8,REG8/MEM8
            case 0x13: // ADC REG16,REG16/MEM16
                decode();
                if (d == 0b0) {
                    dst = getRM(w, mod, rm);
                    src = getReg(w, reg);
                } else {
                    dst = getReg(w, reg);
                    src = getRM(w, mod, rm);
                }
                res = adc(w, dst, src);
                if (d == 0b0)
                    setRM(w, mod, rm, res);
                else
                    setReg(w, reg, res);
                break;

            // Immediate to Accumulator
            case 0x14: // ADC AL,IMMED8
            case 0X15: // ADC AX,IMMED16
                dst = getReg(w, AX);
                src = getMem(w);
                res = adc(w, dst, src);
                setReg(w, AX, res);
                break;

            /*
             * INC destination
             *
             * INC (Increment) adds one to the destination operand. The operand
             * may be a byte or a word and is treated as an unsigned binary
             * number. INC updates AF, OF, PF, SF and ZF; it does not affect CF.
             */
            // Register
            case 0x40: // INC AX
            case 0x41: // INC CX
            case 0x42: // INC DX
            case 0x43: // INC BX
            case 0x44: // INC SP
            case 0x45: // INC BP
            case 0x46: // INC SI
            case 0x47: // INC DI
                reg = op & 0b111;
                src = getReg(W, reg);
                res = inc(W, src);
                setReg(W, reg, res);
                break;

            /*
             * AAA
             *
             * AAA (ASCII Adjust for Addition) changes the contents of register
             * AL to a valid unpacked decimal number; the high-order half-byte
             * is zeroed. AAA updates AF and CF; the content of OF, PF, SF and
             * ZF is undefined following execution of AAA.
             */
            case 0x37: // AAA
                if ((al & 0xf) > 9 || getFlag(AF)) {
                    al += 6;
                    ah = ah + 1 & 0xff;
                    setFlag(CF, true);
                    setFlag(AF, true);
                } else {
                    setFlag(CF, false);
                    setFlag(AF, false);
                }
                al &= 0xf;
                break;

            /*
             * DAA
             *
             * DAA (Decimal Adjust for Addition) corrects the result of
             * previously adding two valid packed decimal operands (the
             * destination operand must have been register AL). DAA changes the
             * content of AL to a pair of valid packed decimal digits. It
             * updates AF, CF, PF, SF and ZF; the content of OF is undefined
             * following execution of DAA.
             */
            case 0x27: { // DAA
                final int oldAL = al;
                final boolean oldCF = getFlag(CF);
                setFlag(CF, false);
                if ((al & 0xf) > 9 || getFlag(AF)) {
                    al += 6;
                    setFlag(CF, oldCF || al < 0);
                    al &= 0xff;
                    setFlag(AF, true);
                } else
                    setFlag(AF, false);
                if (oldAL > 0x99 || oldCF) {
                    al = al + 0x60 & 0xff;
                    setFlag(CF, true);
                } else
                    setFlag(CF, false);
                setFlags(B, al);
                break;
            }

            /*
             * Subtraction
             */
            /*
             * SUB destination,source
             *
             * The source operand is subtracted from the destination operand,
             * and the result replaces the destination operand. The operands may
             * be bytes or words. Both operands may be signed or unsigned binary
             * numbers. SUB updates AF, CF, OF, PF, SF and ZF.
             */
            // Reg./Memory and Register to Either
            case 0x28: // SUB REG8/MEM8,REG8
            case 0x29: // SUB REG16/MEM16,REG16
            case 0x2a: // SUB REG8,REG8/MEM8
            case 0x2b: // SUB REG16,REG16/MEM16
                decode();
                if (d == 0b0) {
                    dst = getRM(w, mod, rm);
                    src = getReg(w, reg);
                } else {
                    dst = getReg(w, reg);
                    src = getRM(w, mod, rm);
                }
                res = sub(w, dst, src);
                if (d == 0b0)
                    setRM(w, mod, rm, res);
                else
                    setReg(w, reg, res);
                break;

            // Immediate from Accumulator
            case 0x2c: // SUB AL,IMMED8
            case 0x2d: // SUB AX,IMMED16
                dst = getReg(w, AX);
                src = getMem(w);
                res = sub(w, dst, src);
                setReg(w, AX, res);
                break;

            /*
             * SBB destination,source
             *
             * SBB (Subtract with Borrow) subtracts the source from the
             * destination, subtracts one if CF is set, and returns the result
             * to the destination operand. Both operands may be bytes or words.
             * Both operands may be signed or unsigned binary numbers. SBB
             * updates AF, CF, OF, PF, SF and ZF. Since it incorporates a borrow
             * from a previous operation, SBB may be used to write routines that
             * subtract numbers longer than 16 bits.
             */
            // Reg./Memory with Register to Either
            case 0x18: // SBB REG8/MEM8,REG8
            case 0x19: // SBB REG16/MEM16,REG16
            case 0x1a: // SBB REG8,REG8/MEM8
            case 0x1b: // SBB REG16,REG16/MEM16
                decode();
                if (d == 0b0) {
                    dst = getRM(w, mod, rm);
                    src = getReg(w, reg);
                } else {
                    dst = getReg(w, reg);
                    src = getRM(w, mod, rm);
                }
                res = sbb(w, dst, src);
                if (d == 0b0)
                    setRM(w, mod, rm, res);
                else
                    setReg(w, reg, res);
                break;

            // Immediate to Accumulator
            case 0x1c: // SBB AL,IMMED8
            case 0X1d: // SBB AX,IMMED16
                dst = getReg(w, AX);
                src = getMem(w);
                res = sbb(w, dst, src);
                setReg(w, AX, res);
                break;

            /*
             * DEC destination
             *
             * DEC (Decrement) subtracts one from the destination, which may be
             * a byte or a word. DEC updates AF, OF, PF, SF, and ZF; it does not
             * affect CF.
             */
            // Register
            case 0x48: // DEC AX
            case 0x49: // DEC CX
            case 0x4a: // DEC DX
            case 0x4b: // DEC BX
            case 0x4c: // DEC SP
            case 0x4d: // DEC BP
            case 0x4e: // DEC SI
            case 0x4f: // DEC DI
                reg = op & 0b111;
                dst = getReg(W, reg);
                res = dec(W, dst);
                setReg(W, reg, res);
                break;

            /*
             * NEG destination
             *
             * NEG (Negate) subtracts the destination operand, which may be a
             * byte or a word, from 0 and returns the result to the destination.
             * This forms the two's complement of the number, effectively
             * reversing the sign of an integer. If the operand is zero, its
             * sign is not changed. Attempting to negate a byte containing -128
             * or a word containing -32,768 causes no change to the operand and
             * sets OF. NEG updates AF, CF, OF, PF, SF and ZF. CF is always set
             * except when the operand is zero, in which case it is cleared.
             */

            /*
             * CMP destination,source
             *
             * CMP (Compare) subtracts the source from the destination, which
             * may be bytes or words, but does not return the result. The
             * operands are unchanged, but the flags are updated and can be
             * tested by the subsequent conditional jump instructions. CMP
             * updates AF, CF, OF, PF, SF and ZF. The comparison reflected in
             * the flags is that of the destination to the source. If a CMP
             * instruction is followed by a JG (jump if greater) instruction,
             * for example, the jump is taken if the destination operand is
             * greater than the source operand.
             */
            // Register/Memory and Register
            case 0x38: // CMP REG8/MEM8,REG8
            case 0x39: // CMP REG16/MEM16,REG16
            case 0x3a: // CMP REG8,REG8/MEM8
            case 0x3b: // CMP REG16,REG16/MEM16
                decode();
                if (d == 0b0) {
                    dst = getRM(w, mod, rm);
                    src = getReg(w, reg);
                } else {
                    dst = getReg(w, reg);
                    src = getRM(w, mod, rm);
                }
                sub(w, dst, src);
                break;

            // Immediate with Accumulator
            case 0x3c: // CMP AL,IMMED8
            case 0x3d: // CMP AX,IMMED16
                dst = getReg(w, AX);
                src = getMem(w);
                sub(w, dst, src);
                break;

            /*
             * AAS
             *
             * AAS (ASCII Adjust for Subtraction) corrects the result of a
             * previous subtraction of two valid unpacked decimal operands (the
             * destination operand must have been specified as register AL). AAS
             * changes the content of AL to a valid unpacked decimal number; the
             * high-order half-byte is zeroed. AAS updates AF and CF; the
             * content of OF, PF, SF and ZF is undefined following execution of
             * AAS.
             */
            case 0x3f: // AAS
                if ((al & 0xf) > 9 || getFlag(AF)) {
                    al -= 6;
                    ah = ah - 1 & 0xff;
                    setFlag(CF, true);
                    setFlag(AF, true);
                } else {
                    setFlag(CF, false);
                    setFlag(AF, false);
                }
                al &= 0xf;
                break;

            /*
             * DAS
             *
             * DAS (Decimal Adjust for Subtraction) corrects the result of a
             * previous subtraction of two valid packed decimal operands (the
             * destination operand must have been specified as register AL). DAS
             * changes the content of AL to a pair of valid packed decimal
             * digits. DAS updates AF, CF, PF, SF and ZF; the content of OF is
             * undefined following the execution of DAS.
             */
            case 0x2f: // DAS
            {
                final int oldAL = al;
                final boolean oldCF = getFlag(CF);
                setFlag(CF, false);
                if ((al & 0xf) > 9 || getFlag(AF)) {
                    al -= 6;
                    setFlag(CF, oldCF || (al & 0xff) > 0);
                    al &= 0xff;
                    setFlag(AF, true);
                } else
                    setFlag(AF, false);
                if (oldAL > 0x99 || oldCF) {
                    al = al - 0x60 & 0xff;
                    setFlag(CF, true);
                } else
                    setFlag(CF, false);
                setFlags(B, al);
                break;
            }

            /*
             * Multiplication
             */
            /*
             * MUL source
             *
             * MUL (Multiply) performs an unsigned multiplication of the source
             * operand and the accumulator. If the source is a byte, then it is
             * multiplied by register AL, and the double-length result is
             * returned in AH and AL. If the source operand is a word, then it
             * is multiplied by register AX, and the double-length result is
             * returned in registers DX and AX. The operands are treated as
             * unsigned binary numbers. If the upper half of the result (AH for
             * byte source, DX for word source) is nonzero, CF and OF are set;
             * otherwise they are cleared. When CF and OF are set, they indicate
             * that AH or DX contains significant digits of the result. The
             * content of AF, PF, SF and ZF is undefined following execution of
             * MUL.
             */

            /*
             * IMUL source
             *
             * IMUL (Integer Multiply) performs a signed multiplication of the
             * source operand and the accumulator. If the source is a byte, then
             * it is multiplied by register AL, and the double-length result is
             * returned in AH and AL. If the source is a word, then it is
             * multiplied by register AX, and the double-length result is
             * returned in registers DX and AX. If the upper half of the result
             * (AH for byte source, DX for word source) is not the sign
             * extension of the lower half of the result, CF and OF are set,
             * they indicate that AH of DX contains significant digits of the
             * result. The content of AF, PF, SF and ZF is undefined following
             * execution of IMUL.
             */

            /*
             * AAM
             *
             * AAM (ASCII Adjust for Multiply) corrects the result of a previous
             * multiplication of two valid unpacked decimal operands. A valid 2-
             * digit unpacked decimal number is derived from the content of AH
             * and AL and is returned to AH and AL. The high-order half-bytes of
             * the multiplied operands must have been 0H for AAM to produce a
             * correct result. AAM updates PF, SF and ZF; the content of AF, CF
             * and OF is undefined following execution of AAM.
             */
            case 0xd4: // AAM
                src = getMem(B);
                if (src == 0)
                    break; //TODO Generate a type 0 interrupt.
                ah = al / src & 0xff;
                al = al % src & 0xff;
                setFlags(W, getReg(W, AX));
                break;

            /*
             * Division
             */
            /*
             * DIV source
             *
             * DIV (divide) performs an unsigned division of the accumulator
             * (and its extension) by the source operand. If the source operand
             * is a byte, it is divided into the double-length dividend assumed
             * to be in register AL and AH. The single-length quotient is
             * returned in AL, and the single-length remainder is returned in
             * AH. If the source operand is a word, it is divided into the
             * double-length dividend in register AX and DX. The single-length
             * quotient is returned in AX, and the single-length remainder is
             * returned in DX. If the quotient exceeds the capacity of its
             * destination register (FFH for byte source, FFFFH for word
             * source), as when division by zero is attempted, a type 0
             * interrupt is generated, and the quotient and remainder are
             * undefined. Nonintegral quotients are truncated to integers. The
             * content of AF, CF, OF, PF, SF and ZF is undefined following
             * execution of DIV.
             */

            /*
             * IDIV source
             *
             * IDIV (Integer Divide) performs a signed division of the
             * accumulator (and its extension) by the source operand. If the
             * source operand is a byte, it is divided into the double-length
             * dividend assumed to be in register AL and AH; the single-length
             * quotient is returned in AL, and the single-length remainder is
             * returned in AH. For byte integer division, the maximum position
             * quotient is +127 (7FH) and the minimum negative quotient is -127
             * (81H). If the source operand is a word, it is divided into the
             * double-length dividend in register AX and DX; the single-length
             * quotient is returned in AX, and the single-length remainder is
             * returned in DX. For word integer division, the maximum positive
             * quotient is +32,767 (7FFFH) and the minimum negative quotient is
             * -32,767 (8001H). If the quotient is positive and exceeds the
             * maximum, or is negative and is less than the minimum, the
             * quotient and remainder are undefined, and a type 0 interrupt is
             * generated. In particular, this occurs if division by 0 is
             * attempted. Nonintegral quotients are truncated (toward 0) to
             * integers, and the remainder has the same sign as the dividend.
             * The content of AF, CF, OF, PF, SF and ZF is undefined following
             * IDIV.
             */

            /*
             * AAD
             *
             * AAD (ASCII Adjust for Division) modifies the numerator in AL
             * before dividing two valid unpacked decimal operands so that the
             * quotient produced by the division will be a valid unpacked
             * decimal number. AH must be zero for the subsequent DIV to produce
             * the correct result. The quotient is returned in AL, and the
             * remainder is returned in AH; both high-order half-bytes are
             * zeroed. AAD updates PF, SF and ZF; the content of AF, CF and OF
             * is undefined following execution of AAD.
             */
            case 0xd5: // AAD
                src = getMem(B);
                al = ah * src + al & 0xff;
                ah = 0;
                setFlags(B, al);
                break;

            /*
             * CBW
             *
             * CBW (Convert Byte to Word) extends the sign of the byte in
             * register AL throughout register AH. CBW does not affect any
             * flags. CBW can be used to produce a double-length (word) dividend
             * from a byte prior to performing byte division.
             */
            case 0x98: // CBW
                if ((al & 0x80) == 0x80)
                    ah = 0xff;
                else
                    ah = 0x00;
                break;

            /*
             * CWD
             *
             * CWD (Convert Word to Doubleword) extends the sign of the word in
             * register AX throughout register DX. CWD does not affect any
             * flags. CWD can be used to produce a double-length (doubleword)
             * dividend from a word prior to performing word division.
             */
            case 0x99: // CWD
                if ((ah & 0x80) == 0x80)
                    setReg(W, DX, 0xffff);
                else
                    setReg(W, DX, 0x0000);
                break;

            /*
             * Bit Manipulation Instructions
             *
             * The 8086 provides three groups of instructions for manipulating
             * bits within both bytes and words: logical, shifts and rotates.
             */
            /*
             * Logical
             *
             * The logical instructions include the boolean operators "not,"
             * "and," "inclusive or," and "exclusive or," plus a TEST
             * instruction that sets the flags, but does not alter either of its
             * operands.
             *
             * AND, OR, XOR and TEST affect the flags as follows: the overflow
             * (OF) and carry (CF) flags are always cleared by logical
             * instructions, and the content of the auxiliary carry (AF) flag is
             * always undefined following execution of a logical instruction.
             * The sign (SF), zero (ZF) and parity (PF) flags are always posted
             * to reflect the result of the operation and can be tested by
             * conditional jump instructions. The interpretation of these flags
             * is the same as for arithmetic instructions. SF is set if the
             * result is negative (high-order bit is 1), and is cleared if the
             * result is positive (high-order bit is 0). ZF is set if the result
             * is zero, cleared otherwise. PF is set if the result contains an
             * even number of 1-bits (has even parity) and is cleared if the
             * number of 1-bits is odd (the result has odd parity). Note that
             * NOT has no effect on the flags.
             */
            /*
             * NOT destination
             *
             * NOT inverts the bits (forms the one's complement) of the byte or
             * word operand.
             */

            /*
             * AND destination,source
             *
             * AND performs the logical "and" of the two operands (byte or word)
             * and returns the result to the destination operand. A bit in the
             * result is set if both corresponding bits of the original operands
             * are set; otherwise the bit is cleared.
             */
            // Register/Memory and Register
            case 0x20: // AND REG8/MEM8,REG8
            case 0x21: // AND REG16/MEM16,REG16
            case 0x22: // AND REG8,REG8/MEM8
            case 0x23: // AND REG16,REG16/MEM16
                decode();
                if (d == 0b0) {
                    dst = getRM(w, mod, rm);
                    src = getReg(w, reg);
                } else {
                    dst = getReg(w, reg);
                    src = getRM(w, mod, rm);
                }
                res = dst & src;
                logic(w, res);
                if (d == 0b0)
                    setRM(w, mod, rm, res);
                else
                    setReg(w, reg, res);
                break;

            // Immediate to Accumulator
            case 0x24: // AND AL,IMMED8
            case 0x25: // AND AX,IMMED16
                dst = getReg(w, AX);
                src = getMem(w);
                res = dst & src;
                logic(w, res);
                setReg(w, AX, res);
                break;

            /*
             * OR destination,source
             *
             * OR performs the logical "inclusive or" of the two operands (byte
             * or word) and returns the result to the destination operand. A bit
             * in the result is set if either or both corresponding bits of the
             * original operands are set; otherwise the result bit is cleared.
             */
            // Register/Memory and Register
            case 0x08: // OR REG8/MEM8,REG8
            case 0x09: // OR REG16/MEM16,REG16
            case 0x0a: // OR REG8,REG8/MEM8
            case 0x0b: // OR REG16,REG16/MEM16
                decode();
                if (d == 0b0) {
                    dst = getRM(w, mod, rm);
                    src = getReg(w, reg);
                } else {
                    dst = getReg(w, reg);
                    src = getRM(w, mod, rm);
                }
                res = dst | src;
                logic(w, res);
                if (d == 0b0)
                    setRM(w, mod, rm, res);
                else
                    setReg(w, reg, res);
                break;

            // Immediate to Accumulator
            case 0x0c: // OR AL,IMMED8
            case 0x0d: // OR AX,IMMED16
                dst = getReg(w, AX);
                src = getMem(w);
                res = dst | src;
                logic(w, res);
                setReg(w, AX, res);
                break;

            /*
             * XOR destination,source
             *
             * XOR (Exclusive Or) performs the logical "exclusive or" of the two
             * operands and returns the result to the destination operand. A bit
             * in the result if set if the corresponding bits of the original
             * operands contain opposite values (one is set, the other is
             * cleared); otherwise the result bit is cleared.
             */
            // Register/Memory and Register
            case 0x30: // XOR REG8/MEM8,REG8
            case 0x31: // XOR REG16/MEM16,REG16
            case 0x32: // XOR REG8,REG8/MEM8
            case 0x33: // XOR REG16,REG16/MEM16
                decode();
                if (d == 0b0) {
                    dst = getRM(w, mod, rm);
                    src = getReg(w, reg);
                } else {
                    dst = getReg(w, reg);
                    src = getRM(w, mod, rm);
                }
                res = dst ^ src;
                logic(w, res);
                if (d == 0b0)
                    setRM(w, mod, rm, res);
                else
                    setReg(w, reg, res);
                break;

            // Immediate to Accumulator
            case 0x34: // XOR AL,IMMED8
            case 0x35: // XOR AX,IMMED16
                dst = getReg(w, AX);
                src = getMem(w);
                res = dst ^ src;
                logic(w, res);
                setReg(w, AX, res);
                break;

            /*
             * TEST destination,source
             *
             * TEST performs the logical "and" of the two operands (byte or
             * word), updates the flags, but does not return the result, i.e.,
             * neither operand is changed. If a TEST instruction is followed by
             * a JNZ (jump if not zero) instruction, the jump will be taken if
             * there are any corresponding 1-bits in both operands.
             */
            // Register/Memory and Register
            case 0x84: // TEST REG8/MEM8,REG8
            case 0x85: // TEST REG16/MEM16,REG16
                decode();
                dst = getRM(w, mod, rm);
                src = getReg(w, reg);
                logic(w, dst & src);
                break;

            // Immediate and Accumulator
            case 0xa8: // TEST AL,IMMED8
            case 0xa9: // TEST AX,IMMED16
                dst = getReg(w, AX);
                src = getMem(w);
                logic(w, dst & src);
                break;

            /*
             * Shifts
             *
             * The bits in bytes and words may be shifted arithmetically or
             * logically. Up to 255 shifts may be performed, according to the
             * value of the count operand coded in the instruction. The count
             * may be specified as the constant l, or as register CL, allowing
             * the shift count to be a variable supplied at execution time.
             * Arithmetic shifts may be used to multiply and divide binary
             * numbers by powers of two. Logical shifts can be used to isolate
             * bits in bytes or words.
             *
             * Shift instructions affect the flags as follows. AF is always
             * undefined following a shift operation. PF, SF and ZF are updated
             * normally, as in the logical instructions. CF always contains the
             * value of the last bit shifted out of the destination operand. The
             * content of OF is always undefined following a multibit shift. In
             * a single-bit shift, OF is set if the value of the high-order
             * (sign) bit was changed by the operation; if the sign bit retains
             * its original value, OF is cleared.
             */
            /*
             * SHL/SAL destination,count
             *
             * SHL and SAL (Shift Logical Left and Shift Arithmetic Left)
             * perform the same operation and are physically the same
             * instruction. The destination byte or word is shifted left by the
             * number of bits specified in the count operand. Zeros are shifted
             * in on the right. If the sign bit retains its original value, then
             * OF is cleared.
             */

            /*
             * SHR destination,source
             *
             * SHR (Shift Logical Right) shifts the bits in the destination
             * operand (byte or word) to the right by the number of bits
             * specified in the count operand. Zeros are shifted in on the left.
             * If the sign bit retains its original value, then OF is cleared.
             */

            /*
             * SAR destination,count
             *
             * SAR (Shift Arithmetic Right) shifts the bits in the destination
             * operand (byte or word) to the right by the number of bits
             * specified in the count operand. Bits equal to the original high-
             * order (sign) bit are shifted in on the left, preserving the sign
             * of the original value. Note that SAR does not produce the same
             * result as the dividend of an "equivalent" IDIV instruction if the
             * destination operand is negative and l-bits are shifted out. For
             * example, shifting -5 right by one bit yields -3, while integer
             * division of -5 by 2 yields -2. The difference in the instructions
             * is that IDIV truncates all numbers toward zero, while SAR
             * truncates positive numbers toward zero and negative numbers
             * toward negative infinity.
             */

            /*
             * Rotates
             *
             * Bits in bytes and words also may be rotated. Bits rotated out of
             * an operand are not lost as in a shift, but are "circled" back
             * into the other "end" of the operand. As in the shift
             * instructions, the number of bits to be rotated is taken from the
             * count operand, which may specify either a constant of l, or the
             * CL register. The carry flag may act as an extension of the
             * operand in two of the rotate instructions, allowing a bit to be
             * isolated in CF and then tested by a JC (jump if carry) or JNC
             * (jump if not carry) instruction.
             *
             * Rotates affect only the carry and overflow flags. CF always
             * contains the value of the last bit rotated out. On multibit
             * rotates, the value of OF is always undefined. In single-bit
             * rotates, OF is set if the operation changes the high-order (sign)
             * bit of the destination operand. If the sign bit retains its
             * original value, OF is cleared.
             */
            /*
             * ROL destination,count
             *
             * ROL (Rotate Left) rotates the destination byte or word left by
             * the number of bits specified in the count operand.
             */

            /*
             * ROR destination,count
             *
             * ROR (Rotate Right) operates similar to ROL except that the bits
             * in the destination byte or word are rotated right instead of
             * left.
             */

            /*
             * RCL destination,count
             *
             * RCL (Rotate through Carry Left) rotates the bits in the byte or
             * word destination operand to the left by the number of bits
             * specified in the count operand. The carry flag (CF) is treated as
             * "part of" the destination operand; that is, its value is rotated
             * into the low-order bit of the destination, and itself is replaced
             * by the high-order bit of the destination.
             */

            /*
             * RCR destination,count
             *
             * RCR (Rotate through Carry Right) operates exactly like RCL except
             * that the bits are rotated right instead of left.
             */

            /*
             * String Instructions
             *
             * Five basic string operations, called primitives, allow strings of
             * bytes or words to be operated on, one element (byte or word) at a
             * time. Strings of up to 64k bytes may be manipulated with these
             * instructions. Instructions are available to move, compare and
             * scan for a value, as well as for moving string elements to and
             * from the accumulator. These basic operations may be preceded by a
             * special one-byte prefix that causes the instruction to be
             * repeated by the hardware, allowing long strings to be processed
             * much faster than would be possible with a software loop. The
             * repetitions can be terminated by a variety of conditions, and a
             * repeated operation may be interrupted and resumed.
             *
             * The string instructions operate quite similarly in many respects;
             * the common characteristics are covered here rather than in the
             * descriptions of the individual instructions. A string instruction
             * may have a source operand, a destination operand, or both. The
             * hardware assumes that a source string resides in the current data
             * segment; a segment prefix byte may be used to override this
             * assumption. A destination string must be in the current extra
             * segment. The assembler checks the attributes of the operands to
             * determine if the elements of the strings are bytes or words. The
             * assembler does not, however, use the operand names to address the
             * strings. Rather, the content of register SI (source index) is
             * used as an offset to address the current element of the source
             * string, and the content of register DI (destination index) is
             * taken as the offset of the current destination string element.
             * These registers must be initialized to point to the
             * source/destination strings before executing the string
             * instruction; the LDS, LES and LEA instructions are useful in this
             * regard.
             *
             * The string instructions automatically update SI and/or DI in
             * anticipation of processing the next string element. The setting
             * of DF (the direction flag) determines whether the index registers
             * are auto- incremented (DF = 0) or auto-decremented (DF = I). If
             * byte strings are being processed, SI and/or DI is adjusted by 1;
             * the adjustment is 2 for word strings.
             *
             * If a Repeat prefix has been coded, then register CX (count
             * register) is decremented by 1 after each repetition of the string
             * instruction; therefore, CX must be initialized to the number of
             * repetitions desired before the string instruction is executed. If
             * CX is 0, the string instruction is not executed, and control goes
             * to the following instruction.
             */
            /*
             * REP/REPE/REPZ/REPNE/REPNZ
             *
             * Repeat, Repeat While Equal, Repeat While Zero, Repeat While Not
             * Equal and Repeat While Not Zero are five mnemonics for two forms
             * of the prefix byte that controls repetition of a subsequent
             * string instruction. The different mnemonics are provided to
             * improve program clarity. The repeat prefixes do not affect the
             * flags.
             *
             * REP is used in conjunction with the MOVS (Move String) and STOS
             * (Store String) instructions and is interpreted as "repeat while
             * not end-of-string" (CX not 0). REPE and REPZ operate identically
             * and are physically the same prefix byte as REP. These
             * instructions are used with the CMPS (Compare String) and SCAS
             * (Scan String) instructions and require ZF (posted by these
             * instructions) to be set before initiating the next repetition.
             * REPNE and REPNZ. are two mnemonics for the same prefix byte.
             * These instructions function the same as REPE and REPZ except that
             * the zero flag must be cleared or the repetition is terminated.
             * Note that ZF does not need to be initialized before executing the
             * repeated string instruction.
             *
             * Repeated string sequences are interruptible; the processor will
             * recognize the interrupt before processing the next string
             * element. System interrupt processing is not affected in any way.
             * Upon return from the interrupt, the repeated operation is resumed
             * from the point of interruption. Note, however, that execution
             * does not resume properly if a second or third prefix (i.e.,
             * segment override or LOCK) has been specified in addition to any
             * of the repeat prefixes. The processor "remembers" only one prefix
             * in effect at the time of the interrupt, the prefix that
             * immediately precedes the string instruction. After returning from
             * the interrupt, processing resumes at this point, but any
             * additional prefixes specified are not in effect. If more than one
             * prefix must be used with a string instruction, interrupts may be
             * disabled for the duration of the repeated execution. However,
             * this will not prevent a non-maskable interrupt from being
             * recognized. Also, the time that the system is unable to respond
             * to interrupts may be unacceptable if long strings are being
             * processed.
             */

            /*
             * MOVS destination-string,source-string
             *
             * MOVS (Move String) transfers a byte or a word from the source
             * string (addressed by SI) to the destination string (addressed by
             * DI) and updates SI and DI to point to the next string element.
             * When used in conjunction with REP, MOVS performs a memory-to-
             * memory block transfer.
             */
            /*
             * MOVSB/MOVSW
             *
             * These are alternate mnemonics for the move string instruction.
             * These mnemonics are coded without operands; they explicitly tell
             * the assembler that a byte string (MOVSB) or a word string (MOVSW)
             * is to be moved (when MOVS is coded, the assembler determines the
             * string type from the attributes of the operands). These mnemonics
             * are useful when the assembler cannot determine the attributes of
             * a string, e.g., a section of code is being moved.
             */
            case 0xa4: // MOVS DEST-STR8,SRC-STR8
            case 0xa5: // MOVS DEST-STR16,SRC-STR16
                src = getMem(w, getAddr(os, si));
                setMem(w, getAddr(es, di), src);
                break;

            /*
             * CMPS destination-string,source-string
             *
             * CMPS (Compare String) subtracts the destination byte or word
             * (addressed by DI) from the source byte or word (addressed by SI).
             * CMPS affects the flags but does not alter either operand, updates
             * SI and DI to point to the next string element and updates AF, CF,
             * OF, PF, SF and ZF to reflect the relationship of the destination
             * element to the source element. For example, if a JG (Jump if
             * Greater) instruction follows CMPS, the jump is taken if the
             * destination element is greater than the source element. If CMPS
             * is prefixed with REPE or REPZ, the operation is interpreted as
             * "compare while not end-of-string (CX not zero) and strings are
             * equal (ZF = 1)." If CMPS is preceded by REPNE or REPNZ, the
             * operation is interpreted as "compare while not end-of-string (CX
             * not zero) and strings are not equal (ZF = 0)." Thus, CMPS can be
             * used to find matching or differing string elements.
             */
            case 0xa6: // CMPS DEST-STR8,SRC-STR8
            case 0xa7: // CMPS DEST-STR16,SRC-STR16
                dst = getMem(w, getAddr(es, di));
                src = getMem(w, getAddr(os, si));
                sub(w, src, dst);
                if (rep == 1 && !getFlag(ZF) || rep == 2 && getFlag(ZF))
                    rep = 0;
                break;

            /*
             * SCAS destination-string
             *
             * SCAS (Scan String) subtracts the destination string element (byte
             * or word) addressed by DI from the content of AL (byte string) or
             * AX (word string) and updates the flags, but does not alter the
             * destination string or the accumulator. SCAS also updates DI to
             * point to the next string element and AF, CF, OF, PF, SF and ZF to
             * reflect the relationship of the scan value in AL/AX to the string
             * element. If SCAS is prefixed with REPE or REPZ, the operation is
             * interpreted as "scan while not end-of-string (CX not 0) and
             * string-element = scan-value (ZF = 1)." This form may be used to
             * scan for departure from a given value. If SCAS is prefixed with
             * REPNE or REPNZ, the operation is interpreted as "scan while not
             * end-of-string (CX not 0) and string-element is not equal to
             * scan-value (ZF = 0)." This form may be used to locate a value in
             * a string.
             */
            case 0xae: // SCAS DEST-STR8
            case 0xaf: // SCAS DEST-STR16
                dst = getMem(w, getAddr(es, di));
                src = getReg(w, AX);
                sub(w, src, dst);
                if (rep == 1 && !getFlag(ZF) || rep == 2 && getFlag(ZF))
                    rep = 0;
                break;

            /*
             * LODS source-string
             *
             * LODS (Load String) transfers the byte or word string element
             * addressed by SI to register AL or AX, and updates SI to point to
             * the next element in the string. This instruction is not
             * ordinarily repeated since the accumulator would be overwritten by
             * each repetition, and only the last element would be retained.
             * However, LODS is very useful in software loops as part of a more
             * complex string function built up from string primitives and other
             * instructions.
             */
            case 0xac: // LODS SRC-STR8
            case 0xad: // LODS SRC-STR16
                src = getMem(w, getAddr(os, si));
                setReg(w, AX, src);
                break;

            /*
             * STOS destination-string
             *
             * STOS (Store String) transfers a byte or word from register AL or
             * AX to the string element addressed by DI and updates DI to point
             * to the next location in the string. As a repeated operation, STOS
             * provides a convenient way to initialize a string to a constant
             * value (e.g., to blank out a print line).
             */
            case 0xaa: // STOS DEST-STR8
            case 0xab: // STOS DEST-STR16
                src = getReg(w, AX);
                setMem(w, getAddr(es, di), src);
                break;

            /*
             * Program Transfer Instructions
             *
             * The sequence of execution of instructions in an 8086 program is
             * determined by the content of the code segment register (CS) and
             * the instruction pointer (IP). The CS register contains the base
             * address of the current code segment, the 64k portion of memory
             * from which instructions are presently being fetched. The IP is
             * used as an offset from the beginning of the code segment; the
             * combination of CS and IP points to the memory location from which
             * the next instruction is to be fetched. (Recall that under most
             * operating conditions, the next instruction to be executed has
             * already been fetched from memory and is waiting in the CPU
             * instruction queue.) The program transfer instructions operate on
             * the instruction pointer and on the CS register; changing the
             * content of these causes normal sequential execution to be
             * altered. When a program transfer occurs, the queue no longer
             * contains the correct instruction, and the BIU obtains the next
             * instruction from memory using the new IP and CS values, passes
             * the instruction directly to the EU, and then begins refilling the
             * queue from the new location.
             *
             * Four groups of program transfers are available in the 8086:
             * unconditional transfers, conditional transfers, iteration control
             * instructions and interrupt-related instructions. Only the
             * interrupt- related instruction affect any CPU flags. As will be
             * seen, however, the execution of many of the program transfer
             * instructions is affected by the states of the flags.
             */
            /*
             * Unconditional Transfers
             *
             * The unconditional transfers instructions may transfer control to
             * a target instruction within the current code segment
             * (intrasegment transfer) or to a different code segment
             * (intersegment transfer). (The ASM-86 assembler terms an
             * intrasegment target NEAR and an intersegment target FAR.) The
             * transfer is made unconditionally any time the instruction is
             * executed.
             */
            /*
             * CALL procedure-name
             *
             * CALL activated an out-of-line procedure, saving information on
             * the stack to permit a RET (return) instruction in the procedure
             * to transfer control back to the instruction following the CALL.
             * The assembler generates a different type of CALL instruction
             * depending on whether the programmer has defined the procedure
             * name as NEAR or FAR. For control to return properly, the type of
             * CALL instruction must match the type of RET instruction that
             * exists from the procedure. (The potential for a mismatch exists
             * if the procedure and the CALL are contained in separately
             * assembled programs.) Different forms of the CALL instruction
             * allow the address of the target procedure to be obtained from the
             * instruction itself (direct CALL) or from a memory location or
             * register referenced by the instruction (indirect CALL). In the
             * following descriptions, bear in mind that the processor
             * automatically adjusts IP to point to the next instruction to be
             * executed before saving it on the stack.
             *
             * For an intrasegment direct CALL, SP (the stack pointer) is
             * decremented by two and IP is pushed onto the stack. The relative
             * displacement (up to ±32k) of the target procedure from the CALL
             * instruction is then added to the instruction pointer. This form
             * of the CALL instruction is "self-relative" and is appropriate for
             * position independent (dynamically relocatable) routines in which
             * the CALL and its target are in the same segment and are moved
             * together.
             *
             * An intrasegment indirect CALL may be made through memory or
             * through a register. SP is decremented by two and IP is pushed
             * onto the stack. The offset of the target procedure is obtained
             * from the memory word or 16-bit general register referenced in the
             * instruction and replaces IP.
             *
             * For an intersegment direct CALL, SP is decremented by two, and CS
             * is pushed onto the stack. CS is replaced by the segment word
             * contained in the instruction. SP again is decremented by two. IP
             * is pushed onto the stack and is replaced by the offset word
             * contained in the instruction.
             *
             * For an intersegment indirect CALL (which only may be made through
             * memory), SP is decremented by two, and CS is pushed onto the
             * stack. CS is then replaced by the content of the second word of
             * the doubleword memory pointer referenced by the instruction. SP
             * again is decremented by two, and IP is pushed onto the stack and
             * is replaced by the content of the first word of the doubleword
             * pointer referenced by the instruction.
             */
            // Direct with Segment
            case 0xe8: // CALL NEAR-PROC
                dst = getMem(W);
                dst = signconv(W, dst);
                push(ip);
                ip += dst;
                break;

            // Direct Intersegment
            case 0x9a: // CALL FAR-PROC
                dst = getMem(W);
                src = getMem(W);
                push(cs);
                push(ip);
                ip = dst;
                cs = src;
                break;

            /*
             * RET optional-pop-value
             *
             * RET (Return) transfers control from a procedure back to the
             * instruction following the CALL that activated the procedure. The
             * assembler generates an intrasegment RET if the programmer has
             * defined the procedure NEAR, or an intersegment RET if the
             * procedure has been defined as FAR. RET pops the word at the top
             * of the stack (pointed to by the register SP) into the instruction
             * pointer and increments SP by two. If RET is intersegment, the
             * word at the top of the stack is popped into the CS register, and
             * SP is again incremented by two. If an optional pop value has been
             * specified, RET adds that value to SP. This feature may be used to
             * discard parameters pushed onto the stack before the execution of
             * the CALL instruction.
             */
            // Within Segment
            case 0xc3: // RET (intrasegment)
                ip = pop();
                break;

            // Within Seg Adding Immed to SP
            case 0xc2: // RET IMMED16 (intraseg)
                src = getMem(W);
                ip = pop();
                sp += src;
                break;

            // Intersegment
            case 0xcb: // RET (intersegment)
                ip = pop();
                cs = pop();
                break;

            // Intersegment Adding Immediate to SP
            case 0xca: // RET IMMED16 (intersegment)
                src = getMem(W);
                ip = pop();
                cs = pop();
                sp += src;
                break;

            /*
             * JMP target
             *
             * JMP unconditionally transfers control to the target location.
             * Unlike a CALL instruction, JMP does not save any information on
             * the stack, and no return to the instruction following the JMP is
             * expected. Like CALL, the address of the target operand may be
             * obtained from the instruction itself (direct JMP) or from memory
             * or a register referenced by the instruction (indirect JMP).
             *
             * An intrasegment direct JMP changes the instruction pointer by
             * adding the relative displacement of the target from the JMP
             * instruction. If the assembler can determine that the target is
             * within 127 bytes of the JMP, it automatically generates a two-
             * byte form of this instruction called a SHORT JMP; otherwise, it
             * generates a NEAR JMP that can address a target within ±32k.
             * Intrasegment direct JMPS are self-relative and are appropriate in
             * position-independent (dynamically relocatable) routines in which
             * the JMP and its target are in the same segment and are moved
             * together.
             *
             * An intrasegment indirect JMP may be made either through memory or
             * through a 16-bit general register. In the first case, the content
             * of the word referenced by the instruction replaces the
             * instruction pointer. In the second case, the new IP value is
             * taken from the register named in the instruction.
             *
             * An intersegment direct JMP replaces IP and CS with values
             * contained in the instruction.
             *
             * An intersegment indirect JMP may be made only through memory. The
             * first word of the doubleword pointer referenced by the
             * instruction replaces IP, and the second word replaces CS.
             */
            // Direct within Segment
            case 0xe9: // JMP NEAR-LABEL
                dst = getMem(W);
                dst = signconv(W, dst);
                ip += dst;
                break;

            // Direct within Segment-Short
            case 0xeb: // JMP SHORT-LABEL
                dst = getMem(B);
                dst = signconv(B, dst);
                ip += dst;
                break;

            // Direct Intersegment
            case 0xea: // JMP FAR-LABEL
                dst = getMem(W);
                src = getMem(W);
                ip = dst;
                cs = src;
                break;

            /*
             * Conditional Transfers
             *
             * The conditional transfer instructions are jumps that may or may
             * not transfer control depending on the state of the CPU flags at
             * the time the instruction is executed. The 18 instructions each
             * test a different combination of flags for a conditional. If the
             * condition is "true," then control is transferred to the target
             * specified in the instruction. If the condition is "false," then
             * control passes to the instruction that follows the conditional
             * jump. All conditional jumps are SHORT, that is, the target must
             * be in the current code segment and within -128 to +127 bytes of
             * the first byte of the next instruction (JMP 00H jumps to the
             * first byte of the next instruction). Since the jump is made by
             * adding the relative displacement of the target to the instruction
             * pointer, all conditional jumps are self-relative and are
             * appropriate for position-independent routines.
             */
            /*
             * JO
             *
             * Jump if overflow - OF=1.
             */
            case 0x70: // JO SHORT-LABEL
                dst = getMem(B);
                dst = signconv(B, dst);
                if (getFlag(OF))
                    ip += dst;
                break;

            /*
             * JNO
             *
             * Jump if not overflow - OF=0.
             */
            case 0x71: // JNO SHORT-LABEL
                dst = getMem(B);
                dst = signconv(B, dst);
                if (!getFlag(OF))
                    ip += dst;
                break;

            /*
             * JB/JNAE/JC
             *
             * Jump if below/not above nor equal/carry - CF=1.
             */
            case 0x72: // JB/JNAE/JC SHORT-LABEL
                dst = getMem(B);
                dst = signconv(B, dst);
                if (getFlag(CF))
                    ip += dst;
                break;

            /*
             * JNE/JAE/JNC
             *
             * Jump if not below/above or equal/not carry - CF=0.
             */
            case 0x73: // JNB/JAE/JNC SHORT-LABEL
                dst = getMem(B);
                dst = signconv(B, dst);
                if (!getFlag(CF))
                    ip += dst;
                break;

            /*
             * JE/JZ
             *
             * Jump if equal/zero - ZF=1.
             */
            case 0x74: // JE/JZ SHORT-LABEL
                dst = getMem(B);
                dst = signconv(B, dst);
                if (getFlag(ZF))
                    ip += dst;
                break;

            /*
             * JNE/JNZ
             *
             * Jump if not equal/not zero - ZF=0.
             */
            case 0x75: // JNE/JNZ SHORT-LABEL
                dst = getMem(B);
                dst = signconv(B, dst);
                if (!getFlag(ZF))
                    ip += dst;
                break;

            /*
             * JBE/JNA
             *
             * Jump if below or equal/not above - (CF or ZF)=1.
             */
            case 0x76: // JBE/JNA SHORT-LABEL
                dst = getMem(B);
                dst = signconv(B, dst);
                if (getFlag(CF) | getFlag(ZF))
                    ip += dst;
                break;

            /*
             * JNBE/JA
             *
             * Jump if not below nor equal/above - (CF or ZF)=0.
             */
            case 0x77: // JNBE/JA SHORT-LABEL
                dst = getMem(B);
                dst = signconv(B, dst);
                if (!(getFlag(CF) | getFlag(ZF)))
                    ip += dst;
                break;

            /*
             * JS
             *
             * Jump if sign - SF=1.
             */
            case 0x78: // JS SHORT-LABEL
                dst = getMem(B);
                dst = signconv(B, dst);
                if (getFlag(SF))
                    ip += dst;
                break;

            /*
             * JNS
             *
             * Jump if not sign - SF=0.
             */
            case 0x79: // JNS SHORT-LABEL
                dst = getMem(B);
                dst = signconv(B, dst);
                if (!getFlag(SF))
                    ip += dst;
                break;

            /*
             * JP/JPE
             *
             * Jump if parity/parity equal - PF=1.
             */
            case 0x7a: // JP/JPE SHORT-LABEL
                dst = getMem(B);
                dst = signconv(B, dst);
                if (getFlag(PF))
                    ip += dst;
                break;

            /*
             * JNP/JPO
             *
             * Jump if not parity/parity odd - PF=0.
             */
            case 0x7b: // JNP/JPO SHORT-LABEL
                dst = getMem(B);
                dst = signconv(B, dst);
                if (!getFlag(PF))
                    ip += dst;
                break;

            /*
             * JL/JNGE
             *
             * Jump if less/not greater nor equal - (SF xor OF)=1.
             */
            case 0x7c: // JL/JNGE SHORT-LABEL
                dst = getMem(B);
                dst = signconv(B, dst);
                if (getFlag(SF) ^ getFlag(OF))
                    ip += dst;
                break;

            /*
             * JNL/JGE
             *
             * Jump if not less/greater or equal - (SF xor OF)=0.
             */
            case 0x7d: // JNL/JGE SHORT-LABEL
                dst = getMem(B);
                dst = signconv(B, dst);
                if (!(getFlag(SF) ^ getFlag(OF)))
                    ip += dst;
                break;

            /*
             * JLE/JNG
             *
             * Jump if less or equal/not greater - ((SF xor OF) or ZF)=1.
             */
            case 0x7e: // JLE/JNG SHORT-LABEL
                dst = getMem(B);
                dst = signconv(B, dst);
                if (getFlag(SF) ^ getFlag(OF) | getFlag(ZF))
                    ip += dst;
                break;

            /*
             * JNLE/JG
             *
             * Jump if not less nor equal/greater - ((SF xor OF) or ZF)=0.
             */
            case 0x7f: // JNLE/JG SHORT-LABEL
                dst = getMem(B);
                dst = signconv(B, dst);
                if (!(getFlag(SF) ^ getFlag(OF) | getFlag(ZF)))
                    ip += dst;
                break;

            /*
             * Iteration Control
             *
             * The iteration control instructions can be used to regulate the
             * repetition of software loops. These instructions use the CX
             * register as a counter. Like the conditional transfers, the
             * iteration control instructions are self-relative and may only
             * transfer to targets that are within -128 to +127 bytes of
             * themselves, i.e., they are SHORT transfers.
             */
            /*
             * LOOP short-label
             *
             * LOOP decrements CX by 1 and transfers control to the target
             * operand if CX is not 0; otherwise the instruction following LOOP
             * is executed.
             */
            case 0xe2: // LOOP SHORT-LABEL
                dst = getMem(B);
                dst = signconv(B, dst);
                src = getReg(W, CX) - 1;
                setReg(W, CX, src);
                if (src != 0)
                    ip += dst;
                break;

            /*
             * LOOPE/LOOPZ short-label
             *
             * LOOPE and LOOPZ (Loop While Equal and Loop While Zero) are
             * different mnemonics for the same instruction (similar to the REPE
             * and REPZ repeat prefixes). CX is decremented by 1, and control is
             * transferred to the target operand if CX is not 0 and if ZF is
             * set; otherwise the instruction following LOOPE/LOOPZ is executed.
             */
            case 0xe1: // LOOPE/LOOPZ SHORT-LABEL
                dst = getMem(B);
                dst = signconv(B, dst);
                src = getReg(W, CX) - 1;
                setReg(W, CX, src);
                if (src != 0 && getFlag(ZF))
                    ip += dst;
                break;

            /*
             * LOOPNE/LOOPNZ short-label
             *
             * LOOPNE and LOOPNZ (Loop While Not Equal and Loop While Not Zero)
             * are also synonyms for the same instruction. CX is decremented by
             * 1, and control is transferred to the target operand if CX is not
             * 0 and if ZF is clear; otherwise the next sequential instruction
             * is executed.
             */
            case 0xe0: // LOOPNE/LOOPNZ SHORT-LABEL
                dst = getMem(B);
                dst = signconv(B, dst);
                src = getReg(W, CX) - 1;
                setReg(W, CX, src);
                if (src != 0 && !getFlag(ZF))
                    ip += dst;
                break;

            /*
             * JCXZ short-label
             *
             * JCXZ (Jump If CX Zero) transfers control to the target operand if
             * CX is O. This instruction is useful at the beginning of a loop;
             * to bypass the loop if CX has a zero value, i.e., to execute the
             * loop zero times.
             */
            case 0xe3: // JCXZ SHORT-LABEL
                dst = getMem(B);
                dst = signconv(B, dst);
                if (getReg(W, CX) == 0)
                    ip += dst;
                break;

            /*
             * Processor Control Instructions
             *
             * These instructions allow programs to control various CPU
             * functions. One group of instructions updates flags, and another
             * group is used primarily for synchronizing the 8086 with external
             * events. A final instruction causes the CPU to do nothing. Except
             * for the flag operations, none of the processor control
             * instructions affect the flags.
             */
            /*
             * Flag Operations
             */
            /*
             * CLC
             *
             * CLC (Clear Carry flag) zeroes the carry flag (CF) and affects no
             * other flags. It (and CMC and STC) is useful in conjunction with
             * the RCL and RCR instructions.
             */
            case 0xf8: // CLC
                setFlag(CF, false);
                break;

            /*
             * CMC
             *
             * CMC (Complement Carry flag) "toggles" CF to its opposite state
             * and affects no other flags.
             */
            case 0xf5: // CMC
                setFlag(CF, !getFlag(CF));
                break;

            /*
             * STC
             *
             * STC (Set Carry flag) sets CF to 1 and affects no other flags.
             */
            case 0xf9: // STC
                setFlag(CF, true);
                break;

            /*
             * CLD
             *
             * CLD (Clear Direction flag) zeroes DF causing the string
             * instructions to auto-increment the SI and/or DI index registers.
             * CLD does not affect any other flags.
             */
            case 0xfc: // CLD
                setFlag(DF, false);
                break;

            /*
             * STD
             *
             * STD (Set Direction flag) sets DF to 1 causing the string
             * instructions to auto-decrement the SI and/or DI index registers.
             * STD does not affect any other flags.
             */
            case 0xfd: // STD
                setFlag(DF, true);
                break;

            /*
             * CLI
             *
             * CLI (Clear Interrupt-enable flag) zeroes IF. When the interrupt-
             * enable flag is cleared, the 8086 and 8088 do not recognize an
             * external interrupt request that appears on the INTR line; in
             * other words maskable interrupts are disabled. A non-maskable
             * interrupt appearing on the NMI line, however, is honored, as is a
             * software interrupt. CLI does not affect any other flags.
             */
            case 0xfa: // CLI
                setFlag(IF, false);
                break;

            /*
             * STI
             *
             * STI (Set Interrupt-enable flag) sets IF to 1, enabling processor
             * recognition of maskable interrupt requests appearing on the INTR
             * line. Note however, that a pending interrupt will not actually be
             * recognized until the instruction following STI has executed. STI
             * does not affect any other flags.
             */
            case 0xfb: // STI
                setFlag(IF, true);
                break;

            /*
             * External Synchronization
             */
            /*
             * HLT
             *
             * HLT (Halt) causes the 8086 to enter the halt state. The processor
             * leaves the halt state upon activation of the RESET line, upon
             * receipt of a non-maskable interrupt request on NMI, or, if
             * interrupts are enabled, upon receipt of a maskable interrupt
             * request on INTR. HLT does not affect any flags. It may be used as
             * an alternative to an endless software loop in situations where a
             * program must wait for an interrupt.
             */
            case 0xf4: // HLT
                return false;

            /*
             * WAIT
             *
             * WAIT causes the CPU to enter the wait state while its /TEST line
             * is not active. WAIT does not affect any flags.
             */
            case 0x9b: // WAIT
                break;

            /*
             * ESC external-opcode, source
             *
             * ESC (Escape) provides a means for an external processor to obtain
             * an opcode and possibly a memory operand from the 8086. The
             * external opcode is a 6-bit immediate constant that the assembler
             * encodes in the machine instruction it builds. An external
             * processor may monitor the system bus and capture this opcode when
             * the ESC is fetched. If the source operand is a register, the
             * processor does nothing. If the source operand is a memory
             * variable, the processor obtains the operand from memory and
             * discards it. An external processor may capture the memory operand
             * when the processor reads it from memory.
             */
            case 0xd8: // ESC OPCODE,SOURCE
            case 0xd9: // ESC OPCODE,SOURCE
            case 0xda: // ESC OPCODE,SOURCE
            case 0xdb: // ESC OPCODE,SOURCE
            case 0xdc: // ESC OPCODE,SOURCE
            case 0xdd: // ESC OPCODE,SOURCE
            case 0xde: // ESC OPCODE,SOURCE
            case 0xdf: // ESC OPCODE,SOURCE
                decode();
                break;

            /*
             * LOCK
             *
             * LOCK is a one-byte prefix that causes the 8086 (configured in
             * maximum mode) to assert its bus /LOCK signal while the following
             * instruction executes. LOCK does not affect any flags.
             */
            case 0xf0: // LOCK
                break;

            /*
             * No Operation
             */
            /*
             * NOP
             *
             * NOP (No Operation) causes the CPU to do nothing. NOP does not
             * affect any flags.
             */
            case 0x90: // NOP
                break;

            /*
             * Extensions
             */
            /*
             * GROUP 1
             */
            case 0x80:
                // ADD REG8/MEM8,IMMED8
                // OR REG8/MEM8,IMMED8
                // ADC REG8/MEM8,IMMED8
                // SBB REG8/MEM8,IMMED8
                // AND REG8/MEM8,IMMED8
                // SUB REG8/MEM8,IMMED8
                // XOR REG8/MEM8,IMMED8
                // CMP REG8/MEM8,IMMED8
            case 0x81:
                // ADD REG16/MEM16,IMMED16
                // OR REG16/MEM16,IMMED16
                // ADC REG16/MEM16,IMMED16
                // SBB REG16/MEM16,IMMED16
                // AND REG16/MEM16,IMMED16
                // SUB REG16/MEM16,IMMED16
                // XOR REG16/MEM16,IMMED16
                // CMP REG16/MEM16,IMMED16
            case 0x82:
                // ADD REG8/MEM8,IMMED8
                // ADC REG8/MEM8,IMMED8
                // SBB REG8/MEM8,IMMED8
                // SUB REG8/MEM8,IMMED8
                // CMP REG8/MEM8,IMMED8
            case 0x83:
                // ADD REG16/MEM16,IMMED8
                // ADC REG16/MEM16,IMMED8
                // SBB REG16/MEM16,IMMED8
                // SUB REG16/MEM16,IMMED8
                // CMP REG16/MEM16,IMMED8
                decode();
                dst = getRM(w, mod, rm);
                src = getMem(B);
                if (op == 0x81)
                    src |= getMem(B) << 8;
                // Perform sign extension if needed.
                else if (op == 0x83 && (src & SIGN[B]) > 0)
                    src |= 0xff00;
                switch (reg) {
                case 0b000: // ADD
                    res = add(w, dst, src);
                    setRM(w, mod, rm, res);
                    break;
                case 0b001: // OR
                    if (op == 0x80 || op == 0x81) {
                        res = dst | src;
                        logic(w, res);
                        setRM(w, mod, rm, res);
                        break;
                    }
                    break;
                case 0b010: // ADC
                    res = adc(w, dst, src);
                    setRM(w, mod, rm, res);
                    break;
                case 0b011: // SBB
                    res = sbb(w, dst, src);
                    setRM(w, mod, rm, res);
                    break;
                case 0b100: // AND
                    if (op == 0x80 || op == 0x81) {
                        res = dst & src;
                        logic(w, res);
                        setRM(w, mod, rm, res);
                    }
                    break;
                case 0b101: // SUB
                    res = sub(w, dst, src);
                    setRM(w, mod, rm, res);
                    break;
                case 0b110: // XOR
                    if (op == 0x80 || op == 0x81) {
                        res = dst ^ src;
                        logic(w, res);
                        setRM(w, mod, rm, res);
                    }
                    break;
                case 0b111: // CMP
                    sub(w, dst, src);
                    break;
                }
                break;

            /*
             * GROUP 1A
             */
            case 0x8f:
                // POP REG16/MEM16
                decode();
                switch (reg) {
                case 0b000: // POP
                    src = pop();
                    setRM(w, mod, rm, src);
                    break;
                }
                break;

            /*
             * GROUP 2
             */
            case 0xd0:
                // ROL REG8/MEM8,1
                // ROR REG8/MEM8,1
                // RCL REG8/MEM8,1
                // RCR REG8/MEM8,1
                // SAL/SHL REG8/MEM8,1
                // SHR REG8/MEM8,1
                // SAR REG8/MEM8,1
            case 0xd1:
                // ROL REG16/MEM16,1
                // ROR REG16/MEM16,1
                // RCL REG16/MEM16,1
                // RCR REG16/MEM16,1
                // SAL/SHL REG16/MEM16,1
                // SHR REG16/MEM16,1
                // SAR REG16/MEM16,1
            case 0xd2:
                // ROL REG8/MEM8,CL
                // ROR REG8/MEM8,CL
                // RCL REG8/MEM8,CL
                // RCR REG8/MEM8,CL
                // SAL/SHL REG8/MEM8,CL
                // SHR REG8/MEM8,CL
                // SAR REG8/MEM8,CL
            case 0xd3:
                // ROL REG16/MEM16,CL
                // ROR REG16/MEM16,CL
                // RCL REG16/MEM16,CL
                // RCR REG16/MEM16,CL
                // SAL/SHL REG16/MEM16,CL
                // SHR REG16/MEM16,CL
                // SAR REG16/MEM16,CL
            {
                decode();
                dst = getRM(w, mod, rm);
                src = op == 0xd0 || op == 0xd1 ? 1 : cl;
                boolean tempCF;
                switch (reg) {
                case 0b000: // ROL
                    for (int cnt = 0; cnt < src; ++cnt) {
                        tempCF = msb(w, dst);
                        dst <<= 1;
                        dst |= tempCF ? 0b1 : 0b0;
                        dst &= MASK[w];
                    }
                    setFlag(CF, (dst & 0b1) == 0b1);
                    if (src == 1)
                        setFlag(OF, msb(w, dst) ^ getFlag(CF));
                    break;
                case 0b001: // ROR
                    for (int cnt = 0; cnt < src; ++cnt) {
                        tempCF = (dst & 0b1) == 0b1;
                        dst >>>= 1;
                        dst |= (tempCF ? 1 : 0) * SIGN[w];
                        dst &= MASK[w];
                    }
                    setFlag(CF, msb(w, dst));
                    if (src == 1)
                        setFlag(OF, msb(w, dst) ^ msb(w, dst << 1));
                    break;
                case 0b010: // RCL
                    for (int cnt = 0; cnt < src; ++cnt) {
                        tempCF = msb(w, dst);
                        dst <<= 1;
                        dst |= getFlag(CF) ? 0b1 : 0b0;
                        dst &= MASK[w];
                        setFlag(CF, tempCF);
                    }
                    if (src == 1)
                        setFlag(OF, msb(w, dst) ^ getFlag(CF));
                    break;
                case 0b011: // RCR
                    if (src == 1)
                        setFlag(OF, msb(w, dst) ^ getFlag(CF));
                    for (int cnt = 0; cnt < src; ++cnt) {
                        tempCF = (dst & 0b1) == 0b1;
                        dst >>>= 1;
                        dst |= (getFlag(CF) ? 1 : 0) * SIGN[w];
                        dst &= MASK[w];
                        setFlag(CF, tempCF);
                    }
                    break;
                case 0b100: // SAL/SHL
                    for (int cnt = 0; cnt < src; ++cnt) {
                        setFlag(CF, (dst & SIGN[w]) == SIGN[w]);
                        dst <<= 1;
                        dst &= MASK[w];
                    }
                    // Determine overflow.
                    if (src == 1)
                        setFlag(OF, (dst & SIGN[w]) == SIGN[w] ^ getFlag(CF));
                    if (src > 0)
                        setFlags(w, dst);
                    break;
                case 0b101: // SHR
                    // Determine overflow.
                    if (src == 1)
                        setFlag(OF, (dst & SIGN[w]) == SIGN[w]);
                    for (int cnt = 0; cnt < src; ++cnt) {
                        setFlag(CF, (dst & 0b1) == 0b1);
                        dst >>>= 1;
                        dst &= MASK[w];
                    }
                    if (src > 0)
                        setFlags(w, dst);
                    break;
                case 0b111: // SAR
                    // Determine overflow.
                    if (src == 1)
                        setFlag(OF, false);
                    for (int cnt = 0; cnt < src; ++cnt) {
                        setFlag(CF, (dst & 0b1) == 0b1);
                        dst = signconv(w, dst);
                        dst >>= 1;
                        dst &= MASK[w];
                    }
                    if (src > 0)
                        setFlags(w, dst);
                    break;
                }
                setRM(w, mod, rm, dst);
                break;
            }

            /*
             * GROUP 3
             */
            case 0xf6:
                // TEST REG8/MEM8
                // NOT REG8/MEM8
                // NEG REG8/MEM8
                // MUL REG8/MEM8
                // IMUL REG8/MEM8
                // DIV REG8/MEM8
                // IDIV REG8/MEM8
            case 0xf7:
                // TEST REG16/MEM16
                // NOT REG16/MEM16
                // NEG REG16/MEM16
                // MUL REG16/MEM16
                // IMUL REG16/MEM16
                // DIV REG16/MEM16
                // IDIV REG16/MEM16
                decode();
                src = getRM(w, mod, rm);
                switch (reg) {
                case 0b000: // TEST
                    dst = getMem(w);
                    logic(w, dst & src);
                    break;
                case 0b010: // NOT
                    setRM(w, mod, rm, ~src);
                    break;
                case 0b011: // NEG
                    dst = sub(w, 0, src);
                    setFlag(CF, dst > 0);
                    setRM(w, mod, rm, dst);
                    break;
                case 0b100: // MUL
                    if (w == B) {
                        dst = al;
                        res = dst * src & 0xffff;
                        setReg(W, AX, res);
                        if (ah > 0) {
                            setFlag(CF, true);
                            setFlag(OF, true);
                        } else {
                            setFlag(CF, false);
                            setFlag(OF, false);
                        }
                    } else {
                        dst = getReg(W, AX);
                        final long lres = (long) dst * (long) src & 0xffffffff;
                        setReg(W, AX, (int) lres);
                        setReg(W, DX, (int) (lres >>> 16));
                        if (getReg(W, DX) > 0) {
                            setFlag(CF, true);
                            setFlag(OF, true);
                        } else {
                            setFlag(CF, false);
                            setFlag(OF, false);
                        }
                    }
                    break;
                case 0b101: // IMUL
                    if (w == B) {
                        src = signconv(B, src);
                        dst = al;
                        dst = signconv(B, dst);
                        res = dst * src & 0xffff;
                        setReg(W, AX, res);
                        if (ah > 0x00 && ah < 0xff) {
                            setFlag(CF, true);
                            setFlag(OF, true);
                        } else {
                            setFlag(CF, false);
                            setFlag(OF, false);
                        }
                    } else {
                        src = signconv(W, src);
                        dst = ah << 8 | al;
                        dst = signconv(W, dst);
                        final long lres = (long) dst * (long) src & 0xffffffff;
                        setReg(W, AX, (int) lres);
                        setReg(W, DX, (int) (lres >>> 16));
                        final int dx = getReg(W, DX);
                        if (dx > 0x0000 && dx < 0xffff) {
                            setFlag(CF, true);
                            setFlag(OF, true);
                        } else {
                            setFlag(CF, false);
                            setFlag(OF, false);
                        }
                    }
                    break;
                case 0b110: // DIV
                    if (src == 0)
                        break; //TODO Generate a type 0 interrupt.
                    if (w == B) {
                        dst = ah << 8 | al;
                        res = dst / src & 0xffff;
                        if (res > 0xff)
                            break; //TODO Generate a type 0 interrupt.
                        else {
                            al = res & 0xff;
                            ah = dst % src & 0xff;
                        }
                    } else {
                        final long ldst = (long) getReg(W, DX) << 16 | getReg(W, AX);
                        long lres = ldst / src & 0xffffffff;
                        if (lres > 0xffff)
                            break; //TODO Generate a type 0 interrupt.
                        else {
                            setReg(W, AX, (int) lres);
                            lres = ldst % src & 0xffff;
                            setReg(W, DX, (int) lres);
                        }
                    }
                    break;
                case 0b111: // IDIV
                    if (src == 0)
                        break; //TODO Generate a type 0 interrupt.
                    if (w == B) {
                        src = signconv(B, src);
                        dst = getReg(W, AX);
                        dst = signconv(W, dst);
                        res = dst / src & 0xffff;
                        if (res > 0x007f && res < 0xff81)
                            break; //TODO Generate a type 0 interrupt.
                        else {
                            al = res & 0xff;
                            ah = dst % src & 0xff;
                        }
                    } else {
                        src = signconv(W, src);
                        long ldst = (long) getReg(W, DX) << 16 | getReg(W, AX);
                        // Do sign conversion manually.
                        ldst = ldst << 32 >> 32;
                        long lres = ldst / src & 0xffffffff;
                        if (lres > 0x00007fff | lres < 0xffff8000)
                            break; //TODO Generate a type 0 interrupt.
                        else {
                            setReg(W, AX, (int) lres);
                            lres = ldst % src & 0xffff;
                            setReg(W, DX, (int) lres);
                        }
                    }
                    break;
                }
                break;

            /*
             * GROUP 4
             */
            case 0xfe:
                // INC REG8/MEM8
                // DEC REG8/MEM8
                decode();
                src = getRM(w, mod, rm);
                switch (reg) {
                case 0b000: // INC
                    res = inc(w, src);
                    setRM(w, mod, rm, res);
                    break;
                case 0b001: // DEC
                    res = dec(w, src);
                    setRM(w, mod, rm, res);
                    break;
                }
                break;

            /*
             * GROUP 5
             */
            case 0xff:
                // INC REG16/MEM16
                // DEC REG16/MEM16
                // CALL REG16/MEM16 (intra)
                // CALL MEM16 (intersegment)
                // JMP REG16/MEM16 (intra)
                // JMP MEM16 (intersegment)
                // PUSH REG16/MEM16
                decode();
                src = getRM(w, mod, rm);
                switch (reg) {
                case 0b000: // INC
                    res = inc(w, src);
                    setRM(w, mod, rm, res);
                    break;
                case 0b001: // DEC
                    res = dec(w, src);
                    setRM(w, mod, rm, res);
                    break;
                case 0b010: // CALL
                    push(ip);
                    ip = src;
                    break;
                case 0b011: // CALL
                    push(cs);
                    push(ip);
                    dst = getEA(mod, rm);
                    ip = getMem(W, dst);
                    cs = getMem(W, dst + 2);
                    break;
                case 0b100: // JMP
                    ip = src;
                    break;
                case 0b101: // JMP
                    dst = getEA(mod, rm);
                    ip = getMem(W, dst);
                    cs = getMem(W, dst + 2);
                    break;
                case 0b110: // PUSH
                    push(src);
                    break;
                }
                break;
            }

            // Repeat prefix present.
            if (rep > 0) {
                // Adjust SI/DI by delta.
                int delta;
                if (w == B) {
                    if (!getFlag(DF))
                        delta = 1;
                    else
                        delta = -1;
                } else {
                    if (!getFlag(DF))
                        delta = 2;
                    else
                        delta = -2;
                }
                si += delta;
                di += delta;
            }
        } while (rep > 0);
        return true;
    }
}
