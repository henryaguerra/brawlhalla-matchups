package henry.smashggbot;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ReadyEvent;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

public class bot implements EventListener {

    public static void main(String[] args)
            throws LoginException, InterruptedException
    {

/*        JDA jda = new JDABuilder("NTIyMjAxMDY3OTMzNTMyMTkw.DvHhjA.EPKlxHRdOk4shFwT6PK2gYg669Y").
                addEventListener().build();

        jda.awaitReady();*/

        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("https://api.smash.gg/gql/alpha");

        System.out.println("BLAH BLAH BLAH");

        // Request parameters and other properties
        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
        params.add(new BasicNameValuePair("query", ""));
        params.add(new BasicNameValuePair("param-2", "Hello!"));

    }

 /*   public void onEvent(Event event){

        if (event instanceof ReadyEvent){
            System.out.println("API is ready!");
        }
    }*/
}