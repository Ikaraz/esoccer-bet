package com.example.Bet365Odds.bet365Services;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MatchOdds implements Runnable{

    private String id;
    private String homeTeam;
    private String awayTeam;

    public MatchOdds(String id, String homeTeam, String awayTeam) {
        this.id = id;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
    }

    @Override
    public void run() {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL("https://api.b365api.com/v3/bet365/prematch?token=73664-ke1U5IScdIK2Ld&FI=" + id).openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestProperty("Content-Type", "application/json: charset=UTF-8");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            InputStream is = conn.getInputStream();
            String jsonResult = streamReader(is);

            List<String> fullTimeOdds = fullTimeOdds(jsonResult);
            List<String> overUnderOdds = goalsOverUnder(jsonResult);

            int id = Integer.parseInt(this.id);
            double odd1 = Double.parseDouble(fullTimeOdds.get(0));
            double oddX = Double.parseDouble(fullTimeOdds.get(1));
            double odd2 = Double.parseDouble(fullTimeOdds.get(2));
            double oddUnder = Double.parseDouble(overUnderOdds.get(0));
            double oddOver = Double.parseDouble(overUnderOdds.get(1));

            //TODO:proveri da li je sinhronizovan i prosledi podatke klijentu
            MatchModel.addMatches(new MatchModel(id, homeTeam, awayTeam, odd1, oddX, odd2, oddUnder, oddOver));

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> goalsOverUnder(String jsonString){
        List<String> list = new ArrayList<>();
        JSONObject obj = new JSONObject(jsonString);
        JSONArray results = obj.getJSONArray("results").getJSONObject(0).getJSONObject("main").getJSONObject("sp").getJSONObject("goal_line").getJSONArray("odds");
        JSONObject oddOver = results.getJSONObject(0);
        JSONObject oddUnder = results.getJSONObject(1);
        list.add(oddOver.getString("name"));
        list.add(oddUnder.getString("name"));
        return list;
    }

    private JSONArray doubleChanceOdds(String jsonString){
        JSONObject obj = new JSONObject(jsonString);
        JSONArray results = obj.getJSONArray("results").getJSONObject(0).getJSONObject("main").getJSONObject("sp").getJSONObject("double_chance").getJSONArray("odds");
        JSONObject odd1X = results.getJSONObject(0);
        JSONObject oddX2 = results.getJSONObject(1);
        JSONObject odd12 = results.getJSONObject(2);
        System.out.println(odd1X.getString("name") + " " + odd1X.getString("odds"));
        System.out.println(oddX2.getString("name") + " " + oddX2.getString("odds"));
        System.out.println(odd12.getString("name") + " " + odd12.getString("odds"));

        return results;
    }

    private List<String> fullTimeOdds(String jsonString){
        List<String> list = new ArrayList<>();
        JSONObject obj = new JSONObject(jsonString);
        JSONArray results = obj.getJSONArray("results").getJSONObject(0).getJSONObject("main").getJSONObject("sp").getJSONObject("full_time_result").getJSONArray("odds");
        JSONObject odd1 = results.getJSONObject(0);
        JSONObject oddX = results.getJSONObject(1);
        JSONObject odd2 = results.getJSONObject(2);
        list.add(odd1.getString("odds"));
        list.add(oddX.getString("odds"));
        list.add(odd2.getString("odds"));
        return list;
    }

    private String streamReader(InputStream stream){
        final int bufferSize = 1024;
        final char[] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();
        InputStreamReader in = new InputStreamReader(stream, StandardCharsets.UTF_8);
        int charsRead;
        try{
            while((charsRead = in.read(buffer, 0, buffer.length)) > 0) {
                out.append(buffer, 0, charsRead);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return out.toString();
    }
}