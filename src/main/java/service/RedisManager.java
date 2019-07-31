/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.Range;
import io.lettuce.core.Range.Boundary;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.api.sync.RedisKeyCommands;
import io.lettuce.core.api.sync.RedisSortedSetCommands;
import io.lettuce.core.api.sync.RedisStringCommands;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.OfficeStock;
import model.User;

/**
 * Manager class that handles operations that required access to Redis
 * @author idilhanhan
 */
public class RedisManager {
    
    private RedisClient client;
    
    public RedisManager(){
        client = RedisClient.create("redis://localhost");
    }
    
    /**
     * Returns connection to database 1 of Redis
     * @return connection
     */
    public StatefulRedisConnection getUserConnection(){
        //Create connection
        StatefulRedisConnection<String, String> connection = client.connect();
        //Create command to switch to different database
        RedisCommands command = connection.sync();
        command.select(1); //user info is in database 1
        return connection;
    }
    
    /**
     * Returns connection to database 2 of Redis
     * @return connection
     */
    public StatefulRedisConnection getStockConnection(){
        //Create connection
        StatefulRedisConnection<String, String> connection = client.connect();
        //Create command to switch to different database
        RedisCommands command = connection.sync();
        command.select(2); //stock info is in database 2
        return connection;
    }
    
    /**
     * Returns connection to database 2 of Redis
     * @return connection
     */
    public StatefulRedisConnection getOwnershipConnection(){
        //Create connection
        StatefulRedisConnection<String, String> connection = client.connect();
        //Create command to switch to different database
        RedisCommands command = connection.sync();
        command.select(3); //ownership info is in database 3
        return connection;
    }
    
    /**
     * Gets the User with given id
     * @param userId
     * @return User object 
     */
    public User getUser(long userId){
        
        StatefulRedisConnection<String, String> connection = getUserConnection();
        RedisStringCommands sync = connection.sync();
        
        String strUser = (String) sync.get("user:" + userId);
        
        ObjectMapper mapper = new ObjectMapper();
        User user = null;
        try {
            user = mapper.readValue(strUser, User.class);
        } catch (IOException e) {
            Logger.getLogger(RedisManager.class.getName()).log(Level.SEVERE, null, e);
        }
        
        connection.close();
        
        return user;
    }
    
    /**
     * Gets the id of the user with given name
     * @param username
     * @return user id
     */
    public long getIdbyName(String username){
        
        StatefulRedisConnection<String, String> connection = getUserConnection();
        RedisStringCommands sync = connection.sync();
        RedisSortedSetCommands setSync = connection.sync();
        
        //Find the id of the user with this name?
        // Range range = Range.create(username + ":", "+");
        Object key = username + ":";
        Object endKey = username + ":" + sync.get("idCounter");
        List nameID = setSync.zrangebylex("user.index", Range.from(Boundary.including(key), Boundary.including(endKey)));  //FIX THIS
        //the issue here right now is the nameID list  is empty??
        
        if (nameID == null || nameID.isEmpty()){
            connection.close();
            return 0;
        }
        else{
            String id = ((String)nameID.get(0)).split(":")[1];
            connection.close();
            return Long.parseLong(id);
        }
    }
    
    /**
     * Adds the given user to Redis in JSON object form
     * @param newUser
     * @return true if successful
     */
    public boolean addUser(User newUser){
        
        if (getIdbyName(newUser.getUsername()) == 0){ //username has to be unique
            
            
            StatefulRedisConnection connection = getUserConnection();
            RedisStringCommands sync = connection.sync();
            RedisSortedSetCommands setSync = connection.sync();
            
            //1. Incr id count and get user id
            long id = sync.incr("idCounter");
            newUser.setId(id);
            
            //2. Add the User to redis
            ObjectMapper mapper = new ObjectMapper();
            String userJSON = "not found";
            try{
                userJSON = mapper.writeValueAsString(newUser);
            } catch(JsonProcessingException e){
                Logger.getLogger(StockService.class.getName()).log(Level.SEVERE, null, e);
            }
            
            //3. Store the new user with user:id#
            String key = "user:" + id;
            sync.set(key, userJSON);
            
            //4. Lexicographical indexing
            String newName = newUser.getUsername() + ":" + id;
            setSync.zadd("user.index", 0, newName);
            
            connection.close();
            return true;
        }
        return false;
    }
    
