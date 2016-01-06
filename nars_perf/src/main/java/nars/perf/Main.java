package nars.perf;

import nars.perf.nars.nar.perf.NARBenchmark;
import org.openjdk.jmh.profile.StackProfiler;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

/**
 * Created by me on 12/11/15.
 */
public enum Main {
	;

	public static void main(String[] args) throws RunnerException {
		Options opt = new OptionsBuilder()
				// .include(".*" + YourClass.class.getSimpleName() + ".*")

				.include(NARBenchmark.class.getSimpleName())
				.warmupIterations(2)
				.measurementIterations(10)
				.threads(1)
				.forks(1)

				.resultFormat(ResultFormatType.TEXT)
				// .verbosity(VerboseMode.EXTRA) //VERBOSE OUTPUT

				.addProfiler(StackProfiler.class,
						"lines=3;top=50;period=4;detailLine=true")

				// .addProfiler(HotspotRuntimeProfiler.class)
				// .addProfiler(HotspotMemoryProfiler.class)
				// .addProfiler(HotspotThreadProfiler.class)
				// .addProfiler(HotspotCompilationProfiler.class)
				// .addProfiler(HotspotClassloadingProfiler.class)
				/*
				 * .addProfiler(LinuxPerfProfiler.class)
				 * .addProfiler(LinuxPerfAsmProfiler.class)
				 * .addProfiler(LinuxPerfNormProfiler.class)
				 */
				// .addProfiler(CompilerProfiler.class)
				// .addProfiler(GCProfiler.class)

				.timeout(TimeValue.seconds(5)).build();

		new Runner(opt).run();
	}

}
