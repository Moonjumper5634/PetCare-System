/* PetCARE — Page: dashboard */

/* ---------------- DASHBOARDS ---------------- */
function screenDashboardOwner(){
  const u = currentUser();
  const hour = new Date().getHours();
  const greeting = hour < 12 ? "Good morning" : hour < 18 ? "Good afternoon" : "Good evening";
  const mine = ownerPets(u.id);
  const myRequests = S.requests.filter(r=>r.ownerId===u.id);
  const myRegs = S.registrations.filter(r=>r.ownerId===u.id);
  const pendingCount = myRequests.filter(r=>r.status!=="Completed" && r.staffDecision!=="Rejected").length
                      + myRegs.filter(r=>r.status==="Pending").length;
  const completedCount = myRequests.filter(r=>r.status==="Completed").length;
  const unread = unreadCount(u.id);
  const myNotifs = S.notifications.filter(n=>n.userId===u.id).slice(0,3);

  const rows = [
    ...myRegs.map(r=>({id:r.id, pet:petById(r.petId)?.name||"—", service:"Pet Registration", date:r.submittedDate, status:r.status, kind:"reg"})),
    ...myRequests.map(r=>({id:r.id, pet:petById(r.petId)?.name||"—", service:r.service, date:r.submittedDate, status: r.staffDecision==="Rejected"?"Rejected":r.status, kind:"req"})),
  ].sort((a,b)=> b.date.localeCompare(a.date)).slice(0,6);

  return `
  <div class="page-head"><div><div class="crumb">Home</div><h2>${greeting}, ${esc(u.name)}</h2></div></div>
  <p style="font-size:0.82rem;color:var(--body);margin-top:-10px;margin-bottom:18px;">Here's an overview of your pets and veterinary service requests.</p>
  <div class="stat-grid">
    <div class="stat-card"><div class="num">${mine.length}</div><div class="lbl">Registered Pets</div></div>
    <div class="stat-card"><div class="num">${pendingCount}</div><div class="lbl">Pending Requests</div></div>
    <div class="stat-card"><div class="num">${completedCount}</div><div class="lbl">Completed Services</div></div>
    <div class="stat-card"><div class="num">${unread}</div><div class="lbl">Unread Notifications</div></div>
  </div>
  <div class="card">
    <h3>Quick Actions</h3>
    <div style="display:flex;gap:10px;flex-wrap:wrap;">
      <button class="btn-red" data-nav="petregistration">Register a Pet</button>
      <button class="btn-line" data-nav="requestservice">Request a Service</button>
      <button class="btn-line" data-nav="mypets">View My Pets</button>
      <button class="btn-line" data-nav="adopt">Adopt a Pet</button>
    </div>
  </div>
  <div class="two-col">
    <div class="card">
      <h3>Recent Requests</h3>
      ${rows.length ? `<table><thead><tr><th>Request ID</th><th>Pet</th><th>Service</th><th>Date</th><th>Status</th><th>Action</th></tr></thead>
      <tbody>${rows.map(r=>`<tr><td>${r.id}</td><td>${esc(r.pet)}</td><td>${esc(r.service)}</td><td>${fmtDate(r.date)}</td><td>${r.kind==='reg'?registrationStatusPill(r.status):statusPill(r.status)}</td>
        <td><button class="btn-sm" data-nav="myrequests" data-openreq="${r.id}">View Details</button></td></tr>`).join("")}</tbody></table>`
      : emptyState(icoSvg('doc'),"No Requests","You haven't submitted a service request yet.",`<button class="btn-red" data-nav="requestservice">Request a Service</button>`)}
    </div>
    <div class="card">
      <h3>Recent Notifications</h3>
      ${myNotifs.length ? myNotifs.map(n=>`<div class="notif-item ${n.read?"read":""}"><div class="notif-dot"></div><div><div class="notif-title">${esc(n.title)}</div><div class="notif-body">${esc(n.body)}</div><div class="notif-date">${fmtDate(n.date)}</div></div></div>`).join("")
      : emptyState(icoSvg('bell'),"No Notifications","You're all caught up.","")}
    </div>
  </div>`;
}
