package henry.smashggbot;


import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;


import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.*;
import java.util.EventListener;
import java.util.Scanner;

import org.json.*;


public class bot {


    public static void main(String[] args)
            throws LoginException, InterruptedException
    {

        JDA jda = new JDABuilder("NDI3OTExNjUyMzQwNTMxMjAw.DvZbTA.FyrIgFr4G5fxQvmGlRdE9Ba7O0E").
                addEventListener(new MessageListener()).build();

        jda.awaitReady();


        // Post request

        /* {"query":"query TournamentsByVideogame($perPage: Int, $videogameId: Int)
            {\n  tournaments(query: {\n    perPage: $perPage\n    page: 1\n
            sortBy: "startAt asc"\n    filter: {\n      past: false\n
            videogameIds: [\n        $videogameId\n      ]\n    }\n  })
            {\n    nodes {\n      id\n      name\n      slug\n    }\n  }\n}",
            "variables":{"perPage":3,"videogameId":287},
            "operationName":"TournamentsByVideogame"}
         */

        //postRequest(275577, 263975);

    }







}

class MessageListener extends ListenerAdapter{
    private String prefix = ">";

    @Override
    public void onMessageReceived(MessageReceivedEvent event){

        JDA jda = event.getJDA();
        int player1 = 0, player2 = 0;
        int foundID = 0;
        String path = "C:\\Users\\Henry\\Documents\\GitHub\\smashgg-bot\\" +
                        "src\\main\\java\\henry\\smashggbot\\players.txt";

        boolean longf = false;

        //Get message string and channel
        Message message = event.getMessage();
        String messageString = message.getContentDisplay();
        MessageChannel channel = event.getChannel();

        // Break if message is from bot to avoid infinite messages
        if(message.getAuthor().isBot()) return;

        // Search for 1s matchups
        if(messageString.toLowerCase().contains(prefix + "1smatchup") ||
                messageString.toLowerCase().contains(prefix + "2smatchup")){

            String longSubstring = messageString.substring(10, 11).toLowerCase();
            // Check for long format in command
            if(longSubstring.equals("l")){
                longf = true;
            }

            // Used for parsing ones or twos
            boolean ones = true;

            // Split command, 2nd argument contains first name, 3rd contains second name
            String args[] = messageString.split(" ");

            // Error checking for wrong amount of args
            if (args.length != 3){
                channel.sendMessage("You need to specify which 2 players to check. Please try \n\n" +
                        "`>1smatchup [player1] [player2]` \n\nwith no spaces between names .If this still does not work," +
                        " message me on discord: Mother Russia#3907").queue();
                return;
            }

            // Save string for embed usage
            String player1name = args[0];
            String player2name = args[1];

            // Loop through players.txt and check for string containing playername
            try {
                File file = new File(path);
                Scanner scanner = new Scanner(file);
                while (scanner.hasNextLine()){
                    String lineFromFile = scanner.nextLine();

                    // If first player specified is found, get ID from file and increment foundID
                    // and check if we found 2 players already
                    if(lineFromFile.toLowerCase().contains(args[1].toLowerCase())){
                        String nameInfo[] = lineFromFile.split("\\t");
                        player1 = Integer.parseInt(nameInfo[0]);
                        foundID++;

                        // Check if found both IDs
                        if(foundID == 2){
                            break;
                        }
                    }
                    // If first player specified is found, get ID from file and increment foundID
                    // and check if we found 2 players already
                    else if (lineFromFile.toLowerCase().contains(args[2].toLowerCase())){
                        String nameInfo[] = lineFromFile.split("\\t");
                        player2 = Integer.parseInt(nameInfo[0]);
                        foundID++;

                        // Check if both IDs were found
                        if(foundID == 2){
                            break;
                        }
                    }
                }
            } catch (FileNotFoundException e){
                System.err.println("Error: " + e);
                return;
            }

            // Specify the correct parsing to use, ones as true indicates checking for no slashes
            // in displayScore
            if(messageString.toLowerCase().contains(prefix + "1smatchup")) {
                postRequest(player1, player2, channel, true, longf);
            }

            // Specify the correct parsing to use, ones as false indicates checking for slashes
            // in displayScore
            else{
                postRequest(player1, player2, channel, false, longf);
            }
        }

        // help message
        else if (message.toString().toLowerCase().contains(prefix + "help")){
            EmbedBuilder embed = new EmbedBuilder();
            embed.setAuthor("Commands for Brawlhalla Matchup Bot");
            embed.addField("Usage: >[command]   [player1]   [player2]", "No spaces in names", false);
            embed.addField("Commands", "`2smatchup`" +
                    " \n`1smatchup` \n`Add 'L' at the end of command alias to show more tournament info`",
                    false);
            embed.setColor(Color.green);
            channel.sendMessage(embed.build()).queue();
        }

        // Embed testing
        else if(message.toString().contains(prefix + "testembed")) {
            // Test discord embed
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("Matchups for player");
            embed.setColor(Color.blue);
            embed.setDescription("ithrow sucks");
            embed.addField("Title of field", "test of field", true);
            embed.setAuthor("Matchups bot");
            channel.sendMessage(embed.build()).queue();
        }

    }

