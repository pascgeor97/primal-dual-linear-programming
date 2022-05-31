package com.auth.gr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Main {
    // com.auth.gr.Main arrays used.
    private static Integer minmax;
    private static String[] c;
    private static ArrayList<ArrayList<String>> A = new ArrayList<ArrayList<String>>();
    private static String[] b;
    private static String[] equin;

    // hashmap that contains the variable as a key and a symbol as a value.
    //e.g [ <"1"," free"> , <"2", ">="> , <"3", "<="> ]
    static HashMap<String, String> vars = new HashMap<String, String>();

    public static void main(String[] args) {

        try {
            fillArrays();
            convertFromPrimalToDual();
            produceOutput();
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    /*
     * Method that reads the input file and fills the 'main' arrays.
     */
    public static void fillArrays() throws IOException {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("Files/input.txt"));
        } catch (FileNotFoundException e) {
            System.out.println("File not found!" + "Files/input.txt");
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        String currLine = "";
        String tempLine = "";
        // reading the input file line by line and extracting the coefficients so we can add them to arrays.
        while (currLine != null) {
            currLine = br.readLine();
            if (currLine == null) {
                break;
            } else {
                currLine = currLine.replaceAll("\\s", "");
            }
            if (currLine.contains("MinMax")) {
                minmax = Integer.parseInt(currLine.replaceAll("MinMax:", ""));
            } else if (currLine.contains("c")) {
                tempLine = currLine.replaceAll("c:", "");
                tempLine = tempLine.replaceAll("[\\[\\] ]", "");
                c = tempLine.split(",");
            } else if (currLine.contains("A")) {
                tempLine = currLine.replaceAll("A:", "");
                tempLine = tempLine.replaceAll("[\\[\\] ]", "");
                // Secondary arrays.
                String[] tempA = tempLine.split(",");
                ArrayList<String> rowA = new ArrayList<String>(Arrays.asList(tempA));
                A.add(rowA);

                while (!currLine.contains("b")) {
                    currLine = br.readLine();
                    if (currLine.equals("")) {
                        break;
                    }
                    tempLine = currLine.replaceAll("[\\[\\] ]", "");
                    tempA = tempLine.split(",");
                    rowA = new ArrayList<String>(Arrays.asList(tempA));
                    A.add(rowA);
                }
            } else if (currLine.contains("b")) {
                tempLine = currLine.replaceAll("b:", "");
                tempLine = tempLine.replaceAll("[\\[\\] ]", "");
                b = tempLine.split(",");
            } else if (currLine.contains("equin")) {
                tempLine = currLine.replaceAll("Equin:", "");
                tempLine = tempLine.replaceAll("[\\[\\] ]", "");
                equin = tempLine.split(",");
            } else if (currLine.contains("x")) {
                tempLine = currLine.replaceAll("[x,]", "");

            }
        }
    }

    /*
     * Method that converts the given primal problem to the equivalent dual.
     */
    public static void convertFromPrimalToDual() {
        //Depending on the inequality(>= , <=) and the type of optimization we are changing the non-negativity constraints.
        String eq;
        if (minmax == 1) {
            eq = "+1";
            for (int i = 0; i < equin.length; i++) {
                if (Integer.parseInt(equin[i]) == 0) {
                    vars.put(i + "", "free");
                } else if (Integer.parseInt(equin[i]) == -1) {
                    vars.put(i + "", ">=");
                } else if (Integer.parseInt(equin[i]) == +1) {
                    vars.put(i + "", "<=");
                }
            }
        } else {
            eq = "-1";
            for (int i = 0; i < equin.length; i++) {

                if (Integer.parseInt(equin[i]) == 0) {
                    vars.put(i + "", "free");
                } else if (Integer.parseInt(equin[i]) == -1) {
                    vars.put(i + "", "<=");
                } else if (Integer.parseInt(equin[i]) == +1) {
                    vars.put(i + "", ">=");
                }
            }
        }
        //Switch the type of optimization.
        minmax = -minmax;
        String[] tempAr;
        //Switching c with b.
        tempAr = c;
        c = b;
        b = tempAr;
        //We take the transpose of the matrix (A) of coefficients of the left-hand side of the inequality
        A = transpose(A);

        //Adding +1 or -1 depending on the optimization.(Since all the variables are >=0)
        for (int i = 0; i < equin.length; i++) {
            equin[i] = eq;
        }

    }

    /*
     * Method that produces the output file.
     */
    public static void produceOutput() throws IOException {

        try (Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Files/output.txt"), "utf-8"))) {

            w.write("MinMax : " + minmax + "\n");
            w.write("\nc : [");
            for (int i = 0; i < c.length; i++) {
                if (i + 1 < c.length) {
                    w.write(c[i] + " ,");
                } else {
                    w.write(c[i]);
                }

            }
            w.write("]\n");
            w.write("\nA: ");

            for (int i = 0; i < A.size(); i++) {

                if (i == 0) {
                    w.write(A.get(i) + "\n");
                } else {
                    w.write("   " + A.get(i) + "\n");
                }
            }
            w.write("\nb: [");
            for (int i = 0; i < b.length; i++) {
                if (i + 1 < b.length) {
                    w.write(b[i] + " ,");
                } else {
                    w.write(b[i]);
                }

            }
            w.write("]\n");

            w.write("\nequin : [");
            for (int i = 0; i < equin.length; i++) {
                if (i + 1 < equin.length) {
                    w.write(equin[i] + " ,");
                } else {
                    w.write(equin[i]);
                }

            }
            w.write("]\n");
            w.write("\n");
            int i = 0;
            for (Map.Entry<String, String> entry : vars.entrySet()) {
                int key = Integer.parseInt(entry.getKey()) + 1;
                Object value = entry.getValue();
                if (!(i++ == vars.size() - 1)) {
                    if (!value.equals("free")) {
                        w.write("x" + key + value + 0 + " ,");
                    } else {
                        w.write("x" + key + " " + value + " ,");
                    }
                } else {
                    if (!value.equals("free")) {
                        w.write("x" + key + value + 0);
                    } else {
                        w.write("x" + key + " " + value);
                    }
                }


            }
            System.out.println("Output file created!");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /*
     * Method that given a 2d arraylist returns it's 2d transpose arraylist.
     */
    public static ArrayList<ArrayList<String>> transpose(ArrayList<ArrayList<String>> ar) {
        ArrayList<ArrayList<String>> ret = new ArrayList<ArrayList<String>>();
        for (int i = 0; i < ar.get(0).size(); i++) {
            ArrayList<String> col = new ArrayList<String>();
            for (ArrayList<String> row : ar) {
                col.add(row.get(i));
            }
            ret.add(col);

        }
        return ret;
    }


    /*
     * Print method for testing purposes.
     */
    public static void printArrays() {
        System.out.println("minmax");
        System.out.println(minmax);
        System.out.println("c: ");
        for (String str : c) {
            System.out.println(str);
        }
        System.out.println("b: ");
        for (String str : b) {
            System.out.println(str);
        }
        for (String str : equin) {
            System.out.println(str);
        }
        for (int i = 0; i < A.size(); i++) {
            System.out.println(A.get(i));
        }
    }

}
