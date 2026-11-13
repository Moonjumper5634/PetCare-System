/* PetCARE — Page: walkin */

/* ---------------- WALK-IN RESIDENT WORKFLOW ---------------- */
function newWalkinState(){ return {stage:"start", residentId:null, searchQuery:"", createDraft:{name:"",mobile:"",email:"",address:"",barangay:BARANGAYS[0]}}; }
function screenWalkin(){
  if(!UI.wiz.walkin) UI.wiz.walkin = newWalkinState();
  const w = UI.wiz.walkin;

  if(w.stage==="start"){
    return `
    <div class="page-head"><div><div class="crumb">Services</div><h2>Walk-in Resident</h2></div></div>
    <div class="card" style="max-width:520px;margin:0 auto;text-align:center;">
      <p style="font-size:0.88rem;color:var(--body);">Does the resident already have a PetCARE account?</p>
      <div style="display:flex;gap:10px;">
        <button class="btn-red" style="flex:1;" id="wi-has-account">Resident Has an Account</button>
        <button class="btn-line" style="flex:1;" id="wi-no-account">Resident Does Not Have an Account</button>
      </div>
    </div>`;
  }
  if(w.stage==="search"){
    const q = (w.searchQuery||"").toLowerCase();
    const results = q ? S.users.filter(u=>u.role==="owner" && (u.name.toLowerCase().includes(q) || (u.mobile||"").includes(q) || u.email.toLowerCase().includes(q))) : [];
    return `
    <div class="page-head"><div><div class="crumb">Walk-in Resident</div><h2>Search Resident</h2></div></div>
    <div class="card" style="max-width:560px;margin:0 auto;">
      ${field("Name / Mobile Number / Email", `<input id="wi-search-input" value="${esc(w.searchQuery)}" placeholder="Search resident...">`)}
      <button class="btn-red" id="wi-search-btn">Search</button>
      <div style="margin-top:16px;">
        ${results.length ? results.map(u=>`<div class="checklist-row"><span><b>${esc(u.name)}</b> — ${esc(u.barangay)} — ${esc(u.mobile)}</span><button class="btn-sm solid" data-wiselect="${u.id}">Select</button></div>`).join("")
        : (q ? `<p class="empty">No matching resident found.</p>` : "")}
      </div>
      <div class="wizard-nav"><button class="btn-line" id="wi-back-start">Back</button></div>
    </div>`;
  }
  if(w.stage==="residentFound"){
    const u = userById(w.residentId);
    return `
    <div class="page-head"><div><div class="crumb">Walk-in Resident</div><h2>Resident Information</h2></div></div>
    <div class="card" style="max-width:520px;margin:0 auto;">
      <div class="kv">
        <div><div class="k">Name</div><div class="v">${esc(u.name)}</div></div>
        <div><div class="k">Mobile</div><div class="v">${esc(u.mobile)}</div></div>
        <div><div class="k">Email</div><div class="v">${esc(u.email)}</div></div>
        <div><div class="k">Barangay</div><div class="v">${esc(u.barangay)}</div></div>
      </div>
      <div class="wizard-nav"><button class="btn-red" id="wi-continue-to-pet">Continue to Pet Registration</button></div>
    </div>`;
  }
  if(w.stage==="create"){
    const d = w.createDraft;
    return `
    <div class="page-head"><div><div class="crumb">Walk-in Resident</div><h2>Create Resident Account</h2></div></div>
    <div class="card" style="max-width:520px;margin:0 auto;">
      ${field("Name", `<input id="wi-c-name" value="${esc(d.name)}">`)}
      ${field("Mobile Number", `<input id="wi-c-mobile" value="${esc(d.mobile)}">`)}
      ${field("Email", `<input id="wi-c-email" value="${esc(d.email)}">`)}
      ${field("Address", `<input id="wi-c-address" value="${esc(d.address)}">`)}
      ${field("Barangay", `<select id="wi-c-barangay">${BARANGAYS.map(b=>`<option ${d.barangay===b?"selected":""}>${b}</option>`).join("")}</select>`)}
      <div class="wizard-nav"><button class="btn-line" id="wi-back-start2">Back</button><button class="btn-red" id="wi-create-account">Create Account</button></div>
    </div>`;
  }
  if(w.stage==="created"){
    const u = userById(w.residentId);
    return `
    <div class="page-head"><div><div class="crumb">Walk-in Resident</div><h2>Account Created</h2></div></div>
    <div class="card" style="max-width:520px;margin:0 auto;text-align:center;">
      <div style="color:var(--green);">${icoSvg('check',44)}</div>
      <p style="font-size:0.88rem;color:var(--body);">Resident account created successfully for <b>${esc(u.name)}</b>.</p>
      ${w.tempPassword ? `<div class="decision-box" style="margin:14px 0;">
        <div class="decision-title">Temporary password (shown once)</div>
        <div style="font-size:1.35rem;font-family:monospace;letter-spacing:1px;margin:6px 0;">${esc(w.tempPassword)}</div>
        <p style="font-size:0.78rem;color:var(--body);margin:0;">Give this to the resident. They must change it the first time they sign in.<br>
        Sign-in email: <b>${esc(w.tempEmail || u.email)}</b>${String(w.tempEmail||"").endsWith("@walk-in.invalid") ? " (placeholder: no email was given, so this account cannot use Forgot Password)" : ""}</p>
      </div>` : ""}
      <button class="btn-red" id="wi-continue-to-pet">Continue to Pet Registration</button>
    </div>`;
  }
  if(w.stage==="pet"){
    return screenPetRegistration(w.residentId);
  }
  if(w.stage==="service"){
    return screenRequestService(w.residentId);
  }
  if(w.stage==="eligibility"){
    return screenReqDetail();
  }
  if(w.stage==="record"){
    return screenServicesRecords();
  }
  if(w.stage==="done"){
    return `
    <div class="page-head"><div><div class="crumb">Walk-in Resident</div><h2>Walk-in Process Completed</h2></div></div>
    <div class="card" style="max-width:520px;margin:0 auto;text-align:center;">
      <div style="color:var(--green);">${icoSvg('check',44)}</div>
      <p style="font-size:0.88rem;color:var(--body);">The walk-in resident's pet registration and service request have been fully processed.</p>
      <button class="btn-red" id="wi-start-new">Start a New Walk-in</button>
    </div>`;
  }
  return "";
}
