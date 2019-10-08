package com.zkc.commandmcu.Parser;

import com.zkc.commandmcu.DataFormat.Report;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class ReportParser {
    public final String Report = "Report";

    private final String STATUS = "status";
    private final String MESSAGE = "message";


    public ArrayList<Report> parseJSON(JSONObject jsonObject){
        ArrayList<Report> newsArrayList = new ArrayList<Report>();

        try {
            JSONArray jsonArray = jsonObject.getJSONArray("Report");
            for (int index = 0; index < jsonArray.length(); index++){
                JSONObject iJSONObject = jsonArray.getJSONObject(index);

                String status = iJSONObject.getString(STATUS);
                String message = iJSONObject.getString(MESSAGE);


                Report report = new Report(status, message);

                newsArrayList.add(report);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return newsArrayList;
    }
}
