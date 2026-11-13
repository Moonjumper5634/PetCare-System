/* PetCARE — Page: serviceRequests */

/* ---------------- STAFF: SERVICE REQUESTS QUEUE ---------------- */
function requestDisplayStatus(r){
  if(r.staffDecision==="Rejected") return "Rejected";
  return r.status;
}
function screenServiceRequests(){
  const filter = UI.filters.reqStatus || "All";
  let rows = [...S.requests].sort((a,b)=>b.submittedDate.localeCompare(a.submittedDate));
  const statuses = ["All","Submitted","For Review","Approved","Rejected","Completed"];
  if(filter!=="All"){
    if(filter==="Approved") rows = rows.filter(r=>r.staffDecision==="Approved" && r.status!=="Completed");
    else if(filter==="Rejected") rows = rows.filter(r=>r.staffDecision==="Rejected");
    else if(filter==="Completed") rows = rows.filter(r=>r.status==="Completed");
    else if(filter==="Submitted") rows = rows.filter(r=>!r.eligibility);
    else if(filter==="For Review") rows = rows.filter(r=>r.eligibility && r.staffDecision==="Pending");
  }
  const pg=paged(rows,"requests"); rows=pg.items;
  return `
  <div class="page-head"><div><div class="crumb">Registrations & Requests</div><h2>Service Requests</h2></div></div>
  <div class="card">
    <div class="filter-bar">${statuses.map(s=>`<button class="btn-sm ${filter===s?"solid":""}" data-setfilter="reqStatus:${s}">${s}</button>`).join("")}</div>
    ${rows.length ? `<table><thead><tr><th>Request ID</th><th>Pet</th><th>Owner</th><th>Service</th><th>Date</th><th>Status</th><th>Action</th></tr></thead>
    <tbody>${rows.map(r=>{const p=petById(r.petId), o=userById(r.ownerId);
      return `<tr><td>${r.id}</td><td><b>${p?esc(p.name):"—"}</b></td><td>${o?esc(o.name):"—"}</td><td>${esc(r.service)}</td><td>${fmtDate(r.submittedDate)}</td><td>${statusPill(requestDisplayStatus(r))}</td>
      <td><button class="btn-sm solid" data-nav="reqdetail" data-req="${r.id}">Open</button></td></tr>`;}).join("")}</tbody></table>`
    : emptyState(icoSvg('doc'),"No Requests","No service requests match this filter.","")}${pager(pg)}
  </div>`;
}
function screenEligibilityQueue(){
  // "Eligibility Assessment" nav — shows requests awaiting assessment
  const rows = S.requests.filter(r=>!r.eligibility).sort((a,b)=>a.submittedDate.localeCompare(b.submittedDate));
  return `
  <div class="page-head"><div><div class="crumb">Decision Support</div><h2>Eligibility Assessment Queue</h2></div></div>
  <p style="font-size:0.82rem;color:var(--body);margin-top:-10px;margin-bottom:18px;max-width:640px;">Requests awaiting an eligibility assessment against the configured criteria for their service.</p>
  <div class="card">
    ${rows.length ? `<table><thead><tr><th>Request ID</th><th>Pet</th><th>Owner</th><th>Service</th><th>Date</th><th>Action</th></tr></thead>
    <tbody>${rows.map(r=>{const p=petById(r.petId), o=userById(r.ownerId);
      return `<tr><td>${r.id}</td><td><b>${p?esc(p.name):"—"}</b></td><td>${o?esc(o.name):"—"}</td><td>${esc(r.service)}</td><td>${fmtDate(r.submittedDate)}</td>
      <td><button class="btn-sm solid" data-nav="reqdetail" data-req="${r.id}">Assess</button></td></tr>`;}).join("")}</tbody></table>`
    : emptyState(icoSvg('check'),"Queue Clear","No requests are currently awaiting eligibility assessment.","")}
  </div>`;
}

