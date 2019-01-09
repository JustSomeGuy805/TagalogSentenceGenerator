package com.renegarcia.tagphrgen;
import net.dongliu.requests.RawResponse;
import net.dongliu.requests.Requests;
import net.dongliu.requests.Session;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.dongliu.commons.Strings;



public class TagalogPhraseGenerator
{
    private final List<Sentence> sentenceList;
    private final static Pattern pattern = Pattern.compile("\\[[A-Z_,:]+(:[A-Z_,:]+)?\\]");
    final static Logger log = Logger.getLogger(TagalogPhraseGenerator.class.getName());
    
    
    public TagalogPhraseGenerator() throws Exception
    {
        log.setLevel(Level.FINE);
        DatabaseUtil.initConnection();
      
        sentenceList = DatabaseUtil.getAllSentences();
        List<Word> wordList = DatabaseUtil.getAllWords();

        
        for(Sentence s : sentenceList)
            log.log(Level.FINE, s.toString());
    
        for(Word w : wordList)
            log.log(Level.FINE, w.toString());
    }

    public List<Sentence> getSentenceList()
    {
        return sentenceList;
    }


    private boolean replace(Sentence sentence, String regex, String replacement)
    {
        Matcher matcher = Pattern.compile(regex).matcher(sentence.english);
        if(matcher.find())
        {
            int idx = matcher.start();
            String newStr = matcher.replaceAll(replacement);
            if(idx == 0)
                newStr = newStr.substring(0,1).toUpperCase() + newStr.substring(1);

            sentence.english = newStr;
            return true;
        }
        return false;
    }




    private void fixGrammar(Sentence sentence)
    {

        if(replace(sentence, "[Dd]o he want", "does he want"))
            return;

        if(replace(sentence, "[Hh]e want", "he wants"))
            return;

        if(replace(sentence, "[Dd]o he have", "does he have"))
            return;

        if(replace(sentence, "[Dd]o he", "does he"))
            return;

        if(replace(sentence, "[Hh]e have", "he has"))
            return;

        replace(sentence, "[Hh]e don't", "he doesn't");
        


    }

    
    public Sentence makeRandomSentence() throws Exception
    {
        int val = new Random().nextInt(sentenceList.size());
        Sentence sentence = sentenceList.get(val);
        return makeSentenceFor(sentence);
    }

    
    
    
    
    public Sentence makeSentenceFor(Sentence sentence) throws Exception
    {       
        return makeSentenceFor(sentence, false);
    }

    
    


    public Sentence makeSentenceFor(Sentence sentence, boolean decorated) throws Exception
    {       
        log.info(sentence.toString());
        //System.out.printf("%s\n", sentence);
        
        Matcher m = pattern.matcher(sentence.tagalog);
        String tagalog = sentence.tagalog;
        String english = sentence.english;

        Word word;
        int idx = 1;
        while( m.find() )
        {
            //parse requirements and optionals from sentence template
            int idxOfWord = m.start();
            String match = m.group();
            String key = match.substring(1, match.length() - 1);

            log.log(Level.FINE, "key = {0}", key);
            
            
            List<Word> words = getSuitableWords(key);
            
            int size = words.size();

            log.log(Level.INFO, "size of list = {0}", size);
            //System.out.printf("size of list = is %d\n", size);


            if(size > 0)
            {
                word = words.get(new Random().nextInt(words.size()));

                String wordTagalog = word.tagalog;
                String wordEnglish = word.english;

                if (idxOfWord == 0)
                    wordTagalog = Strings.capitalize(wordTagalog);
                //wordTagalog = wordTagalog.substring(0, 1).toUpperCase() + wordTagalog.substring(1);   

                idxOfWord = english.indexOf("$" + idx);
                if (idxOfWord == 0)
                    wordEnglish = Strings.capitalize(wordEnglish);
                //wordEnglish = wordEnglish.substring(0, 1).toUpperCase() + wordEnglish.substring(1);


                if(decorated)
                    wordTagalog = "[" + wordTagalog + "]";
                    


                tagalog = tagalog.replace(match, wordTagalog);
                english = english.replace("$" + idx, wordEnglish);
            }

            idx++;

        }

        Sentence s = new Sentence();
        s.english = english;
        s.tagalog = tagalog;
        s.category = sentence.category;
        fixGrammar(s);
        return s;
    }


    private List<Word> getSuitableWords(String key) throws Exception
    {
         String requirements = "";
        String optionals = "";

        if (!key.contains(":"))
            requirements = key;
        else
        {
            requirements = key.split(":")[0];
            optionals = key.split(":")[1];
        }

        log.log(Level.INFO, "requirements = {0}", requirements);
        //System.out.printf("requirements = %s\n", requirements);

        log.log(Level.INFO, "optionals = {0}", optionals);
        //System.out.printf("optionals = %s\n", optionals);

        //query
        List<Word> words = DatabaseUtil.query(requirements, optionals);
        return words;
    }


    public InputStream textToAudio(String word) throws Exception
    {
        Session session = Requests.session();
        session.get("https://translate.google.com/").send();
        String url = "https://translate.google.com/translate_tts";

        Map<String, Object> params = new HashMap<>();
        params.put("ie", "UTF-8");
        params.put("client", "tw-ob");
        params.put("q", word);
        params.put("tl", "tl");
        params.put("total", 1);
        params.put("idx", 0);
        params.put("textlen", word.length());

        RawResponse rr2 = session.get(url).params(params).send();



        InputStream is = rr2.body();
        byte buffer[] = new byte[2048];
        int read = -1;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        while((read = is.read(buffer)) > -1)
            bos.write(buffer,0,read);

        bos.close();
        byte[] data = bos.toByteArray();


        return new ByteArrayInputStream(data);
    }



    
    private static String upperCaseFirst(String value) {

        // Convert String to char array.
        char[] array = value.toCharArray();
        // Modify first element in array.
        array[0] = Character.toUpperCase(array[0]);
        // Return string.
        return new String(array);
    }


}
