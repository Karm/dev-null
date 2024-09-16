package biz.karms;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

/**
 * E.g. native-image 24-beta 2025-03-18 OpenJDK Runtime Environment Mandrel-24.2.0-dev0543913f7cb (build 24-beta+12-ea) OpenJDK
 * 64-Bit Server VM Mandrel-24.2.0-dev0543913f7cb (build 24-beta+12-ea, mixed mode)
 *
 *
 * HotSpot: numbers_10000_10000_byte_6300402964.txt Naive.     Total Sum: 6300402964. Best time of 3 iterations is 1062ms.
 * Optimized. Total Sum: 6300402964. Best time of 3 iterations is 326ms.
 *
 * numbers_10000_10000_int_107370412945462243.txt Naive.     Total Sum: 107370412945462243. Best time of 3 iterations is 3453ms.
 * Optimized. Total Sum: 107370412945462243. Best time of 3 iterations is 1539ms.
 *
 * numbers_10000_10000_long_461163529563343451580171979.txt Naive.     Total Sum: 461163529563343451580171979. Best time of 3
 * iterations is 5375ms. Optimized. Total Sum: 461163529563343451580171979. Best time of 3 iterations is 4181ms.
 *
 * numbers_10000_10000_short_1638289409979.txt Naive.     Total Sum: 1638289409979. Best time of 3 iterations is 2220ms.
 * Optimized. Total Sum: 1638289409979. Best time of 3 iterations is 869ms.
 *
 *
 * Native: numbers_10000_10000_byte_6300402964.txt Naive.     Total Sum: 6300402964. Best time of 3 iterations is 1896ms.
 * Optimized. Total Sum: 6300402964. Best time of 3 iterations is 1135ms.
 *
 * numbers_10000_10000_int_107370412945462243.txt Naive.     Total Sum: 107370412945462243. Best time of 3 iterations is
 * 10946ms. Optimized. Total Sum: 107370412945462243. Best time of 3 iterations is 3933ms.
 *
 * numbers_10000_10000_long_461163529563343451580171979.txt Naive.     Total Sum: 461163529563343451580171979. Best time of 3
 * iterations is 18719ms. Optimized. Total Sum: 461163529563343451580171979. Best time of 3 iterations is 8983ms.
 *
 * numbers_10000_10000_short_1638289409979.txt Naive.     Total Sum: 1638289409979. Best time of 3 iterations is 5928ms.
 * Optimized. Total Sum: 1638289409979. Best time of 3 iterations is 1819ms.
 */
public class Cruncher {

    /**
     * Gets rid of String's substring by constructing the number digit by digit manually. Used long type as long (pun intended)
     * as it fits and then switches to BigInteger.
     *
     * @param line
     * @return sum of numbers in the line
     */
    private static BigInteger processLineOptimized(String line) {
        long sum = 0L;
        BigInteger bigSum = null;

        long currentNumber = 0L;
        BigInteger currentNumberBig = null;

        boolean hasNumber = false;

        final int length = line.length();

        for (int i = 0; i < length; i++) {
            final char c = line.charAt(i);

            if (c >= '0' && c <= '9') {
                if (currentNumberBig == null) {
                    try {
                        long tmpCurrentNumber = Math.multiplyExact(currentNumber, 10);
                        tmpCurrentNumber = Math.addExact(tmpCurrentNumber, c - '0');
                        currentNumber = tmpCurrentNumber;
                    } catch (ArithmeticException e) {
                        currentNumberBig = BigInteger.valueOf(currentNumber);
                        currentNumberBig = currentNumberBig.multiply(BigInteger.TEN).add(BigInteger.valueOf(c - '0'));
                    }
                } else {
                    currentNumberBig = currentNumberBig.multiply(BigInteger.TEN).add(BigInteger.valueOf(c - '0'));
                }
                hasNumber = true;
            } else if (c == ' ') {
                if (hasNumber) {
                    if (bigSum == null) {
                        if (currentNumberBig == null) {
                            try {
                                sum = Math.addExact(sum, currentNumber);
                            } catch (ArithmeticException e) {
                                bigSum = BigInteger.valueOf(sum).add(BigInteger.valueOf(currentNumber));
                            }
                        } else {
                            bigSum = BigInteger.valueOf(sum).add(currentNumberBig);
                        }
                    } else {
                        if (currentNumberBig == null) {
                            bigSum = bigSum.add(BigInteger.valueOf(currentNumber));
                        } else {
                            bigSum = bigSum.add(currentNumberBig);
                        }
                    }
                    currentNumber = 0L;
                    currentNumberBig = null;
                    hasNumber = false;
                }
            }
        }

        // last num If line doesn't end in space
        if (hasNumber) {
            if (bigSum == null) {
                if (currentNumberBig == null) {
                    try {
                        sum = Math.addExact(sum, currentNumber);
                    } catch (ArithmeticException e) {
                        bigSum = BigInteger.valueOf(sum).add(BigInteger.valueOf(currentNumber));
                    }
                } else {
                    bigSum = BigInteger.valueOf(sum).add(currentNumberBig);
                }
            } else {
                if (currentNumberBig == null) {
                    bigSum = bigSum.add(BigInteger.valueOf(currentNumber));
                } else {
                    bigSum = bigSum.add(currentNumberBig);
                }
            }
        }

        return (bigSum != null) ? bigSum : BigInteger.valueOf(sum);
    }

    /**
     * Pays heavily for String's substring and for using BigInteger for all numbers.
     *
     * @param line
     * @return sum of numbers in the line
     */
    private static BigInteger processLineNaive(String line) {
        BigInteger sum = BigInteger.ZERO;
        final int length = line.length();
        int start = 0;
        for (int i = 0; i < length; i++) {
            if (line.charAt(i) == ' ') {
                if (start < i) {
                    sum = sum.add(new BigInteger(line.substring(start, i)));
                }
                start = i + 1;
            }
        }
        // last num If line doesn't end in space
        if (start < length) {
            sum = sum.add(new BigInteger(line.substring(start)));
        }
        return sum;
    }

    public static BigInteger processLine(String line, boolean optimized) {
        if (optimized) {
            return processLineOptimized(line);
        }
        return processLineNaive(line);
    }

    public static void main(String[] args) throws IOException {
        final Path path = Path.of(args.length > 0 ? args[0] : "numbers.txt");

        final int ITERATIONS = 3;
        long bestTime = Long.MAX_VALUE;
        BigInteger totalSum = null;

        for (boolean optimized : new boolean[] { false, true }) {
            for (int i = 0; i < ITERATIONS; i++) {
                long startTime = System.currentTimeMillis();
                try (final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
                        final Stream<String> lines = Files.lines(path)) {
                    final BigInteger sum = lines
                            .map(line ->
                                    CompletableFuture.supplyAsync(() -> processLine(line, optimized), executor)
                            ).toList()
                            .parallelStream()
                            .map(CompletableFuture::join)
                            .reduce(BigInteger.ZERO, BigInteger::add);
                    final long time = System.currentTimeMillis() - startTime;
                    if (time < bestTime) {
                        bestTime = time;
                    }
                    if (totalSum == null) {
                        totalSum = sum;
                    } else {
                        if (!totalSum.equals(sum)) {
                            throw new IllegalStateException("Sums differ: " + totalSum + " vs " + sum);
                        }
                    }
                }
            }
            System.out.println((optimized ? "Optimized." : "Naive.    ") + " Total Sum: " + totalSum + ". "
                    + "Best time of " + ITERATIONS + " iterations is " + bestTime + "ms.");
            bestTime = Long.MAX_VALUE;
        }
    }
}
