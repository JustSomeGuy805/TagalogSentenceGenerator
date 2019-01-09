package com.renegarcia.tagphrgen;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

public class DatabaseUtil
{
    private static final String DB_DRIVER = "org.h2.Driver";
    private static final String DB_CONNECTION = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;INIT=runscript from 'classpath:create.sql'";
    private static final String DB_USER = "SA";
    private static final String DB_PASSWORD = "";
    private static Connection conn;
    private static boolean isInit = false;


    final static Logger log = Logger.getLogger(DatabaseUtil.class.getName());

    public static void initConnection() throws Exception
    {
        if (!isInit)
        {
            Class.forName(DB_DRIVER);
            conn = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);
            
     

            //create sentence insert statements;
            ConfigurationBuilder configuration = new ConfigurationBuilder()
                    .setUrls(ClasspathHelper.forPackage("sentence"))
                    .filterInputsBy(new FilterBuilder().includePackage("sentence."))                 
                    .addScanners(new ResourcesScanner());
            Reflections reflections = new Reflections(configuration);
            Set<String> resources = reflections.getResources(Pattern.compile(".*"));
            
            Scanner scanner  = null;
            Statement st = conn.createStatement();
            
            for (String r : resources)
            {
                scanner = new Scanner(DatabaseUtil.class.getResourceAsStream("/" + r));
                Matcher m = Pattern.compile("\\w+\\.txt").matcher(r);
                m.find();
                String category = m.group();
                category = category.substring(0, category.length() - 4);
               
                String format = "INSERT INTO sentence (category, tagalog, english) VALUES  ('%s', '%s', '%s')";
                
                while (scanner.hasNextLine())
                {
                    String line = scanner.nextLine().trim();
                  
                    
                    if (line.isEmpty())
                        continue;

                    if (line.startsWith("-"))
                        continue;

                    log.log(Level.INFO, "Making {0}", line);
                    String data[] = line.split("#");
                    String query = String.format(format, category, data[0], data[1]);
                    //System.out.printf("insert = %s\n", query);
                    st.addBatch(query);
                }
                scanner.close();
            }
            

            
            
                        //create word insert statements;
            configuration = new ConfigurationBuilder()
                    .setUrls(ClasspathHelper.forPackage("word"))
                    .filterInputsBy(new FilterBuilder().includePackage("word."))
                    .addScanners(new ResourcesScanner());
            reflections = new Reflections(configuration);
            resources = reflections.getResources(Pattern.compile(".*"));


            
            for (String r : resources)
            {
                scanner = new Scanner(DatabaseUtil.class.getResourceAsStream("/" + r));
                
                //Matcher m = Pattern.compile("\\w+\\.txt").matcher(r);
                //m.find();
                //String category = m.group();
                //category = category.substring(0, category.length() - 4);

                //create word insert statements
                String format = "INSERT INTO word (tagalog, english, tags) VALUES  ('%s', '%s', '%s')";
                while (scanner.hasNextLine())
                {
                    String line = scanner.nextLine().trim();
                    if (line.isEmpty())
                        continue;

                    String data[] = line.split(":");
                    String query = String.format(format, data[0], data[1], data[2]);
                    st.addBatch(query);
                }
            }

            st.executeBatch();
            isInit = true;
            scanner.close();
        }
    }

    






    public  static List<Word> getAllWords() throws Exception
    {
        List<Word> wordList = new ArrayList<>();
        ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM word");
        if(rs.first())
        {
            do
            {
                Word w = new Word();
                w.english = rs.getString("english");
                w.tagalog = rs.getString("tagalog");
                w.tags = rs.getString("tags");
              
                wordList.add(w)  ;
            }while(rs.next());
        }

        return wordList;
    }
    
    


    public  static List<Sentence> getAllSentences() throws Exception
    {
        List<Sentence> sentences = new ArrayList<>();
        ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM sentence ORDER BY category");
        if(rs.first())
        {
            do
            {
                Sentence s = new Sentence();
                s.category = rs.getString("category");
                s.tagalog = rs.getString("tagalog");
                s.english = rs.getString("english");
                sentences.add(s)  ;
            }while(rs.next());
        }

        return sentences;
    }





    public static List<Word> query(String required, String optional) throws Exception
    {
        String query = "SELECT * FROM word WHERE";

        //create required query
        String[] requiredArr = required.split(",");
        String requiredQuery = "";
        for(String s: requiredArr)
        {
            if(s.trim().isEmpty())
                continue;

            requiredQuery = requiredQuery + String.format(" REGEXP_LIKE (tags,'.*\\b%s\\b.*') AND", s.trim());
        }
        requiredQuery = requiredQuery.substring(0, requiredQuery.length() - 3);


        //create optional query
        String optionalQuery = "";
        if (!optional.trim().isEmpty())
        {
            optionalQuery = " AND (";
            String[] tagsArr = optional.split(",");

            for (String s : tagsArr) {
                if (s.trim().isEmpty())
                    continue;

                optionalQuery = optionalQuery + String.format(" REGEXP_LIKE(tags,'.*\\b%s\\b.*') OR", s.trim());
            }
            optionalQuery = optionalQuery.substring(0, optionalQuery.length() - 3) + ")";
        }


        query = query + requiredQuery + optionalQuery;
        log.log(Level.FINE, query);
        //System.out.printf("%s\n", query);

        //execute query, get data
        List<Word> words = new ArrayList<>();
        ResultSet rs = conn.createStatement().executeQuery(query);
        if(rs.first())
        {
            do
            {
                //word_tag, word_eng, constraints, tags
                Word word = new Word();
                word.tagalog = rs.getString("tagalog");
                word.english = rs.getString("english");
                word.tags = rs.getString("tags");
                words.add(word);
            }while(rs.next());
        }

        return words;
    }

}
