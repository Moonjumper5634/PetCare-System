/* PetCARE — Page: petRegistrations */

/* ---------------- STAFF: PET REGISTRATIONS QUEUE + REVIEW ---------------- */
function screenPetRegistrations(){
  const filter = UI.filters.regStatus || "All";
  let rows = [...S.registrations].sort((a,b)=>b.submittedDate.localeCompare(a.submittedDate));
  if(filter!=="All") rows = rows.filter(r=>r.status===filter);
  const pg=paged(rows,"registrations"); rows=pg.items;
  return `
  <div class="page-head"><div><div class="crumb">Registrations & Requests</div><h2>Pet Registrations</h2></div></div>
  <div class="card">
    <div class="filter-bar">
      ${[["All","All"],["Pending","Under verification"],["Verified","Verified"],["Rejected","Needs correction"]].map(([value,label])=>`<button class="btn-sm ${filter===value?"solid":""}" data-setfilter="regStatus:${value}">${label}</button>`).join("")}
    </div>
    ${rows.length ? `<table><thead><tr><th>Registration ID</th><th>Pet</th><th>Owner</th><th>Barangay</th><th>Date</th><th>Status</th><th>Action</th></tr></thead>
    <tbody>${rows.map(r=>{const p=petById(r.petId), o=userById(r.ownerId);
      return `<tr><td>${r.id}</td><td><b>${p?esc(p.name):"—"}</b></td><td>${o?esc(o.name):"—"}</td><td>${p?esc(p.barangay):"—"}</td><td>${fmtDate(r.submittedDate)}</td><td>${registrationStatusPill(r.status)}</td>
      <td><button class="btn-sm solid" data-nav="regdetail" data-reg="${r.id}">Review</button></td></tr>`;}).join("")}</tbody></table>`
    : emptyState(icoSvg('doc'),"No Registrations","No pet registrations match this filter.","")}${pager(pg)}
  </div>`;
}

function screenRegDetail(){
  const reg = regById(UI.selected.regId);
  if(!reg) return emptyState(icoSvg('doc'),"No Registration Selected","Choose a registration to review.","");
  const p = petById(reg.petId), o = userById(reg.ownerId);
  const staffMode = currentUser().role !== "owner";
  return `
  <div class="page-head"><div><div class="crumb">Pet Registrations</div><h2>Registration ${reg.id}</h2></div></div>
  <div class="two-col">
    <div class="col-stack">
      <div class="card"><h3>Registration Details</h3>
        <div class="kv">
          <div><div class="k">Registration ID</div><div class="v">${reg.id}</div></div>
          <div><div class="k">Date Submitted</div><div class="v">${fmtDate(reg.submittedDate)}</div></div>
          <div><div class="k">Status</div><div class="v">${registrationStatusPill(reg.status)}</div></div>
          <div><div class="k">Source</div><div class="v">${reg.source}</div></div>
          ${reg.reviewedByName?`<div><div class="k">Last reviewed by</div><div class="v">${esc(reg.reviewedByName)}</div></div><div><div class="k">Review time</div><div class="v">${fmtDateTime(reg.reviewedAt)}</div></div>`:""}
        </div>
      </div>
      <div class="card"><h3>Owner Details</h3>
        <div class="kv">
          <div><div class="k">Name</div><div class="v">${o?esc(o.name):"—"}</div></div>
          <div><div class="k">Contact</div><div class="v">${o?esc(o.mobile):"—"}</div></div>
          <div><div class="k">Barangay</div><div class="v">${o?esc(o.barangay):"—"}</div></div>
        </div>
      </div>
      <div class="card"><h3>Pet Details</h3>
        <div class="kv">
          <div><div class="k">Pet Name</div><div class="v">${p?esc(p.name):"—"}</div></div>
          <div><div class="k">Species</div><div class="v">${p?esc(p.species):"—"}</div></div>
          <div><div class="k">Breed</div><div class="v">${p?esc(p.breed):"—"}</div></div>
          <div><div class="k">Sex</div><div class="v">${p?esc(p.sex):"—"}</div></div>
          <div><div class="k">Age</div><div class="v">${p?ageString(p.dob):"—"}</div></div>
          <div><div class="k">Barangay</div><div class="v">${p?esc(p.barangay):"—"}</div></div>
        </div>
      </div>
      ${staffMode && reg.status==="Pending" ? `
      <div class="card"><h3>Verification Checklist</h3>
        <div class="checklist-row"><span>Required information complete</span>${pill("green:Checked")}</div>
        <div class="checklist-row"><span>Owner information available</span>${pill("green:Checked")}</div>
        <div class="checklist-row"><span>Pet information complete</span>${pill("green:Checked")}</div>
      </div>` : ""}
    </div>
    <div class="col-stack">
      <div class="card"><h3>Registration Timeline</h3>${renderTimeline(registrationTimelineSteps(reg))}</div>
      ${staffMode && reg.status==="Pending" ? `
      <div class="card">
        <h3>Staff Remarks</h3>
        <div class="field"><textarea id="reg-remarks" rows="3" placeholder="Optional remarks..."></textarea></div>
        <div style="display:flex;gap:10px;">
          <button class="btn-red" style="flex:1;" id="reg-approve-btn">Mark as Verified</button>
          <button class="btn-sm danger" style="flex:1;padding:10px;" id="reg-reject-btn">Request Correction</button>
        </div>
      </div>` : reg.status==="Rejected" ? `<div class="card"><h3>Correction Requested</h3><p style="font-size:0.85rem;">${esc(reg.rejectionReason||"—")}</p>${reg.reviewedByName?`<p class="form-note">Requested by ${esc(reg.reviewedByName)}${reg.reviewedAt?` on ${fmtDateTime(reg.reviewedAt)}`:""}.</p>`:""}${!staffMode?`<button class="btn-red" id="reg-correct-btn">Correct and Resubmit</button>`:""}</div>` : ""}
    </div>
  </div>`;
}
function beginRegistrationCorrection(regId){
  const reg=regById(regId),p=petById(reg.petId); if(!reg||!p)return;
  UI.wiz.reg={step:1,resubmitRegId:regId,data:{name:p.name,species:"Dog",breed:p.breed||"",sex:p.sex,dob:p.dob,barangay:p.barangay,color:p.color||"",notes:"",
    weight:String(p.weight||""),healthStatus:p.healthStatus||"Apparently Healthy",healthNotes:p.healthNotes||"",rabiesStatus:p.rabiesStatus||"Unknown",lastRabies:p.lastRabies||"",
    dewormingStatus:p.dewormingStatus||"Unknown",lastDeworming:p.lastDeworming||"",microchipStatus:p.microchipStatus||"Not Microchipped",microchipNumber:p.microchip||"",proofOfResidency:p.proofOfResidency||"Submitted, Pending Verification"}};
  UI.screen="petregistration";render();
}

