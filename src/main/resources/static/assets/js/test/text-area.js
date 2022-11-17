function textArea(textAreaId, messageId) {
    "use strict";


    var maxText = $('#' + textAreaId).attr("maxlength"),

        ourMessage = $('#' + messageId);

        var remText = maxText - $('#' + textAreaId).val().length ;

        ourMessage.html('<span>' + remText + '</span> Characters Remaining');

    $('#' + textAreaId)
        .keydown(function () {

            var textLength = $(this).val().length,

                remText = maxText - textLength;

            ourMessage.html('<span>' + remText + '</span> Characters Remaining');

        })
        .keyup(function () {

            var textLength = $(this).val().length,

                remText = maxText - textLength;

            ourMessage.html('<span>' + remText + '</span> Characters Remaining');

        });

}