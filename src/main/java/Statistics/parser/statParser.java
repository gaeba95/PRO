/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Statistics.parser;

import Statistics.handler.StatisticsHandler;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import com.google.gson.*;
import java.util.ArrayList;

import Statistics.components.Image;
import Statistics.components.Month;
import Statistics.components.Tag;

/**
 *
 * @author Edd993Surface
 */
public class statParser {
 
    private static final String HISTORIC = "history.json";
    private final StatisticsHandler statHandler;
    
    public statParser(StatisticsHandler statHandler) {
        this.statHandler = statHandler;
    }
     
     
    public JsonParser parseFile() {
        JsonParser jsonParser = new JsonParser();
        try (FileReader reader = new FileReader(HISTORIC)) {

            Object obj = jsonParser.parse(reader);

            JsonObject images = (JsonObject) obj;           
            JsonArray content = (JsonArray) images.get("content");
            parseImageContent(content);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return jsonParser;
    }

    private void parseImageContent(JsonArray content) {

        for (Object image : content) {
            JsonObject jimage = (JsonObject) image;
            
            String path = (String) jimage.get("path").getAsString();
            JsonArray tags = (JsonArray) jimage.get("tags");
            
           ArrayList<Tag> tagsStructure = parseContentTags(tags);         
           Image imageStructure = new Image(path, tagsStructure);
           
           statHandler.countCameraObservation(imageStructure.getCamera(), tagsStructure.size());
           statHandler.countDaysObservation(imageStructure.getDate(), tagsStructure.size());
           statHandler.countSequenceObservation(imageStructure.getSequence(), tagsStructure.size());
           statHandler.countMonthlyObservation(imageStructure.getMonth(), tagsStructure.size());
           statHandler.countDailyObservation(imageStructure.getMonth(), imageStructure.getDay(), tagsStructure.size());
           statHandler.countHourlyObservation(imageStructure.getMonth(), imageStructure.getDay(), imageStructure.getHour(), tagsStructure.size());

           statHandler.addNbAnimals(tagsStructure.size());
           
           for (Tag tag : tagsStructure){   
                     
               statHandler.countMonthlyObservationsByAnimalType(tag.getAnimalType(), imageStructure.getMonth());
               statHandler.countDailyObservationsByAnimalType(tag.getAnimalType(), imageStructure.getMonth(), imageStructure.getDay());
               statHandler.countHourlyObservationsByAnimalType(tag.getAnimalType(), imageStructure.getMonth(),imageStructure.getDay(), imageStructure.getHour());
               statHandler.countTotalObservationsByAnimalType(tag.getAnimalType());
           }
           statHandler.addImage(imageStructure);
        }
        
    }

    private ArrayList<Tag> parseContentTags(JsonArray tags) {
        
        ArrayList<Tag> result = new ArrayList<>();
        for (Object tag : tags) {
            JsonArray jtag = (JsonArray) tag;
            Tag tagStructure = new Tag();
            
            tagStructure.setTypeAnimal(jtag.get(0).getAsString());
            tagStructure.setSize(jtag.get(1).getAsDouble());
            tagStructure.setIsMale(jtag.get(2).getAsBoolean());
            tagStructure.setIsEnteringTunnel(jtag.get(3).getAsBoolean());

            result.add(tagStructure);
        }
        return result;
    }
    
}