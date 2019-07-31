/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package service;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import model.OfficeStock;

//TODO -- ADD JAVADOC

/**
 *
 * @author idilhanhan
 */

@Path("/")
@Api(value="/") //??
public class StockService { //admin-user token da tutunucak
    
    
    Random random = new Random();
    RedisManager rm = new RedisManager(); 
    
    
    @POST
    @Path("/stock/new")
    //@Produces(MediaType.APPLICATION_JSON) //RESTEasy will use Jackson provider to handle the JSON conversion automatically!!!
    @ApiOperation(value="Adds a new stock to the inventoy")//?
    public Response addStock(@FormParam("token")String token,
            @FormParam("description") String description,
            @FormParam("capacity") String capacity,
            @FormParam("barcodeBase64") String barcodeBase64){
        
        if (rm.checkToken(token) == null){ //If no token return
            
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        
        OfficeStock newStock = new OfficeStock();
        newStock.setDescription(description);
        newStock.setCapacity(capacity);
        newStock.setBarcodeBase64(barcodeBase64);
        String stockNo = readBarcode(barcodeBase64);
        newStock.setStockNo(stockNo);
        
        rm.addStock(newStock);
        
        return Response.ok().build(); //post should return created???
    }
    
    
    /**
     *
     * @param barcodeBase64
     * @return
     */
    private String readBarcode(String barcodeBase64){
        byte[] decoded = Base64.getDecoder().decode(barcodeBase64);
        try{
            BufferedImage bufferedImg = ImageIO.read(new ByteArrayInputStream(decoded));
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(
                    new BufferedImageLuminanceSource(bufferedImg)));
            //Get the barcode reader
            Reader barcodeReader = new MultiFormatReader();
            Result result = barcodeReader.decode(bitmap);
            return result.getText();
        } catch(IOException | NotFoundException | ChecksumException | FormatException e){
            Logger.getLogger(StockService.class.getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }
    
    @DELETE
    @Path("/stock") //question about the path can multiple services have the same path???
    @ApiOperation(value="Deletes a stock")
    public Response deleteStock(@QueryParam("token")String token,
                                @QueryParam("stockNo") String stockNo,
                                @QueryParam("ownerName") String ownerName){
        if (rm.checkToken(token) == null){ //If no token return
            
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        rm.deleteStock(stockNo, ownerName);
        return Response.noContent().build(); //upon success delete should return no content??
    }
    
    //TODO -- user barcode u okuttuğunda stock bilgilerini döndürür
    @GET//post?
    @Path("/stock")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value="Shows information about a single stock")
    public Response getStock(@QueryParam("token")String token,
            @QueryParam("barcodeBase64") String barcodeBase64){ ///?
        
        if (rm.checkToken(token) == null){ //If no token return
            
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        Map result = new HashMap();
        
        String stockNo = readBarcode(barcodeBase64);
        OfficeStock stock = rm.getStock(stockNo);
        String owner = rm.getOwnerOf(stockNo);
        result.put("stock", stock);
        result.put("owner", owner);
        return Response.ok(result).build(); 
    }
    

    @POST
    @Path("/stock/owner") //??
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value="Creates an ownership between a stock and an employee")
    public Response assignStock(@FormParam("token")String token,
            @FormParam("barcodeBase64") String barcodeBase64,
            @FormParam("employee") String employee){
        
        if (rm.checkToken(token) == null){ //If no token return
            
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        
        
        String stockNo = readBarcode(barcodeBase64);
        
        
        if (rm.getStock(stockNo) == null){ //if barcode is read for the first time => create new stock
            OfficeStock newStock = new OfficeStock();
            newStock.setStockNo(stockNo);
            newStock.setBarcodeBase64(barcodeBase64);
            rm.addStock(newStock);
        }
        
        if (rm.addOwnership(stockNo, employee)){
            return Response.ok("Successful!").build();
        }
        else{
            return Response.ok().build();
        }
        
        

    }
    
    
    
    @GET
    @Path("/stocks")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value="Finds stocks that answer to given prompt")//???
    public Response getStocks(@QueryParam("token")String token, @QueryParam("prompt") String prompt) {//btün service ler token olmalı, her kullanıcı için ayrı;
        
        if (rm.checkToken(token) == null){ //If no token return
            
            return Response.ok().build();
        }
        Map result = rm.getStocks(prompt);
        
        if (result.isEmpty()){
            return Response.ok().build(); 
        }
        else{
        return Response.ok().entity(result).build();
        }//"stocks" -- for json stock objects
                                            //"owners" -- for stockNo:owner
        
    }
    
    
    @POST
    @Path("/stock")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value="Updates the existing information about a stock")
    public Response updateStock(@FormParam("token") String token,
                                @FormParam("stockNo") String stockNo,
                                @FormParam("description") String description,
                                @FormParam("capacity") String capacity){
        
        if (rm.checkToken(token) == null){
            return Response.ok().build();
        }
        rm.updateStock(stockNo, description, capacity);
        return null;
        
    }
    
    
}