function screenReqDetail(){
  const req = reqById(UI.selected.reqId);
  if(!req) return emptyState(icoSvg('doc'),"No Request Selected","Choose a request to view.","");
  const p = petById(req.petId), o = userById(req.ownerId);
  const staffMode = currentUser().role !== "owner";

  let eligBlock;
  if(!req.eligibility){
    eligBlock = staffMode
      ? `<div class="card"><h3>Decision Tree-Based Eligibility Classification</h3><p style="font-size:0.85rem;color:var(--body);">No assessment has been run yet.</p><button class="btn-red" id="run-assessment-btn">Run Assessment</button></div>`
      : `<div class="card"><h3>Decision Tree-Based Eligibility Classification</h3><p class="empty">Assessment pending.</p></div>`;
  } else {
    const cls = req.eligibility.classification || (req.eligibility.eligible ? "ELIGIBLE" : "NOT ELIGIBLE");
    const bannerClass = cls==="ELIGIBLE" ? "eligible" : cls==="NOT ELIGIBLE" ? "noteligible" : "forreview";
    const failed = req.eligibility.criteria.filter(c=>c.result==="FAIL");
    const review = req.eligibility.criteria.filter(c=>c.result==="REVIEW");
    eligBlock = `
    <div class="card"><h3>Decision Tree-Based Eligibility Classification</h3>
      <div class="kv" style="margin-bottom:12px;">
        <div><div class="k">Request ID</div><div class="v">${req.id}</div></div>
        <div><div class="k">Pet</div><div class="v">${p?esc(p.name):"—"}</div></div>
        <div><div class="k">Owner</div><div class="v">${o?esc(o.name):"—"}</div></div>
        <div><div class="k">Service</div><div class="v">${esc(req.service)}</div></div>
        <div><div class="k">Assessment Date</div><div class="v">${fmtDate(req.eligibility.assessedDate)}</div></div>
        <div><div class="k">Assessed by</div><div class="v">${esc(req.eligibility.assessedByName||"—")}</div></div>
      </div>
      ${renderDecisionTree(req.eligibility.decisionPath || [])}
      <div class="result-banner ${bannerClass}">
        <div style="font-size:0.68rem;font-weight:800;letter-spacing:0.05em;color:var(--muted);text-transform:uppercase;">System Recommendation</div>
        <div class="status-line">${cls}</div>
        <div class="reason">Based on the configured eligibility criteria for ${esc(req.service)}.</div>
        ${failed.length ? `<div class="reason" style="margin-top:8px;text-align:left;"><b>Failed Criteria:</b><br>${failed.map(f=>`${esc(f.label)} — ${esc(f.basis)}`).join("<br>")}</div>` : ""}
        ${review.length ? `<div class="reason" style="margin-top:8px;text-align:left;"><b>Requires Verification:</b><br>${review.map(f=>`${esc(f.label)} — ${esc(f.basis)}`).join("<br>")}</div>` : ""}
      </div>
      <div class="support-note">PetCARE provides an eligibility recommendation to support CVO staff review. Final service approval remains with authorized CVO personnel.</div>
    </div>`;
  }

  const cls = req.eligibility ? (req.eligibility.classification || (req.eligibility.eligible?"ELIGIBLE":"NOT ELIGIBLE")) : null;
  const decisionBlock = `
  <div class="card">
    <h3>Final CVO Staff Decision</h3>
    <div class="decision-box">
      <div class="decision-title">Status</div>
      <div class="decision-status" style="color:${req.staffDecision==='Approved'?'var(--green)':req.staffDecision==='Rejected'?'var(--danger)':'var(--amber)'};">${req.staffDecision.toUpperCase()}</div>
      ${req.decidedByName?`<p class="form-note">Decision recorded by <b>${esc(req.decidedByName)}</b>${req.decisionDate?` on ${fmtDate(req.decisionDate)}`:""}.</p>`:""}
      ${staffMode && req.staffDecision==="Pending" ? `
        <div style="display:flex;gap:10px;margin-top:6px;flex-wrap:wrap;">
          <button class="btn-red" style="flex:1;" id="req-approve-btn" ${!req.eligibility?"disabled":""}>Approve</button>
          <button class="btn-sm danger" style="flex:1;padding:10px;" id="req-reject-btn" ${!req.eligibility?"disabled":""}>Reject</button>
        </div>
        ${cls==="FOR REVIEW" ? `<button class="btn-line" style="width:100%;margin-top:8px;" id="req-verify-btn">Request Verification</button>` : ""}
        ${!req.eligibility ? `<p style="font-size:0.7rem;color:var(--muted);margin-top:8px;">Run the eligibility assessment before recording a staff decision.</p>` : ""}
      ` : ""}
      ${req.staffDecision==="Rejected" && req.rejectionReason ? `<p style="font-size:0.78rem;color:var(--body);margin-top:8px;">Reason: ${esc(req.rejectionReason)}</p>` : ""}
      ${req.staffDecision==="Verification Requested" ? `<p style="font-size:0.78rem;color:var(--body);margin-top:8px;">CVO staff has requested additional verification from the resident before a final decision is made.</p>` : ""}
    </div>
    <div class="support-note">Decision Tree result = system recommendation. CVO Staff decision = final administrative/veterinary decision. These remain separate.</div>
  </div>`;

  const svcRecordBlock = (staffMode && req.staffDecision==="Approved") ? `
    <div class="card"><h3>Service Record</h3>
      ${req.serviceRecord ? `<div class="kv">
          <div><div class="k">Date</div><div class="v">${fmtDate(req.serviceRecord.date)}</div></div>
          <div><div class="k">Staff</div><div class="v">${esc(req.serviceRecord.staff)}</div></div>
        </div>` : `<p style="font-size:0.85rem;color:var(--body);">Approval does not mean the service has been completed. Record the actual service from Services &amp; Records.</p><button class="btn-red" data-nav="servicesrecords" data-req="${req.id}">Go to Services &amp; Records</button>`}
    </div>` : "";

  return `
  <div class="page-head"><div><div class="crumb">Service Requests</div><h2>Request ${req.id}</h2></div></div>
  <div class="two-col">
    <div class="col-stack">
      <div class="card"><h3>Request Summary</h3>
        <div class="kv">
          <div><div class="k">Pet</div><div class="v">${p?esc(p.name):"—"}</div></div>
          <div><div class="k">Owner</div><div class="v">${o?esc(o.name):"—"}</div></div>
          <div><div class="k">Service</div><div class="v">${esc(req.service)}</div></div>
          <div><div class="k">Barangay</div><div class="v">${p?esc(p.barangay):"—"}</div></div>
          <div><div class="k">Submitted</div><div class="v">${fmtDate(req.submittedDate)}</div></div>
          <div><div class="k">Remarks</div><div class="v">${esc(req.remarks||"—")}</div></div>
        </div>
      </div>
      ${eligBlock}
      ${svcRecordBlock}
    </div>
    <div class="col-stack">
      <div class="card"><h3>Request Timeline</h3>${renderTimeline(requestTimelineSteps(req))}</div>
      ${decisionBlock}
    </div>
  </div>`;
}

