var CONTEXT_PATH=""

//返回上一级
function ret(){
    window.opener=null;
    window.open('','_self');
    window.close();
}

//返回上一页
function ret1(){
    window.history.go(-1);
}
