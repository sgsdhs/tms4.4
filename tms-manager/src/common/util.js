/**
 * @description 工具函数库
 * @author wuruiqi
 * @createtime 2017-12-06
 */

var toString = Object.prototype.toString,
  hasOwn = Object.prototype.hasOwnProperty,
  push = Array.prototype.push,
  slice = Array.prototype.slice,
  trim = String.prototype.trim,
  indexOf = Array.prototype.indexOf;

// [[Class]] -> type pairs
var class2type = {};

function isFunction( obj ) {
  return type(obj) === "function";
}

function isArray( obj ) {
  return type(obj) === "array";
}

function isWindow( obj ) {
  return obj != null && obj == obj.window;
}

function isNumeric( obj ) {
  return !isNaN( parseFloat(obj) ) && isFinite( obj );
}

function type( obj ) {
  return obj == null ?
    String( obj ) :
    class2type[ toString.call(obj) ] || "object";
}

function isPlainObject( obj ) {
  // Must be an Object.
  // Because of IE, we also have to check the presence of the constructor property.
  // Make sure that DOM nodes and window objects don't pass through, as well
  if ( !obj || type(obj) !== "object" || isWindow( obj ) ) {
    return false;
  }

  try {
    // Not own constructor property must be Object
    if ( obj.constructor &&
      !hasOwn.call(obj, "constructor") &&
      !hasOwn.call(obj.constructor.prototype, "isPrototypeOf") ) {
      return false;
    }
  } catch ( e ) {
    // IE8,9 Will throw exceptions on certain host objects #9897
    return false;
  }

  // Own properties are enumerated firstly, so to speed up,
  // if last one is own, then all properties are own.

  var key;
  for ( key in obj ) {}

  return key === undefined || hasOwn.call( obj, key );
}

function isEmptyObject( obj ) {
  for ( var name in obj ) {
    return false;
  }
  return true;
}

function error( msg ) {
  throw new Error( msg );
}

function parseJSON( data ) {
  if ( typeof data !== "string" || !data ) {
    return null;
  }

  // Make sure leading/trailing whitespace is removed (IE can't handle it)
  data = jQuery.trim( data );

  // Attempt to parse using the native JSON parser first
  if ( window.JSON && window.JSON.parse ) {
    return window.JSON.parse( data );
  }

  // Make sure the incoming data is actual JSON
  // Logic borrowed from http://json.org/json2.js
  if ( rvalidchars.test( data.replace( rvalidescape, "@" )
      .replace( rvalidtokens, "]" )
      .replace( rvalidbraces, "")) ) {

    return ( new Function( "return " + data ) )();

  }
  error( "Invalid JSON: " + data );
}

function noop() {}

function toggleCase(oldObj, newObj, flag){
  if (flag === 'lower') {
    for (var key in oldObj) {
      if (util.isPlainObject(oldObj[key]) || util.isArray(oldObj[key])) {
        newObj[key.toLowerCase()] = toggleObjKey(oldObj[key], flag)
      } else {
        newObj[key.toLowerCase()] = oldObj[key];
      }
    }
  } else if (flag === 'upper') {
    for (var key in oldObj) {
      if (util.isPlainObject(oldObj[key]) || util.isArray(oldObj[key])) {
        newObj[key.toUpperCase()] = toggleObjKey(oldObj[key], flag)
      } else {
        newObj[key.toUpperCase()] = oldObj[key];
      }
    }
  }

  return newObj;
}

/**
 * @description 把对象的key值进行大小写转换
 * @param obj Object or Array
 * @param flag String lower or upper, 默认值lower
 */
function toggleObjKey(obj, flag){
  var newObj, caseFlag;
  caseFlag = flag ? flag : 'lower';

  if (isPlainObject(obj)){
    newObj = {};
    return toggleCase(obj, newObj, caseFlag)
  } else if(util.isArray(obj)){
    newObj = [];
    return toggleCase(obj, newObj, caseFlag)
  } else {
    return obj;
  }
}

/**
 * @description 格式化金额
 * @param s 传入数值
 * @param n 小数点保留位数，默认2位
 * @param f 千位分隔符，默认无
 */
function formatMoney(s, n, f) {
  if (IsNumber(s)){
    n = n > 0 && n <= 20 ? n : 2;
    f = typeof (f) == 'undefined' ? "" : f;
    s = parseFloat((s + "").replace(/[^\d\.-]/g, ""));

    var fixNum = new Number(s + 1).toFixed(n);
    var fixedNum = new Number(fixNum - 1).toFixed(n);
    s = fixedNum + "";

    var l = s.split(".")[0].split("").reverse();
    var r = s.split(".")[1];
    var t = "";
    for( var i = 0; i < l.length; i++ ) {
      t += l[i] + ((i + 1) % 3 == 0 && (i + 1) != l.length ? f : "");
    }
    return t.split("").reverse().join("") + "." + r.substring(0, n);
  }
  return '';
}

/**
 * @description 判断字符串是否为'',null,undefined
 * @param str  String
 */
function isEmpty(str) {
  if (str === '' || str === null || str === undefined){
    return true
  } else {
    return false
  }
}

/**
 * @description 判断字符串是否含有特殊字符
 * @param str  String
 */
function checkSpecialCode(str){
  if (isEmpty(str)) {
    return true
  }

  var reg = "~`#$￥%^&*{}=][【】《》><?？/\\'";	//特殊字符（描述信息可能会有中英文分号、叹号）
  for(var j=0;j<reg.length;j++){
    if(str.indexOf(reg.charAt(j))!=-1){
      return false;
      break;
    };
  }

  return true;
}

var util = {
  isFunction: isFunction,
  isArray: Array.isArray || isArray,
  isWindow: isWindow,
  isNumeric: isNumeric,
  type: type,
  isPlainObject: isPlainObject,
  isEmptyObject: isEmptyObject,
  error: error,
  parseJSON: parseJSON,
  noop: noop,
  trim: trim ?
    function( text ) {
      return text == null ?
        "" :
        trim.call( text );
    } :

    // Otherwise use our own trimming functionality
    function( text ) {
      return text == null ?
        "" :
        text.toString().replace( trimLeft, "" ).replace( trimRight, "" );
    },
  toggleObjKey: toggleObjKey,
  formatMoney: formatMoney,
  isEmpty: isEmpty,
  checkSpecialCode: checkSpecialCode
}

export default util
