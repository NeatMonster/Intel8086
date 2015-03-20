package fr.neatmonster.intel8086;

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
    private static final int CF     = 1 << 0;

    /**
     * PF (parity flag)
     *
     * If the low-order eight bits of an arithmetic or logical operation is
     * zero contain an even number of 1-bits, then the parity flag is set,
     * otherwise it is cleared. PF is provided for 8080/8085 compatibility; it
     * can also be used to check ASCII characters for correct parity.
     */
    private static final int PF     = 1 << 2;

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
    private static final int AF     = 1 << 4;

    /**
     * ZF (zero flag)
     *
     * If the result of an arithmetic or logical operation is zero, then ZF is
     * set; otherwise ZF is cleared. A conditional jump instruction can be used
     * to alter the flow of the program if the result is or is not zero.
     */
    private static final int ZF     = 1 << 6;

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
    private static final int SF     = 1 << 7;

    /**
     * TF (trap flag)
     *
     * Settings TF puts the processor into single-step mode for debugging. In
     * this mode, the CPU automatically generates an internal interrupt after
     * each instruction, allowing a program to be inspected as it executes
     * instruction by instruction.
     */
    private static final int TF     = 1 << 8;

    /**
     * IF (interrupt-enable flag)
     *
     * Setting IF allows the CPU to recognize external (maskable) interrupt
     * requests. Clearing IF disables these interrupts. IF has no affect on
     * either non-maskable external or internally generated interrupts.
     */
    private static final int IF     = 1 << 9;

    /**
     * DF (direction flag)
     *
     * Setting DF causes string instructions to auto-decrement; that is, to
     * process strings from the high addresses to low addresses, or from "right
     * to left". Clearing DF causes string instructions to auto-increment, or
     * to process strings from "left to right."
     */
    private static final int DF     = 1 << 10;

    /**
     * OF (overflow flag)
     *
     * If the result of an operation is too large a positive number, or too
     * small a negative number to fit in the destination operand (excluding the
     * sign bit), then OF is set; otherwise OF is cleared. OF thus indicates
     * signed arithmetic overflow; it can be tested with a conditional jump or
     * the INFO (interrumpt on overflow) instruction. OF may be ignored when
     * performing unsigned arithmetic.
     */

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
    private int              ah, al;

    /**
     * CX (count)
     *
     * Implicit Use:
     * CX - String Operations, Loops
     * CL - Variable Shift and Rotate
     */
    private int              ch, cl;

    /**
     * DX (data)
     *
     * Implicit Use:
     * DX - Word Multiply, Word Divide, Indirect I/O
     */
    private int              dh, dl;

    /**
     * BX (base)
     *
     * Implicit Use:
     * BX - Translate
     */
    private int              bh, bl;

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
    private int              sp;

    /**
     * BP (base pointer)
     *
     * When register BP, the pointer register, is designated as a base register
     * register in an instruction, the variable is assumed to reside in the
     * current stack segment. Register BP thus provides a convenient way to
     * address data on the stack; BP can be sued, however, to access data in
     * any of the other currently addressable segments.
     */
    private int              bp;

    /**
     * SI (source index)
     *
     * String are addressed differently than other variables. The source
     * operand of a string instruction is assumed to lie in the current data
     * segment, but another currently addressable segment may be specified. Its
     * offset is taken from the register SI, the source index register.
     */
    private int              si;

    /**
     * DI (destination index)
     *
     * The destination operand of a string instruction always resides in the
     * current extra segment; its offset is take from DI, the destination index
     * register. The string instructions automatically adjust SI and DI as they
     * process the string one byte or word at a time.
     */
    private int              di;

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
    private int              cs;

    /**
     * DS (data segment)
     *
     * The DS register points to the current data segment; it generally
     * contains program variables.
     */
    private int              ds;

    /**
     * SS (stack segment)
     *
     * The SS register points to the current stack segment, stack operations
     * are performed on locations in this segment.
     */
    private int              ss;

    /**
     * ES (extra segment)
     *
     * The ES register points to the current extra segment, which is also
     * typically used for data storage.
     */
    private int              es;

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
    private int              ip;

    /**
     * Flags
     *
     * The 8086 has six 1-bit status flags that the EU posts to reflect certain
     * properties of the result of an arithmetic or logic operation. A group of
     * instructions is available that allow a program to alter its execution
     * depending of the state of these flags, that is, on the result of a prior
     * operation. Different instructions affect the status flags differently.
     */
    private int              flags;

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
    private final int[]      queue  = new int[6];

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
    private final int[]      memory = new int[1048576];

    /*
     * Typical 8086 Machine Instruction Format
     *
     * |     BYTE 1     |     BYTE 2      |     BYTE 3    |     BYTE 4     |  BYTE 5  |  BYTE 6   |
     * | OPCODE | D | W | MOD | REG | R/M | LOW DISP/DATA | HIGH DISP/DATA | LOW DATA | HIGH DATA |
     */
    /** Operation (Instruction) code */
    private int              op;
    /** Direction is to register/Direction is from register */
    private int              d;
    /** Word/Byte operation */
    private int              w;
    /** Register mode/Memory mode with displacement length */
    private int              mod;
    /** Register operand/Extension of opcode */
    private int              reg;
    /** Register operand/Registers to use in EA calculation */
    private int              rm;

    /**
     * Performs addition and sets flags accordingly.
     *
     * @param w
     *            word/byte operation
     * @param dst
     *            the first operand
     * @param src
     *            the second operand
     * @param flags
     *            the flags to set
     * @return the result
     */
    private int add(final int w, final int dst, final int src, final int flags) {
        final int res = dst + src;

        // Carry Flag
        if ((flags & CF) == CF) {
            if (w == 0b0 && res > 0xff || w == 0b1 && res > 0xffff)
                this.flags |= CF;
            else
                this.flags &= ~CF;
        }

        // Zero Flag
        if ((flags & ZF) == ZF) {
            if (res == 0)
                this.flags |= ZF;
            else
                this.flags &= ~ZF;
        }

        // Sign Flag
        if ((flags & SF) == SF) {
            if (w == 0b0 && (res >> 7 & 0b1) == 0b1 || w == 0b1 && (res >> 15 & 0b1) == 0b1)
                this.flags |= SF;
            else
                this.flags &= ~SF;
        }

        return res & (w == 0b0 ? 0xff : 0xffff);
    }

   /**
    * Performs logical AND and sets flags accordingly.
    *
    * @param w
    *            word/byte operation
    * @param dst
    *            the first operand
    * @param src
    *            the second operand
    * @param flags
    *            the flags to set
    * @return the result
    */
   private int and(final int w, final int dst, final int src, final int flags) {
       final int res = dst & src;

       // Carry Flag
       if ((flags & CF) == CF)
           this.flags &= ~CF;

       // Zero Flag
       if ((flags & ZF) == ZF) {
           if (res == 0)
               this.flags |= ZF;
           else
               this.flags &= ~ZF;
       }

       // Sign Flag
       if ((flags & SF) == SF) {
           if (w == 0b0 && (res >> 7 & 0b1) == 0b1 || w == 0b1 && (res >> 15 & 0b1) == 0b1)
               this.flags |= SF;
           else
               this.flags &= ~SF;
       }

       return res & (w == 0b0 ? 0xff : 0xffff);
   }

    /**
     * Executes one cycle: fetch and execute.
     *
     * @return has more instructions?
     */
    private boolean cycle() {
        // Fetch instruction from memory.
        for (int i = 0; i < 6; i++)
            queue[i] = memory[ip + i];

        // Decode first byte.
        op = queue[0] >>> 2 & 0b111111;
        d = queue[0] >>> 1 & 0b1;
        w = queue[0] & 0b1;

        int dst, src, res = 0;
        switch (queue[0]) {
        /*
         * Data Transfer Instructions
         *
         * The 14 data transfer instructions move single bytes and words
         * between memory and registers as well as between registers AL or AX
         * and I/O ports. The stack manipulation instructions are included in
         * this group as are instructions for transferring flags contents and
         * for loading segment registers.
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
            decode();
            src = getReg(w, reg);
            setRM(w, mod, rm, src);
            break;
        case 0x8a: // MOV REG8,REG8/MEM8
        case 0x8b: // MOV REG16,REG16/MEM16
            decode();
            src = getRM(w, mod, rm);
            setReg(w, reg, src);
            break;

        // Immediate to Register/Memory
        case 0xc6: // MOV REG8/MEM8,IMMED8
        case 0xc7: // MOV REG16/MEM16,IMMED16
            decode();
            switch (reg) {
            case 0b000:
                src = memory[++ip];
                if (w == 0b1)
                    src |= memory[++ip] << 8;
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
            w = queue[0] >> 3 & 0b1;
            reg = queue[0] & 0b111;
            src = memory[++ip];
            if (w == 0b1)
                src |= memory[++ip] << 8;
            setReg(w, reg, src);
            break;

        // Memory to Accumulator
        case 0xa0: // MOV AL,MEM8
        case 0xa1: // MOV AX,MEM16
            dst = memory[++ip];
            dst |= memory[++ip] << 8;
            src = memory[dst];
            if (w == 0b1)
                src |= memory[dst + 1] << 8;
            setReg(w, 0b000, src);
            break;

        // Accumulator to Memory
        case 0xa2: // MOV MEM8,AL
        case 0xa3: // MOV MEM16,AX
            dst = memory[++ip];
            dst |= memory[++ip] << 8;
            src = getReg(w, 0b000);
            memory[dst] = src & 0xff;
            if (w == 0b1)
                memory[dst + 1] = src >>> 8 & 0xff;
            break;

        // Register/Memory to Segment Register
        case 0x8e: // MOV SEGREG,REG16/MEM16
            decode();
            src = getRM(0b1, mod, rm);
            setSegReg(reg, src);
            break;

        // Segment Register to Register/Memory
        case 0x8c: // MOV REG16/MEM16,SEGREG
            decode();
            src = getSegReg(reg);
            setRM(0b1, mod, rm, src);
            break;

        /*
         * PUSH source
         *
         * PUSH decrements SP (the stack pointer) by two and then transfers a
         * word from the source operand to the top of the stack now pointer by
         * SP. PUSH often is used to place parameters on the stack before
         * calling a procedure; more generally, it is the basic means of
         * storing temporary data on the stack.
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
            reg = queue[0] & 0b111;
            src = getReg(0b1, reg);
            push(src);
            break;

        // Segment Register
        case 0x06: // PUSH ES
        case 0x0e: // PUSH CS
        case 0x16: // PUSH SS
        case 0x1e: // PUSH DS
            reg = queue[0] >>> 3 & 0b111;
            src = getSegReg(reg);
            push(src);
            break;

        /*
         * POP destination
         *
         * POP transfers the word at the current top of the stack (pointed to
         * by the SP) to the destination operand, and then increments SP by two
         * to point to the new top of the stack. POP can be used to move
         * temporary variables from the stack to registers or memory.
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
            reg = queue[0] & 0b111;
            src = pop();
            setReg(0b1, reg, src);
            break;

        // Segment Register
        case 0x07: // POP ES
        case 0x0f: // POP CS
        case 0x17: // POP SS
        case 0x1f: // POP DS
            reg = queue[0] >>> 3 & 0b111;
            src = pop();
            setSegReg(reg, src);
            break;
 
        /*
         * XCHG destination,source
         *
         * XCHG (exchange) switches the contents of the source and destination
         * (byte or word) operands. When used in conjunction with the LOCK
         * prefix, XCHG can test and set a semaphore that controls access to a
         * resource shared by multiple processors.
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
            reg = queue[0] & 0b111;
            dst = getReg(0b1, 0b000);
            src = getReg(0b1, reg);
            setReg(0b1, 0b000, src);
            setReg(0b1, reg, dst);
            break;

        /*
         * Arithmetic Instructions
         *
         * Arithmetic Data Formats
         *
         * 8086 arithmetic operations may be performed on four types of
         * numbers: unsigned binary, signed binary (integers), unsigned packed
         * decimal and unsigned unpacked decimal. Binary numbers may be 8 or 16
         * bits long. Decimal numbers are stored in bytes, two digits per byte
         * for packed decimals and one digit per byte for unpacked decimal. The
         * processor always assumes that the operands specified in arithmetic
         * instructions contain data that represents valid numbers for the type
         * of instructions being performed. Invalid data may produce
         * unpredictable results.
         *
         * Unsigned binary numbers may be either 8 or 16 bits long; all bits
         * are considered in determining a number's magnitude. The value range
         * of an 8-bit unsigned binary number is 0-255; 16 bits can represent
         * values from 0 through 65,535. Addition, subtraction, multiplication
         * and division operations are available for unsigned binary numbers.
         *
         * Signed binary numbers (integer) may be either 8 or 16 bits long. The
         * high-order (leftmost) bit is interpreted as the number's sign: 0 =
         * positive and 1 = negative. Negative numbers are represented in
         * standard two's complement notation. Since the high-order is used for
         * a sign, the range of an 8-bit integer is -128 through +127; 16-bit
         * integer may range from -32,768 through +32,767. The value zero has a
         * positive sign. Multiplication and division operations are provided
         * for signed binary numbers. Addition and subtraction are performed
         * with the unsigned binary instructions. Conditional jump
         * instructions, as well as an "interrupt on overflow" instruction, can
         * be used following an unsigned operation on an integer to detect
         * overflow into the sign bit.
         *
         * Packed decimal numbers are stored as unsigned byte quantities. The
         * byte is treated as having one decimal digit in each half-byte
         * (nibble); the digit in the high-order half-byte is the most
         * significant. Hexadecimal values 0-9 are valid in each half-byte, and
         * the range of a packed decimal number is 0-99. Addition and
         * subtraction are performed in two steps. First an unsigned binary
         * instruction is used to produce an intermediate result in register
         * AL. The an adjustment operation is performed which changes the
         * intermediate value in AL to a final correct packed decimal result.
         * Multiplication and division adjustments are not available for packed
         * decimal numbers.
         *
         * Unpacked decimal numbers are stored as unsigned byte quantities. The
         * magnitude of the number is determined from the low-order half-byte;
         * hexadecimal values 0-9 are valid and are interpreted as decimal
         * numbers. The high-order half-byte must be zero for multiplication
         * and division; it may contain any value from addition and
         * subtraction. Arithmetic on unpacked decimal numbers is performed in
         * two steps. The unsigned binary addition, subtraction and
         * multiplication operations are used to produce an intermediate result
         * in register AL. An adjustment instruction then changes the value in
         * AL to a final correct unpacked decimal number. Division is performed
         * similarly, except that the adjustment is carried out on the
         * numerator operand in register AL first, then a following unsigned
         * binary division instruction produces a correct result.
         *
         * Unpacked decimal numbers are similar to the ASCII character
         * representations of the digits 0-9. Note, however, that the high-
         * order half-byte of an ASCII numeral is always 3H. Unpacked decimal
         * arithmetic may be performed on ASCII number characters under the
         * following conditions:
         * - the high-order half-byte of an ASCII numeral must be set to 0H
         * prior to multiplication or division.
         * - unpacked decimal arithmetic leaves the high-order half-byte set to
         * 0H; it must be set to 3H to produce a valid ASCII numeral.
         *
         * Arithmetic Instructions and Flags
         *
         * The 8086 arithmetic instructions post certain characteristics of the
         * result of the operation to six flags. Most of these flags can be
         * tested by following the arithmetic instruction with a conditional
         * jump instruction; the INTO (interrupt on overflow) instruction may
         * also be used. The various instructions affect the flags differently,
         * as explained in the instruction descriptions.
         */
        /*
         * Addition
         */
        /*
         * ADD destination,source
         *
         * The sum of the two operands, which may be bytes or words, replaces
         * the destination operand. Both operands may be signed or unsigned
         * binary numbers. ADD updates AF, CF, OF, PF, SF and ZF.
         */
        // Reg./Memory and Register to Either
        case 0x00: // ADD REG8/MEM8,REG8
        case 0x01: // ADD REG16/MEM16,REG16
            decode();
            dst = getRM(w, mod, rm);
            src = getReg(w, reg);
            res = add(w, dst, src, CF | SF | ZF);
            setRM(w, mod, rm, res);
            break;
        case 0x02: // ADD REG8,REG8/MEM8
        case 0x03: // ADD REG16,REG16/MEM16
            decode();
            dst = getReg(w, reg);
            src = getRM(w, mod, rm);
            res = add(w, dst, src, CF | SF | ZF);
            setReg(w, reg, res);
            break;

        // Immediate to Accumulator
        case 0x04: // ADD AL,IMMED8
        case 0x05: // ADD AX,IMMED16
            dst = getReg(w, 0);
            src = memory[++ip];
            if (w == 0b1)
                src |= memory[++ip] << 8;
            res = add(w, dst, src, CF | SF | ZF);
            setReg(w, 0b000, res);
            break;

        /*
         * ADC destination,source
         *
         * ADC (Add with Carry) sums the operands, which may be bytes or words,
         * adds one if CF is set and replaces the destination operand with the
         * result. Both operands may be signed or unsigned binary numbers. ADC
         * updates AF, CF, OF, PF, SF and ZF. Since ADC incorporates a carry
         * from a previous operation, it can be used to write routines to add
         * numbers longer than 16 bits.
         */
        // Reg./Memory with Register to Either
        case 0x10: // ADC REG8/MEM8,REG8
        case 0x11: // ADC REG16/MEM16,REG16
            decode();
            dst = getRM(w, mod, rm);
            src = getReg(w, reg);
            if ((flags & CF) == CF)
                ++dst;
            res = add(w, dst, src, CF | SF | ZF);
            setRM(w, mod, rm, res);
            break;
        case 0x12: // ADC REG8,REG8/MEM8
        case 0x13: // ADC REG16,REG16/MEM16
            decode();
            dst = getReg(w, reg);
            src = getRM(w, mod, rm);
            if ((flags & CF) == CF)
                ++dst;
            res = add(w, dst, src, CF | SF | ZF);
            setReg(w, reg, res);
            break;

        // Immediate to Accumulator
        case 0x14: // ADC AL,IMMED8
        case 0X15: // ADC AX,IMMED16
            dst = getReg(w, 0b000);
            src = memory[++ip];
            if (w == 0b1)
                src |= memory[++ip] << 8;
            if ((flags & CF) == CF)
                ++dst;
            res = add(w, dst, src, CF | SF | ZF);
            setReg(w, 0b000, res);
            break;

        /*
         * INC destination
         *
         * INC (Increment) adds one to the destination operand. The operand may
         * be a byte or a word and is treated as an unsigned binary number. INC
         * updates AF, OF, PF, SF and ZF; it does not affect CF.
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
            reg = queue[0] & 0b111;
            src = getReg(0b1, reg);
            res = add(0b1, src, 1, SF | ZF);
            setReg(0b1, reg, res);
            break;

        /*
         * Subtraction
         */
        /*
         * SUB destination,source
         *
         * The source operand is subtracted from the destination operand, and
         * the result replaces the destination operand. The operands may be
         * bytes or words. Both operands may be signed or unsigned binary
         * numbers. SUB updates AF, CF, OF, PF, SF and ZF.
         */
        // Reg./Memory and Register to Either
        case 0x28: // SUB REG8/MEM8,REG8
        case 0x29: // SUB REG16/MEM16,REG16
            decode();
            dst = getRM(w, mod, rm);
            src = getReg(w, reg);
            res = sub(w, dst, src, CF | SF | ZF);
            setRM(w, mod, rm, res);
            break;
        case 0x2a: // SUB REG8,REG8/MEM8
        case 0x2b: // SUB REG16,REG16/MEM16
            decode();
            dst = getReg(w, reg);
            src = getRM(w, mod, rm);
            res = sub(w, dst, src, CF | SF | ZF);
            setReg(w, reg, res);
            break;

        // Immediate from Accumulator
        case 0x2c: // SUB AL,IMMED8
        case 0x2d: // SUB AX,IMMED16
            dst = getReg(w, 0b000);
            src = memory[++ip];
            if (w == 0b1)
                src |= memory[++ip] << 8;
            res = sub(w, dst, src, CF | SF | ZF);
            setReg(w, 0b000, res);
            break;

        /*
         * SBB destination,source
         *
         * SBB (Subtract with Borrow) subtracts the source from the
         * destination, subtracts one if CF is set, and returns the result to
         * the destination operand. Both operands may be bytes or words. Both
         * operands may be signed or unsigned binary numbers. SBB updates AF,
         * CF, OF, PF, SF and ZF. Since it incorporates a borrow from a
         * previous operation, SBB may be used to write routines that subtract
         * numbers longer than 16 bits.
         */
        // Reg./Memory with Register to Either
        case 0x18: // SBB REG8/MEM8,REG8
        case 0x19: // SBB REG16/MEM16,REG16
            decode();
            dst = getRM(w, mod, rm);
            src = getReg(w, reg);
            if ((flags & CF) == CF)
                --dst;
            res = sub(w, dst, src, CF | SF | ZF);
            setRM(w, mod, rm, res);
            break;
        case 0x1a: // SBB REG8,REG8/MEM8
        case 0x1b: // SBB REG16,REG16/MEM16
            decode();
            dst = getReg(w, reg);
            src = getRM(w, mod, rm);
            if ((flags & CF) == CF)
                --dst;
            res = sub(w, dst, src, CF | SF | ZF);
            setReg(w, reg, res);
            break;

        // Immediate to Accumulator
        case 0x1c: // SBB AL,IMMED8
        case 0X1d: // SBB AX,IMMED16
            dst = getReg(w, 0b000);
            src = memory[++ip];
            if (w == 0b1)
                src |= memory[++ip] << 8;
            if ((flags & CF) == CF)
                --dst;
            res = sub(w, dst, src, CF | SF | ZF);
            setReg(w, 0b000, res);
            break;

        /*
         * DEC destination
         *
         * DEC (Decrement) subtracts one from the destination, which may be a
         * byte or a word. DEC updates AF, OF, PF, SF, and ZF; it does not
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
            reg = queue[0] & 0b111;
            dst = getReg(0b1, reg);
            res = sub(0b1, dst, 1, SF | ZF);
            setReg(0b1, reg, res);
            break;

        /*
         * CMP destination,source
         *
         * CMP (Compare) subtracts the source from the destination, which may
         * be bytes or words, but does not return the result. The operands are
         * unchanged, but the flags are updated and can be tested by the
         * subsequent conditional jump instructions. CMP updates AF, CF, OF,
         * PF, SF and ZF. The comparison reflected in the flags is that of the
         * destination to the source. If a CMP instruction is followed by a JG
         * (jump if greater) instruction, for example, the jump is taken if the
         * destination operand is greater than the source operand.
         */
        // Register/Memory and Register
        case 0x38: // CMP REG8/MEM8,REG8
        case 0x39: // CMP REG16/MEM16,REG16
            decode();
            dst = getRM(w, mod, rm);
            src = getReg(w, reg);
            sub(w, dst, src, CF | SF | ZF);
            break;
        case 0x3a: // CMP REG8,REG8/MEM8
        case 0x3b: // CMP REG16,REG16/MEM16
            decode();
            dst = getReg(w, reg);
            src = getRM(w, mod, rm);
            sub(w, dst, src, CF | SF | ZF);
            break;

        // Immediate with Accumulator
        case 0x3c: // CMP AL,IMMED8
        case 0x3d: // CMP AX,IMMED16
            dst = getReg(w, 0b000);
            src = memory[++ip];
            if (w == 0b1)
                src |= memory[++ip] << 8;
            sub(w, dst, src, CF | SF | ZF);
            break;

        /*
         * Bit Manipulation Instructions
         *
         * The 8086 provides three groups of instructions for manipulating bits
         * within both bytes and words: logical, shifts and rotates.
         */
        /*
         * Logical
         *
         * The logical instructions include the boolean operators "not," "and,"
         * "inclusive or," and "exclusive or," plus a TEST instruction that
         * sets the flags, but does not alter either of its operands.
         *
         * AND, OR, XOR and TEST affect the flags as follows: the overflow (OF)
         * and carry (CF) flags are always cleared by logical instructions, and
         * the content of the auxiliary carry (AF) flag is always undefined
         * following execution of a logical instruction. The sign (SF), zero
         * (ZF) and parity (PF) flags are always posted to reflect the result
         * of the operation and can be tested by conditional jump instructions.
         * The interpretation of these flags is the same as for arithmetic
         * instructions. SF is set if the result is negative (high-order bit is
         * 1), and is cleared if the result is positive (high-order bit is 0).
         * ZF is set if the result is zero, cleared otherwise. PF is set if the
         * result contains an even number of 1-bits (has even parity) and is
         * cleared if the number of 1-bits is odd (the result has odd parity).
         * Note that NOT has no effect on the flags.
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
            decode();
            dst = getRM(w, mod, rm);
            src = getReg(w, reg);
            res = and(w, dst, src, CF | SF | ZF);
            setRM(w, mod, rm, res);
            break;
        case 0x22: // AND REG8,REG8/MEM8
        case 0x23: // AND REG16,REG16/MEM16
            decode();
            dst = getReg(w, reg);
            src = getRM(w, mod, rm);
            res = and(w, dst, src, CF | SF | ZF);
            setReg(w, reg, res);
            break;

        // Immediate to Accumulator
        case 0x24: // AND AL,IMMED8
        case 0x25: // AND AX,IMMED16
            dst = getReg(w, 0b000);
            src = memory[++ip];
            if (w == 0b1)
                src |= memory[++ip] << 8;
            res = and(w, dst, src, CF | SF | ZF);
            setReg(w, 0b000, res);
            break;

        /*
         * OR destination,source
         *
         * OR performs the logical "inclusive or" of the two operands (byte or
         * word) and returns the result to the destination operand. A bit in
         * the result is set if either or both corresponding bits of the
         * original operands are set; otherwise the result bit is cleared.
         */
        // Register/Memory and Register
        case 0x08: // OR REG8/MEM8,REG8
        case 0x09: // OR REG16/MEM16,REG16
            decode();
            dst = getRM(w, mod, rm);
            src = getReg(w, reg);
            res = or(w, dst, src, CF | SF | ZF);
            setRM(w, mod, rm, res);
            break;
        case 0x0a: // OR REG8,REG8/MEM8
        case 0x0b: // OR REG16,REG16/MEM16
            decode();
            dst = getReg(w, reg);
            src = getRM(w, mod, rm);
            res = or(w, dst, src, CF | SF | ZF);
            setReg(w, reg, res);
            break;

        // Immediate to Accumulator
        case 0x0c: // OR AL,IMMED8
        case 0x0d: // OR AX,IMMED16
            dst = getReg(w, 0b000);
            src = memory[++ip];
            if (w == 0b1)
                src |= memory[++ip] << 8;
            res = or(w, dst, src, CF | SF | ZF);
            setReg(w, 0b000, res);
            break;

        /*
         * XOR destination,source
         *
         * XOR (Exclusive Or) performs the logical "exclusive or" of the two
         * operands and returns the result to the destination operand. A bit in
         * the result if set if the corresponding bits of the original operands
         * contain opposite values (one is set, the other is cleared);
         * otherwise the result bit is cleared.
         */
        // Register/Memory and Register
        case 0x30: // XOR REG8/MEM8,REG8
        case 0x31: // XOR REG16/MEM16,REG16
            decode();
            dst = getRM(w, mod, rm);
            src = getReg(w, reg);
            res = xor(w, dst, src, CF | SF | ZF);
            setRM(w, mod, rm, res);
            break;
        case 0x32: // XOR REG8,REG8/MEM8
        case 0x33: // XOR REG16,REG16/MEM16
            decode();
            dst = getReg(w, reg);
            src = getRM(w, mod, rm);
            res = xor(w, dst, src, CF | SF | ZF);
            setReg(w, reg, res);
            break;

        // Immediate to Accumulator
        case 0x34: // XOR AL,IMMED8
        case 0x35: // XOR AX,IMMED16
            dst = getReg(w, 0b000);
            src = memory[++ip];
            if (w == 0b1)
                src |= memory[++ip] << 8;
            res = xor(w, dst, src, CF | SF | ZF);
            setReg(w, 0b000, res);
            break;

        /*
         * Program Transfer Instructions
         *
         * The sequence of execution of instructions in an 8086 program is
         * determined by the content of the code segment register (CS) and the
         * instruction pointer (IP). The CS register contains the base address
         * of the current code segment, the 64k portion of memory from which
         * instructions are presently being fetched. The IP is used as an
         * offset from the beginning of the code segment; the combination of CS
         * and IP points to the memory location from which the next instruction
         * is to be fetched. (Recall that under most operating conditions, the
         * next instruction to be executed has already been fetched from memory
         * and is waiting in the CPU instruction queue.) The program transfer
         * instructions operate on the instruction pointer and on the CS
         * register; changing the content of these causes normal sequential
         * execution to be altered. When a program transfer occurs, the queue
         * no longer contains the correct instruction, and the BIU obtains the
         * next instruction from memory using the new IP and CS values, passes
         * the instruction directly to the EU, and then begins refilling the
         * queue from the new location.
         *
         * Four groups of program transfers are available in the 8086:
         * unconditional transfers, conditional transfers, iteration control
         * instructions and interrupt-related instructions. Only the interrupt-
         * related instruction affect any CPU flags. As will be seen, however,
         * the execution of many of the program transfer instructions is
         * affected by the states of the flags.
         */
        /*
         * Unconditional Transfers
         *
         * The unconditional transfers instructions may transfer control to a
         * target instruction within the current code segment (intrasegment
         * transfer) or to a different code segment (intersegment transfer).
         * (The ASM-86 assembler terms an intrasegment target NEAR and an
         * intersegment target FAR.) The transfer is made unconditionally any
         * time the instruction is executed.
         */
        /*
         * CALL procedure-name
         *
         * CALL activated an out-of-line procedure, saving information on the
         * stack to permit a RET (return) instruction in the procedure to
         * transfer control back to the instruction following the CALL. The
         * assembler generates a different type of CALL instruction depending
         * on whether the programmer has defined the procedure name as NEAR or
         * FAR. For control to return properly, the type of CALL instruction
         * must match the type of RET instruction that exists from the
         * procedure. (The potential for a mismatch exists if the procedure and
         * the CALL are contained in separately assembled programs.) Different
         * forms of the CALL instruction allow the address of the target
         * procedure to be obtained from the instruction itself (direct CALL)
         * or from a memory location or register referenced by the instruction
         * (indirect CALL). In the following descriptions, bear in mind that
         * the processor automatically adjusts IP to point to the next
         * instruction to be executed before saving it on the stack.
         *
         * For an intrasegment direct CALL, SP (the stack pointer) is
         * decremented by two and IP is pushed onto the stack. The relative
         * displacement (up to ±32k) of the target procedure from the CALL
         * instruction is then added to the instruction pointer. This form of
         * the CALL instruction is "self-relative" and is appropriate for
         * position independent (dynamically relocatable) routines in which the
         * CALL and its target are in the same segment and are moved together.
         *
         * An intrasegment indirect CALL may be made through memory or through
         * a register. SP is decremented by two and IP is pushed onto the
         * stack. The offset of the target procedure is obtained from the
         * memory word or 16-bit general register referenced in the instruction
         * and replaces IP.
         *
         * For an intersegment direct CALL, SP is decremented by two, and CS is
         * pushed onto the stack. CS is replaced by the segment word contained
         * in the instruction. SP again is decremented by two. IP is pushed
         * onto the stack and is replaced by the offset word contained in the
         * instruction.
         *
         * For an intersegment indirect CALL (which only may be made through
         * memory), SP is decremented by two, and CS is pushed onto the stack.
         * CS is then replaced by the content of the second word of the
         * doubleword memory pointer referenced by the instruction. SP again is
         * decremented by two, and IP is pushed onto the stack and is replaced
         * by the content of the first word of the doubleword pointer
         * referenced by the instruction.
         */
        // Direct with Segment
        case 0xe8: // CALL NEAR-PROC
            dst = memory[++ip];
            dst |= memory[++ip] << 8;
            // Unsigned to signed.
            dst = dst << 16 >> 16;
            push(ip);
            ip += dst;
            break;

        // Direct Intersegment
        case 0x9a: // CALL FAR-PROC
            dst = memory[++ip];
            dst |= memory[++ip] << 8;
            src = memory[++ip];
            src |= memory[++ip] << 8;
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
         * defined the procedure NEAR, or an intersegment RET if the procedure
         * has been defined as FAR. RET pops the word at the top of the stack
         * (pointed to by the register SP) into the instruction pointer and
         * increments SP by two. If RET is intersegment, the word at the top of
         * the stack is popped into the CS register, and SP is again
         * incremented by two. If an optional pop value has been specified, RET
         * adds that value to SP. This feature may be used to discard
         * parameters pushed onto the stack before the execution of the CALL
         * instruction.
         */
        // Within Segment
        case 0xc3: // RET (intrasegment)
            ip = pop();
            break;

        // Within Seg Adding Immed to SP
        case 0xc2: // RET IMMED16 (intraseg)
            src = memory[++ip];
            src |= memory[++ip] << 8;
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
            src = memory[++ip];
            src |= memory[++ip] << 8;
            ip = pop();
            cs = pop();
            sp += src;
            break;

        /*
         * JMP target
         *
         * JMP unconditionally transfers control to the target location. Unlike
         * a CALL instruction, JMP does not save any information on the stack,
         * and no return to the instruction following the JMP is expected. Like
         * CALL, the address of the target operand may be obtained from the
         * instruction itself (direct JMP) or from memory or a register
         * referenced by the instruction (indirect JMP).
         *
         * An intrasegment direct JMP changes the instruction pointer by adding
         * the relative displacement of the target from the JMP instruction. If
         * the assembler can determine that the target is within 127 bytes of
         * the JMP, it automatically generates a two-byte form of this
         * instruction called a SHORT JMP; otherwise, it generates a NEAR JMP
         * that can address a target within ±32k. Intrasegment direct JMPS are
         * self-relative and are appropriate in position-independent
         * (dynamically relocatable) routines in which the JMP and its target
         * are in the same segment and are moved together.
         *
         * An intrasegment indirect JMP may be made either through memory or
         * through a 16-bit general register. In the first case, the content of
         * the word referenced by the instruction replaces the instruction
         * pointer. In the second case, the new IP value is taken from the
         * register named in the instruction.
         *
         * An intersegment direct JMP replaces IP and CS with values contained
         * in the instruction.
         *
         * An intersegment indirect JMP may be made only through memory. The
         * first word of the doubleword pointer referenced by the instruction
         * replaces IP, and the second word replaces CS.
         */
        // Direct within Segment
        case 0xe9: // JMP NEAR-LABEL
            dst = memory[++ip];
            dst |= memory[++ip] << 8;
            // Unsigned to signed.
            dst = dst << 16 >> 16;
            ip += dst;
            break;

        // Direct within Segment-Short
        case 0xeb: // JMP SHORT-LABEL
            dst = memory[++ip];
            // Unsigned to signed.
            dst = dst << 24 >> 24;
            ip += dst;
            break;

        // Direct Intersegment
        case 0xea: // JMP FAR-LABEL
            dst = memory[++ip];
            dst |= memory[++ip] << 8;
            src = memory[++ip];
            src |= memory[++ip] << 8;
            ip = dst;
            cs = src;
            break;

        /*
         * Conditional Transfers
         *
         * The conditional transfer instructions are jumps that may or may not
         * transfer control depending on the state of the CPU flags at the time
         * the instruction is executed. The 18 instructions each test a
         * different combination of flags for a conditional. If the condition
         * is "true," then control is transferred to the target specified in
         * the instruction. If the condition is "false," then control passes to
         * the instruction that follows the conditional jump. All conditional
         * jumps are SHORT, that is, the target must be in the current code
         * segment and within -128 to +127 bytes of the first byte of the next
         * instruction (JMP 00H jumps to the first byte of the next
         * instruction). Since the jump is made by adding the relative
         * displacement of the target to the instruction pointer, all
         * conditional jumps are self-relative and are appropriate for
         * position-independent routines.
         */
        /*
         * JE/JZ
         *
         * Jump if equal/zero - ZF=1.
         */
        case 0x74: // JE/JZ SHORT-LABEL
            dst = memory[++ip];
            // Unsigned to signed.
            dst = dst << 24 >> 24;
            if ((flags & ZF) == ZF)
                ip += dst;
            break;

        /*
         * JB/JNAE/JC
         *
         * Jump if below/not above or equal/carry - CF=1.
         */
        case 0x72: // JB/JNAE/JC SHORT-LABEL
            dst = memory[++ip];
            // Unsigned to signed.
            dst = dst << 24 >> 24;
            if ((flags & CF) == CF)
                ip += dst;
            break;

        /*
         * JBE/JNA
         *
         * Jump if below or equal/not above - (CF or ZF)=1.
         */
        case 0x76: // JBE/JNA SHORT-LABEL
            dst = memory[++ip];
            // Unsigned to signed.
            dst = dst << 24 >> 24;
            if ((flags & CF) == CF || (flags & ZF) == ZF)
                ip += dst;
            break;

        /*
         * JS
         *
         * Jump if sign - SF=1.
         */
        case 0x78: // JS SHORT-LABEL
            dst = memory[++ip];
            // Unsigned to signed.
            dst = dst << 24 >> 24;
            if ((flags & SF) == SF)
                ip += dst;
            break;

        /*
         * JNE/JNZ
         *
         * Jump if not equal/not zero - ZF=0.
         */
        case 0x75: // JNE/JNZ SHORT-LABEL
            dst = memory[++ip];
            // Unsigned to signed.
            dst = dst << 24 >> 24;
            if ((flags & ZF) != ZF)
                ip += dst;
            break;

        /*
         * JNB/JAE
         *
         * Jump if not below/above or equal - CF=0.
         */
        case 0x73: // JNE/JNZ SHORT-LABEL
            dst = memory[++ip];
            // Unsigned to signed.
            dst = dst << 24 >> 24;
            if ((flags & CF) != CF)
                ip += dst;
            break;

        /*
         * JNBE/JA
         *
         * Jump if not below nor equal/above - (CF or ZF)=0.
         */
        case 0x77: // JNBE/JA SHORT-LABEL
            dst = memory[++ip];
            // Unsigned to signed.
            dst = dst << 24 >> 24;
            if ((flags & CF) != CF || (flags & ZF) != ZF)
                ip += dst;
            break;

        /*
         * JNS
         *
         * Jump if not sign - SF=0.
         */
        case 0x79: // JNS SHORT-LABEL
            dst = memory[++ip];
            // Unsigned to signed.
            dst = dst << 24 >> 24;
            if ((flags & SF) != SF)
                ip += dst;
            break;

        /*
         * Processor Control Instructions
         *
         * These instructions allow programs to control various CPU functions.
         * One group of instructions updates flags, and another group is used
         * primarily for synchronizing the 8086 with external events. A final
         * instruction causes the CPU to do nothing. Except for the flag
         * operations, none of the processor control instructions affect the
         * flags.
         */
        /*
         * Flag Operations
         */
        /*
         * CLC
         *
         * CLC (Clear Carry flag) zeroes the carry flag (CF) and affects no
         * other flags. It (and CMC and STC) is useful in conjunction with the
         * RCL and RCR instructions.
         */
        case 0xf8:
            flags &= ~CF;
            break;

        /*
         * STC
         *
         * STC (Set Carry flag) sets CF to 1 and afffects no other flags.
         */
        case 0xf9:
            flags |= CF;
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
         * interrupts are enabled, upon receipt of a maskable interrupt request
         * on INTR. HLT does not affect any flags. It may be used as an
         * alternative to an endless software loop in situations where a
         * program must wait for an interrupt.
         */
        case 0xf4: // HLT
            return false;

        /*
         * No Operation
         */
        /*
         * NOP
         *
         * NOP (No Operation) causes the CPU to do nothing. NOP does not affect
         * any flags.
         */
        case 0x90: // NOP
            break; 

        /*
         * Extensions
         */
        // GROUP 1
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
            src = memory[++ip];
            if (queue[0] == 0x81)
                src |= memory[++ip] << 8;
            // Perform sign extension if needed.
            else if (queue[0] == 0x83 && (src & 0x80) == 0x80)
                src |= 0xff00;
            switch (reg) {
            case 0b000: // ADD
                res = add(w, dst, src, CF | SF | ZF);
                setRM(w, mod, rm, res);
                break;
            case 0b001: // OR
                if (queue[0] == 0x80 || queue[0] == 0x81) {
                    res = or(w, dst, src, CF | SF | ZF);
                    setRM(w, mod, rm, res);
                    break;
                }
                break;
            case 0b010: // ADC
                if ((flags & CF) == CF)
                    ++dst;
                res = add(w, dst, src, CF | SF | ZF);
                setRM(w, mod, rm, res);
                break;
            case 0b011: // SBB
                if ((flags & CF) == CF)
                    --dst;
                res = sub(w, dst, src, CF | SF | ZF);
                setRM(w, mod, rm, res);
                break;
            case 0b100: // AND
                if (queue[0] == 0x80 || queue[0] == 0x81) {
                    res = and(w, dst, src, CF | SF | ZF);
                    setRM(w, mod, rm, res);
                }
                break;
            case 0b101: // SUB
                res = sub(w, dst, src, CF | SF | ZF);
                setRM(w, mod, rm, res);
                break;
            case 0b110: // XOR
                if (queue[0] == 0x80 || queue[0] == 0x81) {
                    res = xor(w, dst, src, CF | SF | ZF);
                    setRM(w, mod, rm, res);
                }
                break;
            case 0b111: // CMP
                sub(w, dst, src, CF | SF | ZF);
                break;
            }
            break;

        // GROUP 1A
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

        // GROUP 4
        case 0xfe:
            // INC REG8/MEM8
            // DEC REG8/MEM8
            decode();
            src = getRM(w, mod, rm);
            switch (reg) {
            case 0b000: // INC
                res = add(w, src, 1, SF | ZF);
                setRM(w, mod, rm, res);
                break;
            case 0b001: // DEC
                res = sub(w, src, 1, SF | ZF);
                setRM(w, mod, rm, res);
                break;
            }
            break;

        // GROUP 5
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
                res = add(w, src, 1, SF | ZF);
                setRM(w, mod, rm, res);
                break;
            case 0b001: // DEC
                res = sub(w, src, 1, SF | ZF);
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
                ip = memory[dst + 1] << 8 | memory[dst];
                cs = memory[dst + 3] << 8 | memory[dst + 2];
                break;
            case 0b100: // JMP
                ip = src;
                break;
            case 0b101: // JMP
                dst = getEA(mod, rm);
                ip = memory[dst + 1] << 8 | memory[dst];
                cs = memory[dst + 3] << 8 | memory[dst + 2];
                break;
            case 0b110: // PUSH
                push(src);
                break;
            }
            break;
        }
        return true;
    }

    /**
     * Decodes the second byte of the instruction and increments IP accordingly.
     */
    private void decode() {
        mod = queue[1] >>> 6 & 0b11;
        reg = queue[1] >>> 3 & 0b111;
        rm = queue[1] & 0b111;

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
     * Execute all instructions.
     */
    public void execute() {
        while (cycle())
            ++ip; // Next instruction
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

        switch (rm) {
        case 0b000: // EA = (BX) + (SI) + DISP
            return bh << 8 | bl + si + disp;
        case 0b001: // EA = (BX) + (DI) + DISP
            return bh << 8 | bl + di + disp;
        case 0b010: // EA = (BP) + (SI) + DISP
            return bp + si + disp;
        case 0b011: // EA = (BP) + (DI) + DISP
            return bp + di + disp;
        case 0b100: // EA = (SI) + DISP
            return si + disp;
        case 0b101: // EA = (DI) + DISP
            return di + disp;
        case 0b110:
            if (mod == 0b00) {
                // Direct address
                return queue[3] << 8 | queue[2];
            } else
                // EA = (BP) + DISP
                return bp + disp;
        case 0b111: // EA = (BX) + DISP
            return bh << 8 | bl + disp;
        }
        return 0;
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
        if (w == 0b0)
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
        else {
            // Memory mode
            final int ea = getEA(mod, rm);
            if (w == 0b0)
                // Byte data
                return memory[ea];
            else
                // Word data
                return memory[ea + 1] << 8 | memory[ea];
        }
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
        case 0b00:
            return es;
        case 0b01:
            return cs;
        case 0b10:
            return ss;
        case 0b11:
            return ds;
        }
        return 0;
    }

    /**
     * Loads a binary file into memory at the specified address.
     *
     * @param addr
     *            the address
     * @param bin
     *            the binary file
     */
    public void load(final int addr, final int[] bin) {
        for (int i = 0; i < bin.length; i++)
            memory[addr + i] = bin[i] & 0xff;
    }

    /**
     * Performs logical OR and sets flags accordingly.
     *
     * @param w
     *            word/byte operation
     * @param dst
     *            the first operand
     * @param src
     *            the second operand
     * @param flags
     *            the flags to set
     * @return the result
     */
    private int or(final int w, final int dst, final int src, final int flags) {
        final int res = dst | src;

        // Carry Flag
        if ((flags & CF) == CF)
            this.flags &= ~CF;

        // Zero Flag
        if ((flags & ZF) == ZF) {
            if (res == 0)
                this.flags |= ZF;
            else
                this.flags &= ~ZF;
        }

        // Sign Flag
        if ((flags & SF) == SF) {
            if (w == 0b0 && (res >> 7 & 0b1) == 0b1 || w == 0b1 && (res >> 15 & 0b1) == 0b1)
                this.flags |= SF;
            else
                this.flags &= ~SF;
        }

        return res & (w == 0b0 ? 0xff : 0xffff);
    }

    /**
     * Pops a value at the top of the stack.
     *
     * @return the value
     */
    private int pop() {
        final int val = memory[sp + 1] << 8 | memory[sp];
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
        memory[sp] = val & 0xff;
        memory[sp + 1] = val >>> 8 & 0xff;
    }

    /**
     * Resets the CPU to its default state.
     */
    public void reset() {
        flags = 0;
        ip = 0x0000;
        sp = 0x0100;
        cs = 0xffff;
        ds = 0x0000;
        ss = 0x0000;
        es = 0x0000;
        for (int i = 0; i < 6; i++)
            queue[i] = 0;
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
        if (w == 0b0)
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
            case 0b000: { // AX
                al = val & 0xff;
                ah = val >>> 8 & 0xff;
                break;
            }
            case 0b001: { // CX
                cl = val & 0xff;
                ch = val >>> 8 & 0xff;
                break;
            }
            case 0b010: { // DX
                dl = val & 0xff;
                dh = val >>> 8 & 0xff;
                break;
            }
            case 0b011: { // BX
                bl = val & 0xff;
                bh = val >>> 8 & 0xff;
                break;
            }
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
        else {
            // Memory mode
            final int ea = getEA(mod, rm);
            if (w == 0b0)
                // Byte data
                memory[ea] = val & 0xff;
            else {
                // Word data
                memory[ea] = val & 0xff;
                memory[ea + 1] = val >>> 8 & 0xff;
            }
        }
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
        case 0b00:
            es = val & 0xffff;
            break;
        case 0b01:
            cs = val & 0xffff;
            break;
        case 0b10:
            ss = val & 0xffff;
            break;
        case 0b11:
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
     * @param flags
     *            the flags to set
     * @return the result
     */
    private int sub(final int w, final int dst, final int src, final int flags) {
        int res = dst - src;

        // Handle overflow.
        if (w == 0b0 && res < 0)
            res += 0x100;
        else if (w == 0b1 && res < 0)
            res += 0x10000;

        // Carry Flag
        if ((flags & CF) == CF) {
            if (dst < src)
                this.flags |= CF;
            else
                this.flags &= ~CF;
        }

        // Zero Flag
        if ((flags & ZF) == ZF) {
            if (res == 0)
                this.flags |= ZF;
            else
                this.flags &= ~ZF;
        }

        // Sign Flag
        if ((flags & SF) == SF) {
            if (w == 0b0 && (res >> 7 & 0b1) == 0b1 || w == 0b1 && (res >> 15 & 0b1) == 0b1)
                this.flags |= SF;
            else
                this.flags &= ~SF;
        }

        return res & (w == 0b0 ? 0xff : 0xffff);
    }

    /**
     * Performs logical XOR and sets flags accordingly.
     *
     * @param w
     *            word/byte operation
     * @param dst
     *            the first operand
     * @param src
     *            the second operand
     * @param flags
     *            the flags to set
     * @return the result
     */
    private int xor(final int w, final int dst, final int src, final int flags) {
        final int res = dst ^ src;

        // Carry Flag
        if ((flags & CF) == CF)
            this.flags &= ~CF;

        // Zero Flag
        if ((flags & ZF) == ZF) {
            if (res == 0)
                this.flags |= ZF;
            else
                this.flags &= ~ZF;
        }

        // Sign Flag
        if ((flags & SF) == SF) {
            if (w == 0b0 && (res >> 7 & 0b1) == 0b1 || w == 0b1 && (res >> 15 & 0b1) == 0b1)
                this.flags |= SF;
            else
                this.flags &= ~SF;
        }

        return res & (w == 0b0 ? 0xff : 0xffff);
    }
}