function runAssessment(reqId){
  // The server runs the decision tree with the criteria currently enabled and stores the result.
  runAction(() => Api.post(`/api/service-requests/${encodeURIComponent(reqId)}/assess`), () => render());
}
function approveRequestModal(reqId){
  const req = reqById(reqId), pet = petById(req.petId), owner = userById(req.ownerId);
  openModal(`
  <div class="modal-box">
    <h3>Confirm Approval</h3>
    <div class="modal-kv kv">
      <div><div class="k">Request ID</div><div class="v">${req.id}</div></div>
      <div><div class="k">Pet</div><div class="v">${esc(pet.name)}</div></div>
      <div><div class="k">Owner</div><div class="v">${esc(owner.name)}</div></div>
      <div><div class="k">Service</div><div class="v">${esc(req.service)}</div></div>
      <div><div class="k">System Recommendation</div><div class="v">${req.eligibility.classification || (req.eligibility.eligible?"ELIGIBLE":"NOT ELIGIBLE")}</div></div>
      <div><div class="k">Final Staff Decision</div><div class="v">APPROVE</div></div>
    </div>
    <div class="modal-actions">
      <button class="btn-line" id="m-cancel">Cancel</button>
      <button class="btn-red" id="m-confirm-approve-req">Confirm Approval</button>
    </div>
  </div>`);
  UI._pendingReqId = reqId;
}
function confirmApproveRequest(){
  const reqId = UI._pendingReqId;
  runAction(() => Api.post(`/api/service-requests/${encodeURIComponent(reqId)}/decision`, {decision:"Approved"}),
    () => { closeModal(); showToast(`Request ${reqId} approved.`); });
}
function rejectRequestModal(reqId){
  openModal(`
  <div class="modal-box">
    <h3>Reject Request</h3>
    ${field("Rejection Reason *", `<textarea id="req-reject-reason" rows="3"></textarea>`)}
    <div class="modal-actions">
      <button class="btn-line" id="m-cancel">Cancel</button>
      <button class="btn-sm danger" style="flex:1;padding:10px;" id="m-confirm-reject-req">Confirm Rejection</button>
    </div>
  </div>`);
  UI._pendingReqId = reqId;
}
function confirmRejectRequest(reason){
  if(!reason || !reason.trim()){ showToast("Please enter a rejection reason."); return; }
  const reqId = UI._pendingReqId;
  runAction(() => Api.post(`/api/service-requests/${encodeURIComponent(reqId)}/decision`, {decision:"Rejected", reason:reason.trim()}),
    () => { closeModal(); showToast(`Request ${reqId} rejected.`); });
}
function requestVerification(reqId){
  runAction(() => Api.post(`/api/service-requests/${encodeURIComponent(reqId)}/decision`, {decision:"Verification Requested"}),
    () => { showToast(`Verification requested for ${reqId}.`); render(); });
}
