var app = angular.module("filters", []);

app.filter("counterValue",function(){
  return function(value){
    var count=parseInt(value), suffix="";
    if (count>=1000000){
      count=Math.round(count/1000000);
      suffix="M"
    } else if (count>=1000){
      count=Math.round(count/1000);
      suffix="K"
    }
    return""+count+suffix
  }
});
