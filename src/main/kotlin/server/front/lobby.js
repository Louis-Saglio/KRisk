function addOnClick() {
    const links = document.getElementsByClassName("game-link");
    console.log(links);
    for (let link of links) {
        console.log(link);
        link.addEventListener('click', (ev) => {
            joinGame(
                ev.srcElement.textContent.split(' : ')[0],
                prompt("Choose a user name")
            ).then()
        })
    }
}

async function joinGame(gameCode, playerCode) {
    const result = await fetch(
        `${document.location.origin}/games/${gameCode}/players/`,
        {
            method: 'POST',
            body: JSON.stringify({"code": gameCode, "playerCode": playerCode})
        }
    );
    console.log(result);
    if (result.code === 200) {
        alert("joined " + result.code)
    } else {
        alert("error " + result.code)
    }
}


window.onload = () => {
    addOnClick()
};
