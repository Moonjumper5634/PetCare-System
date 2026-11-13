/* DogCARE — Page: dashboard */

function screenDashboardStaff(){
  const regsPending = S.registrations.filter(r=>r.status==="Pending").length;
  const reqsOpen = S.requests.filter(r=>r.status!=="Completed" && r.staffDecision!=="Rejected").length;
  const reqsNeedAssessment = S.requests.filter(r=>!r.eligibility).length;
  const reqsNeedDecision = S.requests.filter(r=>r.eligibility && r.staffDecision==="Pending").length;
  const adoptPending = S.adoptions.filter(a=>a.status==="Interview Required" || a.status==="Interview Completed").length;
  const completedToday = S.requests.filter(r=>r.status==="Completed" && r.serviceRecord && r.serviceRecord.date===todayISO()).length;

  const recentReqs = [...S.requests].sort((a,b)=>b.submittedDate.localeCompare(a.submittedDate)).slice(0,6);
  const recentRegs = [...S.registrations].filter(r=>r.status==="Pending").slice(0,6);

  return `
  <div class="page-head"><div><div class="crumb">Home</div><h2>Dashboard</h2></div></div>
  <div class="stat-grid">
    <div class="stat-card"><div class="num">${regsPending}</div><div class="lbl">Pending Pet Registrations</div></div>
    <div class="stat-card"><div class="num">${reqsOpen}</div><div class="lbl">Open Service Requests</div></div>
    <div class="stat-card"><div class="num">${reqsNeedAssessment}</div><div class="lbl">Needing Eligibility Assessment</div></div>
    <div class="stat-card"><div class="num">${reqsNeedDecision}</div><div class="lbl">Needing Staff Decision</div></div>
    <div class="stat-card"><div class="num">${adoptPending}</div><div class="lbl">Pending Adoption Applications</div></div>
    <div class="stat-card"><div class="num">${completedToday}</div><div class="lbl">Services Completed Today</div></div>
  </div>
  <div class="two-col">
    <div class="card">
      <h3>Pending Pet Registrations</h3>
      ${recentRegs.length? recentRegs.map(r=>{const p=petById(r.petId);return `<div class="checklist-row"><span><b>${p?p.name:"—"}</b> — ${userById(r.ownerId)?.name||"—"}</span>${registrationStatusPill(r.status)}</div>`;}).join("")
        : emptyState(icoSvg('doc'),"No Pending Registrations","All pet registrations have been reviewed.","")}
      <div style="margin-top:12px;text-align:right;"><button class="btn-line" data-nav="petregistrations">View All</button></div>
    </div>
    <div class="card">
      <h3>Recent Service Requests</h3>
      ${recentReqs.length? `<table><thead><tr><th>Request ID</th><th>Pet</th><th>Service</th><th>Status</th></tr></thead><tbody>
        ${recentReqs.map(r=>`<tr><td>${r.id}</td><td>${petById(r.petId)?.name||"—"}</td><td>${esc(r.service)}</td><td>${statusPill(r.staffDecision==="Rejected"?"Rejected":r.status)}</td></tr>`).join("")}
        </tbody></table>` : emptyState(icoSvg('doc'),"No Requests Yet","Service requests will appear here.","")}
      <div style="margin-top:12px;text-align:right;"><button class="btn-line" data-nav="servicerequests">View All</button></div>
    </div>
  </div>`;
}
