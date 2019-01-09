package com.renegarcia.tagphrgen;
import net.dongliu.requests.RawResponse;
import net.dongliu.requests.Requests;
import net.dongliu.requests.Session;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javazoom.jl.player.Player;

public class Main
{
    private static final Logger log = Logger.getLogger(Main.class.getName());

    
    static {
      System.setProperty("java.util.logging.SimpleFormatter.format",
              "[%1$tF %1$tT] [%4$-7s] %5$s %n");
  
  }
    
    public static void generateSentences() throws Exception
    {
        TagalogPhraseGenerator gen = new TagalogPhraseGenerator();

        List<String> generated = new ArrayList<>();
        for(int i=0; i < 40; i++)
        {
            Sentence s  = gen.makeRandomSentence();
            String sen = String.format("%s\n%s\n\n", s.tagalog, s.english);
            System.out.print(sen);
            generated.add(sen);
        }

        System.out.print("--------------------------------\n\n");
        for(String s: generated)
        {
            System.out.printf("%s\n", s);
        }
    }




    public static void downloadTest()
    {
        Session session = Requests.session();
        RawResponse rr1 = session.get("https://translate.google.com/").send();

        String url = "https://translate.google.com/translate_tts";

        Map<String, Object> params = new HashMap<>();
        params.put("ie", "UTF-8");
        params.put("client", "tw-ob");
        params.put("q", "pera");
        params.put("tl", "tl");
        params.put("total", 1);
        params.put("idx", 0);
        params.put("textlen", 4);

        RawResponse rr2 = session.get(url).params(params).send();
        try(FileOutputStream fos = new FileOutputStream("word.mp3"))
        {
            InputStream inputStream = rr2.body();
            byte[] buffer = new byte[2048];
            int read = -1;

            while (  (read = inputStream.read(buffer)) > -1  )
            {
                fos.write(buffer,0,read);
            }
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }

//        url = "https://translate.google.com/translate_tts"
//
//        r1 = session.get(url, params={'ie':'UTF-8', 'client': 'tw-ob', 'q':'pera', 'tl':'tl', 'total': 1, 'idx':0, 'textlen' : 4})
//        print_info(url, r1)
//        print(r1.headers)
//
//        file = open("test.mp3", "wb")
//        for chunk in r1.iter_content(chunk_size=128):
//        file.write(chunk)
//        file.close()
    }



    public static void pronounce(String word)  throws Exception
    {
        InputStream is = new TagalogPhraseGenerator().textToAudio(word);

        Player p = new Player(is);
        p.play();
    }

    
    
    public static void main(String args[])
    {
        try
        {
            //generateSentences();
            //downloadTest();
            // pronounce("kotse");
            
            
            /* Set look and feel */
            //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
            try
            {
                javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
            }
            catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException e)
            {
                //  log.log(Level.SEVERE, e.getMessage(), e);
            }
            //</editor-fold>
            
            
            
            SwingGUI gui = new SwingGUI();
            gui.setVisible(true);      
        }
        catch (Exception ex)
        {
           log.log(Level.SEVERE, "Failed to initiate JFrame", ex);
           JOptionPane.showMessageDialog(null, ex.getMessage(), "Fatal Error", JOptionPane.ERROR_MESSAGE);
           System.exit(1);
        }
        //</editor-fold>
                
            


    }
}
