package com.isograd.exercise;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class RunTestWithData {
    public static void main(String[] args) throws Exception {
        PrintStream originalOut = System.out;
        MyPrintStream myPrintStream = new MyPrintStream(originalOut, "dataOutputExpected.txt");
        System.setOut(myPrintStream);
        InputStream originalIn = System.in;
        System.setIn(new MyInputStream("dataInput.txt"));
        IsoContest.main(null);

        System.setOut(originalOut);
        System.setIn(originalIn);

        if (myPrintStream.isAllExpectedLinesProvided()) {
            System.out.println("Success !");
        } else {
            System.out.println("Failed challenge :(");
        }
    }

    static class MyPrintStream extends PrintStream {
        List<String> allLinesExpected;
        int nExpectedLine;
        int nBadOut;
        boolean allExpectedLinesProvided = false;

        public MyPrintStream(OutputStream out, String dataOuputExpectedFile) throws IOException {
            super(out);
            allLinesExpected = Files.readAllLines(Paths.get(getClass()
                    .getClassLoader().getResource(dataOuputExpectedFile).getPath()));
        }

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

        @Override
        public void println(String x) {
            String expectedOut = null;
            if (nExpectedLine < allLinesExpected.size()) {
                expectedOut = allLinesExpected.get(nExpectedLine++);
                if (!expectedOut.equals(x)) {
                    super.print("Bad out ! Expected : " + expectedOut + " | ");
                    nBadOut++;
                } else if (nExpectedLine == allLinesExpected.size() && nBadOut == 0) {
                    allExpectedLinesProvided = true;
                }
            } else {
                super.print("No out expected !! => ");
            }

            super.println(x);
        }

        public boolean isAllExpectedLinesProvided() {
            return allExpectedLinesProvided;
        }
    }

    static class MyInputStream extends InputStream {

        private InputStream inputStreamFile;

        public MyInputStream(String dataFile) throws IOException {
            inputStreamFile = getClass()
                    .getClassLoader().getResourceAsStream(dataFile);

        }

        @Override
        public int read() throws IOException {
            return inputStreamFile.read();
        }
    }
}
