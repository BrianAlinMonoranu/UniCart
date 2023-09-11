
function updateLocalPrice(id) {
    var price = document.querySelector("#price" + id);
    var isTrained = document.querySelector("#train" + id);
    var priceNow = parseInt(price.innerHTML, 10);
    var newPrice = priceNow * 1.5;
    price.innerHTML = newPrice;

    console.log(price.innerHTML);
    console.log(isTrained.value);
    updateTotalPrice(priceNow, newPrice);
};

function updateTotalPrice(oldValue, newValue) {
    var totalPrice = document.querySelector("#totalCheckout");
    var itemsPrice = document.querySelector("#sum");
    totalPrice.innerHTML = parseInt(totalPrice.innerHTML, 10) - oldValue + newValue;
    itemsPrice.innerHTML = parseInt(itemsPrice.innerHTML, 10) - oldValue + newValue;
};