var div2 = document.getElementsByClassName("menu");

function handleClick(event) {
    if (event.target.classList[1] === "selected") {
    event.target.classList.remove("selected");
    } else {
    for (var i = 0; i < div2.length; i++) {
    div2[i].classList.remove("selected");
    }

    event.target.classList.add("selected");
    }
}

function init() {
    for (var i = 0; i < div2.length; i++) {
    div2[i].addEventListener("click", handleClick);
    }
}

init();