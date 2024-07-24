package org.students;

public class TextEditor {
    /**
     * Regexes that are used in class to parse the .json file
     */
    public static final String idRegex = "\\s+\"id\": \\d+,?";
    public static final String fieldRegex = "\\s+\"[\\w\\d]+\":\\s+\"?[\\w\\d\\s]+\"?,?\n?";
    public static final String symbolsRegex = "[\\s\"{},]";
    public static final String endOfEntryRegex = "\\s+},";
    public static final String endOfLastEntryRegex = "\\s+}";
    public static final String beginOfNewEntryRegex = "\\s+\\{";
    public static final String inputRegex = "\\w+ [\\w\\d]+((, \\w+ [\\w\\d,.]+)+)?";

    public TextEditor() {}

    /**
     * Writes new student entry into file
     * @param id ID of new student
     * @param text StringBuilder with altered file
     * @param info String of specified format with information about student
     */
    public void writeNewStudent(int id, StringBuilder text, String info) {
        String[] infoPairs = info.split(", ");
        int i = infoPairs.length;

        writeNewLine(text, "\t\t{");
        writeNewLine(text, "\t\t\t\"id\": " + id + ",");

        for (String infoPair: infoPairs) {
            i-=1;
            writeNewField(i, infoPair, text);
        }

        text.append("\t\t}\n\t]\n}");
    }
    /**
     * Writes into text new field with data from infoPair
     * @param i Counter of how many fields is left to write
     * @param infoPair Contains key and value divided by space
     * @param text StringBuilder with altered file
     */
    public void writeNewField(int i, String infoPair, StringBuilder text) {
        String argumentDgt = "\t\t\t\"%s\": %s";
        String argumentStr = "\t\t\t\"%s\": \"%s\"";

        String[] entry = infoPair.split(" ");

        if (entry[1].matches("\\d+")) {
            text.append(String.format(argumentDgt, entry[0].toLowerCase(), entry[1]));
        } else text.append(String.format(argumentStr, entry[0].toLowerCase(), entry[1]));
        if (i == 0) {
            text.append("\n");
        } else text.append(",\n");
    }

    /**
     * Writes line with newline at the end into StringBuilder
     * @param sb StringBuilder
     * @param line String to write
     */
    public void writeNewLine(StringBuilder sb, String line) {
        sb.append(line).append("\n");
    }

    /**
     * Parses ID from string of specified format
     * @param line String of specified format
     * @return ID
     */
    public int parseId(String line) {
        return Integer.parseInt(
                clean(line).replace("id:", "")
        );
    }

    /**
     * Removes all special symbols that can be in a json file from string
     * @param line String to alter
     * @return Name of field and its value
     */
    public String clean(String line) {
        return line.replaceAll(symbolsRegex,"");
    }

    /**
     * Checks if String contains valid input
     * @param line String to be checked
     * @return True if line contains valid input
     */
    public boolean isValidInput(String line) {
        return line.matches(inputRegex);
    }
    /**
     * Checks if String contains ID
     * @param line String to be checked
     * @return True if line contains ID
     */
    public boolean isId(String line) {
        return line.matches(idRegex);
    }
    /**
     * Checks if String contains '}' that indicates last entry in json list
     * @param line String to be checked
     * @return True if line contains '}'
     */
    public boolean isEndOfLastEntry(String line) {
        return line.matches(endOfLastEntryRegex);
    }
    /**
     * Checks if String contains '{' that indicates beginning of new entry in json list
     * @param line String to be checked
     * @return True if line contains '{'
     */
    public boolean isBeginning(String line) {
        return line.matches(beginOfNewEntryRegex);
    }
    /**
     * Checks if String contains field of entry in json list
     * @param line String to be checked
     * @return True if line contains field
     */
    public boolean isField(String line){
        return line.matches(fieldRegex);
    }
    /**
     * Checks if String contains '},' that indicates beginning of new entry in json list
     * @param line String to be checked
     * @return True if line contains '},'
     */
    public boolean isEndOfEntry(String line) {
       return line.matches(endOfEntryRegex);
    }
}
