/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.codec.binary.Hex;
import model.User;

/**
 *
 * @author idilhanhan
 */

@Path("/admin")
@Api(value = "/admin")
public class AdminService {
    
    RedisManager rm = new RedisManager();
    
    @POST
    @Path("/user/new")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value="Creates a new user")
    public Response createUser(@FormParam("token")String token,
                               @FormParam("username") String username,
                               @FormParam("password") String password, 
                               @FormParam("admin") String admin){
        
        if (rm.checkToken(token).equals("admin")){
        
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setAdmin(admin.equals("true"));
            newUser.setPassword(this.hash(password));

            rm.addUser(newUser);

            return Response.ok().build();
        }
        return null;
    }
    
    /**
     * Method that hashes the given password
     * @param pass
     * @return Hashed password
     */
    private String hash(String pass){
        try {
            int iterations = 1000;
            int keyLength = 128;
            char[] chars = pass.toCharArray();
            //create salt
            SecureRandom sr = new SecureRandom();
            byte[] salt = new byte[16];
            sr.nextBytes(salt);

            PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, keyLength);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            String hashedString = Hex.encodeHexString(hash);
            String saltHex = Hex.encodeHexString(salt);
            return saltHex + ":" + hashedString;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e){
            Logger.getLogger(StockService.class.getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    
    @DELETE
    @Path("/user")
    @ApiOperation(value="Deletes user by id")
    public Response deleteUser(@QueryParam("token")String token, @QueryParam("userId") String userId){
        
        String check = (String)rm.checkToken(token);
        if (check != null && check.equals("admin")){
            
            rm.deleteUser(Long.parseLong(userId));
            return Response.ok().build();
            
        }
        return Response.noContent().build(); //bu response??
        
    }
    
    
    @GET
    @Path("/users")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value="Gets all of the users of the system")
    public Response getAllUsers(@QueryParam("token") String token){
        
        if (rm.checkToken(token) != null && rm.checkToken(token).equals("admin")){ //null kısmını farklı yerlere de koyman gerekebilir
            
            List<String> users = rm.getAllUsers();
            return Response.ok(users).build(); 
            
        }
        return Response.ok().build();//Bu sorun olabilir?
        
    }
}
