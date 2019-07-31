/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

function login(){
    event.preventDefault();
    var username = document.getElementById("username").value;
    var password = document.getElementById("password").value;
            
            
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
        if (this.readyState == 4 && this.status >= 200 && this.status < 400) {
            // Typical action to be performed when the document is ready:
            //Get the token and store it in localStorage
            if (xhttp.responseText != ""){
                var token = xhttp.responseText;
                window.localStorage.setItem("token", encodeURIComponent(token));
                window.location.href = "officeStock.html";
            }
            else{
                getAlert("danger", "Username or password is wrong!");
            }
            //document.getElementById("welcome").visibility = "hidden"; //hide the welcome 'page'
            //document.getElementById("navbar").visibility = "visible"; //make the home page and navbar visible
            //document.getElementById("home").visibility = "visible"; 
        }
    };
    xhttp.open("POST", "/officeStock/api/user/session"); 
    xhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    xhttp.send("username=" + encodeURIComponent(username) + "&password=" + encodeURIComponent(password));
}     

function logout(){
    event.preventDefault();
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
        if (this.readyState == 4 && this.status >= 200 &&this.status < 400) {
            //window.localStorage.clear();
            debugger;
            window.location.href = "login.html";
            //window.location.href = "/officeStock/api/user/assignStock.html";
        }
    };
    xhttp.open("DELETE", "/officeStock/api/user/session" + "?token=" + encodeURIComponent(window.localStorage.getItem("token"))); 
    xhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    xhttp.send();
}

function getAssignStock(){
    // event.preventDeafult(); //??
    hidePages();
    //document.getElementById("home").style
    document.getElementById("assignStock").style.visibility = "visible";
    printEmployee();
    
}

function printEmployee(){ 
    event.preventDefault();
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
        if (this.readyState == 4 && this.status >= 200 && this.status < 400) {           
            var employeeList = JSON.parse(xhttp.responseText);                    
            for (var i in employeeList){
                document.getElementById("selectEmployee").innerHTML += "<option>" + employeeList[i] + "</option>";  
            }
        }
    };
    xhttp.open("GET", "/officeStock/api/user/employees" + "?token=" + encodeURIComponent(window.localStorage.getItem("token"))); //CHECK
    xhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    xhttp.send();
}
            
function assignStock(){ //update this to get the value from the input file!!
    event.preventDefault();
    var xhttp = new XMLHttpRequest();
    var employeeNo = document.getElementById("selectEmployee").value;
    var barcode = document.getElementById("assign_barcode").files[0]; 
    var stockNo = document.getElementById("editBarcode").value;
    var reader = new FileReader();      
    //var data = new FormData();
    //data.append("employeeNo", encodeURIComponent(employeeNo));
                
    xhttp.onreadystatechange = function() {
        if (this.readyState == 4 && this.status >= 200 && this.status < 400) {
            // Typical action to be performed when the document is ready:
            //window.location.href = "/officeStock/api/user/home";
            if (xhttp.responseText != ""){
                window.location.href = "officeStock.html";
            }
            else{
                getAlert("danger", "Stock cannot be assigned!");
            }            
        }
    };
    
    if (barcode === undefined){ //if here than the file was not uploaded
        xhttp.open("POST", "/officeStock/api/stock/owner");
        xhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        //document.getElementById("test").innerHTML = encodeURI(event.target.result.split(",")[1]);
        xhttp.send("token=" + encodeURIComponent(window.localStorage.getItem("token")) + "&barcodeBase64=" + null  +"&barcode=" + encodeURIComponent(stockNo) + "&employee=" + encodeURIComponent(employeeNo)); 
    }

    reader.addEventListener("load", function(){ //if file does not exist this function is not called
        var barcodeBase64 = event.target.result.split(",")[1];
        //data.append("barcodeBase64", encodeURIComponent(barcodeBase64));
        xhttp.open("POST", "/officeStock/api/stock/owner");
        xhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        //document.getElementById("test").innerHTML = encodeURI(event.target.result.split(",")[1]);
        xhttp.send("token=" + encodeURIComponent(window.localStorage.getItem("token")) + "&barcodeBase64=" + encodeURIComponent(barcodeBase64)  +"&barcode=" + encodeURIComponent(stockNo) + "&employee=" + encodeURIComponent(employeeNo)); 
        //xhttp.send(data);
    }, false);
                
                               
    if (barcode){
        reader.readAsDataURL(barcode);
    }
}

function hidePages(){
    var toClose = document.getElementsByClassName("page");
    for ( close of toClose){
        close.style.visibility = "hidden";
    }
}

function getAddStock(){
    hidePages();
    document.getElementById("addStock").style.visibility = "visible";
}

