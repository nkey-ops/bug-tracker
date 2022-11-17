function getLink(links, name){
    for (const link of links) {
        if (link['rel'] === name) {
            return  link['href'];
        }
    }
    alert("Link " + name + " wasn't found");
    return null;
}