package com.github.fge.grappa.run.trace;

import com.github.fge.grappa.buffers.InputBuffer;
import com.github.fge.grappa.exceptions.GrappaException;
import com.github.fge.grappa.matchers.base.Matcher;
import com.github.fge.grappa.run.ParseRunnerListener;
import com.github.fge.grappa.run.events.MatchFailureEvent;
import com.github.fge.grappa.run.events.MatchSuccessEvent;
import com.github.fge.grappa.run.events.PostParseEvent;
import com.github.fge.grappa.run.events.PreMatchEvent;
import com.github.fge.grappa.run.events.PreParseEvent;
import com.github.fge.grappa.run.context.MatcherContext;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

@ParametersAreNonnullByDefault
public final class TracingListener<V>
    extends ParseRunnerListener<V>
{
    private static final Map<String, ?> ENV
        = Collections.singletonMap("create", "true");
    private static final String NODE_PATH = "/nodes.csv";
    private static final String MATCHERS_PATH = "/matchers.csv";
    private static final String INPUT_TEXT_PATH = "/input.txt";
    private static final String INFO_PATH = "/info.csv";

    private InputBuffer inputBuffer = null;
    private long startTime = 0L;
    private int nrLines = 0;
    private int nrChars = 0;
    private int nrCodePoints = 0;

    private final Map<Matcher, MatcherDescriptor> matcherDescriptors
        = new IdentityHashMap<>();

    private final Map<Matcher, Integer> matcherIds = new IdentityHashMap<>();
    private int nextMatcherId = 0;

    private final Map<Integer, Integer> nodeIds = new HashMap<>();
    private int nextNodeId = 0;

    private final Map<Integer, Integer> prematchMatcherIds = new HashMap<>();
    private final Map<Integer, Integer> prematchIndices = new HashMap<>();
    private final Map<Integer, Long> prematchTimes = new HashMap<>();

    private final Path zipPath;
    private final Path nodeFile;
    private final BufferedWriter writer;
    private final StringBuilder sb = new StringBuilder();

    public TracingListener(final Path zipPath, final boolean delete)
        throws IOException
    {
        this.zipPath = zipPath;
        if (delete)
            Files.deleteIfExists(zipPath);
        nodeFile = Files.createTempFile("nodes", ".csv");
        writer = Files.newBufferedWriter(nodeFile, UTF_8);
    }

    @Override
    public void beforeParse(final PreParseEvent<V> event)
    {
        nodeIds.put(-1, -1);
        inputBuffer = event.getContext().getInputBuffer();
        nrChars = inputBuffer.length();
        nrLines = inputBuffer.getLineCount();
        startTime = System.currentTimeMillis();
    }

    @Override
    public void beforeMatch(final PreMatchEvent<V> event)
    {
        final MatcherContext<V> context = event.getContext();
        final Matcher matcher = context.getMatcher();

        Integer id = matcherIds.get(matcher);
        if (id == null) {
            //noinspection UnnecessaryBoxing
            id = Integer.valueOf(nextMatcherId);
            matcherIds.put(matcher, id);
            matcherDescriptors.put(matcher,
                new MatcherDescriptor(nextMatcherId, matcher));
            nextMatcherId++;
        }

        final int level = context.getLevel();

        nodeIds.put(level, nextNodeId);
        nextNodeId++;

        prematchMatcherIds.put(level, id);
        final int startIndex = Math.min(nrChars, context.getCurrentIndex());
        prematchIndices.put(level, startIndex);
        prematchTimes.put(level, System.nanoTime());
    }

    @SuppressWarnings({ "AutoBoxing", "AutoUnboxing" })
    @Override
    public void matchSuccess(final MatchSuccessEvent<V> event)
    {
        final long endTime = System.nanoTime();
        final MatcherContext<V> context = event.getContext();
        final int level = context.getLevel();

        final Integer parentNodeId = nodeIds.get(level - 1);
        final Integer nodeId = nodeIds.get(level);

        final int startIndex = prematchIndices.get(level);
        final int endIndex
            = Math.min(nrChars, context.getCurrentIndex());

        final Integer matcherId = prematchMatcherIds.get(level);

        final long time = endTime - prematchTimes.get(level);

        // Write:
        // parent;id;level;success;matcherId;start;end;time
        sb.setLength(0);
        sb.append(parentNodeId).append(';')
            .append(nodeId).append(';')
            .append(level).append(";1;")
            .append(matcherId).append(';')
            .append(startIndex).append(';')
            .append(endIndex).append(';')
            .append(time).append('\n');
        try {
            writer.append(sb);
        } catch (IOException e) {
            throw cleanup(e);
        }
    }

    @SuppressWarnings({ "AutoBoxing", "AutoUnboxing" })
    @Override
    public void matchFailure(final MatchFailureEvent<V> event)
    {
        final long endTime = System.nanoTime();
        final MatcherContext<V> context = event.getContext();
        final int level = context.getLevel();

        final Integer parentNodeId = nodeIds.get(level - 1);
        final Integer nodeId = nodeIds.get(level);

        final int startIndex = prematchIndices.get(level);
        final int endIndex = context.getCurrentIndex();

        final Integer matcherId = prematchMatcherIds.get(level);

        final long time = endTime - prematchTimes.get(level);

        // Write:
        // parent;id;level;success;matcherId;start;end;time
        sb.setLength(0);
        sb.append(parentNodeId).append(';')
            .append(nodeId).append(';')
            .append(level).append(";0;")
            .append(matcherId).append(';')
            .append(startIndex).append(';')
            .append(endIndex).append(';')
            .append(time).append('\n');
        try {
            writer.append(sb);
        } catch (IOException e) {
            throw cleanup(e);
        }
    }

    @Override
    public void afterParse(final PostParseEvent<V> event)
    {
        try {
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw cleanup(e);
        }

        final URI uri = URI.create("jar:" + zipPath.toUri());

        try (
            final FileSystem zipfs = FileSystems.newFileSystem(uri, ENV);
        ) {
            Files.move(nodeFile, zipfs.getPath(NODE_PATH));
            copyInputText(zipfs);
            copyMatcherInfo(zipfs);
            copyParseInfo(zipfs);
        } catch (IOException e) {
            throw cleanup(e);
        }
    }

    private void copyInputText(final FileSystem zipfs)
        throws IOException
    {
        final Path path = zipfs.getPath(INPUT_TEXT_PATH);

        final String s = inputBuffer.extract(0, nrChars);
        nrCodePoints = s.codePointCount(0, nrChars);

        try (
            final BufferedWriter writer = Files.newBufferedWriter(path, UTF_8);
        ) {
            writer.write(s);
            writer.flush();
        }
    }

    private void copyMatcherInfo(final FileSystem zipfs)
    {
        final Path path = zipfs.getPath(MATCHERS_PATH);

        try (
            final BufferedWriter writer = Files.newBufferedWriter(path, UTF_8);
        ) {
            for (final MatcherDescriptor descriptor:
                matcherDescriptors.values()) {
                sb.setLength(0);
                sb.append(descriptor.getId()).append(';')
                    .append(descriptor.getClassName()).append(';')
                    .append(descriptor.getType()).append(';')
                    .append(descriptor.getName()).append('\n');
                writer.append(sb);
            }
            writer.flush();
        } catch (IOException e) {
            throw cleanup(e);
        }
    }

    // MUST be called after copyInputText!
    private void copyParseInfo(final FileSystem zipfs)
        throws IOException
    {
        final Path path = zipfs.getPath(INFO_PATH);
        try (

            final BufferedWriter writer = Files.newBufferedWriter(path, UTF_8);
        ) {
            sb.setLength(0);
            sb.append(startTime).append(';')
                .append(prematchIndices.size()).append(';')
                .append(nextMatcherId).append(';')
                .append(nrLines).append(';')
                .append(nrChars).append(';')
                .append(nrCodePoints).append(';')
                .append(nextNodeId).append('\n');
            writer.append(sb);
            writer.flush();
        }
    }

    private GrappaException cleanup(final IOException e)
    {
        final GrappaException ret
            = new GrappaException("failed to write event", e);
        try {
            writer.close();
        } catch (IOException e2) {
            ret.addSuppressed(e2);
        }

        try {
            Files.deleteIfExists(nodeFile);
        } catch (IOException e3) {
            ret.addSuppressed(e3);
        }

        return ret;
    }
}
