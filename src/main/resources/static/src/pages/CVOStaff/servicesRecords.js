/* DogCARE — Page: servicesRecords */

/* ---------------- SERVICES & RECORDS ---------------- */
function screenServicesRecords(){
  const approved = S.requests.filter(r=>r.staffDecision==="Approved" && !r.serviceRecord);
  const completed = S.requests.filter(r=>r.status==="Completed").sort((a,b)=>b.serviceRecord.date.localeCompare(a.serviceRecord.date));
  const query = String(UI.recordSearch||"").trim().toLowerCase();
  const matching = completed.filter(r=>{
    const p=petById(r.petId), o=userById(r.ownerId), rec=r.serviceRecord||{};
    return !query || [r.id,r.service,p?.id,p?.name,o?.name,o?.houseStreet,o?.barangay,rec.serviceLocation,rec.presenterName,rec.sourceReference,rec.date]
      .filter(Boolean).join(" ").toLowerCase().includes(query);
  });
  const active = UI.selected.reqId && approved.find(r=>r.id===UI.selected.reqId) ? UI.selected.reqId : (approved[0] ? approved[0].id : null);

  const scanCard = `
  <div class="card">
    <h3>Returning Resident? Scan Pet QR</h3>
    <p style="font-size:0.78rem;color:var(--body);margin-top:-8px;">Scan the Digital Pet ID to identify a registered pet and open its current record. The QR is for lookup only; staff must still confirm the resident, select the service, and save the visit details.</p>
    <button class="btn-red" id="scan-qr-btn">Scan to Find Pet</button>
  </div>`;

  if(!approved.length){
    return `
    <div class="page-head"><div><div class="crumb">Services</div><h2>Services &amp; Records</h2></div></div>
    ${scanCard}
    ${emptyState(icoSvg('vet'),"No Approved Requests Awaiting Service","Approved requests will appear here for service recording.","")}
    ${serviceRecordSearchCard(matching, query)}
    </div>`;
  }

  const req = reqById(active);
  const pet = petById(req.petId), owner = userById(req.ownerId);
  const conditional = req.service === "Anti-Rabies Vaccination" ? `
    <div class="two-field">
      ${field("Vaccine Type", `<input id="svr-vaccine" placeholder="e.g. Rabisin">`)}
      ${field("Batch / Lot Number", `<input id="svr-batch" placeholder="e.g. AR-2026-0417">`)}
    </div>
    ${field("Next Due Date", `<input id="svr-nextdue" type="date">`)}`
  : req.service === "Microchipping" ? `
    ${field("Microchip Number", `<input id="svr-chip" placeholder="e.g. 900182820012345">`)}`
  : `
    ${field("Procedure Date", `<input id="svr-procdate" type="date" value="${todayISO()}">`)}
    ${field("Post-Procedure Remarks", `<textarea id="svr-postremarks" rows="2"></textarea>`)}`;

  return `
  <div class="page-head"><div><div class="crumb">Services</div><h2>Services &amp; Records</h2></div></div>
  ${scanCard}
  <div class="card">
    <div class="filter-bar">
      <select id="svr-select-req" style="flex:1;">
        ${approved.map(r=>`<option value="${r.id}" ${r.id===active?"selected":""}>${r.id} — ${petById(r.petId)?.name} — ${r.service}</option>`).join("")}
      </select>
    </div>
    <div class="role-banner">Selected Pet: <b>${esc(pet.name)} (${pet.id})</b> &nbsp;•&nbsp; Owner: <b>${esc(owner.name)}</b> &nbsp;•&nbsp; Service: <b>${esc(req.service)}</b></div>
    <h3>Service Information</h3>
    <div class="kv" style="margin-bottom:14px;">
      <div><div class="k">Request ID</div><div class="v">${req.id}</div></div>
      <div><div class="k">Pet</div><div class="v">${esc(pet.name)}</div></div>
      <div><div class="k">Owner</div><div class="v">${esc(owner.name)}</div></div>
      <div><div class="k">Service</div><div class="v">${esc(req.service)}</div></div>
    </div>
    <h3>Service Details</h3>
    <div class="two-field">
      ${field("Service Date *", `<input id="svr-date" type="date" value="${todayISO()}">`)}
      ${field("Service Provider / Vaccinator *", `<input id="svr-staff" value="${esc(currentUser().name)}">`)}
    </div>
    <div class="two-field">
      ${field("Provider Role", `<select id="svr-provider-role"><option>CVO Staff</option><option>Trained Vaccinator</option><option>Veterinarian</option><option>Data Encoder</option></select>`)}
      ${field("Person Presenting the Pet", `<input id="svr-presenter" placeholder="Owner, household member, or representative">`)}
    </div>
    <div class="two-field">
      ${field("Record Source", `<select id="svr-source"><option>Digital request</option><option>Walk-in</option><option>Barangay activity</option><option>Encoded from paper</option></select>`)}
      ${field("Service Location", `<input id="svr-location" placeholder="CVO office, barangay, or subdivision">`)}
    </div>
    ${field("Paper / Event Reference", `<input id="svr-reference" placeholder="Optional requisition slip, event, or source record number">`)}
    ${conditional}
    ${field("Remarks", `<textarea id="svr-remarks" rows="2"></textarea>`)}
    <div style="display:flex;gap:10px;margin-top:10px;">
      <button class="btn-red" style="flex:1;" id="svr-save">Save Service Record</button>
      <button class="btn-line" style="flex:1;" id="svr-cancel">Cancel</button>
    </div>
  </div>
  ${serviceRecordSearchCard(matching, query)}`;
}