    /**
     * Deletes user with given id
     * @param userId
     * @return true if successful
     */
    public boolean deleteUser(long userId){
        //Get the user to be deleted
        User toDelete = getUser(userId);
        
        if (toDelete != null){
            
            StatefulRedisConnection connection = getUserConnection();
            RedisKeyCommands keySync = connection.sync();
            RedisSortedSetCommands setSync = connection.sync();
            
            //Delete key stock:stockNo
            keySync.del("user:" + userId);
            
            //Delete username:userid from sorted set user.index
            setSync.zrem("user.index", toDelete.getUsername() + ":" + userId);
            
            connection.close();
            return true;
        }
        return false;
    }
    
    /**
     * Gets all of the users of the system
     * @return List of Users written in JSON object form, null unsuccessful
     */
    public List<String> getAllUsers(){ //change
        StatefulRedisConnection<String, String> connection = getUserConnection();
        RedisStringCommands sync = connection.sync();
        RedisKeyCommands keySync = connection.sync();
        
        List<String> userKeys = keySync.keys("user:*");
        
        if (userKeys != null){
            List<String> users = new ArrayList<>();
            ObjectMapper mapper = new ObjectMapper();
            for (String userKey : userKeys){
                String jsonUser = (String)sync.get(userKey);
                users.add(jsonUser);
                /*try {
                users.add(mapper.readValue(jsonUser, User.class));
                } catch (IOException e) {
                Logger.getLogger(RedisManager.class.getName()).log(Level.SEVERE, null, e);
                }*/
            }
            return users;
        }
        return null;
        
    }
    
    /**
     * Adds the token to Redis as key with the given value attached to it
     * @param token, token of the currently logged in user in t
     * @param val, String value that holds the type of the user, admin or user
     */
    public void addToken(String token, String val){ //note that tokens are on the same db as users!
        StatefulRedisConnection<String, String> connection = getUserConnection();
        RedisStringCommands sync = connection.sync();
        //Store all of the tokens for currently logged in users
        sync.set(token, val);
        connection.close();
    }
    
