const prompt = require('prompt-sync')();

//Date exercise
let day = prompt ("What day were you born? [number] ");
let month = prompt ("What month were you born? [number] ") - 1;
let year = prompt ("What year were you born? [number] ");
let date = new Date();
let output = (date.getFullYear()-year)*365 + (date.getMonth()-month)*30 + date.getDate()-day
console.log(output)

//Prime number exercise
let input = prompt("Insert your number: ")
console.log(2)
for (let i = 3; i <= input; i++){
  let isPrime = true;
  for(let j = 2; j < i; j++){
    if(i%j == 0){
      isPrime = false;
      break;
    }
  }
  if(isPrime){
    console.log(i);
  }
}

//Refactor of exercise 2
let input2 = prompt("Insert your number: ")
displayPrime(input2)
function isPrime(n){
    for(let j = 2; j < Math.sqrt(n); j++){
      if((n%j == 0 && n!=2) || n==1){
        return false;
      }
    }
    return true
  }
function displayPrime(n){
  console.log(2);
  for(i = 3; i <= n; i++){
    if(isPrime(i)){
      console.log(i);
    }
  }
}

//Exercise 4
function AddressBook(name, address, civic, zipCode) {
  this.name = name;
  this.address = address;
  this.civic = civic;
  this.zipCode = zipCode;
  this.changeAddress = function(newAddress){
    console.log("Old Address: " + this.address);
    this.address = newAddress;
    console.log("New Address: " + this.address);
  }
};

let user = new AddressBook(prompt("Nome: "), prompt("Address: "), prompt("Civic: "), prompt("Zip Code: "));
user.showInfo();
user.changeAddress("Milano");

//Exercise 5
let input5 = parseInt(prompt("Insert temp: "));
let temps=[];
while(input5 != -99){
  temps.push(input5);
  input5 =  parseInt(prompt("Insert temp: "));
}
let sum=0;
let max=Number.MIN_VALUE;
let min=Number.MAX_VALUE;
for(let key in temps){
  if(temps[key] >= max ){
    max = temps[key];
  }
  if(temps[key] <= min){
    min = temps[key];
  }
  sum+=temps[key];
}
console.log("Average temp was: " + sum/temps.length);
console.log("Max temp was: " + max);
console.log("Min temp was: " + min);

//Exercise 6
let ObjectCostructor = function(input){
  this.k = Math.floor(Math.random() * (input - 2) ) + 2;
  this.array = [];
  
  for(let i = 2; i < this.k; i++){
    if(isPrime(i)){
      this.array.push(i);
    }
  }
  
  this.showK = function(){
    console.log(this.k);
  }
  
  this.showArray = function(){
    for(let key in this.array){
      console.log(this.array[key]);
    }
  }
};

function isPrime(n){
  for(let j = 2; j < Math.sqrt(n); j++){
    if((n%j == 0 && n!=2) || n==1){
      return false;
    }
  }
  return true
}

let arrayOfObj = [];
let input6 = prompt("Enter a number or 'stop': ");
while(input6!="stop"){
  arrayOfObj.push(new ObjectCostructor(input))
  input = prompt("Enter a number or 'stop': ");
}

for(key in arrayOfObj){
  arrayOfObj[key].showK();
  console.log("--------------")
  arrayOfObj[key].showArray();
}