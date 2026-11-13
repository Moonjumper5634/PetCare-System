/* DogCARE — Page: adoption */

/* ---------------- ADOPTION ---------------- */
function screenAdopt(){
  const list = S.shelterPets.filter(s=>s.status!=="Adopted");
  if(!list.length) return `<div class="page-head"><div><div class="crumb">Pet Records</div><h2>Adopt a Pet</h2></div></div>${emptyState(icoSvg('paw'),"No Adoption Pets","No pets are currently available for adoption.","")}`;
  return `
  <div class="page-head"><div><div class="crumb">Pet Records</div><h2>Adopt a Pet</h2></div></div>
  <p style="font-size:0.82rem;color:var(--body);margin-top:-10px;margin-bottom:18px;max-width:640px;">View dogs that the City Veterinary Office has listed as available for adoption. Facility details and adoption arrangements must be confirmed directly with CVO staff.</p>
  <div class="pet-card-grid">
    ${list.map(d=>`
    <div class="pet-card">
      <div class="pet-card-top">
        <div class="pet-avatar"><img src="${d.photo}" alt="${esc(d.name)}"></div>
        <div><div class="pet-card-name">${esc(d.name)}</div><div class="pet-card-sub">${esc(d.breed)} · ${esc(d.sex)} · ${esc(d.age)}</div></div>
      </div>
      <div class="pet-card-status">
        <div class="checklist-row"><span>Status</span>${statusPill(d.status)}</div>
        <p style="font-size:0.78rem;color:var(--body);margin:4px 0 0;">${esc(d.temperament)}</p>
      </div>
      <button class="btn-red pet-card-btn" data-nav="adoptdetail" data-shelter="${d.id}">View Details</button>
    </div>`).join("")}
  </div>`;
}
function screenAdoptDetail(){
  const d = shelterById(UI.selected.shelterId);
  if(!d) return emptyState(icoSvg('paw'),"No Pet Selected","Choose a pet to view details.","");
  const canApply = d.status === "Available";
  return `
  <div class="page-head"><div><div class="crumb">Adopt a Pet / ${esc(d.name)}</div><h2>${esc(d.name)}</h2></div></div>
  <div class="two-col">
    <div class="card" style="text-align:center;">
      <img src="${d.photo}" alt="${esc(d.name)}" style="width:100%;max-width:280px;border-radius:10px;margin-bottom:14px;">
    </div>
    <div class="col-stack">
      <div class="card"><h3>Pet Information</h3>
        <div class="kv">
          <div><div class="k">Pet Name</div><div class="v">${esc(d.name)}</div></div>
          <div><div class="k">Species</div><div class="v">${esc(d.species)}</div></div>
          <div><div class="k">Breed</div><div class="v">${esc(d.breed)}</div></div>
          <div><div class="k">Sex</div><div class="v">${esc(d.sex)}</div></div>
          <div><div class="k">Age</div><div class="v">${esc(d.age)}</div></div>
          <div><div class="k">Status</div><div class="v">${statusPill(d.status)}</div></div>
        </div>
        <p style="font-size:0.82rem;color:var(--body);margin-top:12px;"><b>Description:</b> ${esc(d.description)}</p>
        <p style="font-size:0.82rem;color:var(--body);"><b>Temperament:</b> ${esc(d.temperament)}</p>
      </div>
      ${canApply ? `<button class="btn-red" data-nav="adoptapply" data-shelter="${d.id}">Request to Adopt</button>` : `<p class="empty">This pet is not currently available for adoption applications.</p>`}
    </div>
  </div>`;
}
function newAdoptDraft(){ return {applicantName:"", contact:"", email:"", address:"", barangay:BARANGAYS[0], reason:"", experience:"", housing:HOUSING_TYPES[0], agree:false}; }
function screenAdoptApply(){
  const d = shelterById(UI.selected.shelterId);
  if(!UI.wiz.adopt) UI.wiz.adopt = {step:1, data:newAdoptDraft()};
  const w = UI.wiz.adopt, dr = w.data;
  const u = currentUser();
  if(w.step===1 && !dr.applicantName) dr.applicantName = u.name;
  if(w.step===1 && !dr.contact) dr.contact = u.mobile||"";
  if(w.step===1 && !dr.email) dr.email = u.email||"";
  if(w.step===1 && !dr.address) dr.address = u.houseStreet||"";
  if(w.step===1 && u.barangay) dr.barangay = u.barangay;

  const stepHead = `<div class="stepper-head">
    <div class="step-dot ${w.step>1?"done":"active"}">1</div><div class="step-line ${w.step>1?"done":""}"></div>
    <div class="step-dot ${w.step===2?"active":""}">2</div>
  </div>`;

  if(w.step===1){
    return `
    <div class="page-head"><div><div class="crumb">Adopt a Pet</div><h2>Adoption Application — ${esc(d.name)}</h2></div></div>
    <div class="card" style="max-width:600px;margin:0 auto;">
      ${stepHead}
      ${field("Applicant Name *", `<input id="ad-name" value="${esc(dr.applicantName)}">`,"adname")}
      ${field("Contact Number *", `<input id="ad-contact" value="${esc(dr.contact)}">`,"adcontact")}
      ${field("Email *", `<input id="ad-email" value="${esc(dr.email)}">`,"ademail")}
      ${field("Address *", `<input id="ad-address" value="${esc(dr.address)}">`,"adaddress")}
      ${field("Barangay / Municipality *", `<select id="ad-barangay">${BARANGAYS.map(b=>`<option ${dr.barangay===b?"selected":""}>${b}</option>`).join("")}</select>`)}
      <p class="form-note">Adoption applications may be submitted by nonresidents. CVO staff will review identity, address, the dog's origin, and overall suitability during the interview.</p>
      ${field("Reason for Adoption *", `<textarea id="ad-reason" rows="2">${esc(dr.reason)}</textarea>`,"adreason")}
      ${field("Previous Pet Experience", `<textarea id="ad-experience" rows="2">${esc(dr.experience)}</textarea>`)}
      ${field("Housing Type *", `<select id="ad-housing">${HOUSING_TYPES.map(h=>`<option ${dr.housing===h?"selected":""}>${h}</option>`).join("")}</select>`)}
      <div class="checkbox-row"><input type="checkbox" id="ad-agree" ${dr.agree?"checked":""}><label for="ad-agree">I certify that the information provided in this adoption application is true and accurate.</label></div>
      ${UI.errors.adagree ? `<div class="field-error-msg">${esc(UI.errors.adagree)}</div>` : ""}
      <div class="wizard-nav"><button class="btn-line" id="ad-cancel">Back</button><button class="btn-red" id="ad-review">Review Application</button></div>
    </div>`;
  }
  return `
  <div class="page-head"><div><div class="crumb">Adopt a Pet</div><h2>Review Application — ${esc(d.name)}</h2></div></div>
  <div class="card" style="max-width:600px;margin:0 auto;">
    ${stepHead}
    <div class="kv">
      <div><div class="k">Applicant</div><div class="v">${esc(dr.applicantName)}</div></div>
      <div><div class="k">Contact</div><div class="v">${esc(dr.contact)}</div></div>
      <div><div class="k">Email</div><div class="v">${esc(dr.email)}</div></div>
      <div><div class="k">Address</div><div class="v">${esc(dr.address)}, ${esc(dr.barangay)}</div></div>
      <div><div class="k">Housing Type</div><div class="v">${esc(dr.housing)}</div></div>
      <div><div class="k">Selected Pet</div><div class="v">${esc(d.name)}</div></div>
    </div>
    <p style="font-size:0.8rem;color:var(--body);margin-top:10px;"><b>Reason for Adoption:</b> ${esc(dr.reason)}</p>
    <div class="wizard-nav"><button class="btn-line" id="ad-back-edit">Back to Edit</button><button class="btn-red" id="ad-submit">Submit Application</button></div>
  </div>`;
}
function screenAdoptSubmitted(){
  const a = adoptById(UI._lastAdoptId);
  if(!a) return "";
  const d = shelterById(a.shelterPetId);
  return `
  <div class="page-head"><div><div class="crumb">Adopt a Pet</div><h2>Application Submitted</h2></div></div>
  <div class="card" style="max-width:560px;margin:0 auto;text-align:center;">
    <div style="color:var(--green);">${icoSvg('check',44)}</div>
    <p style="font-size:0.88rem;color:var(--body);">Application ID: <b>${a.id}</b><br>Status: Interview Required</p>
    <p style="font-size:0.82rem;color:var(--body);">Your adoption application for ${esc(d.name)} has been submitted for CVO staff review.</p>
    <button class="btn-red" data-nav="adopt">Back to Adoption List</button>
  </div>`;
}
