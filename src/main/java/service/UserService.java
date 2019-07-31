/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import model.User;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

/**
 *
 * @author idilhanhan
 */
@Path("/user")//??
@Api(value = "/user")
public class UserService {
    
    Random random = new Random();
    RedisManager rm = new RedisManager(); //burda mı olmalı?
    
    @POST
    @Path("/session")
    @Produces(MediaType.APPLICATION_JSON) 
    @ApiOperation(value="Logs user in to the system")
    public Response login(@FormParam("username") String username, 
                            @FormParam("password") String password){
        
        long id = rm.getIdbyName(username); 
        User user = rm.getUser(id);
            String storedPass = user.getPassword();
            if (authenticate(storedPass, password)){
                String token = createToken();
                String val = "user";
                if (user.isAdmin()){
                    val = "admin";
                }
                rm.addToken(token, val);
                return Response.ok().entity(token).build(); //return success message, with id as path param??
            }
    return Response.ok().build(); //error message??   
    }
   
    
    private String createToken(){
        byte[] buffer = new byte[27]; //27 -- bc. divisible by 3
        random.nextBytes(buffer);
        return Base64.getEncoder().encodeToString(buffer);
    }
    
    /**
     * Method that checks if the given passwords are the same
     * @param storedPass
     * @param givenPass
     * @return true if the given passwords are the same
     */
    private boolean authenticate(String storedPass, String givenPass){
        try {
            int iterations = 1000;
            int keyLength = 128;
            String[] parts = storedPass.split(":");
            byte[] salt = Hex.decodeHex(parts[0].toCharArray());
            byte[] hash = Hex.decodeHex(parts[1].toCharArray());
            
            PBEKeySpec spec = new PBEKeySpec(givenPass.toCharArray(), salt, iterations, keyLength);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] testHash = skf.generateSecret(spec).getEncoded();
            
            return equal(hash, testHash);
        }catch (NoSuchAlgorithmException | InvalidKeySpecException | DecoderException e){
            System.out.println(e.getMessage());
        }
        return false;
    }
    
    /**
     * Method that checks if two byte arrays are the same
     * @param original
     * @param check
     * @return true if the given arrays are the same
     */
    private boolean equal(byte[] original, byte[] check){
        int diff = original.length ^ check.length;
        for (int i = 0; i < original.length && i < check.length; i++)
            diff |= original[i] ^ check[i];
        return diff == 0;
    }
    
    
    @DELETE
    @Path("/session")
    @ApiOperation(value="Logs user out of the system")
    public Response logout(@QueryParam("token")String token){
        if (rm.checkToken(token) != null){
            rm.deleteToken(token);
            return Response.noContent().build();
        }
        return Response.status(Status.INTERNAL_SERVER_ERROR).build(); //???
    }
    
    @GET
    @Path("/employees")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value="Gets the name of all of the employees")
    public Response getEmployees(@QueryParam("token") String token){
        
        if (rm.checkToken(token) == null){ //If no token return
            
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        
        List<String> employees = new ArrayList<>();
        employees.add("DenizYüksel");
        employees.add("IremHanhan");
        employees.add("KeremYılmaz");
        return Response.ok(employees).build();
    }
    
}
