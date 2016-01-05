package objenome.op.compute;

import java.util.Arrays;

/**
 * Created by me on 7/11/15.
 */
public class BrainfuckMachine {
	// The indexable memory available to programs.
	protected final byte[] memory;
	// Pointer to current memory address.
	protected int pointer;

	public BrainfuckMachine() {
		this(64);
	}

	public BrainfuckMachine(int memorySize) {
		memory = new byte[memorySize];
		pointer = 0;
	}

	/*
	 * Resets the memory array to be filled with 0 bytes, and the pointer to
	 * address element 0.
	 */
	public void reset() {
		Arrays.fill(memory, (byte) 0);
		pointer = 0;
	}

	public void move(int p) {
		if (p < 0) {
			p = memory.length - 1;
		}
		pointer = p;
	}

	/*
	 * Parses and executes the given source string as a Brainfuck program.
	 */
	public void execute(String source) {
		if (source == null) {
			return;
		}

		for (int i = 0; i < source.length(); i++) {
			char c = source(source, i);

			switch (c) {
				case '>' :
					move(1 + pointer % memory.length);
					break;
				case '<' :
					move(pointer - 1);
					break;
				case '+' :
					add(pointer, 1); // memory[pointer]++;
					break;
				case '-' :
					add(pointer, -1); // memory[pointer]--;
					break;
				case ',' :
					// Not supported.
					break;
				case '.' :
					// Not supported.
					get(pointer); // System.out...

					break;
				case '[' :
					int bracketIndex = closingBracket(source.substring(i + 1))
							+ (i + 1);
					String loopSource = source.substring((i + 1), bracketIndex);
					while (isEmpty(pointer)) {
						execute(loopSource);
					}
					i = bracketIndex;
					break;
				case ']' :
					// Implemented as part of '['.
					break;
				default :
					// Ignore all other characters.
					break;
			}

			getMemory(); // state of each memory per cycle
		}
	}

	public char source(String source, int i) {
		return source.charAt(i);
	}

	public boolean isEmpty(int pointer) {
		return get(pointer) != 0;
	}

	public int get(int pointer) {
		return memory[pointer];
	}
	public int set(int pointer, int newValue) {
		return memory[pointer] = (byte) newValue;
	}
	public int add(int pointer, int delta) {
		return memory[pointer] += delta;
	}

	/*
	 * Locate the matching bracket in the given source.
	 */
	public int closingBracket(String source) {
		int open = 1;
		for (int i = 0; i < source.length(); i++) {
			char c = source(source, i);

			if (c == '[') {
				open++;
			} else if (c == ']') {
				open--;
				if (open == 0) {
					return i;
				}
			}
		}
		// There is no closing bracket.
		return -1;
	}

	/**
	 * Returns the byte array which is providing indexed memory for the
	 * programs. The array will be cleared for each execution.
	 * 
	 * @return the program's indexed memory.
	 */
	public byte[] getMemory() {
		return memory;
	}
}
