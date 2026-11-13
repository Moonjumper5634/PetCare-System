/* PetCARE — Page: auditLogs */

/* ---------------- ADMIN: AUDIT LOGS ---------------- */
function screenAuditLogs(){
  const pg=paged(S.audit,"audit",20), rows=pg.items;
  return `
  <div class="page-head"><div><div class="crumb">Administration</div><h2>Action History</h2><p style="font-size:0.8rem;color:var(--body);margin:6px 0 0;max-width:720px;">Administrator-only history showing the named account responsible for each verification, decision, and record change.</p></div></div>
  <div class="card">
    ${rows.length ? `<table><thead><tr><th>Timestamp</th><th>User</th><th>Action</th><th>Module</th><th>Reference ID</th><th>Description</th></tr></thead>
    <tbody>${rows.map(a=>`<tr><td>${fmtDateTime(a.datetime)}</td><td>${esc(a.user)}</td><td>${pill("gray:"+a.action)}</td><td>${esc(a.module)}</td><td><button class="reference-link" data-openref="${esc(a.refId)}" data-refmodule="${esc(a.module)}">${esc(a.refId)}</button></td><td>${esc(a.description)}</td></tr>`).join("")}</tbody></table>`
    : emptyState(icoSvg('folder'),"No Activity Yet","Named user actions performed in the system will appear here.","")}${pager(pg)}
  </div>`;
}
