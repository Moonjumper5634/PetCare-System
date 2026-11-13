/* PetCARE — Page: pets */

/* ---------------- MY PETS / PET PROFILE ---------------- */
function petCard(p){
  const owner = userById(p.ownerId);
  return `
  <div class="pet-card">
    <div class="pet-card-top">
      ${petAvatar(p,48)}
      <div>
        <div class="pet-card-name">${esc(p.name)}</div>
        <div class="pet-card-sub">${esc(p.species)} · ${esc(p.breed)} · ${esc(p.sex)} · ${ageString(p.dob)}</div>
      </div>
    </div>
    <div class="pet-card-status">
      <div class="checklist-row"><span>Registration Status</span>${registrationStatusPill(p.registrationStatus)}</div>
      <div class="checklist-row"><span>Barangay</span><b style="font-size:0.78rem;">${esc(p.barangay)}</b></div>
      <div class="checklist-row"><span>Last Anti-Rabies Vaccination</span><b style="font-size:0.78rem;">${p.lastRabies?fmtDate(p.lastRabies):"No Record"}</b></div>
    </div>
    <button class="btn-red pet-card-btn" data-nav="petprofile" data-pet="${p.id}">View Pet Profile</button>
  </div>`;
}
function screenMyPets(){
  const u = currentUser();
  const mine = ownerPets(u.id);
  return `
  <div class="page-head">
    <div><div class="crumb">Pet Records</div><h2>My Pets</h2></div>
    <button class="btn-red" data-nav="petregistration">+ Register a New Pet</button>
  </div>
  ${mine.length ? `<div class="pet-card-grid">${mine.map(petCard).join("")}</div>`
    : emptyState(icoSvg('paw'),"No Pets","You don't have any registered pets yet.",`<button class="btn-red" data-nav="petregistration">Register a Pet</button>`)}
  `;
}

