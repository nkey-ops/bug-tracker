function update(requestLink, updateId){
    $.ajax({
        type: "GET",
        url: requestLink,
        success: function (data) {
            $('#' + updateId).html($(data).html());
        },
        error: function (){
            alert("Request to " + requestLink + " failed." )
        }
    });
}