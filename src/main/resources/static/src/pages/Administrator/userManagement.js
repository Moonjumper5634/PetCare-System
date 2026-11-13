/* PetCARE — Page: userManagement */

/* ---------------- ADMIN: USER MANAGEMENT ---------------- */
function screenUsers(){
  const filter = UI.filters.userRole || "All";
  let rows = [...S.users];
  if(filter!=="All") rows = rows.filter(u=> filter==="Inactive" ? u.status==="Inactive" : u.role===({Resident:"owner","CVO Staff":"staff",Administrator:"admin"}[filter]));
  const pg=paged(rows,"users"); rows=pg.items;
  const roleFilters = ["All","Resident","CVO Staff","Administrator","Inactive"];
  return `
  <div class="page-head"><div><div class="crumb">Administration</div><h2>User Management</h2><p class="page-intro">Resident accounts come from public registration or walk-in encoding. Privileged accounts are created only by an administrator. Archive personnel accounts when staff leave; their names remain on historical actions.</p></div><button class="btn-red" id="add-personnel">Add Personnel</button></div>
  <div class="card">
    <div class="filter-bar">${roleFilters.map(r=>`<button class="btn-sm ${filter===r?"solid":""}" data-setfilter="userRole:${r}">${r}</button>`).join("")}</div>
    <table><thead><tr><th>User ID</th><th>Name</th><th>Role</th><th>Barangay</th><th>Status</th><th>Action</th></tr></thead>
    <tbody>${rows.map(u=>`<tr><td>${u.id}</td><td><b>${esc(u.name)}</b></td><td><span class="badge-role">${roleDisplay(u.role)}</span></td><td>${esc(u.barangay||"—")}</td><td>${statusPill(u.status)}</td>
      <td><button class="btn-sm" data-viewuser="${u.id}">View</button>
      ${u.status==="Active" ? `<button class="btn-sm danger" data-toggleuser="${u.id}">${u.role==="staff"?"Archive Staff Account":"Deactivate"}</button>` : u.status==="Inactive" ? `<button class="btn-sm solid" data-toggleuser="${u.id}">Reactivate</button>` : ""}
      </td></tr>`).join("")}</tbody></table>${pager(pg)}
  </div>`;
}
function addPersonnelModal(){
  openModal(`
  <form class="modal-box" id="personnel-form">
    <h3>Add CVO Personnel</h3>
    <p class="form-note">The user receives a one-time temporary password and must replace it at first sign-in.</p>
    ${field("Full name", `<input id="personnel-name" maxlength="160" autocomplete="name" required>`)}
    ${field("Official email", `<input id="personnel-email" type="email" maxlength="190" autocomplete="email" required>`)}
    ${field("Mobile (optional)", `<input id="personnel-mobile" maxlength="30" inputmode="tel">`)}
    <div class="form-grid-2">
      ${field("Role", `<select id="personnel-role" required><option value="staff">CVO Staff</option><option value="admin">Administrator</option></select>`)}
      ${field("Job title", `<input id="personnel-title" maxlength="80" placeholder="e.g. Veterinarian" required>`)}
    </div>
    <div class="security-notice">Administrator access is highly privileged. Create it only for an authorized system administrator.</div>
    <div class="modal-actions"><button type="button" class="btn-line" id="m-cancel">Cancel</button><button class="btn-red" type="submit">Create Account</button></div>
  </form>`);
}
function submitPersonnel(){
  const body = {name:document.getElementById("personnel-name").value.trim(), email:document.getElementById("personnel-email").value.trim(),
    mobile:document.getElementById("personnel-mobile").value.trim(), role:document.getElementById("personnel-role").value,
    jobTitle:document.getElementById("personnel-title").value.trim()};
  runAction(() => Api.post("/api/admin/personnel", body), data => {
    openModal(`<div class="modal-box"><h3>Account Created</h3><p>Give these credentials directly to the authorized personnel. The password is shown only once.</p>
      <div class="credential-box"><b>${esc(data.email)}</b><code>${esc(data.temporaryPassword)}</code></div>
      <div class="modal-actions"><button class="btn-red" id="m-cancel">Done</button></div></div>`);
  });
}
function toggleUserStatus(userId){
  if(!AuthService.hasRole("admin")) return;   // convenience only: the server enforces this
  const u = userById(userId);
  if(u.status !== "Active" && u.status !== "Inactive") return;
  const target = u.status === "Active" ? "Inactive" : "Active";
  runAction(() => Api.post(`/api/admin/users/${encodeURIComponent(userId)}/status`, {status:target}),
    () => { showToast(target==="Inactive"&&u.role==="staff"?`${u.name}'s staff account was archived. Historical actions were retained.`:`${u.name} is now ${target}.`); render(); });
}
function viewUserModal(userId){
  const u = userById(userId);
  openModal(`
  <div class="modal-box">
    <h3>${esc(u.name)}</h3>
    <div class="kv">
      <div><div class="k">Role</div><div class="v">${u.role}</div></div>
      <div><div class="k">Email</div><div class="v">${esc(u.email)}</div></div>
      <div><div class="k">Mobile</div><div class="v">${esc(u.mobile||"—")}</div></div>
      <div><div class="k">Barangay</div><div class="v">${esc(u.barangay||"—")}</div></div>
      <div><div class="k">Status</div><div class="v">${u.status}</div></div>
    </div>
    <div class="modal-actions"><button class="btn-line" style="flex:1;" id="m-cancel">Close</button></div>
  </div>`);
}
