function userInfo(id, name, quantity, quantityADayAgo, quantityAMonthAgo, icon ) {
    var percentageClass;
    
    if (quantityADayAgo > 0) {
        percentageClass = 'text-success';
        quantityADayAgo = '+'+  quantityADayAgo;
    } else if (quantityADayAgo === 0) {
        percentageClass = 'text-gray';
    }
    if(quantityAMonthAgo > 0){
        quantityAMonthAgo = '+' + quantityAMonthAgo;
    }

    document.getElementById(id).innerHTML =
        ' <div class="card">\n' +
        '     <div class="card-body">\n' +
        '         <h5>' + name + '</h5>\n' +
        '         <div class="row">\n' +
        '             <div class="col-8 col-sm-12 col-xl-8 my-auto">\n' +
        '                 <div class="d-flex d-sm-block d-md-flex align-items-center">\n' +
        '                     <h2 class="mb-0">' + quantity + '</h2>\n' +
        '                     <p class="'+ percentageClass +' ml-2 mb-0 font-weight-medium">' + quantityADayAgo + ' today</p>\n' +
        '                 </div>\n' +
        '                 <h6 class="text-muted font-weight-normal">'+ quantityAMonthAgo +' Since last month</h6>\n' +
        '             </div>\n' +
        '             <div class="col-4 col-sm-12 col-xl-4 text-center text-xl-right">\n' +
        '                 <i class="icon-lg mdi '+ icon + ' text-primary ml-auto"></i>\n' +
        '             </div>\n' +
        '         </div>\n' +
        '     </div>\n' +
        ' </div>';
}