/* PetCARE — Wizard reset, form readers and validators */

/* ---------------- WIZARD RESET ON FRESH NAV ---------------- */
function resetWizardsForScreen(screen){
  if(screen==="petregistration") UI.wiz.reg = {step:1, data:newRegDraft()};
  if(screen==="requestservice") UI.wiz.svc = {step:1, data:newSvcDraft()};
  if(screen==="adoptapply") UI.wiz.adopt = {step:1, data:newAdoptDraft()};
  if(screen==="walkin") UI.wiz.walkin = newWalkinState();
}

/* ---------------- EVENT WIRING ---------------- */
function readWizardStep1Reg(){
  const w = UI.wiz.reg;
  w.data.name = document.getElementById("reg-name").value.trim();
  w.data.species = document.getElementById("reg-species").value;
  w.data.sex = document.getElementById("reg-sex").value;
  w.data.breed = document.getElementById("reg-breed").value.trim();
  w.data.dob = document.getElementById("reg-dob").value;
  w.data.barangay = document.getElementById("reg-brgy").value;
  w.data.color = document.getElementById("reg-color").value.trim();
  w.data.notes = document.getElementById("reg-notes").value.trim();
}
function validateRegStep1(d){
  const errs = {};
  if(!d.name) errs.regname = "Required field";
  if(!d.breed) errs.regbreed = "Required field";
  if(!d.dob) errs.regdob = "Required field";
  return errs;
}
function readWizardStep2Reg(){
  const w = UI.wiz.reg;
  w.data.weight = document.getElementById("reg-weight").value;
  w.data.healthStatus = document.getElementById("reg-health").value;
  w.data.healthNotes = document.getElementById("reg-healthnotes").value.trim();
  w.data.rabiesStatus = document.getElementById("reg-rabiesstatus").value;
  w.data.lastRabies = document.getElementById("reg-lastrabies").value;
  w.data.dewormingStatus = document.getElementById("reg-dewormstatus").value;
  w.data.lastDeworming = document.getElementById("reg-lastdeworm").value;
  w.data.microchipStatus = document.getElementById("reg-chipstatus").value;
  w.data.microchipNumber = document.getElementById("reg-chipnumber").value.trim();
  w.data.proofOfResidency = document.getElementById("reg-proofres").value;
}
function validateRegStep2(d){
  const errs = {};
  if(!d.weight) errs.regweight = "Required field";
  return errs;
}
function readWizardStep1Svc(){
  const w = UI.wiz.svc;
  w.data.petId = document.getElementById("svc-pet").value;
  w.data.service = document.getElementById("svc-service").value;
  w.data.remarks = document.getElementById("svc-remarks").value.trim();
}
