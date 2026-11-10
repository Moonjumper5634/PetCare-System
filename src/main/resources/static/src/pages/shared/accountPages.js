/* PetCARE — Page: accountPages */

/* ---------------- NOTIFICATIONS ---------------- */
function screenNotifications(){
  const u = currentUser();
  let rows;
  if(u.role === "owner"){
    rows = S.notifications.filter(n=>n.userId===u.id);
  } else {
    // Staff/Admin: show a system-wide feed derived from audit activity plus any notifications addressed to them
    rows = S.notifications.filter(n=>n.userId===u.id);
  }
  if(!rows.length && u.role!=="owner"){
    // fall back to recent audit-derived feed so the screen isn't empty when nothing is targeted at staff
    const feed = S.audit.slice(0,12).map(a=>({id:a.id, title:`${a.action} — ${a.module}`, body:a.description, date:a.datetime.slice(0,10), read:true}));
    rows = feed;
  }
  if(!rows.length){
    return `<div class="page-head"><div><div class="crumb">Account</div><h2>Notifications</h2></div></div>${emptyState(icoSvg('bell'),"No Notifications","You're all caught up.","")}`;
  }
  return `
  <div class="page-head"><div><div class="crumb">Account</div><h2>Notifications</h2></div></div>
  <div class="card">
    ${rows.map(n=>`<div class="notif-item ${n.read?"read":""}" ${n.id&&u.role==='owner'?`data-marknotif="${n.id}"`:""}>
      <div class="notif-dot"></div>
      <div><div class="notif-title">${esc(n.title)}</div><div class="notif-body">${esc(n.body)}</div><div class="notif-date">${fmtDate(n.date)}</div></div>
    </div>`).join("")}
  </div>`;
}

/* ---------------- PROFILE ---------------- */
function screenProfile(){
  const u = currentUser();
  const roleLabel = roleDisplay(u.role);
  return `
  <div class="page-head"><div><div class="crumb">Account</div><h2>My Profile</h2></div></div>
  <div class="card" style="max-width:560px;">
    <h3>Account Information</h3>
    <div class="kv">
      <div><div class="k">Name</div><div class="v">${esc(u.name)}</div></div>
      <div><div class="k">Role</div><div class="v">${roleLabel}</div></div>
      <div><div class="k">Email</div><div class="v">${esc(u.email)}</div></div>
      <div><div class="k">Mobile</div><div class="v">${esc(u.mobile||"—")}</div></div>
      ${u.barangay?`<div><div class="k">Barangay</div><div class="v">${esc(u.barangay)}</div></div>`:""}
      <div><div class="k">Status</div><div class="v">${statusPill(u.status)}</div></div>
    </div>
  </div>
  <div class="card" style="max-width:560px;">
    <h3>Change Password</h3>
    ${field("Current Password", `<input id="pw-current" type="password" placeholder="••••••••" autocomplete="current-password">`)}
    ${field("New Password", `<input id="pw-new" type="password" placeholder="••••••••" autocomplete="new-password">`)}
    ${field("Confirm New Password", `<input id="pw-confirm" type="password" placeholder="••••••••" autocomplete="new-password">`)}
    <button class="btn-red" id="profile-save-pw">Save Changes</button>
  </div>`;
}
async function saveProfilePassword(){
  const cur = document.getElementById("pw-current").value;
  const nw = document.getElementById("pw-new").value;
  const cf = document.getElementById("pw-confirm").value;
  if(nw !== cf){ showToast("New password and confirmation do not match."); return; }
  if(nw.length < 8 || !/[A-Za-z]/.test(nw) || !/[0-9]/.test(nw)){ showToast("New password must be at least 8 characters and contain a letter and a number."); return; }
  const result = await AuthService.changePassword(cur, nw);
  showToast(result.ok ? "Password updated." : result.error);
  if(result.ok) render();
}