function addStock(){
    event.preventDefault();
    var xhttp = new XMLHttpRequest();
    var descp = document.getElementById("description").value;
    var capacity = document.getElementById("capacity").value;
    var barcode = document.getElementById("add_barcode").files[0];
    var reader = new FileReader();
    
    xhttp.onreadystatechange = function() {
        if (this.readyState == 4 && this.status >= 200 && this.status < 400) {
            // Typical action to be performed when the document is ready:
            //window.location.href = "/officeStock/api/user/home";
            window.location.href = "officeStock.html";
                       
        }
    };
    
    reader.addEventListener("load", function(){
        var barcodeBase64 = event.target.result.split(",")[1];
        //data.append("barcodeBase64", encodeURIComponent(barcodeBase64));
        xhttp.open("POST", "/officeStock/api/stock/new");
        xhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        //document.getElementById("test").innerHTML = encodeURI(event.target.result.split(",")[1]);
        xhttp.send("token=" + encodeURIComponent(window.localStorage.getItem("token")) + 
                "&description=" + encodeURIComponent(descp) + 
                "&capacity=" + encodeURIComponent(capacity) + 
                "&barcodeBase64=" + encodeURIComponent(barcodeBase64)); 
        //xhttp.send(data);
    }, false);
                
                               
    if (barcode){
        reader.readAsDataURL(barcode);
    }
}

function getCreateUser(){
    hidePages();
    document.getElementById("createUser").style.visibility = "visible";
}

function createUser(){
    event.preventDefault();
    var username = document.getElementById("username").value;
    var password = document.getElementById("password").value;

    var admin = document.getElementById("admin").checked;
    //password
    // var shaObj = new jsSHA("SHA-256", "TEXT");
    //shaObj.update(document.getElementById("password").value);
    //var hashpass = shaObj.getHash("HEX"); //??
                
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
        if (this.readyState == 4 && this.status >= 200 && this.status < 400) {
            // Typical action to be performed when the document is ready:
            //document.getElementById("message").innerHTML = xhttp.responseText; 
            window.location.href = "officeStock.html";
        }
    };
    xhttp.open("POST", "/officeStock/api/admin/user/new");
    xhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    xhttp.send("token=" + encodeURIComponent(window.localStorage.getItem("token")) +
            "&username=" + encodeURIComponent(username)  +  
            "&password=" + encodeURIComponent(password) +  "&admin=" + admin);
}

function getAllUsers(){
    hidePages();
    document.getElementById("allUsers").style.visibility = "visible";
    allUsers();
}

function allUsers(){
    event.preventDefault(); //stops the default action of an element from happening
    var xhttp = new XMLHttpRequest();
    
    xhttp.onreadystatechange = function() {
        if (this.readyState == 4 && this.status >= 200 && this.status < 400) {
            //here the response text contains the list of users
            //get each of them in a loop
            //document.getElementbyId("userCards").innerHTML
            if (xhttp.responseText != ""){
                var userList = JSON.parse(xhttp.responseText);
                var html = "";
                for ( user of userList){
                    var user = JSON.parse(user);
                    html += '<div class=col-md-4>&nbsp;<div class="card mb-4" style="width: 18rem;">&nbsp;<div class="card-body">&nbsp;<ul class="list-group list-group-flush">&nbsp;';
                    html += '<li class="list-group-item">' + user.username + '</li>';
                    if (user.admin){
                        html += '<li class="list-group-item">Admin</li>' ;
                    }
                    html+= '</ul></div><div class="card-body">&nbsp;<input type=hidden id="' + user.id + '" value="' + user.id + '"><button type="button" class="btn btn-danger" onClick="deleteUser(' + user.id + ');">Delete</button></div>&nbsp;</div></div>';
                }
                document.getElementById("userCards").innerHTML = html;
            }
            else{
                getAlert("danger","Not Authorised!");
            }
         
            //document.getElementById("test").innerHTML = userList;
          
            //after you get them you need to put the information on a card! 
            //the button on the card should include a javascript function for deleting the user?
          
        }  
    };
    
    xhttp.open("GET", "/officeStock/api/admin/users?token=" + encodeURIComponent(window.localStorage.getItem("token")));
    xhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    xhttp.send(); 
}

function deleteUser(userId){
    event.preventDefault();
    var id = document.getElementById(userId).value; //??

    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
        if (this.readyState == 4 && this.status >= 200 && this.status < 400) {
            // Typical action to be performed when the document is ready:
            //document.getElementById("message").innerHTML = xhttp.responseText; 
            window.location.href = "officeStock.html";
        }
    };
    xhttp.open("DELETE", "/officeStock/api/admin/user" + "?token=" + encodeURIComponent(window.localStorage.getItem("token")) + "&userId=" + encodeURIComponent(userId));
    xhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    xhttp.send();
    
}

