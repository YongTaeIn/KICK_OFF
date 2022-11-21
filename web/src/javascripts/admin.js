"use strict";

const loginId = document.getElementById('LOGIN_ID');
const loginPw = document.getElementById('LOGIN_PW');
const loginBtn = document.getElementById('LOGIN_BTN');

function color() {
    if(loginId.value.length>0 && loginPw.value.length>=5){
        loginBtn.style.backgroundColor = "#ffd53b";
        loginBtn.disabled = false;
    }
    else{
        loginBtn.style.backgroundColor = "#e9ce6d";
        loginBtn.disabled = true;
    }
}

function moveToMain(){
    if (loginId.value == "kickoffadmin") {
        if (loginPw.value == "aisl1234!") {
            alert('You are logged in with your administrator ID.')
            location.replace("./main.html");
        }
        else {
            alert('Please check your ID and password again.')
            errStack ++;
        }
    }
    else {
        alert('This account does not exist.')
    }
    if (errStack >= 5) {
        alert("You've got your password wrong more than five times. I recommend you find your password.")
    }
}

loginId.addEventListener('keyup', color);
loginPw.addEventListener('keyup', color);
loginBtn.addEventListener('click',moveToMain);
