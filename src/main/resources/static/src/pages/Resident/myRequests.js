/* PetCARE — Page: myRequests */

/* ---------------- OWNER: MY REQUESTS ---------------- */
function screenMyRequests(){
  const u = currentUser();
  const regs = S.registrations.filter(r=>r.ownerId===u.id).map(r=>({type:"Registration", id:r.id, petId:r.petId, service:"Pet Registration", date:r.submittedDate, status:r.status, kind:"reg"}));
  const reqs = S.requests.filter(r=>r.ownerId===u.id).map(r=>({type:"Service", id:r.id, petId:r.petId, service:r.service, date:r.submittedDate, status: requestDisplayStatus(r), kind:"req"}));
  const rows = [...regs, ...reqs].sort((a,b)=>b.date.localeCompare(a.date));
  return `
  <div class="page-head"><div><div class="crumb">Account</div><h2>My Requests</h2></div></div>
  <div class="card">
    ${rows.length ? `<table><thead><tr><th>Request ID</th><th>Type</th><th>Pet</th><th>Service</th><th>Date</th><th>Status</th><th>Action</th></tr></thead>
    <tbody>${rows.map(r=>{const p=petById(r.petId);
      return `<tr><td>${r.id}</td><td><span class="type-tag">${r.type}</span></td><td>${p?esc(p.name):"—"}</td><td>${esc(r.service)}</td><td>${fmtDate(r.date)}</td><td>${r.kind==='reg'?registrationStatusPill(r.status):statusPill(r.status)}</td>
      <td><button class="btn-sm" data-nav="${r.kind==='reg'?'regdetail':'reqdetail'}" data-${r.kind==='reg'?'reg':'req'}="${r.id}">View Details</button></td></tr>`;}).join("")}</tbody></table>`
    : emptyState(icoSvg('doc'),"No Requests","You haven't submitted a service request yet.",`<button class="btn-red" data-nav="requestservice">Request a Service</button>`)}
  </div>`;
}
