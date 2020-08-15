package com.panpan.erm2text;

import com.panpan.erm2text.plugin.MyBatis_MysqlMojo;

import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        //File[] files = new File[]{new File("/Users/lipan/IdeaProjects/my_projects/my_dev/erm2text/src/main/resources/apsApply_mysql.erm")};

        if (args.length != 2){
            System.out.println("Usage: java -jar erm2text ${inputfile} ${outputfile}");
            System.exit(1);
        }

        String inputFile = args[0];
        String outputFile = args[1];

        File[] files = new File[]{new File(inputFile)};
        MyBatis_MysqlMojo app = new MyBatis_MysqlMojo();
        app.setSources(files);
        app.execute(outputFile);
    }
}
