/* DogCARE — Page: accountPages */

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
    <h3>Edit Account Information</h3>
    <div class="two-field">${field("First name *",`<input id="profile-first" value="${esc(u.firstName||"")}">`)}${field("Middle name",`<input id="profile-middle" value="${esc(u.middleName||"")}">`)}</div>
    ${field("Last name *",`<input id="profile-last" value="${esc(u.lastName||"")}">`)}
    ${field("Email",`<input value="${esc(u.email)}" disabled>`)}
    ${field("Mobile",`<input id="profile-mobile" value="${esc(u.mobile||"")}">`)}
    ${field("Address",`<input id="profile-address" value="${esc(u.houseStreet||"")}">`)}
    ${u.role==="owner"?field("Barangay *",`<select id="profile-barangay">${BARANGAYS.map(b=>`<option ${u.barangay===b?"selected":""}>${b}</option>`).join("")}</select>`):""}
    <div class="form-note">Role: ${roleLabel} · Status: ${u.status}. Contact the administrator to change your sign-in email or account role.</div>
    <button class="btn-red" id="profile-save-info">Save Profile</button>
  </div>
  <div class="card" style="max-width:560px;">
    <h3>Change Password</h3>
    ${field("Current Password", `<input id="pw-current" type="password" placeholder="••••••••" autocomplete="current-password">`)}
    ${field("New Password", `<input id="pw-new" type="password" placeholder="••••••••" autocomplete="new-password">`)}
    ${field("Confirm New Password", `<input id="pw-confirm" type="password" placeholder="••••••••" autocomplete="new-password">`)}
    <button class="btn-red" id="profile-save-pw">Save Changes</button>
  </div>`;
}
async function saveProfileInfo(){
  const result=await AuthService.updateProfile({
    firstName:document.getElementById("profile-first").value, middleName:document.getElementById("profile-middle").value,
    lastName:document.getElementById("profile-last").value, mobile:document.getElementById("profile-mobile").value,
    houseStreet:document.getElementById("profile-address").value, barangay:document.getElementById("profile-barangay")?.value||null
  });
  showToast(result.ok?"Profile updated.":result.error); if(result.ok) render();
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
