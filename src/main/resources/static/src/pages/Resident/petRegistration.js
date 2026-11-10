/* PetCARE — Page: petRegistration */

/* ---------------- PET REGISTRATION WIZARD ---------------- */
function newRegDraft(){
  return {name:"", species:"Dog", breed:"", sex:"Male", dob:"", barangay:BARANGAYS[0], color:"", notes:"",
    weight:"", healthStatus:"Apparently Healthy", healthNotes:"",
    rabiesStatus:"Unknown", lastRabies:"",
    dewormingStatus:"Unknown", lastDeworming:"",
    microchipStatus:"Not Microchipped", microchipNumber:"",
    proofOfResidency:"Verified at Barangay"};
}
function screenPetRegistration(walkinOwnerId){
  const isWalkin = !!walkinOwnerId;
  if(!UI.wiz.reg) UI.wiz.reg = {step:1, data:newRegDraft()};
  const w = UI.wiz.reg;
  const owner = isWalkin ? userById(walkinOwnerId) : currentUser();
  const d = w.data;

  const stepHead = `
  <div class="stepper-head">
    ${[1,2,3].map((n,i)=>`
      <div class="step-dot ${w.step>n?"done":w.step===n?"active":""}">${n}</div>
      ${i<2?`<div class="step-line ${w.step>n?"done":""}"></div>`:""}
    `).join("")}
  </div>`;

  if(w.step===1){
    return `
    <div class="page-head"><div><div class="crumb">Pet Records</div><h2>Register a New Dog</h2></div></div>
    <p style="font-size:0.78rem;color:var(--body);margin-top:-10px;max-width:640px;">PetCARE is a canine veterinary service management system. Registration is available for dogs only.</p>
    <div class="card" style="max-width:640px;margin:0 auto;">
      ${stepHead}
      <h3>Step 1 of 3 — Dog &amp; Owner Information</h3>
      ${field("Pet Name *", `<input id="reg-name" value="${esc(d.name)}">`,"regname")}
      <div class="two-field">
        ${field("Species", `<input value="Dog" disabled><input type="hidden" id="reg-species" value="Dog">`)}
        ${field("Sex *", `<select id="reg-sex"><option ${d.sex==="Male"?"selected":""}>Male</option><option ${d.sex==="Female"?"selected":""}>Female</option></select>`)}
      </div>
      <div class="two-field">
        ${field("Breed *", `<input id="reg-breed" value="${esc(d.breed)}">`,"regbreed")}
        ${field("Date of Birth *", `<input id="reg-dob" type="date" value="${d.dob}" max="${todayISO()}">`,"regdob")}
      </div>
      ${field("Barangay *", `<select id="reg-brgy">${BARANGAYS.map(b=>`<option ${d.barangay===b?"selected":""}>${b}</option>`).join("")}</select>`)}
      ${field("Color / Markings", `<input id="reg-color" value="${esc(d.color)}">`)}
      ${field("Additional Notes", `<textarea id="reg-notes" rows="2">${esc(d.notes)}</textarea>`)}
      <h3 style="margin-top:16px;">Owner Information</h3>
      <div class="two-field">
        <div class="field"><label>Owner Name</label><input value="${esc(owner.name)}" disabled></div>
        <div class="field"><label>Contact Number</label><input value="${esc(owner.mobile||"—")}" disabled></div>
      </div>
      <div class="wizard-nav">
        <button class="btn-line" id="reg-cancel">Cancel</button>
        <button class="btn-red" id="reg-continue">Continue to Health &amp; Requirements</button>
      </div>
    </div>`;
  }
  if(w.step===2){
    return `
    <div class="page-head"><div><div class="crumb">Pet Records</div><h2>Register a New Dog</h2></div></div>
    <div class="card" style="max-width:640px;margin:0 auto;">
      ${stepHead}
      <h3>Step 2 of 3 — Health &amp; Service Requirement Information</h3>
      <p style="font-size:0.76rem;color:var(--body);margin-top:-8px;">This information is stored on the pet profile and reused later when assessing eligibility for veterinary services. Owners will not be asked to re-enter it.</p>
      <div class="two-field">
        ${field("Weight (kg) *", `<input id="reg-weight" type="number" step="0.1" min="0" value="${esc(d.weight)}" placeholder="e.g. 8.5">`,"regweight")}
        ${field("Health Status *", `<select id="reg-health"><option ${d.healthStatus==="Apparently Healthy"?"selected":""}>Apparently Healthy</option><option ${d.healthStatus==="With Known Condition"?"selected":""}>With Known Condition</option><option ${d.healthStatus==="For Verification"?"selected":""}>For Verification</option></select>`)}
      </div>
      ${field("Health Notes", `<textarea id="reg-healthnotes" rows="2" placeholder="Optional — any condition CVO staff should know about">${esc(d.healthNotes)}</textarea>`)}
      <div class="two-field">
        ${field("Anti-Rabies Vaccination Status *", `<select id="reg-rabiesstatus"><option ${d.rabiesStatus==="Updated"?"selected":""}>Updated</option><option ${d.rabiesStatus==="Not Updated"?"selected":""}>Not Updated</option><option ${d.rabiesStatus==="Unknown"?"selected":""}>Unknown</option></select>`)}
        ${field("Last Anti-Rabies Vaccination Date", `<input id="reg-lastrabies" type="date" value="${d.lastRabies}" max="${todayISO()}">`)}
      </div>
      <div class="two-field">
        ${field("Deworming Status *", `<select id="reg-dewormstatus"><option ${d.dewormingStatus==="Updated"?"selected":""}>Updated</option><option ${d.dewormingStatus==="Not Updated"?"selected":""}>Not Updated</option><option ${d.dewormingStatus==="Unknown"?"selected":""}>Unknown</option></select>`)}
        ${field("Last Deworming Date", `<input id="reg-lastdeworm" type="date" value="${d.lastDeworming}" max="${todayISO()}">`)}
      </div>
      <p style="font-size:0.68rem;color:var(--muted);margin:-6px 0 12px;">Deworming is recorded as a health/requirement record. It is not offered as a standalone PetCARE service.</p>
      <div class="two-field">
        ${field("Microchip Status", `<select id="reg-chipstatus"><option ${d.microchipStatus==="Not Microchipped"?"selected":""}>Not Microchipped</option><option ${d.microchipStatus==="Microchipped"?"selected":""}>Microchipped</option></select>`)}
        ${field("Microchip Number (if applicable)", `<input id="reg-chipnumber" value="${esc(d.microchipNumber)}" placeholder="e.g. 933000000000000">`)}
      </div>
      ${field("Owner Proof of Residency *", `<select id="reg-proofres"><option ${d.proofOfResidency==="Verified at Barangay"?"selected":""}>Verified at Barangay</option><option ${d.proofOfResidency==="Submitted, Pending Verification"?"selected":""}>Submitted, Pending Verification</option><option ${d.proofOfResidency==="Not Submitted"?"selected":""}>Not Submitted</option></select>`)}
      <div class="wizard-nav">
        <button class="btn-line" id="reg-back-h">Back</button>
        <button class="btn-red" id="reg-continue-h">Continue to Review</button>
      </div>
    </div>`;
  }
  if(w.step===3){
    return `
    <div class="page-head"><div><div class="crumb">Pet Records</div><h2>Review Registration</h2></div></div>
    <div class="card" style="max-width:640px;margin:0 auto;">
      ${stepHead}
      <h3>Step 3 of 3 — Review &amp; Submit</h3>
      <h3 style="margin-top:0;">Owner</h3>
      <div class="kv" style="margin-bottom:16px;">
        <div><div class="k">Owner Name</div><div class="v">${esc(owner.name)}</div></div>
        <div><div class="k">Barangay</div><div class="v">${esc(owner.barangay||"—")}</div></div>
        <div><div class="k">Contact</div><div class="v">${esc(owner.mobile||"—")}</div></div>
        <div><div class="k">Proof of Residency</div><div class="v">${esc(d.proofOfResidency)}</div></div>
      </div>
      <h3>Dog</h3>
      <div class="kv" style="margin-bottom:16px;">
        <div><div class="k">Pet Name</div><div class="v">${esc(d.name)}</div></div>
        <div><div class="k">Species</div><div class="v">Dog</div></div>
        <div><div class="k">Breed</div><div class="v">${esc(d.breed)}</div></div>
        <div><div class="k">Sex</div><div class="v">${esc(d.sex)}</div></div>
        <div><div class="k">Age</div><div class="v">${d.dob?ageString(d.dob):"—"}</div></div>
        <div><div class="k">Weight</div><div class="v">${d.weight?d.weight+" kg":"—"}</div></div>
        <div><div class="k">Barangay</div><div class="v">${esc(d.barangay)}</div></div>
        <div><div class="k">Microchip</div><div class="v">${esc(d.microchipStatus)}</div></div>
      </div>
      <h3>Health &amp; Requirement Records</h3>
      <div class="kv">
        <div><div class="k">Health Status</div><div class="v">${esc(d.healthStatus)}</div></div>
        <div><div class="k">Anti-Rabies</div><div class="v">${esc(d.rabiesStatus)}${d.lastRabies?" — "+fmtDate(d.lastRabies):""}</div></div>
        <div><div class="k">Deworming</div><div class="v">${esc(d.dewormingStatus)}${d.lastDeworming?" — "+fmtDate(d.lastDeworming):""}</div></div>
      </div>
      <div class="wizard-nav">
        <button class="btn-line" id="reg-back">Back to Edit</button>
        <button class="btn-red" id="reg-submit">Submit Registration</button>
      </div>
    </div>`;
  }
  // step 4 — submitted confirmation
  const reg = regById(w.createdRegId);
  const pet = reg ? petById(reg.petId) : null;
  return `
  <div class="page-head"><div><div class="crumb">Pet Records</div><h2>Pet Registration Submitted</h2></div></div>
  <div class="card" style="max-width:640px;margin:0 auto;text-align:center;">
    <div style="color:var(--green);">${icoSvg('check',44)}</div>
    <h3 style="border:none;padding:0;">Pet Registration Submitted</h3>
    <p style="font-size:0.85rem;color:var(--body);">Registration ID: <b>${reg?reg.id:""}</b></p>
    <div class="kv" style="text-align:left;max-width:340px;margin:0 auto 18px;">
      <div><div class="k">Pet</div><div class="v">${pet?esc(pet.name):""}</div></div>
      <div><div class="k">Status</div><div class="v">Pending CVO Verification</div></div>
      <div><div class="k">Submitted</div><div class="v">${fmtDate(todayISO())}</div></div>
    </div>
    <div style="text-align:left;max-width:420px;margin:0 auto;">${renderTimeline(registrationTimelineSteps(reg))}</div>
    <div style="margin-top:18px;">
      ${isWalkin ? `<button class="btn-red" id="walkin-verify-now">Verify Registration Now</button>`
        : `<button class="btn-red" data-nav="myrequests" data-openreg="${reg?reg.id:''}">View Registration</button>`}
    </div>
  </div>`;
}