function getFindStocks(){
    hidePages();
    document.getElementById("findStocks").style.visibility = "visible";
}

function getStocks(){
    event.preventDefault();
    var prompt = document.getElementById("prompt").value
    
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
        if (this.readyState == 4 && this.status >= 200 && this.status < 400) {
            // Typical action to be performed when the document is ready:
            //here print the stocks in the response text to this page/div
            if (xhttp.responseText != ""){ ///CHANGE THUS
                var result = JSON.parse(xhttp.responseText);
                var stocks = result.stocks;//this is working!
                var owners = result.owners;
                var html = "";
                for ( var jsonStock of stocks){
                    var stock = JSON.parse(jsonStock)
                    var stockNo = stock["stockNo"];
                    var ownerName = owners[stockNo];
                    html += '<div class=col-md-4>&nbsp;<div class="card mb-4" style="width: 18rem;overflow-y:auto;">&nbsp;<div class="card-body"><ul class="list-group list-group-flush">&nbsp;';
                    html += '<li class="list-group-item">' + stockNo + '</li>';
                    html += '<li class="list-group-item">Description: ' + stock.description + '</li>';
                    html += '<li class="list-group-item">Capacity: ' + stock.capacity + '</li>';
                    html += '<li class="list-group-item">Owner: ' + ownerName + '</li>';
                    html += '</ul></div>&nbsp;<div class="card-body">&nbsp;<input type=hidden id="' + stockNo + '" value="' + stockNo + '"><input type=hidden id="' + stockNo + '.' + ownerName +'" value="' + ownerName + '">';
                    html += '<button type="button" class="btn btn-danger" onClick="deleteStock(\'' +   stockNo + '\',\''  + ownerName + '\');">Delete</button>';
                    html += '<button type="button" class="btn btn-primary" onClick="getUpdateStock(\''+ stockNo +'\');">Update</button></div>&nbsp;</div></div>';
                }
            
                document.getElementById("stockCards").innerHTML = html;
            }
            else{
               getAlert("danger", "Stock Not Found!");
            }
            
            
        }
    };
    
    xhttp.open("GET", "/officeStock/api/stocks" + "?token=" + encodeURIComponent(window.localStorage.getItem("token")) + "&prompt=" + encodeURIComponent(prompt));
    xhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    xhttp.send();
}

function deleteStock(stockNo, owner){
    
    event.preventDefault();
    
    var stock = document.getElementById(stockNo).value;
    var ownerName = null;
    if (ownerName != ''){
        ownerName = document.getElementById( stockNo +"." + owner).value;
    }
    
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
        if (this.readyState == 4 && this.status >= 200 && this.status < 400) {
        }
    };
        
    xhttp.open("DELETE", "/officeStock/api/stock" + "?token=" + encodeURIComponent(window.localStorage.getItem("token"))
            + "&stockNo=" + encodeURIComponent(stock) + "&ownerName=" + encodeURIComponent(ownerName));
    xhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    xhttp.send();
}

function getStock(){
    event.preventDefault();
    //ok, in here you will get the barcode, send it to the service
    var barcode = document.getElementById("getBarcode").files[0];
    var reader = new FileReader();
    var xhttp = new XMLHttpRequest();
    //the server will read the barcode and will get the stock with the given stockNO
    //inside onreadystatechange -- we will just print this on a card that will have buttons for 
    //delete and update like the normal cards
    
    xhttp.onreadystatechange = function() {
        if (this.readyState == 4 && this.status >= 200 && this.status < 400) {
            // Typical action to be performed when the document is ready:
            var result = JSON.parse(xhttp.responseText);
            var stock = result.stock;//this is working!
            var owner = result.owner;
            if (stock != null){
                var html = "";
                html += '<div class="card" style="width: 18rem;position:fixed;top:160px;">&nbsp;<div class="card-body">&nbsp;<ul class="list-group list-group-flush">&nbsp;';
                html += '<li class="list-group-item">' + stock.stockNo + '</li>';
                html += '<li class="list-group-item">Description: ' + stock.description + '</li>';
                html += '<li class="list-group-item">Capacity: ' + stock.capacity + '</li>';
                html += '<li class="list-group-item">Owner: ' + owner + '</li>';
                html += '</ul>&nbsp; <div class="card-body">&nbsp;<input type=hidden id="' + stock.stockNo + '" value="' + stock.stockNo + '"><input type=hidden id="' + stock.stockNo + '.' + owner +'" value="' + owner + '">';
                html += '<button type="button" class="btn btn-danger" onClick="deleteStock(\'' +   stock.stockNo + '\',\''  + owner + '\');">Delete</button>';
                html += '<button type="button" class="btn btn-primary" onClick="getUpdateStock(\'' + stock.stockNo + '\');">Update</button>&nbsp</div>&nbsp;</div>&nbsp;';
            
                document.getElementById("stockCards").innerHTML = html;
            }
                       
        }
    };
    
    reader.addEventListener("load", function(){
        var barcodeBase64 = event.target.result.split(",")[1];
        //data.append("barcodeBase64", encodeURIComponent(barcodeBase64));
        xhttp.open("GET", "/officeStock/api/stock" + "?token=" + encodeURIComponent(window.localStorage.getItem("token")) + "&barcodeBase64=" + encodeURIComponent(barcodeBase64));
        xhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        xhttp.send(); 
    }, false);
    
    
    if(barcode){
        reader.readAsDataURL(barcode);
    }
}