function serviceRecordSearchCard(rows, query){
  return `<div class="card"><h3>Record Search &amp; Retrieval</h3>
    <p class="form-note">Search by request or pet ID, pet, owner, household address, barangay, year, service location, presenter, or paper reference.</p>
    <div class="filter-bar"><input id="record-search" value="${esc(query)}" placeholder="Search completed records..."><button class="btn-line" id="record-search-clear">Clear</button></div>
    ${rows.length ? `<div style="overflow-x:auto"><table><thead><tr><th>Date</th><th>Pet</th><th>Owner / Household</th><th>Service</th><th>Location</th><th>Source</th></tr></thead><tbody>${rows.map(r=>{const p=petById(r.petId),o=userById(r.ownerId),x=r.serviceRecord||{};return `<tr><td>${fmtDate(x.date)}</td><td><b>${esc(p?.name||"—")}</b><br><small>${esc(p?.id||"")}</small></td><td>${esc(o?.name||"—")}<br><small>${esc([o?.houseStreet,o?.barangay].filter(Boolean).join(", "))}</small></td><td>${esc(r.service)}</td><td>${esc(x.serviceLocation||"—")}</td><td>${esc(x.sourceType||"Digital request")}</td></tr>`}).join("")}</tbody></table></div>` : `<p class="empty">No completed records match this search.</p>`}
  </div>`;
}

function saveServiceRecord(reqId, data){
  runAction(() => Api.post(`/api/service-requests/${encodeURIComponent(reqId)}/record`, {
      serviceDate:data.date || null, staffName:data.staff || null, remarks:data.remarks || null,
      vaccineType:data.vaccine || null, batchNumber:data.batch || null, nextDue:data.nextdue || null,
      microchipNumber:data.chip || null, procedureDate:data.procdate || null, postRemarks:data.postremarks || null,
      presenterName:data.presenter || null, providerRole:data.providerRole || null, sourceType:data.sourceType || null,
      sourceReference:data.sourceReference || null, serviceLocation:data.serviceLocation || null,
    }), () => {
      showToast(`Service record saved. ${reqId} marked Completed.`);
      UI.selected.reqId = null;
      UI.screen = "servicesrecords";
      render();
    });
}
