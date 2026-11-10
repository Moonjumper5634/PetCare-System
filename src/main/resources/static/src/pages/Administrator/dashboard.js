/* PetCARE — Page: dashboard */

function screenDashboardAdmin(){
  const totalPets = S.pets.length;
  const totalUsers = S.users.filter(u=>u.role==="owner").length;
  const totalRequests = S.requests.length;
  const totalAdoptions = S.adoptions.length;
  const totalAssessments = S.requests.filter(r=>r.eligibility).length;
  const activeUsers = S.users.filter(u=>u.status==="Active").length;
  return `
  <div class="page-head"><div><div class="crumb">Home</div><h2>Dashboard</h2></div></div>
  <div class="stat-grid">
    <div class="stat-card"><div class="num">${totalPets}</div><div class="lbl">Registered Pets</div></div>
    <div class="stat-card"><div class="num">${totalUsers}</div><div class="lbl">Resident Accounts</div></div>
    <div class="stat-card"><div class="num">${totalRequests}</div><div class="lbl">Service Requests</div></div>
    <div class="stat-card"><div class="num">${totalAssessments}</div><div class="lbl">Eligibility Assessments Run</div></div>
    <div class="stat-card"><div class="num">${totalAdoptions}</div><div class="lbl">Adoption Applications</div></div>
    <div class="stat-card"><div class="num">${activeUsers}</div><div class="lbl">Active System Users</div></div>
  </div>
  <div class="two-col">
    <div class="card">
      <h3>System Users by Role</h3>
      <div class="checklist-row"><span>Residents</span><b>${S.users.filter(u=>u.role==="owner").length}</b></div>
      <div class="checklist-row"><span>CVO Staff</span><b>${S.users.filter(u=>u.role==="staff").length}</b></div>
      <div class="checklist-row"><span>Administrators</span><b>${S.users.filter(u=>u.role==="admin").length}</b></div>
    </div>
    <div class="card">
      <h3>Recent Audit Activity</h3>
      ${S.audit.slice(0,5).map(a=>`<div class="checklist-row"><span>${esc(a.description)}</span><span style="font-size:0.7rem;color:var(--muted);">${fmtDate(a.datetime)}</span></div>`).join("") || emptyState(icoSvg('folder'),"No Activity Yet","Audit log entries will appear here.","")}
      <div style="margin-top:12px;text-align:right;"><button class="btn-line" data-nav="auditlogs">View All</button></div>
    </div>
  </div>`;
}