    private static void postRequest(int player1, int player2, MessageChannel channel, boolean ones, boolean longf){

        // START OF POST REQUEST
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("https://api.smash.gg/gql/alpha");

        String json = String.format("{\"query\":\"query setsByPlayer($playerId: Int!, $oppPlayerId: Int!) {\\n  player(id: $playerId)" +
                " {\\n    id\\n    gamerTag\\n    recentSets(opponentId: $oppPlayerId){\\n      id\\n      " +
                "phaseGroupId\\n      event{\\n        name\\n        tournament{\\n          name\\n        }\\n      " +
                "}\\n      displayScore\\n    }\\n  }\\n}\",\"variables\":{\"playerId\":%d,\"oppPlayerId\":%d}," +
                "\"operationName\":\"setsByPlayer\"}", player1, player2);

        // Set entity to json string we created and try for unsupported coding exception
        try {
            StringEntity entity = new StringEntity(json);
            httpPost.setEntity(entity);
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Authorization", "Bearer 0343f613f034d647e9ddc29b24287323");

            // Execute post request and catch io exception
            try {
                CloseableHttpResponse response = client.execute(httpPost);
                System.out.println(entity.getContent());


                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                StringBuffer content = new StringBuffer();
                String line = "";
                while ((line = rd.readLine()) != null) {
                    content.append(line);
                }

                System.out.println(content);
                parseMatchups(content, channel, ones, longf);

                client.close();
            }
            catch (IOException e){
                System.err.println("Error: " + e.toString());
            }
        }
        catch (UnsupportedEncodingException e){
            System.err.println("Error: " + e.toString());
        }
    }

    private static void parseMatchups(StringBuffer s, MessageChannel channel, boolean ones, boolean longf){

        JSONObject object = new JSONObject(s.toString());
        JSONArray sets = null;

        // Catch a null player or set value, then send an error message
        try {
            sets = object.getJSONObject("data").getJSONObject("player").getJSONArray("recentSets");
        }
        catch (Exception e){
            System.err.println("Error: " + e.toString());
            channel.sendMessage("A set between these two players does not exist. " +
                    "If you think this is an error, message me on discord: Mother Russia #3907").queue();
            return;
        }

        // Iterate through set JSON object and display correct score
        for (int i = 0; i < sets.length(); i++){

            // If ones then do ones matchups
            if(ones) {

                // Skip DQs
                if((sets.getJSONObject(i).getString("displayScore")).equals("DQ") ||
                        sets.getJSONObject(i).getString("displayScore").equals("Bye")){
                    continue;
                }

                // Print all 2s scores
                else if (!(sets.getJSONObject(i).getString("displayScore")).contains("/")) {
                    System.out.println(sets.getJSONObject(i).getString("displayScore"));
                    StringBuilder messageToSend = new StringBuilder(sets.getJSONObject(i).
                                                        getString("displayScore"));

                    // Append the tournament name
                    if(longf){
                        messageToSend.append(" - " + sets.getJSONObject(i).getJSONObject("event").
                                                getJSONObject("tournament").getString("name"));
                    }
                    channel.sendMessage(messageToSend).queue();
                }
            }

            // If ones is false then do twos parse
            else{
                if (((String) sets.getJSONObject(i).getString("displayScore")).contains("/")) {
                    System.out.println(sets.getJSONObject(i).getString("displayScore"));
                    channel.sendMessage(sets.getJSONObject(i).getString("displayScore")).queue();
                }
            }
        }
    }
}