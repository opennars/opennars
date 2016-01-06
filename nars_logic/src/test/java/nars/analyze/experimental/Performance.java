/*
 * Copyright (C) 2014 me
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nars.analyze.experimental;

import java.text.DecimalFormat;

public abstract class Performance {
	public final int repeats;
	final String name;
	private long totalTime;
	private long totalMemory;
	protected final DecimalFormat df = new DecimalFormat("#.###");

	public Performance(String name, int repeats, int warmups) {
		this(name, repeats, warmups, true);
	}

	public Performance(String name, int repeats, int warmups, boolean gc) {
		this.repeats = repeats;
		this.name = name;

		init();

		totalTime = 0;
		totalMemory = 0;

		int total = repeats + warmups;
		for (int r = 0; r < total; r++) {

			if (gc) {
				System.gc();
			}

			long usedMemStart = (Runtime.getRuntime().totalMemory() - Runtime
					.getRuntime().freeMemory());

			long start = System.nanoTime();

			run(warmups != 0);

			if (warmups == 0) {
				totalTime += System.nanoTime() - start;
				totalMemory += (Runtime.getRuntime().totalMemory() - Runtime
						.getRuntime().freeMemory()) - usedMemStart;
			} else
				warmups--;
		}
	}

	public Performance print() {
		System.out.print(": " + df.format(getCycleTimeMS()) + "ms/test, ");
		System.out
				.print(df.format(totalMemory / repeats / 1024.0) + " kb/test");
		return this;
	}
	public Performance printCSV(boolean finalComma) {
		System.out.print(name + ", " + df.format(getCycleTimeMS()) + ", ");
		System.out.print(df.format(totalMemory / repeats / 1024.0));
		if (finalComma)
			System.out.print(",");
		return this;
	}

	public abstract void init();
	public abstract void run(boolean warmup);

	public double getCycleTimeMS() {
		return totalTime / repeats / 1000000.0;
	}
}