function screenPetProfile(){
  const p = petById(UI.selected.petId) || ownerPets(currentUser().id)[0];
  if(!p) return emptyState(icoSvg('paw'),"No Pet Selected","Select a pet from My Pets to view its profile.","");
  const role = currentUser().role;
  const isOwner = role === "owner";
  const owner = userById(p.ownerId);
  const myReqs = S.requests.filter(r=>r.petId===p.id);
  const canRequest = isOwner && p.registrationStatus==="Verified";
  const actions = isOwner
    ? `<button class="btn-line" data-editpet="${p.id}">Edit Dog Profile</button> <button class="btn-sm danger" data-archivepet="${p.id}">Archive</button> ${canRequest ? `<button class="btn-red" data-nav="requestservice" data-pet="${p.id}">Request a Service</button>` : ""}`
    : `<button class="btn-red" data-nav="eligibility" data-req="${myReqs[0]?myReqs[0].id:''}">Assess Eligibility</button>`;

  const historyRows = myReqs.filter(r=>r.serviceRecord).sort((a,b)=>b.serviceRecord.date.localeCompare(a.serviceRecord.date));
  const vaxRows = historyRows.filter(r=>r.service==="Anti-Rabies Vaccination")
    .map(r=>`<tr><td>${fmtDate(r.serviceRecord.date)}</td><td>${esc(r.serviceRecord.vaccineType||"—")}</td><td>${esc(r.serviceRecord.batchNumber||"—")}</td><td>${statusPill("Completed")}</td></tr>`).join("");
  const kaponRows = historyRows.filter(r=>r.service==="Kapon / Spay-Neuter")
    .map(r=>`<tr><td>${fmtDate(r.serviceRecord.date)}</td><td>${esc(r.serviceRecord.staff||"—")}</td><td>${statusPill("Completed")}</td></tr>`).join("");

  return `
  <div class="page-head">
    <div><div class="crumb">Pet Records / ${esc(p.name)}</div><h2>${esc(p.name)}</h2></div>
    <div>${actions}</div>
  </div>
  <div class="two-col">
    <div class="col-stack">
      <div class="card" style="text-align:center;">
        <div style="width:120px;height:120px;border-radius:50%;overflow:hidden;margin:0 auto 14px;border:3px solid var(--border);box-shadow:0 4px 10px rgba(0,0,0,0.1);display:flex;align-items:center;justify-content:center;background:var(--danger-bg);color:var(--red);font-weight:700;font-size:1.6rem;">
          ${p.photo?`<img src="${p.photo}" alt="${esc(p.name)}" style="width:100%;height:100%;object-fit:cover;">`:esc(initials(p.name))}
        </div>
        <h3 style="border-left:none;padding-left:0;margin-bottom:4px;">${esc(p.name)}</h3>
        <div style="font-size:0.78rem;color:var(--muted);margin-bottom:14px;">${esc(p.breed)} (${esc(p.species)})</div>
        ${registrationStatusPill(p.registrationStatus)}
      </div>
      <div class="card"><h3>Pet Information</h3>
        <div class="kv">
          <div><div class="k">Pet ID</div><div class="v">${p.id}</div></div>
          <div><div class="k">Species</div><div class="v">${esc(p.species)}</div></div>
          <div><div class="k">Breed</div><div class="v">${esc(p.breed)}</div></div>
          <div><div class="k">Sex</div><div class="v">${esc(p.sex)}</div></div>
          <div><div class="k">Age</div><div class="v">${ageString(p.dob)}</div></div>
          <div><div class="k">Barangay</div><div class="v">${esc(p.barangay)}</div></div>
          <div><div class="k">Registration Status</div><div class="v">${p.registrationStatus}</div></div>
        </div>
      </div>
      <div class="card"><h3>Owner</h3>
        <div class="kv">
          <div><div class="k">Owner Name</div><div class="v">${owner?esc(owner.name):"—"}</div></div>
          <div><div class="k">Contact Number</div><div class="v">${owner?esc(owner.mobile):"—"}</div></div>
        </div>
      </div>
      ${p.registrationStatus==="Verified" ? `
      <div class="card" style="text-align:center;">
        <h3 style="text-align:left;">Digital Pet ID</h3>
        <p style="font-size:0.72rem;color:var(--body);text-align:left;margin-top:-6px;">This replaces a physical booklet. Staff can scan this code on a return visit (e.g. next year's anti-rabies shot) to instantly pull up ${esc(p.name)}'s record and add the new visit — no paperwork to lose.</p>
        <div id="pet-qr-${p.id}" style="display:flex;align-items:center;justify-content:center;margin:10px auto;width:220px;height:220px;"></div>
        <p style="font-size:0.66rem;color:var(--muted);text-align:left;margin:6px 0 0;">Scanning this with any camera app (e.g. Google Lens) shows ${esc(p.name)}'s pet, owner, and vaccination snapshot directly. Scanning it inside PetCARE (Services &amp; Records → Scan Pet QR) opens the live, up-to-date record.</p>
        <div style="font-weight:800;font-size:0.9rem;letter-spacing:0.03em;">${p.id}</div>
      </div>` : ""}
    </div>
    <div class="col-stack">
      <div class="card"><h3>Microchipping</h3><p class="empty">${p.microchip ? "Microchip #"+esc(p.microchip) : "No records on file."}</p></div>
      <div class="card"><h3>Kapon / Spay-Neuter</h3>
        ${kaponRows ? `<table><thead><tr><th>Date</th><th>Staff</th><th>Status</th></tr></thead><tbody>${kaponRows}</tbody></table>` : `<p class="empty">No records on file.</p>`}
      </div>
      <div class="card"><h3>Anti-Rabies Vaccine</h3>
        ${vaxRows ? `<table><thead><tr><th>Date</th><th>Vaccine</th><th>Batch / Lot</th><th>Status</th></tr></thead><tbody>${vaxRows}</tbody></table><button class="btn-line" style="margin-top:12px;width:100%" data-print-vax="${p.id}">Print / Save Digital Vaccination Record</button>` : `<p class="empty">No veterinary service records yet.</p>`}
      </div>
      ${!isOwner ? `<div class="card"><h3>Service Requests</h3>
        ${myReqs.length ? myReqs.map(r=>`<div class="checklist-row"><span>${r.id} — ${esc(r.service)}</span>${statusPill(r.staffDecision==="Rejected"?"Rejected":r.status)}</div>`).join("") : `<p class="empty">No service requests on file.</p>`}
      </div>` : ""}
    </div>
  </div>`;
}

function editPetModal(petId){
  const p=petById(petId); if(!p)return;
  UI._petEditPhoto="";
  openModal(`<div class="modal-box"><h3>Edit Dog Profile</h3><p class="form-note">Saving changes returns this record to CVO verification and alerts personnel.</p>
    <div class="two-field">${field("Dog name *",`<input id="pet-edit-name" value="${esc(p.name)}">`)}${field("Breed *",`<input id="pet-edit-breed" value="${esc(p.breed)}">`)}</div>
    <div class="two-field">${field("Sex *",`<select id="pet-edit-sex"><option ${p.sex==="Male"?"selected":""}>Male</option><option ${p.sex==="Female"?"selected":""}>Female</option></select>`)}${field("Birth date *",`<input id="pet-edit-dob" type="date" value="${p.dob}" max="${todayISO()}">`)}</div>
    ${field("Color / markings",`<input id="pet-edit-color" value="${esc(p.color||"")}">`)}
    <div class="two-field">${field("Weight (kg) *",`<input id="pet-edit-weight" type="number" min="0.1" max="120" step="0.1" value="${p.weight||""}">`)}${field("Health status *",`<select id="pet-edit-health">${["Apparently Healthy","With Known Condition","For Verification"].map(x=>`<option ${p.healthStatus===x?"selected":""}>${x}</option>`).join("")}</select>`)}</div>
    ${field("Health notes",`<textarea id="pet-edit-notes">${esc(p.healthNotes||"")}</textarea>`)}
    ${field("Replacement verification photo",`<input id="pet-edit-photo" type="file" accept="image/jpeg,image/png,image/webp"><div class="form-note">Optional. JPG, PNG or WebP, maximum 2 MB.</div>`)}
    <div class="modal-actions"><button class="btn-line" id="m-cancel">Cancel</button><button class="btn-red" id="m-save-pet" data-petid="${p.id}">Save and Submit for Verification</button></div></div>`);
}

function petUpdatePayload(p){return {ownerId:null,name:document.getElementById("pet-edit-name").value,species:"Dog",breed:document.getElementById("pet-edit-breed").value,sex:document.getElementById("pet-edit-sex").value,dob:document.getElementById("pet-edit-dob").value,color:document.getElementById("pet-edit-color").value,barangay:p.barangay,weight:parseFloat(document.getElementById("pet-edit-weight").value),healthStatus:document.getElementById("pet-edit-health").value,healthNotes:document.getElementById("pet-edit-notes").value,rabiesStatus:p.rabiesStatus,lastRabies:p.lastRabies,dewormingStatus:p.dewormingStatus,lastDeworming:p.lastDeworming,microchipStatus:p.microchipStatus,microchipNumber:p.microchip,proofOfResidency:p.proofOfResidency};}

function archivePet(petId){
  const p=petById(petId); if(!p||!confirm(`Archive ${p.name}'s record? Historical service records will remain available to authorized personnel.`))return;
  runAction(()=>Api.delete(`/api/pets/${encodeURIComponent(petId)}`),()=>{UI.screen="mypets";showToast("Dog record archived and CVO personnel notified.");render();});
}

function printVaccinationRecord(petId){
  const p=petById(petId), owner=userById(p?.ownerId);
  const rows=S.requests.filter(r=>r.petId===petId&&r.service==="Anti-Rabies Vaccination"&&r.serviceRecord).sort((a,b)=>b.serviceRecord.date.localeCompare(a.serviceRecord.date));
  if(!p||!rows.length){showToast("No completed vaccination record is available.","error");return;}
  const body=rows.map(r=>{const x=r.serviceRecord;return `<tr><td>${fmtDate(x.date)}</td><td>${esc(x.vaccineType||"—")}</td><td>${esc(x.batchNumber||"—")}</td><td>${fmtDate(x.nextDue)||"—"}</td><td>${esc(x.staff||"—")}</td><td>${esc(x.serviceLocation||"—")}</td></tr>`}).join("");
  const w=window.open("","petcare-vaccination-record","width=920,height=700");
  if(!w){showToast("Allow pop-ups to print this record.","error");return;}
  w.document.write(`<!doctype html><html><head><title>${esc(p.name)} — Vaccination Record</title><style>body{font:14px Arial;margin:36px;color:#222}h1{color:#761927;margin-bottom:4px}.meta{display:grid;grid-template-columns:1fr 1fr;gap:8px;margin:22px 0}table{width:100%;border-collapse:collapse}th,td{border:1px solid #bbb;padding:8px;text-align:left}th{background:#f4ecee}.note{margin-top:24px;font-size:12px;color:#555}@media print{button{display:none}}</style></head><body><h1>PetCARE Vaccination Record</h1><p>City Veterinary Office — Santa Rosa, Laguna</p><div class="meta"><div><b>Pet:</b> ${esc(p.name)} (${esc(p.id)})</div><div><b>Owner:</b> ${esc(owner?.name||"—")}</div><div><b>Breed:</b> ${esc(p.breed)}</div><div><b>Address:</b> ${esc([owner?.houseStreet,owner?.barangay].filter(Boolean).join(", "))}</div></div><table><thead><tr><th>Date</th><th>Vaccine</th><th>Batch / Lot</th><th>Next Due</th><th>Provider</th><th>Location</th></tr></thead><tbody>${body}</tbody></table><p class="note">Generated from PetCARE. CVO staff must verify the live server record when official confirmation is required.</p><button onclick="print()">Print / Save as PDF</button></body></html>`);
  w.document.close();
}
