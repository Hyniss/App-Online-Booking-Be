package com.fpt.h2s.utilities;

import ananta.utility.ListEx;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
public final class ExceptionPrinter {
    
    private static final Set<String> packages = new HashSet<>();
    
    private static final Set<String> ignoreClasses = new HashSet<>();
    
    public static void registerPackage(@NonNull final String packageName) {
        ExceptionPrinter.packages.add(packageName);
    }
    
    public static void ignoreClass(@NonNull final Class<?> clazz) {
        ExceptionPrinter.ignoreClasses.add(clazz.getName());
    }
    
    public void print(@NonNull final Throwable throwable) {
        List<String> lines = getLinesToSprint(throwable);
        lines.forEach(System.err::println);
    }

    @NotNull
    public static List<String> getLinesToSprint(@NotNull Throwable throwable) {
        final Throwable rootCause = ExceptionUtils.getRootCause(throwable);

        return Stream.of(
            getLinesWrittenByDev(throwable, rootCause).stream(),
            Stream.of(throwable.getStackTrace()[0].getClassName() + ": " + throwable.getMessage()),
            ExceptionPrinter.getNextLinesToPrint(throwable, rootCause).stream().map(StackTraceElement::toString).map(line -> "\tat " + line)
        )
            .flatMap(lineList -> lineList)
            .toList();
    }

    private static List<String> getLinesWrittenByDev(@NotNull Throwable throwable, Throwable rootCause) {
        if (ExceptionPrinter.isSameCause(rootCause, throwable)) {
            return Collections.emptyList();
        }
        return ListEx.merge(
            List.of("Caused by: " + rootCause),
            ExceptionPrinter.getStackLinesWrittenByDev(rootCause).stream().map(StackTraceElement::toString).map(line -> "\tat " + line).collect(Collectors.toList())
        );
    }

    private static List<StackTraceElement> getNextLinesToPrint(@NonNull final Throwable throwable, final Throwable rootCause) {
        final StackTraceElement bugLine = rootCause.getStackTrace()[0];
        final boolean isErrorCausedByDependency = !ExceptionPrinter.isLineWrittenByDeveloper(bugLine);
        if (isErrorCausedByDependency) {
            final List<StackTraceElement> lines = List.of(throwable.getStackTrace());
            
            return Stream.of(
                ExceptionPrinter.getBugsLineOfDependency(lines),
                ExceptionPrinter.getStackLinesWrittenByDev(throwable)
            ).flatMap(Collection::stream).toList();
        }
        return ExceptionPrinter.getStackLinesWrittenByDev(throwable);
    }
    
    private static List<StackTraceElement> getBugsLineOfDependency(final List<StackTraceElement> lines) {
        final int firstLineWrittenByDev = MoreLists.firstIndexMatch(ExceptionPrinter::isLineWrittenByDeveloper, lines);
        if (firstLineWrittenByDev > 0) {
            return lines.subList(0, firstLineWrittenByDev);
        }
        return Collections.emptyList();
    }
    
    private static List<StackTraceElement> getStackLinesWrittenByDev(final Throwable throwable) {
        return Arrays
            .stream(throwable.getStackTrace())
            .filter(ExceptionPrinter::isLineWrittenByDeveloper)
            .toList();
    }
    
    private static void printLines(final List<StackTraceElement> lines) {
        lines.forEach(line -> System.err.println("\tat " + line));
    }
    
    private static boolean isSameCause(final Throwable t1, final Throwable t2) {
        if (t1.getClass() == t2.getClass()) {
            final StackTraceElement[] trace1 = t1.getStackTrace();
            final StackTraceElement[] trace2 = t2.getStackTrace();
            return trace1[0].equals(trace2[0]);
        }
        return false;
    }
    
    private static boolean isLineWrittenByDeveloper(final StackTraceElement line) {
        return ExceptionPrinter.packages.stream().anyMatch(package_ -> ExceptionPrinter.isOfValidPackage(line, package_) && ExceptionPrinter.isOfValidClass(line));
    }
    
    private static boolean isOfValidPackage(final StackTraceElement line, final String package_) {
        return line.getClassName().startsWith(package_);
    }
    
    private static boolean isOfValidClass(final StackTraceElement line) {
        return !ExceptionPrinter.ignoreClasses.contains(line.getClassName());
    }
    
}