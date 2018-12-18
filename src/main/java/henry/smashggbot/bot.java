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
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.json.*;


public class bot {

    public static void main(String[] args)
            throws LoginException, InterruptedException
    {

        JDA jda = new JDABuilder("NDI3OTExNjUyMzQwNTMxMjAw.DvZbTA.FyrIgFr4G5fxQvmGlRdE9Ba7O0E")
                .addEventListener(new MessageListener()).build();

        jda.awaitReady();
    }
}

/* Listens for message */
class MessageListener extends ListenerAdapter{
    private String prefix = ">";

    @Override
    public void onMessageReceived(MessageReceivedEvent event){

        // Integers used for player IDs and foundID used for knowing when 2 IDs have been foound in file
        int player1 = 0, player2 = 0;
        int foundID = 0;

        // Path to player file
        // TODO: Read path from file
        String path = "C:\\Users\\Henry\\Documents\\GitHub\\smashgg-bot\\" +
                        "src\\main\\java\\henry\\smashggbot\\players.txt";

        //Get message string and channel
        Message message = event.getMessage();
        String messageString = message.getContentDisplay();
        MessageChannel channel = event.getChannel();

        // Break if message is from bot to avoid infinite messages
        if(message.getAuthor().isBot()) return;

        // Split command, 2nd argument contains first name, 3rd contains second name
        String args[] = messageString.split(" ");

        // Search for 1s matchups
        if(messageString.toLowerCase().contains(prefix + "matches1") ||
                messageString.toLowerCase().contains(prefix + "matches2")){

            // Error checking for wrong amount of args
            if (args.length != 3){
                channel.sendMessage("You need to specify which 2 players to check. Please try \n\n" +
                        "`>matches1 [player1] [player2]` \n\nwith no spaces between names ." +
                        "\n\nIf this still does not work, message me on discord: Mother Russia#3907").queue();
                return;
            }

            // Save string for embed usage
            String player1name = null;
            String player2name = null;

            // Loop through players.txt and check for string containing playername
            try {

                // Scan file for player IDs
                File file = new File(path);
                Scanner scanner = new Scanner(file);
                while (scanner.hasNextLine()){
                    String lineFromFile = scanner.nextLine();

                    // If first player specified is found, get ID from file and increment foundID
                    // and check if we found 2 players already
                    if(lineFromFile.toLowerCase().contains(args[1].toLowerCase())){
                        String nameInfo[] = lineFromFile.split("\\t");
                        player1 = Integer.parseInt(nameInfo[0]);
                        player1name = nameInfo[1];
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
                        player2name = nameInfo[1];
                        foundID++;

                        // Check if both IDs were found
                        if(foundID == 2){
                            break;
                        }
                    }
                }
            }

            // Return if file is not found
            catch (FileNotFoundException e){
                System.err.println("Error: " + e);
                System.err.println("Error: " + e);
                System.err.println("Error: " + e);
                System.err.println("Error: " + e);
                return;
            }

            // Specify the correct parsing to use, ones as true indicates checking for no slashes
            // in displayScore
            if(messageString.toLowerCase().contains(prefix + "matches1")) {
                postRequest(player1, player2, channel, true, player1name, player2name);
            }

            // Specify the correct parsing to use, ones as false indicates checking for slashes
            // in displayScore
            else{
                postRequest(player1, player2, channel, false, player1name, player2name);
            }
        }

        // help message
        else if (message.toString().toLowerCase().contains(prefix + "help")){
            EmbedBuilder embed = new EmbedBuilder();
            embed.setAuthor("Commands for Brawlhalla Matchup Bot");
            embed.addField("Usage: >[command]   [player1]   [player2]", "No spaces in names", false);
            embed.addField("Commands", "`matches2`" +
                    " \n`matches1` \n`Add 'L' at the end of command alias to show more tournament info`",
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

    private static void postRequest(int player1, int player2, MessageChannel channel, boolean ones,
                                    String player1name, String player2name){

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
                parseMatches(content, channel, ones, player1name, player2name);

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

    private static void parseMatches(StringBuffer s, MessageChannel channel, boolean ones,
                                      String player1name, String player2name){

        // Initialize JSON object and declare JSON array that holds sets
        JSONObject object = new JSONObject(s.toString());
        JSONArray sets;

        // Catch a null player or set value, then send an error message
        try {
            sets = object.getJSONObject("data").getJSONObject("player").getJSONArray("recentSets");
        }
        catch (Exception e){
            System.err.println("Error: " + e.toString());
            channel.sendMessage("A set between these two players does not exist. " +
                    "If you think this is an error, message me on discord: Mother Russia #3907 with" +
                    "the 2 player's names").queue();
            return;
        }

        // Initialize object required for embed and set the title and color
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor("Head to Head Matchups For " + player1name + " and " + player2name);
        embed.setColor(Color.red);

        // Stores sets won by player1 and player2
        int setsWonBy1 = 0, setsWonBy2 = 0;

        // Iterate through set JSON object and display correct score
        for (int i = 0, j = 1; i < sets.length(); i++){

            // If ones then do ones matchups
            if(ones) {

                // Skip DQs and Byes
                if((sets.getJSONObject(i).getString("displayScore")).equals("DQ") ||
                        sets.getJSONObject(i).getString("displayScore").equals("Bye")){
                    continue;
                }

                // Print all 2s scores
                else if (!(sets.getJSONObject(i).getString("displayScore")).contains("/")) {
                    System.out.println(sets.getJSONObject(i).getString("displayScore"));
                    String messageToSend = sets.getJSONObject(i).getString("displayScore");

                    // Get win or loss
                    int winner = winner(messageToSend, player1name, player2name);

                    // Increment the number of sets won by the player
                    if(winner == 1){
                        setsWonBy1++;
                    }
                    else if (winner == 2){
                        setsWonBy2++;
                    }
                    else {
                        System.err.println("Error: Could not get winner of match");
                    }

                    //channel.sendMessage(messageToSend).queue();
                    embed.addField(sets.getJSONObject(i).getJSONObject("event").
                            getJSONObject("tournament").getString("name"), messageToSend, false);
                    j++;
                }
            }

            // If ones is false then do twos parse
            else{
                if ((sets.getJSONObject(i).getString("displayScore")).contains("/")) {
                    System.out.println(sets.getJSONObject(i).getString("displayScore"));

                    String messageToSend = sets.getJSONObject(i).getString("displayScore");

                    // Get win or loss
                    int winner = winner(messageToSend, player1name, player2name);

                    // Increment the number of sets won by the player
                    if(winner == 1){
                        setsWonBy1++;
                    }
                    else if (winner == 2){
                        setsWonBy2++;
                    }
                    else {
                        System.err.println("Error: Could not get winner of match");
                    }
                    //channel.sendMessage(messageToSend).queue();
                    embed.addField(sets.getJSONObject(i).getJSONObject("event").
                            getJSONObject("tournament").getString("name"), messageToSend, false);
                    j++;
                }
            }
        }

        StringBuilder fieldTitle = new StringBuilder("Total Set Count For " + player1name + " and " + player2name);

        String fieldDescription = player1name + " " + setsWonBy1 + " - " +
                player2name + " " + setsWonBy2;

        // Append correct game type to field description
        if(ones){
            fieldTitle.append(" in 1s");
        }
        else{
            fieldTitle.append(" in 2s");
        }


        embed.addField(fieldTitle.toString(),fieldDescription, false);

        // Queue up embed message
        channel.sendMessage(embed.build()).completeAfter(1, TimeUnit.SECONDS);
    }

    /* Returns an int representing if player1 or player2 won the match, returns -1 if it could not
    *  find a winner */
    private static int winner(String set, String player1, String player2) {

        String split[] = set.split("-");
        int set1 = 0, set2 = 0;
        int winner;
        boolean setHasNumbers = false;

        //char leftStringChars[] = split[0].toCharArray();
        //char rightStringChars[] = split[1].toCharArray();

        // Check if last character is digit, if not then check 2nd to last digit
        // This will get the games won for the left side
        if (Character.isDigit(split[0].charAt(split[0].length() - 1))){
            set1 = split[0].charAt(split[0].length() - 1);
            setHasNumbers = true;
        } else if (Character.isDigit(split[0].charAt(split[0].length() - 2))) {
            set1 = split[0].charAt(split[0].length() - 2);
            setHasNumbers = true;
        } else if (split[0].charAt(split[0].length() - 1) == 'W') {
            winner = 1;
            setHasNumbers = false;
        }

        // Check if last character is digit, if not then check 2nd to last digit
        // This will get the games won for the right side
        if (Character.isDigit(split[1].charAt(split[1].length() - 1))) {
            set2 = split[1].charAt(split[1].length() - 1);
            setHasNumbers = true;
        } else if (Character.isDigit(split[1].charAt(split[1].length() - 2))) {
            set2 = split[1].charAt(split[1].length() - 2);
            setHasNumbers = true;
        } else if (split[1].charAt(split[1].length() - 1) == 'W') {
            winner = 2;
            setHasNumbers = false;
        }

        // If set has numbers then
        if (setHasNumbers) {

            // set the winner int value
            if (set1 > set2) {
                winner = 1;
            } else {
                winner = 2;
            }

            // Check if winner side of string contains player1, if it does set as winner and return
            if(split[winner - 1].toLowerCase().contains(player1.toLowerCase())){
                /* if winner = 1, it checks the 0th index string, and checks if
                * it has player1 string */
                return 1;
            }

            // Check if winner side contains player2, if it does set as winner and return
            else if (split[winner - 1].toLowerCase().contains(player2.toLowerCase())){
                return 2;
            }

            // If the string of player1 or 2 is not in winner string, check if one is in the other
            else if (winner == 1){

                if(split[1].contains(player2)){
                    // Player 1 wins if player2 is in loser string
                    return 1;
                }
                else if(split[1].contains(player1)){
                    // Player2 wins if player1 is in loser string
                    return 2;
                }
                // Couldn't find winner
                else {
                    return -1;
                }

            }

            else if (winner == 2){

                if(split[0].contains(player2)){
                    // Player 1 wins if player2 is in loser string
                    return 1;
                }
                else if(split[0].contains(player1)){
                    // Player2 wins if player1 is in loser string
                    return 2;
                }
                // Couldn't find winner
                else {
                    return -1;
                }

            }
        }
        return -1;
    }
}