function getUpdateStock(stockNo){ 
    var html = '<form>&nbsp;<div class="form-group"><label for="upDescription">Description</label><input type="text" class="form-control" id="upDescription" placeholder="description" required>&nbsp;</div>';
    html += '<div class="form-group"><label for="upCapacity">Capacity</label><input type="text" class="form-control" id="upCapacity" placeholder="1" required></div>';
    html += '<button type="submit" class="btn btn-primary" onClick="updateStock(\''+ stockNo +'\');">Update</button><button class="btn btn-secondary" type="submit" onClick="closeUpdate();">Cancel</button></form>';
    document.getElementById("updateStock").innerHTML = html;
    document.getElementById("updateStock").style.visibility = "visible";
}

function updateStock(stockNo){
    //ok, in here i will get the info about description and capacity and then call put stock
    event.preventDefault();
    //var stockNo = document.getElementById(stock).value;
    var descp = document.getElementById("upDescription").value;
    var capacity = document.getElementById("upCapacity").value;
    var xhttp = new XMLHttpRequest();
    
    
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
        if (this.readyState == 4 && this.status >= 200 && this.status < 400) {
            //hide the form
            document.getElementById("updateStock").style.visibility = "hidden";
            //add an alert message
            getAlert("success", "Stock information updated!")
            getStocks();
        }
    };
        
    xhttp.open("POST", "/officeStock/api/stock");
    xhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    xhttp.send("token=" + encodeURIComponent(window.localStorage.getItem("token"))
            + "&stockNo=" + encodeURIComponent(stockNo) +"&description=" + encodeURIComponent(descp) + "&capacity=" + encodeURIComponent(capacity));
   
}

function checkToken(){
    if (window.localStorage.length == 0){
        window.location.href = "login.html";
    }
}

function getHome(){
    hidePages();
    document.getElementById("home").style.visibility = "visible";
}
      
function getAlert(type, msg){ //the close may not work??
    document.getElementById("alertDiv").innerHTML = '<div class="alert alert-' + type + '" role="alert">' + msg + '  <button type="button" class="close" data-dismiss="alert" aria-label="Close" onClick="closeAlert();"><span aria-hidden="true">&times;</span></button></div>';
    document.getElementById("alertDiv").style.visibility = "visible";
}      

function closeUpdate(){
    document.getElementById("updateStock").style.visibility = "hidden";
    getStocks(); 
}

function closeAlert(){
    document.getElementById("alertDiv").style.visibility = "hidden";
}

function readBarcode(){
    event.preventDefault();
    var xhttp = new XMLHttpRequest();
    var barcode = document.getElementById("assign_barcode").files[0]; 
    var reader = new FileReader();    
                
    xhttp.onreadystatechange = function() {
        if (this.readyState == 4 && this.status >= 200 && this.status < 400) {
            // Typical action to be performed when the document is ready:
            //window.location.href = "/officeStock/api/user/home";
            if (xhttp.responseText != ""){
                //in here print the read value to the input place
                var barcodeValue = xhttp.responseText;
                document.getElementById("editBarcode").value = barcodeValue;
            }  
            else{
                getAlert("danger", "Barcode can not be read!");
            }
        }
    };

    reader.addEventListener("load", function(){
        var barcodeBase64 = event.target.result.split(",")[1];
        //data.append("barcodeBase64", encodeURIComponent(barcodeBase64));
        xhttp.open("GET", "/officeStock/api/stock/barcode" + "?token=" + encodeURIComponent(window.localStorage.getItem("token")) + "&barcodeBase64=" + encodeURIComponent(barcodeBase64));
        xhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        //document.getElementById("test").innerHTML = encodeURI(event.target.result.split(",")[1]);
        xhttp.send(); 
        //xhttp.send(data);
    }, false);
                
                               
    if (barcode){
        reader.readAsDataURL(barcode);
    }
}
