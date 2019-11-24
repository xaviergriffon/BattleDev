package com.isograd.exercise;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;

public class RunTestWithData {

    private static final int WARN_TIME = 30;
    private static final int WARN_MEMORY_MO = 5;

    private static final double MEGABYTE = 1024d * 1024d;
    /*
     * Palette de couleur pour la console
     */
    enum ConsoleColor {
        ANSI_RESET("\u001B[0m"),
        ANSI_BLACK("\u001B[30m"),
        ANSI_RED("\u001B[31m"),
        ANSI_GREEN("\u001B[32m"),
        ANSI_YELLOW("\u001B[33m"),
        ANSI_BLUE("\u001B[34m"),
        ANSI_PURPLE("\u001B[35m"),
        ANSI_CYAN("\u001B[36m"),
        ANSI_WHITE("\u001B[37m");
        private String stringColor;
        ConsoleColor(String stringColor) {
            this.stringColor = stringColor;
        }
        public String getStringColor() {
            return this.stringColor;
        }
    }

    public static void main(String[] args) throws Exception {
        /*
         * Configuration des Entrées/Sorties pour intercepter les demandes afin de fournir
         * les entrées de l'exercice et comparer les sorties aux attentes.
         */
        PrintStream originalOut = System.out;
        RunTestPrintStream runTestPrintStream = new RunTestPrintStream(originalOut, "dataOutputExpected.txt");
        System.setOut(runTestPrintStream);
        InputStream originalIn = System.in;
        System.setIn(RunTestWithData.class
                .getClassLoader()
                .getResourceAsStream("dataInput.txt"));

        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        Calendar startTime = Calendar.getInstance();
        long memory = runtime.totalMemory() - runtime.freeMemory();
        IsoContest.main(null);
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) - memory;
        Calendar endTime = Calendar.getInstance();

        // Redéfinition des Entrées/Sorties avec leurs valeurs originales.
        System.setOut(originalOut);
        System.setIn(originalIn);

        // Indicateurs d'exécutions
        checkMemoryUsage(usedMemory);
        checkExecutionTime(endTime.getTimeInMillis() - startTime.getTimeInMillis());

        // Résultat
        if (runTestPrintStream.isAllExpectedLinesProvided()) {
            println(ConsoleColor.ANSI_BLUE, "Success !");
        } else {
            println(ConsoleColor.ANSI_RED, "Failed challenge :(");
        }
    }

    private static void checkExecutionTime(long timeInMillis) {
        DecimalFormat df = new DecimalFormat("0.##");
        double timeInSecond = timeInMillis / 1000d;
        println(ConsoleColor.ANSI_GREEN, "Execution time : " + df.format(timeInSecond) + "s");
        if (timeInSecond > WARN_TIME) {
            println(ConsoleColor.ANSI_RED, "/!\\ execution time > " + WARN_TIME + "s");
        }
    }

    public static void checkMemoryUsage(double usedMemoryInOctet) {
        DecimalFormat df = new DecimalFormat("0.##");
        println(ConsoleColor.ANSI_GREEN, "Used memory : "+(df.format( usedMemoryInOctet/MEGABYTE)) + "Mo");
        if (usedMemoryInOctet > (WARN_MEMORY_MO * MEGABYTE)) {
            println(ConsoleColor.ANSI_YELLOW, "/!\\ Memory usage > " + WARN_MEMORY_MO + "Mo");
        }
    }

    public static void println(ConsoleColor color, String message) {
        println(System.out, color, message);
    }

    public static void println(PrintStream printStream, ConsoleColor color, String message) {
        printStream.println(color.getStringColor() + message + ConsoleColor.ANSI_RESET.getStringColor());
    }

    public static void print(PrintStream printStream, ConsoleColor color, String message) {
        printStream.print(color.getStringColor() + message + ConsoleColor.ANSI_RESET.getStringColor());
    }

    static class RunTestPrintStream extends PrintStream {
        List<String> allLinesExpected;
        int nExpectedLine;
        int nBadOut;
        boolean allExpectedLinesProvided = false;

        public RunTestPrintStream(OutputStream out, String dataOuputExpectedFile) throws IOException {
            super(out);
            allLinesExpected = Files.readAllLines(Paths.get(getClass()
                    .getClassLoader().getResource(dataOuputExpectedFile).getPath()));
        }

        // --- Toutes méthodes println doivent utiliser println(String x)

        @Override
        public void println(int x) {
            this.println(String.valueOf(x));
        }

        @Override
        public void println(long x) {
            this.println(String.valueOf(x));
        }

        @Override
        public void println(double x) {
            super.println(String.valueOf(x));
        }

        // -----------------

        @Override
        public void println(String x) {
            String expectedOut = null;
            if (nExpectedLine < allLinesExpected.size()) {
                expectedOut = allLinesExpected.get(nExpectedLine++);
                if (!expectedOut.equals(x)) {
                    RunTestWithData.print((PrintStream) super.out, ConsoleColor.ANSI_RED, "Bad out ! Expected : " + expectedOut + " | ");
                    nBadOut++;
                } else if (nExpectedLine == allLinesExpected.size() && nBadOut == 0) {
                    allExpectedLinesProvided = true;
                }
            } else {
                RunTestWithData.print((PrintStream) super.out, ConsoleColor.ANSI_RED, "No out expected !! => ");
            }

            super.println(x);
        }

        public boolean isAllExpectedLinesProvided() {
            return allExpectedLinesProvided;
        }
    }
}
