package com.github.fge.grappa.run.trace;

import com.github.fge.grappa.buffers.InputBuffer;
import com.github.fge.grappa.exceptions.GrappaException;
import com.github.fge.grappa.matchers.base.Matcher;
import com.github.fge.grappa.run.ParseRunnerListener;
import com.github.fge.grappa.run.context.MatcherContext;
import com.github.fge.grappa.run.events.*;
import com.gs.collections.impl.map.mutable.primitive.IntIntHashMap;
import com.gs.collections.impl.map.mutable.primitive.IntLongHashMap;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

@ParametersAreNonnullByDefault
public final class TracingListener<V>
    extends ParseRunnerListener<V>
{
    private static final Map<String, ?> ENV
        = Collections.singletonMap("create", "true");
    @SuppressWarnings("HardcodedFileSeparator")
    private static final String NODE_PATH = "/nodes.csv";
    @SuppressWarnings("HardcodedFileSeparator")
    private static final String MATCHERS_PATH = "/matchers.csv";
    @SuppressWarnings("HardcodedFileSeparator")
    private static final String INPUT_TEXT_PATH = "/input.txt";
    @SuppressWarnings("HardcodedFileSeparator")
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

    private final IntIntHashMap nodeIds = new IntIntHashMap();
    private int nextNodeId = 0;

    private final IntIntHashMap prematchMatcherIds = new IntIntHashMap();
    private final IntIntHashMap prematchIndices = new IntIntHashMap();
    private final IntLongHashMap prematchTimes = new IntLongHashMap();

    private final Path zipPath;
    private final Path nodeFile;
    private final BufferedWriter writer;
    private final StringBuilder sb = new StringBuilder();

    public TracingListener(Path zipPath, boolean delete)
        throws IOException
    {
        this.zipPath = zipPath;
        if (delete)
            Files.deleteIfExists(zipPath);
        nodeFile = Files.createTempFile("nodes", ".csv");
        writer = Files.newBufferedWriter(nodeFile, UTF_8);
    }

    @Override
    public void beforeParse(PreParseEvent<V> event)
    {
        nodeIds.put(-1, -1);
        inputBuffer = event.getContext().getInputBuffer();
        nrChars = inputBuffer.length();
        nrLines = inputBuffer.getLineCount();
        startTime = System.currentTimeMillis();
    }

    @Override
    public void beforeMatch(PreMatchEvent<V> event)
    {
        MatcherContext<V> context = event.getContext();
        Matcher matcher = context.getMatcher();

        Integer id = matcherIds.get(matcher);
        if (id == null) {
            //noinspection UnnecessaryBoxing
            id = Integer.valueOf(nextMatcherId);
            matcherIds.put(matcher, id);
            matcherDescriptors.put(matcher,
                new MatcherDescriptor(nextMatcherId, matcher));
            nextMatcherId++;
        }

        int level = context.getLevel();

        nodeIds.put(level, nextNodeId);
        nextNodeId++;

        prematchMatcherIds.put(level, id);
        int startIndex = Math.min(nrChars, context.getCurrentIndex());
        prematchIndices.put(level, startIndex);
        prematchTimes.put(level, System.nanoTime());
    }

    @SuppressWarnings({ "AutoBoxing", "AutoUnboxing" })
    @Override
    public void matchSuccess(MatchSuccessEvent<V> event)
    {
        long endTime = System.nanoTime();
        MatcherContext<V> context = event.getContext();
        int level = context.getLevel();

        Integer parentNodeId = nodeIds.get(level - 1);
        Integer nodeId = nodeIds.get(level);

        int startIndex = prematchIndices.get(level);
        int endIndex
            = Math.min(nrChars, context.getCurrentIndex());

        Integer matcherId = prematchMatcherIds.get(level);

        long time = endTime - prematchTimes.get(level);

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
    public void matchFailure(MatchFailureEvent<V> event)
    {
        long endTime = System.nanoTime();
        MatcherContext<V> context = event.getContext();
        int level = context.getLevel();

        Integer parentNodeId = nodeIds.get(level - 1);
        Integer nodeId = nodeIds.get(level);

        int startIndex = prematchIndices.get(level);
        int endIndex = context.getCurrentIndex();

        Integer matcherId = prematchMatcherIds.get(level);

        long time = endTime - prematchTimes.get(level);

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
    public void afterParse(PostParseEvent<V> event)
    {
        try {
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw cleanup(e);
        }

        URI uri = URI.create("jar:" + zipPath.toUri());

        try (
            final FileSystem zipfs = FileSystems.newFileSystem(uri, ENV)
        ) {
            Files.move(nodeFile, zipfs.getPath(NODE_PATH));
            copyInputText(zipfs);
            copyMatcherInfo(zipfs);
            copyParseInfo(zipfs);
        } catch (IOException e) {
            throw cleanup(e);
        }
    }

    private void copyInputText(FileSystem zipfs)
        throws IOException
    {
        Path path = zipfs.getPath(INPUT_TEXT_PATH);

        String s = inputBuffer.extract(0, nrChars);
        nrCodePoints = s.codePointCount(0, nrChars);

        try (
            final BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)
        ) {
            writer.write(s);
            writer.flush();
        }
    }

    private void copyMatcherInfo(FileSystem zipfs)
    {
        Path path = zipfs.getPath(MATCHERS_PATH);

        try (
            final BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)
        ) {
            for (MatcherDescriptor descriptor:
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
    private void copyParseInfo(FileSystem zipfs)
        throws IOException
    {
        Path path = zipfs.getPath(INFO_PATH);
        try (

            final BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)
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

    private GrappaException cleanup(IOException e)
    {
        GrappaException ret
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
