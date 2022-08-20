/*global $, console*/


$(function () {
    "use strict";

    var maxText = $("textarea").attr("maxlength"),

        ourMessage = $(".message");

    ourMessage.html('<span>' + maxText + '</span> Characters Remaining');

    $("textarea").keydown(function () {

        var textLength = $(this).val().length,

            remText = maxText - textLength;

        ourMessage.html('<span>' + remText + '</span> Characters Remaining');

    });


    $("textarea").keyup(function () {

        var textLength = $(this).val().length,

            remText = maxText - textLength;

        ourMessage.html('<span>' + remText + '</span> Characters Remaining');

    });

});