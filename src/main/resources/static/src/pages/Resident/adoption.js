/* PetCARE — Page: adoption */

/* ---------------- ADOPTION ---------------- */
function screenAdopt(){
  const list = S.shelterPets.filter(s=>s.status==="Available");
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

function screenSurrender(){
  const u=currentUser(), mine=S.pets.filter(p=>p.ownerId===u.id&&!p.archived);
  const rows=(S.surrenderRequests||[]).filter(r=>r.ownerId===u.id);
  return `<div class="page-head"><div><div class="crumb">Pet Records</div><h2>Request Pet Surrender</h2></div></div>
  <div class="support-note" style="margin-bottom:16px;"><b>This form requests CVO assessment.</b> Keep your dog in your care until staff confirm a handover date. Submission does not guarantee acceptance or immediate adoption listing.</div>
  <div class="two-col surrender-grid"><div class="card"><h3>Submit a Request</h3>
    ${mine.length?`${field("Registered Dog *",`<select id="sur-pet">${mine.map(p=>`<option value="${p.id}">${esc(p.name)} — ${esc(p.breed||'Breed not recorded')}</option>`).join("")}</select>`)}
    ${field("Reason for Surrender *",`<textarea id="sur-reason" rows="3" maxlength="1000" placeholder="Explain why CVO assistance is being requested."></textarea>`)}
    ${field("Care, Health and Behavior Information",`<textarea id="sur-circumstances" rows="3" maxlength="1000" placeholder="Current care arrangement, health concerns, temperament, bite history, or urgent circumstances."></textarea>`)}
    ${field("Contact Number *",`<input id="sur-contact" value="${esc(u.mobile||'')}" inputmode="tel">`)}
    <label class="checkbox-row"><input type="checkbox" id="sur-ack"><span>I understand that this is an assessment request and I must wait for CVO confirmation before bringing the dog.</span></label>
    <button class="btn-red" id="sur-submit" style="width:100%;margin-top:14px;">Submit Surrender Request</button>`:emptyState(icoSvg('paw'),"No Registered Dog","Register your dog before requesting surrender assessment.",`<button class="btn-red" data-nav="petregistration">Register a Dog</button>`)}</div>
  <div class="card"><h3>My Surrender Requests</h3>${rows.length?rows.map(r=>{const p=petById(r.petId);return `<div class="checklist-row" style="align-items:flex-start;"><span><b>${r.id} — ${esc(p?.name||'Dog')}</b><br><small>${fmtDate(r.submittedDate)}${r.handoverDate?` · Handover: ${fmtDate(r.handoverDate)}`:''}</small>${r.staffNotes?`<br><small>${esc(r.staffNotes)}</small>`:''}</span>${statusPill(r.status)}</div>`}).join(''):emptyState(icoSvg('doc'),"No Requests","Your submitted surrender requests will appear here.","")}</div></div>`;
}
function screenAdoptDetail(){
  const d = shelterById(UI.selected.shelterId);
  if(!d) return emptyState(icoSvg('paw'),"No Pet Selected","Choose a pet to view details.","");
  const canApply = d.status === "Available";
  return `
  <div class="page-head"><div><div class="crumb">Adopt a Pet / ${esc(d.name)}</div><h2>${esc(d.name)}</h2></div></div>
  <div class="two-col adoption-detail-grid">
    <div class="card adoption-photo-card" style="text-align:center;">
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
function newAdoptDraft(){ return {applicantName:"",contact:"",email:"",address:"",barangay:BARANGAYS[0],reason:"",experience:"",housing:HOUSING_TYPES[0],householdConsent:false,childrenPets:"",secureArea:false,keepingArrangement:"Primarily Indoors",dailyCare:"",financialReady:false,landlordPermission:"Not Applicable",homeAssessment:false,agree:false}; }
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
      <h3>Home and Care Suitability</h3>
      <p class="form-note">Answer honestly. These answers help authorized staff assess suitability during the interview; an unchecked answer does not automatically reject the application.</p>
      <div class="checkbox-row"><input type="checkbox" id="ad-household" ${dr.householdConsent?"checked":""}><label for="ad-household">All household members agree to adopting this dog.</label></div>
      ${field("Children and Existing Pets *", `<textarea id="ad-children-pets" rows="2" placeholder="List children, dogs, cats, or write None.">${esc(dr.childrenPets)}</textarea>`)}
      <div class="checkbox-row"><input type="checkbox" id="ad-secure" ${dr.secureArea?"checked":""}><label for="ad-secure">The home has a secure area suitable for the dog.</label></div>
      ${field("Where will the dog stay? *", `<select id="ad-keeping"><option ${dr.keepingArrangement==='Primarily Indoors'?'selected':''}>Primarily Indoors</option><option ${dr.keepingArrangement==='Indoors and Outdoors'?'selected':''}>Indoors and Outdoors</option><option ${dr.keepingArrangement==='Primarily Outdoors'?'selected':''}>Primarily Outdoors</option></select>`)}
      ${field("Expected Daily Care *", `<textarea id="ad-daily-care" rows="3" placeholder="Feeding, exercise, grooming, supervision and veterinary care.">${esc(dr.dailyCare)}</textarea>`)}
      <div class="checkbox-row"><input type="checkbox" id="ad-financial" ${dr.financialReady?"checked":""}><label for="ad-financial">I can provide food, preventive care and necessary veterinary treatment.</label></div>
      ${field("Landlord Permission *", `<select id="ad-landlord"><option ${dr.landlordPermission==='Not Applicable'?'selected':''}>Not Applicable</option><option ${dr.landlordPermission==='Yes'?'selected':''}>Yes</option><option ${dr.landlordPermission==='No'?'selected':''}>No</option></select>`)}
      <div class="checkbox-row"><input type="checkbox" id="ad-home-assessment" ${dr.homeAssessment?"checked":""}><label for="ad-home-assessment">I am willing to attend an interview and permit a home assessment if required by CVO staff.</label></div>
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
      <div><div class="k">Keeping Arrangement</div><div class="v">${esc(dr.keepingArrangement)}</div></div>
      <div><div class="k">Landlord Permission</div><div class="v">${esc(dr.landlordPermission)}</div></div>
      <div><div class="k">Household Consent</div><div class="v">${dr.householdConsent?'Yes':'No'}</div></div>
      <div><div class="k">Secure Living Area</div><div class="v">${dr.secureArea?'Yes':'No'}</div></div>
      <div><div class="k">Financial Readiness</div><div class="v">${dr.financialReady?'Yes':'No'}</div></div>
      <div><div class="k">Interview / Home Assessment</div><div class="v">${dr.homeAssessment?'Yes':'No'}</div></div>
    </div>
    <p style="font-size:0.8rem;color:var(--body);margin-top:10px;"><b>Reason for Adoption:</b> ${esc(dr.reason)}</p>
    <p style="font-size:0.8rem;color:var(--body);"><b>Children / Existing Pets:</b> ${esc(dr.childrenPets)}</p>
    <p style="font-size:0.8rem;color:var(--body);"><b>Daily Care Plan:</b> ${esc(dr.dailyCare)}</p>
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
