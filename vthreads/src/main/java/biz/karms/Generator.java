package biz.karms;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.SplittableRandom;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * Dead simple single threaded number generator. The point is to provide data for Cruncher.java to add up. Storing pre-generated
 * files is impractical as they are too big for GitHub even when excruciatingly xz compressed and even after the entropy is
 * lowered so as there are many repeated same strings.
 * <p>
 * java biz.karms.Generator 10000 Generated 10000x10000 Byte    type numbers in file 'numbers_10000x10000_Byte_6163717920.txt'.
 * It took 3229ms. Total sum is: 6163717920. Generated 10000x10000 Short   type numbers in file
 * 'numbers_10000x10000_Short_1572124727100.txt'. It took 3955ms. Total sum is: 1572124727100. Generated 10000x10000 Integer
 * type numbers in file 'numbers_10000x10000_Integer_105886702716832946.txt'. It took 5868ms. Total sum is: 105886702716832946.
 * Generated 10000x10000 Long    type numbers in file 'numbers_10000x10000_Long_455773982995457442748406608.txt'. It took
 * 8084ms. Total sum is: 455773982995457442748406608.
 */
public class Generator {

    private static final int MAX_DIGITS_LONG_SPACE = 20;
    private static final int MAX_DIGITS_INT_SPACE = 11;
    private static final int MAX_DIGITS_SHORT_SPACE = 6;
    private static final int MAX_DIGITS_BYTE_SPACE = 4;
    private static final int NUM_RND_VALUES = 1000;
    private static final long[] numbers = new long[NUM_RND_VALUES];
    private static final SplittableRandom s = new SplittableRandom();

    private static void fillNumbers(Class<?> type) {
        for (int i = 0; i < NUM_RND_VALUES; i++) {
            final long max = type == Byte.class ? Byte.MAX_VALUE
                    : type == Short.class ? Short.MAX_VALUE
                            : type == Integer.class ? Integer.MAX_VALUE : Long.MAX_VALUE;
            numbers[i] = s.nextLong(0, max);
        }
    }

    private static BigInteger generateLine(int dimension, FileChannel channel, Class<?> type) {
        final int capacity = (type == Byte.class ? MAX_DIGITS_BYTE_SPACE
                : type == Short.class ? MAX_DIGITS_SHORT_SPACE
                        : type == Integer.class ? MAX_DIGITS_INT_SPACE : MAX_DIGITS_LONG_SPACE) * dimension;
        final ByteBuffer buffer = ByteBuffer.allocate(capacity);
        BigInteger lineSumBig = null;
        long lineSum = 0L;
        if (type == Byte.class || type == Short.class) {
            for (int j = 0; j < dimension; j++) {
                final long n = numbers[s.nextInt(0, NUM_RND_VALUES)];
                lineSum = lineSum + n;
                buffer.put(String.valueOf(n).getBytes(StandardCharsets.US_ASCII));
                buffer.put((byte) ' ');
            }
        } else {
            lineSumBig = BigInteger.ZERO;
            for (int j = 0; j < dimension; j++) {
                final long n = numbers[s.nextInt(0, NUM_RND_VALUES)];
                lineSumBig = lineSumBig.add(BigInteger.valueOf(n));
                buffer.put(String.valueOf(n).getBytes(StandardCharsets.US_ASCII));
                buffer.put((byte) ' ');
            }

        }
        try {
            buffer.put((byte) '\n');
            channel.write(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return lineSumBig != null ? lineSumBig : BigInteger.valueOf(lineSum);
    }

    public static void main(String[] args) throws IOException {
        final int dimension = Integer.parseInt(args[0]);
        for (Class<?> type : new Class[] { Byte.class, Short.class, Integer.class, Long.class }) {
            final long startTime = System.currentTimeMillis();
            fillNumbers(type);
            final Path tmp = Files.createTempFile("numbers", "tmp");
            try (final FileChannel channel = FileChannel.open(tmp, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING);

                    final ExecutorService executor = (type == Byte.class || type == Short.class)
                            ? Executors.newSingleThreadExecutor()
                            : Executors.newVirtualThreadPerTaskExecutor()) {

                final BigInteger totalSum = IntStream.range(0, dimension)
                        .mapToObj(i -> CompletableFuture.supplyAsync(() -> generateLine(dimension, channel, type),
                                executor))
                        .toList()
                        .stream()
                        .map(CompletableFuture::join)
                        .reduce(BigInteger.ZERO, BigInteger::add);

                final Path dst = Path.of(
                        "numbers_" + dimension + "x" + dimension + "_" + type.getSimpleName() + "_" + totalSum + ".txt");
                Files.move(tmp, dst);
                System.out.println("Generated " + dimension + "x" + dimension + " " +
                        type.getSimpleName() + (" ".repeat(7 - type.getSimpleName().length())) +
                        " type numbers in file '" + dst + "'. It took " + (System.currentTimeMillis() - startTime)
                        + "ms. Total sum is: " + totalSum + ".");
                Files.deleteIfExists(tmp);
            }
        }
    }

    public static void main1(String[] args) throws IOException {
        final int dimension = Integer.parseInt(args[0]);
        for (Class<?> type : new Class[] { Byte.class, Short.class, Integer.class, Long.class }) {
            final long startTime = System.currentTimeMillis();
            fillNumbers(type);
            BigInteger totalSum = BigInteger.ZERO;
            final Path tmp = Files.createTempFile("numbers", "tmp");
            try (final BufferedWriter writer = Files.newBufferedWriter(tmp)) {
                for (int i = 0; i < dimension; i++) {
                    for (int j = 0; j < dimension; j++) {
                        final long n = numbers[s.nextInt(0, NUM_RND_VALUES)];
                        totalSum = totalSum.add(BigInteger.valueOf(n));
                        writer.write(Long.toString(n));
                        writer.write(' ');
                    }
                    writer.newLine();
                }
                writer.flush();
            }
            final Path dst = Path.of(
                    "numbers_" + dimension + "x" + dimension + "_" + type.getSimpleName() + "_" + totalSum + ".txt");
            Files.move(tmp, dst);
            System.out.println("Generated " + dimension + "x" + dimension + " " +
                    type.getSimpleName() + (" ".repeat(7 - type.getSimpleName().length())) +
                    " type numbers in file '" + dst + "'. It took " + (System.currentTimeMillis() - startTime)
                    + "ms. Total sum is: " + totalSum + ".");
            Files.deleteIfExists(tmp);
        }
    }
}
