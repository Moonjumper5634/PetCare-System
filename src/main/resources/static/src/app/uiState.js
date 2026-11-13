/* DogCARE — Transient (non-persisted) UI state */

/* ---------------- TRANSIENT UI STATE (not persisted) ---------------- */
function defaultUI(){
  return {
    view:"login",              // "login" | "app"
    authScreen:"landing",      // landing|login|signup|signupSuccess|verify|verifySuccess|forgotEmail|forgotCode|forgotNewPw|forgotSuccess
    screen:"dashboard",
    selected:{petId:null, regId:null, reqId:null, adoptId:null, shelterId:null},
    signupDraft:{},
    forgotDraft:{},
    wiz:{},                    // active wizard state, keyed by wizard name
    modal:null,                // {type, data}
    cancelTarget:null,         // function to call on discard-cancel confirmation
    toasts:[],
    errors:{},
    filters:{},
    pages:{},
    sidebarOpen:false,
  };
}
var UI = defaultUI();

function paged(rows,key,size=10){
  const pages=Math.max(1,Math.ceil(rows.length/size)),current=Math.min(UI.pages[key]||1,pages);UI.pages[key]=current;
  return {items:rows.slice((current-1)*size,current*size),current,pages,total:rows.length,key};
}
function pager(p){
  if(p.pages<=1)return `<div class="table-count">${p.total} record${p.total===1?'':'s'}</div>`;
  return `<div class="pager"><span>${p.total} records · Page ${p.current} of ${p.pages}</span><div><button class="btn-sm" data-page="${p.key}:${p.current-1}" ${p.current===1?'disabled':''}>Previous</button><button class="btn-sm" data-page="${p.key}:${p.current+1}" ${p.current===p.pages?'disabled':''}>Next</button></div></div>`;
}