    /**
     * Checks if given token is stored in Redis
     * @param token, token of the user, given as String in URI encoded form
     * @return type of user with given token; admin/user
     */
    public Object checkToken(String token){ //???
        StatefulRedisConnection connection = getUserConnection();
        RedisStringCommands sync = connection.sync();
        String decodedToken = token;
        try {
             decodedToken = URLDecoder.decode(token, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Logger.getLogger(RedisManager.class.getName()).log(Level.SEVERE, null, e);
        }
        String val = (String)sync.get(decodedToken);
        connection.close();
        return val;
    }    
    
    /**
     * Deletes the given token from Redis
     * @param token, token of the user
     */
    public void deleteToken(String token){
        StatefulRedisConnection<String, String> connection = getUserConnection();
        RedisKeyCommands sync = connection.sync();
        String toDel = token;
        try {
             toDel = URLDecoder.decode(token, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Logger.getLogger(RedisManager.class.getName()).log(Level.SEVERE, null, e);
        }
        sync.del(toDel);
        connection.close();
    }
    
    /**
     * Gets the stock with given stockNo
     * @param stockNo
     * @return Office Stock object
     */
    public OfficeStock getStock(String stockNo){
        
        StatefulRedisConnection<String, String> connection = getStockConnection();
        RedisStringCommands sync = connection.sync();
        
        String strStock = (String) sync.get("stock:" + stockNo);
        
        if (strStock != null){
            
            ObjectMapper mapper = new ObjectMapper();
            OfficeStock stock = null;
            try {
                stock = mapper.readValue(strStock, OfficeStock.class);
            } catch (IOException e) {
                Logger.getLogger(RedisManager.class.getName()).log(Level.SEVERE, null, e);
            }
            connection.close();
            
            return stock;
        }
        connection.close();
        return null;
    }
    
    /**
     * Adds given stock to Redis in JSON object form
     * @param newStock
     * @return true if successful
     */
    public boolean addStock(OfficeStock newStock){//param -- necessary info
        
        if(getStock(newStock.getStockNo()) == null){
            
            StatefulRedisConnection connection = getStockConnection();
            RedisStringCommands sync = connection.sync();
            RedisSortedSetCommands setSync = connection.sync();
            
            //1. Parse the obj to JSON
            ObjectMapper mapper = new ObjectMapper();
            String stockJSON = "not found";
            try {
                stockJSON = mapper.writeValueAsString(newStock);
            } catch (JsonProcessingException e) {
                Logger.getLogger(StockService.class.getName()).log(Level.SEVERE, null, e);
            }
            
            //2. add the stock with key stock:id#
            String stockNo = newStock.getStockNo();
            //String key = "stock:" + stockToAdd.getStockNo();
            sync.set("stock:" + stockNo, stockJSON); //ERROR- stockJSON null olarak mı gönderiyor??
            
            //3. Add the stock to ordered set
            setSync.zadd("stock.index", 0, stockNo);
            
            connection.close();
            return true;
        }
        return false;
    }
    
    /**
     * Deletes stock with given stockNo and is owned by the employee with given name
     * @param stockNo
     * @param ownerName, could be null(if the stock is not owned by anyone)
     * @return true if successful
     */
    public boolean deleteStock(String stockNo, String ownerName){
        
        OfficeStock toDelete = getStock(stockNo);
        
        if (toDelete != null){
            
            StatefulRedisConnection connection = getStockConnection();
            RedisKeyCommands keySync = connection.sync();
            RedisSortedSetCommands setSync = connection.sync();
            
            //decode stockNo and owner name?
           /* String decodedNo = stockNo;
            String decodedName = ownerName;

        try {
             decodedNo = URLDecoder.decode(stockNo, "UTF-8");
             decodedName = URLDecoder.decode(ownerName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Logger.getLogger(RedisManager.class.getName()).log(Level.SEVERE, null, e);
        }*/
            
            //Delete key stock:stockNo
            keySync.del("stock:" + stockNo);
            
            //Delete stockNo from sorted set stock.index
            setSync.zrem("stock.index", stockNo);
            
            //Delete ownership -- TODO!!
            //String ownerName = getOwnerOf(stockNo);
            if (ownerName != null){
                deleteOwnership(stockNo, ownerName);
            }
            connection.close();
            return true;
        }
        return false;
        
    }
    
    /**
     * Gets all of the stocks that fit the given prompt
     * @param prompt
     * @return Map with owners and stocks members where stocks keep the stock information and owners keep the owner names linked with stockNos
     */
    public Map getStocks(String prompt){
        
        StatefulRedisConnection connection = getStockConnection();
        RedisStringCommands sync = connection.sync();
        RedisSortedSetCommands setSync = connection.sync();
        
        
        List<String> stockNos = setSync.zrange("stock.index", 0, -1);
        
        Map result = new HashMap(); //the result map will hold a list of stocks(JSON obj)
                                    //and the stockNo:owner map
        
        List<String> stocks = new ArrayList<>(); //JSON stock objects
        Map owners = new HashMap(); //stock:owner
        
        
        for (String no: stockNos){ //FIX THIS
            String jsonStock = (String) sync.get("stock:" + no);
            if (jsonStock != null){
                if (prompt != null){
                    if (jsonStock.contains(prompt)){
                        //1. add the stock to the stocks list
                        stocks.add(jsonStock);
                        //2. using the stockNo get the owner
                        String owner = getOwnerOf(no);
                        //3. Add the owner to owners map with stockNo as the key
                        if (owner != null){
                            owners.put(no, owner);
                        }
                    }
                } else{
                    //1. add the stock to the stocks list
                    stocks.add(jsonStock);
                    //2. using the stockNo get the owner
                    String owner = getOwnerOf(no);
                    //3. Add the owner to owners map with stockNo as the key
                    if (owner != null){
                        owners.put(no, owner);
                    }
                }
            }
        }
        
        connection.close();
        if (!stocks.isEmpty() && !owners.isEmpty()){
            result.put("stocks", stocks);
            result.put("owners", owners);
        }
        return result;
    }
    
    /**
     * Updates the stock with given stockNo with the new description and capacity
     * @param stockNo
     * @param description
     * @param capacity
     * @return true if successful
     */
    public boolean updateStock(String stockNo, String description, String capacity){
        //??
        //using the stockNo get the stock
        OfficeStock toUpdate = getStock(stockNo);
        if (toUpdate != null){   
            //connection
            StatefulRedisConnection connection = getStockConnection();
            RedisStringCommands sync = connection.sync();
            
            //Set the description and capacity
            toUpdate.setDescription(description);
            toUpdate.setCapacity(capacity);
            //parse to json and store it again
            ObjectMapper mapper = new ObjectMapper();
            String jsonUpdate = "";
            try {
                jsonUpdate = mapper.writeValueAsString(toUpdate);
                        } catch (JsonProcessingException e) {
                Logger.getLogger(RedisManager.class.getName()).log(Level.SEVERE, null, e);
            }
            sync.set("stock:" + stockNo, jsonUpdate);
            return true;
        }
        
        return false;
    }
    
    
    /**
     *
     * @param employeeId
     * @return
     */
    /* private List<OfficeStock> getStocksOf(long employeeId){ //???
    
    StatefulRedisConnection connection = getOwnershipConnection();
    RedisKeyCommands keySync = connection.sync();
    
    String pattern = "owner:" + employeeId + ".stock:*";
    List<String> stockNos =  keySync.keys(pattern); //each stockNO is in the form owner:#.stock:#
    
    List<OfficeStock> stocksJSON = new ArrayList<>();
    for( String key : stockNos){
    String no = key.split(":")[3];
    stocksJSON.add(this.getStock(no));
    }
    connection.close();
    return stocksJSON;
    }*/
    
    /**
     * Gets the owner of the stock with given stockNo
     * @param stockNo
     * @return name of the owner
     */
    public String getOwnerOf(String stockNo){
        StatefulRedisConnection connection = getOwnershipConnection();
        RedisKeyCommands keySync = connection.sync();
        
        String pattern = "owner:*" + ".stock:" + stockNo;
        List<String> key = keySync.keys(pattern);
        connection.close();
        
        if (key != null && key.size() > 0){
            
            String owner = key.get(0);
            String[] parts = owner.split("\\.");
            String [] tmp2 = parts[0].split(":");
            
            return key.get(0).split("\\.")[0].split(":")[1];
            
            
        }
        return "";
    }
    
    /**
     * Adds new ownership between stock with given no and employee with given name
     * @param stockNo
     * @param ownerName 
     * @return true if successful
     */
    public boolean addOwnership(String stockNo, String ownerName){
        StatefulRedisConnection connection = getOwnershipConnection();
        RedisKeyCommands keySync = connection.sync();
        RedisStringCommands sync = connection.sync();
        
        // decode the employee name                      
        String employee = ownerName;
        try {
             employee = URLDecoder.decode(ownerName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Logger.getLogger(RedisManager.class.getName()).log(Level.SEVERE, null, e);
        }

        String keyToAdd = "owner:" + employee + ".stock:" + stockNo;         
        
        //first check if this ownership exists     
        //String searchKey = "owner:*."
        List <String> keys = keySync.keys("owner:*.stock:" + stockNo);
        if (keys != null && keys.size() > 0){
            for (String key : keys){
                String owner = key.split("\\.")[0].split(":")[1];
                if (owner.equals(employee)){
                    connection.close();
                    return false;
                }
                else{
                    keySync.del(key); //delete old ownerships??  --- what is the point of capacity??
                }
            }
        }
        //if here then we can add the ownership
        sync.set(keyToAdd, ""); 
        
        connection.close();
        return true;
 
    }
    
    
    /**
     * Deletes the ownership between stock with given no and and employee with given name
     * @param stockNo
     * @param ownerName
     * @return true if successful
     */
    public boolean deleteOwnership(String stockNo, String ownerName){//?????
        StatefulRedisConnection connection = getOwnershipConnection();
        RedisKeyCommands keySync = connection.sync();
        
        String keyToDelete = "owner:" + ownerName + ".stock:" + stockNo;
        long check = keySync.del(keyToDelete);
        connection.close();
        return (check != 0);
    }
    
}
