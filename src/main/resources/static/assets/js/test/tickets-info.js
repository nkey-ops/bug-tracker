function ticketsInfo(id, name, quantity, quantityADayAgo) {
    var iconSuccess = '<div class="icon icon-box-success ">\n' +
        '                        <span class="mdi mdi-arrow-top-right icon-item"></span>\n' +
        '                    </div>';
    var iconDanger = '<div class="icon icon-box-danger">\n' +
        '                         <span class="mdi mdi-arrow-bottom-left icon-item"></span>\n' +
        '                    </div>';

    var percentageClass;
    var icon;
    
    if (quantityADayAgo > 0) {
        percentageClass = 'text-success';
        icon = iconSuccess;
        quantityADayAgo = '+'+  quantityADayAgo;
    } else  if (quantityADayAgo === 0) {
        percentageClass = 'text-gray';
        icon = '';
    }
    
    document.getElementById(id).innerHTML =
        ' <div class="card">\n' +
        '        <div class="card-body">\n' +
        '         <h5>My Projects</h5>\n' +
        '            <div class="row">\n' +
        '                <div class="col-9">\n' +
        '                    <div class="d-flex align-items-center align-self-start">\n' +
        '                        <h3 id="quantity" class="mb-0">' + quantity + '</h3>\n' +
        '                        <p id="quantityADayAgo" class="' + percentageClass + ' ml-2 mb-0 font-weight-medium">' + quantityADayAgo + ' today</p>\n' +
        '                    </div>\n' +
        '                </div>\n' +
        '                <div class="col-3">\n' +
        '                    ' + icon + '\n' +
        '                </div>\n' +
        '            </div>\n' +
        '            <h6 id="name" class="text-muted font-weight-normal">'+ name + '</h6>\n' +
        '        </div>\n' +
        '    </div>';
}