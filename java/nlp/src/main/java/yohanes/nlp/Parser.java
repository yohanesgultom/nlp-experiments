package yohanes.nlp;

import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.Parse;
import yohanes.nlp.tools.util.Tree;

import java.io.*;

public class Parser {

    public String parse(opennlp.tools.parser.Parser parser, String sentence) throws Exception {
        StringBuffer sb =  new StringBuffer();
        Parse[] topParses = ParserTool.parseLine(sentence, parser, 1);
        topParses[0].show(sb);
        return sb.toString();
    }

    public void convertTrainFile(String rawTrainFile, String trainFile) throws Exception {
        BufferedReader br = null;
        String line = null;
        br = new BufferedReader(new FileReader(rawTrainFile));
        File outFile = new File(trainFile);

        // if file doesn't exists, then create it
        if (!outFile.exists()) {
            outFile.createNewFile();
        }

        FileWriter fw = new FileWriter(outFile.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);

        try {
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    Tree tree = Tree.parse(line);
                    String toWrite = tree.toString();
                    if (toWrite != null) {
                        bw.write(toWrite + "\n");
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage() + "\n" + line + "\n");
        }

        // close streams
        if (br != null) br.close();
        if (bw != null) bw.close();
    }
}