function approveRegistration(regId){
  const reg = regById(regId);
  const pet = petById(reg.petId);
  const owner = userById(reg.ownerId);
  const staff = currentUser();
  openModal(`
  <div class="modal-box">
    <h3>Verify registration for ${esc(pet.name)}?</h3>
    <div class="modal-kv kv">
      <div><div class="k">Registration</div><div class="v">${reg.id}</div></div>
      <div><div class="k">Owner</div><div class="v">${esc(owner.name)}</div></div>
    </div>
    <div class="modal-actions">
      <button class="btn-line" id="m-cancel">Cancel</button>
      <button class="btn-red" id="m-confirm-approve-reg">Confirm Verification</button>
    </div>
  </div>`);
  UI._pendingRegId = regId;
}
function confirmApproveRegistration(){
  const reg = regById(UI._pendingRegId), pet = petById(reg.petId);
  runAction(() => Api.post(`/api/registrations/${encodeURIComponent(reg.id)}/verify`, {onSite:false}),
    () => { closeModal(); showToast(`${pet.name}'s registration has been verified.`); });
}
function rejectRegistration(regId){
  const reg = regById(regId);
  const pet = petById(reg.petId);
  openModal(`
  <div class="modal-box">
    <h3>Request a Correction</h3>
    ${field("What should the resident correct? *", `<textarea id="reg-reject-reason" rows="3"></textarea>`)}
    <div class="modal-actions">
      <button class="btn-line" id="m-cancel">Cancel</button>
      <button class="btn-sm danger" style="flex:1;padding:10px;" id="m-confirm-reject-reg">Send Correction Request</button>
    </div>
  </div>`);
  UI._pendingRegId = regId;
}
function confirmRejectRegistration(reason){
  if(!reason || !reason.trim()){ showToast("Please explain what needs correction."); return; }
  const reg = regById(UI._pendingRegId), pet = petById(reg.petId);
  runAction(() => Api.post(`/api/registrations/${encodeURIComponent(reg.id)}/reject`, {reason:reason.trim()}),
    () => { closeModal(); showToast(`A correction was requested for ${pet.name}'s registration.`); });
}
